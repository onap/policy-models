/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnr;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PciCommonHeader implements Serializable {

    private static final long serialVersionUID = 5435363539127062114L;

    @SerializedName(value = "TimeStamp")
    private Instant timeStamp = Instant.now();

    @SerializedName(value = "APIVer")
    private String apiVer = "1.0";

    @SerializedName(value = "RequestID")
    private UUID requestId;

    @SerializedName(value = "SubRequestID")
    private String subRequestId;

    @SerializedName(value = "RequestTrack")
    private Map<String, String> requestTrack = new HashMap<>();

    @SerializedName(value = "Flags")
    private Map<String, String> flags = new HashMap<>();

    public PciCommonHeader() {

    }

    /**
     * Used to copy a pci common header.
     *
     * @param commonHeader a header that is defined by the Pci api guide that contains information
     *        about the request (requestId, flags, etc.)
     */
    public PciCommonHeader(PciCommonHeader commonHeader) {
        this.timeStamp = commonHeader.timeStamp;
        this.requestId = commonHeader.requestId;
        this.subRequestId = commonHeader.subRequestId;
        this.apiVer = commonHeader.getApiVer();
        if (commonHeader.requestTrack != null) {
            this.requestTrack.putAll(commonHeader.requestTrack);
        }
        if (commonHeader.flags != null) {
            this.flags.putAll(commonHeader.flags);
        }
    }

    @Override
    public String toString() {
        return "CommonHeader [timeStamp=" + timeStamp + ", apiVer=" + apiVer
                   + ", requestId=" + requestId + ", subRequestId=" + subRequestId + ", requestTrack=" + requestTrack
                   + ", flags=" + flags + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((apiVer == null) ? 0 : apiVer.hashCode());
        result = prime * result + ((flags == null) ? 0 : flags.hashCode());
        result = prime * result + ((requestTrack == null) ? 0 : requestTrack.hashCode());
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        result = prime * result + ((subRequestId == null) ? 0 : subRequestId.hashCode());
        result = prime * result + ((timeStamp == null) ? 0 : timeStamp.hashCode());
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
        PciCommonHeader other = (PciCommonHeader) obj;
        if (apiVer == null) {
            if (other.apiVer != null) {
                return false;
            }
        } else if (!apiVer.equals(other.apiVer)) {
            return false;
        }
        if (flags == null) {
            if (other.flags != null) {
                return false;
            }
        } else if (!flags.equals(other.flags)) {
            return false;
        }
        if (requestTrack == null) {
            if (other.requestTrack != null) {
                return false;
            }
        } else if (!requestTrack.equals(other.requestTrack)) {
            return false;
        }
        if (requestId == null) {
            if (other.requestId != null) {
                return false;
            }
        } else if (!requestId.equals(other.requestId)) {
            return false;
        }
        if (subRequestId == null) {
            if (other.subRequestId != null) {
                return false;
            }
        } else if (!subRequestId.equals(other.subRequestId)) {
            return false;
        }
        if (timeStamp == null) {
            return other.timeStamp == null;
        } else {
            return timeStamp.equals(other.timeStamp);
        }
    }
}
