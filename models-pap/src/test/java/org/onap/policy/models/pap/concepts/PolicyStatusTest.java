/*-
 * ============LICENSE_START=======================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.pap.concepts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;

/**
 * This only tests the methods that aren't already tested via TestModels.
 */
public class PolicyStatusTest {

    @Test
    public void test() throws CoderException {
        ToscaPolicyIdentifier ident = new ToscaPolicyIdentifier();
        ident.setName("my-name");
        ident.setVersion("1.2.3");

        // test constructor with arguments
        PolicyStatus status = new PolicyStatus(ident);
        assertSame(ident, status.getPolicyId());

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
