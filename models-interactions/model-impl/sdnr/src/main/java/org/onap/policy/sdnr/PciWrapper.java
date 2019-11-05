/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnr;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((cambriaPartition == null) ? 0 : cambriaPartition.hashCode());
        result = prime * result + ((correlationId == null) ? 0 : correlationId.hashCode());
        result = prime * result + ((rpcName == null) ? 0 : rpcName.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PciWrapper other = (PciWrapper) obj;
        if (cambriaPartition == null) {
            if (other.cambriaPartition != null) {
                return false;
            }
        } else if (!cambriaPartition.equals(other.cambriaPartition)) {
            return false;
        }
        if (correlationId == null) {
            if (other.correlationId != null) {
                return false;
            }
        } else if (!correlationId.equals(other.correlationId)) {
            return false;
        }
        if (rpcName == null) {
            if (other.rpcName != null) {
                return false;
            }
        } else if (!rpcName.equals(other.rpcName)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (version == null) {
            return other.version == null;
        } else {
            return version.equals(other.version);
        }
    }
}
