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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

public class BidirectionalTopicOperationTest {
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
    @Before
    public void setUp() throws CoderException {
        MockitoAnnotations.initMocks(this);

        when(config.getTopicHandler()).thenReturn(handler);
        when(config.getForwarder()).thenReturn(forwarder);
        when(config.getTimeoutMs()).thenReturn(TIMEOUT_MS);

        when(handler.send(any())).thenReturn(true);
        when(handler.getSinkTopicCommInfrastructure()).thenReturn(SINK_INFRA);

        executor = new PseudoExecutor();

        params = ControlLoopOperationParams.builder().actor(ACTOR).operation(OPERATION).executor(executor).build();
        outcome = params.makeOutcome(null);

        response = new MyResponse();
        response.setRequestId(REQ_ID);
        responseText = coder.encode(response);
        stdResponse = coder.decode(responseText, StandardCoderObject.class);

        ntimes = 1;

        oper = new MyOperation();
    }

    @Test
    public void testConstructor_testGetTopicHandler_testGetForwarder_testGetTopicParams() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertSame(handler, oper.getTopicHandler());
        assertSame(forwarder, oper.getForwarder());
        assertEquals(TIMEOUT_MS, oper.getTimeoutMs());
        assertSame(MyResponse.class, oper.getResponseClass());
    }

    @Test
    public void testStartOperationAsync() throws Exception {

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

        verify(forwarder).unregister(eq(Arrays.asList(REQ_ID)), eq(listenerCaptor.getValue()));
    }

    /**
     * Tests startOperationAsync() when processResponse() throws an exception.
     */
    @Test
    public void testStartOperationAsyncProcException() throws Exception {
        oper = new MyOperation() {
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

        verify(forwarder).unregister(eq(Arrays.asList(REQ_ID)), eq(listenerCaptor.getValue()));
    }

    /**
     * Tests startOperationAsync() when the publisher throws an exception.
     */
    @Test
    public void testStartOperationAsyncPubException() throws Exception {
        // indicate that nothing was published
        when(handler.send(any())).thenReturn(false);

        assertThatIllegalStateException().isThrownBy(() -> oper.startOperationAsync(1, outcome));

        verify(forwarder).register(eq(Arrays.asList(REQ_ID)), listenerCaptor.capture());

        // must still unregister
        verify(forwarder).unregister(eq(Arrays.asList(REQ_ID)), eq(listenerCaptor.getValue()));
    }

    @Test
    public void testGetTimeoutMsInteger() {
        // use default
        assertEquals(TIMEOUT_MS, oper.getTimeoutMs(null));
        assertEquals(TIMEOUT_MS, oper.getTimeoutMs(0));

        // use provided value
        assertEquals(5000, oper.getTimeoutMs(5));
    }

    @Test
    public void testPublishRequest() {
        assertThatCode(() -> oper.publishRequest(new MyRequest())).doesNotThrowAnyException();
    }

    /**
     * Tests publishRequest() when nothing is published.
     */
    @Test
    public void testPublishRequestUnpublished() {
        when(handler.send(any())).thenReturn(false);
        assertThatIllegalStateException().isThrownBy(() -> oper.publishRequest(new MyRequest()));
    }

    /**
     * Tests publishRequest() when the request type is a String.
     */
    @Test
    public void testPublishRequestString() {
        MyStringOperation oper2 = new MyStringOperation();
        assertThatCode(() -> oper2.publishRequest(TEXT)).doesNotThrowAnyException();
    }

    /**
     * Tests publishRequest() when the coder throws an exception.
     */
    @Test
    public void testPublishRequestException() {
        setOperCoderException();
        assertThatIllegalArgumentException().isThrownBy(() -> oper.publishRequest(new MyRequest()));
    }

    /**
     * Tests processResponse() when it's a success and the response type is a String.
     */
    @Test
    public void testProcessResponseSuccessString() {
        MyStringOperation oper2 = new MyStringOperation();

        assertSame(outcome, oper2.processResponse(outcome, TEXT, null));
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals(TEXT, outcome.getResponse());
    }

    /**
     * Tests processResponse() when it's a success and the response type is a
     * StandardCoderObject.
     */
    @Test
    public void testProcessResponseSuccessSco() {
        MyScoOperation oper2 = new MyScoOperation();

        assertSame(outcome, oper2.processResponse(outcome, responseText, stdResponse));
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals(stdResponse, outcome.getResponse());
    }

    /**
     * Tests processResponse() when it's a failure.
     */
    @Test
    public void testProcessResponseFailure() throws CoderException {
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
    public void testProcessResponseDecodeOk() throws CoderException {
        assertSame(outcome, oper.processResponse(outcome, responseText, stdResponse));
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals(response, outcome.getResponse());
    }

    /**
     * Tests processResponse() when the decoder throws an exception.
     */
    @Test
    public void testProcessResponseDecodeExcept() throws CoderException {
        // @formatter:off
        assertThatIllegalArgumentException().isThrownBy(
            () -> oper.processResponse(outcome, "{invalid json", stdResponse));
        // @formatter:on
    }

    @Test
    public void testPostProcessResponse() {
        assertThatCode(() -> oper.postProcessResponse(outcome, null, null)).doesNotThrowAnyException();
    }

    @Test
    public void testGetCoder() {
        assertNotNull(oper.getCoder());
    }

    /**
     * Creates a new {@link #oper} whose coder will throw an exception.
     */
    private void setOperCoderException() {
        oper = new MyOperation() {
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
    public static class MyRequest {
        private String theRequestId = REQ_ID;
        private String input;
    }

    @Getter
    @Setter
    @EqualsAndHashCode
    public static class MyResponse {
        private String requestId;
        private String output;
    }


    private class MyStringOperation extends BidirectionalTopicOperation<String, String> {

        public MyStringOperation() {
            super(BidirectionalTopicOperationTest.this.params, config, String.class, Collections.emptyList());
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
        public MyScoOperation() {
            super(BidirectionalTopicOperationTest.this.params, config, StandardCoderObject.class,
                            Collections.emptyList());
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
        public MyOperation() {
            super(BidirectionalTopicOperationTest.this.params, config, MyResponse.class, Collections.emptyList());
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
