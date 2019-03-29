/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GSON type adapter for TOSCA policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ToscaPolicyJsonAdapter implements JsonSerializer<JpaToscaPolicy>, JsonDeserializer<JpaToscaPolicy> {
    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaPolicyJsonAdapter.class);

    private static final String TYPE = "type";
    private static final String DESCRIPTION = "description";
    private static final String VERSION = "version";
    private static final String METADATA = "metadata";
    private static final String PROPERTIES = "properties";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public JpaToscaPolicy deserialize(@NonNull final JsonElement policyElement, @NonNull final Type type,
            @NonNull final JsonDeserializationContext context) {

        // The incoming JSON
        final JsonObject policyJsonMapObject = policyElement.getAsJsonObject();

        // We should only have a single entry for the policy
        if (policyJsonMapObject.entrySet().size() != 1) {
            String errorMessage = "a policy list entry may only contain one and only one policy";
            LOGGER.debug(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        final String policyName = policyJsonMapObject.entrySet().iterator().next().getKey();
        final JsonObject policyJsonObject = policyJsonMapObject.entrySet().iterator().next()
                                            .getValue().getAsJsonObject();

        // Set keys
        PfConceptKey policyKey = new PfConceptKey(policyName, policyJsonObject.get(VERSION).getAsString());
        PfConceptKey policyTypeKey = new PfConceptKey(
                policyJsonObject.get(TYPE).getAsString(),
                policyJsonObject.get(VERSION).getAsString());
        JpaToscaPolicy policy = new JpaToscaPolicy(policyKey, policyTypeKey);

        // Set description
        if (policyJsonObject.has(DESCRIPTION)) {
            final String policyDescription = policyJsonObject.get(DESCRIPTION).getAsString();
            policy.setDescription(policyDescription);
        }

        // Set metadata
        if (policyJsonObject.has(METADATA)) {
            final JsonObject policyMetadataMapObject = policyJsonObject.get(METADATA).getAsJsonObject();
            Map<String, String> policyMetadataMap = new HashMap<>();
            for (Entry<String, JsonElement> entry : policyMetadataMapObject.entrySet()) {
                policyMetadataMap.put(entry.getKey(), entry.getValue().getAsString());
            }
            policy.setMetadata(policyMetadataMap);
        }

        // Set properties
        if (policyJsonObject.has(PROPERTIES)) {
            final JsonObject policyPropertiesMapObject = policyJsonObject.get(PROPERTIES).getAsJsonObject();
            Map<String, String> propertiesMap = new HashMap<>();
            for (Entry<String, JsonElement> entry : policyPropertiesMapObject.entrySet()) {
                // TODO: This is a HACK, we need to validate the properties against their
                // TODO: their data type in their policy type definition in TOSCA, which means reading
                // TODO: the policy type from the database and parsing the property value object correctly
                // TODO: Here we are simply serializing the property value into a string and storing it
                // TODO: unvalidated into the database
                propertiesMap.put(entry.getKey(), gson.toJson(entry.getValue()));
            }
            policy.setProperties(propertiesMap);
        }
        return policy;
    }

    @Override
    public JsonElement serialize(@NonNull final JpaToscaPolicy policy, @NonNull final Type type,
            @NonNull final JsonSerializationContext context) {

        JsonObject policyValJsonObject = new JsonObject();

        // Add type
        policyValJsonObject.addProperty(TYPE, policy.getType().getName());

        // Add version
        policyValJsonObject.addProperty(VERSION, policy.getType().getVersion());

        // Add description
        if (policy.getDescription() != null) {
            policyValJsonObject.addProperty(DESCRIPTION, policy.getDescription());
        }

        // Add metadata
        if (policy.getMetadata() != null) {
            JsonObject metadataMapObject = new JsonObject();
            for (Entry<String, String> entry : policy.getMetadata().entrySet()) {
                metadataMapObject.addProperty(entry.getKey(), entry.getValue());
            }
            policyValJsonObject.add(METADATA, metadataMapObject);
        }

        // Add properties
        if (policy.getProperties() != null) {
            JsonObject propertiesMapObject = new JsonObject();
            for (Entry<String, String> entry : policy.getProperties().entrySet()) {
                // TODO: This is the other direction of the HACK
                JsonElement valueObject = gson.fromJson(entry.getValue(), JsonElement.class);
                propertiesMapObject.add(entry.getKey(), valueObject);
            }
            policyValJsonObject.add(PROPERTIES, propertiesMapObject);
        }

        JsonObject policyJsonObject = new JsonObject();
        policyJsonObject.add(policy.getKey().getName(), policyValJsonObject);
        return policyJsonObject;
    }
}
