/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023, 2024 Nordix Foundation.
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams.TopicParamsBuilder;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class HttpOperationTest {

    private static final IllegalStateException EXPECTED_EXCEPTION = new IllegalStateException("expected exception");
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-name";
    private static final String HTTP_CLIENT = "my-client";
    private static final String HTTP_NO_SERVER = "my-http-no-server-client";
    private static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";
    private static final String BASE_URI = "oper";
    private static final String PATH = "/my-path";
    private static final String TEXT = "my-text";
    private static final UUID REQ_ID = UUID.randomUUID();

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
    private HttpClientFactory clientFactory;
    @Mock
    private Response response;
    @Mock
    private Executor executor;

    private ControlLoopOperationParams params;
    private OperationOutcome outcome;
    private AtomicReference<InvocationCallback<Response>> callback;
    private Future<Response> future;
    private HttpConfig config;
    private MyGetOperation<String> oper;

    /**
     * Starts the simulator.
     */
    @BeforeAll
   void setUpBeforeClass() throws Exception {
        // allocate a port
        int port = NetworkUtil.allocPort();

        /*
         * Start the simulator. Must use "Properties" to configure it, otherwise the
         * server will use the wrong serialization provider.
         */
        Properties svrprops = getServerProperties("my-server", port);
        HttpServletServerFactoryInstance.getServerFactory().build(svrprops).forEach(HttpServletServer::start);

        if (!NetworkUtil.isTcpPortOpen("localhost", port, 100, 100)) {
            HttpServletServerFactoryInstance.getServerFactory().destroy();
            throw new IllegalStateException("server is not running");
        }

        /*
         * Start the clients, one to the server, and one to a non-existent server.
         */
        TopicParamsBuilder builder = BusTopicParams.builder().managed(true).hostname("localhost").basePath(BASE_URI);

        HttpClientFactoryInstance.getClientFactory().build(builder.clientName(HTTP_CLIENT).port(port).build());

        HttpClientFactoryInstance.getClientFactory()
                        .build(builder.clientName(HTTP_NO_SERVER).port(NetworkUtil.allocPort()).build());
    }

    /**
     * Destroys the Http factories and stops the appender.
     */
    @AfterAll
   static void tearDownAfterClass() {
        HttpClientFactoryInstance.getClientFactory().destroy();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    /**
     * Initializes fields, including {@link #oper}, and resets thestatic fields used by
     * the REST server.
     */
    @BeforeEach
   void setUp() {
        rejectRequest = false;
        nget = 0;
        npost = 0;
        nput = 0;
        ndelete = 0;

        Mockito.lenient().when(response.readEntity(String.class)).thenReturn(TEXT);
        Mockito.lenient().when(response.getStatus()).thenReturn(200);

        params = ControlLoopOperationParams.builder().actor(ACTOR).operation(OPERATION).requestId(REQ_ID).build();

        outcome = params.makeOutcome();

        callback = new AtomicReference<>();
        future = new CompletableFuture<>();

        Mockito.lenient().when(clientFactory.get(any())).thenReturn(client);

        initConfig(HTTP_CLIENT);

        oper = new MyGetOperation<>(String.class);
    }

    @Test
   void testHttpOperator() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(ACTOR + "." + OPERATION, oper.getFullName());
    }

    @Test
   void testMakeHeaders() {
        assertEquals(Collections.emptyMap(), oper.makeHeaders());
    }

    @Test
   void testGetPath() {
        assertEquals(PATH, oper.getPath());
    }

    @Test
   void testMakeUrl() {
        // use a real client
        initRealConfig(HTTP_CLIENT);

        oper = new MyGetOperation<>(String.class);

        assertThat(oper.getUrl()).endsWith("/" + BASE_URI + PATH);
    }

    @Test
   void testDoConfigureMapOfStringObject_testGetClient_testGetPath_testGetTimeoutMs() {

        // use value from operator
        assertEquals(1000L, oper.getTimeoutMs(null));
        assertEquals(1000L, oper.getTimeoutMs(0));

        // should use given value
        assertEquals(20 * 1000L, oper.getTimeoutMs(20));
    }

    /**
     * Tests handleResponse() when it completes.
     */
    @Test
   void testHandleResponseComplete() throws Exception {
        CompletableFuture<OperationOutcome> future2 = oper.handleResponse(outcome, PATH, cb -> {
            callback.set(cb);
            return future;
        });

        assertFalse(future2.isDone());
        assertNotNull(callback.get());
        callback.get().completed(response);

        assertSame(outcome, future2.get(5, TimeUnit.SECONDS));
        assertSame(TEXT, outcome.getResponse());

        assertEquals(OperationResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests handleResponse() when it fails.
     */
    @Test
   void testHandleResponseFailed() throws Exception {
        CompletableFuture<OperationOutcome> future2 = oper.handleResponse(outcome, PATH, cb -> {
            callback.set(cb);
            return future;
        });

        assertFalse(future2.isDone());
        assertNotNull(callback.get());
        callback.get().failed(EXPECTED_EXCEPTION);

        assertThatThrownBy(() -> future2.get(5, TimeUnit.SECONDS)).hasCause(EXPECTED_EXCEPTION);

        // future and future2 may be completed in parallel so we must wait again
        assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS)).isInstanceOf(CancellationException.class);
        assertTrue(future.isCancelled());
    }

    /**
     * Tests processResponse() when it's a success and the response type is a String.
     */
    @Test
    void testProcessResponseSuccessString() throws Exception {
        CompletableFuture<OperationOutcome> result = oper.processResponse(outcome, PATH, response);
        assertTrue(result.isDone());
        assertSame(outcome, result.get());
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertSame(TEXT, outcome.getResponse());
    }

    /**
     * Tests processResponse() when it's a failure.
     */
    @Test
    void testProcessResponseFailure() throws Exception {
        when(response.getStatus()).thenReturn(555);
        CompletableFuture<OperationOutcome> result = oper.processResponse(outcome, PATH, response);
        assertTrue(result.isDone());
        assertSame(outcome, result.get());
        assertEquals(OperationResult.FAILURE, outcome.getResult());
        assertSame(TEXT, outcome.getResponse());
    }

    /**
     * Tests processResponse() when the decoder succeeds.
     */
    @Test
    void testProcessResponseDecodeOk() throws Exception {
        when(response.readEntity(String.class)).thenReturn("10");

        MyGetOperation<Integer> oper2 = new MyGetOperation<>(Integer.class);

        CompletableFuture<OperationOutcome> result = oper2.processResponse(outcome, PATH, response);
        assertTrue(result.isDone());
        assertSame(outcome, result.get());
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals(Integer.valueOf(10), outcome.getResponse());
    }

    /**
     * Tests processResponse() when the decoder throws an exception.
     */
    @Test
    void testProcessResponseDecodeExcept() throws CoderException {
        MyGetOperation<Integer> oper2 = new MyGetOperation<>(Integer.class);

        assertThatIllegalArgumentException().isThrownBy(() -> oper2.processResponse(outcome, PATH, response));
    }

    @Test
    void testPostProcessResponse() {
        assertThatCode(() -> oper.postProcessResponse(outcome, PATH, null, null)).doesNotThrowAnyException();
    }

    @Test
    void testIsSuccess() {
        when(response.getStatus()).thenReturn(200);
        assertTrue(oper.isSuccess(response, null));

        when(response.getStatus()).thenReturn(555);
        assertFalse(oper.isSuccess(response, null));
    }

    /**
     * Tests a GET.
     */
    @Test
    void testGet() throws Exception {
        // use a real client
        initRealConfig(HTTP_CLIENT);

        MyGetOperation<MyResponse> oper2 = new MyGetOperation<>(MyResponse.class);

        OperationOutcome outcome = runOperation(oper2);
        assertNotNull(outcome);
        assertEquals(1, nget);
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof MyResponse);
    }

    /**
     * Tests a DELETE.
     */
    @Test
    void testDelete() throws Exception {
        // use a real client
        initRealConfig(HTTP_CLIENT);

        MyDeleteOperation oper2 = new MyDeleteOperation();

        OperationOutcome outcome = runOperation(oper2);
        assertNotNull(outcome);
        assertEquals(1, ndelete);
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof String);
    }

    /**
     * Tests a POST.
     */
    @Test
    void testPost() throws Exception {
        // use a real client
        initRealConfig(HTTP_CLIENT);
        MyPostOperation oper2 = new MyPostOperation();

        OperationOutcome outcome = runOperation(oper2);
        assertNotNull(outcome);
        assertEquals(1, npost);
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof MyResponse);
    }

    /**
     * Tests a PUT.
     */
    @Test
    void testPut() throws Exception {
        // use a real client
        initRealConfig(HTTP_CLIENT);

        MyPutOperation oper2 = new MyPutOperation();

        OperationOutcome outcome = runOperation(oper2);
        assertNotNull(outcome);
        assertEquals(1, nput);
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof MyResponse);
    }

    @Test
    void testMakeDecoder() {
        assertNotNull(oper.getCoder());
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
     * Initializes the configuration.
     *
     * @param clientName name of the client which it should use
     */
    private void initConfig(String clientName) {
        initConfig(clientName, clientFactory);
    }

    /**
     * Initializes the configuration with a real client.
     *
     * @param clientName name of the client which it should use
     */
    private void initConfig(String clientName, HttpClientFactory factory) {
        HttpParams params = HttpParams.builder().clientName(clientName).path(PATH).timeoutSec(1).build();
        config = new HttpConfig(executor, params, factory);
    }

    /**
     * Initializes the configuration with a real client.
     *
     * @param clientName name of the client which it should use
     */
    private void initRealConfig(String clientName) {
        initConfig(clientName, HttpClientFactoryInstance.getClientFactory());
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

        CompletableFuture<OperationOutcome> future = operator.start();

        return future.get(5, TimeUnit.SECONDS);
    }

    @Getter
    @Setter
    static class MyRequest {
        private String input = "some input";
    }

    @Getter
    @Setter
    static class MyResponse {
        private String output = "some output";
    }

    private class MyGetOperation<T> extends HttpOperation<T> {
        MyGetOperation(Class<T> responseClass) {
            super(HttpOperationTest.this.params, HttpOperationTest.this.config, responseClass, Collections.emptyList());
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {
            Map<String, Object> headers = makeHeaders();

            headers.put("Accept", MediaType.APPLICATION_JSON);
            String url = getUrl();

            logMessage(EventType.OUT, CommInfrastructure.REST, url, null);

            // @formatter:off
            return handleResponse(outcome, url,
                callback -> getClient().get(callback, getPath(), headers));
            // @formatter:on
        }
    }

    private class MyPostOperation extends HttpOperation<MyResponse> {
        MyPostOperation() {
            super(HttpOperationTest.this.params, HttpOperationTest.this.config, MyResponse.class,
                            Collections.emptyList());
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

            MyRequest request = new MyRequest();

            Map<String, Object> headers = makeHeaders();

            headers.put("Accept", MediaType.APPLICATION_JSON);
            String url = getUrl();

            String strRequest = prettyPrint(request);
            logMessage(EventType.OUT, CommInfrastructure.REST, url, strRequest);

            Entity<String> entity = Entity.entity(strRequest, MediaType.APPLICATION_JSON);

            // @formatter:off
            return handleResponse(outcome, url,
                callback -> getClient().post(callback, getPath(), entity, headers));
            // @formatter:on
        }
    }

    private class MyPutOperation extends HttpOperation<MyResponse> {
        MyPutOperation() {
            super(HttpOperationTest.this.params, HttpOperationTest.this.config, MyResponse.class,
                            Collections.emptyList());
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

            MyRequest request = new MyRequest();

            Map<String, Object> headers = makeHeaders();

            headers.put("Accept", MediaType.APPLICATION_JSON);
            String url = getUrl();

            String strRequest = prettyPrint(request);
            logMessage(EventType.OUT, CommInfrastructure.REST, url, strRequest);

            Entity<String> entity = Entity.entity(strRequest, MediaType.APPLICATION_JSON);

            // @formatter:off
            return handleResponse(outcome, url,
                callback -> getClient().put(callback, getPath(), entity, headers));
            // @formatter:on
        }
    }

    private class MyDeleteOperation extends HttpOperation<String> {
        MyDeleteOperation() {
            super(HttpOperationTest.this.params, HttpOperationTest.this.config, String.class, Collections.emptyList());
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {
            Map<String, Object> headers = makeHeaders();

            headers.put("Accept", MediaType.APPLICATION_JSON);
            String url = getUrl();

            logMessage(EventType.OUT, CommInfrastructure.REST, url, null);

            // @formatter:off
            return handleResponse(outcome, url,
                callback -> getClient().delete(callback, getPath(), headers));
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
