/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
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

package org.onap.policy.models.tosca.authorative.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import org.junit.Test;

/**
 * Test of the {@link PolicyTypeImpl} class.
 */
public class PolicyTypeImplTest {

    @Test
    public void testPolicyTypeImpl() {
        assertThatThrownBy(() -> {
            new PolicyTypeImpl(null);
        }).hasMessageMatching("copyObject is marked .*on.*ull but is null");

        PolicyTypeImpl policyImpl = new PolicyTypeImpl();

        assertEquals(policyImpl, new PolicyTypeImpl(policyImpl));

        policyImpl.setMetadata(new HashMap<>());
        policyImpl.setPolicyTypeImplRef(new ToscaConceptIdentifier("dummyPolicyImpl", "0.0.1"));

        assertEquals(policyImpl, new PolicyTypeImpl(policyImpl));

        assertEquals(policyImpl.getMetadata(), new PolicyTypeImpl(policyImpl).getMetadata());
        assertEquals(policyImpl.getIdentifier(), new PolicyTypeImpl(policyImpl).getPolicyTypeImplRef());
    }
}