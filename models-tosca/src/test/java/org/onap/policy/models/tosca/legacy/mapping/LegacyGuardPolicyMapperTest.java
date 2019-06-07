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

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;

/**
 * Test the {@link LegacyGuardPolicyMapper} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyGuardPolicyMapperTest {

    @Test
    public void testLegacyGuardPolicyMapper() {
        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());

        JpaToscaPolicy policy = new JpaToscaPolicy(new PfConceptKey("PolicyName", "2.0.0"));
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(policy.getKey(), policy);

        assertThatThrownBy(() -> {
            new LegacyGuardPolicyMapper().fromToscaServiceTemplate(serviceTemplate);
        }).hasMessageContaining("no metadata defined on TOSCA policy");

        policy.setMetadata(new LinkedHashMap<>());
        assertThatThrownBy(() -> {
            new LegacyGuardPolicyMapper().fromToscaServiceTemplate(serviceTemplate);
        }).hasMessageContaining("no properties defined on TOSCA policy");

        policy.setProperties(new LinkedHashMap<>());
        Map<String, LegacyGuardPolicyOutput> guardPolicyMap =
                new LegacyGuardPolicyMapper().fromToscaServiceTemplate(serviceTemplate);
        LegacyGuardPolicyOutput guardPolicy = guardPolicyMap.values().iterator().next();
        assertEquals("2.0.0", guardPolicy.getVersion());
    }
}
