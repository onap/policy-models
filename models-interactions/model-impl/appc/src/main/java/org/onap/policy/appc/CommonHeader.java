/*-
 * ============LICENSE_START=======================================================
 * appc
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

package org.onap.policy.appc;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class CommonHeader implements Serializable {
    private static final long serialVersionUID = -3581658269910980336L;

    @SerializedName("TimeStamp")
    private Instant timeStamp = Instant.now();

    @SerializedName("APIver")
    private String apiVer = "1.01";

    @SerializedName("OriginatorID")
    private String originatorId;

    @SerializedName("RequestID")
    private UUID requestId;

    @SerializedName("SubRequestID")
    private String subRequestId;

    @SerializedName("RequestTrack")
    private Collection<String> requestTrack = new ArrayList<>();

    @SerializedName("Flags")
    private Collection<Map<String, String>> flags = new ArrayList<>();

    public CommonHeader() {}

    /**
     * Construct an instance from an existing instance.
     * 
     * @param commonHeader the existing instance
     */
    public CommonHeader(CommonHeader commonHeader) {
        this.originatorId = commonHeader.originatorId;
        this.requestId = commonHeader.requestId;
        this.subRequestId = commonHeader.subRequestId;
        if (commonHeader.requestTrack != null) {
            this.requestTrack.addAll(commonHeader.requestTrack);
        }
        if (commonHeader.flags != null) {
            this.flags.addAll(commonHeader.flags);
        }
    }

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getApiVer() {
        return apiVer;
    }

    public void setApiVer(String apiVer) {
        this.apiVer = apiVer;
    }

    public String getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public String getSubRequestId() {
        return subRequestId;
    }

    public void setSubRequestId(String subRequestId) {
        this.subRequestId = subRequestId;
    }

    public Collection<String> getRequestTrack() {
        return requestTrack;
    }

    public void setRequestTrack(Collection<String> requestTrack) {
        this.requestTrack = requestTrack;
    }

    public Collection<Map<String, String>> getFlags() {
        return flags;
    }

    public void setFlags(Collection<Map<String, String>> flags) {
        this.flags = flags;
    }

    @Override
    public String toString() {
        return "CommonHeader [TimeStamp=" + timeStamp + ", APIver=" + apiVer + ", OriginatorId=" + originatorId
                + ", RequestId=" + requestId + ", SubrequestId=" + subRequestId + ", RequestTrack=" + requestTrack
                + ", Flags=" + flags + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((apiVer == null) ? 0 : apiVer.hashCode());
        result = prime * result + ((flags == null) ? 0 : flags.hashCode());
        result = prime * result + ((originatorId == null) ? 0 : originatorId.hashCode());
        result = prime * result + ((requestId == null) ? 0 : requestId.hashCode());
        result = prime * result + ((requestTrack == null) ? 0 : requestTrack.hashCode());
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
        CommonHeader other = (CommonHeader) obj;
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
        if (originatorId == null) {
            if (other.originatorId != null) {
                return false;
            }
        } else if (!originatorId.equals(other.originatorId)) {
            return false;
        }
        if (requestId == null) {
            if (other.requestId != null) {
                return false;
            }
        } else if (!requestId.equals(other.requestId)) {
            return false;
        }
        if (requestTrack == null) {
            if (other.requestTrack != null) {
                return false;
            }
        } else if (!requestTrack.equals(other.requestTrack)) {
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
            if (other.timeStamp != null) {
                return false;
            }
        } else if (!timeStamp.equals(other.timeStamp)) {
            return false;
        }
        return true;
    }

}
