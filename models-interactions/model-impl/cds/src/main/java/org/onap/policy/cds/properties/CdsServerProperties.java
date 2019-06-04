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

import com.google.common.base.Strings;
import java.util.Base64;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.parameters.ParameterRuntimeException;
import org.onap.policy.common.parameters.ValidationStatus;

@Getter
@Setter
@ToString
public class CdsServerProperties implements ParameterGroup {

    // Port range constants
    private static final int MIN_USER_PORT = 1024;
    private static final int MAX_USER_PORT = 65535;

    private static final String INVALID_PROP = "Invalid CDS property: ";
    private static final String SERVER_PROPERTIES_TYPE = "CDS gRPC Server Properties";

    // CDS carrier properties
    private String host;
    private int port;
    private String username;
    private String password;
    private int timeout;

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
        final GroupValidationResult result = new GroupValidationResult(this);
        // Check if CDS host IP or URI is provided
        if (Strings.isNullOrEmpty(getHost())) {
            result.setResult("host", ValidationStatus.INVALID, "must be specified as a string");
        }
        // Check if port is defined
        if (port < MIN_USER_PORT || port > MAX_USER_PORT) {
            result.setResult("port", ValidationStatus.INVALID, "must be specified as an integer range [1024, 65535]");
        }
        // Check if username is provided
        if (Strings.isNullOrEmpty(getUsername())) {
            result.setResult("username", ValidationStatus.INVALID, "must be specified as a string");
        }
        // Check if password is provided
        if (Strings.isNullOrEmpty(getPassword())) {
            result.setResult("password", ValidationStatus.INVALID, "must be specified as a string");
        }
        // Check if timeout is defined
        if (timeout <= 0) {
            result.setResult("timeout", ValidationStatus.INVALID, "must be specified as an integer greater than zero");
        }
        return result;
    }

    /**
     * Generate base64-encoded Authorization header from username and password.
     *
     * @return Base64 encoded string
     */
    public String getBasicAuth() {
        return Base64.getEncoder().encodeToString(String.format("%s:%s", getUsername(), getPassword()).getBytes());
    }
}
