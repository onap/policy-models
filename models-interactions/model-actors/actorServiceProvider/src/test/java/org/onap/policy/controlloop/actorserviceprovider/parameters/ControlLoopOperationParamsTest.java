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

package org.onap.policy.controlloop.actorserviceprovider.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.OperationManager;
import org.onap.policy.controlloop.actorserviceprovider.PipelineController;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams.ControlLoopOperationParamsBuilder;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;

public class ControlLoopOperationParamsTest {
    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";
    private static final String TARGET = "my-target";
    private static final UUID REQ_ID = UUID.randomUUID();
    private static final int ATTEMPT = 10;
    private static final String ATTEMPT_STRING = String.valueOf(ATTEMPT);

    @Mock
    private Actor actor;

    @Mock
    private ActorService actorService;

    @Mock
    private Consumer<ControlLoopOperation> completer;

    @Mock
    private ControlLoopEventContext context;

    @Mock
    private PipelineController controller;

    @Mock
    private VirtualControlLoopEvent event;

    @Mock
    private Executor executor;

    @Mock
    private CompletableFuture<ControlLoopOperation> operation;

    @Mock
    private OperationManager operator;

    @Mock
    private Policy policy;

    @Mock
    private Consumer<ControlLoopOperation> starter;

    private ControlLoopOperationParams params;
    private ControlLoopOperation outcome;


    /**
     * Initializes mocks and sets {@link #params} to a fully-loaded set of parameters.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(actorService.getActor(ACTOR)).thenReturn(actor);
        when(actor.getOperationManager(OPERATION)).thenReturn(operator);
        when(operator.startOperation(any())).thenReturn(operation);

        when(event.getRequestId()).thenReturn(REQ_ID);

        when(context.getEvent()).thenReturn(event);

        when(policy.getActor()).thenReturn(ACTOR);
        when(policy.getRecipe()).thenReturn(OPERATION);

        doAnswer(args -> {
            args.getArgument(0, Runnable.class).run();
            return null;
        }).when(controller).doIfRunning(any());

        params = ControlLoopOperationParams.builder().actorService(actorService).attempt(ATTEMPT)
                        .completeCallback(completer).context(context).executor(executor).pipelineController(controller)
                        .policy(policy).startCallback(starter).target(TARGET).build();

        outcome = params.makeOutcome();
    }

    @Test
    public void testStart() throws InterruptedException, ExecutionException {
        assertSame(operation, params.start());
        verify(controller, never()).stop();

        CompletableFuture<ControlLoopOperation> future = params.toBuilder().context(null).build().start();
        assertTrue(future.isDone());
        assertEquals(PolicyResult.FAILURE_EXCEPTION.toString(), future.get().getOutcome());
        verify(controller).stop();

        assertSame(operation, params.start());
    }

    @Test
    public void testHandleException() throws InterruptedException, ExecutionException {
        CompletableFuture<ControlLoopOperation> future = params.toBuilder().context(null).build().start();
        assertTrue(future.isDone());
        assertEquals(PolicyResult.FAILURE_EXCEPTION.toString(), future.get().getOutcome());

        // repeat, but without a controller
        future = params.toBuilder().context(null).pipelineController(null).build().start();
        assertTrue(future.isDone());
        assertEquals(PolicyResult.FAILURE_EXCEPTION.toString(), future.get().getOutcome());
    }

    @Test
    public void testGetActor() {
        assertEquals(ACTOR, params.getActor());

        // try with null policy
        assertEquals(ControlLoopOperationParams.UNKNOWN, params.toBuilder().policy(null).build().getActor());

        // try with null name in the policy
        when(policy.getActor()).thenReturn(null);
        assertEquals(ControlLoopOperationParams.UNKNOWN, params.getActor());
    }

    @Test
    public void testGetOperation() {
        assertEquals(OPERATION, params.getOperation());

        // try with null policy
        assertEquals(ControlLoopOperationParams.UNKNOWN, params.toBuilder().policy(null).build().getOperation());

        // try with null name in the policy
        when(policy.getRecipe()).thenReturn(null);
        assertEquals(ControlLoopOperationParams.UNKNOWN, params.getOperation());
    }

    @Test
    public void testGetRequestId() {
        assertSame(REQ_ID, params.getRequestId());

        // try with null context
        assertNull(params.toBuilder().context(null).build().getRequestId());

        // try with null event
        when(context.getEvent()).thenReturn(null);
        assertNull(params.getRequestId());
    }

    @Test
    public void testMakeOutcome() {
        assertEquals(ACTOR, outcome.getActor());
        assertEquals(OPERATION, outcome.getOperation());
        assertEquals(TARGET, outcome.getTarget());
        assertNotNull(outcome.getStart());
        assertNull(outcome.getEnd());
        assertEquals(ATTEMPT_STRING, outcome.getSubRequestId());
        assertNull(outcome.getOutcome());
        assertNull(outcome.getMessage());

        // try again with a null policy
        outcome = params.toBuilder().policy(null).build().makeOutcome();
        assertEquals(ControlLoopOperationParams.UNKNOWN, outcome.getActor());
        assertEquals(ControlLoopOperationParams.UNKNOWN, outcome.getOperation());
        assertEquals(TARGET, outcome.getTarget());
        assertNotNull(outcome.getStart());
        assertNull(outcome.getEnd());
        assertEquals(ATTEMPT_STRING, outcome.getSubRequestId());
        assertNull(outcome.getOutcome());
        assertNull(outcome.getMessage());
    }

    @Test
    public void testStopPipeline() {
        params.stopPipeline();
        verify(controller).stop();

        // try again with a null controller - no additional calls
        params.toBuilder().pipelineController(null).build().stopPipeline();
        verify(controller).stop();
    }

    @Test
    public void testCallbackStarted() {
        params.callbackStarted(outcome);
        verify(starter).accept(outcome);

        // modify starter to throw an exception
        AtomicInteger count = new AtomicInteger();
        doAnswer(args -> {
            count.incrementAndGet();
            throw new IllegalStateException(EXPECTED_EXCEPTION);
        }).when(starter).accept(outcome);

        params.callbackStarted(outcome);
        verify(starter, times(2)).accept(outcome);
        assertEquals(1, count.get());

        // repeat with no start-callback - no additional calls expected
        params.toBuilder().startCallback(null).build().callbackStarted(outcome);
        verify(starter, times(2)).accept(outcome);
        assertEquals(1, count.get());

        // should not call complete-callback
        verify(completer, never()).accept(any());
    }

    @Test
    public void testCallbackCompleted() {
        params.callbackCompleted(outcome);
        verify(completer).accept(outcome);

        // modify completer to throw an exception
        AtomicInteger count = new AtomicInteger();
        doAnswer(args -> {
            count.incrementAndGet();
            throw new IllegalStateException(EXPECTED_EXCEPTION);
        }).when(completer).accept(outcome);

        params.callbackCompleted(outcome);
        verify(completer, times(2)).accept(outcome);
        assertEquals(1, count.get());

        // repeat with no complete-callback - no additional calls expected
        params.toBuilder().completeCallback(null).build().callbackCompleted(outcome);
        verify(completer, times(2)).accept(outcome);
        assertEquals(1, count.get());

        // should not call start-callback
        verify(starter, never()).accept(any());
    }

    @Test
    public void testValidateFields() {
        testValidate("actorService", "null", bldr -> bldr.actorService(null));
        testValidate("attempt", "minimum", bldr -> bldr.attempt(0));
        testValidate("context", "null", bldr -> bldr.context(null));
        testValidate("executor", "null", bldr -> bldr.executor(null));
        testValidate("pipelineController", "null", bldr -> bldr.pipelineController(null));
        testValidate("policy", "null", bldr -> bldr.policy(null));
        testValidate("target", "null", bldr -> bldr.target(null));

        // check edge cases
        assertTrue(params.toBuilder().attempt(1).build().validate().isValid());

        // these can be null
        assertTrue(params.toBuilder().startCallback(null).completeCallback(null).build().validate().isValid());
    }

    private void testValidate(String fieldName, String expected,
                    Function<ControlLoopOperationParamsBuilder, ControlLoopOperationParamsBuilder> makeInvalid) {

        // original params should be valid
        BeanValidationResult result = params.validate();
        assertTrue(fieldName, result.isValid());

        // make invalid params
        result = makeInvalid.apply(params.toBuilder()).build().validate();
        assertFalse(fieldName, result.isValid());

        String msg = result.getResult();
        assertTrue(fieldName, msg.contains(fieldName));
        assertTrue(fieldName, msg.contains(expected));
    }

    @Test
    public void testBuilder_testToBuilder() {
        assertEquals(params, params.toBuilder().build());
    }

    @Test
    public void testActorService() {
        assertSame(actorService, params.getActorService());
    }

    @Test
    public void testGetAttempt() {
        assertEquals(ATTEMPT, params.getAttempt());
    }

    @Test
    public void testGetContext() {
        assertSame(context, params.getContext());
    }

    @Test
    public void testGetExecutor() {
        assertSame(executor, params.getExecutor());

        // should use default when unspecified
        assertSame(ForkJoinPool.commonPool(), ControlLoopOperationParams.builder().build().getExecutor());
    }

    @Test
    public void testGetPipelineController() {
        assertSame(controller, params.getPipelineController());

        // should create a new one when unspecified
        PipelineController controller2 = ControlLoopOperationParams.builder().build().getPipelineController();
        assertNotNull(controller2);
        assertTrue(controller2 != controller);
    }

    @Test
    public void testGetPolicy() {
        assertSame(policy, params.getPolicy());
    }

    @Test
    public void testGetStartCallback() {
        assertSame(starter, params.getStartCallback());
    }

    @Test
    public void testGetCompleteCallback() {
        assertSame(completer, params.getCompleteCallback());
    }

    @Test
    public void testGetTarget() {
        assertEquals(TARGET, params.getTarget());
    }
}
