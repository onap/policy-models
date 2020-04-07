/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnr;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class PciWrapper implements Serializable {

    private static final long serialVersionUID = 375215806432396532L;

    private String version;

    @SerializedName(value = "cambria-partition")
    private String cambriaPartition;

    @SerializedName(value = "rpc-name")
    private String rpcName;

    @SerializedName(value = "correlation-id")
    private String correlationId;

    private String type;

    public PciWrapper() {
        // Create a default PciWrapper instance
    }

    @Override
    public String toString() {
        return "Wrapper [version=" + version + ", cambriaPartition=" + cambriaPartition + ", rpcName=" + rpcName
            + ", correlationId=" + correlationId + ", type=" + type + "]";
    }
}
