/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Huawei Technologies Co., Ltd. All rights reserved.
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

public class SdncHealVfModuleRequestInput implements Serializable {

    private static final long serialVersionUID = 3208673205100673119L;

    @SerializedName("vf-module-input-parameters")
    private SdncHealVfModuleParametersInfo vfModuleParametersInfo;

    public SdncHealVfModuleRequestInput() {
        // Default constructor for SdncHealVfModuleRequestInput
    }

    public SdncHealVfModuleParametersInfo getVfModuleParametersInfo() {
        return vfModuleParametersInfo;
    }

    public void setVfModuleParametersInfo(SdncHealVfModuleParametersInfo info) {
        this.vfModuleParametersInfo = info;
    }
}
