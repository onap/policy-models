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
import org.onap.policy.models.tosca.simple.concepts.ToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.ToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.ToscaTopologyTemplate;
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
                .registerTypeAdapter(ToscaServiceTemplate.class, new ToscaServiceTemplateJsonAdapter())
                .registerTypeAdapter(ToscaTopologyTemplate.class, new ToscaTopologyTemplateJsonAdapter())
                .registerTypeAdapter(ToscaPolicies.class, new ToscaPoliciesJsonAdapter())
                .registerTypeAdapter(ToscaPolicy.class, new ToscaPolicyJsonAdapter())
                .registerTypeAdapter(ToscaPolicyTypes.class, new ToscaPolicyTypesJsonAdapter())
                .registerTypeAdapter(ToscaPolicyType.class, new ToscaPolicyTypeJsonAdapter())
                .registerTypeAdapter(ToscaDataTypes.class, new ToscaDataTypesJsonAdapter())
                .registerTypeAdapter(ToscaDataType.class, new ToscaDataTypeJsonAdapter())
                .setPrettyPrinting()
                .create()
        );
        // @formatter:on
    }

}
