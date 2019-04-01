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

public class AaiNqCloudRegion implements Serializable {

    private static final long serialVersionUID = -897231529157222683L;

    @SerializedName("cloud-owner")
    private String cloudOwner;

    @SerializedName("cloud-region-id")
    private String cloudRegionId;

    @SerializedName("cloud-region-version")
    private String cloudRegionVersion;

    @SerializedName("complex-name")
    private String complexName;

    @SerializedName("resource-version")
    private String resourceVersion;

    public String getCloudOwner() {
        return cloudOwner;
    }

    public String getCloudRegionId() {
        return cloudRegionId;
    }

    public String getCloudRegionVersion() {
        return cloudRegionVersion;
    }

    public String getComplexName() {
        return complexName;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setCloudOwner(String cloudOwner) {
        this.cloudOwner = cloudOwner;
    }

    public void setCloudRegionId(String cloudRegionId) {
        this.cloudRegionId = cloudRegionId;
    }

    public void setCloudRegionVersion(String cloudRegionVersion) {
        this.cloudRegionVersion = cloudRegionVersion;
    }

    public void setComplexName(String complexName) {
        this.complexName = complexName;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }
}
