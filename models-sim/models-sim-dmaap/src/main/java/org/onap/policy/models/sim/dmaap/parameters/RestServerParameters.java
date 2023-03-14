/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019,2023 Nordix Foundation.
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

package org.onap.policy.models.sim.dmaap.parameters;

import java.util.Properties;
import lombok.Getter;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.parameters.ParameterGroupImpl;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.models.sim.dmaap.rest.CambriaMessageBodyHandler;
import org.onap.policy.models.sim.dmaap.rest.DmaapSimRestControllerV1;
import org.onap.policy.models.sim.dmaap.rest.TextMessageBodyHandler;

/**
 * Class to hold all parameters needed for rest server.
 */
@NotNull
@NotBlank
@Getter
public class RestServerParameters extends ParameterGroupImpl {
    private String host;

    @Min(value = 1)
    private int port;

    private String userName;

    private String password;

    private boolean useHttps;

    private boolean sniHostCheck;

    public RestServerParameters() {
        super(RestServerParameters.class.getSimpleName());
    }

    /**
     * Creates a set of properties, suitable for building a REST server, from the
     * parameters.
     *
     * @return a set of properties representing the given parameters
     */
    public Properties getServerProperties() {
        final var props = new Properties();
        props.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES, getName());

        final String svcpfx =
            PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + getName();

        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, getHost());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX,
            Integer.toString(getPort()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX,
            DmaapSimRestControllerV1.class.getName());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "false");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX, "false");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HTTPS_SUFFIX, Boolean.toString(isUseHttps()));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SNI_HOST_CHECK_SUFFIX,
            Boolean.toString(isSniHostCheck()));

        if (getUserName() != null && getPassword() != null) {
            props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_USERNAME_SUFFIX, getUserName());
            props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_AUTH_PASSWORD_SUFFIX, getPassword());
        }

        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER,
            String.join(",", CambriaMessageBodyHandler.class.getName(),
                GsonMessageBodyHandler.class.getName(),
                TextMessageBodyHandler.class.getName()));
        return props;
    }
}
