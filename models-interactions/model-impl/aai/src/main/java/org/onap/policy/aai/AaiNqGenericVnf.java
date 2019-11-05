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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}
