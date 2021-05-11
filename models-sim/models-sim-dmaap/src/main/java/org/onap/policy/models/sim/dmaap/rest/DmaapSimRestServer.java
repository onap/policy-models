/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.List;
import java.util.Properties;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.services.ServiceManagerContainer;
import org.onap.policy.models.sim.dmaap.parameters.RestServerParameters;

/**
 * Class to manage life cycle of DMaaP Simulator rest server.
 */
public class DmaapSimRestServer extends ServiceManagerContainer {

    private final List<HttpServletServer> servers;

    /**
     * Constructor for instantiating DmaapSimRestServer.
     *
     * @param restServerParameters the rest server parameters
     */
    public DmaapSimRestServer(final RestServerParameters restServerParameters) {
        this.servers = HttpServletServerFactoryInstance.getServerFactory()
                        .build(getServerProperties(restServerParameters));

        for (HttpServletServer server : this.servers) {
            addAction("REST " + server.getName(), server::start, server::stop);
        }
    }

    /**
     * Creates a set of properties, suitable for building a REST server, from the
     * parameters.
     *
     * @param restServerParameters parameters from which to build the properties
     * @return a set of properties representing the given parameters
     */
    public static Properties getServerProperties(RestServerParameters restServerParameters) {
        final var props = new Properties();
        props.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES, restServerParameters.getName());

        final String svcpfx =
                        PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + restServerParameters.getName();

        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, restServerParameters.getHost());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX,
                        Integer.toString(restServerParameters.getPort()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX,
                        DmaapSimRestControllerV1.class.getName());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "false");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX, "false");

        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER,
                        String.join(",", CambriaMessageBodyHandler.class.getName(),
                                        GsonMessageBodyHandler.class.getName(),
                                        TextMessageBodyHandler.class.getName()));
        return props;
    }
}
