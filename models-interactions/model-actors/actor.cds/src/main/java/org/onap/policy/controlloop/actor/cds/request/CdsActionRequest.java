/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds.request;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;

@Getter
@Setter
public class CdsActionRequest implements Serializable {

    private static final long serialVersionUID = -4172157702597791493L;
    private static final StandardCoder CODER = new StandardCoder();

    private String actionName;
    private String resolutionKey;
    private Map<String, String> aaiProperties;
    private Map<String, String> policyPayload;
    private Map<String, String> additionalEventParams;

    /**
     * Generate the CDS gRPC request payload from the action-name (aka operational policy recipe).
     * The CDS gRPC request payload generation follows the below pattern:
     *  {
     *    "{@link CdsActionRequest#getActionName()}-request": {
     *      "resolution-key": "{@link CdsActionRequest#getResolutionKey()}",
     *      "{@link CdsActionRequest#getActionName()}-properties": {
     *        "{@link CdsActionRequest#getAaiProperties()}",
     *        "{@link CdsActionRequest#getPolicyPayload()}",
     *        "{@link CdsActionRequest#getAdditionalEventParams()}"
     *      }
     *    }
     *  }
     * @return JSON string equivalent of the CDS request object
     * @throws CoderException if error occurs when serializing to JSON string
     */
    public String generateCdsPayload() throws CoderException {
        // 1. Build the innermost object to include AAI properties and policy payload information
        Map<String, String> cdsActionPropsMap = new LinkedHashMap<>();
        cdsActionPropsMap.putAll(aaiProperties);
        cdsActionPropsMap.putAll(policyPayload);
        cdsActionPropsMap.putAll(additionalEventParams);

        // 2. Build the enclosing CDS action request properties object to contain (1) and the resolution-key
        Map<String, Object> cdsActionRequestMap = new LinkedHashMap<>();
        cdsActionRequestMap.put(CdsActorConstants.KEY_RESOLUTION_KEY, resolutionKey);
        cdsActionRequestMap.put(generateCdsActionPropertiesKey(), cdsActionPropsMap);

        // 3. Finally build the CDS action request object
        Map<String, Object> cdsActionRequestObj = new LinkedHashMap<>();
        cdsActionRequestObj.put(generateCdsActionRequestKey(), cdsActionRequestMap);

        // 4. Serialize the CDS action request object
        return CODER.encode(cdsActionRequestObj);
    }

    private String generateCdsActionPropertiesKey() {
        return actionName + CdsActorConstants.CDS_REQUEST_PROPERTIES_SUFFIX;
    }

    private String generateCdsActionRequestKey() {
        return actionName + CdsActorConstants.CDS_REQUEST_SUFFIX;
    }
}
