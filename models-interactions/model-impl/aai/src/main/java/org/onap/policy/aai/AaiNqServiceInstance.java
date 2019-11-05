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
public class AaiNqServiceInstance implements Serializable {
    private static final long serialVersionUID = -8000944396593889586L;

    @SerializedName("service-instance-id")
    private String serviceInstanceId;

    @SerializedName("service-instance-name")
    private String serviceInstanceName;

    @SerializedName("persona-model-id")
    private String personaModelId;

    @SerializedName("persona-model-version")
    private String personaModelVersion;

    @SerializedName("service-instance-location-id")
    private String serviceInstanceLocationId;

    @SerializedName("resource-version")
    private String resourceVersion;

    @SerializedName("model-invariant-id")
    private String modelInvariantId;

    @SerializedName("model-version-id")
    private String modelVersionId;
}
