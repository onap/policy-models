/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019, 2022 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

package org.onap.policy.simulators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSink;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.endpoints.http.server.internal.JettyJerseyServer;
import org.onap.policy.common.endpoints.parameters.TopicParameterGroup;
import org.onap.policy.common.utils.coder.StandardCoder;

public class DmaapSimulatorTest {
    private static final int MAX_WAIT_SEC = 10;
    private static final String TOPIC = "my-topic";
    private static final String AUTH_TOPIC = "my-auth-topic";
    private static final String AUTH_PORT = "3903";

    private static final HashMap<String, String> savedValuesMap = new HashMap<>();

    /**
     * Messages from the topic are placed here by the endpoint.
     */
    private BlockingQueue<String> queue;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TopicEndpointManager.getManager().shutdown();
    }

    /**
     * Starts the simulator and the topic.
     *
     * @throws Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception {
        assertNotNull(Util.buildDmaapSim());
        setSystemProperties();
        assertNotNull(Util.buildDmaapSim("org/onap/policy/simulators/dmaap/AuthDmaapParameters.json"));

        String topicJson = Files.readString(
                new File("src/test/resources/org/onap/policy/simulators/dmaap/TopicParameters.json").toPath());
        topicJson = topicJson
                .replace("${port}", String.valueOf(Util.DMAAPSIM_SERVER_PORT))
                .replace("${authPort}", AUTH_PORT);

        TopicParameterGroup topicConfig = new StandardCoder().decode(topicJson, TopicParameterGroup.class);

        TopicEndpointManager.getManager().addTopics(topicConfig);
        TopicEndpointManager.getManager().start();

        queue = new LinkedBlockingQueue<>();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        clearSystemProperties();
    }

    @After
    public void tearDown() {
        TopicEndpointManager.getManager().shutdown();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    public void test() throws InterruptedException {
        // test basic DMaaP simulator (default port, http, no auth)
        sendAndPoll(TOPIC);
        // test custom DMaaP simulator (custom port, https, basic auth)
        sendAndPoll(AUTH_TOPIC);
    }

    /**
     * Sets up keystore and truststore for https test server.
     */
    private static void setSystemProperties() {
        String keyStoreSystemProperty = System.getProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME);
        if (keyStoreSystemProperty != null) {
            savedValuesMap.put(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME, keyStoreSystemProperty);
        }

        String keyStorePasswordSystemProperty =
                System.getProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME);
        if (keyStorePasswordSystemProperty != null) {
            savedValuesMap.put(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME,
                    keyStorePasswordSystemProperty);
        }

        String trustStoreSystemProperty = System.getProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME);
        if (trustStoreSystemProperty != null) {
            savedValuesMap.put(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME, trustStoreSystemProperty);
        }

        String trustStorePasswordSystemProperty =
                System.getProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME);
        if (trustStorePasswordSystemProperty != null) {
            savedValuesMap.put(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME,
                    trustStorePasswordSystemProperty);
        }

        System.setProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME, "src/test/resources/keystore-test");
        System.setProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME, "kstest");

        System.setProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME, "src/test/resources/keystore-test");
        System.setProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME, "kstest");
    }

    /**
     * Clears the keystore/truststore properties.
     */
    private static void clearSystemProperties() {
        if (savedValuesMap.containsKey(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME)) {
            System.setProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME,
                    savedValuesMap.get(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME));
            savedValuesMap.remove(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME);
        } else {
            System.clearProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PROPERTY_NAME);
        }

        if (savedValuesMap.containsKey(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME)) {
            System.setProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME,
                    savedValuesMap.get(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME));
            savedValuesMap.remove(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME);
        } else {
            System.clearProperty(JettyJerseyServer.SYSTEM_KEYSTORE_PASSWORD_PROPERTY_NAME);
        }

        if (savedValuesMap.containsKey(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME)) {
            System.setProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME,
                    savedValuesMap.get(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME));
            savedValuesMap.remove(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME);
        } else {
            System.clearProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PROPERTY_NAME);
        }

        if (savedValuesMap.containsKey(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME)) {
            System.setProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME,
                    savedValuesMap.get(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME));
            savedValuesMap.remove(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME);
        } else {
            System.clearProperty(JettyJerseyServer.SYSTEM_TRUSTSTORE_PASSWORD_PROPERTY_NAME);
        }
    }

    private void sendAndPoll(String topicName) throws InterruptedException {
        TopicEndpointManager.getManager().getDmaapTopicSource(topicName)
                .register((infra, topic, event) -> queue.add(event));

        DmaapTopicSink sink = TopicEndpointManager.getManager().getDmaapTopicSink(topicName);
        assertThat(queue.poll(1, TimeUnit.SECONDS)).isNull();

        sink.send("hello");
        assertEquals("hello", queue.poll(MAX_WAIT_SEC, TimeUnit.SECONDS));

        sink.send("world");
        assertEquals("world", queue.poll(MAX_WAIT_SEC, TimeUnit.SECONDS));
    }

}
