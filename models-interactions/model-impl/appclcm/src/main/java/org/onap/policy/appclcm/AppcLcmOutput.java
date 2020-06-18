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
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class AppcLcmOutput implements Serializable {

    private static final long serialVersionUID = 6332508597287669750L;

    @SerializedName(value = "common-header")
    private AppcLcmCommonHeader commonHeader;

    @SerializedName(value = "status")
    private AppcLcmResponseStatus status = new AppcLcmResponseStatus();

    @SerializedName(value = "payload")
    private String payload;

    /**
     * Constructs a response using the common header of the request since they will be the same.
     *
     * @param request an appc lcm request object specified by the lcm api guide
     */
    public AppcLcmOutput(AppcLcmInput request) {
        this.commonHeader = new AppcLcmCommonHeader(request.getCommonHeader());
        String requestPayload = request.getPayload();
        if (requestPayload != null) {
            this.payload = requestPayload;
        }
    }

}
