/*-
 * ============LICENSE_START=======================================================
 * sdc
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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

package org.onap.policy.sdc;

import java.io.Serializable;
import java.util.UUID;
import lombok.Data;

@Data
public class Service implements Serializable {

    private static final long serialVersionUID = -1249276698549996806L;

    /*
     * Note the field names ending in "UUID" may not be changed without breaking the
     * interface, due to limitations in the YAML encoder/decoder.
     */
    private UUID serviceUUID;
    private UUID serviceInvariantUUID;
    private String serviceName;
    private String serviceVersion;

    public Service() {
        // Empty Constructor
    }

    public Service(UUID uuid) {
        this.serviceUUID = uuid;
    }

    public Service(String name) {
        this.serviceName = name;
    }

    /**
     * Constructor.
     *
     * @param uuid service id
     * @param invariantUuid service invariant id
     * @param name name
     * @param version version
     */
    public Service(UUID uuid, UUID invariantUuid, String name, String version) {
        this.serviceUUID = uuid;
        this.serviceInvariantUUID = invariantUuid;
        this.serviceName = name;
        this.serviceVersion = version;
    }

    /**
     * Constructor.
     *
     * @param service copy object
     */
    public Service(Service service) {
        this.serviceUUID = service.serviceUUID;
        this.serviceInvariantUUID = service.serviceInvariantUUID;
        this.serviceName = service.serviceName;
        this.serviceVersion = service.serviceVersion;
    }
}
