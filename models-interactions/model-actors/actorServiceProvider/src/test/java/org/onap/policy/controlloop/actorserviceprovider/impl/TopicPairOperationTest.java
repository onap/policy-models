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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
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

import ch.qos.logback.classic.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.PropertyUtils.TriConsumer;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.TopicPairParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;
import org.onap.policy.controlloop.actorserviceprovider.topic.TopicPair;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.LoggerFactory;

public class TopicPairOperationTest {
    private static final List<CommInfrastructure> INFRA_LIST =
                    Arrays.asList(CommInfrastructure.NOOP, CommInfrastructure.UEB);
    private static final IllegalStateException EXPECTED_EXCEPTION = new IllegalStateException("expected exception");
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";
    private static final String REQ_ID = "my-request-id";
    private static final String MY_SOURCE = "my-source";
    private static final String MY_TARGET = "my-target";
    private static final String TEXT = "some text";
    private static final int TIMEOUT_SEC = 10;
    private static final long TIMEOUT_MS = 1000 * TIMEOUT_SEC;

    private static final StandardCoder coder = new StandardCoder();

    /**
     * Used to attach an appender to the class' logger.
     */
    private static final Logger logger = (Logger) LoggerFactory.getLogger(TopicPairOperation.class);
    private static final ExtractAppender appender = new ExtractAppender();

    @Mock
    private TopicPairOperator operator;
    @Mock
    private TopicPair pair;
    @Mock
    private Forwarder forwarder;

    @Captor
    private ArgumentCaptor<TriConsumer<CommInfrastructure, String, StandardCoderObject>> listenerCaptor;

    private ControlLoopOperationParams params;
    private TopicPairParams topicParams;
    private OperationOutcome outcome;
    private StandardCoderObject stdResponse;
    private String responseText;
    private MyExec executor;
    private TopicPairOperation<MyRequest, MyResponse> oper;

    /**
     * Attaches the appender to the logger.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        /**
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
     * Sets up.
     */
    @Before
    public void setUp() throws CoderException {
        MockitoAnnotations.initMocks(this);

        appender.clearExtractions();

        topicParams = TopicPairParams.builder().source(MY_SOURCE).target(MY_TARGET).timeoutSec(TIMEOUT_SEC).build();

        when(operator.getActorName()).thenReturn(ACTOR);
        when(operator.getName()).thenReturn(OPERATION);
        when(operator.getTopicPair()).thenReturn(pair);
        when(operator.getForwarder()).thenReturn(forwarder);
        when(operator.getParams()).thenReturn(topicParams);
        when(operator.isAlive()).thenReturn(true);

        when(pair.publish(any())).thenReturn(INFRA_LIST);

        executor = new MyExec(100);

        params = ControlLoopOperationParams.builder().actor(ACTOR).operation(OPERATION).executor(executor).build();
        outcome = params.makeOutcome();

        responseText = coder.encode(new MyResponse());
        stdResponse = coder.decode(responseText, StandardCoderObject.class);

        oper = new MyOperation();
    }

    @Test
    public void testTopicPairOperation_testGetTopicPair_testGetForwarder_testGetPairParams() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertSame(pair, oper.getTopicPair());
        assertSame(forwarder, oper.getForwarder());
        assertSame(topicParams, oper.getPairParams());
        assertEquals(TIMEOUT_MS, oper.getTimeoutMs());
        assertSame(MyResponse.class, oper.getResponseClass());
    }

    @Test
    public void testStartOperationAsync() throws Exception {
        CompletableFuture<OperationOutcome> future = oper.startOperationAsync(1, outcome);
        assertFalse(future.isDone());

        verify(forwarder).register(eq(Arrays.asList(REQ_ID)), listenerCaptor.capture());

        verify(forwarder, never()).unregister(any(), any());

        verify(pair).publish(any());

        // provide the response
        listenerCaptor.getValue().accept(CommInfrastructure.NOOP, responseText, stdResponse);

        // run the tasks
        assertTrue(executor.runAll());

        assertTrue(future.isDone());

        assertSame(outcome, future.get(5, TimeUnit.SECONDS));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());

        verify(forwarder).unregister(eq(Arrays.asList(REQ_ID)), eq(listenerCaptor.getValue()));
    }

    /**
     * Tests startOperationAsync() when nothing gets published.
     */
    @Test
    public void testStartOperationAsyncUnpublished() throws Exception {
        // indicate that nothing was published
        when(pair.publish(any())).thenReturn(Arrays.asList());

        CompletableFuture<OperationOutcome> future = oper.startOperationAsync(1, outcome);
        assertTrue(future.isDone());

        verify(forwarder).register(eq(Arrays.asList(REQ_ID)), listenerCaptor.capture());

        assertSame(outcome, future.get(5, TimeUnit.SECONDS));
        assertEquals(PolicyResult.FAILURE, outcome.getResult());

        // must still unregister
        verify(forwarder).unregister(eq(Arrays.asList(REQ_ID)), eq(listenerCaptor.getValue()));
    }

    /**
     * Tests startOperationAsync() when the coder throws an exception.
     */
    @Test
    public void testStartOperationAsyncException() throws Exception {
        setOperCoderException();

        // indicate that nothing was published
        when(pair.publish(any())).thenReturn(Arrays.asList());

        assertThatIllegalArgumentException().isThrownBy(() -> oper.startOperationAsync(1, outcome));

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
        oper.publishRequest(new MyRequest());
        assertEquals(INFRA_LIST.size(), appender.getExtracted().size());
    }

    /**
     * Tests publishRequest() when the request type is a String.
     */
    @Test
    public void testPublishRequestString() {
        MyStringOperation oper2 = new MyStringOperation();
        assertTrue(oper2.publishRequest(TEXT));
        assertEquals(INFRA_LIST.size(), appender.getExtracted().size());
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

        assertSame(outcome, oper2.processResponse(CommInfrastructure.NOOP, outcome, TEXT, null));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests processResponse() when it's a success and the response type is a
     * StandardCoderObject.
     */
    @Test
    public void testProcessResponseSuccessSco() {
        MyScoOperation oper2 = new MyScoOperation();

        assertSame(outcome, oper2.processResponse(CommInfrastructure.NOOP, outcome, responseText, stdResponse));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
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

        assertSame(outcome, oper.processResponse(CommInfrastructure.NOOP, outcome, responseText, stdResponse));
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
    }

    /**
     * Tests processResponse() when the decoder succeeds.
     */
    @Test
    public void testProcessResponseDecodeOk() throws CoderException {
        assertSame(outcome, oper.processResponse(CommInfrastructure.NOOP, outcome, responseText, stdResponse));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests processResponse() when the decoder throws an exception.
     */
    @Test
    public void testProcessResponseDecodeExcept() throws CoderException {
        assertSame(outcome, oper.processResponse(CommInfrastructure.NOOP, outcome, "{invalid json", stdResponse));
        assertEquals(PolicyResult.FAILURE_EXCEPTION, outcome.getResult());
    }

    @Test
    public void testPostProcessResponse() {
        assertThatCode(() -> oper.postProcessResponse(outcome, null, null)).doesNotThrowAnyException();
    }

    @Test
    public void testLogTopicRequest() {
        // nothing to log
        appender.clearExtractions();
        oper.logTopicRequest(Arrays.asList(), new MyRequest());
        assertEquals(0, appender.getExtracted().size());

        // log structured data
        appender.clearExtractions();
        oper.logTopicRequest(INFRA_LIST, new MyRequest());
        List<String> output = appender.getExtracted();
        assertEquals(2, output.size());

        assertThat(output.get(0)).contains(CommInfrastructure.NOOP.toString())
                        .contains("{\n  \"theRequestId\": \"my-request-id\"\n}");

        assertThat(output.get(1)).contains(CommInfrastructure.UEB.toString())
                        .contains("{\n  \"theRequestId\": \"my-request-id\"\n}");

        // log a plain string
        appender.clearExtractions();
        new MyStringOperation().logTopicRequest(Arrays.asList(CommInfrastructure.NOOP), TEXT);
        output = appender.getExtracted();
        assertEquals(1, output.size());
        assertThat(output.get(0)).contains(CommInfrastructure.NOOP.toString()).contains(TEXT);

        // log a null request
        appender.clearExtractions();
        oper.logTopicRequest(Arrays.asList(CommInfrastructure.NOOP), null);
        output = appender.getExtracted();
        assertEquals(1, output.size());

        assertThat(output.get(0)).contains(CommInfrastructure.NOOP.toString()).contains("null");

        // exception from coder
        setOperCoderException();

        appender.clearExtractions();
        oper.logTopicRequest(Arrays.asList(CommInfrastructure.NOOP), new MyRequest());
        output = appender.getExtracted();
        assertEquals(2, output.size());
        assertThat(output.get(0)).contains("cannot pretty-print request");
        assertThat(output.get(1)).contains(CommInfrastructure.NOOP.toString());
    }

    @Test
    public void testLogTopicResponse() {
        // log structured data
        appender.clearExtractions();
        oper.logTopicResponse(CommInfrastructure.NOOP, new MyResponse());
        List<String> output = appender.getExtracted();
        assertEquals(1, output.size());

        assertThat(output.get(0)).contains(CommInfrastructure.NOOP.toString())
                        .contains("{\n  \"requestId\": \"my-request-id\"\n}");

        // log a plain string
        appender.clearExtractions();
        new MyStringOperation().logTopicResponse(CommInfrastructure.NOOP, TEXT);
        output = appender.getExtracted();
        assertEquals(1, output.size());
        assertThat(output.get(0)).contains(CommInfrastructure.NOOP.toString()).contains(TEXT);

        // log a null response
        appender.clearExtractions();
        oper.logTopicResponse(CommInfrastructure.NOOP, null);
        output = appender.getExtracted();
        assertEquals(1, output.size());

        assertThat(output.get(0)).contains(CommInfrastructure.NOOP.toString()).contains("null");

        // exception from coder
        setOperCoderException();

        appender.clearExtractions();
        oper.logTopicResponse(CommInfrastructure.NOOP, new MyResponse());
        output = appender.getExtracted();
        assertEquals(2, output.size());
        assertThat(output.get(0)).contains("cannot pretty-print response");
        assertThat(output.get(1)).contains(CommInfrastructure.NOOP.toString());
    }

    @Test
    public void testMakeCoder() {
        assertNotNull(oper.makeCoder());
    }

    /**
     * Creates a new {@link #oper} whose coder will throw an exception.
     */
    private void setOperCoderException() {
        oper = new MyOperation() {
            @Override
            protected Coder makeCoder() {
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
    public static class MyResponse {
        private String requestId = REQ_ID;
        private String output;
    }


    private class MyStringOperation extends TopicPairOperation<String, String> {
        public MyStringOperation() {
            super(TopicPairOperationTest.this.params, operator, String.class);
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
        protected boolean isSuccess(String rawResponse, String response) {
            return (response != null);
        }
    }


    private class MyScoOperation extends TopicPairOperation<MyRequest, StandardCoderObject> {
        public MyScoOperation() {
            super(TopicPairOperationTest.this.params, operator, StandardCoderObject.class);
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
        protected boolean isSuccess(String rawResponse, StandardCoderObject response) {
            return (response.getString("output") == null);
        }
    }


    private class MyOperation extends TopicPairOperation<MyRequest, MyResponse> {
        public MyOperation() {
            super(TopicPairOperationTest.this.params, operator, MyResponse.class);
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
        protected boolean isSuccess(String rawResponse, MyResponse response) {
            return (response.getOutput() == null);
        }
    }
}
