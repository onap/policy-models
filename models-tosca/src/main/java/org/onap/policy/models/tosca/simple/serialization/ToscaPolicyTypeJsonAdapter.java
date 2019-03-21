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
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import javax.ws.rs.core.Response;

import lombok.NonNull;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.tosca.simple.concepts.ToscaConstraint;
import org.onap.policy.models.tosca.simple.concepts.ToscaConstraintValidValues;
import org.onap.policy.models.tosca.simple.concepts.ToscaEntrySchema;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.ToscaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GSON type adapter for TOSCA policy types.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ToscaPolicyTypeJsonAdapter implements JsonSerializer<ToscaPolicyType>, JsonDeserializer<ToscaPolicyType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaPolicyTypeJsonAdapter.class);

    private static final String DERIVED_FROM = "derived_from";
    private static final String DESCRIPTION = "description";
    private static final String VERSION = "version";
    private static final String PROPERTIES = "properties";
    private static final String DEFAULT_VERSION = "1.0.0";

    @Override
    public ToscaPolicyType deserialize(@NonNull final JsonElement policyTypeElement, @NonNull final Type type,
            @NonNull final JsonDeserializationContext context) {

        // The incoming JSON
        final JsonObject policyTypeJsonMapObject = policyTypeElement.getAsJsonObject();

        // We should only have a single entry for the policy type
        if (policyTypeJsonMapObject.entrySet().size() != 1) {
            String errorMessage = "a policy type list entry may only contain one and only one policy type";
            LOGGER.debug(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }

        final String policyTypeName = policyTypeJsonMapObject.entrySet().iterator().next().getKey();
        final JsonObject policyTypeJsonObject = policyTypeJsonMapObject.entrySet().iterator().next()
                                            .getValue().getAsJsonObject();

        // Set keys
        PfConceptKey policyTypeKey;
        if (policyTypeJsonObject.get(VERSION) == null) {
            policyTypeKey = new PfConceptKey(policyTypeName, DEFAULT_VERSION);
        } else {
            policyTypeKey = new PfConceptKey(policyTypeName, policyTypeJsonObject.get(VERSION).getAsString());
        }
        ToscaPolicyType policyType = new ToscaPolicyType(policyTypeKey);

        // Set derived_from
        policyType.setDerivedFrom(new PfConceptKey(policyTypeJsonObject.get(DERIVED_FROM).getAsString(),
                DEFAULT_VERSION));

        // Set description
        if (policyTypeJsonObject.has(DESCRIPTION)) {
            final String policyTypeDescription = policyTypeJsonObject.get(DESCRIPTION).getAsString();
            policyType.setDescription(policyTypeDescription);
        }

        // Set properties
        if (policyTypeJsonObject.has(PROPERTIES)) {
            policyType.setProperties(
                    new ToscaPropertiesJsonAdapter().deserializeProperties(policyTypeJsonObject.get(PROPERTIES)));
            for (ToscaProperty property : policyType.getProperties()) {
                property.getKey().setParentConceptKey(policyTypeKey);
                property.getType().setVersion(policyType.getKey().getVersion());
            }
        }

        return policyType;
    }

    @Override
    public JsonElement serialize(@NonNull final ToscaPolicyType policyType, @NonNull final Type type,
            @NonNull final JsonSerializationContext context) {

        //TODO: Chenfei
        return new JsonObject();
    }
}