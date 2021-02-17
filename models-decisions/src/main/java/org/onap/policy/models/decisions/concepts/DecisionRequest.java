/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Decision Models
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.decisions.concepts;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This class is for a Decision Request to a Decision PDP Engine.
 *
 * @author pameladragosh
 *
 */
@Data
@NoArgsConstructor
public class DecisionRequest {
    @SerializedName("ONAPName")
    private String  onapName;

    @SerializedName("ONAPComponent")
    private String  onapComponent;

    @SerializedName("ONAPInstance")
    private String  onapInstance;

    @SerializedName("requestId")
    private String  requestId;

    @SerializedName("context")
    private Map<String, Object> context;

    @SerializedName("action")
    private String  action;

    @SerializedName("currentDateTime")
    private OffsetDateTime currentDateTime;

    @SerializedName("currentDate")
    private LocalDate currentDate;

    @SerializedName("currentTime")
    private OffsetTime currentTime;

    @SerializedName("timeZone")
    private ZoneOffset timeZone;

    @SerializedName("resource")
    private Map<String, Object> resource;

    /**
     * Copy constructor.
     *
     * @param request Incoming DecisionRequest
     */
    public DecisionRequest(DecisionRequest request) {
        this.setOnapName(request.getOnapName());
        this.setOnapComponent(request.getOnapComponent());
        this.setOnapInstance(request.getOnapInstance());
        this.setRequestId(request.getRequestId());
        if (request.getContext() != null) {
            this.setContext(new HashMap<>());
            this.getContext().putAll(request.getContext());
        }
        this.setAction(request.getAction());
        this.setCurrentDate(request.getCurrentDate());
        this.setCurrentDateTime(request.getCurrentDateTime());
        this.setCurrentTime(request.getCurrentTime());
        this.setTimeZone(request.getTimeZone());
        if (request.getResource() != null) {
            this.setResource(new HashMap<>());
            this.getResource().putAll(request.getResource());
        }
    }
}
