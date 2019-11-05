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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AaiNqVfModule implements Serializable {
    private static final long serialVersionUID = 8019133081429638232L;

    @SerializedName("vf-module-id")
    private String vfModuleId;

    @SerializedName("vf-module-name")
    private String vfModuleName;

    @SerializedName("heat-stack-id")
    private String heatStackId;

    @SerializedName("orchestration-status")
    private String orchestrationStatus;

    @SerializedName("is-base-vf-module")
    private Boolean isBaseVfModule;

    @SerializedName("resource-version")
    private String resourceVersion;

    @SerializedName("persona-model-id")
    private String personaModelId;

    @SerializedName("persona-model-version")
    private String personaModelVersion;

    @SerializedName("widget-model-id")
    private String widgetModelId;

    @SerializedName("widget-model-version")
    private String widgetModelVersion;

    @SerializedName("contrail-service-instance-fqdn")
    private String contrailServiceInstanceFqdn;

    @SerializedName("model-invariant-id")
    private String modelInvariantId;

    @SerializedName("model-version-id")
    private String modelVersionId;

    @SerializedName("model-customization-id")
    private String modelCustomizationId = null;
}
