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
import java.util.Map;

public class LcmRequest implements Serializable {

    private static final long serialVersionUID = 219375564922846624L;

    @SerializedName(value = "common-header")
    private LcmCommonHeader commonHeader;

    @SerializedName(value = "action")
    private String action;

    @SerializedName(value = "action-identifiers")
    private Map<String, String> actionIdentifiers;

    @SerializedName(value = "payload")
    private String payload;

    public LcmRequest() {
        // Create a default LCM request
    }

    public LcmCommonHeader getCommonHeader() {
        return commonHeader;
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
     * @param action the action to set
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Get the action identifiers.
     * 
     * @return the actionIdentifiers
     */
    public Map<String, String> getActionIdentifiers() {
        return actionIdentifiers;
    }

    /**
     * Set the action identifiers.
     * 
     * @param actionIdentifiers the actionIdentifiers to set
     */
    public void setActionIdentifiers(Map<String, String> actionIdentifiers) {
        this.actionIdentifiers = actionIdentifiers;
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

    /**
     * Get the common header.
     * 
     * @param commonHeader the commonHeader to set
     */
    public void setCommonHeader(LcmCommonHeader commonHeader) {
        this.commonHeader = commonHeader;
    }

    @Override
    public String toString() {
        return "Request [commonHeader=" + commonHeader + ", action=" + action + ", actionIdentifiers="
                + actionIdentifiers + ", payload=" + payload + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((commonHeader == null) ? 0 : commonHeader.hashCode());
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((actionIdentifiers == null) ? 0 : actionIdentifiers.hashCode());
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
        LcmRequest other = (LcmRequest) obj;
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
        if (actionIdentifiers == null) {
            if (other.actionIdentifiers != null) {
                return false;
            }
        } else if (!actionIdentifiers.equals(other.actionIdentifiers)) {
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
