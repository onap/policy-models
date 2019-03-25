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

public class AaiNqTenant implements Serializable {
    private static final long serialVersionUID = 4702784101671984364L;

    @SerializedName("tenant-id")
    private String tenantId;

    @SerializedName("tenant-name")
    private String tenantName;

    @SerializedName("resource-version")
    private String resourceVersion;

    public String getTenantId() {
        return tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public String getResourceVersion() {
        return resourceVersion;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public void setResourceVersion(String resourceVersion) {
        this.resourceVersion = resourceVersion;
    }


}
