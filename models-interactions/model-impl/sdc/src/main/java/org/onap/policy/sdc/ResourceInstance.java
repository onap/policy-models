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

public class ResourceInstance implements Serializable {
    private static final long serialVersionUID = -5506162340393802424L;

    private String resourceInstanceName;
    private String resourceName;
    private UUID    resourceInvariantUuid;
    private String resourceVersion;
    private ResourceType resourceType;
    private UUID    resourceUuid;
    
    public ResourceInstance() {
        //Empty Constructor
    }
    
    /**
     * Constructor.
     *
     * @param instance copy object
     */
    public ResourceInstance(ResourceInstance instance) {
        if (instance == null) { 
            return;
        }
        this.resourceInstanceName = instance.resourceInstanceName;
        this.resourceName = instance.resourceName;
        this.resourceInvariantUuid = instance.resourceInvariantUuid;
        this.resourceVersion = instance.resourceVersion;
        this.resourceType = instance.resourceType;
        this.resourceUuid = instance.resourceUuid;
    }
    
    public String getResourceInstanceName() {
        return resourceInstanceName;
    }

    public void setResourceInstanceName(String resourceInstanceName) {
        this.resourceInstanceName = resourceInstanceName;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public UUID getResourceInvariantUUID() {
        return resourceInvariantUuid;
    }

    public void setResourceInvariantUUID(UUID resourceInvariantUuid) {
        this.resourceInvariantUuid = resourceInvariantUuid;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public UUID getResourceUuid() {
        return resourceUuid;
    }

    public void setResourceUuid(UUID resourceUuid) {
        this.resourceUuid = resourceUuid;
    }

    @Override
    public String toString() {
        return "ResourceInstance [resourceInstanceName=" + resourceInstanceName + ", resourceName=" + resourceName
                + ", resourceInvariantUuid=" + resourceInvariantUuid + ", resourceVersion=" + resourceVersion
                + ", resourceType=" + resourceType + ", resourceUuid=" + resourceUuid + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((resourceInstanceName == null) ? 0 : resourceInstanceName.hashCode());
        result = prime * result + ((resourceInvariantUuid == null) ? 0 : resourceInvariantUuid.hashCode());
        result = prime * result + ((resourceName == null) ? 0 : resourceName.hashCode());
        result = prime * result + ((resourceType == null) ? 0 : resourceType.hashCode());
        result = prime * result + ((resourceUuid == null) ? 0 : resourceUuid.hashCode());
        result = prime * result + ((resourceVersion == null) ? 0 : resourceVersion.hashCode());
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
        ResourceInstance other = (ResourceInstance) obj;
        if (resourceInstanceName == null) {
            if (other.resourceInstanceName != null) {
                return false;
            }
        } else if (!resourceInstanceName.equals(other.resourceInstanceName)) {
            return false;
        }
        if (resourceInvariantUuid == null) {
            if (other.resourceInvariantUuid != null) {
                return false;
            }
        } else if (!resourceInvariantUuid.equals(other.resourceInvariantUuid)) {
            return false;
        }
        if (resourceName == null) {
            if (other.resourceName != null) {
                return false;
            }
        } else if (!resourceName.equals(other.resourceName)) {
            return false;
        }
        if (resourceType != other.resourceType) {
            return false;
        }
        if (resourceUuid == null) {
            if (other.resourceUuid != null) {
                return false;
            }
        } else if (!resourceUuid.equals(other.resourceUuid)) {
            return false;
        }
        if (resourceVersion == null) {
            if (other.resourceVersion != null) {
                return false;
            }
        } else if (!resourceVersion.equals(other.resourceVersion)) {
            return false;
        }
        return true;
    }
    
}
