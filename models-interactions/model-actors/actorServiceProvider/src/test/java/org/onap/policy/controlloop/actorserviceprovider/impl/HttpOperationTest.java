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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.Getter;
import lombok.Setter;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams.TopicParamsBuilder;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.test.log.logback.ExtractAppender;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.LoggerFactory;

public class HttpOperationTest {

    private static final IllegalStateException EXPECTED_EXCEPTION = new IllegalStateException("expected exception");
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-name";
    private static final String HTTP_CLIENT = "my-client";
    private static final String HTTP_NO_SERVER = "my-http-no-server-client";
    private static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";
    private static final String MY_REQUEST = "my-request";
    private static final String BASE_URI = "oper";
    private static final String PATH = "/my-path";
    private static final String TEXT = "my-text";
    private static final UUID REQ_ID = UUID.randomUUID();

    /**
     * Used to attach an appender to the class' logger.
     */
    private static final Logger logger = (Logger) LoggerFactory.getLogger(HttpOperation.class);
    private static final ExtractAppender appender = new ExtractAppender();

    /**
     * {@code True} if the server should reject the request, {@code false} otherwise.
     */
    private static boolean rejectRequest;

    // call counts of each method type in the server
    private static int nget;
    private static int npost;
    private static int nput;
    private static int ndelete;

    @Mock
    private HttpClient client;

    @Mock
    private Response response;

    private VirtualControlLoopEvent event;
    private ControlLoopEventContext context;
    private ControlLoopOperationParams params;
    private OperationOutcome outcome;
    private AtomicReference<InvocationCallback<Response>> callback;
    private Future<Response> future;
    private HttpOperator operator;
    private MyGetOperation<String> oper;

    /**
     * Starts the simulator.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // allocate a port
        int port = NetworkUtil.allocPort();

        /*
         * Start the simulator. Must use "Properties" to configure it, otherwise the
         * server will use the wrong serialization provider.
         */
        Properties svrprops = getServerProperties("my-server", port);
        HttpServletServerFactoryInstance.getServerFactory().build(svrprops).forEach(HttpServletServer::start);

        /*
         * Start the clients, one to the server, and one to a non-existent server.
         */
        TopicParamsBuilder builder = BusTopicParams.builder().managed(true).hostname("localhost").basePath(BASE_URI)
                        .serializationProvider(GsonMessageBodyHandler.class.getName());

        HttpClientFactoryInstance.getClientFactory().build(builder.clientName(HTTP_CLIENT).port(port).build());

        HttpClientFactoryInstance.getClientFactory()
                        .build(builder.clientName(HTTP_NO_SERVER).port(NetworkUtil.allocPort()).build());

        /**
         * Attach appender to the logger.
         */
        appender.setContext(logger.getLoggerContext());
        appender.start();

        logger.addAppender(appender);
    }

    /**
     * Destroys the Http factories and stops the appender.
     */
    @AfterClass
    public static void tearDownAfterClass() {
        appender.stop();

        HttpClientFactoryInstance.getClientFactory().destroy();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    /**
     * Initializes fields, including {@link #oper}, and resets the static fields used by
     * the REST server.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        appender.clearExtractions();

        rejectRequest = false;
        nget = 0;
        npost = 0;
        nput = 0;
        ndelete = 0;

        when(response.readEntity(String.class)).thenReturn(TEXT);
        when(response.getStatus()).thenReturn(200);

        event = new VirtualControlLoopEvent();
        event.setRequestId(REQ_ID);

        context = new ControlLoopEventContext(event);
        params = ControlLoopOperationParams.builder().actor(ACTOR).operation(OPERATION).context(context).build();

        outcome = params.makeOutcome();

        callback = new AtomicReference<>();
        future = new CompletableFuture<>();

        operator = new HttpOperator(ACTOR, OPERATION) {
            @Override
            public Operation buildOperation(ControlLoopOperationParams params) {
                return null;
            }

            @Override
            public HttpClient getClient() {
                return client;
            }
        };

        initOper(operator, HTTP_CLIENT);

        oper = new MyGetOperation<>(String.class);
    }

    @Test
    public void testHttpOperator() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(ACTOR + "." + OPERATION, oper.getFullName());
    }

    @Test
    public void testMakeHeaders() {
        assertEquals(Collections.emptyMap(), oper.makeHeaders());
    }

    @Test
    public void testMakePath() {
        assertEquals(PATH, oper.makePath());
    }

    @Test
    public void testMakeUrl() {
        // use a real client
        client = HttpClientFactoryInstance.getClientFactory().get(HTTP_CLIENT);

        assertThat(oper.makeUrl()).endsWith("/" + BASE_URI + PATH);
    }

    @Test
    public void testDoConfigureMapOfStringObject_testGetClient_testGetPath_testGetTimeoutMs() {

        // no default yet
        assertEquals(0L, oper.getTimeOutMs(null));
        assertEquals(0L, oper.getTimeOutMs(0));

        // should use given value
        assertEquals(20 * 1000L, oper.getTimeOutMs(20));

        // indicate we have a timeout value
        operator = spy(operator);
        when(operator.getTimeoutMs()).thenReturn(30L);

        oper = new MyGetOperation<String>(String.class);

        // should use default
        assertEquals(30L, oper.getTimeOutMs(null));
        assertEquals(30L, oper.getTimeOutMs(0));

        // should use given value
        assertEquals(40 * 1000L, oper.getTimeOutMs(40));
    }

    /**
     * Tests handleResponse() when it completes.
     */
    @Test
    public void testHandleResponseComplete() throws Exception {
        CompletableFuture<OperationOutcome> future2 = oper.handleResponse(outcome, PATH, cb -> {
            callback.set(cb);
            return future;
        });

        assertFalse(future2.isDone());
        assertNotNull(callback.get());
        callback.get().completed(response);

        assertSame(outcome, future2.get(5, TimeUnit.SECONDS));

        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests handleResponse() when it fails.
     */
    @Test
    public void testHandleResponseFailed() throws Exception {
        CompletableFuture<OperationOutcome> future2 = oper.handleResponse(outcome, PATH, cb -> {
            callback.set(cb);
            return future;
        });

        assertFalse(future2.isDone());
        assertNotNull(callback.get());
        callback.get().failed(EXPECTED_EXCEPTION);

        assertThatThrownBy(() -> future2.get(5, TimeUnit.SECONDS)).hasCause(EXPECTED_EXCEPTION);

        assertTrue(future.isCancelled());
    }

    /**
     * Tests processResponse() when it's a success and the response type is a String.
     */
    @Test
    public void testProcessResponseSuccessString() {
        assertSame(outcome, oper.processResponse(outcome, PATH, response));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests processResponse() when it's a failure.
     */
    @Test
    public void testProcessResponseFailure() {
        when(response.getStatus()).thenReturn(555);
        assertSame(outcome, oper.processResponse(outcome, PATH, response));
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
    }

    /**
     * Tests processResponse() when the decoder succeeds.
     */
    @Test
    public void testProcessResponseDecodeOk() throws CoderException {
        when(response.readEntity(String.class)).thenReturn("10");

        MyGetOperation<Integer> oper2 = new MyGetOperation<>(Integer.class);

        assertSame(outcome, oper2.processResponse(outcome, PATH, response));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests processResponse() when the decoder throws an exception.
     */
    @Test
    public void testProcessResponseDecodeExcept() throws CoderException {
        MyGetOperation<Integer> oper2 = new MyGetOperation<>(Integer.class);

        assertSame(outcome, oper2.processResponse(outcome, PATH, response));
        assertEquals(PolicyResult.FAILURE_EXCEPTION, outcome.getResult());
    }

    @Test
    public void testPostProcessResponse() {
        assertThatCode(() -> oper.postProcessResponse(outcome, PATH, null, null)).doesNotThrowAnyException();
    }

    @Test
    public void testIsSuccess() {
        when(response.getStatus()).thenReturn(200);
        assertTrue(oper.isSuccess(response, null));

        when(response.getStatus()).thenReturn(555);
        assertFalse(oper.isSuccess(response, null));
    }

    @Test
    public void testStartQueryAsync() {
        // ensure future is canceled if the GET is canceled
        CompletableFuture<Response> future = new CompletableFuture<>();
        when(client.get(any(), any(), any())).thenReturn(future);

        CompletableFuture<OperationOutcome> result = oper.startOperationAsync(1, params.makeOutcome());
        result.cancel(false);

        assertTrue(future.isCancelled());
    }

    /**
     * Tests startQueryAsync() for a GET.
     */
    @Test
    public void testStartQueryAsyncGet() throws Exception {
        // use a real client
        client = HttpClientFactoryInstance.getClientFactory().get(HTTP_CLIENT);

        MyGetOperation<MyResponse> oper2 = new MyGetOperation<>(MyResponse.class);

        OperationOutcome outcome = runOperation(oper2);
        assertNotNull(outcome);
        assertEquals(1, nget);
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests startQueryAsync() for a GET.
     */
    @Test
    public void testStartQueryAsyncDelete() throws Exception {
        // use a real client
        client = HttpClientFactoryInstance.getClientFactory().get(HTTP_CLIENT);

        MyDeleteOperation oper2 = new MyDeleteOperation();

        OperationOutcome outcome = runOperation(oper2);
        assertNotNull(outcome);
        assertEquals(1, ndelete);
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    @Test
    public void testStartRequestAsync() {
        // ensure future is canceled if the POST is canceled
        CompletableFuture<Response> future = new CompletableFuture<>();
        when(client.post(any(), any(), any(), any())).thenReturn(future);

        MyPostOperation oper2 = new MyPostOperation();

        CompletableFuture<OperationOutcome> result = oper2.startOperationAsync(1, params.makeOutcome());
        result.cancel(false);

        assertTrue(future.isCancelled());
    }

    /**
     * Tests startRequestAsync() for a POST.
     */
    @Test
    public void testStartRequestAsyncPost() throws Exception {
        // use a real client
        client = HttpClientFactoryInstance.getClientFactory().get(HTTP_CLIENT);

        MyPostOperation oper2 = new MyPostOperation();

        OperationOutcome outcome = runOperation(oper2);
        assertNotNull(outcome);
        assertEquals(1, npost);
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests startRequestAsync() for a PUT.
     */
    @Test
    public void testStartRequestAsyncPut() throws Exception {
        // use a real client
        client = HttpClientFactoryInstance.getClientFactory().get(HTTP_CLIENT);

        MyPutOperation oper2 = new MyPutOperation();

        OperationOutcome outcome = runOperation(oper2);
        assertNotNull(outcome);
        assertEquals(1, nput);
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests postRequest() when the response is not a success.
     */
    @Test
    public void testPostRequestOutputFailure() throws Exception {
        // use a real client
        client = HttpClientFactoryInstance.getClientFactory().get(HTTP_CLIENT);

        rejectRequest = true;

        MyGetOperation<MyResponse> oper2 = new MyGetOperation<>(MyResponse.class);

        OperationOutcome outcome = runOperation(oper2);
        assertNotNull(outcome);
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
    }

    /**
     * Tests postRequest() when the get() request throws an exception retrieving the
     * response.
     */
    @Test
    public void testPostRequestException() throws Exception {
        // use a real client, but point it to a non-existent server
        client = HttpClientFactoryInstance.getClientFactory().get(HTTP_NO_SERVER);

        CompletableFuture<OperationOutcome> future = oper.startOperationAsync(1, params.makeOutcome());

        assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class);
    }

    @Test
    public void testLogRestRequest() throws CoderException {
        // log structured data
        appender.clearExtractions();
        oper.logRestRequest(PATH, new MyRequest());
        List<String> output = appender.getExtracted();
        assertEquals(1, output.size());

        assertThat(output.get(0)).contains(PATH).contains("{\n  \"input\": \"some input\"\n}");

        // log a plain string
        appender.clearExtractions();
        oper.logRestRequest(PATH, MY_REQUEST);
        output = appender.getExtracted();
        assertEquals(1, output.size());

        assertThat(output.get(0)).contains(PATH).contains(MY_REQUEST);

        // exception from coder
        oper = new MyGetOperation<>(String.class) {
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

        appender.clearExtractions();
        oper.logRestRequest(PATH, new MyRequest());
        output = appender.getExtracted();
        assertEquals(2, output.size());
        assertThat(output.get(0)).contains("cannot pretty-print request");
        assertThat(output.get(1)).contains(PATH);
    }

    @Test
    public void testLogRestResponse() throws CoderException {
        // log structured data
        appender.clearExtractions();
        oper.logRestResponse(PATH, new MyResponse());
        List<String> output = appender.getExtracted();
        assertEquals(1, output.size());

        assertThat(output.get(0)).contains(PATH).contains("{\n  \"output\": \"some output\"\n}");

        // log a plain string
        appender.clearExtractions();
        oper.logRestResponse(PATH, MY_REQUEST);
        output = appender.getExtracted();
        assertEquals(1, output.size());

        // log a null response
        appender.clearExtractions();
        oper.logRestResponse(PATH, null);
        output = appender.getExtracted();
        assertEquals(1, output.size());

        assertThat(output.get(0)).contains(PATH).contains("null");

        // exception from coder
        oper = new MyGetOperation<>(String.class) {
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

        appender.clearExtractions();
        oper.logRestResponse(PATH, new MyResponse());
        output = appender.getExtracted();
        assertEquals(2, output.size());
        assertThat(output.get(0)).contains("cannot pretty-print response");
        assertThat(output.get(1)).contains(PATH);
    }

    @Test
    public void testMakeDecoder() {
        assertNotNull(oper.makeCoder());
    }

    /**
     * Gets server properties.
     *
     * @param name server name
     * @param port server port
     * @return server properties
     */
    private static Properties getServerProperties(String name, int port) {
        final Properties props = new Properties();
        props.setProperty(PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES, name);

        final String svcpfx = PolicyEndPointProperties.PROPERTY_HTTP_SERVER_SERVICES + "." + name;

        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_REST_CLASSES_SUFFIX, Server.class.getName());
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_HOST_SUFFIX, "localhost");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_PORT_SUFFIX, String.valueOf(port));
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_MANAGED_SUFFIX, "true");
        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SWAGGER_SUFFIX, "false");

        props.setProperty(svcpfx + PolicyEndPointProperties.PROPERTY_HTTP_SERIALIZATION_PROVIDER,
                        GsonMessageBodyHandler.class.getName());
        return props;
    }

    /**
     * Initializes the given operator.
     *
     * @param operator operator to be initialized
     * @param clientName name of the client which it should use
     */
    private void initOper(HttpOperator operator, String clientName) {
        operator.stop();

        HttpParams params = HttpParams.builder().clientName(clientName).path(PATH).build();
        Map<String, Object> mapParams = Util.translateToMap(OPERATION, params);
        operator.configure(mapParams);
        operator.start();
    }

    /**
     * Runs the operation.
     *
     * @param operator operator on which to start the operation
     * @return the outcome of the operation, or {@code null} if it does not complete in
     *         time
     */
    private <T> OperationOutcome runOperation(HttpOperation<T> operator)
                    throws InterruptedException, ExecutionException, TimeoutException {

        CompletableFuture<OperationOutcome> future = operator.startOperationAsync(1, params.makeOutcome());

        return future.get(5, TimeUnit.SECONDS);
    }

    @Getter
    @Setter
    public static class MyRequest {
        private String input = "some input";
    }

    @Getter
    @Setter
    public static class MyResponse {
        private String output = "some output";
    }

    private class MyGetOperation<T> extends HttpOperation<T> {
        public MyGetOperation(Class<T> responseClass) {
            super(HttpOperationTest.this.params, HttpOperationTest.this.operator, responseClass);
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {
            Map<String, Object> headers = makeHeaders();

            headers.put("Accept", MediaType.APPLICATION_JSON);
            String url = makeUrl();

            logRestRequest(url, null);

            // @formatter:off
            return handleResponse(outcome, url,
                callback -> operator.getClient().get(callback, makePath(), headers));
            // @formatter:on
        }
    }

    private class MyPostOperation extends HttpOperation<MyResponse> {
        public MyPostOperation() {
            super(HttpOperationTest.this.params, HttpOperationTest.this.operator, MyResponse.class);
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

            MyRequest request = new MyRequest();

            Entity<MyRequest> entity = Entity.entity(request, MediaType.APPLICATION_JSON);

            Map<String, Object> headers = makeHeaders();

            headers.put("Accept", MediaType.APPLICATION_JSON);
            String url = makeUrl();

            logRestRequest(url, request);

            // @formatter:off
            return handleResponse(outcome, url,
                callback -> operator.getClient().post(callback, makePath(), entity, headers));
            // @formatter:on
        }
    }

    private class MyPutOperation extends HttpOperation<MyResponse> {
        public MyPutOperation() {
            super(HttpOperationTest.this.params, HttpOperationTest.this.operator, MyResponse.class);
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

            MyRequest request = new MyRequest();

            Entity<MyRequest> entity = Entity.entity(request, MediaType.APPLICATION_JSON);

            Map<String, Object> headers = makeHeaders();

            headers.put("Accept", MediaType.APPLICATION_JSON);
            String url = makeUrl();

            logRestRequest(url, request);

            // @formatter:off
            return handleResponse(outcome, url,
                callback -> operator.getClient().put(callback, makePath(), entity, headers));
            // @formatter:on
        }
    }

    private class MyDeleteOperation extends HttpOperation<String> {
        public MyDeleteOperation() {
            super(HttpOperationTest.this.params, HttpOperationTest.this.operator, String.class);
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {
            Map<String, Object> headers = makeHeaders();

            headers.put("Accept", MediaType.APPLICATION_JSON);
            String url = makeUrl();

            logRestRequest(url, null);

            // @formatter:off
            return handleResponse(outcome, url,
                callback -> operator.getClient().delete(callback, makePath(), headers));
            // @formatter:on
        }
    }

    /**
     * Simulator.
     */
    @Path("/" + BASE_URI)
    @Produces(MEDIA_TYPE_APPLICATION_JSON)
    @Consumes(value = {MEDIA_TYPE_APPLICATION_JSON})
    public static class Server {

        /**
         * Generates a response to a GET.
         *
         * @return resulting response
         */
        @GET
        @Path(PATH)
        public Response getRequest() {
            ++nget;

            if (rejectRequest) {
                return Response.status(Status.BAD_REQUEST).build();

            } else {
                return Response.status(Status.OK).entity(new MyResponse()).build();
            }
        }

        /**
         * Generates a response to a POST.
         *
         * @param request incoming request
         * @return resulting response
         */
        @POST
        @Path(PATH)
        public Response postRequest(MyRequest request) {
            ++npost;

            if (rejectRequest) {
                return Response.status(Status.BAD_REQUEST).build();

            } else {
                return Response.status(Status.OK).entity(new MyResponse()).build();
            }
        }

        /**
         * Generates a response to a PUT.
         *
         * @param request incoming request
         * @return resulting response
         */
        @PUT
        @Path(PATH)
        public Response putRequest(MyRequest request) {
            ++nput;

            if (rejectRequest) {
                return Response.status(Status.BAD_REQUEST).build();

            } else {
                return Response.status(Status.OK).entity(new MyResponse()).build();
            }
        }

        /**
         * Generates a response to a DELETE.
         *
         * @return resulting response
         */
        @DELETE
        @Path(PATH)
        public Response deleteRequest() {
            ++ndelete;

            if (rejectRequest) {
                return Response.status(Status.BAD_REQUEST).build();

            } else {
                return Response.status(Status.OK).entity(new MyResponse()).build();
            }
        }
    }
}
