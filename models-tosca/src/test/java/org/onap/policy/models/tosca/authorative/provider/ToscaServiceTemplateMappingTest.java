/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2024 Nordix Foundation.
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

package org.onap.policy.models.tosca.authorative.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.utils.ToscaServiceTemplateUtils;
import org.yaml.snakeyaml.Yaml;

/**
 * This class performs unit test of {@link PlainToscaServiceTemplateMapper}}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
class ToscaServiceTemplateMappingTest {

    private StandardCoder standardCoder;
    private YamlJsonTranslator yamlJsonTranslator = new YamlJsonTranslator();

    @BeforeEach
    void setUp() {
        standardCoder = new StandardCoder();
    }

    @Test
    void testPlainToscaPolicies() throws Exception {
        String policyTypeInputJson =
                ResourceUtils.getResourceAsString("policytypes/onap.policies.monitoring.tcagen2.yaml");
        ToscaServiceTemplate plainPolicyTypes =
                yamlJsonTranslator.fromYaml(policyTypeInputJson, ToscaServiceTemplate.class);

        String policyInputJson = ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json");
        ToscaServiceTemplate plainPolicies = standardCoder.decode(policyInputJson, ToscaServiceTemplate.class);

        JpaToscaServiceTemplate policyTypeServiceTemplate = new JpaToscaServiceTemplate();
        policyTypeServiceTemplate.fromAuthorative(plainPolicyTypes);

        JpaToscaServiceTemplate policyFragmentServiceTemplate = new JpaToscaServiceTemplate();
        policyFragmentServiceTemplate.fromAuthorative(plainPolicies);

        JpaToscaServiceTemplate internalServiceTemplate =
                ToscaServiceTemplateUtils.addFragment(policyTypeServiceTemplate, policyFragmentServiceTemplate);

        assertTrue(internalServiceTemplate.validate("").isValid());
        ToscaServiceTemplate plainPolicies2 = internalServiceTemplate.toAuthorative();

        ToscaPolicy pp1 = plainPolicies.getToscaTopologyTemplate().getPolicies().get(0).values().iterator().next();
        ToscaPolicy pp2 = plainPolicies2.getToscaTopologyTemplate().getPolicies().get(0).values().iterator().next();

        assertEquals(pp1.getProperties().keySet(), pp2.getProperties().keySet());
    }

    @Test
    void testPlainToscaPolicyTypes() throws Exception {
        Yaml yaml = new Yaml();
        String inputYaml =
                ResourceUtils.getResourceAsString("policytypes/onap.policies.monitoring.tcagen2.yaml");
        Object yamlObject = yaml.load(inputYaml);
        String yamlAsJsonString = standardCoder.encode(yamlObject);

        ToscaServiceTemplate plainPolicyTypes = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);
        JpaToscaServiceTemplate internalPolicyTypes = new JpaToscaServiceTemplate();
        internalPolicyTypes.fromAuthorative(plainPolicyTypes);
        assertTrue(internalPolicyTypes.validate("").isValid());
        ToscaServiceTemplate plainPolicyTypes2 = internalPolicyTypes.toAuthorative();
        JpaToscaServiceTemplate internalPolicyTypes2 = new JpaToscaServiceTemplate();
        internalPolicyTypes2.fromAuthorative(plainPolicyTypes2);
        assertTrue(internalPolicyTypes2.validate("").isValid());
        assertEquals(0, internalPolicyTypes.compareTo(internalPolicyTypes2));
    }
}
