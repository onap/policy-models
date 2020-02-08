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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
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
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.LoggerFactory;

public class HttpOperatorTest {

    private static final IllegalStateException EXPECTED_EXCEPTION = new IllegalStateException("expected exception");
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-name";
    private static final String HTTP_CLIENT = "my-client";
    private static final String HTTP_NO_SERVER = "my-http-no-server-client";
    private static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";
    private static final String MY_REQUEST = "my-request";
    private static final String BASE_URI = "oper";
    private static final String PATH = "my-path";
    private static final String TEXT = "my-text";
    private static final int TIMEOUT = 100;
    private static final UUID REQ_ID = UUID.randomUUID();

    /**
     * Used to attach an appender to the class' logger.
     */
    private static final Logger logger = (Logger) LoggerFactory.getLogger(HttpOperator.class);
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
    private MyGetOperator<String> oper;

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

        oper = new MyGetOperator<>(String.class);
        initOper(oper, HTTP_CLIENT);
    }

    @Test
    public void testHttpOperator() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(ACTOR + "." + OPERATION, oper.getFullName());
    }

    @Test
    public void testMakeHeaders() {
        assertEquals(Collections.emptyMap(), oper.makeHeaders(null));
    }

    @Test
    public void testMakePath() {
        assertEquals(PATH, oper.makePath(params));
    }

    @Test
    public void testMakeUrl() {
        assertThat(oper.makeUrl(params)).endsWith("/" + BASE_URI + "/" + PATH);
    }

    @Test
    public void testGetClient() {
        assertNotNull(oper.getClient());
    }

    @Test
    public void testDoConfigureMapOfStringObject_testGetClient_testGetPath_testGetTimeoutMs() {
        // start with an UNCONFIGURED operator
        oper.shutdown();
        oper = new MyGetOperator<>(String.class);

        assertNull(oper.getClient());
        assertNull(oper.getPath());

        // no default yet
        assertEquals(0L, oper.getTimeOutMs(null));
        assertEquals(0L, oper.getTimeOutMs(0));

        // should use given value
        assertEquals(2 * TIMEOUT * 1000L, oper.getTimeOutMs(2 * TIMEOUT));

        oper.shutdown();
        oper = new MyGetOperator<String>(String.class) {
            @Override
            public HttpClientFactory getClientFactory() {
                HttpClientFactory factory = mock(HttpClientFactory.class);
                when(factory.get(HTTP_CLIENT)).thenReturn(client);
                return factory;
            }
        };

        HttpParams params = HttpParams.builder().clientName(HTTP_CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        Map<String, Object> paramMap = Util.translateToMap(OPERATION, params);
        oper.configure(paramMap);

        assertSame(client, oper.getClient());
        assertEquals(PATH, oper.getPath());

        // should use default
        assertEquals(TIMEOUT * 1000L, oper.getTimeOutMs(null));
        assertEquals(TIMEOUT * 1000L, oper.getTimeOutMs(0));

        // should use given value
        assertEquals(2 * TIMEOUT * 1000L, oper.getTimeOutMs(2 * TIMEOUT));

        // test invalid parameters
        paramMap.remove("path");
        assertThatThrownBy(() -> oper.configure(paramMap)).isInstanceOf(ParameterValidationRuntimeException.class);
    }

    /**
     * Tests handleResponse() when it completes.
     */
    @Test
    public void testHandleResponseComplete() throws Exception {
        CompletableFuture<OperationOutcome> future2 = oper.handleResponse(params, outcome, PATH, String.class, cb -> {
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
        CompletableFuture<OperationOutcome> future2 = oper.handleResponse(params, outcome, PATH, String.class, cb -> {
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
        assertSame(outcome, oper.processResponse(params, outcome, PATH, String.class, response));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests processResponse() when it's a failure.
     */
    @Test
    public void testProcessResponseFailure() {
        when(response.getStatus()).thenReturn(555);
        assertSame(outcome, oper.processResponse(params, outcome, PATH, String.class, response));
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
    }

    /**
     * Tests processResponse() when the decoder succeeds.
     */
    @Test
    public void testProcessResponseDecodeOk() throws CoderException {
        when(response.readEntity(String.class)).thenReturn("10");

        MyGetOperator<Integer> oper2 = new MyGetOperator<>(Integer.class);

        assertSame(outcome, oper2.processResponse(params, outcome, PATH, Integer.class, response));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests processResponse() when the decoder throws an exception.
     */
    @Test
    public void testProcessResponseDecodeExcept() throws CoderException {
        MyGetOperator<Integer> oper2 = new MyGetOperator<>(Integer.class);

        assertSame(outcome, oper2.processResponse(params, outcome, PATH, Integer.class, response));
        assertEquals(PolicyResult.FAILURE_EXCEPTION, outcome.getResult());
    }

    @Test
    public void testPostProcessResponse() {
        assertThatCode(() -> oper.postProcessResponse(params, outcome, PATH, null, null)).doesNotThrowAnyException();
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

        oper = spy(oper);
        when(oper.getClient()).thenReturn(client);

        CompletableFuture<OperationOutcome> result = oper.startOperationAsync(params, 1, params.makeOutcome());
        result.cancel(false);

        assertTrue(future.isCancelled());
    }

    /**
     * Tests startQueryAsync() for a GET.
     */
    @Test
    public void testStartQueryAsyncGet() throws Exception {
        MyGetOperator<MyResponse> oper2 = new MyGetOperator<>(MyResponse.class);
        initOper(oper2, HTTP_CLIENT);

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
        MyDeleteOperator oper2 = new MyDeleteOperator();
        initOper(oper2, HTTP_CLIENT);

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

        MyPostOperator oper2 = spy(new MyPostOperator());
        when(oper2.getClient()).thenReturn(client);

        CompletableFuture<OperationOutcome> result = oper2.startOperationAsync(params, 1, params.makeOutcome());
        result.cancel(false);

        assertTrue(future.isCancelled());
    }

    /**
     * Tests startRequestAsync() for a POST.
     */
    @Test
    public void testStartRequestAsyncPost() throws Exception {
        MyPostOperator oper2 = new MyPostOperator();
        initOper(oper2, HTTP_CLIENT);

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
        MyPutOperator oper2 = new MyPutOperator();
        initOper(oper2, HTTP_CLIENT);

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

        rejectRequest = true;

        MyGetOperator<MyResponse> oper2 = new MyGetOperator<>(MyResponse.class);
        initOper(oper2, HTTP_CLIENT);

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

        // reset "oper" to point to a non-existent server
        oper.shutdown();
        oper = new MyGetOperator<String>(String.class);
        initOper(oper, HTTP_NO_SERVER);

        CompletableFuture<OperationOutcome> future = oper.startOperationAsync(params, 1, params.makeOutcome());

        assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS)).isInstanceOf(ExecutionException.class);
    }

    @Test
    public void testGetClientFactory() {
        assertNotNull(oper.getClientFactory());
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
        oper.shutdown();
        oper = new MyGetOperator<>(String.class) {
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
        oper.shutdown();
        oper = new MyGetOperator<>(String.class) {
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
    private <Q, S> void initOper(HttpOperator<Q, S> operator, String clientName) {
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
    private <Q, S> OperationOutcome runOperation(HttpOperator<Q, S> operator)
                    throws InterruptedException, ExecutionException, TimeoutException {

        CompletableFuture<OperationOutcome> future = operator.startOperationAsync(params, 1, params.makeOutcome());

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

    private class MyGetOperator<T> extends HttpOperator<Void, T> {
        public MyGetOperator(Class<T> responseClass) {
            super(ACTOR, OPERATION, responseClass);
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(ControlLoopOperationParams params,
                        int attempt, OperationOutcome outcome) {
            return this.startQueryAsync(params, outcome);
        }

        // do a GET
        @Override
        protected Future<Response> startQueryAsync(InvocationCallback<Response> callback, String path,
                        Map<String, Object> headers) {
            return getClient().get(callback, path, headers);
        }

        @Override
        protected Future<Response> startRequestAsync(InvocationCallback<Response> callback, String path,
                        Entity<Void> entity, Map<String, Object> headers) {
            return null;
        }

        @Override
        protected Void makeRequest(ControlLoopOperationParams params, int attempt) {
            return null;
        }
    }

    private class MyPostOperator extends HttpOperator<MyRequest, MyResponse> {
        public MyPostOperator() {
            super(ACTOR, OPERATION, MyResponse.class);
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(ControlLoopOperationParams params,
                        int attempt, OperationOutcome outcome) {
            return this.startRequestAsync(params, attempt, outcome);
        }

        // do a POST
        @Override
        protected Future<Response> startRequestAsync(InvocationCallback<Response> callback, String path,
                        Entity<MyRequest> entity, Map<String, Object> headers) {
            return getClient().post(callback, path, entity, headers);
        }

        @Override
        protected MyRequest makeRequest(ControlLoopOperationParams params, int attempt) {
            return new MyRequest();
        }

        @Override
        protected Future<Response> startQueryAsync(InvocationCallback<Response> callback, String path,
                        Map<String, Object> headers) {
            return null;
        }
    }

    private class MyPutOperator extends MyPostOperator {
        // do a PUT
        @Override
        protected Future<Response> startRequestAsync(InvocationCallback<Response> callback, String path,
                        Entity<MyRequest> entity, Map<String, Object> headers) {
            return getClient().put(callback, path, entity, headers);
        }
    }

    private class MyDeleteOperator extends MyGetOperator<MyResponse> {
        public MyDeleteOperator() {
            super(MyResponse.class);
        }

        // do a DELETE
        @Override
        protected Future<Response> startQueryAsync(InvocationCallback<Response> callback, String path,
                        Map<String, Object> headers) {
            return getClient().delete(callback, path, headers);
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
        @Path("/" + PATH)
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
        @Path("/" + PATH)
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
        @Path("/" + PATH)
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
        @Path("/" + PATH)
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
