/*-
 * ============LICENSE_START=======================================================
 * appclcm
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

package org.onap.policy.appclcm;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class LcmWrapper implements Serializable {

    private static final long serialVersionUID = 753005805432396532L;

    @SerializedName(value = "version")
    private String version;

    @SerializedName(value = "cambria-partition")
    private String cambriaPartition;

    @SerializedName(value = "rpc-name")
    private String rpcName;

    @SerializedName(value = "correlation-id")
    private String correlationId;

    @SerializedName(value = "type")
    private String type;

    public LcmWrapper() {
        // Create a default LCMWrapper instance
    }

    /**
     * Get the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set the version.
     *
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get the cambria partition.
     *
     * @return the cambriaPartition
     */
    public String getCambriaPartition() {
        return cambriaPartition;
    }

    /**
     * Set the cambria partition.
     *
     * @param cambriaPartition the cambriaPartition to set
     */
    public void setCambriaPartition(String cambriaPartition) {
        this.cambriaPartition = cambriaPartition;
    }

    /**
     * Get the RPN name.
     *
     * @return the rpcName
     */
    public String getRpcName() {
        return rpcName;
    }

    /**
     * Set the RPC name.
     *
     * @param rpcName the rpcName to set
     */
    public void setRpcName(String rpcName) {
        this.rpcName = rpcName;
    }

    /**
     * Get the correlation Id.
     *
     * @return the correlationId
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Set the correclation Id.
     *
     * @param correlationId the correlationId to set
     */
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    /**
     * Get the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Set the type.
     *
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
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
        LcmWrapper other = (LcmWrapper) obj;
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
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        return true;
    }

}
