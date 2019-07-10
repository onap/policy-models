/*-
 * ============LICENSE_START=======================================================
 * sdc
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Service implements Serializable {

    private static final long serialVersionUID = -1249276698549996806L;

    private UUID serviceUuid;
    private UUID serviceInvariantUuid;
    private String serviceName;
    private String serviceVersion;

    public Service() {
        //Empty Constructor
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

    @Override
    public String toString() {
        return "Service [serviceUUID=" + serviceUuid + ", serviceInvariantUUID=" + serviceInvariantUuid
            + ", serviceName=" + serviceName + ", serviceVersion=" + serviceVersion + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serviceInvariantUuid == null) ? 0 : serviceInvariantUuid.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        result = prime * result + ((serviceUuid == null) ? 0 : serviceUuid.hashCode());
        result = prime * result + ((serviceVersion == null) ? 0 : serviceVersion.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Service other = (Service) obj;
        if (serviceInvariantUuid == null) {
            if (other.serviceInvariantUuid != null) {
                return false;
            }
        } else if (!serviceInvariantUuid.equals(other.serviceInvariantUuid)) {
            return false;
        }
        if (serviceName == null) {
            if (other.serviceName != null) {
                return false;
            }
        } else if (!serviceName.equals(other.serviceName)) {
            return false;
        }
        if (serviceUuid == null) {
            if (other.serviceUuid != null) {
                return false;
            }
        } else if (!serviceUuid.equals(other.serviceUuid)) {
            return false;
        }
        if (serviceVersion == null) {
            if (other.serviceVersion != null) {
                return false;
            }
        } else if (!serviceVersion.equals(other.serviceVersion)) {
            return false;
        }
        return true;
    }

}
