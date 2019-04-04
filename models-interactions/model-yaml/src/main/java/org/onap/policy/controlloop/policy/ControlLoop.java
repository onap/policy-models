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
import java.util.LinkedList;
import java.util.List;

import org.onap.policy.aai.Pnf;
import org.onap.policy.sdc.Resource;
import org.onap.policy.sdc.Service;

public class ControlLoop implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final String COMPILER_VERSION = "2.0.0";

    private String controlLoopName;
    private String version = COMPILER_VERSION;
    private List<Service> services;
    private List<Resource> resources;
    private Pnf pnf;
    private String triggerPolicy = FinalResult.FINAL_OPENLOOP.toString();
    private Integer timeout;
    private Boolean abatement = false;

    public ControlLoop() {
        // Empty Constructor.
    }

    /**
     * Constructor.
     * 
     * @param controlLoop copy object
     */
    public ControlLoop(ControlLoop controlLoop) {
        this.controlLoopName = controlLoop.controlLoopName;
        this.services = new LinkedList<>();
        if (controlLoop.services != null) {
            for (Service service : controlLoop.services) {
                this.services.add(service);
            }
        }
        this.resources = new LinkedList<>();
        if (controlLoop.resources != null) {
            for (Resource resource : controlLoop.resources) {
                this.resources.add(resource);
            }
        }
        this.triggerPolicy = controlLoop.triggerPolicy;
        this.timeout = controlLoop.timeout;
        this.abatement = controlLoop.abatement;
    }

    public static String getCompilerVersion() {
        return ControlLoop.COMPILER_VERSION;
    }

    public String getControlLoopName() {
        return controlLoopName;
    }

    public void setControlLoopName(String controlLoopName) {
        this.controlLoopName = controlLoopName;
    }

    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        this.services = services;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    public String getTrigger_policy() {
        return triggerPolicy;
    }

    public void setTrigger_policy(String triggerPolicy) {
        this.triggerPolicy = triggerPolicy;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Boolean getAbatement() {
        return abatement;
    }

    public void setAbatement(Boolean abatement) {
        this.abatement = abatement;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Pnf getPnf() {
        return pnf;
    }

    public void setPnf(Pnf pnf) {
        this.pnf = pnf;
    }

    @Override
    public String toString() {
        return "ControlLoop [controlLoopName=" + controlLoopName + ", version=" + version + ", services=" + services
                + ", resources=" + resources + ", trigger_policy=" + triggerPolicy + ", timeout=" + timeout
                + ", abatement=" + abatement + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((controlLoopName == null) ? 0 : controlLoopName.hashCode());
        result = prime * result + ((resources == null) ? 0 : resources.hashCode());
        result = prime * result + ((services == null) ? 0 : services.hashCode());
        result = prime * result + ((timeout == null) ? 0 : timeout.hashCode());
        result = prime * result + ((triggerPolicy == null) ? 0 : triggerPolicy.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((abatement == null) ? 0 : abatement.hashCode());
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
        ControlLoop other = (ControlLoop) obj;
        return equalsMayBeNull(controlLoopName, other.controlLoopName) && equalsMayBeNull(resources, other.resources)
                && equalsMayBeNull(services, other.services) && equalsMayBeNull(timeout, other.timeout)
                && equalsMayBeNull(triggerPolicy, other.triggerPolicy) && equalsMayBeNull(version, other.version)
                && equalsMayBeNull(abatement, other.abatement);
    }

    private boolean equalsMayBeNull(final Object obj1, final Object obj2) {
        if (obj1 == null) {
            return obj2 == null;
        }
        return obj1.equals(obj2);
    }

}
