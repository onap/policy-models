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

public class ServiceInstance implements Serializable {
    private static final long serialVersionUID = 6285260780966679625L;

    private UUID        personaModelUUID;
    private UUID        serviceUUID;
    private UUID        serviceInstanceUUID;
    private UUID        widgetModelUUID;
    private String  widgetModelVersion;
    private String  serviceName;
    private String  serviceInstanceName;
    
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
        this.personaModelUUID = instance.personaModelUUID;
        this.serviceUUID = instance.serviceUUID;
        this.serviceInstanceUUID = instance.serviceInstanceUUID;
        this.widgetModelUUID = instance.widgetModelUUID;
        this.widgetModelVersion = instance.widgetModelVersion;
        this.serviceName = instance.serviceName;
        this.serviceInstanceName = instance.serviceInstanceName;
    }
    
    public UUID getPersonaModelUUID() {
        return personaModelUUID;
    }

    public void setPersonaModelUUID(UUID personaModelUUID) {
        this.personaModelUUID = personaModelUUID;
    }

    public UUID getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(UUID serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public UUID getServiceInstanceUUID() {
        return serviceInstanceUUID;
    }

    public void setServiceInstanceUUID(UUID serviceInstanceUUID) {
        this.serviceInstanceUUID = serviceInstanceUUID;
    }

    public UUID getWidgetModelUUID() {
        return widgetModelUUID;
    }

    public void setWidgetModelUUID(UUID widgetModelUUID) {
        this.widgetModelUUID = widgetModelUUID;
    }

    public String getWidgetModelVersion() {
        return widgetModelVersion;
    }

    public void setWidgetModelVersion(String widgetModelVersion) {
        this.widgetModelVersion = widgetModelVersion;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceInstanceName() {
        return serviceInstanceName;
    }

    public void setServiceInstanceName(String serviceInstanceName) {
        this.serviceInstanceName = serviceInstanceName;
    }

    @Override
    public String toString() {
        return "ServiceInstance [personaModelUUID=" + personaModelUUID + ", serviceUUID=" + serviceUUID
                + ", serviceInstanceUUID=" + serviceInstanceUUID + ", widgetModelUUID=" + widgetModelUUID
                + ", widgetModelVersion=" + widgetModelVersion + ", serviceName=" + serviceName
                + ", serviceInstanceName=" + serviceInstanceName + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((personaModelUUID == null) ? 0 : personaModelUUID.hashCode());
        result = prime * result + ((serviceInstanceName == null) ? 0 : serviceInstanceName.hashCode());
        result = prime * result + ((serviceInstanceUUID == null) ? 0 : serviceInstanceUUID.hashCode());
        result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
        result = prime * result + ((serviceUUID == null) ? 0 : serviceUUID.hashCode());
        result = prime * result + ((widgetModelUUID == null) ? 0 : widgetModelUUID.hashCode());
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
        if (personaModelUUID == null) {
            if (other.personaModelUUID != null) {
                return false;
            }
        } else if (!personaModelUUID.equals(other.personaModelUUID)) {
            return false;
        }
        if (serviceInstanceName == null) {
            if (other.serviceInstanceName != null) {
                return false;
            }
        } else if (!serviceInstanceName.equals(other.serviceInstanceName)) {
            return false;
        }
        if (serviceInstanceUUID == null) {
            if (other.serviceInstanceUUID != null) {
                return false;
            }
        } else if (!serviceInstanceUUID.equals(other.serviceInstanceUUID)) {
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
        if (widgetModelUUID == null) {
            if (other.widgetModelUUID != null) {
                return false;
            }
        } else if (!widgetModelUUID.equals(other.widgetModelUUID)) {
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
