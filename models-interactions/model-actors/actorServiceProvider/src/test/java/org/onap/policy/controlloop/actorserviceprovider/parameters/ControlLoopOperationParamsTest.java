/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Operator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams.ControlLoopOperationParamsBuilder;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class ControlLoopOperationParamsTest {
    private static final String NULL_MSG = "null";
    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";
    private static final Integer RETRY = 3;
    private static final Integer TIMEOUT = 100;
    private static final UUID REQ_ID = UUID.randomUUID();

    @Mock
    private Actor actor;

    @Mock
    private ActorService actorService;

    @Mock
    private Consumer<OperationOutcome> completer;

    @Mock
    private Executor executor;

    @Mock
    private CompletableFuture<OperationOutcome> operFuture;

    @Mock
    private Operator operator;

    @Mock
    private Operation operation;

    @Mock
    private Consumer<OperationOutcome> starter;

    private Map<String, Object> payload;

    private ControlLoopOperationParams params;
    private OperationOutcome outcome;


    /**
     * Initializes mocks and sets {@link #params} to a fully-loaded set of parameters.
     */
    @BeforeEach
     void setUp() {
        Mockito.lenient().when(actorService.getActor(ACTOR)).thenReturn(actor);
        Mockito.lenient().when(actor.getOperator(OPERATION)).thenReturn(operator);
        Mockito.lenient().when(operator.buildOperation(any())).thenReturn(operation);
        Mockito.lenient().when(operation.start()).thenReturn(operFuture);

        payload = new TreeMap<>();

        params = ControlLoopOperationParams.builder().actorService(actorService).completeCallback(completer)
                        .requestId(REQ_ID).executor(executor).actor(ACTOR).operation(OPERATION).payload(payload)
                        .retry(RETRY).timeoutSec(TIMEOUT)
                        .startCallback(starter).build();

        outcome = params.makeOutcome();
    }

    @Test
     void testStart() {
        assertThatIllegalArgumentException().isThrownBy(() -> params.toBuilder().requestId(null).build().start());

        assertSame(operFuture, params.start());
    }

    @Test
     void testBuild() {
        assertThatIllegalArgumentException().isThrownBy(() -> params.toBuilder().requestId(null).build().build());

        assertSame(operation, params.build());
    }

    @Test
     void testGetRequestId() {
        assertSame(REQ_ID, params.getRequestId());
    }

    @Test
     void testMakeOutcome() {
        assertEquals(ACTOR, outcome.getActor());
        assertEquals(OPERATION, outcome.getOperation());
        assertNull(outcome.getStart());
        assertNull(outcome.getEnd());
        assertNull(outcome.getSubRequestId());
        assertNotNull(outcome.getResult());
        assertNull(outcome.getMessage());
    }

    @Test
     void testCallbackStarted() {
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
     void testCallbackCompleted() {
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
     void testValidateFields() {
        testValidate("actor", NULL_MSG, bldr -> bldr.actor(null));
        testValidate("actorService", NULL_MSG, bldr -> bldr.actorService(null));
        testValidate("executor", NULL_MSG, bldr -> bldr.executor(null));
        testValidate("operation", NULL_MSG, bldr -> bldr.operation(null));
        testValidate("requestId", NULL_MSG, bldr -> bldr.requestId(null));

        // has no target entity
        BeanValidationResult result = params.toBuilder().build().validate();
        assertTrue(result.isValid());

        // check edge cases
        assertTrue(params.toBuilder().build().validate().isValid());

        // these can be null
        assertTrue(params.toBuilder().payload(null).retry(null).timeoutSec(null).startCallback(null)
                        .completeCallback(null).build().validate().isValid());

        // test with minimal fields
        assertTrue(ControlLoopOperationParams.builder().actorService(actorService).requestId(REQ_ID).actor(ACTOR)
                        .operation(OPERATION).build().validate().isValid());
    }

    private void testValidate(String fieldName, String expected,
                    Function<ControlLoopOperationParamsBuilder, ControlLoopOperationParamsBuilder> makeInvalid) {

        // original params should be valid
        BeanValidationResult result = params.validate();
        assertTrue(result.isValid(), fieldName);

        // make invalid params
        result = makeInvalid.apply(params.toBuilder()).build().validate();
        assertFalse(result.isValid(), fieldName);
        assertThat(result.getResult()).contains(fieldName).contains(expected);
    }

    @Test
     void testBuilder_testToBuilder() {
        assertEquals(params, params.toBuilder().build());
    }

    @Test
     void testGetActor() {
        assertSame(ACTOR, params.getActor());
    }

    @Test
     void testGetActorService() {
        assertSame(actorService, params.getActorService());
    }

    @Test
     void testGetExecutor() {
        assertSame(executor, params.getExecutor());

        // should use default when unspecified
        assertSame(ForkJoinPool.commonPool(), ControlLoopOperationParams.builder().build().getExecutor());
    }

    @Test
     void testGetOperation() {
        assertSame(OPERATION, params.getOperation());
    }

    @Test
     void testGetPayload() {
        assertSame(payload, params.getPayload());

        // should be null when unspecified
        assertNull(ControlLoopOperationParams.builder().build().getPayload());
    }

    @Test
     void testGetRetry() {
        assertSame(RETRY, params.getRetry());

        // should be null when unspecified
        assertNull(ControlLoopOperationParams.builder().build().getRetry());
    }

    @Test
     void testGetTimeoutSec() {
        assertSame(TIMEOUT, params.getTimeoutSec());

        // should be 300 when unspecified
        assertEquals(Integer.valueOf(300), ControlLoopOperationParams.builder().build().getTimeoutSec());

        // null should be ok too
        assertNull(ControlLoopOperationParams.builder().timeoutSec(null).build().getTimeoutSec());
    }

    @Test
     void testGetStartCallback() {
        assertSame(starter, params.getStartCallback());
    }

    @Test
     void testGetCompleteCallback() {
        assertSame(completer, params.getCompleteCallback());
    }
}
