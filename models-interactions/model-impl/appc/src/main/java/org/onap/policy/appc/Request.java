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
import java.util.HashMap;
import java.util.Map;

public class Request implements Serializable {
    private static final long serialVersionUID = -3912323643990646431L;

    @SerializedName("CommonHeader")
    private CommonHeader commonHeader;

    @SerializedName("Action")
    private String action;

    @SerializedName("TargetID")
    private String targetId;

    @SerializedName("ObjectID")
    private String objectId;

    @SerializedName("Payload")
    private transient HashMap<String, Object> payload = new HashMap<>();

    public Request() {
        // Initiate an empty Request instance
    }

    public CommonHeader getCommonHeader() {
        return commonHeader;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public void setCommonHeader(CommonHeader commonHeader) {
        this.commonHeader = commonHeader;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = new HashMap<>(payload);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((commonHeader == null) ? 0 : commonHeader.hashCode());
        result = prime * result + ((objectId == null) ? 0 : objectId.hashCode());
        result = prime * result + ((payload == null) ? 0 : payload.hashCode());
        result = prime * result + ((targetId == null) ? 0 : targetId.hashCode());
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

        Request other = (Request) obj;
        if (action != null ? !action.equals(other.action) : other.action != null) {
            return false;
        }

        if (commonHeader != null ? !commonHeader.equals(other.commonHeader) : other.commonHeader != null) {
            return false;
        }

        if (objectId != null ? !objectId.equals(other.objectId) : other.objectId != null) {
            return false;
        }


        if (payload != null ? !payload.equals(other.payload) : other.payload != null) {
            return false;
        }

        if (targetId != null ? !targetId.equals(other.targetId) : other.targetId != null) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "Request [CommonHeader=" + commonHeader + ", Action=" + action + ", TargetId=" + targetId + ", ObjectId="
                + objectId + ", Payload=" + payload + "]";
    }

}
