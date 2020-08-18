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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
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
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.policy.PolicyResult;

public class AaiGetPnfOperationTest extends BasicAaiOperation {
    private static final String INPUT_FIELD = "input";
    private static final String TEXT = "my-text";

    private AaiGetPnfOperation oper;

    public AaiGetPnfOperationTest() {
        super(AaiConstants.ACTOR_NAME, AaiGetPnfOperation.NAME);
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
        oper = new AaiGetPnfOperation(params, config);
    }

    @Test
    public void testConstructor() {
        assertEquals(AaiConstants.ACTOR_NAME, oper.getActorName());
        assertEquals(AaiGetPnfOperation.NAME, oper.getName());
    }

    @Test
    public void testGetPropertyNames() {
        assertThat(oper.getPropertyNames()).isEqualTo(List.of(OperationProperties.AAI_TARGET_ENTITY));
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    public void testSuccess() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT).path("v16/network/pnfs/pnf").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();
        oper = new AaiGetPnfOperation(params, config);
        oper.setProperty(OperationProperties.AAI_TARGET_ENTITY, "OzVServer");

        outcome = oper.start().get();
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof StandardCoderObject);
    }

    /**
     * Tests "failure" case with simulator.
     */
    @Test
    public void testFailure() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT).path("v16/network/pnfs/pnf").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();
        oper = new AaiGetPnfOperation(params, config);
        oper.setProperty(OperationProperties.AAI_TARGET_ENTITY, "getFail");

        outcome = oper.start().get();
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testStartOperationAsync_testStartQueryAsync() throws Exception {

        // return a map in the reply
        Map<String, String> reply = Map.of(INPUT_FIELD, TEXT);
        when(rawResponse.readEntity(String.class)).thenReturn(new StandardCoder().encode(reply));

        when(webAsync.get(any(InvocationCallback.class))).thenAnswer(provideResponse(rawResponse));

        oper.generateSubRequestId(1);
        outcome.setSubRequestId(oper.getSubRequestId());

        CompletableFuture<OperationOutcome> future2 = oper.startOperationAsync(1, outcome);
        assertFalse(future2.isDone());

        executor.runAll(100);
        assertTrue(future2.isDone());

        assertEquals(PolicyResult.SUCCESS, future2.get().getResult());

        // data should have been cached within the context
        StandardCoderObject data = context.getProperty(AaiGetPnfOperation.getKey(TARGET_ENTITY));
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
        assertNull(context.getProperty(AaiGetPnfOperation.getKey(TARGET_ENTITY)));
    }

    @Test
    public void testGetKey() {
        assertEquals("AAI.Pnf." + TARGET_ENTITY, AaiGetPnfOperation.getKey(TARGET_ENTITY));
    }
}
