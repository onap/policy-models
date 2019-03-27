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
import java.time.LocalDateTime;
import java.util.UUID;

public class SoRequest implements Serializable {

    private static final long serialVersionUID = -3283942659786236032L;

    @SerializedName("requestId")
    private UUID requestId;

    @SerializedName("startTime")
    private String startTime;

    @SerializedName("finishTime")
    private LocalDateTime finishTime;

    @SerializedName("requestScope")
    private String requestScope;

    @SerializedName("requestType")
    private String requestType;

    @SerializedName("requestDetails")
    private SoRequestDetails requestDetails;

    @SerializedName("requestStatus")
    private SoRequestStatus requestStatus;

    private transient SoOperationType operationType;

    public SoRequest() {
        // required by author
    }

    public LocalDateTime getFinishTime() {
        return finishTime;
    }

    public SoRequestDetails getRequestDetails() {
        return requestDetails;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public String getRequestScope() {
        return requestScope;
    }

    public SoRequestStatus getRequestStatus() {
        return requestStatus;
    }

    public String getRequestType() {
        return requestType;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setFinishTime(LocalDateTime finishTime) {
        this.finishTime = finishTime;
    }

    public void setRequestDetails(SoRequestDetails requestDetails) {
        this.requestDetails = requestDetails;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public void setRequestScope(String requestScope) {
        this.requestScope = requestScope;
    }

    public void setRequestStatus(SoRequestStatus requestStatus) {
        this.requestStatus = requestStatus;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public SoOperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(SoOperationType operationType) {
        this.operationType = operationType;
    }
}
