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
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@NoArgsConstructor
@RequiredArgsConstructor
@Data
public class AppcLcmCommonHeader implements Serializable {

    private static final long serialVersionUID = 6581963539127062114L;

    @SerializedName(value = "timestamp")
    private Instant timeStamp = Instant.now();

    @SerializedName(value = "api-ver")
    private String apiVer = "2.00";

    @NonNull
    @SerializedName(value = "originator-id")
    private String originatorId;

    @NonNull
    @SerializedName(value = "request-id")
    private UUID requestId;

    @NonNull
    @SerializedName(value = "sub-request-id")
    private String subRequestId;

    @SerializedName(value = "flags")
    private Map<String, String> flags = new HashMap<>();

    /**
     * Used to copy a common header.
     *
     * @param commonHeader a header that is defined by the lcm api guide that contains information
     *        about the request (requestId, flags, etc.)
     */
    public AppcLcmCommonHeader(AppcLcmCommonHeader commonHeader) {
        this.originatorId = commonHeader.originatorId;
        this.requestId = commonHeader.requestId;
        this.subRequestId = commonHeader.subRequestId;
        this.timeStamp = commonHeader.timeStamp;
        if (commonHeader.flags != null) {
            this.flags.putAll(commonHeader.flags);
        }
    }

}
