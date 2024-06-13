/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.common.utils.time.PseudoExecutor;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;

@ExtendWith(MockitoExtension.class)
class BidirectionalTopicOperationTest {
    private static final CommInfrastructure SINK_INFRA = CommInfrastructure.NOOP;
    private static final IllegalStateException EXPECTED_EXCEPTION = new IllegalStateException("expected exception");
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";
    private static final String REQ_ID = "my-request-id";
    private static final String TEXT = "some text";
    private static final int TIMEOUT_SEC = 10;
    private static final long TIMEOUT_MS = 1000 * TIMEOUT_SEC;
    private static final int MAX_REQUESTS = 100;

    private static final StandardCoder coder = new StandardCoder();

    @Mock
    private BidirectionalTopicConfig config;
    @Mock
    private BidirectionalTopicHandler handler;
    @Mock
    private Forwarder forwarder;

    @Captor
    private ArgumentCaptor<BiConsumer<String, StandardCoderObject>> listenerCaptor;

    private ControlLoopOperationParams params;
    private OperationOutcome outcome;
    private StandardCoderObject stdResponse;
    private MyResponse response;
    private String responseText;
    private PseudoExecutor executor;
    private int ntimes;
    private BidirectionalTopicOperation<MyRequest, MyResponse> oper;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() throws CoderException {
        Mockito.lenient().when(config.getTopicHandler()).thenReturn(handler);
        Mockito.lenient().when(config.getForwarder()).thenReturn(forwarder);
        Mockito.lenient().when(config.getTimeoutMs()).thenReturn(TIMEOUT_MS);

        Mockito.lenient().when(handler.send(any())).thenReturn(true);
        Mockito.lenient().when(handler.getSinkTopicCommInfrastructure()).thenReturn(SINK_INFRA);

        executor = new PseudoExecutor();

        params = ControlLoopOperationParams.builder().actor(ACTOR).operation(OPERATION).executor(executor).build();
        outcome = params.makeOutcome();

        response = new MyResponse();
        response.setRequestId(REQ_ID);
        responseText = coder.encode(response);
        stdResponse = coder.decode(responseText, StandardCoderObject.class);

        ntimes = 1;

        oper = new MyOperation(params, config);
    }

    @Test
    void testConstructor_testGetTopicHandler_testGetForwarder_testGetTopicParams() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertSame(handler, oper.getTopicHandler());
        assertSame(forwarder, oper.getForwarder());
        assertEquals(TIMEOUT_MS, oper.getTimeoutMs());
        assertSame(MyResponse.class, oper.getResponseClass());
    }

    @Test
    void testStartOperationAsync() throws Exception {
        // tell it to expect three responses
        ntimes = 3;

        CompletableFuture<OperationOutcome> future = oper.startOperationAsync(1, outcome);
        assertFalse(future.isDone());

        verify(forwarder).register(eq(Arrays.asList(REQ_ID)), listenerCaptor.capture());

        verify(forwarder, never()).unregister(any(), any());

        verify(handler).send(any());

        // provide first response
        listenerCaptor.getValue().accept(responseText, stdResponse);
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertFalse(future.isDone());

        // provide second response
        listenerCaptor.getValue().accept(responseText, stdResponse);
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertFalse(future.isDone());

        // provide final response
        listenerCaptor.getValue().accept(responseText, stdResponse);
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(future.isDone());

        assertSame(outcome, future.get());
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals(response, outcome.getResponse());

        verify(forwarder).unregister(Arrays.asList(REQ_ID), listenerCaptor.getValue());
    }

    /**
     * Tests startOperationAsync() when processResponse() throws an exception.
     */
    @Test
    void testStartOperationAsyncProcException() throws Exception {
        oper = new MyOperation(params, config) {
            @Override
            protected OperationOutcome processResponse(OperationOutcome outcome, String rawResponse,
                                                       StandardCoderObject scoResponse) {
                throw EXPECTED_EXCEPTION;
            }
        };

        CompletableFuture<OperationOutcome> future = oper.startOperationAsync(1, outcome);
        assertFalse(future.isDone());

        verify(forwarder).register(eq(Arrays.asList(REQ_ID)), listenerCaptor.capture());

        verify(forwarder, never()).unregister(any(), any());

        // provide a response
        listenerCaptor.getValue().accept(responseText, stdResponse);
        assertTrue(executor.runAll(MAX_REQUESTS));
        assertTrue(future.isCompletedExceptionally());

        verify(forwarder).unregister(Arrays.asList(REQ_ID), listenerCaptor.getValue());
    }

    /**
     * Tests startOperationAsync() when the publisher throws an exception.
     */
    @Test
     void testStartOperationAsyncPubException() throws Exception {
        // indicate that nothing was published
        when(handler.send(any())).thenReturn(false);

        assertThatIllegalStateException().isThrownBy(() -> oper.startOperationAsync(1, outcome));

        verify(forwarder).register(eq(Arrays.asList(REQ_ID)), listenerCaptor.capture());

        // must still unregister
        verify(forwarder).unregister(Arrays.asList(REQ_ID), listenerCaptor.getValue());
    }

    @Test
     void testGetTimeoutMsInteger() {
        // use default
        assertEquals(TIMEOUT_MS, oper.getTimeoutMs(null));
        assertEquals(TIMEOUT_MS, oper.getTimeoutMs(0));

        // use provided value
        assertEquals(5000, oper.getTimeoutMs(5));
    }

    @Test
     void testPublishRequest() {
        assertThatCode(() -> oper.publishRequest(new MyRequest())).doesNotThrowAnyException();
    }

    /**
     * Tests publishRequest() when nothing is published.
     */
    @Test
     void testPublishRequestUnpublished() {
        when(handler.send(any())).thenReturn(false);
        assertThatIllegalStateException().isThrownBy(() -> oper.publishRequest(new MyRequest()));
    }

    /**
     * Tests publishRequest() when the request type is a String.
     */
    @Test
     void testPublishRequestString() {
        MyStringOperation oper2 = new MyStringOperation(params, config);
        assertThatCode(() -> oper2.publishRequest(TEXT)).doesNotThrowAnyException();
    }

    /**
     * Tests publishRequest() when the coder throws an exception.
     */
    @Test
     void testPublishRequestException() {
        setOperCoderException();
        assertThatIllegalArgumentException().isThrownBy(() -> oper.publishRequest(new MyRequest()));
    }

    /**
     * Tests processResponse() when it's a success and the response type is a String.
     */
    @Test
     void testProcessResponseSuccessString() {
        MyStringOperation oper2 = new MyStringOperation(params, config);

        assertSame(outcome, oper2.processResponse(outcome, TEXT, null));
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals(TEXT, outcome.getResponse());
    }

    /**
     * Tests processResponse() when it's a success and the response type is a
     * StandardCoderObject.
     */
    @Test
     void testProcessResponseSuccessSco() {
        MyScoOperation oper2 = new MyScoOperation(params, config);

        assertSame(outcome, oper2.processResponse(outcome, responseText, stdResponse));
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals(stdResponse, outcome.getResponse());
    }

    /**
     * Tests processResponse() when it's a failure.
     */
    @Test
     void testProcessResponseFailure() throws CoderException {
        // indicate error in the response
        MyResponse resp = new MyResponse();
        resp.setOutput("error");

        responseText = coder.encode(resp);
        stdResponse = coder.decode(responseText, StandardCoderObject.class);

        assertSame(outcome, oper.processResponse(outcome, responseText, stdResponse));
        assertEquals(OperationResult.FAILURE, outcome.getResult());
        assertEquals(resp, outcome.getResponse());
    }

    /**
     * Tests processResponse() when the decoder succeeds.
     */
    @Test
     void testProcessResponseDecodeOk() throws CoderException {
        assertSame(outcome, oper.processResponse(outcome, responseText, stdResponse));
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals(response, outcome.getResponse());
    }

    /**
     * Tests processResponse() when the decoder throws an exception.
     */
    @Test
     void testProcessResponseDecodeExcept() throws CoderException {
        assertThatIllegalArgumentException().isThrownBy(
            () -> oper.processResponse(outcome, "{invalid json", stdResponse));
    }

    @Test
     void testPostProcessResponse() {
        assertThatCode(() -> oper.postProcessResponse(outcome, null, null)).doesNotThrowAnyException();
    }

    @Test
     void testGetCoder() {
        assertNotNull(oper.getCoder());
    }

    /**
     * Creates a new {@link #oper} whose coder will throw an exception.
     */
    private void setOperCoderException() {
        oper = new MyOperation(params, config) {
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
    @Setter
    static class MyRequest {
        private String theRequestId = REQ_ID;
        private String input;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    static class MyResponse {
        private String requestId;
        private String output;
    }


    private class MyStringOperation extends BidirectionalTopicOperation<String, String> {

        MyStringOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
            super(params, config, String.class, Collections.emptyList());
        }

        @Override
        protected String makeRequest(int attempt) {
            return TEXT;
        }

        @Override
        protected List<String> getExpectedKeyValues(int attempt, String request) {
            return Arrays.asList(REQ_ID);
        }

        @Override
        protected Status detmStatus(String rawResponse, String response) {
            return (response != null ? Status.SUCCESS : Status.FAILURE);
        }
    }


    private class MyScoOperation extends BidirectionalTopicOperation<MyRequest, StandardCoderObject> {
        MyScoOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
            super(params, config, StandardCoderObject.class, Collections.emptyList());
        }

        @Override
        protected MyRequest makeRequest(int attempt) {
            return new MyRequest();
        }

        @Override
        protected List<String> getExpectedKeyValues(int attempt, MyRequest request) {
            return Arrays.asList(REQ_ID);
        }

        @Override
        protected Status detmStatus(String rawResponse, StandardCoderObject response) {
            return (response.getString("output") == null ? Status.SUCCESS : Status.FAILURE);
        }
    }


    private class MyOperation extends BidirectionalTopicOperation<MyRequest, MyResponse> {
        MyOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
            super(params, config, MyResponse.class, Collections.emptyList());
        }

        @Override
        protected MyRequest makeRequest(int attempt) {
            return new MyRequest();
        }

        @Override
        protected List<String> getExpectedKeyValues(int attempt, MyRequest request) {
            return Arrays.asList(REQ_ID);
        }

        @Override
        protected Status detmStatus(String rawResponse, MyResponse response) {
            if (--ntimes <= 0) {
                return (response.getOutput() == null ? Status.SUCCESS : Status.FAILURE);
            }

            return Status.STILL_WAITING;
        }
    }
}
