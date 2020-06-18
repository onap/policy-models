/*-
 * ============LICENSE_START=======================================================
 * controlloop
 * ================================================================================
 * Copyright (C) 2019 Wipro Limited Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ControlLoopResponse implements Serializable {

    private static final long serialVersionUID = 2391252138583119195L;

    @SerializedName("requestID")
    private UUID requestId;
    private String closedLoopControlName;
    private String version = "1.0.0";
    private String target;
    private String from;
    private String policyName;
    private String policyVersion;
    private String payload;

    /**
     * Construct an instance from an existing instance.
     *
     * @param response
     *            the existing instance
     */
    public ControlLoopResponse(ControlLoopResponse response) {
        if (response == null) {
            return;
        }
        this.closedLoopControlName = response.closedLoopControlName;
        this.requestId = response.requestId;
        this.target = response.target;
        this.from = response.from;
        this.policyName = response.policyName;
        this.policyVersion = response.policyVersion;
        this.payload = response.payload;
        this.version = response.version;
    }
}
