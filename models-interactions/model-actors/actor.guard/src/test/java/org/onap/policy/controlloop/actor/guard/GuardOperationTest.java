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

package org.onap.policy.controlloop.actor.guard;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.controlloop.actor.test.BasicHttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;

public class GuardOperationTest extends BasicHttpOperation<DecisionRequest> {

    private GuardOperation oper;

    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        oper = new GuardOperation(params, operator);
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
    }

    @Test
    public void testStartOperationAsync() throws Exception {
        CompletableFuture<OperationOutcome> future2 = oper.start();
        executor.runAll(100);
        assertFalse(future2.isDone());

        DecisionResponse resp = new DecisionResponse();
        resp.setStatus(GuardOperation.PERMIT);
        when(rawResponse.readEntity(DecisionResponse.class)).thenReturn(resp);

        verify(client).post(callbackCaptor.capture(), any(), requestCaptor.capture(), any());
        callbackCaptor.getValue().completed(rawResponse);

        executor.runAll(100);
        assertTrue(future2.isDone());

        assertEquals(PolicyResult.SUCCESS, future2.get().getResult());

        DecisionRequest request = requestCaptor.getValue().getEntity();
    }

    @Test
    public void testPostProcessResponse() {
        DecisionResponse response = new DecisionResponse();

        // null status
        response.setStatus(null);
        verifyOutcome(response, PolicyResult.FAILURE, "response contains no status");

        // permit, mixed case
        response.setStatus("peRmit");
        verifyOutcome(response, PolicyResult.SUCCESS, "peRmit");

        // indeterminate, mixed case
        response.setStatus("inDETerminate");
        verifyOutcome(response, PolicyResult.SUCCESS, "inDETerminate");

        // deny, mixed case
        response.setStatus("deNY");
        verifyOutcome(response, PolicyResult.FAILURE, "deNY");

        // unknown status
        response.setStatus("unknown");
        verifyOutcome(response, PolicyResult.FAILURE, "unknown");
    }

    private void verifyOutcome(DecisionResponse response, PolicyResult expectedResult, String expectedMessage) {
        oper.postProcessResponse(outcome, BASE_URI, rawResponse, response);
        assertEquals(expectedResult, outcome.getResult());
        assertEquals(expectedMessage, outcome.getMessage());
    }

    @Override
    protected Map<String, String> makePayload() {
        DecisionRequest req = new DecisionRequest();
    }
}
