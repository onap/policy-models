/*-
 * ============LICENSE_START=======================================================
 * controlloop
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

package org.onap.policy.controlloop;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class ControlLoopEvent implements Serializable {

    private static final long serialVersionUID = 2391252138583119195L;

    @SerializedName("requestID")
    private UUID requestId;
    @SerializedName("target_type")
    private String targetType;
    private String closedLoopControlName;
    private String version = "1.0.2";
    private String closedLoopEventClient;
    private String target;
    private String from;
    private String policyScope;
    private String policyName;
    private String policyVersion;
    private ControlLoopEventStatus closedLoopEventStatus;
    private Map<String, String> additionalEventParams;

    /**
     * Construct an instance from an existing instance.
     *
     * @param event the existing instance
     */
    public ControlLoopEvent(ControlLoopEvent event) {
        if (event == null) {
            return;
        }
        this.version =  event.version;
        this.closedLoopControlName = event.closedLoopControlName;
        this.requestId = event.requestId;
        this.closedLoopEventClient = event.closedLoopEventClient;
        this.targetType = event.targetType;
        this.target = event.target;
        this.from = event.from;
        this.policyScope = event.policyScope;
        this.policyName = event.policyName;
        this.policyVersion = event.policyVersion;
        this.closedLoopEventStatus = event.closedLoopEventStatus;
        this.additionalEventParams = event.additionalEventParams;
    }

    public boolean isEventStatusValid() {
        return this.closedLoopEventStatus != null;
    }
}
