/*-
 * ============LICENSE_START=======================================================
 * so
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

package org.onap.policy.so;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SoRequestStatus implements Serializable {

    private static final long serialVersionUID = -3283942659786236032L;

    @SerializedName("percentProgress")
    private int percentProgress;

    @SerializedName("requestState")
    private String requestState;

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("wasRolledBack")
    private boolean wasRolledBack;

    public SoRequestStatus() {
      //required by author
    }

    public int getPercentProgress() {
        return percentProgress;
    }

    public String getRequestState() {
        return requestState;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isWasRolledBack() {
        return wasRolledBack;
    }

    public void setPercentProgress(int percentProgress) {
        this.percentProgress = percentProgress;
    }

    public void setRequestState(String requestState) {
        this.requestState = requestState;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setWasRolledBack(boolean wasRolledBack) {
        this.wasRolledBack = wasRolledBack;
    }

}
