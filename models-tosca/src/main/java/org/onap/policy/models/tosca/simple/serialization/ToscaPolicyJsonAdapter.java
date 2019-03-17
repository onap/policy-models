/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.tosca.simple.serialization;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GSON type adapter for TOSCA policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaPolicyJsonAdapter implements JsonSerializer<ToscaPolicy>, JsonDeserializer<ToscaPolicy> {
    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaPolicyJsonAdapter.class);

    @Override
    public ToscaPolicy deserialize(@NonNull final JsonElement policyElement, @NonNull final Type type,
            @NonNull final JsonDeserializationContext context) {

        // The incoming JSON
        final JsonObject policyJsonMapObject = policyElement.getAsJsonObject();

        // We should only have a single entry for the policy
        if (policyJsonMapObject.entrySet().size() != 1) {
            String errorMessage = "a policy list entry may only contain one and only one policy";
            LOGGER.debug(errorMessage);
            throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, errorMessage);
        }

        String policyName = policyJsonMapObject.entrySet().iterator().next().getKey();
        JsonObject policyJsonObject = policyJsonMapObject.entrySet().iterator().next().getValue().getAsJsonObject();

        PfConceptKey policyKey = new PfConceptKey(policyName, policyJsonObject.get("version").getAsString());
        PfConceptKey policyTypeKey = new PfConceptKey(
                policyJsonObject.get("type").getAsString(),
                policyJsonObject.get("version").getAsString());
        ToscaPolicy policy = new ToscaPolicy(policyKey, policyTypeKey);

        // TODO: Rest of parsing

        return policy;
    }

    @Override
    public JsonElement serialize(@NonNull final ToscaPolicy policy, @NonNull final Type type,
            @NonNull final JsonSerializationContext context) {

        return null;
    }
}
