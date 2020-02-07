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

package org.onap.policy.controlloop.actor.sdnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import lombok.Setter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams.TopicParamsBuilder;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.endpoints.properties.PolicyEndPointProperties;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.sdnc.SdncHealRequest;
import org.onap.policy.sdnc.SdncRequest;
import org.onap.policy.sdnc.SdncResponse;
import org.onap.policy.sdnc.SdncResponseOutput;

public class SdncOperatorTest {
    public static final String MEDIA_TYPE_APPLICATION_JSON = "application/json";
    private static final String EXPECTED_EXCEPTION = "expected exception";
    public static final String HTTP_CLIENT = "my-http-client";
    public static final String HTTP_NO_SERVER = "my-http-no-server-client";
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";

    /**
     * Outcome to be added to the response.
     */
    @Setter
    private static SdncResponseOutput output;


    private VirtualControlLoopEvent event;
    private ControlLoopEventContext context;
    private MyOper oper;

    /**
     * Starts the SDNC simulator.
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
        TopicParamsBuilder builder = BusTopicParams.builder().managed(true).hostname("localhost").basePath("sdnc")
                        .serializationProvider(GsonMessageBodyHandler.class.getName());

        HttpClientFactoryInstance.getClientFactory().build(builder.clientName(HTTP_CLIENT).port(port).build());

        HttpClientFactoryInstance.getClientFactory()
                        .build(builder.clientName(HTTP_NO_SERVER).port(NetworkUtil.allocPort()).build());
    }

    @AfterClass
    public static void tearDownAfterClass() {
        HttpClientFactoryInstance.getClientFactory().destroy();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    /**
     * Initializes {@link #oper} and sets {@link #output} to a success code.
     */
    @Before
    public void setUp() {
        event = new VirtualControlLoopEvent();
        context = new ControlLoopEventContext(event);

        initOper(HTTP_CLIENT);

        output = new SdncResponseOutput();
        output.setResponseCode("200");
    }

    @After
    public void tearDown() {
        oper.shutdown();
    }

    @Test
    public void testSdncOperator() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(ACTOR + "." + OPERATION, oper.getFullName());
    }

    @Test
    public void testGetClient() {
        assertNotNull(oper.getTheClient());
    }

    @Test
    public void testStartOperationAsync_testPostRequest() throws Exception {
        OperationOutcome outcome = runOperation();
        assertNotNull(outcome);
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests postRequest() when decode() throws an exception.
     */
    @Test
    public void testPostRequestDecodeException() throws Exception {

        oper.setDecodeFailure(true);

        OperationOutcome outcome = runOperation();
        assertNotNull(outcome);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, outcome.getResult());
    }

    /**
     * Tests postRequest() when there is no "output" field in the response.
     */
    @Test
    public void testPostRequestNoOutput() throws Exception {

        setOutput(null);

        OperationOutcome outcome = runOperation();
        assertNotNull(outcome);
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
    }

    /**
     * Tests postRequest() when the output is not a success.
     */
    @Test
    public void testPostRequestOutputFailure() throws Exception {

        output.setResponseCode(null);

        OperationOutcome outcome = runOperation();
        assertNotNull(outcome);
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
    }

    /**
     * Tests postRequest() when the post() request throws an exception retrieving the
     * response.
     */
    @Test
    public void testPostRequestException() throws Exception {

        // reset "oper" to point to a non-existent server
        oper.shutdown();
        initOper(HTTP_NO_SERVER);

        OperationOutcome outcome = runOperation();
        assertNotNull(outcome);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, outcome.getResult());
    }

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
     * Initializes {@link #oper}.
     *
     * @param clientName name of the client which it should use
     */
    private void initOper(String clientName) {
        oper = new MyOper();

        HttpParams params = HttpParams.builder().clientName(clientName).path("request").build();
        Map<String, Object> mapParams = Util.translateToMap(OPERATION, params);
        oper.configure(mapParams);
        oper.start();
    }

    /**
     * Runs the operation.
     *
     * @return the outcome of the operation, or {@code null} if it does not complete in
     *         time
     */
    private OperationOutcome runOperation() throws InterruptedException, ExecutionException, TimeoutException {
        ControlLoopOperationParams params =
                        ControlLoopOperationParams.builder().actor(ACTOR).operation(OPERATION).context(context).build();

        CompletableFuture<OperationOutcome> future = oper.startOperationAsync(params, 1, params.makeOutcome());

        return future.get(5, TimeUnit.SECONDS);
    }


    private class MyOper extends SdncOperator {

        /**
         * Set to {@code true} to cause the decoder to throw an exception.
         */
        @Setter
        private boolean decodeFailure = false;

        public MyOper() {
            super(ACTOR, OPERATION);
        }

        protected HttpClient getTheClient() {
            return getClient();
        }

        @Override
        protected SdncRequest constructRequest(ControlLoopEventContext context) {
            SdncRequest request = new SdncRequest();

            SdncHealRequest heal = new SdncHealRequest();
            request.setHealRequest(heal);

            return request;
        }

        @Override
        protected StandardCoder makeDecoder() {
            if (decodeFailure) {
                // return a coder that throws exceptions when decode() is invoked
                return new StandardCoder() {
                    @Override
                    public <T> T decode(String json, Class<T> clazz) throws CoderException {
                        throw new CoderException(EXPECTED_EXCEPTION);
                    }
                };

            } else {
                return super.makeDecoder();
            }
        }
    }

    /**
     * SDNC Simulator.
     */
    @Path("/sdnc")
    @Produces(MEDIA_TYPE_APPLICATION_JSON)
    public static class Server {

        /**
         * Generates a response.
         *
         * @param request incoming request
         * @return resulting response
         */
        @POST
        @Path("/request")
        @Consumes(value = {MEDIA_TYPE_APPLICATION_JSON})
        public Response postRequest(SdncRequest request) {

            SdncResponse response = new SdncResponse();
            response.setResponseOutput(output);

            return Response.status(Status.OK).entity(response).build();
        }
    }
}
