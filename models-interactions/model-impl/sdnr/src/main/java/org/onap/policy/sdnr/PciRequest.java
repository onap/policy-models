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

public class PciRequest implements Serializable {

    private static final long serialVersionUID = 323235565922846624L;

    @SerializedName(value = "CommonHeader")
    private PciCommonHeader commonHeader;

    @SerializedName(value = "Action")
    private String action;

    @SerializedName(value = "Payload")
    private String payload;

    public PciRequest() {
        // Create a default PCI request
    }

    public PciCommonHeader getCommonHeader() {
        return commonHeader;
    }

    public void setCommonHeader(PciCommonHeader commonHeader) {
        this.commonHeader = commonHeader;
    }

    /**
     * Get the action.
     * 
     * @return the action
     */
    public String getAction() {
        return action;
    }

    /**
     * Set the action.
     * 
     * @param action
     *            the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Get the payload.
     * 
     * @return the payload
     */

    public String getPayload() {
        return payload;
    }

    /**
     * Set the payload.
     * 
     * @param action
     *            the action to set
     */

    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "PciRequest[commonHeader=" + commonHeader + ", action=" + action + ", payload=" + payload + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((commonHeader == null) ? 0 : commonHeader.hashCode());
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((payload == null) ? 0 : payload.hashCode());
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
        PciRequest other = (PciRequest) obj;
        if (commonHeader == null) {
            if (other.commonHeader != null) {
                return false;
            }
        } else if (!commonHeader.equals(other.commonHeader)) {
            return false;
        }
        if (action == null) {
            if (other.action != null) {
                return false;
            }
        } else if (!action.equals(other.action)) {
            return false;
        }
        if (payload == null) {
            if (other.payload != null) {
                return false;
            }
        } else if (!payload.equals(other.payload)) {
            return false;
        }
        return true;
    }

}
