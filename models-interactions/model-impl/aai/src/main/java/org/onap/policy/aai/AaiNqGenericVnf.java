/*-
 * ============LICENSE_START=======================================================
 * aai
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

package org.onap.policy.aai;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class AaiNqGenericVnf implements Serializable {

    private static final long serialVersionUID = 834322706248060560L;

    @SerializedName("vnf-id")
    private String vnfId;

    @SerializedName("vnf-name")
    private String vnfName;

    @SerializedName("vnf-name2")
    private String vnfName2;

    @SerializedName("vnf-type")
    private String vnfType;

    @SerializedName("service-id")
    private String serviceId;

    @SerializedName("prov-status")
    private String provStatus;

    @SerializedName("operational-state")
    private String operationalState;

    @SerializedName("ipv4-oam-address")
    private String ipv4OamAddress;

    @SerializedName("ipv4-loopback0-address")
    private String ipv4Loopback0Address;

    @SerializedName("in-maint")
    private Boolean inMaint;

    @SerializedName("is-closed-loop-disabled")
    private Boolean isClosedLoopDisabled;

    @SerializedName("resource-version")
    private String resourceVersion;

    @SerializedName("encrypted-access-flag")
    private Boolean encrypedAccessFlag;

    @SerializedName("persona-model-id")
    private String personaModelId;

    @SerializedName("persona-model-version")
    private String personaModelVersion;

    @SerializedName("model-invariant-id")
    private String modelInvariantId;

    @SerializedName("model-version-id")
    private String modelVersionId;

    @SerializedName("model-customization-id")
    private String modelCustomizationId = null;

    public String getVnfId() {
        return vnfId;
    }

    public String getVnfName() {
        return vnfName;
    }

    public String getVnfName2() {
        return vnfName2;
    }

    public String getVnfType() {
        return vnfType;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getProvStatus() {
        return provStatus;
    }

    public String getOperationalState() {
        return operationalState;
    }

    public String getIpv4OamAddress() {
        return ipv4OamAddress;
    }

    public String getIpv4Loopback0Address() {
        return ipv4Loopback0Address;
    }

    public Boolean getInMaint() {
        return inMaint;
    }

    public Boolean getIsClosedLoopDisabled() {
        return isClosedLoopDisabled;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public Boolean getEncrypedAccessFlag() {
        return encrypedAccessFlag;
    }

    public String getPersonaModelId() {
        return personaModelId;
    }

    public String getPersonaModelVersion() {
        return personaModelVersion;
    }

    public String getModelInvariantId() {
        return modelInvariantId;
    }

    public String getModelVersionId() {
        return modelVersionId;
    }

    public String getModelCustomizationId() {
        return modelCustomizationId;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    public void setVnfName(String vnfName) {
        this.vnfName = vnfName;
    }

    public void setVnfName2(String vnfName2) {
        this.vnfName2 = vnfName2;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setProvStatus(String provStatus) {
        this.provStatus = provStatus;
    }

    public void setOperationalState(String operationalState) {
        this.operationalState = operationalState;
    }

    public void setIpv4OamAddress(String ipv4OamAddress) {
        this.ipv4OamAddress = ipv4OamAddress;
    }

    public void setIpv4Loopback0Address(String ipv4Loopback0Address) {
        this.ipv4Loopback0Address = ipv4Loopback0Address;
    }

    public void setInMaint(Boolean inMaint) {
        this.inMaint = inMaint;
    }

    public void setIsClosedLoopDisabled(Boolean isClosedLoopDisabled) {
        this.isClosedLoopDisabled = isClosedLoopDisabled;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }

    public void setEncrypedAccessFlag(Boolean encrypedAccessFlag) {
        this.encrypedAccessFlag = encrypedAccessFlag;
    }

    public void setPersonaModelId(String personaModelId) {
        this.personaModelId = personaModelId;
    }

    public void setPersonaModelVersion(String personaModelVersion) {
        this.personaModelVersion = personaModelVersion;
    }

    public void setModelInvariantId(String modelInvariantId) {
        this.modelInvariantId = modelInvariantId;
    }

    public void setModelVersionId(String modelVersionId) {
        this.modelVersionId = modelVersionId;
    }

    public void setModelCustomizationId(String modelCustomizationId) {
        this.modelCustomizationId = modelCustomizationId;
    }
}
