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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;

public class OperatorPartialTest {
    private static final int MAX_PARALLEL_REQUESTS = 10;
    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final String ACTOR = "my-actor";
    private static final String OPERATOR = "my-operator";
    private static final String TARGET = "my-target";
    private static final int TIMEOUT = 1000;
    private static final UUID REQ_ID = UUID.randomUUID();

    private static final List<PolicyResult> FAILURE_RESULTS = Arrays.asList(PolicyResult.values()).stream()
                    .filter(result -> result != PolicyResult.SUCCESS).collect(Collectors.toList());

    private static final List<String> FAILURE_STRINGS =
                    FAILURE_RESULTS.stream().map(Object::toString).collect(Collectors.toList());

    private VirtualControlLoopEvent event;
    private Map<String, Object> config;
    private ControlLoopEventContext context;
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
        event = new VirtualControlLoopEvent();
        event.setRequestId(REQ_ID);

        config = new TreeMap<>();
        context = new ControlLoopEventContext(event);
        executor = new MyExec();

        policy = new Policy();
        policy.setActor(ACTOR);
        policy.setRecipe(OPERATOR);
        policy.setTimeout(TIMEOUT);

        params = ControlLoopOperationParams.builder().completeCallback(this::completer).context(context)
                        .executor(executor).policy(policy).startCallback(this::starter).target(TARGET).build();

        oper = new MyOper();
        oper.configure(new TreeMap<>());
        oper.start();

        tstart = null;

        opstart = null;
        opend = null;
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
    public void testStartOperation_testVerifyRunning() {
        verifyRun("testStartOperation", 1, 1, PolicyResult.SUCCESS);
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

        verifyRun("testStartOperationWithPreprocessor_testStartPreprocessor", 1, 1, PolicyResult.SUCCESS);

        assertEquals(1, count.get());
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

        verifyRun("testStartPreprocessorFailure", 1, 0, PolicyResult.FAILURE_GUARD);
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

        verifyRun("testStartPreprocessorException", 1, 0, PolicyResult.FAILURE_GUARD);
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

        oper.startOperation(params).cancel(false);
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
                            ControlLoopOperationParams params, int attempt) {
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
        assertFalse(oper.isActorFailed(null));

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
        OperatorPartial oper2 = new OperatorPartial(ACTOR, OPERATOR) {};

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
                            ControlLoopOperationParams params, int attempt) {

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

        verifyRun("testSetRetryFlag_testRetryOnFailure_ZeroRetries", 1, 1, PolicyResult.FAILURE);
    }

    /**
     * Tests retry functions, when the count is null and retries are exhausted.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_NullRetries() {
        policy.setRetry(null);
        oper.setMaxFailures(10);

        verifyRun("testSetRetryFlag_testRetryOnFailure_NullRetries", 1, 1, PolicyResult.FAILURE);
    }

    /**
     * Tests retry functions, when retries are exhausted.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_RetriesExhausted() {
        final int maxRetries = 3;
        policy.setRetry(maxRetries);
        oper.setMaxFailures(10);

        verifyRun("testVerifyRunningWhenNot", maxRetries + 1, maxRetries + 1, PolicyResult.FAILURE_RETRIES);
    }

    /**
     * Tests retry functions, when a success follows some retries.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_SuccessAfterRetries() {
        policy.setRetry(10);

        final int maxFailures = 3;
        oper.setMaxFailures(maxFailures);

        verifyRun("testSetRetryFlag_testRetryOnFailure_SuccessAfterRetries", maxFailures + 1, maxFailures + 1,
                        PolicyResult.SUCCESS);
    }

    /**
     * Tests retry functions, when the outcome is {@code null}.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_NullOutcome() {

        // arrange to return null from doOperation()
        oper = new MyOper() {
            @Override
            protected ControlLoopOperation doOperation(ControlLoopOperationParams params, int attempt,
                            ControlLoopOperation operation) {

                // update counters
                super.doOperation(params, attempt, operation);
                return null;
            }
        };

        oper.configure(new TreeMap<>());
        oper.start();

        verifyRun("testSetRetryFlag_testRetryOnFailure_NullOutcome", 1, 1, PolicyResult.FAILURE, null, noop());
    }

    @Test
    public void testGetActorOutcome() {
        assertNull(oper.getActorOutcome(null));

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
    public void testOnSuccess() throws Exception {
        AtomicInteger count = new AtomicInteger();

        final Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> nextStep = oper -> {
            count.incrementAndGet();
            return CompletableFuture.completedFuture(oper);
        };

        // pass it a null outcome
        ControlLoopOperation outcome = oper.onSuccess(params, nextStep).apply(null).get();
        assertNotNull(outcome);
        assertEquals(PolicyResult.FAILURE.toString(), outcome.getOutcome());
        assertEquals(0, count.get());

        // pass it an unpopulated (i.e., failed) outcome
        outcome = new ControlLoopOperation();
        assertSame(outcome, oper.onSuccess(params, nextStep).apply(outcome).get());
        assertEquals(0, count.get());

        // pass it a successful outcome
        outcome = params.makeOutcome();
        outcome.setOutcome(PolicyResult.SUCCESS.toString());
        assertSame(outcome, oper.onSuccess(params, nextStep).apply(outcome).get());
        assertEquals(PolicyResult.SUCCESS.toString(), outcome.getOutcome());
        assertEquals(1, count.get());
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

        verifyRun("testOnSuccessTrue_testHandleFailureTrue", 1, 1, PolicyResult.SUCCESS);
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

        verifyRun("testOnSuccessFalse_testHandleFailureFalse", 1, 0, PolicyResult.FAILURE_GUARD);
    }

    /**
     * Tests onSuccess() and handleFailure() when the outcome is {@code null}.
     */
    @Test
    public void testOnSuccessFalse_testHandleFailureNull() throws Exception {
        // arrange to return null from the preprocessor
        oper.setPreProcessor(oper -> {
            return CompletableFuture.completedFuture(null);
        });

        verifyRun("testOnSuccessFalse_testHandleFailureNull", 1, 0, PolicyResult.FAILURE_GUARD);
    }

    @Test
    public void testFromException() {
        // arrange to generate an exception when operation runs
        oper.setGenException(true);

        verifyRun("testFromException", 1, 1, PolicyResult.FAILURE_EXCEPTION);
    }

    /**
     * Tests fromException() when there is no exception.
     */
    @Test
    public void testFromExceptionNoExcept() {
        verifyRun("testFromExceptionNoExcept", 1, 1, PolicyResult.SUCCESS);
    }

    /**
     * Tests verifyRunning() when the pipeline is not running.
     */
    @Test
    public void testVerifyRunningWhenNot() {
        verifyRun("testVerifyRunningWhenNot", 0, 0, PolicyResult.SUCCESS, future -> future.cancel(false));
    }

    /**
     * Tests callbackStarted() when the pipeline has already been stopped.
     */
    @Test
    public void testCallbackStartedNotRunning() {
        AtomicReference<Future<ControlLoopOperation>> future = new AtomicReference<>();

        /*
         * arrange to stop the controller when the start-callback is invoked, but capture
         * the outcome
         */
        params = params.toBuilder().startCallback(oper -> {
            starter(oper);
            future.get().cancel(false);
        }).build();

        future.set(oper.startOperation(params));
        assertTrue(executor.runAll());

        // should have only run once
        assertEquals(1, numStart);
    }

    /**
     * Tests callbackCompleted() when the pipeline has already been stopped.
     */
    @Test
    public void testCallbackCompletedNotRunning() {
        AtomicReference<Future<ControlLoopOperation>> future = new AtomicReference<>();

        // arrange to stop the controller when the start-callback is invoked
        params = params.toBuilder().startCallback(oper -> {
            future.get().cancel(false);
        }).build();

        future.set(oper.startOperation(params));
        assertTrue(executor.runAll());

        // should not have been set
        assertNull(opend);
        assertEquals(0, numEnd);
    }

    @Test
    public void testSetOutcomeControlLoopOperationThrowable() {
        final CompletionException timex = new CompletionException(new TimeoutException(EXPECTED_EXCEPTION));

        ControlLoopOperation outcome;

        outcome = new ControlLoopOperation();
        oper.setOutcome(params, outcome, timex);
        assertEquals(ControlLoopOperation.FAILED_MSG, outcome.getMessage());
        assertEquals(PolicyResult.FAILURE_TIMEOUT.toString(), outcome.getOutcome());

        outcome = new ControlLoopOperation();
        oper.setOutcome(params, outcome, new IllegalStateException());
        assertEquals(ControlLoopOperation.FAILED_MSG, outcome.getMessage());
        assertEquals(PolicyResult.FAILURE_EXCEPTION.toString(), outcome.getOutcome());
    }

    @Test
    public void testSetOutcomeControlLoopOperationPolicyResult() {
        ControlLoopOperation outcome;

        outcome = new ControlLoopOperation();
        oper.setOutcome(params, outcome, PolicyResult.SUCCESS);
        assertEquals(ControlLoopOperation.SUCCESS_MSG, outcome.getMessage());
        assertEquals(PolicyResult.SUCCESS.toString(), outcome.getOutcome());

        for (PolicyResult result : FAILURE_RESULTS) {
            outcome = new ControlLoopOperation();
            oper.setOutcome(params, outcome, result);
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
        assertFalse(oper.isTimeout(new CompletionException(new CompletionException(timex))));

        assertTrue(oper.isTimeout(timex));
        assertTrue(oper.isTimeout(new CompletionException(timex)));
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

    /**
     * Gets a function that does nothing.
     *
     * @param <T> type of input parameter expected by the function
     * @return a function that does nothing
     */
    private <T> Consumer<T> noop() {
        return unused -> {
        };
    }

    /**
     * Verifies a run.
     *
     * @param testName test name
     * @param expectedCallbacks number of callbacks expected
     * @param expectedOperations number of operation invocations expected
     * @param expectedResult expected outcome
     */
    private void verifyRun(String testName, int expectedCallbacks, int expectedOperations,
                    PolicyResult expectedResult) {

        String expectedSubRequestId =
                        (expectedResult == PolicyResult.FAILURE_EXCEPTION ? null : String.valueOf(expectedOperations));

        verifyRun(testName, expectedCallbacks, expectedOperations, expectedResult, expectedSubRequestId, noop());
    }

    /**
     * Verifies a run.
     *
     * @param testName test name
     * @param expectedCallbacks number of callbacks expected
     * @param expectedOperations number of operation invocations expected
     * @param expectedResult expected outcome
     * @param manipulator function to modify the future returned by
     *        {@link OperatorPartial#startOperation(ControlLoopOperationParams)} before
     *        the tasks in the executor are run
     */
    private void verifyRun(String testName, int expectedCallbacks, int expectedOperations, PolicyResult expectedResult,
                    Consumer<CompletableFuture<ControlLoopOperation>> manipulator) {

        String expectedSubRequestId =
                        (expectedResult == PolicyResult.FAILURE_EXCEPTION ? null : String.valueOf(expectedOperations));

        verifyRun(testName, expectedCallbacks, expectedOperations, expectedResult, expectedSubRequestId, manipulator);
    }

    /**
     * Verifies a run.
     *
     * @param testName test name
     * @param expectedCallbacks number of callbacks expected
     * @param expectedOperations number of operation invocations expected
     * @param expectedResult expected outcome
     * @param expectedSubRequestId expected sub request ID
     * @param manipulator function to modify the future returned by
     *        {@link OperatorPartial#startOperation(ControlLoopOperationParams)} before
     *        the tasks in the executor are run
     */
    private void verifyRun(String testName, int expectedCallbacks, int expectedOperations, PolicyResult expectedResult,
                    String expectedSubRequestId, Consumer<CompletableFuture<ControlLoopOperation>> manipulator) {

        CompletableFuture<ControlLoopOperation> future = oper.startOperation(params);

        manipulator.accept(future);

        assertTrue(testName, executor.runAll());

        assertEquals(testName, expectedCallbacks, numStart);
        assertEquals(testName, expectedCallbacks, numEnd);

        if (expectedCallbacks > 0) {
            assertNotNull(testName, opstart);
            assertNotNull(testName, opend);
            assertEquals(testName, expectedResult.toString(), opend.getOutcome());

            assertSame(testName, tstart, opstart.getStart());
            assertSame(testName, tstart, opend.getStart());

            try {
                assertTrue(future.isDone());
                assertSame(testName, opend, future.get());

            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }

            if (expectedOperations > 0) {
                assertEquals(testName, expectedSubRequestId, opend.getSubRequestId());
            }
        }

        assertEquals(testName, expectedOperations, oper.getCount());
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
        protected ControlLoopOperation doOperation(ControlLoopOperationParams params, int attempt,
                        ControlLoopOperation operation) {
            ++count;
            if (genException) {
                throw new IllegalStateException(EXPECTED_EXCEPTION);
            }

            operation.setSubRequestId(String.valueOf(attempt));

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

        private Queue<Runnable> commands = new LinkedList<>();

        public MyExec() {
            // do nothing
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

            return commands.isEmpty();
        }
    }
}
