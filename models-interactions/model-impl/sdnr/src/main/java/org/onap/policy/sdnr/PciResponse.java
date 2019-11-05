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
public class PciResponse implements Serializable {

    private static final long serialVersionUID = 8375708697287669750L;

    @SerializedName(value = "CommonHeader")
    private PciCommonHeader commonHeader;

    @SerializedName(value = "Status")
    private Status status = new Status();

    @SerializedName(value = "Payload")
    private String payload;

    public PciResponse() {
        // EMPTY
    }

    /**
     * Constructs a response using the common header of the request since they will
     * be the same.
     *
     * @param request
     *            an sdnr Pci request object specified by the Pci api guide
     */
    public PciResponse(PciRequest request) {
        this.commonHeader = new PciCommonHeader(request.getCommonHeader());
    }

    @Override
    public String toString() {
        return "PciResponse[CommonHeader=" + commonHeader + ", Status=" + status + ", Payload="
                + payload + "]";
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
        PciResponse other = (PciResponse) obj;
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
            return other.status == null;
        } else {
            return status.equals(other.status);
        }
    }
}
