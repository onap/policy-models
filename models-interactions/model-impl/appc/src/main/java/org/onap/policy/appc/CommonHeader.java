/*-
 * ============LICENSE_START=======================================================
 * appc
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@ToString
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

    /**
     * Construct an instance from an existing instance.
     *
     * @param commonHeader the existing instance
     */
    public CommonHeader(CommonHeader commonHeader) {
        this.originatorId = commonHeader.originatorId;
        this.requestId = commonHeader.requestId;
        this.subRequestId = commonHeader.subRequestId;
        this.timeStamp = commonHeader.getTimeStamp();
        this.apiVer = commonHeader.getApiVer();
        if (commonHeader.requestTrack != null) {
            this.requestTrack.addAll(commonHeader.requestTrack);
        }
        if (commonHeader.flags != null) {
            this.flags.addAll(commonHeader.flags);
        }
    }
}
