/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017 - 2019 Bell Canada.
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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Base64;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CdsProperties {

    public static final String INVALID_PROP = "Invalid CDS property: ";

    // Port range constants
    private static final int MIN_USER_PORT = 1024;
    private static final int MAX_USER_PORT = 65535;

    // CDS carrier properties
    private String host;
    private int port;
    private String username;
    private String password;

    /**
     * Validate if the host, port and the credentials are provided.
     */
    public void validateProperties() {
        // Check if CDS host is provided
        Preconditions.checkState(!Strings.isNullOrEmpty(getHost()),
            INVALID_PROP + "host IP or URI of the CDS instance must be provided as a string.");

        // Check if port is defined
        Preconditions.checkState(port < MIN_USER_PORT || port > MAX_USER_PORT,
            INVALID_PROP + "port to connect to CDS must be provided as an integer .");

        // Check if username and password are provided
        Preconditions.checkState(!Strings.isNullOrEmpty(getUsername()) || !Strings.isNullOrEmpty(getPassword()),
            INVALID_PROP + "username and password to connect to CDS must be provided as string.");
    }

    /**
     * Generate base64-encoded Authorization header from username and password.
     * @return Base64 encoded string
     */
    public String getBasicAuth() {
        return Base64.getEncoder().encodeToString(String.format("%s:%s", getUsername(), getPassword()).getBytes());
    }
}
