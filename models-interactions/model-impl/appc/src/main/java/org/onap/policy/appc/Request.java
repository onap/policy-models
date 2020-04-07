/*-
 * ============LICENSE_START=======================================================
 * appc
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Request implements Serializable {
    private static final long serialVersionUID = -3912323643990646431L;

    @SerializedName("CommonHeader")
    private CommonHeader commonHeader;

    @SerializedName("Action")
    private String action;

    @SerializedName("TargetID")
    private String targetId;

    @SerializedName("ObjectID")
    private String objectId;

    @SerializedName("Payload")
    private Map<String, Object> payload = new HashMap<>();

    public Request() {
        // Initiate an empty Request instance
    }

    @Override
    public String toString() {
        return "Request [CommonHeader=" + commonHeader + ", Action=" + action + ", TargetId=" + targetId + ", ObjectId="
                + objectId + ", Payload=" + payload + "]";
    }

}
