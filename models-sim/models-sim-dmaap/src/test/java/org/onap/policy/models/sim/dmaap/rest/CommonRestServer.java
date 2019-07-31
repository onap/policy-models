/*
 * ============LICENSE_START=======================================================
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.sim.dmaap.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import lombok.Getter;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.services.Registry;
import org.onap.policy.models.sim.dmaap.DmaapSimException;
import org.onap.policy.models.sim.dmaap.startstop.Main;
import org.onap.policy.sim.dmaap.parameters.CommonTestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common base class for rest server tests.
 */
public class CommonRestServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonRestServer.class);

    public static final String NOT_ALIVE = "not alive";
    public static final String ALIVE = "alive";
    public static final String SELF = "self";
    public static final String NAME = "DMaaP Simulator";
    public static final String ENDPOINT_PREFIX = "events/";

    @Getter
    private static int port;

    protected static String httpPrefix;

    private static Main main;

    /**
     * Allocates a port for the server, writes a config file, and then starts Main.
     *
     * @throws Exception if an error occurs
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        port = NetworkUtil.allocPort();

        httpPrefix = "http://localhost:" + port + "/";

        String json = new CommonTestData().getParameterGroupAsString(port);
        makeConfigFile("src/test/resources/parameters/TestConfigParams.json", json);

        HttpServletServerFactoryInstance.getServerFactory().destroy();

        startMain();
    }

    /**
     * Stops Main.
     */
    @AfterClass
    public static void teardownAfterClass() {
        try {
            if (main != null) {
                Main main2 = main;
                main = null;

                main2.shutdown();
            }

        } catch (DmaapSimException exp) {
            LOGGER.error("cannot stop main", exp);
        }
    }

    /**
     * Set up.
     *
     * @throws Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception {
        // restart, if not currently running
        if (main == null) {
            startMain();
        }
    }

    /**
     * Makes a parameter configuration file.
     * @param fileName name of the config file to be created
     * @param json json to be written to the file
     *
     * @throws Exception if an error occurs
     */
    protected static void makeConfigFile(String fileName, String json) throws Exception {
        File file = new File(fileName);
        file.deleteOnExit();

        try (FileOutputStream output = new FileOutputStream(file)) {
            output.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Starts the "Main".
     *
     * @throws Exception if an error occurs
     */
    private static void startMain() throws Exception {
        Registry.newRegistry();

        // make sure port is available
        if (NetworkUtil.isTcpPortOpen("localhost", port, 1, 1L)) {
            throw new IllegalStateException("port " + port + " is still in use");
        }

        final String[] simConfigParameters = {"-c", "src/test/resources/parameters/TestConfigParams.json"};

        main = new Main(simConfigParameters);

        if (!NetworkUtil.isTcpPortOpen("localhost", port, 6, 10000L)) {
            throw new IllegalStateException("server is not listening on port " + port);
        }
    }

    /**
     * Sends a request to an endpoint.
     *
     * @param endpoint the target endpoint
     * @return a request builder
     * @throws Exception if an error occurs
     */
    protected Invocation.Builder sendRequest(final String endpoint) throws Exception {
        return sendFqeRequest(httpPrefix + ENDPOINT_PREFIX + endpoint);
    }

    /**
     * Sends a request to a fully qualified endpoint.
     *
     * @param fullyQualifiedEndpoint the fully qualified target endpoint
     * @return a request builder
     */
    protected Invocation.Builder sendFqeRequest(final String fullyQualifiedEndpoint) {
        final Client client = ClientBuilder.newBuilder().build();

        client.property(ClientProperties.METAINF_SERVICES_LOOKUP_DISABLE, "true");
        client.register(GsonMessageBodyHandler.class);

        final WebTarget webTarget = client.target(fullyQualifiedEndpoint);

        return webTarget.request(MediaType.APPLICATION_JSON);
    }
}
