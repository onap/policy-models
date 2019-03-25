/*-
 * ============LICENSE_START=======================================================
 * aai
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file excthe License at
 * ept in compliance with the License.
 * You may obtain a copy of
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.aai;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AaiGetVnfResponse extends AaiGetResponse implements Serializable {

    private static final long serialVersionUID = -6247505944905898871L;

    @SerializedName("vnf-id")
    private String vnfId;

    @SerializedName("vnf-name")
    private String vnfName;

    @SerializedName("vnf-type")
    private String vnfType;

    @SerializedName("service-id")
    private String serviceId;

    @SerializedName("orchestration-status")
    private String orchestrationStatus;

    @SerializedName("in-maint")
    private boolean inMaint;

    @SerializedName("is-closed-loop-disabled")
    private boolean isClosedLoopDisabled;

    @SerializedName("model-invariant-id")
    private String modelInvariantId;

    @SerializedName("prov-status")
    private String provStatus;

    @SerializedName("resource-version")
    private String resourceVersion;

    @SerializedName("relationship-list")
    private RelationshipList relationshipList;

    public String getVnfId() {
        return vnfId;
    }

    public String getVnfName() {
        return vnfName;
    }

    public String getVnfType() {
        return vnfType;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getOrchestrationStatus() {
        return orchestrationStatus;
    }

    public boolean getInMaint() {
        return inMaint;
    }

    public boolean getIsClosedLoopDisabled() {
        return isClosedLoopDisabled;
    }

    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public String getProvStatus() {
        return provStatus;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public RelationshipList getRelationshipList() {
        return relationshipList;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    public void setVnfName(String vnfName) {
        this.vnfName = vnfName;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setOrchestrationStatus(String orchestrationStatus) {
        this.orchestrationStatus = orchestrationStatus;
    }

    public void setInMaint(boolean inMaint) {
        this.inMaint = inMaint;
    }

    public void setIsClosedLoopDisabled(boolean isClosedLoopDisabled) {
        this.isClosedLoopDisabled = isClosedLoopDisabled;
    }

    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    public void setProvStatus(String provStatus) {
        this.provStatus = provStatus;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public void setRelationshipList(RelationshipList relationshipList) {
        this.relationshipList = relationshipList;
    }
}
