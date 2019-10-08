/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada.
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

package org.onap.policy.cds.properties;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.parameters.ParameterRuntimeException;
import org.onap.policy.common.parameters.annotations.Max;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotNull;

@Getter
@Setter
@ToString
public class CdsServerProperties implements ParameterGroup {

    // Port range constants
    private static final int MIN_USER_PORT = 1024;
    private static final int MAX_USER_PORT = 65535;

    private static final String SERVER_PROPERTIES_TYPE = "CDS gRPC Server Properties";

    // CDS carrier properties

    // Request timeout in seconds
    @Min(value = 1)
    private int timeout;

    @Min(value = MIN_USER_PORT)
    @Max(value = MAX_USER_PORT)
    private int port;

    @NotNull
    private String host;

    @NotNull
    private String username;

    @NotNull
    private String password;


    @Override
    public String getName() {
        return SERVER_PROPERTIES_TYPE;
    }

    @Override
    public void setName(final String name) {
        throw new ParameterRuntimeException("The name of this ParameterGroup implementation is always " + getName());
    }

    @Override
    public GroupValidationResult validate() {
        return new GroupValidationResult(this);
    }

    /**
     * Generate base64-encoded Authorization header from username and password.
     *
     * @return Base64 encoded string
     */
    public String getBasicAuth() {
        String encodedAuth = Base64.getEncoder().encodeToString(
                String.format("%s:%s", getUsername(), getPassword()).getBytes(StandardCharsets.UTF_8));
        // Return encoded basic auth header
        return "Basic " + encodedAuth;
    }
}
