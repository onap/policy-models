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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.tosca.legacy.mapping;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedHashMap;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.legacy.mapping.LegacyOperationalPolicyMapper;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test serialization of monitoring policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyOperationalPolicyMapperTest {
    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyOperationalPolicyMapperTest.class);

    private StandardCoder standardCoder;

    @Before
    public void setUp() {
        standardCoder = new StandardCoder();
    }

    @Test
    public void testJsonDeserialization() throws Exception {
        String vcpePolicyJson = ResourceUtils.getResourceAsString("policies/vCPE.policy.operational.input.json");

        LegacyOperationalPolicy legacyOperationalPolicy =
                standardCoder.decode(vcpePolicyJson, LegacyOperationalPolicy.class);

        JpaToscaServiceTemplate serviceTemplate =
                new LegacyOperationalPolicyMapper().toToscaServiceTemplate(legacyOperationalPolicy);

        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        assertEquals("operational.restart:1.0.0",
                serviceTemplate.getTopologyTemplate().getPolicies().get("operational.restart").getId());
    }

    @Test
    public void testOperationalPolicyMapper() throws Exception {
        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());

        JpaToscaPolicy policy0 = new JpaToscaPolicy(new PfConceptKey("PolicyName0", "0.0.1"));
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(policy0.getKey(), policy0);
        JpaToscaPolicy policy1 = new JpaToscaPolicy(new PfConceptKey("PolicyName1", "0.0.1"));
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(policy1.getKey(), policy1);

        assertThatThrownBy(() -> {
            new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(serviceTemplate);
        }).hasMessage("more than one policy found in service template");

        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().remove(policy1.getKey());

        assertThatThrownBy(() -> {
            new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(serviceTemplate);
        }).hasMessage("no properties defined on TOSCA policy");

        policy0.setProperties(new LinkedHashMap<>());
        assertThatThrownBy(() -> {
            new LegacyOperationalPolicyMapper().fromToscaServiceTemplate(serviceTemplate);
        }).hasMessage("property \"content\" not defined on TOSCA policy");
    }
}
