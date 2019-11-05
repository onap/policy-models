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
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
}
