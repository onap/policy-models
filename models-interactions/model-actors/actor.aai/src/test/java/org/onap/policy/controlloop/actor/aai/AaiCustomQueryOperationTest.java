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

package org.onap.policy.controlloop.actor.aai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.client.InvocationCallback;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.policy.PolicyResult;

public class AaiCustomQueryOperationTest extends BasicAaiOperation<Map<String, String>> {
    private static final StandardCoder coder = new StandardCoder();

    private static final String MY_LINK = "my-link";
    private static final String MY_VSERVER = "my-vserver-name";
    private static final String SIM_VSERVER = "OzVServer";

    @Mock
    private Actor tenantActor;

    private AaiCustomQueryOperation oper;

    public AaiCustomQueryOperationTest() {
        super(AaiConstants.ACTOR_NAME, AaiCustomQueryOperation.NAME);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        initBeforeClass();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        destroyAfterClass();
    }

    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        super.setUpBasic();

        params.getContext().getEnrichment().put(AaiCustomQueryOperation.VSERVER_VSERVER_NAME, MY_VSERVER);

        MyTenantOperator tenantOperator = new MyTenantOperator();

        when(service.getActor(AaiConstants.ACTOR_NAME)).thenReturn(tenantActor);
        when(tenantActor.getOperator(AaiGetTenantOperation.NAME)).thenReturn(tenantOperator);

        oper = new AaiCustomQueryOperation(params, config);
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    public void testSuccess() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT).path("v16/query").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        preloadTenantData();

        params = params.toBuilder().targetEntity(SIM_VSERVER).retry(0).timeoutSec(5).executor(blockingExecutor).build();
        oper = new AaiCustomQueryOperation(params, config);

        outcome = oper.start().get();
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());

        String resp = outcome.getResponse();
        assertThat(resp).isNotNull().contains("relationship-list");
    }

    @Test
    public void testConstructor() {
        assertEquals(AaiConstants.ACTOR_NAME, oper.getActorName());
        assertEquals(AaiCustomQueryOperation.NAME, oper.getName());
        assertEquals(MY_VSERVER, oper.getVserver());

        // verify that it works with an empty target entity
        params = params.toBuilder().targetEntity("").build();
        assertThatCode(() -> new AaiCustomQueryOperation(params, config)).doesNotThrowAnyException();

        // try without enrichment data
        params.getContext().getEnrichment().remove(AaiCustomQueryOperation.VSERVER_VSERVER_NAME);
        assertThatIllegalArgumentException().isThrownBy(() -> new AaiCustomQueryOperation(params, config))
                        .withMessage("missing " + AaiCustomQueryOperation.VSERVER_VSERVER_NAME + " in enrichment data");
    }

    @Test
    public void testGenerateSubRequestId() {
        oper.generateSubRequestId(3);
        assertEquals("3", oper.getSubRequestId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testStartOperationAsync_testStartPreprocessorAsync_testMakeRequest_testPostProcess() throws Exception {
        // need two responses
        when(rawResponse.readEntity(String.class)).thenReturn(makeTenantReply()).thenReturn(makeCqReply());
        when(webAsync.get(any(InvocationCallback.class))).thenAnswer(provideResponse(rawResponse));
        when(webAsync.put(any(), any(InvocationCallback.class))).thenAnswer(provideResponse(rawResponse, 1));

        CompletableFuture<OperationOutcome> future2 = oper.start();

        assertEquals(PolicyResult.SUCCESS, getResult(future2));

        // tenant response should have been cached within the context
        assertNotNull(context.getProperty(AaiGetTenantOperation.getKey(MY_VSERVER)));

        // custom query response should have been cached within the context
        AaiCqResponse cqData = context.getProperty(AaiCqResponse.CONTEXT_KEY);
        assertNotNull(cqData);

        assertEquals("1", future2.get().getSubRequestId());
    }

    /**
     * Tests when preprocessor step is not needed.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testStartOperationAsync_testStartPreprocessorAsyncNotNeeded() throws Exception {
        // pre-load the tenant data
        final StandardCoderObject data = preloadTenantData();

        // only need one response
        when(rawResponse.readEntity(String.class)).thenReturn(makeCqReply());
        when(webAsync.put(any(), any(InvocationCallback.class))).thenAnswer(provideResponse(rawResponse, 1));

        CompletableFuture<OperationOutcome> future2 = oper.start();

        assertEquals(PolicyResult.SUCCESS, getResult(future2));

        // should not have replaced tenant response
        assertSame(data, context.getProperty(AaiGetTenantOperation.getKey(MY_VSERVER)));

        // custom query response should have been cached within the context
        AaiCqResponse cqData = context.getProperty(AaiCqResponse.CONTEXT_KEY);
        assertNotNull(cqData);
    }

    @Test
    public void testMakeHeaders() {
        verifyHeaders(oper.makeHeaders());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMakeRequest() throws Exception {
        // preload
        preloadTenantData();

        when(rawResponse.readEntity(String.class)).thenReturn(makeCqReply());
        when(webAsync.put(any(), any(InvocationCallback.class))).thenAnswer(provideResponse(rawResponse, 1));

        oper.start();
        executor.runAll(100);

        verify(webAsync).put(requestCaptor.capture(), any(InvocationCallback.class));

        String reqText = requestCaptor.getValue().getEntity();
        Map<String, String> reqMap = coder.decode(reqText, Map.class);

        // sort the request fields so they match the order in cq.json
        Map<String, String> request = new TreeMap<>(reqMap);

        verifyRequest("cq.json", request);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMakeRequestNoResourceLink() throws Exception {
        // pre-load EMPTY tenant data
        preloadTenantData(new StandardCoderObject());

        when(rawResponse.readEntity(String.class)).thenReturn(makeCqReply());
        when(webAsync.put(any(), any(InvocationCallback.class))).thenAnswer(provideResponse(rawResponse, 1));

        CompletableFuture<OperationOutcome> future2 = oper.start();

        assertEquals(PolicyResult.FAILURE_EXCEPTION, getResult(future2));
    }

    private String makeTenantReply() throws Exception {
        Map<String, String> links = Map.of(AaiCustomQueryOperation.RESOURCE_LINK, MY_LINK);
        List<Map<String, String>> data = Arrays.asList(links);

        Map<String, Object> reply = Map.of(AaiCustomQueryOperation.RESULT_DATA, data);
        return coder.encode(reply);
    }

    private String makeCqReply() {
        return "{}";
    }

    private StandardCoderObject preloadTenantData() throws Exception {
        StandardCoderObject data = coder.decode(makeTenantReply(), StandardCoderObject.class);
        preloadTenantData(data);
        return data;
    }

    private void preloadTenantData(StandardCoderObject data) {
        context.setProperty(AaiGetTenantOperation.getKey(MY_VSERVER), data);
        context.setProperty(AaiGetTenantOperation.getKey(SIM_VSERVER), data);
    }

    private PolicyResult getResult(CompletableFuture<OperationOutcome> future2)
                    throws InterruptedException, ExecutionException, TimeoutException {

        executor.runAll(100);
        assertTrue(future2.isDone());

        return future2.get().getResult();
    }

    protected class MyTenantOperator extends HttpOperator {
        public MyTenantOperator() {
            super(AaiConstants.ACTOR_NAME, AaiGetTenantOperation.NAME);

            HttpParams http = HttpParams.builder().clientName(MY_CLIENT).path(PATH).timeoutSec(1).build();

            configure(Util.translateToMap(AaiGetTenantOperation.NAME, http));
            start();
        }

        @Override
        public HttpOperation<?> buildOperation(ControlLoopOperationParams params) {
            return new AaiGetTenantOperation(params, getCurrentConfig());
        }

        @Override
        protected HttpClientFactory getClientFactory() {
            return factory;
        }
    }
}
