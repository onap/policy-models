/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.tosca.authorative.mapping;

import com.google.gson.Gson;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.mapping.JpaToscaServiceTemplateMapper;
import org.onap.policy.models.tosca.simple.serialization.ToscaServiceTemplateMessageBodyHandler;

/**
 * This class maps a TOSCA service template from client input form to internal representation and vice verse.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class PlainToscaServiceTemplateMapper
        implements JpaToscaServiceTemplateMapper<ToscaServiceTemplate, ToscaServiceTemplate> {

    private Gson defaultGson = new Gson();
    private Gson customGson = new ToscaServiceTemplateMessageBodyHandler().getGson();

    @Override
    public JpaToscaServiceTemplate toToscaServiceTemplate(ToscaServiceTemplate otherPolicy) {

        String serializedServiceTemplate = defaultGson.toJson(otherPolicy);
        return customGson.fromJson(serializedServiceTemplate, JpaToscaServiceTemplate.class);

    }

    @Override
    public ToscaServiceTemplate fromToscaServiceTemplate(JpaToscaServiceTemplate serviceTemplate) {

        String serializedServiceTemplate = customGson.toJson(serviceTemplate);
        return defaultGson.fromJson(serializedServiceTemplate, ToscaServiceTemplate.class);
    }
}
