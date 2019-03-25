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

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Iterator;
import javax.ws.rs.core.Response;
import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.simple.concepts.ToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.ToscaDataTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GSON type adapter for TOSCA data types.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ToscaDataTypesJsonAdapter implements JsonSerializer<ToscaDataTypes>, JsonDeserializer<ToscaDataTypes> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaDataTypesJsonAdapter.class);

    @Override
    public ToscaDataTypes deserialize(@NonNull final JsonElement dataTypesElement, @NonNull final Type type,
            @NonNull final JsonDeserializationContext context) {

        // The incoming JSON
        final JsonArray dataTypesJsonArray = dataTypesElement.getAsJsonArray();

        // The outgoing object
        final PfConceptKey dataTypesKey = new PfConceptKey("IncomingDataTypes", "0.0.1");
        final ToscaDataTypes dataTypes = new ToscaDataTypes(dataTypesKey);

        // Get the dataTypes
        Iterator<JsonElement> dataTypesIterator = dataTypesJsonArray.iterator();
        while (dataTypesIterator.hasNext()) {
            ToscaDataType dataType = new ToscaDataTypeJsonAdapter()
                    .deserialize(dataTypesIterator.next(), ToscaDataType.class, context);

            dataTypes.getConceptMap().put(dataType.getKey(), dataType);
        }

        return dataTypes;
    }

    @Override
    public JsonElement serialize(@NonNull final ToscaDataTypes dataTypes, @NonNull final Type type,
            @NonNull final JsonSerializationContext context) {

        JsonArray dataTypesJsonArray = new JsonArray();

        if (dataTypes.getConceptMap().isEmpty()) {
            String errorMessage = "data type list is empty";
            LOGGER.debug(errorMessage);
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage);
        }

        for (ToscaDataType dataType: dataTypes.getConceptMap().values()) {
            JsonElement dataTypeEntry = new  ToscaDataTypeJsonAdapter().serialize(dataType, type, context);
            dataTypesJsonArray.add(dataTypeEntry);
        }

        return dataTypesJsonArray;
    }
}
