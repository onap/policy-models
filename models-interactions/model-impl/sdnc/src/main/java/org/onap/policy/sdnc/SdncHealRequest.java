/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018-2019 Huawei. All rights reserved.
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

package org.onap.policy.sdnc;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SdncHealRequest implements Serializable {

    private static final long serialVersionUID = -7341931593089709247L;

    @SerializedName("sdnc-request-header")
    private SdncHealRequestHeaderInfo requestHeaderInfo;

    @SerializedName("request-information")
    private SdncHealRequestInfo requestInfo;

    @SerializedName("service-information")
    private SdncHealServiceInfo serviceInfo;

    @SerializedName("network-information")
    private SdncHealNetworkInfo networkInfo;

    @SerializedName("vnf-information")
    private SdncHealVnfInfo vnfInfo;

    @SerializedName("vf-module-information")
    private SdncHealVfModuleInfo vfModuleInfo;

    @SerializedName("vf-module-request-input")
    private SdncHealVfModuleRequestInput vfModuleRequestInput;

    public SdncHealRequest() {
        // Default constructor for SdncHealRequest
    }

    public SdncHealRequestHeaderInfo getRequestHeaderInfo() {
        return requestHeaderInfo;
    }

    public void setRequestHeaderInfo(SdncHealRequestHeaderInfo requestHeaderInfo) {
        this.requestHeaderInfo = requestHeaderInfo;
    }

    public SdncHealRequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(SdncHealRequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public SdncHealServiceInfo getServiceInfo() {
        return serviceInfo;
    }

    public void setServiceInfo(SdncHealServiceInfo serviceInfo) {
        this.serviceInfo = serviceInfo;
    }

    public SdncHealNetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    public void setNetworkInfo(SdncHealNetworkInfo networkInfo) {
        this.networkInfo = networkInfo;
    }

    public SdncHealVnfInfo getVnfInfo() {
        return vnfInfo;
    }

    public void setVnfInfo(SdncHealVnfInfo vnfInfo) {
        this.vnfInfo = vnfInfo;
    }

    public SdncHealVfModuleRequestInput getVfModuleRequestInput() {
        return vfModuleRequestInput;
    }

    public void setVfModuleRequestInput(SdncHealVfModuleRequestInput input) {
        this.vfModuleRequestInput = input;
    }

    public SdncHealVfModuleInfo getVfModuleInfo() {
        return vfModuleInfo;
    }

    public void setVfModuleInfo(SdncHealVfModuleInfo vfModuleInfo) {
        this.vfModuleInfo = vfModuleInfo;
    }
}
