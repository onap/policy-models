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
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicyTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GSON type adapter for TOSCA policy types.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ToscaPolicyTypesJsonAdapter implements JsonSerializer<ToscaPolicyTypes>,
                                                    JsonDeserializer<ToscaPolicyTypes> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaPolicyTypesJsonAdapter.class);

    @Override
    public ToscaPolicyTypes deserialize(@NonNull final JsonElement policyTypesElement, @NonNull final Type type,
            @NonNull final JsonDeserializationContext context) {

        // The incoming JSON
        final JsonArray policyTypesJsonArray = policyTypesElement.getAsJsonArray();

        // The outgoing object
        final PfConceptKey policyTypesKey = new PfConceptKey("IncomingPolicyTypes", "0.0.1");
        final ToscaPolicyTypes policyTypes = new ToscaPolicyTypes(policyTypesKey);

        // Get the policyTypes
        Iterator<JsonElement> policyTypesIterator = policyTypesJsonArray.iterator();
        while (policyTypesIterator.hasNext()) {
            ToscaPolicyType policyType = new ToscaPolicyTypeJsonAdapter()
                    .deserialize(policyTypesIterator.next(), ToscaPolicyType.class, context);

            policyTypes.getConceptMap().put(policyType.getKey(), policyType);
        }

        return policyTypes;
    }

    @Override
    public JsonElement serialize(@NonNull final ToscaPolicyTypes policyTypes, @NonNull final Type type,
            @NonNull final JsonSerializationContext context) {

        JsonArray policyTypesJsonArray = new JsonArray();

        if (policyTypes.getConceptMap().isEmpty()) {
            String errorMessage = "policy type list is empty";
            LOGGER.debug(errorMessage);
            throw new PfModelRuntimeException(Response.Status.INTERNAL_SERVER_ERROR, errorMessage);
        }

        for (ToscaPolicyType policyType: policyTypes.getConceptMap().values()) {
            JsonElement policyTypeEntry = new  ToscaPolicyTypeJsonAdapter().serialize(policyType, type, context);
            policyTypesJsonArray.add(policyTypeEntry);
        }

        return policyTypesJsonArray;
    }
}