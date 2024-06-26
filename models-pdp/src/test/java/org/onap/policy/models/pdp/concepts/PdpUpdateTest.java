/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.pdp.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.onap.policy.models.pdp.concepts.PdpMessageUtils.removeVariableFields;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * Test the copy constructor, as {@link ModelsTest} tests the other methods.
 */
class PdpUpdateTest {

    @Test
    void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpUpdate(null)).isInstanceOf(NullPointerException.class);

        PdpUpdate orig = new PdpUpdate();
        orig.setPoliciesToBeDeployed(null);
        orig.setPoliciesToBeUndeployed(null);

        // verify with null values
        assertEquals(removeVariableFields(orig.toString()), removeVariableFields(new PdpUpdate(orig).toString()));

        // verify with all values
        orig.setSource("my-source");
        orig.setDescription("my-description");
        orig.setName("my-name");
        orig.setPdpGroup("my-group");
        orig.setPdpSubgroup("my-subgroup");
        orig.setPdpHeartbeatIntervalMs(30000L);

        ToscaPolicy policy1 = new ToscaPolicy();
        policy1.setName("policy-a");
        policy1.setVersion("1.2.3");

        ToscaPolicy policy2 = new ToscaPolicy();
        policy2.setName("policy-b");
        policy2.setVersion("4.5.6");

        List<ToscaPolicy> policies = Arrays.asList(policy1, policy2);
        orig.setPoliciesToBeDeployed(policies);
        orig.setPoliciesToBeUndeployed(policies.stream().map(ToscaPolicy::getIdentifier).collect(Collectors.toList()));

        PdpUpdate other = new PdpUpdate(orig);

        assertEquals(removeVariableFields(orig.toString()), removeVariableFields(other.toString()));

        // ensure list and items are not the same object
        assertNotSame(other.getPoliciesToBeDeployed(), policies);
        assertNotSame(other.getPoliciesToBeDeployed().get(0), policies.get(0));
        assertNotSame(other.getPoliciesToBeUndeployed(), policies);
        assertNotSame(other.getPoliciesToBeUndeployed().get(0), policies.get(0));
    }
}
