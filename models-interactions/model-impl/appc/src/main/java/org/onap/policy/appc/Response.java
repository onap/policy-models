/*-
 * ============LICENSE_START=======================================================
 * appc
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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
import java.util.HashMap;
import java.util.Map;
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
public class Response implements Serializable {

    private static final long serialVersionUID = 434953706339865151L;

    @SerializedName("CommonHeader")
    private CommonHeader commonHeader;

    /**
     * This should only be populated if the incoming message actually has a "Status"
     * field. Otherwise, actor.appc will be unable to use this to distinguish between
     * Request and Response objects.
     */
    @SerializedName("Status")
    private ResponseStatus status;

    @SerializedName("Payload")
    private Map<String, Object> payload = new HashMap<>(); //NOSONAR

    /**
     * Construct an instance from an existing instance.
     *
     * @param request the existing instance
     */
    public Response(Request request) {
        if (request.getCommonHeader() != null) {
            this.commonHeader = new CommonHeader(request.getCommonHeader());
        }
        if (request.getPayload() != null) {
            this.payload.putAll(request.getPayload());
        }
    }
}
