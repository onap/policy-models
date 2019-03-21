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

import lombok.NonNull;

import org.onap.policy.models.tosca.simple.concepts.ToscaDataTypes;


/**
 * GSON type adapter for TOSCA data types.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ToscaDataTypesJsonAdapter implements JsonSerializer<ToscaDataTypes>, JsonDeserializer<ToscaDataTypes> {

    @Override
    public ToscaDataTypes deserialize(@NonNull final JsonElement policyTypesElement, @NonNull final Type type,
            @NonNull final JsonDeserializationContext context) {

        //TODO: Chenfei
        final ToscaDataTypes dataTypes = new ToscaDataTypes();
        return dataTypes;
    }

    @Override
    public JsonElement serialize(@NonNull final ToscaDataTypes policyTypes, @NonNull final Type type,
            @NonNull final JsonSerializationContext context) {

        //TODO: Chenfei
        JsonArray dataTypesJsonArray = new JsonArray();
        return dataTypesJsonArray;
    }
}
