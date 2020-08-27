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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;
import org.onap.policy.common.utils.time.PseudoExecutor;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.Operator;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.OperatorConfig;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.slf4j.LoggerFactory;

public class OperationPartialTest {
    private static final CommInfrastructure SINK_INFRA = CommInfrastructure.NOOP;
    private static final CommInfrastructure SOURCE_INFRA = CommInfrastructure.UEB;
    private static final int MAX_REQUESTS = 100;
    private static final int MAX_PARALLEL = 10;
    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";
    private static final String MY_SINK = "my-sink";
    private static final String MY_SOURCE = "my-source";
    private static final String MY_TARGET_ENTITY = "my-entity";
    private static final String TEXT = "my-text";
    private static final int TIMEOUT = 1000;
    private static final UUID REQ_ID = UUID.randomUUID();

    private static final List<OperationResult> FAILURE_RESULTS = Arrays.asList(OperationResult.values()).stream()
                    .filter(result -> result != OperationResult.SUCCESS).collect(Collectors.toList());

    /**
     * Used to attach an appender to the class' logger.
     */
    private static final Logger logger = (Logger) LoggerFactory.getLogger(OperationPartial.class);
    private static final ExtractAppender appender = new ExtractAppender();

    private static final List<String> PROP_NAMES = List.of("hello", "world");

    @Mock
    private ActorService service;
    @Mock
    private Actor guardActor;
    @Mock
    private Operator guardOperator;
    @Mock
    private Operation guardOperation;

    private VirtualControlLoopEvent event;
    private ControlLoopEventContext context;
    private PseudoExecutor executor;
    private ControlLoopOperationParams params;

    private MyOper oper;

    private int numStart;
    private int numEnd;

    private Instant tstart;

    private OperationOutcome opstart;
    private OperationOutcome opend;

    private Deque<OperationOutcome> starts;
    private Deque<OperationOutcome> ends;

    private OperatorConfig config;

    /**
     * Attaches the appender to the logger.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        /*
         * Attach appender to the logger.
         */
        appender.setContext(logger.getLoggerContext());
        appender.start();

        logger.addAppender(appender);
    }

    /**
     * Stops the appender.
     */
    @AfterClass
    public static void tearDownAfterClass() {
        appender.stop();
    }

    /**
     * Initializes the fields, including {@link #oper}.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        event = new VirtualControlLoopEvent();
        event.setRequestId(REQ_ID);

        context = new ControlLoopEventContext(event);
        executor = new PseudoExecutor();

        params = ControlLoopOperationParams.builder().completeCallback(this::completer).context(context)
                        .executor(executor).actorService(service).actor(ACTOR).operation(OPERATION).timeoutSec(TIMEOUT)
                        .startCallback(this::starter).targetEntity(MY_TARGET_ENTITY).build();

        when(service.getActor(OperationPartial.GUARD_ACTOR_NAME)).thenReturn(guardActor);
        when(guardActor.getOperator(OperationPartial.GUARD_OPERATION_NAME)).thenReturn(guardOperator);
        when(guardOperator.buildOperation(any())).thenReturn(guardOperation);
        when(guardOperation.start()).thenReturn(CompletableFuture.completedFuture(makeSuccess()));

        config = new OperatorConfig(executor);

        oper = new MyOper();

        tstart = null;

        opstart = null;
        opend = null;

        starts = new ArrayDeque<>(10);
        ends = new ArrayDeque<>(10);
    }

    @Test
    public void testOperatorPartial_testGetActorName_testGetName() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(ACTOR + "." + OPERATION, oper.getFullName());
    }

    @Test
    public void testGetBlockingThread() throws Exception {
        CompletableFuture<Void> future = new CompletableFuture<>();

        // use the real executor
        OperatorPartial oper2 = new OperatorPartial(ACTOR, OPERATION) {
            @Override
            public Operation buildOperation(ControlLoopOperationParams params) {
                return null;
            }
        };

        oper2.getBlockingExecutor().execute(() -> future.complete(null));

        assertNull(future.get(5, TimeUnit.SECONDS));
    }

    @Test
    public void testGetPropertyNames() {
        assertThat(oper.getPropertyNames()).isEqualTo(PROP_NAMES);
    }

    @Test
    public void testGetProperty_testSetProperty() {
        oper.setProperty("propertyA", "valueA");
        oper.setProperty("propertyB", "valueB");
        oper.setProperty("propertyC", 20);

        assertEquals("valueA", oper.getProperty("propertyA"));
        assertEquals("valueB", oper.getProperty("propertyB"));
        assertEquals(Integer.valueOf(20), oper.getProperty("propertyC"));
    }

    @Test
    public void testStart() {
        verifyRun("testStart", 1, 1, OperationResult.SUCCESS);
    }

    /**
     * Tests start() with multiple running requests.
     */
    @Test
    public void testStartMultiple() {
        for (int count = 0; count < MAX_PARALLEL; ++count) {
            oper.start();
        }

        assertTrue(executor.runAll(MAX_REQUESTS * MAX_PARALLEL));

        assertNotNull(opstart);
        assertNotNull(opend);
        assertEquals(OperationResult.SUCCESS, opend.getResult());

        assertEquals(MAX_PARALLEL, numStart);
        assertEquals(MAX_PARALLEL, oper.getCount());
        assertEquals(MAX_PARALLEL, numEnd);
    }

    /**
     * Tests startPreprocessor() when the preprocessor returns a failure.
     */
    @Test
    public void testStartPreprocessorFailure() {
        oper.setPreProc(CompletableFuture.completedFuture(makeFailure()));

        verifyRun("testStartPreprocessorFailure", 1, 0, OperationResult.FAILURE_GUARD);
    }

    /**
     * Tests startPreprocessor() when the preprocessor throws an exception.
     */
    @Test
    public void testStartPreprocessorException() {
        // arrange for the preprocessor to throw an exception
        oper.setPreProc(CompletableFuture.failedFuture(new IllegalStateException(EXPECTED_EXCEPTION)));

        verifyRun("testStartPreprocessorException", 1, 0, OperationResult.FAILURE_GUARD);
    }

    /**
     * Tests startPreprocessor() when the pipeline is not running.
     */
    @Test
    public void testStartPreprocessorNotRunning() {
        // arrange for the preprocessor to return success, which will be ignored
        // oper.setGuard(CompletableFuture.completedFuture(makeSuccess()));

        oper.start().cancel(false);
        assertTrue(executor.runAll(MAX_REQUESTS));

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
            protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
                throw new IllegalStateException(EXPECTED_EXCEPTION);
            }
        };

        assertThatIllegalStateException().isThrownBy(() -> oper.start());

        // should be nothing in the queue
        assertEquals(0, executor.getQueueLength());
    }

    @Test
    public void testStartPreprocessorAsync() {
        assertNull(oper.startPreprocessorAsync());
    }

    @Test
    public void testStartGuardAsync() throws Exception {
        CompletableFuture<OperationOutcome> future = oper.startGuardAsync();
        assertTrue(future.isDone());
        assertEquals(OperationResult.SUCCESS, future.get().getResult());

        // verify the parameters that were passed
        ArgumentCaptor<ControlLoopOperationParams> paramsCaptor =
                        ArgumentCaptor.forClass(ControlLoopOperationParams.class);
        verify(guardOperator).buildOperation(paramsCaptor.capture());

        params = paramsCaptor.getValue();
        assertEquals(OperationPartial.GUARD_ACTOR_NAME, params.getActor());
        assertEquals(OperationPartial.GUARD_OPERATION_NAME, params.getOperation());
        assertNull(params.getRetry());
        assertNull(params.getTimeoutSec());

        Map<String, Object> payload = params.getPayload();
        assertNotNull(payload);

        assertEquals(oper.makeGuardPayload(), payload);
    }

    /**
     * Tests startGuardAsync() when preprocessing is disabled.
     */
    @Test
    public void testStartGuardAsyncDisabled() {
        params = params.toBuilder().preprocessed(true).build();
        assertNull(new MyOper().startGuardAsync());
    }

    @Test
    public void testMakeGuardPayload() {
        Map<String, Object> payload = oper.makeGuardPayload();
        assertSame(REQ_ID, payload.get("requestId"));

        // request id changes, so remove it
        payload.remove("requestId");

        assertEquals("{actor=my-actor, operation=my-operation, target=my-entity}", payload.toString());

        // repeat, but with closed loop name
        event.setClosedLoopControlName("my-loop");
        payload = oper.makeGuardPayload();
        payload.remove("requestId");
        assertEquals("{actor=my-actor, operation=my-operation, target=my-entity, clname=my-loop}", payload.toString());
    }

    @Test
    public void testStartOperationAsync() {
        oper.start();
        assertTrue(executor.runAll(MAX_REQUESTS));

        assertEquals(1, oper.getCount());
    }

    @Test
    public void testIsSuccess() {
        assertFalse(oper.isSuccess(null));

        OperationOutcome outcome = new OperationOutcome();

        outcome.setResult(OperationResult.SUCCESS);
        assertTrue(oper.isSuccess(outcome));

        for (OperationResult failure : FAILURE_RESULTS) {
            outcome.setResult(failure);
            assertFalse("testIsSuccess-" + failure, oper.isSuccess(outcome));
        }
    }

    @Test
    public void testIsActorFailed() {
        assertFalse(oper.isActorFailed(null));

        OperationOutcome outcome = params.makeOutcome(null);

        // incorrect outcome
        outcome.setResult(OperationResult.SUCCESS);
        assertFalse(oper.isActorFailed(outcome));

        outcome.setResult(OperationResult.FAILURE_RETRIES);
        assertFalse(oper.isActorFailed(outcome));

        // correct outcome
        outcome.setResult(OperationResult.FAILURE);

        // incorrect actor
        outcome.setActor(MY_SINK);
        assertFalse(oper.isActorFailed(outcome));
        outcome.setActor(null);
        assertFalse(oper.isActorFailed(outcome));
        outcome.setActor(ACTOR);

        // incorrect operation
        outcome.setOperation(MY_SINK);
        assertFalse(oper.isActorFailed(outcome));
        outcome.setOperation(null);
        assertFalse(oper.isActorFailed(outcome));
        outcome.setOperation(OPERATION);

        // correct values
        assertTrue(oper.isActorFailed(outcome));
    }

    @Test
    public void testDoOperation() {
        /*
         * Use an operation that doesn't override doOperation().
         */
        OperationPartial oper2 = new OperationPartial(params, config, Collections.emptyList()) {};

        oper2.start();
        assertTrue(executor.runAll(MAX_REQUESTS));

        assertNotNull(opend);
        assertEquals(OperationResult.FAILURE_EXCEPTION, opend.getResult());
    }

    @Test
    public void testTimeout() throws Exception {

        // use a real executor
        params = params.toBuilder().executor(ForkJoinPool.commonPool()).build();

        // trigger timeout very quickly
        oper = new MyOper() {
            @Override
            protected long getTimeoutMs(Integer timeoutSec) {
                return 1;
            }

            @Override
            protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

                OperationOutcome outcome2 = params.makeOutcome(null);
                outcome2.setResult(OperationResult.SUCCESS);

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

        assertEquals(OperationResult.FAILURE_TIMEOUT, oper.start().get().getResult());
    }

    /**
     * Tests retry functions, when the count is set to zero and retries are exhausted.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_ZeroRetries_testStartOperationAttempt() {
        params = params.toBuilder().retry(0).build();

        // new params, thus need a new operation
        oper = new MyOper();

        oper.setMaxFailures(10);

        verifyRun("testSetRetryFlag_testRetryOnFailure_ZeroRetries", 1, 1, OperationResult.FAILURE);
    }

    /**
     * Tests retry functions, when the count is null and retries are exhausted.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_NullRetries() {
        params = params.toBuilder().retry(null).build();

        // new params, thus need a new operation
        oper = new MyOper();

        oper.setMaxFailures(10);

        verifyRun("testSetRetryFlag_testRetryOnFailure_NullRetries", 1, 1, OperationResult.FAILURE);
    }

    /**
     * Tests retry functions, when retries are exhausted.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_RetriesExhausted() {
        final int maxRetries = 3;
        params = params.toBuilder().retry(maxRetries).build();

        // new params, thus need a new operation
        oper = new MyOper();

        oper.setMaxFailures(10);

        verifyRun("testSetRetryFlag_testRetryOnFailure_RetriesExhausted", maxRetries + 1, maxRetries + 1,
                        OperationResult.FAILURE_RETRIES);
    }

    /**
     * Tests retry functions, when a success follows some retries.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_SuccessAfterRetries() {
        params = params.toBuilder().retry(10).build();

        // new params, thus need a new operation
        oper = new MyOper();

        final int maxFailures = 3;
        oper.setMaxFailures(maxFailures);

        verifyRun("testSetRetryFlag_testRetryOnFailure_SuccessAfterRetries", maxFailures + 1, maxFailures + 1,
                        OperationResult.SUCCESS);
    }

    /**
     * Tests retry functions, when the outcome is {@code null}.
     */
    @Test
    public void testSetRetryFlag_testRetryOnFailure_NullOutcome() {

        // arrange to return null from doOperation()
        oper = new MyOper() {
            @Override
            protected OperationOutcome doOperation(int attempt, OperationOutcome outcome) {

                // update counters
                super.doOperation(attempt, outcome);
                return null;
            }
        };

        verifyRun("testSetRetryFlag_testRetryOnFailure_NullOutcome", 1, 1, OperationResult.FAILURE, noop());
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

        OperationOutcome outcome = params.makeOutcome(null);

        // wrong actor - should be false
        outcome.setActor(null);
        assertFalse(oper.isSameOperation(outcome));
        outcome.setActor(MY_SINK);
        assertFalse(oper.isSameOperation(outcome));
        outcome.setActor(ACTOR);

        // wrong operation - should be null
        outcome.setOperation(null);
        assertFalse(oper.isSameOperation(outcome));
        outcome.setOperation(MY_SINK);
        assertFalse(oper.isSameOperation(outcome));
        outcome.setOperation(OPERATION);

        assertTrue(oper.isSameOperation(outcome));
    }

    /**
     * Tests handleFailure() when the outcome is a success.
     */
    @Test
    public void testHandlePreprocessorFailureSuccess() {
        oper.setPreProc(CompletableFuture.completedFuture(makeSuccess()));
        verifyRun("testHandlePreprocessorFailureTrue", 1, 1, OperationResult.SUCCESS);
    }

    /**
     * Tests handleFailure() when the outcome is <i>not</i> a success.
     */
    @Test
    public void testHandlePreprocessorFailureFailed() throws Exception {
        oper.setPreProc(CompletableFuture.completedFuture(makeFailure()));
        verifyRun("testHandlePreprocessorFailureFalse", 1, 0, OperationResult.FAILURE_GUARD);
    }

    /**
     * Tests handleFailure() when the outcome is {@code null}.
     */
    @Test
    public void testHandlePreprocessorFailureNull() throws Exception {
        // arrange to return a null outcome from the preprocessor
        oper.setPreProc(CompletableFuture.completedFuture(null));
        verifyRun("testHandlePreprocessorFailureNull", 1, 0, OperationResult.FAILURE_GUARD);
    }

    @Test
    public void testFromException() {
        // arrange to generate an exception when operation runs
        oper.setGenException(true);

        verifyRun("testFromException", 1, 1, OperationResult.FAILURE_EXCEPTION);
    }

    /**
     * Tests fromException() when there is no exception.
     */
    @Test
    public void testFromExceptionNoExcept() {
        verifyRun("testFromExceptionNoExcept", 1, 1, OperationResult.SUCCESS);
    }

    /**
     * Tests both flavors of anyOf(), because one invokes the other.
     */
    @Test
    public void testAnyOf() throws Exception {
        // first task completes, others do not
        List<Supplier<CompletableFuture<OperationOutcome>>> tasks = new LinkedList<>();

        final OperationOutcome outcome = params.makeOutcome(null);

        tasks.add(() -> CompletableFuture.completedFuture(outcome));
        tasks.add(() -> new CompletableFuture<>());
        tasks.add(() -> null);
        tasks.add(() -> new CompletableFuture<>());

        CompletableFuture<OperationOutcome> result = oper.anyOf(tasks);
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isDone());
        assertSame(outcome, result.get());

        // repeat using array form
        @SuppressWarnings("unchecked")
        Supplier<CompletableFuture<OperationOutcome>>[] taskArray = new Supplier[tasks.size()];
        result = oper.anyOf(tasks.toArray(taskArray));
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isDone());
        assertSame(outcome, result.get());

        // second task completes, others do not
        tasks.clear();
        tasks.add(() -> new CompletableFuture<>());
        tasks.add(() -> CompletableFuture.completedFuture(outcome));
        tasks.add(() -> new CompletableFuture<>());

        result = oper.anyOf(tasks);
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isDone());
        assertSame(outcome, result.get());

        // third task completes, others do not
        tasks.clear();
        tasks.add(() -> new CompletableFuture<>());
        tasks.add(() -> new CompletableFuture<>());
        tasks.add(() -> CompletableFuture.completedFuture(outcome));

        result = oper.anyOf(tasks);
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isDone());
        assertSame(outcome, result.get());
    }

    /**
     * Tests both flavors of anyOf(), for edge cases: zero items, and one item.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testAnyOfEdge() throws Exception {
        List<Supplier<CompletableFuture<OperationOutcome>>> tasks = new LinkedList<>();

        // zero items: check both using a list and using an array
        assertNull(oper.anyOf(tasks));
        assertNull(oper.anyOf());

        // one item: : check both using a list and using an array
        CompletableFuture<OperationOutcome> future1 = new CompletableFuture<>();
        tasks.add(() -> future1);

        assertSame(future1, oper.anyOf(tasks));
        assertSame(future1, oper.anyOf(() -> future1));
    }

    @Test
    public void testAllOfArray() throws Exception {
        final OperationOutcome outcome = params.makeOutcome(null);

        CompletableFuture<OperationOutcome> future1 = new CompletableFuture<>();
        CompletableFuture<OperationOutcome> future2 = new CompletableFuture<>();
        CompletableFuture<OperationOutcome> future3 = new CompletableFuture<>();

        @SuppressWarnings("unchecked")
        CompletableFuture<OperationOutcome> result =
                        oper.allOf(() -> future1, () -> future2, () -> null, () -> future3);

        assertTrue(executor.runAll(MAX_REQUESTS));
        assertFalse(result.isDone());
        future1.complete(outcome);

        // complete 3 before 2
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertFalse(result.isDone());
        future3.complete(outcome);

        assertTrue(executor.runAll(MAX_REQUESTS));
        assertFalse(result.isDone());
        future2.complete(outcome);

        // all of them are now done
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isDone());
        assertSame(outcome, result.get());
    }

    @Test
    public void testAllOfList() throws Exception {
        final OperationOutcome outcome = params.makeOutcome(null);

        CompletableFuture<OperationOutcome> future1 = new CompletableFuture<>();
        CompletableFuture<OperationOutcome> future2 = new CompletableFuture<>();
        CompletableFuture<OperationOutcome> future3 = new CompletableFuture<>();

        List<Supplier<CompletableFuture<OperationOutcome>>> tasks = new LinkedList<>();
        tasks.add(() -> future1);
        tasks.add(() -> future2);
        tasks.add(() -> null);
        tasks.add(() -> future3);

        CompletableFuture<OperationOutcome> result = oper.allOf(tasks);

        assertTrue(executor.runAll(MAX_REQUESTS));
        assertFalse(result.isDone());
        future1.complete(outcome);

        // complete 3 before 2
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertFalse(result.isDone());
        future3.complete(outcome);

        assertTrue(executor.runAll(MAX_REQUESTS));
        assertFalse(result.isDone());
        future2.complete(outcome);

        // all of them are now done
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isDone());
        assertSame(outcome, result.get());
    }

    /**
     * Tests both flavors of allOf(), for edge cases: zero items, and one item.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testAllOfEdge() throws Exception {
        List<Supplier<CompletableFuture<OperationOutcome>>> tasks = new LinkedList<>();

        // zero items: check both using a list and using an array
        assertNull(oper.allOf(tasks));
        assertNull(oper.allOf());

        // one item: : check both using a list and using an array
        CompletableFuture<OperationOutcome> future1 = new CompletableFuture<>();
        tasks.add(() -> future1);

        assertSame(future1, oper.allOf(tasks));
        assertSame(future1, oper.allOf(() -> future1));
    }

    @Test
    public void testAttachFutures() throws Exception {
        List<Supplier<CompletableFuture<OperationOutcome>>> tasks = new LinkedList<>();

        // third task throws an exception during construction
        CompletableFuture<OperationOutcome> future1 = new CompletableFuture<>();
        CompletableFuture<OperationOutcome> future2 = new CompletableFuture<>();
        CompletableFuture<OperationOutcome> future3 = new CompletableFuture<>();
        tasks.add(() -> future1);
        tasks.add(() -> future2);
        tasks.add(() -> {
            throw new IllegalStateException(EXPECTED_EXCEPTION);
        });
        tasks.add(() -> future3);

        assertThatIllegalStateException().isThrownBy(() -> oper.anyOf(tasks)).withMessage(EXPECTED_EXCEPTION);

        // should have canceled the first two, but not the last
        assertTrue(future1.isCancelled());
        assertTrue(future2.isCancelled());
        assertFalse(future3.isCancelled());
    }

    @Test
    public void testCombineOutcomes() throws Exception {
        // only one outcome
        verifyOutcomes(0, OperationResult.SUCCESS);
        verifyOutcomes(0, OperationResult.FAILURE_EXCEPTION);

        // maximum is in different positions
        verifyOutcomes(0, OperationResult.FAILURE, OperationResult.SUCCESS, OperationResult.FAILURE_GUARD);
        verifyOutcomes(1, OperationResult.SUCCESS, OperationResult.FAILURE, OperationResult.FAILURE_GUARD);
        verifyOutcomes(2, OperationResult.SUCCESS, OperationResult.FAILURE_GUARD, OperationResult.FAILURE);

        // null outcome - takes precedence over a success
        List<Supplier<CompletableFuture<OperationOutcome>>> tasks = new LinkedList<>();
        tasks.add(() -> CompletableFuture.completedFuture(params.makeOutcome(null)));
        tasks.add(() -> CompletableFuture.completedFuture(null));
        tasks.add(() -> CompletableFuture.completedFuture(params.makeOutcome(null)));
        CompletableFuture<OperationOutcome> result = oper.allOf(tasks);

        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isDone());
        assertNull(result.get());

        // one throws an exception during execution
        IllegalStateException except = new IllegalStateException(EXPECTED_EXCEPTION);

        tasks.clear();
        tasks.add(() -> CompletableFuture.completedFuture(params.makeOutcome(null)));
        tasks.add(() -> CompletableFuture.failedFuture(except));
        tasks.add(() -> CompletableFuture.completedFuture(params.makeOutcome(null)));
        result = oper.allOf(tasks);

        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isCompletedExceptionally());
        result.whenComplete((unused, thrown) -> assertSame(except, thrown));
    }

    /**
     * Tests both flavors of sequence(), because one invokes the other.
     */
    @Test
    public void testSequence() throws Exception {
        final OperationOutcome outcome = params.makeOutcome(null);

        List<Supplier<CompletableFuture<OperationOutcome>>> tasks = new LinkedList<>();
        tasks.add(() -> CompletableFuture.completedFuture(outcome));
        tasks.add(() -> null);
        tasks.add(() -> CompletableFuture.completedFuture(outcome));
        tasks.add(() -> CompletableFuture.completedFuture(outcome));

        CompletableFuture<OperationOutcome> result = oper.sequence(tasks);
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isDone());
        assertSame(outcome, result.get());

        // repeat using array form
        @SuppressWarnings("unchecked")
        Supplier<CompletableFuture<OperationOutcome>>[] taskArray = new Supplier[tasks.size()];
        result = oper.sequence(tasks.toArray(taskArray));
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isDone());
        assertSame(outcome, result.get());

        // second task fails, third should not run
        OperationOutcome failure = params.makeOutcome(null);
        failure.setResult(OperationResult.FAILURE);
        tasks.clear();
        tasks.add(() -> CompletableFuture.completedFuture(outcome));
        tasks.add(() -> CompletableFuture.completedFuture(failure));
        tasks.add(() -> CompletableFuture.completedFuture(outcome));

        result = oper.sequence(tasks);
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isDone());
        assertSame(failure, result.get());
    }

    /**
     * Tests both flavors of sequence(), for edge cases: zero items, and one item.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testSequenceEdge() throws Exception {
        List<Supplier<CompletableFuture<OperationOutcome>>> tasks = new LinkedList<>();

        // zero items: check both using a list and using an array
        assertNull(oper.sequence(tasks));
        assertNull(oper.sequence());

        // one item: : check both using a list and using an array
        CompletableFuture<OperationOutcome> future1 = new CompletableFuture<>();
        tasks.add(() -> future1);

        assertSame(future1, oper.sequence(tasks));
        assertSame(future1, oper.sequence(() -> future1));
    }

    private void verifyOutcomes(int expected, OperationResult... results) throws Exception {
        List<Supplier<CompletableFuture<OperationOutcome>>> tasks = new LinkedList<>();

        OperationOutcome expectedOutcome = null;

        for (int count = 0; count < results.length; ++count) {
            OperationOutcome outcome = params.makeOutcome(null);
            outcome.setResult(results[count]);
            tasks.add(() -> CompletableFuture.completedFuture(outcome));

            if (count == expected) {
                expectedOutcome = outcome;
            }
        }

        CompletableFuture<OperationOutcome> result = oper.allOf(tasks);

        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(result.isDone());
        assertSame(expectedOutcome, result.get());
    }

    @Test
    public void testDetmPriority() throws CoderException {
        assertEquals(1, oper.detmPriority(null));

        OperationOutcome outcome = params.makeOutcome(null);

        Map<OperationResult, Integer> map = Map.of(OperationResult.SUCCESS, 0, OperationResult.FAILURE_GUARD, 2,
                OperationResult.FAILURE_RETRIES, 3, OperationResult.FAILURE, 4, OperationResult.FAILURE_TIMEOUT, 5,
                OperationResult.FAILURE_EXCEPTION, 6);

        for (Entry<OperationResult, Integer> ent : map.entrySet()) {
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

        // new params, thus need a new operation
        oper = new MyOper();

        future.set(oper.start());
        assertTrue(executor.runAll(MAX_REQUESTS));

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

        // new params, thus need a new operation
        oper = new MyOper();

        future.set(oper.start());
        assertTrue(executor.runAll(MAX_REQUESTS));

        // should not have been set
        assertNull(opend);
        assertEquals(0, numEnd);
    }

    @Test
    public void testSetOutcomeControlLoopOperationOutcomeThrowable() {
        final CompletionException timex = new CompletionException(new TimeoutException(EXPECTED_EXCEPTION));

        OperationOutcome outcome;

        outcome = new OperationOutcome();
        oper.setOutcome(outcome, timex);
        assertEquals(ControlLoopOperation.FAILED_MSG, outcome.getMessage());
        assertEquals(OperationResult.FAILURE_TIMEOUT, outcome.getResult());

        outcome = new OperationOutcome();
        oper.setOutcome(outcome, new IllegalStateException(EXPECTED_EXCEPTION));
        assertEquals(ControlLoopOperation.FAILED_MSG, outcome.getMessage());
        assertEquals(OperationResult.FAILURE_EXCEPTION, outcome.getResult());
    }

    @Test
    public void testSetOutcomeControlLoopOperationOutcomePolicyResult() {
        OperationOutcome outcome;

        outcome = new OperationOutcome();
        oper.setOutcome(outcome, OperationResult.SUCCESS);
        assertEquals(ControlLoopOperation.SUCCESS_MSG, outcome.getMessage());
        assertEquals(OperationResult.SUCCESS, outcome.getResult());

        oper.setOutcome(outcome, OperationResult.SUCCESS);
        assertEquals(ControlLoopOperation.SUCCESS_MSG, outcome.getMessage());
        assertEquals(OperationResult.SUCCESS, outcome.getResult());

        for (OperationResult result : FAILURE_RESULTS) {
            outcome = new OperationOutcome();
            oper.setOutcome(outcome, result);
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
    public void testLogMessage() {
        final String infraStr = SINK_INFRA.toString();

        // log structured data
        appender.clearExtractions();
        oper.logMessage(EventType.OUT, SINK_INFRA, MY_SINK, new MyData());
        List<String> output = appender.getExtracted();
        assertEquals(1, output.size());

        assertThat(output.get(0)).contains(infraStr).contains(MY_SINK).contains("OUT")
                        .contains("{\n  \"text\": \"my-text\"\n}");

        // repeat with a response
        appender.clearExtractions();
        oper.logMessage(EventType.IN, SOURCE_INFRA, MY_SOURCE, new MyData());
        output = appender.getExtracted();
        assertEquals(1, output.size());

        assertThat(output.get(0)).contains(SOURCE_INFRA.toString()).contains(MY_SOURCE).contains("IN")
                        .contains("{\n  \"text\": \"my-text\"\n}");

        // log a plain string
        appender.clearExtractions();
        oper.logMessage(EventType.OUT, SINK_INFRA, MY_SINK, TEXT);
        output = appender.getExtracted();
        assertEquals(1, output.size());
        assertThat(output.get(0)).contains(infraStr).contains(MY_SINK).contains(TEXT);

        // log a null request
        appender.clearExtractions();
        oper.logMessage(EventType.OUT, SINK_INFRA, MY_SINK, null);
        output = appender.getExtracted();
        assertEquals(1, output.size());

        assertThat(output.get(0)).contains(infraStr).contains(MY_SINK).contains("null");

        // generate exception from coder
        setOperCoderException();

        appender.clearExtractions();
        oper.logMessage(EventType.OUT, SINK_INFRA, MY_SINK, new MyData());
        output = appender.getExtracted();
        assertEquals(2, output.size());
        assertThat(output.get(0)).contains("cannot pretty-print request");
        assertThat(output.get(1)).contains(infraStr).contains(MY_SINK);

        // repeat with a response
        appender.clearExtractions();
        oper.logMessage(EventType.IN, SOURCE_INFRA, MY_SOURCE, new MyData());
        output = appender.getExtracted();
        assertEquals(2, output.size());
        assertThat(output.get(0)).contains("cannot pretty-print response");
        assertThat(output.get(1)).contains(MY_SOURCE);
    }

    @Test
    public void testGetRetry() {
        assertEquals(0, oper.getRetry(null));
        assertEquals(10, oper.getRetry(10));
    }

    @Test
    public void testGetRetryWait() {
        // need an operator that doesn't override the retry time
        OperationPartial oper2 = new OperationPartial(params, config, Collections.emptyList()) {};
        assertEquals(OperationPartial.DEFAULT_RETRY_WAIT_MS, oper2.getRetryWaitMs());
    }

    @Test
    public void testGetTargetEntity() {
        // get it from the params
        assertEquals(MY_TARGET_ENTITY, oper.getTargetEntity());

        // now get it from the properties
        oper.setProperty(OperationProperties.AAI_TARGET_ENTITY, "entityX");
        assertEquals("entityX", oper.getTargetEntity());
    }

    @Test
    public void testGetTimeOutMs() {
        assertEquals(TIMEOUT * 1000, oper.getTimeoutMs(params.getTimeoutSec()));

        params = params.toBuilder().timeoutSec(null).build();

        // new params, thus need a new operation
        oper = new MyOper();

        assertEquals(0, oper.getTimeoutMs(params.getTimeoutSec()));
    }

    private void starter(OperationOutcome oper) {
        ++numStart;
        tstart = oper.getStart();
        opstart = oper;
        starts.add(oper);
    }

    private void completer(OperationOutcome oper) {
        ++numEnd;
        opend = oper;
        ends.add(oper);
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
        OperationOutcome outcome = params.makeOutcome(null);
        outcome.setResult(OperationResult.SUCCESS);

        return outcome;
    }

    private OperationOutcome makeFailure() {
        OperationOutcome outcome = params.makeOutcome(null);
        outcome.setResult(OperationResult.FAILURE);

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
            OperationResult expectedResult) {

        verifyRun(testName, expectedCallbacks, expectedOperations, expectedResult, noop());
    }

    /**
     * Verifies a run.
     *
     * @param testName test name
     * @param expectedCallbacks number of callbacks expected
     * @param expectedOperations number of operation invocations expected
     * @param expectedResult expected outcome
     * @param manipulator function to modify the future returned by
     *        {@link OperationPartial#start(ControlLoopOperationParams)} before the tasks
     *        in the executor are run
     */
    private void verifyRun(String testName, int expectedCallbacks, int expectedOperations,
            OperationResult expectedResult, Consumer<CompletableFuture<OperationOutcome>> manipulator) {

        tstart = null;
        opstart = null;
        opend = null;
        starts.clear();
        ends.clear();

        CompletableFuture<OperationOutcome> future = oper.start();

        manipulator.accept(future);

        assertTrue(testName, executor.runAll(MAX_REQUESTS));

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
                assertEquals(testName, opend, future.get());

                // "start" is never final
                for (OperationOutcome outcome : starts) {
                    assertFalse(testName, outcome.isFinalOutcome());
                }

                // only the last "complete" is final
                assertTrue(testName, ends.removeLast().isFinalOutcome());

                for (OperationOutcome outcome : ends) {
                    assertFalse(outcome.isFinalOutcome());
                }

            } catch (InterruptedException | ExecutionException e) {
                throw new IllegalStateException(e);
            }

            if (expectedOperations > 0) {
                assertNotNull(testName, oper.getSubRequestId());
                assertEquals(testName + " op start", oper.getSubRequestId(), opstart.getSubRequestId());
                assertEquals(testName + " op end", oper.getSubRequestId(), opend.getSubRequestId());
            }
        }

        assertEquals(testName, expectedOperations, oper.getCount());
    }

    /**
     * Creates a new {@link #oper} whose coder will throw an exception.
     */
    private void setOperCoderException() {
        oper = new MyOper() {
            @Override
            protected Coder getCoder() {
                return new StandardCoder() {
                    @Override
                    public String encode(Object object, boolean pretty) throws CoderException {
                        throw new CoderException(EXPECTED_EXCEPTION);
                    }
                };
            }
        };
    }


    @Getter
    public static class MyData {
        private String text = TEXT;
    }


    private class MyOper extends OperationPartial {
        @Getter
        private int count = 0;

        @Setter
        private boolean genException;
        @Setter
        private int maxFailures = 0;
        @Setter
        private CompletableFuture<OperationOutcome> preProc;


        public MyOper() {
            super(OperationPartialTest.this.params, config, PROP_NAMES);
        }

        @Override
        protected OperationOutcome doOperation(int attempt, OperationOutcome operation) {
            ++count;
            if (genException) {
                throw new IllegalStateException(EXPECTED_EXCEPTION);
            }

            operation.setSubRequestId(String.valueOf(attempt));

            if (count > maxFailures) {
                operation.setResult(OperationResult.SUCCESS);
            } else {
                operation.setResult(OperationResult.FAILURE);
            }

            return operation;
        }

        @Override
        protected long getRetryWaitMs() {
            /*
             * Sleep timers run in the background, but we want to control things via the
             * "executor", thus we avoid sleep timers altogether by simply returning 0.
             */
            return 0L;
        }

        @Override
        protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
            return (preProc != null ? preProc : super.startPreprocessorAsync());
        }
    }
}
