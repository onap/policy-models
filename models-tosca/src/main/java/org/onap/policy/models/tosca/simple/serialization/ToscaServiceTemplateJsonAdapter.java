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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import lombok.NonNull;

import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;

/**
 * GSON type adapter for TOSCA policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ToscaServiceTemplateJsonAdapter
        implements JsonSerializer<JpaToscaServiceTemplate>, JsonDeserializer<JpaToscaServiceTemplate> {

    private static final String TOPOLOGY_TEMPLATE = "topology_template";
    private static final String TOSCA_DEFINITIONS_VERSION = "tosca_definitions_version";
    private static final String POLICY_TYPES = "policy_types";
    private static final String DATA_TYPES = "data_types";

    @Override
    public JpaToscaServiceTemplate deserialize(@NonNull final JsonElement serviceTemplateElement,
            @NonNull final Type type, @NonNull final JsonDeserializationContext context) {

        // The incoming JSON
        final JsonObject serviceTemplateJsonObject = serviceTemplateElement.getAsJsonObject();

        // The outgoing object
        final JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate
                .setToscaDefinitionsVersion(serviceTemplateJsonObject.get(TOSCA_DEFINITIONS_VERSION).getAsString());

        // Set topology_template
        if (serviceTemplateJsonObject.has(TOPOLOGY_TEMPLATE)) {
            serviceTemplate.setTopologyTemplate(new ToscaTopologyTemplateJsonAdapter().deserialize(
                    serviceTemplateJsonObject.get(TOPOLOGY_TEMPLATE), JpaToscaTopologyTemplate.class, context));
        }

        // Set policy_types
        if (serviceTemplateJsonObject.has(POLICY_TYPES)) {
            serviceTemplate.setPolicyTypes(new ToscaPolicyTypesJsonAdapter()
                    .deserialize(serviceTemplateJsonObject.get(POLICY_TYPES), JpaToscaPolicyTypes.class, context));
        }

        // Set data_types
        if (serviceTemplateJsonObject.has(DATA_TYPES)) {
            serviceTemplate.setDataTypes(new ToscaDataTypesJsonAdapter()
                    .deserialize(serviceTemplateJsonObject.get(DATA_TYPES), JpaToscaDataTypes.class, context));
        }

        return serviceTemplate;
    }

    @Override
    public JsonElement serialize(@NonNull final JpaToscaServiceTemplate serviceTemplate, @NonNull final Type type,
            @NonNull final JsonSerializationContext context) {

        JsonObject serviceTemplateJsonObject = new JsonObject();

        // Serialize tosca_definitions_version
        if (serviceTemplate.getToscaDefinitionsVersion() != null) {
            serviceTemplateJsonObject.addProperty(TOSCA_DEFINITIONS_VERSION,
                    serviceTemplate.getToscaDefinitionsVersion());
        }

        // Serialize topoligy_template
        if (serviceTemplate.getTopologyTemplate() != null) {
            JsonElement topologyTemplateJsonElement = new ToscaTopologyTemplateJsonAdapter()
                    .serialize(serviceTemplate.getTopologyTemplate(), type, context);
            serviceTemplateJsonObject.add(TOPOLOGY_TEMPLATE, topologyTemplateJsonElement);
        }

        // Serialize policy_types
        if (serviceTemplate.getPolicyTypes() != null) {
            JsonElement policyTypesJsonElement =
                    new ToscaPolicyTypesJsonAdapter().serialize(serviceTemplate.getPolicyTypes(), type, context);
            serviceTemplateJsonObject.add(POLICY_TYPES, policyTypesJsonElement);
        }

        // Serialize data_types
        if (serviceTemplate.getDataTypes() != null) {
            JsonElement dataTypesJsonElement =
                    new ToscaDataTypesJsonAdapter().serialize(serviceTemplate.getDataTypes(), type, context);
            serviceTemplateJsonObject.add(DATA_TYPES, dataTypesJsonElement);
        }

        return serviceTemplateJsonObject;
    }
}
