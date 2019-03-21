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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import javax.ws.rs.core.Response;

import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.tosca.simple.concepts.ToscaConstraint;
import org.onap.policy.models.tosca.simple.concepts.ToscaConstraintValidValues;
import org.onap.policy.models.tosca.simple.concepts.ToscaEntrySchema;
import org.onap.policy.models.tosca.simple.concepts.ToscaProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * GSON type adapter for TOSCA properties.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ToscaPropertiesJsonAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaPropertiesJsonAdapter.class);

    private static final String DESCRIPTION = "description";
    private static final String REQUIRED = "required";
    private static final String DEFAULT = "default";
    private static final String TYPE = "type";
    private static final String ENTRY_SCHEMA = "entry_schema";
    private static final String CONSTRAINTS = "constraints";
    private static final String EQUAL = "equal";
    private static final String VALID_VALUES = "valid_values";
    private static final String DEFAULT_VERSION = "1.0.0";

    /**
     * Deserializes the properties.
     *
     * @param propertiesElement the properties in JsonElement
     *
     * @return deserialized ToscaProperty list
     */
    public List<ToscaProperty> deserializeProperties(JsonElement propertiesElement) {

        final JsonObject propertiesMapObject = propertiesElement.getAsJsonObject();
        List<ToscaProperty> properties = new LinkedList<>();

        for (Entry<String, JsonElement> entry : propertiesMapObject.entrySet()) {
            final String propertyEntryKey = entry.getKey();
            final JsonElement propertyEntryVal = entry.getValue();

            // Set property: key and type
            ToscaProperty property = new ToscaProperty(
                    new PfReferenceKey(new PfConceptKey(), propertyEntryKey),
                    new PfConceptKey(propertyEntryVal.getAsJsonObject().get(TYPE).getAsString(), DEFAULT_VERSION));

            // Set property: description
            JsonObject propertyJsonObject = propertyEntryVal.getAsJsonObject();
            if (propertyJsonObject.has(DESCRIPTION)) {
                property.setDescription(propertyJsonObject.get(DESCRIPTION).getAsString());
            }

            // Set property: required
            if (propertyJsonObject.has(REQUIRED)) {
                property.setRequired(propertyJsonObject.get(REQUIRED).getAsBoolean());
            }

            // Set property: default
            if (propertyJsonObject.has(DEFAULT)) {
                property.setDefaultValue(propertyJsonObject.get(DEFAULT).getAsString());
            }

            // Set property: entry_schema
            if (propertyJsonObject.has(ENTRY_SCHEMA)) {
                checkEntrySchemaCompatibility(property.getType().getName());
                property.setEntrySchema(deserializeEntrySchema(propertyJsonObject.get(ENTRY_SCHEMA)));
                property.getEntrySchema().getKey().setParentConceptKey(property.getType());
                property.getEntrySchema().getType().setVersion(property.getType().getVersion());
            }

            // Set property: constraints
            if (propertyJsonObject.has(CONSTRAINTS)) {
                property.setConstraints(deserializeConstraints(propertyJsonObject.get(CONSTRAINTS)));
                for (ToscaConstraint c : property.getConstraints()) {
                    c.getKey().setParentConceptKey(property.getType());
                }
            }

            // Add property to properties list
            properties.add(property);
        }

        return properties;
    }

    private ToscaEntrySchema deserializeEntrySchema(JsonElement entrySchemaElement) {

        JsonObject entrySchemaJsonObject = entrySchemaElement.getAsJsonObject();

        // Set entry_schema: key and type
        ToscaEntrySchema entrySchema = new ToscaEntrySchema(
                new PfReferenceKey(new PfConceptKey(), ENTRY_SCHEMA),
                new PfConceptKey(entrySchemaJsonObject.get(ENTRY_SCHEMA).getAsJsonObject().get(TYPE).getAsString(),
                        DEFAULT_VERSION));

        // Set entry_schema: description
        if (entrySchemaJsonObject.has(DESCRIPTION)) {
            entrySchema.setDescription(entrySchemaJsonObject.get(DESCRIPTION).getAsString());
        }

        // Set entry_schema: constraints
        if (entrySchemaJsonObject.has(CONSTRAINTS)) {
            entrySchema.setConstraints(deserializeConstraints(entrySchemaJsonObject.get(CONSTRAINTS)));
            for (ToscaConstraint c : entrySchema.getConstraints()) {
                c.getKey().setParentConceptKey(entrySchema.getType());
            }
        }

        return entrySchema;
    }

    private List<ToscaConstraint> deserializeConstraints(JsonElement constraintsElement) {

        JsonArray constraintsJsonArray = constraintsElement.getAsJsonArray();
        List<ToscaConstraint> constraints = new LinkedList<>();

        for (Iterator<JsonElement> constraintsIter = constraintsJsonArray.iterator(); constraintsIter.hasNext(); ) {
            JsonObject constraintJsonObject = constraintsIter.next().getAsJsonObject();
            // Check which type of constraint it is
            // TODO: here we only check 'valid_values' and 'equal'
            if (constraintJsonObject.get(VALID_VALUES) != null) {
                List<String> validValues = new LinkedList<>();
                for (Iterator<JsonElement> validValuesIter = constraintJsonObject.get(VALID_VALUES).getAsJsonArray()
                        .iterator(); validValuesIter.hasNext(); ) {
                    validValues.add(validValuesIter.next().getAsString());
                }
                ToscaConstraint constraint = new ToscaConstraintValidValues(
                        new PfReferenceKey(new PfConceptKey(), VALID_VALUES), validValues);
                constraints.add(constraint);
            } else if (constraintJsonObject.get(EQUAL) != null) {
                List<String> equals = new LinkedList<>();
                equals.add(constraintJsonObject.get(EQUAL).getAsString());
                ToscaConstraint constraint = new ToscaConstraintValidValues(
                        new PfReferenceKey(new PfConceptKey(), EQUAL), equals);
                constraints.add(constraint);
            } else {
                String errorMessage = "specified constraint is neither valid_values nor equal";
                LOGGER.debug(errorMessage);
                throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
            }
        }

        return constraints;
    }

    private void checkEntrySchemaCompatibility(String type) {
        if (!("list".equalsIgnoreCase(type)) && !("map".equalsIgnoreCase(type))) {
            String errorMessage = "entry schema can only be specified for list or map property";
            LOGGER.debug(errorMessage);
            throw new PfModelRuntimeException(Response.Status.BAD_REQUEST, errorMessage);
        }
    }
}
