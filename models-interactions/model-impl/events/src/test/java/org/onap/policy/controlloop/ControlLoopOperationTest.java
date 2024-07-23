/*-
 * ============LICENSE_START=======================================================
 * controlloop
 * ================================================================================
 * Copyright (C) 2018-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.controlloop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ControlLoopOperationTest {

    @Test
    void test() {
        ControlLoopOperation operation = new ControlLoopOperation();

        assertEquals(operation, (Object) operation);
        assertNotEquals(operation, (Object) "");
        assertNotNull(operation);

        assertNotEquals(0, operation.hashCode());
        assertTrue(operation.toString().startsWith("ControlLoopOperation"));

        assertNotNull(operation);

        operation.setActor("actor");
        assertEquals("actor", operation.getActor());

        operation.setOperation("operation");
        assertEquals("operation", operation.getOperation());

        Instant now = Instant.now();
        operation.setStart(now);
        assertEquals(now, operation.getStart());
        operation.setEnd(now);
        assertEquals(now, operation.getEnd());

        operation.setMessage("message");
        assertEquals("message", operation.getMessage());

        operation.setOutcome("outcome");
        assertEquals("outcome", operation.getOutcome());

        operation.setSubRequestId("1");
        assertEquals("1", operation.getSubRequestId());

        operation.setTarget("target");
        assertEquals("target", operation.getTarget());

        assertNotEquals(0, operation.hashCode());

        ControlLoopOperation operation2 = new ControlLoopOperation(operation);
        assertEquals(now, operation2.getEnd());

        assertEquals(operation, operation2);

        operation2.setActor("foo");
        assertNotEquals(operation, operation2);

        operation = new ControlLoopOperation(null);
        assertNotNull(operation.getStart());

        assertNotEquals(operation, operation2);

        assertTrue(operation.toMessage().startsWith("actor="));
        assertTrue(operation.toHistory().startsWith("actor="));

    }
}
