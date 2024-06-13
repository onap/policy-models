/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021, 2024 Nordix Foundation.
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

package org.onap.policy.models.pap.concepts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * This only tests the methods that aren't already tested via TestModels.
 */
class PolicyStatusTest {

    @Test
    void test() throws CoderException {
        ToscaConceptIdentifier type = new ToscaConceptIdentifier("my-type", "3.2.1");
        ToscaConceptIdentifier policy = new ToscaConceptIdentifier("my-name", "1.2.3");

        // test constructor with arguments
        PolicyStatus status = new PolicyStatus(type, policy);
        assertEquals("my-type", status.getPolicyTypeId());
        assertEquals("3.2.1", status.getPolicyTypeVersion());
        assertEquals("my-name", status.getPolicyId());
        assertEquals("1.2.3", status.getPolicyVersion());

        assertEquals(type, status.getPolicyType());
        assertEquals(policy, status.getPolicy());

        assertEquals(0, status.getSuccessCount());
        assertEquals(0, status.getFailureCount());
        assertEquals(0, status.getIncompleteCount());

        // change values
        status.setFailureCount(10);
        status.setIncompleteCount(20);
        status.setSuccessCount(30);

        // encode & decode
        StandardCoder coder = new StandardCoder();
        PolicyStatus status2 = coder.decode(coder.encode(status), PolicyStatus.class);

        // test equals() method (and verify encode/decode worked)
        assertEquals(status, status2);
    }
}
