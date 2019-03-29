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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.tosca.simple.serialization;

import com.google.gson.GsonBuilder;

import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider used to serialize and deserialize TOSCA objects using GSON.
 */
public class ToscaServiceTemplateMessageBodyHandler extends GsonMessageBodyHandler {

    public static final Logger logger = LoggerFactory.getLogger(ToscaServiceTemplateMessageBodyHandler.class);

    /**
     * Constructs the object.
     */
    public ToscaServiceTemplateMessageBodyHandler() {
        this(new GsonBuilder());

        logger.info("Using GSON with TOSCA for REST calls");
    }

    /**
     * Constructs the object.
     *
     * @param builder builder to use to create the gson object
     */
    public ToscaServiceTemplateMessageBodyHandler(final GsonBuilder builder) {
        // @formatter:off
        super(builder
                .registerTypeAdapter(JpaToscaServiceTemplate.class, new ToscaServiceTemplateJsonAdapter())
                .registerTypeAdapter(JpaToscaTopologyTemplate.class, new ToscaTopologyTemplateJsonAdapter())
                .registerTypeAdapter(JpaToscaPolicies.class, new ToscaPoliciesJsonAdapter())
                .registerTypeAdapter(JpaToscaPolicy.class, new ToscaPolicyJsonAdapter())
                .registerTypeAdapter(JpaToscaPolicyTypes.class, new ToscaPolicyTypesJsonAdapter())
                .registerTypeAdapter(JpaToscaPolicyType.class, new ToscaPolicyTypeJsonAdapter())
                .registerTypeAdapter(JpaToscaDataTypes.class, new ToscaDataTypesJsonAdapter())
                .registerTypeAdapter(JpaToscaDataType.class, new ToscaDataTypeJsonAdapter())
                .setPrettyPrinting()
                .create()
        );
        // @formatter:on
    }

}
