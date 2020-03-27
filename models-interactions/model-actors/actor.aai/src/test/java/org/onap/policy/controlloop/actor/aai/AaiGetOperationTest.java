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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.InvocationCallback;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.policy.PolicyResult;

public class AaiGetOperationTest extends BasicAaiOperation<Void> {
    private static final String INPUT_FIELD = "input";
    private static final String TEXT = "my-text";

    private AaiGetOperation oper;

    public AaiGetOperationTest() {
        super(AaiConstants.ACTOR_NAME, AaiGetOperation.TENANT);
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
        oper = new AaiGetOperation(params, config);
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    public void testSuccess() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT).path("v16/search/nodes-query").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().targetEntity("OzVServer").retry(0).timeoutSec(5).executor(blockingExecutor).build();
        oper = new AaiGetOperation(params, config);

        outcome = oper.start().get();
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests "failure" case with simulator.
     */
    @Test
    public void testFailure() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT).path("v16/search/nodes-query").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().targetEntity("failedVserver").retry(0).timeoutSec(5).executor(blockingExecutor)
                        .build();
        oper = new AaiGetOperation(params, config);

        outcome = oper.start().get();
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
    }

    @Test
    public void testGetRetry() {
        // use default if null retry
        assertEquals(AaiGetOperation.DEFAULT_RETRY, oper.getRetry(null));

        // otherwise, use specified value
        assertEquals(0, oper.getRetry(0));
        assertEquals(10, oper.getRetry(10));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testStartOperationAsync_testStartQueryAsync_testPostProcessResponse() throws Exception {

        // return a map in the reply
        Map<String, String> reply = Map.of(INPUT_FIELD, TEXT);
        when(rawResponse.readEntity(String.class)).thenReturn(new StandardCoder().encode(reply));

        when(webAsync.get(any(InvocationCallback.class))).thenAnswer(provideResponse(rawResponse));

        CompletableFuture<OperationOutcome> future2 = oper.startOperationAsync(1, outcome);
        assertFalse(future2.isDone());

        executor.runAll(100);
        assertTrue(future2.isDone());

        assertEquals(PolicyResult.SUCCESS, future2.get().getResult());

        // data should have been cached within the context
        StandardCoderObject data = context.getProperty(AaiGetOperation.getTenantKey(TARGET_ENTITY));
        assertNotNull(data);
        assertEquals(TEXT, data.getString(INPUT_FIELD));

        assertEquals("1", future2.get().getSubRequestId());
    }

    /**
     * Tests startOperationAsync() when there's a failure.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testStartOperationAsyncFailure() throws Exception {

        when(rawResponse.getStatus()).thenReturn(500);
        when(rawResponse.readEntity(String.class)).thenReturn("");

        when(webAsync.get(any(InvocationCallback.class))).thenAnswer(provideResponse(rawResponse));

        CompletableFuture<OperationOutcome> future2 = oper.startOperationAsync(1, outcome);
        assertFalse(future2.isDone());

        executor.runAll(100);
        assertTrue(future2.isDone());

        assertEquals(PolicyResult.FAILURE, future2.get().getResult());

        // data should NOT have been cached within the context
        assertNull(context.getProperty(AaiGetOperation.getTenantKey(TARGET_ENTITY)));
    }

    @Test
    public void testMakeHeaders() {
        verifyHeaders(oper.makeHeaders());
    }

    @Test
    public void testAaiGetOperator() {
        assertEquals(AaiConstants.ACTOR_NAME, oper.getActorName());
        assertEquals(AaiGetOperation.TENANT, oper.getName());
    }

    @Test
    public void testGetTenantKey() {
        assertEquals("AAI.Tenant." + TARGET_ENTITY, AaiGetOperation.getTenantKey(TARGET_ENTITY));
    }
}
