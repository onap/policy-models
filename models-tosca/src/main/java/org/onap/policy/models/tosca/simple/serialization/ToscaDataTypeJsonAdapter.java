/*-
 * ============LICENSE_START=======================================================
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
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GSON type adapter for TOSCA data types.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ToscaDataTypeJsonAdapter implements JsonSerializer<JpaToscaDataType>, JsonDeserializer<JpaToscaDataType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaDataTypeJsonAdapter.class);

    private static final String DERIVED_FROM = "derived_from";
    private static final String DESCRIPTION = "description";
    private static final String VERSION = "version";
    private static final String PROPERTIES = "properties";
    private static final String DEFAULT_VERSION = "1.0.0";

    @Override
    public JpaToscaDataType deserialize(@NonNull final JsonElement dataTypeElement, @NonNull final Type type,
            @NonNull final JsonDeserializationContext context) {

        // The incoming JSON
        final JsonObject dataTypeJsonMapObject = dataTypeElement.getAsJsonObject();

        // We should only have a single entry for the policy type
        if (dataTypeJsonMapObject.entrySet().size() != 1) {
            String errorMessage = "a policy type list entry may only contain one and only one policy type";
            LOGGER.debug(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        final String dataTypeName = dataTypeJsonMapObject.entrySet().iterator().next().getKey();
        final JsonObject dataTypeJsonObject = dataTypeJsonMapObject.entrySet().iterator().next()
                                            .getValue().getAsJsonObject();

        // Set keys
        PfConceptKey dataTypeKey;
        if (dataTypeJsonObject.get(VERSION) == null) {
            dataTypeKey = new PfConceptKey(dataTypeName, DEFAULT_VERSION);
        } else {
            dataTypeKey = new PfConceptKey(dataTypeName, dataTypeJsonObject.get(VERSION).getAsString());
        }
        JpaToscaDataType dataType = new JpaToscaDataType(dataTypeKey);

        // Set derived_from
        dataType.setDerivedFrom(new PfConceptKey(dataTypeJsonObject.get(DERIVED_FROM).getAsString(),
                DEFAULT_VERSION));

        // Set description
        if (dataTypeJsonObject.has(DESCRIPTION)) {
            final String dataTypeDescription = dataTypeJsonObject.get(DESCRIPTION).getAsString();
            dataType.setDescription(dataTypeDescription);
        }

        // Set properties
        if (dataTypeJsonObject.has(PROPERTIES)) {
            dataType.setProperties(
                    new ToscaPropertiesJsonAdapter().deserializeProperties(dataTypeJsonObject.get(PROPERTIES)));
            for (JpaToscaProperty property : dataType.getProperties()) {
                property.getKey().setParentConceptKey(dataTypeKey);
                property.getType().setVersion(dataType.getKey().getVersion());
            }
        }

        return dataType;
    }

    @Override
    public JsonElement serialize(@NonNull final JpaToscaDataType dataType, @NonNull final Type type,
            @NonNull final JsonSerializationContext context) {

        JsonObject dataTypeValJsonObject = new JsonObject();

        // Add derived_from
        if (dataType.getDerivedFrom() != null) {
            dataTypeValJsonObject.addProperty(DERIVED_FROM, dataType.getDerivedFrom().getName());
        }

        // Add description
        if (dataType.getDescription() != null) {
            dataTypeValJsonObject.addProperty(DESCRIPTION, dataType.getDescription());
        }

        // Add version
        if (dataType.getKey().getVersion() != null) {
            dataTypeValJsonObject.addProperty(VERSION, dataType.getKey().getVersion());
        }

        // Add properties
        if (dataType.getProperties() != null) {
            JsonElement propertiesJsonElement = new ToscaPropertiesJsonAdapter()
                    .serializeProperties(dataType.getProperties());
            dataTypeValJsonObject.add(PROPERTIES, propertiesJsonElement);
        }

        JsonObject dataTypeJsonObject = new JsonObject();
        dataTypeJsonObject.add(dataType.getKey().getName(), dataTypeValJsonObject);
        return dataTypeJsonObject;
    }
}
