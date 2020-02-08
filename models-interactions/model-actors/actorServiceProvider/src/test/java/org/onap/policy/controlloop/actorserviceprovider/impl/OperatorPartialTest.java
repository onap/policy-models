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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
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

    private VirtualControlLoopEvent event;
    private Map<String, Object> config;
    private ControlLoopEventContext context;
    private MyExec executor;
    private ControlLoopOperationParams params;

    private MyOper oper;

    private int numStart;
    private int numEnd;

    private Instant tstart;

    private OperationOutcome opstart;
    private OperationOutcome opend;

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

        params = ControlLoopOperationParams.builder().completeCallback(this::completer).context(context)
                        .executor(executor).actor(ACTOR).operation(OPERATOR).timeoutSec(TIMEOUT)
                        .startCallback(this::starter).targetEntity(TARGET).build();

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
    public void testGetBlockingExecutor() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);

        /*
         * Use an operator that doesn't override getBlockingExecutor().
         */
        OperatorPartial oper2 = new OperatorPartial(ACTOR, OPERATOR) {};
        oper2.getBlockingExecutor().execute(() -> latch.countDown());

        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    public void testDoConfigure() {
        oper = spy(new MyOper());

        oper.configure(config);
        verify(oper).configure(config);

        // repeat - SHOULD be run again
        oper.configure(config);
        verify(oper, times(2)).configure(config);
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
    public void testStartOperation() {
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
    public void testStartOperationWithPreprocessor() {
        AtomicInteger count = new AtomicInteger();

        CompletableFuture<OperationOutcome> preproc = CompletableFuture.supplyAsync(() -> {
            count.incrementAndGet();
            return makeSuccess();
        }, executor);

        oper.setGuard(preproc);

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
        assertEquals(PolicyResult.SUCCESS, opend.getResult());

        assertEquals(MAX_PARALLEL_REQUESTS, numStart);
        assertEquals(MAX_PARALLEL_REQUESTS, oper.getCount());
        assertEquals(MAX_PARALLEL_REQUESTS, numEnd);
    }

    /**
     * Tests startPreprocessor() when the preprocessor returns a failure.
     */
    @Test
    public void testStartPreprocessorFailure() {
        oper.setGuard(CompletableFuture.completedFuture(makeFailure()));

        verifyRun("testStartPreprocessorFailure", 1, 0, PolicyResult.FAILURE_GUARD);
    }

    /**
     * Tests startPreprocessor() when the preprocessor throws an exception.
     */
    @Test
    public void testStartPreprocessorException() {
        // arrange for the preprocessor to throw an exception
        oper.setGuard(CompletableFuture.failedFuture(new IllegalStateException(EXPECTED_EXCEPTION)));

        verifyRun("testStartPreprocessorException", 1, 0, PolicyResult.FAILURE_GUARD);
    }

    /**
     * Tests startPreprocessor() when the pipeline is not running.
     */
    @Test
    public void testStartPreprocessorNotRunning() {
        // arrange for the preprocessor to return success, which will be ignored
        oper.setGuard(CompletableFuture.completedFuture(makeSuccess()));

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
            protected CompletableFuture<OperationOutcome> startPreprocessorAsync(ControlLoopOperationParams params) {
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
    public void testStartPreprocessorAsync() {
        assertNull(oper.startPreprocessorAsync(params));
    }

    @Test
    public void testStartGuardAsync() {
        assertNull(oper.startGuardAsync(params));
    }

    @Test
    public void testStartOperationAsync() {
        oper.startOperation(params);
        assertTrue(executor.runAll());

        assertEquals(1, oper.getCount());
    }

    @Test
    public void testIsSuccess() {
        OperationOutcome outcome = new OperationOutcome();

        outcome.setResult(PolicyResult.SUCCESS);
        assertTrue(oper.isSuccess(outcome));

        for (PolicyResult failure : FAILURE_RESULTS) {
            outcome.setResult(failure);
            assertFalse("testIsSuccess-" + failure, oper.isSuccess(outcome));
        }
    }

    @Test
    public void testIsActorFailed() {
        assertFalse(oper.isActorFailed(null));

        OperationOutcome outcome = params.makeOutcome();

        // incorrect outcome
        outcome.setResult(PolicyResult.SUCCESS);
        assertFalse(oper.isActorFailed(outcome));

        outcome.setResult(PolicyResult.FAILURE_RETRIES);
        assertFalse(oper.isActorFailed(outcome));

        // correct outcome
        outcome.setResult(PolicyResult.FAILURE);

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
            protected Executor getBlockingExecutor() {
                return executor;
            }
        };

        oper2.configure(new TreeMap<>());
        oper2.start();

        oper2.startOperation(params);
        assertTrue(executor.runAll());

        assertNotNull(opend);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, opend.getResult());
    }

    @Test
    public void testTimeout() throws Exception {

        // use a real executor
        params = params.toBuilder().executor(ForkJoinPool.commonPool()).build();

        // trigger timeout very quickly
        oper = new MyOper() {
            @Override
            protected long getTimeOutMs(Integer timeoutSec) {
                return 1;
            }

            @Override
            protected CompletableFuture<OperationOutcome> startOperationAsync(ControlLoopOperationParams params,
                            int attempt, OperationOutcome outcome) {

                OperationOutcome outcome2 = params.makeOutcome();
                outcome2.setResult(PolicyResult.SUCCESS);

                /*
                 * Create an incomplete future that will timeout after the operation's
                 * timeout. If it fires before the other timer, then it will return a
                 * SUCCESS outcome.
                 */
                CompletableFuture<OperationOutcome> future = new CompletableFuture<>();
                future = future.orTimeout(1, TimeUnit.SECONDS).handleAsync((unused1, unused2) -> outcome,
                                params.getExecutor());

                return future;
            }
        };

        oper.configure(new TreeMap<>());
        oper.start();

        assertEquals(PolicyResult.FAILURE_TIMEOUT, oper.startOperation(params).get().getResult());
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
            protected long getTimeOutMs(Integer timeoutSec) {
                return 10;
            }

            @Override
            protected Executor getBlockingExecutor() {
                return command -> {
                    Thread thread = new Thread(command);
                    thread.start();
                };
            }

            @Override
            protected CompletableFuture<OperationOutcome> startPreprocessorAsync(ControlLoopOperationParams params) {

                OperationOutcome outcome = makeSuccess();

                /*
                 * Create an incomplete future that will timeout after the operation's
                 * timeout. If it fires before the other timer, then it will return a
                 * SUCCESS outcome.
                 */
                CompletableFuture<OperationOutcome> future = new CompletableFuture<>();
                future = future.orTimeout(200, TimeUnit.MILLISECONDS).handleAsync((unused1, unused2) -> outcome,
                                params.getExecutor());

                return future;
            }
        };

        oper.configure(new TreeMap<>());
        oper.start();

        OperationOutcome result = oper.startOperation(params).get();
        assertEquals(PolicyResult.SUCCESS, result.getResult());

        assertNotNull(opstart);
        assertNotNull(opend);
        assertEquals(PolicyResult.SUCCESS, opend.getResult());

        assertEquals(1, numStart);
        assertEquals(1, oper.getCount());
        assertEquals(1, numEnd);
    }

    /**
     * Tests retry functions, when the count is set to zero and retries are exhausted.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_ZeroRetries_testStartOperationAttempt() {
        params = params.toBuilder().retry(0).build();
        oper.setMaxFailures(10);

        verifyRun("testSetRetryFlag_testRetryOnFailure_ZeroRetries", 1, 1, PolicyResult.FAILURE);
    }

    /**
     * Tests retry functions, when the count is null and retries are exhausted.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_NullRetries() {
        params = params.toBuilder().retry(null).build();
        oper.setMaxFailures(10);

        verifyRun("testSetRetryFlag_testRetryOnFailure_NullRetries", 1, 1, PolicyResult.FAILURE);
    }

    /**
     * Tests retry functions, when retries are exhausted.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_RetriesExhausted() {
        final int maxRetries = 3;
        params = params.toBuilder().retry(maxRetries).build();
        oper.setMaxFailures(10);

        verifyRun("testSetRetryFlag_testRetryOnFailure_RetriesExhausted", maxRetries + 1, maxRetries + 1,
                        PolicyResult.FAILURE_RETRIES);
    }

    /**
     * Tests retry functions, when a success follows some retries.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_SuccessAfterRetries() {
        params = params.toBuilder().retry(10).build();

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
            protected OperationOutcome doOperation(ControlLoopOperationParams params, int attempt,
                            OperationOutcome operation) {

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
    public void testSleep() throws Exception {
        CompletableFuture<Void> future = oper.sleep(-1, TimeUnit.SECONDS);
        assertTrue(future.isDone());
        assertNull(future.get());

        // edge case
        future = oper.sleep(0, TimeUnit.SECONDS);
        assertTrue(future.isDone());
        assertNull(future.get());

        /*
         * Start a second sleep we can use to check the first while it's running.
         */
        tstart = Instant.now();
        future = oper.sleep(100, TimeUnit.MILLISECONDS);

        CompletableFuture<Void> future2 = oper.sleep(10, TimeUnit.MILLISECONDS);

        // wait for second to complete and verify that the first has not completed
        future2.get();
        assertFalse(future.isDone());

        // wait for second to complete
        future.get();

        long diff = Instant.now().toEpochMilli() - tstart.toEpochMilli();
        assertTrue(diff >= 99);
    }

    @Test
    public void testIsSameOperation() {
        assertFalse(oper.isSameOperation(null));

        OperationOutcome outcome = params.makeOutcome();

        // wrong actor - should be false
        outcome.setActor(null);
        assertFalse(oper.isSameOperation(outcome));
        outcome.setActor(TARGET);
        assertFalse(oper.isSameOperation(outcome));
        outcome.setActor(ACTOR);

        // wrong operation - should be null
        outcome.setOperation(null);
        assertFalse(oper.isSameOperation(outcome));
        outcome.setOperation(TARGET);
        assertFalse(oper.isSameOperation(outcome));
        outcome.setOperation(OPERATOR);

        assertTrue(oper.isSameOperation(outcome));
    }

    /**
     * Tests handleFailure() when the outcome is a success.
     */
    @Test
    public void testHandlePreprocessorFailureTrue() {
        oper.setGuard(CompletableFuture.completedFuture(makeSuccess()));
        verifyRun("testHandlePreprocessorFailureTrue", 1, 1, PolicyResult.SUCCESS);
    }

    /**
     * Tests handleFailure() when the outcome is <i>not</i> a success.
     */
    @Test
    public void testHandlePreprocessorFailureFalse() throws Exception {
        oper.setGuard(CompletableFuture.completedFuture(makeFailure()));
        verifyRun("testHandlePreprocessorFailureFalse", 1, 0, PolicyResult.FAILURE_GUARD);
    }

    /**
     * Tests handleFailure() when the outcome is {@code null}.
     */
    @Test
    public void testHandlePreprocessorFailureNull() throws Exception {
        // arrange to return null from the preprocessor
        oper.setGuard(CompletableFuture.completedFuture(null));

        verifyRun("testHandlePreprocessorFailureNull", 1, 0, PolicyResult.FAILURE_GUARD);
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
     * Tests both flavors of anyOf(), because one invokes the other.
     */
    @Test
    public void testAnyOf() throws Exception {
        // first task completes, others do not
        List<CompletableFuture<OperationOutcome>> tasks = new LinkedList<>();

        final OperationOutcome outcome = params.makeOutcome();

        tasks.add(CompletableFuture.completedFuture(outcome));
        tasks.add(new CompletableFuture<>());
        tasks.add(new CompletableFuture<>());

        CompletableFuture<OperationOutcome> result = oper.anyOf(params, tasks);
        assertTrue(executor.runAll());

        assertTrue(result.isDone());
        assertSame(outcome, result.get());

        // second task completes, others do not
        tasks = new LinkedList<>();

        tasks.add(new CompletableFuture<>());
        tasks.add(CompletableFuture.completedFuture(outcome));
        tasks.add(new CompletableFuture<>());

        result = oper.anyOf(params, tasks);
        assertTrue(executor.runAll());

        assertTrue(result.isDone());
        assertSame(outcome, result.get());

        // third task completes, others do not
        tasks = new LinkedList<>();

        tasks.add(new CompletableFuture<>());
        tasks.add(new CompletableFuture<>());
        tasks.add(CompletableFuture.completedFuture(outcome));

        result = oper.anyOf(params, tasks);
        assertTrue(executor.runAll());

        assertTrue(result.isDone());
        assertSame(outcome, result.get());
    }

    /**
     * Tests both flavors of anyOf(), for edge cases: zero items, and one item.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testAnyOfEdge() throws Exception {
        List<CompletableFuture<OperationOutcome>> tasks = new LinkedList<>();

        // zero items: check both using a list and using an array
        assertThatIllegalArgumentException().isThrownBy(() -> oper.anyOf(params, tasks));
        assertThatIllegalArgumentException().isThrownBy(() -> oper.anyOf(params));

        // one item: : check both using a list and using an array
        CompletableFuture<OperationOutcome> future1 = new CompletableFuture<>();
        tasks.add(future1);

        assertSame(future1, oper.anyOf(params, tasks));
        assertSame(future1, oper.anyOf(params, future1));
    }

    /**
     * Tests both flavors of allOf(), because one invokes the other.
     */
    @Test
    public void testAllOf() throws Exception {
        List<CompletableFuture<OperationOutcome>> tasks = new LinkedList<>();

        final OperationOutcome outcome = params.makeOutcome();

        CompletableFuture<OperationOutcome> future1 = new CompletableFuture<>();
        CompletableFuture<OperationOutcome> future2 = new CompletableFuture<>();
        CompletableFuture<OperationOutcome> future3 = new CompletableFuture<>();

        tasks.add(future1);
        tasks.add(future2);
        tasks.add(future3);

        CompletableFuture<OperationOutcome> result = oper.allOf(params, tasks);

        assertTrue(executor.runAll());
        assertFalse(result.isDone());
        future1.complete(outcome);

        // complete 3 before 2
        assertTrue(executor.runAll());
        assertFalse(result.isDone());
        future3.complete(outcome);

        assertTrue(executor.runAll());
        assertFalse(result.isDone());
        future2.complete(outcome);

        // all of them are now done
        assertTrue(executor.runAll());
        assertTrue(result.isDone());
        assertSame(outcome, result.get());
    }

    /**
     * Tests both flavors of allOf(), for edge cases: zero items, and one item.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testAllOfEdge() throws Exception {
        List<CompletableFuture<OperationOutcome>> tasks = new LinkedList<>();

        // zero items: check both using a list and using an array
        assertThatIllegalArgumentException().isThrownBy(() -> oper.allOf(params, tasks));
        assertThatIllegalArgumentException().isThrownBy(() -> oper.allOf(params));

        // one item: : check both using a list and using an array
        CompletableFuture<OperationOutcome> future1 = new CompletableFuture<>();
        tasks.add(future1);

        assertSame(future1, oper.allOf(params, tasks));
        assertSame(future1, oper.allOf(params, future1));
    }

    @Test
    public void testCombineOutcomes() throws Exception {
        // only one outcome
        verifyOutcomes(0, PolicyResult.SUCCESS);
        verifyOutcomes(0, PolicyResult.FAILURE_EXCEPTION);

        // maximum is in different positions
        verifyOutcomes(0, PolicyResult.FAILURE, PolicyResult.SUCCESS, PolicyResult.FAILURE_GUARD);
        verifyOutcomes(1, PolicyResult.SUCCESS, PolicyResult.FAILURE, PolicyResult.FAILURE_GUARD);
        verifyOutcomes(2, PolicyResult.SUCCESS, PolicyResult.FAILURE_GUARD, PolicyResult.FAILURE);

        // null outcome
        final List<CompletableFuture<OperationOutcome>> tasks = new LinkedList<>();
        tasks.add(CompletableFuture.completedFuture(null));
        CompletableFuture<OperationOutcome> result = oper.allOf(params, tasks);

        assertTrue(executor.runAll());
        assertTrue(result.isDone());
        assertNull(result.get());

        // one throws an exception during execution
        IllegalStateException except = new IllegalStateException(EXPECTED_EXCEPTION);

        tasks.clear();
        tasks.add(CompletableFuture.completedFuture(params.makeOutcome()));
        tasks.add(CompletableFuture.failedFuture(except));
        tasks.add(CompletableFuture.completedFuture(params.makeOutcome()));
        result = oper.allOf(params, tasks);

        assertTrue(executor.runAll());
        assertTrue(result.isCompletedExceptionally());
        result.whenComplete((unused, thrown) -> assertSame(except, thrown));
    }

    private void verifyOutcomes(int expected, PolicyResult... results) throws Exception {
        List<CompletableFuture<OperationOutcome>> tasks = new LinkedList<>();


        OperationOutcome expectedOutcome = null;

        for (int count = 0; count < results.length; ++count) {
            OperationOutcome outcome = params.makeOutcome();
            outcome.setResult(results[count]);
            tasks.add(CompletableFuture.completedFuture(outcome));

            if (count == expected) {
                expectedOutcome = outcome;
            }
        }

        CompletableFuture<OperationOutcome> result = oper.allOf(params, tasks);

        assertTrue(executor.runAll());
        assertTrue(result.isDone());
        assertSame(expectedOutcome, result.get());
    }

    private Function<OperationOutcome, CompletableFuture<OperationOutcome>> makeTask(
                    final OperationOutcome taskOutcome) {

        return outcome -> CompletableFuture.completedFuture(taskOutcome);
    }

    @Test
    public void testDetmPriority() throws CoderException {
        assertEquals(1, oper.detmPriority(null));

        OperationOutcome outcome = params.makeOutcome();

        Map<PolicyResult, Integer> map = Map.of(PolicyResult.SUCCESS, 0, PolicyResult.FAILURE_GUARD, 2,
                        PolicyResult.FAILURE_RETRIES, 3, PolicyResult.FAILURE, 4, PolicyResult.FAILURE_TIMEOUT, 5,
                        PolicyResult.FAILURE_EXCEPTION, 6);

        for (Entry<PolicyResult, Integer> ent : map.entrySet()) {
            outcome.setResult(ent.getKey());
            assertEquals(ent.getKey().toString(), ent.getValue().intValue(), oper.detmPriority(outcome));
        }

        /*
         * Test null result. We can't actually set it to null, because the set() method
         * won't allow it. Instead, we decode it from a structure.
         */
        outcome = new StandardCoder().decode("{\"result\":null}", OperationOutcome.class);
        assertEquals(1, oper.detmPriority(outcome));
    }

    /**
     * Tests doTask(Future) when the controller is not running.
     */
    @Test
    public void testDoTaskFutureNotRunning() throws Exception {
        CompletableFuture<OperationOutcome> taskFuture = new CompletableFuture<>();

        PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();
        controller.complete(params.makeOutcome());

        CompletableFuture<OperationOutcome> future =
                        oper.doTask(params, controller, false, params.makeOutcome(), taskFuture);
        assertFalse(future.isDone());
        assertTrue(executor.runAll());

        // should not have run the task
        assertFalse(future.isDone());

        // should have canceled the task future
        assertTrue(taskFuture.isCancelled());
    }

    /**
     * Tests doTask(Future) when the previous outcome was successful.
     */
    @Test
    public void testDoTaskFutureSuccess() throws Exception {
        CompletableFuture<OperationOutcome> taskFuture = new CompletableFuture<>();
        final OperationOutcome taskOutcome = params.makeOutcome();

        PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        CompletableFuture<OperationOutcome> future =
                        oper.doTask(params, controller, true, params.makeOutcome(), taskFuture);

        taskFuture.complete(taskOutcome);
        assertTrue(executor.runAll());

        assertTrue(future.isDone());
        assertSame(taskOutcome, future.get());

        // controller should not be done yet
        assertFalse(controller.isDone());
    }

    /**
     * Tests doTask(Future) when the previous outcome was failed.
     */
    @Test
    public void testDoTaskFutureFailure() throws Exception {
        CompletableFuture<OperationOutcome> taskFuture = new CompletableFuture<>();
        final OperationOutcome failedOutcome = params.makeOutcome();
        failedOutcome.setResult(PolicyResult.FAILURE);

        PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        CompletableFuture<OperationOutcome> future = oper.doTask(params, controller, true, failedOutcome, taskFuture);
        assertFalse(future.isDone());
        assertTrue(executor.runAll());

        // should not have run the task
        assertFalse(future.isDone());

        // should have canceled the task future
        assertTrue(taskFuture.isCancelled());

        // controller SHOULD be done now
        assertTrue(controller.isDone());
        assertSame(failedOutcome, controller.get());
    }

    /**
     * Tests doTask(Future) when the previous outcome was failed, but not checking
     * success.
     */
    @Test
    public void testDoTaskFutureUncheckedFailure() throws Exception {
        CompletableFuture<OperationOutcome> taskFuture = new CompletableFuture<>();
        final OperationOutcome failedOutcome = params.makeOutcome();
        failedOutcome.setResult(PolicyResult.FAILURE);

        PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        CompletableFuture<OperationOutcome> future = oper.doTask(params, controller, false, failedOutcome, taskFuture);
        assertFalse(future.isDone());

        // complete the task
        OperationOutcome taskOutcome = params.makeOutcome();
        taskFuture.complete(taskOutcome);

        assertTrue(executor.runAll());

        // should have run the task
        assertTrue(future.isDone());

        assertTrue(future.isDone());
        assertSame(taskOutcome, future.get());

        // controller should not be done yet
        assertFalse(controller.isDone());
    }

    /**
     * Tests doTask(Function) when the controller is not running.
     */
    @Test
    public void testDoTaskFunctionNotRunning() throws Exception {
        AtomicBoolean invoked = new AtomicBoolean();

        Function<OperationOutcome, CompletableFuture<OperationOutcome>> task = outcome -> {
            invoked.set(true);
            return CompletableFuture.completedFuture(params.makeOutcome());
        };

        PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();
        controller.complete(params.makeOutcome());

        CompletableFuture<OperationOutcome> future =
                        oper.doTask(params, controller, false, task).apply(params.makeOutcome());
        assertFalse(future.isDone());
        assertTrue(executor.runAll());

        // should not have run the task
        assertFalse(future.isDone());

        // should not have even invoked the task
        assertFalse(invoked.get());
    }

    /**
     * Tests doTask(Function) when the previous outcome was successful.
     */
    @Test
    public void testDoTaskFunctionSuccess() throws Exception {
        final OperationOutcome taskOutcome = params.makeOutcome();

        final OperationOutcome failedOutcome = params.makeOutcome();

        Function<OperationOutcome, CompletableFuture<OperationOutcome>> task = makeTask(taskOutcome);

        PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        CompletableFuture<OperationOutcome> future = oper.doTask(params, controller, true, task).apply(failedOutcome);

        assertTrue(future.isDone());
        assertSame(taskOutcome, future.get());

        // controller should not be done yet
        assertFalse(controller.isDone());
    }

    /**
     * Tests doTask(Function) when the previous outcome was failed.
     */
    @Test
    public void testDoTaskFunctionFailure() throws Exception {
        final OperationOutcome failedOutcome = params.makeOutcome();
        failedOutcome.setResult(PolicyResult.FAILURE);

        AtomicBoolean invoked = new AtomicBoolean();

        Function<OperationOutcome, CompletableFuture<OperationOutcome>> task = outcome -> {
            invoked.set(true);
            return CompletableFuture.completedFuture(params.makeOutcome());
        };

        PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        CompletableFuture<OperationOutcome> future = oper.doTask(params, controller, true, task).apply(failedOutcome);
        assertFalse(future.isDone());
        assertTrue(executor.runAll());

        // should not have run the task
        assertFalse(future.isDone());

        // should not have even invoked the task
        assertFalse(invoked.get());

        // controller should have the failed task
        assertTrue(controller.isDone());
        assertSame(failedOutcome, controller.get());
    }

    /**
     * Tests doTask(Function) when the previous outcome was failed, but not checking
     * success.
     */
    @Test
    public void testDoTaskFunctionUncheckedFailure() throws Exception {
        final OperationOutcome taskOutcome = params.makeOutcome();

        final OperationOutcome failedOutcome = params.makeOutcome();
        failedOutcome.setResult(PolicyResult.FAILURE);

        Function<OperationOutcome, CompletableFuture<OperationOutcome>> task = makeTask(taskOutcome);

        PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        CompletableFuture<OperationOutcome> future = oper.doTask(params, controller, false, task).apply(failedOutcome);

        assertTrue(future.isDone());
        assertSame(taskOutcome, future.get());

        // controller should not be done yet
        assertFalse(controller.isDone());
    }

    /**
     * Tests callbackStarted() when the pipeline has already been stopped.
     */
    @Test
    public void testCallbackStartedNotRunning() {
        AtomicReference<Future<OperationOutcome>> future = new AtomicReference<>();

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
        AtomicReference<Future<OperationOutcome>> future = new AtomicReference<>();

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
    public void testSetOutcomeControlLoopOperationOutcomeThrowable() {
        final CompletionException timex = new CompletionException(new TimeoutException(EXPECTED_EXCEPTION));

        OperationOutcome outcome;

        outcome = new OperationOutcome();
        oper.setOutcome(params, outcome, timex);
        assertEquals(ControlLoopOperation.FAILED_MSG, outcome.getMessage());
        assertEquals(PolicyResult.FAILURE_TIMEOUT, outcome.getResult());

        outcome = new OperationOutcome();
        oper.setOutcome(params, outcome, new IllegalStateException(EXPECTED_EXCEPTION));
        assertEquals(ControlLoopOperation.FAILED_MSG, outcome.getMessage());
        assertEquals(PolicyResult.FAILURE_EXCEPTION, outcome.getResult());
    }

    @Test
    public void testSetOutcomeControlLoopOperationOutcomePolicyResult() {
        OperationOutcome outcome;

        outcome = new OperationOutcome();
        oper.setOutcome(params, outcome, PolicyResult.SUCCESS);
        assertEquals(ControlLoopOperation.SUCCESS_MSG, outcome.getMessage());
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());

        for (PolicyResult result : FAILURE_RESULTS) {
            outcome = new OperationOutcome();
            oper.setOutcome(params, outcome, result);
            assertEquals(result.toString(), ControlLoopOperation.FAILED_MSG, outcome.getMessage());
            assertEquals(result.toString(), result, outcome.getResult());
        }
    }

    @Test
    public void testIsTimeout() {
        final TimeoutException timex = new TimeoutException(EXPECTED_EXCEPTION);

        assertFalse(oper.isTimeout(new IllegalStateException(EXPECTED_EXCEPTION)));
        assertFalse(oper.isTimeout(new IllegalStateException(timex)));
        assertFalse(oper.isTimeout(new CompletionException(new IllegalStateException(timex))));
        assertFalse(oper.isTimeout(new CompletionException(null)));
        assertFalse(oper.isTimeout(new CompletionException(new CompletionException(timex))));

        assertTrue(oper.isTimeout(timex));
        assertTrue(oper.isTimeout(new CompletionException(timex)));
    }

    @Test
    public void testGetRetry() {
        assertEquals(0, oper.getRetry(null));
        assertEquals(10, oper.getRetry(10));
    }

    @Test
    public void testGetRetryWait() {
        // need an operator that doesn't override the retry time
        OperatorPartial oper2 = new OperatorPartial(ACTOR, OPERATOR) {};
        assertEquals(OperatorPartial.DEFAULT_RETRY_WAIT_MS, oper2.getRetryWaitMs());
    }

    @Test
    public void testGetTimeOutMs() {
        assertEquals(TIMEOUT * 1000, oper.getTimeOutMs(params.getTimeoutSec()));

        params = params.toBuilder().timeoutSec(null).build();
        assertEquals(0, oper.getTimeOutMs(params.getTimeoutSec()));
    }

    private void starter(OperationOutcome oper) {
        ++numStart;
        tstart = oper.getStart();
        opstart = oper;
    }

    private void completer(OperationOutcome oper) {
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

    private OperationOutcome makeSuccess() {
        OperationOutcome outcome = params.makeOutcome();
        outcome.setResult(PolicyResult.SUCCESS);

        return outcome;
    }

    private OperationOutcome makeFailure() {
        OperationOutcome outcome = params.makeOutcome();
        outcome.setResult(PolicyResult.FAILURE);

        return outcome;
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
     * @param expectedSubRequestId expected sub request ID
     * @param manipulator function to modify the future returned by
     *        {@link OperatorPartial#startOperation(ControlLoopOperationParams)} before
     *        the tasks in the executor are run
     */
    private void verifyRun(String testName, int expectedCallbacks, int expectedOperations, PolicyResult expectedResult,
                    String expectedSubRequestId, Consumer<CompletableFuture<OperationOutcome>> manipulator) {

        CompletableFuture<OperationOutcome> future = oper.startOperation(params);

        manipulator.accept(future);

        assertTrue(testName, executor.runAll());

        assertEquals(testName, expectedCallbacks, numStart);
        assertEquals(testName, expectedCallbacks, numEnd);

        if (expectedCallbacks > 0) {
            assertNotNull(testName, opstart);
            assertNotNull(testName, opend);
            assertEquals(testName, expectedResult, opend.getResult());

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

    private class MyOper extends OperatorPartial {
        @Getter
        private int count = 0;

        @Setter
        private boolean genException;

        @Setter
        private int maxFailures = 0;

        @Setter
        private CompletableFuture<OperationOutcome> guard;


        public MyOper() {
            super(ACTOR, OPERATOR);
        }

        @Override
        protected OperationOutcome doOperation(ControlLoopOperationParams params, int attempt,
                        OperationOutcome operation) {
            ++count;
            if (genException) {
                throw new IllegalStateException(EXPECTED_EXCEPTION);
            }

            operation.setSubRequestId(String.valueOf(attempt));

            if (count > maxFailures) {
                operation.setResult(PolicyResult.SUCCESS);
            } else {
                operation.setResult(PolicyResult.FAILURE);
            }

            return operation;
        }

        @Override
        protected CompletableFuture<OperationOutcome> startGuardAsync(ControlLoopOperationParams params) {
            return (guard != null ? guard : super.startGuardAsync(params));
        }

        @Override
        protected Executor getBlockingExecutor() {
            return executor;
        }

        @Override
        protected long getRetryWaitMs() {
            /*
             * Sleep timers run in the background, but we want to control things via the
             * "executor", thus we avoid sleep timers altogether by simply returning 0.
             */
            return 0L;
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
