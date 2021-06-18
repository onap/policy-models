/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnr;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
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
public class PciCommonHeader implements Serializable {

    private static final long serialVersionUID = 5435363539127062114L;

    @SerializedName(value = "TimeStamp")
    private Instant timeStamp = Instant.now();

    @SerializedName(value = "APIVer")
    private String apiVer = "1.0";

    @SerializedName(value = "RequestID")
    private UUID requestId;

    @SerializedName(value = "SubRequestID")
    private String subRequestId;

    @SerializedName(value = "RequestTrack")
    private Map<String, String> requestTrack = new HashMap<>();

    @SerializedName(value = "Flags")
    private Map<String, String> flags = new HashMap<>();

    /**
     * Used to copy a pci common header.
     *
     * @param commonHeader a header that is defined by the Pci api guide that contains information
     *        about the request (requestId, flags, etc.)
     */
    public PciCommonHeader(PciCommonHeader commonHeader) {
        this.timeStamp = commonHeader.timeStamp;
        this.requestId = commonHeader.requestId;
        this.subRequestId = commonHeader.subRequestId;
        this.apiVer = commonHeader.getApiVer();
        if (commonHeader.requestTrack != null) {
            this.requestTrack.putAll(commonHeader.requestTrack);
        }
        if (commonHeader.flags != null) {
            this.flags.putAll(commonHeader.flags);
        }
    }
}
