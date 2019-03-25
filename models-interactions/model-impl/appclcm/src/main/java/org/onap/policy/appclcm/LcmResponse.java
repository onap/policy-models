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

public class LcmResponse implements Serializable {

    private static final long serialVersionUID = 6332508597287669750L;

    @SerializedName(value = "common-header")
    private LcmCommonHeader commonHeader;

    @SerializedName(value = "status")
    private LcmResponseStatus status = new LcmResponseStatus();

    @SerializedName(value = "payload")
    private String payload;

    public LcmResponse() {
        // EMPTY
    }

    /**
     * Constructs a response using the common header of the request since they will be the same.
     * 
     * @param request an appc lcm request object specified by the lcm api guide
     */
    public LcmResponse(LcmRequest request) {
        this.commonHeader = new LcmCommonHeader(request.getCommonHeader());
        String requestPayload = request.getPayload();
        if (requestPayload != null) {
            this.payload = requestPayload;
        }
    }

    /**
     * Get the common header.
     * 
     * @return the commonHeader
     */
    public LcmCommonHeader getCommonHeader() {
        return commonHeader;
    }

    /**
     * Set the common header.
     * 
     * @param commonHeader the commonHeader to set
     */
    public void setCommonHeader(LcmCommonHeader commonHeader) {
        this.commonHeader = commonHeader;
    }

    /**
     * Get the status.
     * 
     * @return the status
     */
    public LcmResponseStatus getStatus() {
        return status;
    }

    /**
     * Set the status.
     * 
     * @param status the status to set
     */
    public void setStatus(LcmResponseStatus status) {
        this.status = status;
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
     * @param payload the payload to set
     */
    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "Response [commonHeader=" + commonHeader + ", status=" + status + ", payload=" + payload + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((commonHeader == null) ? 0 : commonHeader.hashCode());
        result = prime * result + ((payload == null) ? 0 : payload.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
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
        LcmResponse other = (LcmResponse) obj;
        if (commonHeader == null) {
            if (other.commonHeader != null) {
                return false;
            }
        } else if (!commonHeader.equals(other.commonHeader)) {
            return false;
        }
        if (payload == null) {
            if (other.payload != null) {
                return false;
            }
        } else if (!payload.equals(other.payload)) {
            return false;
        }
        if (status == null) {
            if (other.status != null) {
                return false;
            }
        } else if (!status.equals(other.status)) {
            return false;
        }
        return true;
    }
}
