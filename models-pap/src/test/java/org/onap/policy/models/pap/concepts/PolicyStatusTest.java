/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;

/**
 * This only tests the methods that aren't already tested via TestModels.
 */
public class PolicyStatusTest {

    private static final String POLICY_VERSION = "1.2.3";
    private static final String MY_NAME = "my-name";
    private static final String TYPE_VERSION = "3.2.1";
    private static final String MY_TYPE = "my-type";

    private ToscaPolicyTypeIdentifier type;
    private ToscaPolicyIdentifier policy;
    private PolicyStatus status;

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        type = new ToscaPolicyTypeIdentifier(MY_TYPE, TYPE_VERSION);
        policy = new ToscaPolicyIdentifier(MY_NAME, POLICY_VERSION);
        status = new PolicyStatus(type, policy);
    }

    @Test
    public void test() throws CoderException {
        // test constructor with arguments
        assertEquals(MY_TYPE, status.getPolicyTypeId());
        assertEquals(TYPE_VERSION, status.getPolicyTypeVersion());
        assertEquals(MY_NAME, status.getPolicyId());
        assertEquals(POLICY_VERSION, status.getPolicyVersion());

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

    @Test
    public void testBumpSuccessCount() {
        assertTrue(status.isEmpty());
        status.bumpSuccessCount();
        status.bumpSuccessCount();
        assertEquals(2, status.getSuccessCount());
        assertFalse(status.isEmpty());
    }

    @Test
    public void testBumpFailureCount() {
        assertTrue(status.isEmpty());
        status.bumpFailureCount();
        status.bumpFailureCount();
        assertEquals(2, status.getFailureCount());
        assertFalse(status.isEmpty());
    }

    @Test
    public void testBumpIncompleteCount() {
        assertTrue(status.isEmpty());
        status.bumpIncompleteCount();
        status.bumpIncompleteCount();
        assertEquals(2, status.getIncompleteCount());
        assertFalse(status.isEmpty());
    }
}
