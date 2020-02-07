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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.policy.PolicyResult;

public class AsyncResponseHandlerTest {

    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";
    private static final UUID REQ_ID = UUID.randomUUID();
    private static final String TEXT = "some text";

    private VirtualControlLoopEvent event;
    private ControlLoopEventContext context;
    private ControlLoopOperationParams params;
    private OperationOutcome outcome;
    private MyHandler handler;

    /**
     * Initializes all fields, including {@link #handler}.
     */
    @Before
    public void setUp() {
        event = new VirtualControlLoopEvent();
        event.setRequestId(REQ_ID);

        context = new ControlLoopEventContext(event);
        params = ControlLoopOperationParams.builder().actor(ACTOR).operation(OPERATION).context(context).build();
        outcome = params.makeOutcome();

        handler = new MyHandler(params, outcome);
    }

    @Test
    public void testAsyncResponseHandler_testGetParams_testGetOutcome() {
        assertSame(params, handler.getParams());
        assertSame(outcome, handler.getOutcome());
    }

    @Test
    public void testHandle() {
        CompletableFuture<String> future = new CompletableFuture<>();
        handler.handle(future).complete(outcome);

        assertTrue(future.isCancelled());
    }

    @Test
    public void testCompleted() throws Exception {
        CompletableFuture<OperationOutcome> result = handler.handle(new CompletableFuture<>());
        handler.completed(TEXT);
        assertTrue(result.isDone());
        assertSame(outcome, result.get());
        assertEquals(PolicyResult.FAILURE_RETRIES, outcome.getResult());
        assertEquals(TEXT, outcome.getMessage());
    }

    /**
     * Tests completed() when doCompleted() throws an exception.
     */
    @Test
    public void testCompletedException() throws Exception {
        IllegalStateException except = new IllegalStateException();

        outcome = params.makeOutcome();
        handler = new MyHandler(params, outcome) {
            @Override
            protected OperationOutcome doComplete(String rawResponse) {
                throw except;
            }
        };

        CompletableFuture<OperationOutcome> result = handler.handle(new CompletableFuture<>());
        handler.completed(TEXT);
        assertTrue(result.isCompletedExceptionally());

        AtomicReference<Throwable> thrown = new AtomicReference<>();
        result.whenComplete((unused, thrown2) -> thrown.set(thrown2));

        assertSame(except, thrown.get());
    }

    @Test
    public void testFailed() throws Exception {
        IllegalStateException except = new IllegalStateException();

        CompletableFuture<OperationOutcome> result = handler.handle(new CompletableFuture<>());
        handler.failed(except);

        assertTrue(result.isDone());
        assertSame(outcome, result.get());
        assertEquals(PolicyResult.FAILURE_GUARD, outcome.getResult());
    }

    /**
     * Tests failed() when doFailed() throws an exception.
     */
    @Test
    public void testFailedException() throws Exception {
        IllegalStateException except = new IllegalStateException();

        outcome = params.makeOutcome();
        handler = new MyHandler(params, outcome) {
            @Override
            protected OperationOutcome doFailed(Throwable thrown) {
                throw except;
            }
        };

        CompletableFuture<OperationOutcome> result = handler.handle(new CompletableFuture<>());
        handler.failed(except);
        assertTrue(result.isCompletedExceptionally());

        AtomicReference<Throwable> thrown = new AtomicReference<>();
        result.whenComplete((unused, thrown2) -> thrown.set(thrown2));

        assertSame(except, thrown.get());
    }

    private class MyHandler extends AsyncResponseHandler<String> {

        public MyHandler(ControlLoopOperationParams params, OperationOutcome outcome) {
            super(params, outcome);
        }

        @Override
        protected OperationOutcome doComplete(String rawResponse) {
            OperationOutcome outcome = getOutcome();
            outcome.setResult(PolicyResult.FAILURE_RETRIES);
            outcome.setMessage(rawResponse);
            return outcome;
        }

        @Override
        protected OperationOutcome doFailed(Throwable thrown) {
            OperationOutcome outcome = getOutcome();
            outcome.setResult(PolicyResult.FAILURE_GUARD);
            return outcome;
        }
    }
}
