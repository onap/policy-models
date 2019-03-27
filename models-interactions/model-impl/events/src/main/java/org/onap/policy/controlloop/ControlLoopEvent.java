/*-
 * ============LICENSE_START=======================================================
 * controlloop
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

package org.onap.policy.controlloop;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.UUID;

public abstract class ControlLoopEvent implements Serializable {

    private static final long serialVersionUID = 2391252138583119195L;
    
    @SerializedName("closedLoopControlName")
    private String closedLoopControlName;
    
    @SerializedName("version")
    private String version = "1.0.2";
    
    @SerializedName("requestID")
    private UUID requestId;
    
    @SerializedName("closedLoopEventClient")
    private String closedLoopEventClient;
    
    @SerializedName("target_type")
    private ControlLoopTargetType targetType;
    
    @SerializedName("target")
    private String target;
    
    @SerializedName("from")
    private String from;
    
    @SerializedName("policyScope")
    private String policyScope;
    
    @SerializedName("policyName")
    private String policyName;
    
    @SerializedName("policyVersion")
    private String policyVersion;
    
    @SerializedName("closedLoopEventStatus")
    private ControlLoopEventStatus closedLoopEventStatus;

    @SerializedName("payload")
    private String payload;
    
    public ControlLoopEvent() {

    }

    /**
     * Construct an instace from an existing instance.
     * 
     * @param event the existing instance
     */
    public ControlLoopEvent(ControlLoopEvent event) {
        if (event == null) {
            return;
        }
        this.closedLoopControlName = event.closedLoopControlName;
        this.requestId = event.requestId;
        this.closedLoopEventClient = event.closedLoopEventClient;
        this.targetType = event.targetType;
        this.target = event.target;
        this.from = event.from;
        this.policyScope = event.policyScope;
        this.policyName = event.policyName;
        this.policyVersion = event.policyVersion;
        this.closedLoopEventStatus = event.closedLoopEventStatus;
        this.payload = event.payload;
    }

    public boolean isEventStatusValid() {
        return this.closedLoopEventStatus != null;
    }

    public String getClosedLoopControlName() {
        return closedLoopControlName;
    }

    public void setClosedLoopControlName(String closedLoopControlName) {
        this.closedLoopControlName = closedLoopControlName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getClosedLoopEventClient() {
        return closedLoopEventClient;
    }

    public void setClosedLoopEventClient(String closedLoopEventClient) {
        this.closedLoopEventClient = closedLoopEventClient;
    }

    public ControlLoopTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(ControlLoopTargetType targetType) {
        this.targetType = targetType;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getPolicyScope() {
        return policyScope;
    }

    public void setPolicyScope(String policyScope) {
        this.policyScope = policyScope;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    public ControlLoopEventStatus getClosedLoopEventStatus() {
        return closedLoopEventStatus;
    }

    public void setClosedLoopEventStatus(ControlLoopEventStatus closedLoopEventStatus) {
        this.closedLoopEventStatus = closedLoopEventStatus;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
