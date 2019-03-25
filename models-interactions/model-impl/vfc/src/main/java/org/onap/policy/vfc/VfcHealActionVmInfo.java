/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017-2019 Intel Corp. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2018-2019 AT&T Corporation. All rights reserved.
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

package org.onap.policy.vfc;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class VfcHealActionVmInfo implements Serializable {

    private static final long serialVersionUID = 3208673205100673119L;

    @SerializedName("vmid")
    private String vmid;

    @SerializedName("vmname")
    private String vmname;

    public VfcHealActionVmInfo() {
        // Default constructor
    }

    public String getVmid() {
        return vmid;
    }

    public void setVmid(String vmid) {
        this.vmid = vmid;
    }

    public String getVmname() {
        return vmname;
    }

    public void setVmname(String vmname) {
        this.vmname = vmname;
    }
}
