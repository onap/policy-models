/*-
 * ============LICENSE_START=======================================================
 * controlloop
 * ================================================================================
 * Copyright (C) 2019 Wipro Limited Intellectual Property. All rights reserved.
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

public class ControlLoopResponse implements Serializable {

    private static final long serialVersionUID = 2391252138583119195L;

    @SerializedName("closedLoopControlName")
    private String closedLoopControlName;

    @SerializedName("version")
    private String version = "1.0.0";

    @SerializedName("requestID")
    private UUID requestId;

    @SerializedName("target")
    private String target;

    @SerializedName("from")
    private String from;

    @SerializedName("policyName")
    private String policyName;

    @SerializedName("policyVersion")
    private String policyVersion;

    @SerializedName("payload")
    private String payload;

    public ControlLoopResponse() {

    }

    /**
     * Construct an instace from an existing instance.
     *
     * @param response
     *            the existing instance
     */
    public ControlLoopResponse(ControlLoopResponse response) {
        if (response == null) {
            return;
        }
        this.closedLoopControlName = response.closedLoopControlName;
        this.requestId = response.requestId;
        this.target = response.target;
        this.from = response.from;
        this.policyName = response.policyName;
        this.policyVersion = response.policyVersion;
        this.payload = response.payload;
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

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
