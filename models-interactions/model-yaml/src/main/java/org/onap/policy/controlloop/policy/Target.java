/*-
 * ============LICENSE_START=======================================================
 * policy-yaml
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.policy;

import java.io.Serializable;

public class Target implements Serializable {

    private static final long serialVersionUID = 2180988443264988319L;
     
    private String resourceId;
    private TargetType type;

    public Target() {
        //Does Nothing Empty Constructor
    }
    
    public Target(TargetType type) {
        this.type = type;
    }
    
    public Target(String resourceId) {
        this.resourceId = resourceId;
    }
    
    public Target(TargetType type, String resourceId) {
        this.type = type;
        this.resourceId = resourceId;
    }
    
    public Target(Target target) {
        this.type = target.type;
        this.resourceId = target.resourceId;
    }
    
    public String getResourceID() {
        return resourceId;
    }

    public void setResourceID(String resourceId) {
        this.resourceId = resourceId;
    }

    public TargetType getType() {
        return type;
    }

    public void setType(TargetType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Target [type=" + type + ", resourceId=" + resourceId + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((resourceId == null) ? 0 : resourceId.hashCode());
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
        Target other = (Target) obj;
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (resourceId == null) {
            if (other.resourceId != null) {
                return false;
            }
        } else if (!resourceId.equals(other.resourceId)) {
            return false;
        }
        return true;
    }
}
