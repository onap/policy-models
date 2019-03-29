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

public class Service implements Serializable {

    private static final long serialVersionUID = -1249276698549996806L;
    
    private UUID        serviceUUID;
    private UUID        serviceInvariantUUID;
    private String  serviceName;
    private String  serviceVersion;
    
    public Service() {
        //Empty Constructor
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
     * @param invariantUUID service invariant id
     * @param name name
     * @param version version
     */
    public Service(UUID uuid, UUID invariantUUID, String name, String version) {
        this.serviceUUID = uuid;
        this.serviceInvariantUUID = invariantUUID;
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
    
    public UUID getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(UUID serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public UUID getServiceInvariantUUID() {
        return serviceInvariantUUID;
    }

    public void setServiceInvariantUUID(UUID serviceInvariantUUID) {
        this.serviceInvariantUUID = serviceInvariantUUID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceVersion() {
        return serviceVersion;
    }

    public void setServiceVersion(String serviceVersion) {
        this.serviceVersion = serviceVersion;
    }

    @Override
    public String toString() {
        return "Service [serviceUUID=" + serviceUUID + ", serviceInvariantUUID=" + serviceInvariantUUID
                + ", serviceName=" + serviceName + ", serviceVersion=" + serviceVersion + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((serviceInvariantUUID == null) ? 0 : serviceInvariantUUID.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        result = prime * result + ((serviceUUID == null) ? 0 : serviceUUID.hashCode());
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
        if (serviceInvariantUUID == null) {
            if (other.serviceInvariantUUID != null) {
                return false;
            }
        } else if (!serviceInvariantUUID.equals(other.serviceInvariantUUID)) {
            return false;
        }
        if (serviceName == null) {
            if (other.serviceName != null) {
                return false;
            }
        } else if (!serviceName.equals(other.serviceName)) {
            return false;
        }
        if (serviceUUID == null) {
            if (other.serviceUUID != null) {
                return false;
            }
        } else if (!serviceUUID.equals(other.serviceUUID)) {
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
