/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018 Huawei. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnc;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.UUID;

public class SdncRequest implements Serializable {

    private static final long serialVersionUID = 3736300970326332512L;
    // These fields are not serialized and not part of JSON
    private transient String nsInstanceId;
    private transient UUID requestId;
    private transient String url;

    @SerializedName("input")
    private SdncHealRequest healRequest;

    public SdncRequest() {
            // Default constructor for SdncRequest
    }

    public String getNsInstanceId() {
        return nsInstanceId;
    }

    public void setNsInstanceId(String nsInstanceId) {
        this.nsInstanceId = nsInstanceId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public SdncHealRequest getHealRequest() {
        return healRequest;
    }

    public void setHealRequest(SdncHealRequest healRequest) {
        this.healRequest = healRequest;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
