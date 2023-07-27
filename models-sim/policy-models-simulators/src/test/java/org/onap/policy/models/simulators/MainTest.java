/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.simulators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientConfigException;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.internal.JettyJerseyServer;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.security.SelfSignedKeyStore;

public class MainTest {
    private static final String PARAMETER_FILE = "simParameters.json";
    private static final String HOST = "localhost";
    private static final String EXPECTED_EXCEPTION = "expected exception";

    private static Map<String, String> savedValues;

    /**
     * Saves system properties.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws IOException, InterruptedException {
        savedValues = new HashMap<>();

        for (String prop : List.of(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME,
                        JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME,
                        JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME,
                        JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME)) {

            savedValues.put(prop, System.getProperty(prop));
        }

        System.setProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME, new SelfSignedKeyStore().getKeystoreName());
        System.setProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME,
                        SelfSignedKeyStore.KEYSTORE_PASSWORD);

        System.setProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME,
                        "src/main/resources/ssl/policy-truststore");
        System.setProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME,
                        SelfSignedKeyStore.KEYSTORE_PASSWORD);
    }

    /**
     * Restores system properties.
     */
    @AfterClass
    public static void tearDownAfterClass() {
        for (Entry<String, String> ent : savedValues.entrySet()) {
            if (ent.getValue() == null) {
                System.getProperties().remove(ent.getKey());
            } else {
                System.setProperty(ent.getKey(), ent.getValue());
            }
        }
    }

    /**
     * Shuts down the simulator instance.
     */
    @After
    public void tearDown() {
        Main main = Main.getInstance();
        if (main != null && main.isAlive()) {
            main.shutdown();
        }
    }

    @Test
    public void testConstructor() throws Exception {
        assertThatIllegalArgumentException().isThrownBy(() -> new Main("invalidDmaapProvider.json"))
                        .withMessage("invalid simulator parameters");
    }

    /**
     * Verifies that all of the simulators are brought up and that HTTPS works with at
     * least one of them.
     */
    @Test
    public void testMain() throws Exception {
        Main.main(new String[0]);
        assertTrue(Main.getInstance() == null || !Main.getInstance().isAlive());

        Main.main(new String[] {PARAMETER_FILE});

        // don't need to wait long, because buildXxx() does the wait for us
        for (int port : new int[] {6666, 6667, 6668, 6669, 6670, 6680}) {
            assertTrue("simulator on port " + port, NetworkUtil.isTcpPortOpen(HOST, port, 1, 100));
        }

        // it's sufficient to verify that one of the simulators works
        checkAai();
    }

    @Test
    public void testMainMinimalParameters() {
        Main.main(new String[] {"minParameters.json"});
        assertNotNull(Main.getInstance());
        assertTrue(Main.getInstance().isAlive());
    }

    private void checkAai() throws HttpClientConfigException {
        BusTopicParams params = BusTopicParams.builder().clientName("client").hostname(HOST).port(6666).useHttps(true)
                        .allowSelfSignedCerts(true).basePath("aai").build();
        HttpClient client = HttpClientFactoryInstance.getClientFactory().build(params);

        Response response = client.get("/v21/network/pnfs/pnf/demo-pnf");
        assertEquals(200, response.getStatus());

        String result = response.readEntity(String.class);
        assertThat(result).contains("model-123456");
    }

    /**
     * Tests readParameters() when the file cannot be found.
     */
    @Test
    public void testReadParametersNoFile() {
        assertThatIllegalArgumentException().isThrownBy(() -> new Main("missing-file.json"))
                        .withCauseInstanceOf(FileNotFoundException.class);
    }

    /**
     * Tests readParameters() when the json cannot be decoded.
     */
    @Test
    public void testReadParametersInvalidJson() throws CoderException {
        Coder coder = mock(Coder.class);
        when(coder.decode(any(String.class), any())).thenThrow(new CoderException(EXPECTED_EXCEPTION));

        assertThatIllegalArgumentException().isThrownBy(() -> new Main(PARAMETER_FILE) {
            @Override
            protected Coder makeCoder() {
                return coder;
            }
        }).withCauseInstanceOf(CoderException.class);
    }

    /**
     * Tests buildRestServer() when the server port is not open.
     */
    @Test
    public void testBuildRestServerNotOpen() {
        HttpServletServer server = mock(HttpServletServer.class);

        Main main = new Main(PARAMETER_FILE) {
            @Override
            protected HttpServletServer makeServer(Properties props) {
                return server;
            }

            @Override
            protected boolean isTcpPortOpen(String hostName, int port) throws InterruptedException {
                return false;
            }
        };

        assertThatThrownBy(main::start).hasCauseInstanceOf(IllegalStateException.class);
    }

    /**
     * Tests buildRestServer() when the port checker is interrupted.
     */
    @Test
    public void testBuildRestServerInterrupted() throws InterruptedException {
        HttpServletServer server = mock(HttpServletServer.class);

        Main main = new Main(PARAMETER_FILE) {
            @Override
            protected HttpServletServer makeServer(Properties props) {
                return server;
            }

            @Override
            protected boolean isTcpPortOpen(String hostName, int port) throws InterruptedException {
                throw new InterruptedException(EXPECTED_EXCEPTION);
            }
        };

        // run in another thread so we don't interrupt this thread
        LinkedBlockingQueue<RuntimeException> queue = new LinkedBlockingQueue<>();
        Thread thread = new Thread(() -> {
            try {
                main.start();
            } catch (RuntimeException e) {
                queue.add(e);
            }
        });

        thread.setDaemon(true);
        thread.start();

        RuntimeException ex = queue.poll(5, TimeUnit.SECONDS);
        assertNotNull(ex);
        assertTrue(ex.getCause() instanceof IllegalStateException);
        assertThat(ex.getCause()).hasMessageStartingWith("interrupted while building");
    }

    /**
     * Tests buildTopicServer() when the provider class is invalid.
     */
    @Test
    public void testBuildTopicServerInvalidProvider() {
        assertThatThrownBy(() -> new Main("invalidTopicServer.json").start())
                        .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Tests buildTopicServer() when the sink is missing.
     */
    @Test
    public void testBuildTopicServerNoSink() {
        assertThatThrownBy(() -> new Main("missingSink.json").start())
                        .hasCauseInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Tests buildTopicServer() when the sink is missing.
     */
    @Test
    public void testBuildTopicServerNoSource() {
        assertThatThrownBy(() -> new Main("missingSource.json").start())
                        .hasCauseInstanceOf(IllegalArgumentException.class);
    }
}
