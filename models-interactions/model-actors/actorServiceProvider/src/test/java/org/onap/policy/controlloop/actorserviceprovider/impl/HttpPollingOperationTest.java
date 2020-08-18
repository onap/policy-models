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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.controlloop.policy.PolicyResult;

/**
 * Tests HttpOperation when polling is enabled.
 */
public class HttpPollingOperationTest {
    private static final String BASE_URI = "http://my-host:6969/base-uri/";
    private static final String MY_PATH = "my-path";
    private static final String FULL_PATH = BASE_URI + MY_PATH;
    private static final int MAX_POLLS = 3;
    private static final int POLL_WAIT_SEC = 20;
    private static final String POLL_PATH = "my-poll-path";
    private static final String MY_ACTOR = "my-actor";
    private static final String MY_OPERATION = "my-operation";
    private static final String MY_RESPONSE = "my-response";
    private static final int RESPONSE_ACCEPT = 100;
    private static final int RESPONSE_SUCCESS = 200;
    private static final int RESPONSE_FAILURE = 500;

    @Mock
    private HttpPollingConfig config;
    @Mock
    private HttpClient client;
    @Mock
    private Response rawResponse;

    protected ControlLoopOperationParams params;
    private String response;
    private OperationOutcome outcome;

    private HttpOperation<String> oper;

    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(client.getBaseUrl()).thenReturn(BASE_URI);

        when(config.getClient()).thenReturn(client);
        when(config.getPath()).thenReturn(MY_PATH);
        when(config.getMaxPolls()).thenReturn(MAX_POLLS);
        when(config.getPollPath()).thenReturn(POLL_PATH);
        when(config.getPollWaitSec()).thenReturn(POLL_WAIT_SEC);

        response = MY_RESPONSE;

        when(rawResponse.getStatus()).thenReturn(RESPONSE_SUCCESS);
        when(rawResponse.readEntity(String.class)).thenReturn(response);

        params = ControlLoopOperationParams.builder().actor(MY_ACTOR).operation(MY_OPERATION).build();
        outcome = params.makeOutcome(null);

        oper = new MyOper(params, config);
    }

    @Test
    public void testConstructor_testGetWaitMsGet() {
        assertEquals(MY_ACTOR, oper.getActorName());
        assertEquals(MY_OPERATION, oper.getName());
        assertSame(config, oper.getConfig());
        assertEquals(1000 * POLL_WAIT_SEC, oper.getPollWaitMs());
    }

    @Test
    public void testSetUsePollExceptions() {
        // should be no exception
        oper.setUsePolling();

        // should throw an exception if we pass a plain HttpConfig
        HttpConfig config2 = mock(HttpConfig.class);
        when(config2.getClient()).thenReturn(client);
        when(config2.getPath()).thenReturn(MY_PATH);

        assertThatIllegalStateException().isThrownBy(() -> new MyOper(params, config2).setUsePolling());
    }

    @Test
    public void testPostProcess() throws Exception {
        // completed
        oper.generateSubRequestId(2);
        CompletableFuture<OperationOutcome> future2 =
                        oper.postProcessResponse(outcome, FULL_PATH, rawResponse, response);
        assertTrue(future2.isDone());
        assertSame(outcome, future2.get());
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
        assertNotNull(oper.getSubRequestId());
        assertSame(response, outcome.getResponse());

        // failed
        oper.generateSubRequestId(2);
        when(rawResponse.getStatus()).thenReturn(RESPONSE_FAILURE);
        future2 = oper.postProcessResponse(outcome, FULL_PATH, rawResponse, response);
        assertTrue(future2.isDone());
        assertSame(outcome, future2.get());
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
        assertNotNull(oper.getSubRequestId());
        assertSame(response, outcome.getResponse());
    }

    /**
     * Tests postProcess() when the poll is repeated a couple of times.
     */
    @Test
    public void testPostProcessRepeated_testResetGetCount() throws Exception {
        /*
         * Two accepts and then a success - should result in two polls.
         */
        when(rawResponse.getStatus()).thenReturn(RESPONSE_ACCEPT, RESPONSE_ACCEPT, RESPONSE_SUCCESS, RESPONSE_SUCCESS);

        when(client.get(any(), any(), any())).thenAnswer(provideResponse(rawResponse));

        // use a real executor
        params = params.toBuilder().executor(ForkJoinPool.commonPool()).build();

        oper = new MyOper(params, config) {
            @Override
            public long getPollWaitMs() {
                return 1;
            }
        };

        CompletableFuture<OperationOutcome> future2 =
                        oper.postProcessResponse(outcome, FULL_PATH, rawResponse, response);

        assertSame(outcome, future2.get(5, TimeUnit.SECONDS));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
        assertEquals(2, oper.getPollCount());

        /*
         * repeat - this time, the "poll" count will be exhausted, so it should fail
         */
        when(rawResponse.getStatus()).thenReturn(RESPONSE_ACCEPT);

        future2 = oper.postProcessResponse(outcome, FULL_PATH, rawResponse, response);

        assertSame(outcome, future2.get(5, TimeUnit.SECONDS));
        assertEquals(PolicyResult.FAILURE_TIMEOUT, outcome.getResult());
        assertEquals(MAX_POLLS + 1, oper.getPollCount());

        oper.resetPollCount();
        assertEquals(0, oper.getPollCount());
        assertNull(oper.getSubRequestId());
    }

    @Test
    public void testDetmStatus() {
        // make an operation that does NOT override detmStatus()
        oper = new HttpOperation<String>(params, config, String.class, Collections.emptyList()) {};

        assertThatThrownBy(() -> oper.detmStatus(rawResponse, response))
                        .isInstanceOf(UnsupportedOperationException.class);
    }

    /**
     * Provides a response to an asynchronous HttpClient call.
     *
     * @param response response to be provided to the call
     * @return a function that provides the response to the call
     */
    protected Answer<CompletableFuture<Response>> provideResponse(Response response) {
        return provideResponse(response, 0);
    }

    /**
     * Provides a response to an asynchronous HttpClient call.
     *
     * @param response response to be provided to the call
     * @param index index of the callback within the arguments
     * @return a function that provides the response to the call
     */
    protected Answer<CompletableFuture<Response>> provideResponse(Response response, int index) {
        return args -> {
            InvocationCallback<Response> cb = args.getArgument(index);
            cb.completed(response);
            return CompletableFuture.completedFuture(response);
        };
    }

    private static class MyOper extends HttpOperation<String> {

        public MyOper(ControlLoopOperationParams params, HttpConfig config) {
            super(params, config, String.class, Collections.emptyList());

            setUsePolling();
        }

        @Override
        protected Status detmStatus(Response rawResponse, String response) {
            switch (rawResponse.getStatus()) {
                case RESPONSE_ACCEPT:
                    return Status.STILL_WAITING;
                case RESPONSE_SUCCESS:
                    return Status.SUCCESS;
                default:
                    return Status.FAILURE;
            }
        }

        @Override
        protected boolean isSuccess(Response rawResponse, String response) {
            return true;
        }
    }
}
