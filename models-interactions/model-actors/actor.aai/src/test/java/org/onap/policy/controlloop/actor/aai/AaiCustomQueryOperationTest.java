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
import javax.ws.rs.client.Entity;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.policy.PolicyResult;

public class AaiCustomQueryOperationTest extends BasicAaiOperation<Map<String, String>> {
    private static final StandardCoder coder = new StandardCoder();

    private static final String MY_LINK = "my-link";

    @Captor
    private ArgumentCaptor<Entity<Map<String, String>>> entityCaptor;

    @Mock
    private Actor tenantActor;

    private AaiCustomQueryOperation oper;

    public AaiCustomQueryOperationTest() {
        super(AaiConstants.ACTOR_NAME, AaiCustomQueryOperation.NAME);
    }

    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        super.setUpBasic();

        MyTenantOperator tenantOperator = new MyTenantOperator();

        when(service.getActor(AaiConstants.ACTOR_NAME)).thenReturn(tenantActor);
        when(tenantActor.getOperator(AaiGetOperation.TENANT)).thenReturn(tenantOperator);

        oper = new AaiCustomQueryOperation(params, config);
    }

    @Test
    public void testAaiCustomQueryOperation() {
        assertEquals(AaiConstants.ACTOR_NAME, oper.getActorName());
        assertEquals(AaiCustomQueryOperation.NAME, oper.getName());
    }

    @Test
    public void testStartOperationAsync_testStartPreprocessorAsync_testMakeRequest_testPostProcess() throws Exception {
        // need two responses
        when(rawResponse.readEntity(String.class)).thenReturn(makeTenantReply()).thenReturn(makeCqReply());
        when(client.get(any(), any(), any())).thenAnswer(provideResponse(rawResponse));
        when(client.put(any(), any(), any(), any())).thenAnswer(provideResponse(rawResponse));

        CompletableFuture<OperationOutcome> future2 = oper.start();

        assertEquals(PolicyResult.SUCCESS, getResult(future2));

        // tenant response should have been cached within the context
        assertNotNull(context.getProperty(AaiGetOperation.getTenantKey(TARGET_ENTITY)));

        // custom query response should have been cached within the context
        AaiCqResponse cqData = context.getProperty(AaiCqResponse.CONTEXT_KEY);
        assertNotNull(cqData);
    }

    /**
     * Tests when preprocessor step is not needed.
     */
    @Test
    public void testStartOperationAsync_testStartPreprocessorAsyncNotNeeded() throws Exception {
        // pre-load the tenant data
        final StandardCoderObject data = preloadTenantData();

        // only need one response
        when(rawResponse.readEntity(String.class)).thenReturn(makeCqReply());
        when(client.put(any(), any(), any(), any())).thenAnswer(provideResponse(rawResponse));

        CompletableFuture<OperationOutcome> future2 = oper.start();

        assertEquals(PolicyResult.SUCCESS, getResult(future2));

        // should not have replaced tenant response
        assertSame(data, context.getProperty(AaiGetOperation.getTenantKey(TARGET_ENTITY)));

        // custom query response should have been cached within the context
        AaiCqResponse cqData = context.getProperty(AaiCqResponse.CONTEXT_KEY);
        assertNotNull(cqData);
    }

    @Test
    public void testMakeHeaders() {
        verifyHeaders(oper.makeHeaders());
    }

    @Test
    public void testMakeRequest() throws Exception {
        // preload
        preloadTenantData();

        when(rawResponse.readEntity(String.class)).thenReturn(makeCqReply());
        when(client.put(any(), any(), any(), any())).thenAnswer(provideResponse(rawResponse));

        oper.start();
        executor.runAll(100);

        verify(client).put(any(), any(), entityCaptor.capture(), any());

        // sort the request fields so they match the order in cq.json
        Map<String, String> request = new TreeMap<>(entityCaptor.getValue().getEntity());

        verifyRequest("cq.json", request);
    }

    @Test
    public void testMakeRequestNoResourceLink() throws Exception {
        // pre-load EMPTY tenant data
        preloadTenantData(new StandardCoderObject());

        when(rawResponse.readEntity(String.class)).thenReturn(makeCqReply());
        when(client.put(any(), any(), any(), any())).thenAnswer(provideResponse(rawResponse));

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
        context.setProperty(AaiGetOperation.getTenantKey(TARGET_ENTITY), data);
    }

    private PolicyResult getResult(CompletableFuture<OperationOutcome> future2)
                    throws InterruptedException, ExecutionException, TimeoutException {

        executor.runAll(100);
        assertTrue(future2.isDone());

        return future2.get().getResult();
    }

    protected class MyTenantOperator extends HttpOperator {
        public MyTenantOperator() {
            super(AaiConstants.ACTOR_NAME, AaiGetOperation.TENANT);

            HttpParams http = HttpParams.builder().clientName(MY_CLIENT).path(PATH).timeoutSec(1).build();

            configure(Util.translateToMap(AaiGetOperation.TENANT, http));
            start();
        }

        @Override
        public Operation buildOperation(ControlLoopOperationParams params) {
            return new AaiGetOperation(params, getCurrentConfig());
        }

        @Override
        protected HttpClientFactory getClientFactory() {
            return factory;
        }
    }
}
