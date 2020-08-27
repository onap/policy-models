/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.controlloop.ControlLoopOperation;

public class OperationOutcomeTest {
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";
    private static final String TARGET = "my-target";
    private static final Instant START = Instant.ofEpochMilli(10);
    private static final Instant END = Instant.ofEpochMilli(20);
    private static final String SUB_REQ_ID = "my-sub-request-id";
    private static final OperationResult RESULT = OperationResult.FAILURE_GUARD;
    private static final String MESSAGE = "my-message";
    private static final String RESPONSE = "my-response";

    private OperationOutcome outcome;

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        outcome = new OperationOutcome();
    }

    @Test
    public void testOperationOutcomeOperationOutcome() {
        setAll();

        OperationOutcome outcome2 = new OperationOutcome(outcome);

        assertEquals(ACTOR, outcome2.getActor());
        assertEquals(OPERATION, outcome2.getOperation());
        assertEquals(TARGET, outcome2.getTarget());
        assertEquals(START, outcome2.getStart());
        assertEquals(END, outcome2.getEnd());
        assertEquals(SUB_REQ_ID, outcome2.getSubRequestId());
        assertEquals(RESULT, outcome2.getResult());
        assertEquals(MESSAGE, outcome2.getMessage());
        assertSame(RESPONSE, outcome2.getResponse());
    }

    @Test
    public void testToControlLoopOperation() {
        setAll();

        ControlLoopOperation outcome2 = outcome.toControlLoopOperation();

        assertEquals(ACTOR, outcome2.getActor());
        assertEquals(OPERATION, outcome2.getOperation());
        assertEquals(TARGET, outcome2.getTarget());
        assertEquals(START, outcome2.getStart());
        assertEquals(END, outcome2.getEnd());
        assertEquals(SUB_REQ_ID, outcome2.getSubRequestId());
        assertEquals(RESULT.toString(), outcome2.getOutcome());
        assertEquals(MESSAGE, outcome2.getMessage());
    }

    /**
     * Tests both isFor() methods, as one invokes the other.
     */
    @Test
    public void testIsFor() {
        setAll();

        // null case
        assertFalse(OperationOutcome.isFor(null, ACTOR, OPERATION));

        // actor mismatch
        assertFalse(OperationOutcome.isFor(outcome, TARGET, OPERATION));

        // operation mismatch
        assertFalse(OperationOutcome.isFor(outcome, ACTOR, TARGET));

        // null actor in outcome
        outcome.setActor(null);
        assertFalse(OperationOutcome.isFor(outcome, ACTOR, OPERATION));
        outcome.setActor(ACTOR);

        // null operation in outcome
        outcome.setOperation(null);
        assertFalse(OperationOutcome.isFor(outcome, ACTOR, OPERATION));
        outcome.setOperation(OPERATION);

        // null actor argument
        assertThatThrownBy(() -> outcome.isFor(null, OPERATION));

        // null operation argument
        assertThatThrownBy(() -> outcome.isFor(ACTOR, null));

        // true case
        assertTrue(OperationOutcome.isFor(outcome, ACTOR, OPERATION));
    }

    @Test
    public void testSetResult() {
        outcome.setResult(OperationResult.FAILURE_EXCEPTION);
        assertEquals(OperationResult.FAILURE_EXCEPTION, outcome.getResult());

        assertThatThrownBy(() -> outcome.setResult(null));
    }

    private void setAll() {
        outcome.setActor(ACTOR);
        outcome.setEnd(END);
        outcome.setMessage(MESSAGE);
        outcome.setOperation(OPERATION);
        outcome.setResult(RESULT);
        outcome.setStart(START);
        outcome.setSubRequestId(SUB_REQ_ID);
        outcome.setTarget(TARGET);
        outcome.setResponse(RESPONSE);
    }
}
