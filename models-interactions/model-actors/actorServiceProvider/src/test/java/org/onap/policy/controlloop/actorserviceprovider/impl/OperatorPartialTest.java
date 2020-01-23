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

package org.onap.policy.controlloop.actorserviceprovider.impl;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.PipelineController;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;

public class OperatorPartialTest {
    private static final int MAX_PARALLEL_REQUESTS = 10;
    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final String ACTOR = "my-actor";
    private static final String OPERATOR = "my-operator";
    private static final int ATTEMPT = 5;
    private static final String TARGET = "my-target";
    private static final int TIMEOUT = 1000;
    private static final UUID REQ_ID = UUID.randomUUID();

    private static final List<PolicyResult> FAILURE_RESULTS = Arrays.asList(PolicyResult.values()).stream()
                    .filter(result -> result != PolicyResult.SUCCESS).collect(Collectors.toList());

    private static final List<String> FAILURE_STRINGS =
                    FAILURE_RESULTS.stream().map(Object::toString).collect(Collectors.toList());

    @Mock
    private VirtualControlLoopEvent event;

    private Map<String, Object> config;
    private ControlLoopEventContext context;
    private PipelineController controller;
    private MyExec executor;
    private Policy policy;
    private ControlLoopOperationParams params;

    private MyOper oper;

    private int numStart;
    private int numEnd;

    private Instant tstart;

    private ControlLoopOperation opstart;
    private ControlLoopOperation opend;

    /**
     * Initializes the fields, including {@link #oper}.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(event.getRequestId()).thenReturn(REQ_ID);

        config = new TreeMap<>();
        context = new ControlLoopEventContext(event);
        controller = spy(new PipelineController());
        executor = new MyExec(controller);

        policy = new Policy();
        policy.setActor(ACTOR);
        policy.setRecipe(OPERATOR);
        policy.setTimeout(TIMEOUT);

        params = ControlLoopOperationParams.builder().attempt(ATTEMPT).completeCallback(this::completer)
                        .context(context).executor(executor).pipelineController(controller).policy(policy)
                        .startCallback(this::starter).target(TARGET).build();

        oper = new MyOper();
        oper.configure(new TreeMap<>());
        oper.start();

        tstart = null;

        opstart = null;
        opend = null;
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testOperatorPartial_testGetActorName_testGetName() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATOR, oper.getName());
        assertEquals(ACTOR + "." + OPERATOR, oper.getFullName());
    }

    @Test
    public void testDoStart() {
        oper = spy(new MyOper());

        oper.configure(config);
        oper.start();

        verify(oper).doStart();

        // others should not have been invoked
        verify(oper, never()).doStop();
        verify(oper, never()).doShutdown();
    }

    @Test
    public void testDoStop() {
        oper = spy(new MyOper());

        oper.configure(config);
        oper.start();
        oper.stop();

        verify(oper).doStop();

        // should not have been re-invoked
        verify(oper).doStart();

        // others should not have been invoked
        verify(oper, never()).doShutdown();
    }

    @Test
    public void testDoShutdown() {
        oper = spy(new MyOper());

        oper.configure(config);
        oper.start();
        oper.shutdown();

        verify(oper).doShutdown();

        // should not have been re-invoked
        verify(oper).doStart();

        // others should not have been invoked
        verify(oper, never()).doStop();
    }

    @Test
    public void testDoConfigure() {
        /*
         * Use an operator that doesn't override configure().
         */
        OperatorPartial oper2 = new OperatorPartial(ACTOR, OPERATOR) {};
        assertThatThrownBy(() -> oper2.configure(config)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void testStartOperation_testVerifyRunning() {
        verifyRunOnce("testStartOperation", 1, PolicyResult.SUCCESS);
    }

    private void verifyRunOnce(String testName, int expectedOperations, PolicyResult expectedResult) {
        oper.startOperation(params);
        assertTrue(testName, executor.runAll());

        assertNotNull(testName, opstart);
        assertNotNull(testName, opend);
        assertEquals(testName, expectedResult.toString(), opend.getOutcome());

        assertSame(tstart, opstart.getStart());
        assertSame(tstart, opend.getStart());

        assertEquals(testName, 1, numStart);
        assertEquals(testName, 1, numEnd);

        assertEquals(testName, expectedOperations, oper.getCount());
    }

    /**
     * Tests startOperation() when the operator is not running.
     */
    @Test
    public void testStartOperationNotRunning() {
        // use a new operator, one that hasn't been started yet
        oper = new MyOper();
        oper.configure(new TreeMap<>());

        assertThatIllegalStateException().isThrownBy(() -> oper.startOperation(params));
    }

    /**
     * Tests startOperation() when the operation has a preprocessor.
     */
    @Test
    public void testStartOperationWithPreprocessor_testStartPreprocessor() {
        AtomicInteger count = new AtomicInteger();

        // @formatter:off
        Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> preproc =
            oper -> CompletableFuture.supplyAsync(() -> {
                count.incrementAndGet();
                oper.setOutcome(PolicyResult.SUCCESS.toString());
                return oper;
            }, executor);
        // @formatter:on

        oper.setPreProcessor(preproc);

        verifyRunOnce("testStartOperationWithPreprocessor_testStartPreprocessor", 1, PolicyResult.SUCCESS);
    }

    /**
     * Tests startOperation() with multiple running requests.
     */
    @Test
    public void testStartOperationMultiple() {
        for (int count = 0; count < MAX_PARALLEL_REQUESTS; ++count) {
            oper.startOperation(params);
        }

        assertTrue(executor.runAll());

        assertNotNull(opstart);
        assertNotNull(opend);
        assertEquals(PolicyResult.SUCCESS.toString(), opend.getOutcome());

        assertEquals(MAX_PARALLEL_REQUESTS, numStart);
        assertEquals(MAX_PARALLEL_REQUESTS, oper.getCount());
        assertEquals(MAX_PARALLEL_REQUESTS, numEnd);
    }

    /**
     * Tests startPreprocessor() when the preprocessor returns a failure.
     */
    @Test
    public void testStartPreprocessorFailure() {
        // arrange for the preprocessor to return a failure
        oper.setPreProcessor(oper -> {
            oper.setOutcome(PolicyResult.FAILURE_GUARD.toString());
            return CompletableFuture.completedFuture(oper);
        });

        verifyRunOnce("testStartPreprocessorFailure", 0, PolicyResult.FAILURE_GUARD);
    }

    /**
     * Tests startPreprocessor() when the preprocessor throws an exception.
     */
    @Test
    public void testStartPreprocessorException() {
        // arrange for the preprocessor to throw an exception
        oper.setPreProcessor(oper -> {
            throw new IllegalStateException(EXPECTED_EXCEPTION);
        });

        verifyRunOnce("testStartPreprocessorException", 0, PolicyResult.FAILURE_GUARD);
    }

    /**
     * Tests startPreprocessor() when the pipeline is not running.
     */
    @Test
    public void testStartPreprocessorNotRunning() {
        // arrange for the preprocessor to return success, which will be ignored
        oper.setPreProcessor(oper -> {
            oper.setOutcome(PolicyResult.SUCCESS.toString());
            return CompletableFuture.completedFuture(oper);
        });

        controller.stop();

        oper.startOperation(params);
        assertTrue(executor.runAll());

        assertNull(opstart);
        assertNull(opend);

        assertEquals(0, numStart);
        assertEquals(0, oper.getCount());
        assertEquals(0, numEnd);
    }

    /**
     * Tests startPreprocessor() when the preprocessor <b>builder</b> throws an exception.
     */
    @Test
    public void testStartPreprocessorBuilderException() {
        oper = new MyOper() {
            @Override
            protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> doPreprocessorAsFuture(
                            ControlLoopOperationParams params) {
                throw new IllegalStateException(EXPECTED_EXCEPTION);
            }
        };

        oper.configure(new TreeMap<>());
        oper.start();

        assertThatIllegalStateException().isThrownBy(() -> oper.startOperation(params));

        // should be nothing in the queue
        assertEquals(0, executor.getQueueLength());
    }

    @Test
    public void testDoPreprocessorAsFuture() {
        assertNull(oper.doPreprocessorAsFuture(params));
    }

    @Test
    public void testStartOperationOnly_testDoOperationAsFuture() {
        oper.startOperation(params);
        assertTrue(executor.runAll());

        assertEquals(1, oper.getCount());
    }

    /**
     * Tests startOperationOnce() when
     * {@link OperatorPartial#doOperationAsFuture(ControlLoopOperationParams)} throws an
     * exception.
     */
    @Test
    public void testStartOperationOnceBuilderException() {
        oper = new MyOper() {
            @Override
            protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> doOperationAsFuture(
                            ControlLoopOperationParams params) {
                throw new IllegalStateException(EXPECTED_EXCEPTION);
            }
        };

        oper.configure(new TreeMap<>());
        oper.start();

        assertThatIllegalStateException().isThrownBy(() -> oper.startOperation(params));

        // should be nothing in the queue
        assertEquals(0, executor.getQueueLength());
    }

    @Test
    public void testIsSuccess() {
        ControlLoopOperation outcome = new ControlLoopOperation();

        outcome.setOutcome(PolicyResult.SUCCESS.toString());
        assertTrue(oper.isSuccess(outcome));

        for (String failure : FAILURE_STRINGS) {
            outcome.setOutcome(failure);
            assertFalse("testIsSuccess-" + failure, oper.isSuccess(outcome));
        }
    }

    @Test
    public void testIsActorFailed() {
        ControlLoopOperation outcome = params.makeOutcome();

        // incorrect outcome
        outcome.setOutcome(PolicyResult.SUCCESS.toString());
        assertFalse(oper.isActorFailed(outcome));

        outcome.setOutcome(PolicyResult.FAILURE_RETRIES.toString());
        assertFalse(oper.isActorFailed(outcome));

        // correct outcome
        outcome.setOutcome(PolicyResult.FAILURE.toString());

        // incorrect actor
        outcome.setActor(TARGET);
        assertFalse(oper.isActorFailed(outcome));
        outcome.setActor(null);
        assertFalse(oper.isActorFailed(outcome));
        outcome.setActor(ACTOR);

        // incorrect operation
        outcome.setOperation(TARGET);
        assertFalse(oper.isActorFailed(outcome));
        outcome.setOperation(null);
        assertFalse(oper.isActorFailed(outcome));
        outcome.setOperation(OPERATOR);

        // correct values
        assertTrue(oper.isActorFailed(outcome));
    }

    @Test
    public void testDoOperation() {
        /*
         * Use an operator that doesn't override doOperation().
         */
        OperatorPartial oper2 = new OperatorPartial(ACTOR, OPERATOR) {
            @Override
            protected void doConfigure(Map<String, Object> parameters) {
                // do nothing
            }
        };

        oper2.configure(new TreeMap<>());
        oper2.start();

        oper2.startOperation(params);
        assertTrue(executor.runAll());

        assertNotNull(opend);
        assertEquals(PolicyResult.FAILURE_EXCEPTION.toString(), opend.getOutcome());
    }

    @Test
    public void testTimeout() throws Exception {

        // use a real executor
        params = params.toBuilder().executor(ForkJoinPool.commonPool()).build();

        // trigger timeout very quickly
        oper = new MyOper() {
            @Override
            protected long getTimeOutMillis(Policy policy) {
                return 1;
            }

            @Override
            protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> doOperationAsFuture(
                            ControlLoopOperationParams params) {

                return outcome -> {
                    ControlLoopOperation outcome2 = params.makeOutcome();
                    outcome2.setOutcome(PolicyResult.SUCCESS.toString());

                    /*
                     * Create an incomplete future that will timeout after the operation's
                     * timeout. If it fires before the other timer, then it will return a
                     * SUCCESS outcome.
                     */
                    CompletableFuture<ControlLoopOperation> future = new CompletableFuture<>();
                    future = future.orTimeout(1, TimeUnit.SECONDS).handleAsync((unused1, unused2) -> outcome,
                                    params.getExecutor());

                    return future;
                };
            }
        };

        oper.configure(new TreeMap<>());
        oper.start();

        assertEquals(PolicyResult.FAILURE_TIMEOUT.toString(), oper.startOperation(params).get().getOutcome());
    }

    /**
     * Verifies that the timer doesn't encompass the preprocessor and doesn't stop the
     * operation once the preprocessor completes.
     */
    @Test
    public void testTimeoutInPreprocessor() throws Exception {

        // use a real executor
        params = params.toBuilder().executor(ForkJoinPool.commonPool()).build();

        // trigger timeout very quickly
        oper = new MyOper() {
            @Override
            protected long getTimeOutMillis(Policy policy) {
                return 10;
            }

            @Override
            protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> doPreprocessorAsFuture(
                            ControlLoopOperationParams params) {

                return outcome -> {
                    outcome.setOutcome(PolicyResult.SUCCESS.toString());

                    /*
                     * Create an incomplete future that will timeout after the operation's
                     * timeout. If it fires before the other timer, then it will return a
                     * SUCCESS outcome.
                     */
                    CompletableFuture<ControlLoopOperation> future = new CompletableFuture<>();
                    future = future.orTimeout(200, TimeUnit.MILLISECONDS).handleAsync((unused1, unused2) -> outcome,
                                    params.getExecutor());

                    return future;
                };
            }
        };

        oper.configure(new TreeMap<>());
        oper.start();

        ControlLoopOperation result = oper.startOperation(params).get();
        assertEquals(PolicyResult.SUCCESS.toString(), result.getOutcome());

        assertNotNull(opstart);
        assertNotNull(opend);
        assertEquals(PolicyResult.SUCCESS.toString(), opend.getOutcome());

        assertEquals(1, numStart);
        assertEquals(1, oper.getCount());
        assertEquals(1, numEnd);
    }

    /**
     * Tests retry functions, when the count is set to zero and retries are exhausted.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_ZeroRetries() {
        policy.setRetry(0);
        oper.setMaxFailures(10);

        verifyRunOnce("testSetRetryFlag_testRetryOnFailure_ZeroRetries", 1, PolicyResult.FAILURE);
    }

    /**
     * Tests retry functions, when the count is null and retries are exhausted.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_NullRetries() {
        policy.setRetry(null);
        oper.setMaxFailures(10);

        verifyRunOnce("testSetRetryFlag_testRetryOnFailure_NullRetries", 1, PolicyResult.FAILURE);
    }

    /**
     * Tests retry functions, when retries are exhausted.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_RetriesExhausted() {
        final int maxRetries = 3;
        policy.setRetry(maxRetries);
        oper.setMaxFailures(10);

        oper.startOperation(params);
        assertTrue(executor.runAll());

        assertNotNull(opstart);
        assertNotNull(opend);
        assertEquals(PolicyResult.FAILURE_RETRIES.toString(), opend.getOutcome());

        assertEquals(maxRetries + 1, numStart);
        assertEquals(maxRetries + 1, oper.getCount());
        assertEquals(maxRetries + 1, numEnd);
    }

    /**
     * Tests retry functions, when a success follows some retries.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_SuccessAfterRetries() {
        policy.setRetry(10);

        final int maxFailures = 3;
        oper.setMaxFailures(maxFailures);

        oper.startOperation(params);
        assertTrue(executor.runAll());

        assertNotNull(opstart);
        assertNotNull(opend);
        assertEquals(PolicyResult.SUCCESS.toString(), opend.getOutcome());

        assertEquals(maxFailures + 1, numStart);
        assertEquals(maxFailures + 1, oper.getCount());
        assertEquals(maxFailures + 1, numEnd);
    }

    @Test
    public void testGetActorOutcome() {
        ControlLoopOperation outcome = params.makeOutcome();
        outcome.setOutcome(TARGET);

        // wrong actor - should be null
        outcome.setActor(null);
        assertNull(oper.getActorOutcome(outcome));
        outcome.setActor(TARGET);
        assertNull(oper.getActorOutcome(outcome));
        outcome.setActor(ACTOR);

        // wrong operation - should be null
        outcome.setOperation(null);
        assertNull(oper.getActorOutcome(outcome));
        outcome.setOperation(TARGET);
        assertNull(oper.getActorOutcome(outcome));
        outcome.setOperation(OPERATOR);

        assertEquals(TARGET, oper.getActorOutcome(outcome));
    }

    @Test
    public void testStopPipeline() {
        oper.startOperation(params);
        assertTrue(executor.runAll());

        assertFalse(controller.isRunning());
    }

    /**
     * Tests onSuccess() and handleFailure() when the outcome is a success.
     */
    @Test
    public void testOnSuccessTrue_testHandleFailureTrue() {
        // arrange to return a success from the preprocessor
        oper.setPreProcessor(oper -> {
            oper.setOutcome(PolicyResult.SUCCESS.toString());
            return CompletableFuture.completedFuture(oper);
        });

        verifyRunOnce("testOnSuccessTrue_testHandleFailureTrue", 1, PolicyResult.SUCCESS);
    }

    /**
     * Tests onSuccess() and handleFailure() when the outcome is <i>not</i> a success.
     */
    @Test
    public void testOnSuccessFalse_testHandleFailureFalse() throws Exception {
        // arrange to return a failure from the preprocessor
        oper.setPreProcessor(oper -> {
            oper.setOutcome(PolicyResult.FAILURE.toString());
            return CompletableFuture.completedFuture(oper);
        });

        verifyRunOnce("testOnSuccessFalse_testHandleFailureFalse", 0, PolicyResult.FAILURE_GUARD);
    }

    @Test
    public void testFromException() {
        // arrange to generate an exception when operation runs
        oper.setGenException(true);

        verifyRunOnce("testFromException", 1, PolicyResult.FAILURE_EXCEPTION);
    }

    /**
     * Tests fromException() when there is no exception.
     */
    @Test
    public void testFromExceptionNoExcept() {
        oper.startOperation(params);
        assertTrue(executor.runAll());

        verifyRunOnce("testFromExceptionNoExcept", 1, PolicyResult.SUCCESS);
    }

    /**
     * Tests verifyRunning() when the pipeline is not running.
     */
    @Test
    public void testVerifyRunningWhenNot() {
        controller.stop();

        oper.startOperation(params);
        assertTrue(executor.runAll());

        assertEquals(0, numStart);
        assertEquals(0, oper.getCount());
        assertEquals(0, numEnd);
    }

    /**
     * Tests callbackStarted() when the pipeline has already been stopped.
     */
    @Test
    public void testCallbackStartedNotRunning() {
        /*
         * arrange to stop the controller when the start-callback is invoked, but capture
         * the outcome
         */
        params = params.toBuilder().startCallback(oper -> {
            starter(oper);
            controller.stop();
        }).build();

        oper.startOperation(params);
        assertTrue(executor.runAll());

        // should have only run once
        assertEquals(1, numStart);
    }

    /**
     * Tests callbackCompleted() when the pipeline has already been stopped.
     */
    @Test
    public void testCallbackCompletedNotRunning() {
        // arrange to stop the controller when the start-callback is invoked
        params = params.toBuilder().startCallback(oper -> {
            controller.stop();
        }).build();

        oper.startOperation(params);
        assertTrue(executor.runAll());

        // should not have been set
        assertNull(opend);
        assertEquals(0, numEnd);
    }

    @Test
    public void testSetOutcomeControlLoopOperationThrowable() {
        final TimeoutException timex = new TimeoutException(EXPECTED_EXCEPTION);

        ControlLoopOperation outcome;

        outcome = new ControlLoopOperation();
        oper.setOutcome(outcome, timex);
        assertEquals(ControlLoopOperation.FAILED_MSG, outcome.getMessage());
        assertEquals(PolicyResult.FAILURE_TIMEOUT.toString(), outcome.getOutcome());

        outcome = new ControlLoopOperation();
        oper.setOutcome(outcome, new IllegalStateException());
        assertEquals(ControlLoopOperation.FAILED_MSG, outcome.getMessage());
        assertEquals(PolicyResult.FAILURE_EXCEPTION.toString(), outcome.getOutcome());
    }

    @Test
    public void testSetOutcomeControlLoopOperationPolicyResult() {
        ControlLoopOperation outcome;

        outcome = new ControlLoopOperation();
        oper.setOutcome(outcome, PolicyResult.SUCCESS);
        assertEquals(ControlLoopOperation.SUCCESS_MSG, outcome.getMessage());
        assertEquals(PolicyResult.SUCCESS.toString(), outcome.getOutcome());

        for (PolicyResult result : FAILURE_RESULTS) {
            outcome = new ControlLoopOperation();
            oper.setOutcome(outcome, result);
            assertEquals(result.toString(), ControlLoopOperation.FAILED_MSG, outcome.getMessage());
            assertEquals(result.toString(), result.toString(), outcome.getOutcome());
        }
    }

    @Test
    public void testIsTimeout() {
        final TimeoutException timex = new TimeoutException(EXPECTED_EXCEPTION);

        assertFalse(oper.isTimeout(new IllegalStateException()));
        assertFalse(oper.isTimeout(new IllegalStateException(timex)));
        assertFalse(oper.isTimeout(new CompletionException(new IllegalStateException(timex))));
        assertFalse(oper.isTimeout(new CompletionException(null)));

        assertTrue(oper.isTimeout(timex));
        assertTrue(oper.isTimeout(new CompletionException(timex)));
        assertTrue(oper.isTimeout(new CompletionException(new CompletionException(timex))));
    }

    @Test
    public void testGetTimeOutMillis() {
        assertEquals(TIMEOUT * 1000, oper.getTimeOutMillis(policy));

        policy.setTimeout(null);
        assertEquals(0, oper.getTimeOutMillis(policy));
    }

    private void starter(ControlLoopOperation oper) {
        ++numStart;
        tstart = oper.getStart();
        opstart = oper;
    }

    private void completer(ControlLoopOperation oper) {
        ++numEnd;
        opend = oper;
    }

    private static class MyOper extends OperatorPartial {
        @Getter
        private int count = 0;

        @Setter
        private boolean genException;

        @Setter
        private int maxFailures = 0;

        @Setter
        private Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> preProcessor;

        public MyOper() {
            super(ACTOR, OPERATOR);
        }

        @Override
        protected void doConfigure(Map<String, Object> parameters) {
            // don't invoke the superclass method, because it throw's an exception
        }

        @Override
        protected ControlLoopOperation doOperation(ControlLoopOperationParams params, ControlLoopOperation operation) {
            ++count;
            if (genException) {
                throw new IllegalStateException(EXPECTED_EXCEPTION);
            }

            if (count > maxFailures) {
                operation.setOutcome(PolicyResult.SUCCESS.toString());
            } else {
                operation.setOutcome(PolicyResult.FAILURE.toString());
            }

            return operation;
        }

        @Override
        protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> doPreprocessorAsFuture(
                        ControlLoopOperationParams params) {

            return (preProcessor != null ? preProcessor : super.doPreprocessorAsFuture(params));
        }
    }

    /**
     * Executor that will run tasks until the queue is empty or a maximum number of tasks
     * have been executed.
     */
    private static class MyExec implements Executor {
        private static final int MAX_TASKS = MAX_PARALLEL_REQUESTS * 100;

        private boolean done = false;
        private Queue<Runnable> commands = new LinkedList<>();

        public MyExec(PipelineController controller) {
            controller.add(() -> done = true);
        }

        public int getQueueLength() {
            return commands.size();
        }

        @Override
        public void execute(Runnable command) {
            commands.add(command);
        }

        public boolean runAll() {
            for (int count = 0; count < MAX_TASKS && !commands.isEmpty(); ++count) {
                commands.remove().run();
            }

            return done && commands.isEmpty();
        }
    }
}
