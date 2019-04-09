/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Model
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * This class performs unit test of {@link PlainToscaServiceTemplateMapper}}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ToscaServiceTemplateMappingTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToscaServiceTemplateMappingTest.class);

    private StandardCoder standardCoder;

    @Before
    public void setUp() {
        standardCoder = new StandardCoder();
    }

    @Test
    public void testPlainToscaPolicies() throws Exception {
        try {
            String inputJson = ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json");

            ToscaServiceTemplate plainPolicies = standardCoder.decode(inputJson, ToscaServiceTemplate.class);
            JpaToscaServiceTemplate internalPolicies = new JpaToscaServiceTemplate();
            internalPolicies.fromAuthorative(plainPolicies);

            assertTrue(internalPolicies.validate(new PfValidationResult()).isValid());
            ToscaServiceTemplate plainPolicies2 = internalPolicies.toAuthorative();

            ToscaPolicy pp1 = plainPolicies.getToscaTopologyTemplate().getPolicies().get(0).values().iterator().next();
            ToscaPolicy pp2 = plainPolicies2.getToscaTopologyTemplate().getPolicies().get(0).values().iterator().next();

            assertEquals(pp1.getProperties().keySet(), pp2.getProperties().keySet());

        } catch (Exception e) {
            LOGGER.warn("no exception should be thrown", e);
            fail("no exception should be thrown");
        }
    }

    @Test
    public void testPlainToscaPolicyTypes() throws Exception {
        try {
            Yaml yaml = new Yaml();
            String inputYaml = ResourceUtils.getResourceAsString(
                    "policytypes/onap.policy.monitoring.cdap.tca.hi.lo.app.yaml");
            Object yamlObject = yaml.load(inputYaml);
            String yamlAsJsonString = standardCoder.encode(yamlObject);

            ToscaServiceTemplate plainPolicyTypes = standardCoder.decode(yamlAsJsonString,
                    ToscaServiceTemplate.class);
            JpaToscaServiceTemplate internalPolicyTypes = new JpaToscaServiceTemplate();
            internalPolicyTypes.fromAuthorative(plainPolicyTypes);
            assertTrue(internalPolicyTypes.validate(new PfValidationResult()).isValid());
            ToscaServiceTemplate plainPolicyTypes2 = internalPolicyTypes.toAuthorative();
            JpaToscaServiceTemplate internalPolicyTypes2 = new JpaToscaServiceTemplate();
            internalPolicyTypes2.fromAuthorative(plainPolicyTypes2);
            assertTrue(internalPolicyTypes2.validate(new PfValidationResult()).isValid());
            assertTrue(internalPolicyTypes.compareTo(internalPolicyTypes2) == 0);

        } catch (Exception e) {
            LOGGER.warn("no exception should be thrown", e);
            fail("no exception should be thrown");
        }

    }
}
