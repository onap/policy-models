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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.tosca.authorative.concepts.PlainToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.yaml.snakeyaml.Yaml;

/**
 * This class performs unit test of {@link PlainToscaServiceTemplateMapper}}
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class PlainToscaServiceTemplateMapperTest {

    private Gson defaultGson;
    private PlainToscaServiceTemplateMapper mapper;

    @Before
    public void setUp() {
        defaultGson = new Gson();
        mapper = new PlainToscaServiceTemplateMapper();
    }

    @Test
    public void testPlainToscaPolicies() throws JsonSyntaxException, IOException {
        try {
            String inputJson = ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json");

            PlainToscaServiceTemplate plainPolicies = defaultGson.fromJson(inputJson, PlainToscaServiceTemplate.class);
            ToscaServiceTemplate internalPolicies = mapper.toToscaServiceTemplate(plainPolicies);
            PlainToscaServiceTemplate plainPolicies2 = mapper.fromToscaServiceTemplate(internalPolicies);
            assertTrue(plainPolicies.equals(plainPolicies2));

        } catch (Exception e) {
            fail("no exception should be thrown");
        }
    }

    @Test
    public void testPlainToscaPolicyTypes() throws JsonSyntaxException, IOException {
        try {
            Yaml yaml = new Yaml();
            String inputYaml = ResourceUtils.getResourceAsString(
                    "policytypes/onap.policy.monitoring.cdap.tca.hi.lo.app.yaml");
            Object yamlObject = yaml.load(inputYaml);
            String yamlAsJsonString = defaultGson.toJson(yamlObject);

            PlainToscaServiceTemplate plainPolicyTypes = defaultGson.fromJson(yamlAsJsonString,
                    PlainToscaServiceTemplate.class);
            ToscaServiceTemplate internalPolicyTypes = mapper.toToscaServiceTemplate(plainPolicyTypes);
            PlainToscaServiceTemplate plainPolicyTypes2 = mapper.fromToscaServiceTemplate(internalPolicyTypes);
            ToscaServiceTemplate internalPolicyTypes2 = mapper.toToscaServiceTemplate(plainPolicyTypes2);
            assertTrue(internalPolicyTypes.compareTo(internalPolicyTypes2) == 0);

        } catch (Exception e) {
            fail("no exception should be thrown");
        }

    }
}
