/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2024 Nordix Foundation
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

/**
 * This only tests the methods that aren't already tested via TestModels.
 */
class PolicyNotificationTest {

    @Test
    void test() throws CoderException {
        PolicyStatus statusAdd1 = new PolicyStatus();
        statusAdd1.setSuccessCount(10);
        PolicyStatus statusAdd2 = new PolicyStatus();
        statusAdd2.setFailureCount(20);
        List<PolicyStatus> add = Arrays.asList(statusAdd1, statusAdd2);

        PolicyStatus statusDel1 = new PolicyStatus();
        statusDel1.setIncompleteCount(30);
        PolicyStatus statusDel2 = new PolicyStatus();
        List<PolicyStatus> del = Arrays.asList(statusDel1, statusDel2);

        // test constructor with arguments
        PolicyNotification notify = new PolicyNotification(add, del);
        assertSame(add, notify.getAdded());
        assertSame(del, notify.getDeleted());

        // encode & decode
        StandardCoder coder = new StandardCoder();
        PolicyNotification notify2 = coder.decode(coder.encode(notify), PolicyNotification.class);

        // test equals() method (and verify encode/decode worked)
        assertEquals(notify, notify2);

        /*
         * Test isEmpty()
         */
        assertFalse(notify.isEmpty());
        assertFalse(notify2.isEmpty());
        assertTrue(new PolicyNotification().isEmpty());
        assertFalse(new PolicyNotification(add, Collections.emptyList()).isEmpty());
        assertFalse(new PolicyNotification(Collections.emptyList(), del).isEmpty());
    }
}
