/*-
 * ============LICENSE_START=======================================================
 * AppcLcmActorServiceProvider
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.params;

import java.io.Serializable;

public class ControlLoopParams implements Serializable {

    private static final long serialVersionUID = 970755684770982776L;

    private String closedLoopControlName;
    private String controlLoopYaml;
    private String policyName;
    private String policyScope;
    private String policyVersion;

    public ControlLoopParams() {
        super();
    }

    /**
     * Construct an instance from an existing instance.
     *
     * @param params the existing instance
     */
    public ControlLoopParams(ControlLoopParams params) {
        this.closedLoopControlName = params.closedLoopControlName;
        this.controlLoopYaml = params.controlLoopYaml;
        this.policyName = params.policyName;
        this.policyScope = params.policyScope;
        this.policyVersion = params.policyVersion;
    }

    public String getClosedLoopControlName() {
        return closedLoopControlName;
    }

    public void setClosedLoopControlName(String closedLoopControlName) {
        this.closedLoopControlName = closedLoopControlName;
    }

    public String getControlLoopYaml() {
        return controlLoopYaml;
    }

    public void setControlLoopYaml(String controlLoopYaml) {
        this.controlLoopYaml = controlLoopYaml;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyScope() {
        return policyScope;
    }

    public void setPolicyScope(String policyScope) {
        this.policyScope = policyScope;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

}
