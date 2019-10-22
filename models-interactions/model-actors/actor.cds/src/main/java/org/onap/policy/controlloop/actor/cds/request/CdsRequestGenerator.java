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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;

/**
 * Custom GSON serializer to generate the CDS gRPC request payload from the action-name (or operational policy recipe).
 * The CDS gRPC request payload generation follows the below pattern:
 * {
 *   "{@link CdsActionRequest#getActionName()}-request": {
 *     "resolution-key": "{@link CdsActionRequest#getResolutionKey()} ()}",
 *     "{@link CdsActionRequest#getActionName()}-properties": {
 *       "{@link CdsActionRequest#getAaiProperties()} ()}",
 *       "{@link CdsActionRequest#getPolicyPayload()} ()}"
 *     }
 *   }
 * }
 */
class CdsRequestGenerator implements JsonSerializer<CdsActionRequest> {

    @Override
    public JsonElement serialize(CdsActionRequest cdsActionRequest, Type type,
            JsonSerializationContext jsonSerializationContext) {

        // 1. Build the innermost child-node to include AAI properties and policy payload information
        JsonObject cdsActionPropsObj = new JsonObject();
        cdsActionRequest.getAaiProperties().forEach(cdsActionPropsObj::addProperty);
        cdsActionRequest.getPolicyPayload().forEach(cdsActionPropsObj::addProperty);

        // 2. Build the child-node to enclose CDS action request properties object from (1) and resolution-key
        JsonObject cdsActionRequestObj = new JsonObject();
        cdsActionRequestObj.addProperty(CdsActorConstants.KEY_RESOLUTION_KEY, cdsActionRequest.getResolutionKey());
        cdsActionRequestObj.add(generateCdsActionPropertiesKey(cdsActionRequest), cdsActionPropsObj);

        // 2. Build the root-node for CDS action request object
        JsonObject rootNode = new JsonObject();
        rootNode.add(generateCdsActionRequestKey(cdsActionRequest), cdsActionRequestObj);
        return rootNode;
    }

    private String generateCdsActionPropertiesKey(CdsActionRequest cdsActionRequest) {
        return cdsActionRequest.getActionName() + CdsActorConstants.CDS_REQUEST_PROPERTIES_SUFFIX;
    }

    private String generateCdsActionRequestKey(CdsActionRequest cdsActionRequest) {
        return cdsActionRequest.getActionName() + CdsActorConstants.CDS_REQUEST_SUFFIX;
    }
}
