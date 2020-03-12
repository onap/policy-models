/*-
 * ============LICENSE_START=======================================================
 * sdc
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.UUID;

import lombok.Data;

@Data
public class Service implements Serializable {

    private static final long serialVersionUID = -1249276698549996806L;

    @SerializedName("serviceUuid")
    private UUID serviceUuid;

    @SerializedName("serviceInvariantUuid")
    private UUID serviceInvariantUuid;

    private String serviceName;
    private String serviceVersion;

    public Service() {
        // Empty Constructor
    }

    public Service(UUID uuid) {
        this.serviceUuid = uuid;
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
        this.serviceUuid = uuid;
        this.serviceInvariantUuid = invariantUuid;
        this.serviceName = name;
        this.serviceVersion = version;
    }

    /**
     * Constructor.
     *
     * @param service copy object
     */
    public Service(Service service) {
        this.serviceUuid = service.serviceUuid;
        this.serviceInvariantUuid = service.serviceInvariantUuid;
        this.serviceName = service.serviceName;
        this.serviceVersion = service.serviceVersion;
    }
}
