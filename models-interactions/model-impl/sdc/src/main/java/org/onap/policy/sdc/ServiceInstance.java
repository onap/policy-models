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
public class ServiceInstance implements Serializable {

    private static final long serialVersionUID = 6285260780966679625L;

    private UUID personaModelUuid;
    private UUID serviceUuid;
    private UUID serviceInstanceUuid;
    private UUID widgetModelUuid;
    private String widgetModelVersion;
    private String serviceName;
    private String serviceInstanceName;

    public ServiceInstance() {
        //Empty Constructor
    }

    /**
     * Constructor.
     *
     * @param instance copy object
     */
    public ServiceInstance(ServiceInstance instance) {
        if (instance == null) {
            return;
        }
        this.personaModelUuid = instance.personaModelUuid;
        this.serviceUuid = instance.serviceUuid;
        this.serviceInstanceUuid = instance.serviceInstanceUuid;
        this.widgetModelUuid = instance.widgetModelUuid;
        this.widgetModelVersion = instance.widgetModelVersion;
        this.serviceName = instance.serviceName;
        this.serviceInstanceName = instance.serviceInstanceName;
    }

    @Override
    public String toString() {
        return "ServiceInstance [personaModelUUID=" + personaModelUuid + ", serviceUUID=" + serviceUuid
            + ", serviceInstanceUUID=" + serviceInstanceUuid + ", widgetModelUUID=" + widgetModelUuid
            + ", widgetModelVersion=" + widgetModelVersion + ", serviceName=" + serviceName
            + ", serviceInstanceName=" + serviceInstanceName + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((personaModelUuid == null) ? 0 : personaModelUuid.hashCode());
        result = prime * result + ((serviceInstanceName == null) ? 0 : serviceInstanceName.hashCode());
        result = prime * result + ((serviceInstanceUuid == null) ? 0 : serviceInstanceUuid.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        result = prime * result + ((serviceUuid == null) ? 0 : serviceUuid.hashCode());
        result = prime * result + ((widgetModelUuid == null) ? 0 : widgetModelUuid.hashCode());
        result = prime * result + ((widgetModelVersion == null) ? 0 : widgetModelVersion.hashCode());
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
        ServiceInstance other = (ServiceInstance) obj;
        if (personaModelUuid == null) {
            if (other.personaModelUuid != null) {
                return false;
            }
        } else if (!personaModelUuid.equals(other.personaModelUuid)) {
            return false;
        }
        if (serviceInstanceName == null) {
            if (other.serviceInstanceName != null) {
                return false;
            }
        } else if (!serviceInstanceName.equals(other.serviceInstanceName)) {
            return false;
        }
        if (serviceInstanceUuid == null) {
            if (other.serviceInstanceUuid != null) {
                return false;
            }
        } else if (!serviceInstanceUuid.equals(other.serviceInstanceUuid)) {
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
        if (widgetModelUuid == null) {
            if (other.widgetModelUuid != null) {
                return false;
            }
        } else if (!widgetModelUuid.equals(other.widgetModelUuid)) {
            return false;
        }
        if (widgetModelVersion == null) {
            if (other.widgetModelVersion != null) {
                return false;
            }
        } else if (!widgetModelVersion.equals(other.widgetModelVersion)) {
            return false;
        }
        return true;
    }

}
