/*
 * ============LICENSE_START=======================================================
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property.
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
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.models.sim.dmaap.parameters.CommonTestData;

/**
 * Common base class for rest server tests.
 */
public class CommonRestServer {
    public static final String NOT_ALIVE = "not alive";
    public static final String ALIVE = "alive";
    public static final String SELF = "self";
    public static final String NAME = "DMaaP Simulator";
    public static final String ENDPOINT_PREFIX = "events/";

    protected static final String CONFIG_FILE = "src/test/resources/parameters/TestConfigParams.json";

    @Getter
    private static int port;

    protected static String httpPrefix;

    /**
     * Allocates a port for the server, writes a config file.
     *
     * @throws Exception if an error occurs
     */
    public static void reconfigure() throws Exception {
        port = NetworkUtil.allocPort();

        httpPrefix = "http://localhost:" + port + "/";

        String json = new CommonTestData().getParameterGroupAsString(port);
        makeConfigFile(CONFIG_FILE, json);

        HttpServletServerFactoryInstance.getServerFactory().destroy();
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
