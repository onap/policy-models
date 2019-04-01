/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.pdp.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * Test the copy constructor, as {@link TestModels} tests the other methods.
 */
public class TestPdpUpdate {

    @Test
    public void testCopyConstructor() {
        assertThatThrownBy(() -> new PdpUpdate(null)).isInstanceOf(NullPointerException.class);

        PdpUpdate orig = new PdpUpdate();

        // verify with null values
        assertEquals("PdpUpdate(name=null, pdpType=null, description=null, pdpGroup=null, "
                        + "pdpSubgroup=null, policies=null)", new PdpUpdate(orig).toString());

        // verify with all values
        orig.setDescription("my-description");
        orig.setName("my-name");
        orig.setPdpGroup("my-group");
        orig.setPdpSubgroup("my-subgroup");
        orig.setPdpType("my-type");

        ToscaPolicy policy1 = new ToscaPolicy();
        policy1.setName("policy-a");
        policy1.setVersion("1.2.3");

        ToscaPolicy policy2 = new ToscaPolicy();
        policy2.setName("policy-b");
        policy2.setVersion("4.5.6");

        List<ToscaPolicy> policies = Arrays.asList(policy1, policy2);
        orig.setPolicies(policies);

        PdpUpdate other = new PdpUpdate(orig);

        assertEquals("PdpUpdate(name=my-name, pdpType=my-type, description=my-description, "
                        + "pdpGroup=my-group, pdpSubgroup=my-subgroup, policies=["
                        + "ToscaPolicy(super=ToscaEntity(name=policy-a, version=1.2.3, derivedFrom=null, "
                        + "metadata=null, description=null), type=null, typeVersion=null, properties=null), "
                        + "ToscaPolicy(super=ToscaEntity(name=policy-b, version=4.5.6, derivedFrom=null, "
                        + "metadata=null, description=null), type=null, typeVersion=null, properties=null)])",
                        other.toString());

        // ensure list and items are not the same object
        assertTrue(other.getPolicies() != policies);
        assertTrue(other.getPolicies().get(0) != policies.get(0));
    }
}
