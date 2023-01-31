/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;

@RunWith(MockitoJUnitRunner.class)
public class AaiCustomQueryOperationTest extends BasicAaiOperation {
    private static final StandardCoder coder = new StandardCoder();

    private static final String MY_LINK = "my-link";

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

        oper = new AaiCustomQueryOperation(params, config);
        oper.setProperty(OperationProperties.AAI_VSERVER_LINK, MY_LINK);
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    public void testSuccess() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT).path("v16/query").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();
        oper = new AaiCustomQueryOperation(params, config);

        oper.setProperty(OperationProperties.AAI_VSERVER_LINK, MY_LINK);

        outcome = oper.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());

        assertNotNull(outcome.getResponse());
    }

    @Test
    public void testConstructor() {
        assertEquals(AaiConstants.ACTOR_NAME, oper.getActorName());
        assertEquals(AaiCustomQueryOperation.NAME, oper.getName());
    }

    @Test
    public void testGetPropertyNames() {
        assertThat(oper.getPropertyNames()).isEqualTo(List.of(OperationProperties.AAI_VSERVER_LINK));
    }

    @Test
    public void testGenerateSubRequestId() {
        oper.generateSubRequestId(3);
        assertEquals("3", oper.getSubRequestId());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testStartOperationAsync_testMakeRequest() throws Exception {
        // need two responses
        when(rawResponse.readEntity(String.class)).thenReturn(makeTenantReply()).thenReturn(makeCqReply());
        lenient().when(webAsync.get(any(InvocationCallback.class))).thenAnswer(provideResponse(rawResponse));
        when(webAsync.put(any(), any(InvocationCallback.class))).thenAnswer(provideResponse(rawResponse, 1));

        CompletableFuture<OperationOutcome> future2 = oper.start();

        assertEquals(OperationResult.SUCCESS, getResult(future2));

        assertEquals("1", future2.get().getSubRequestId());
    }

    @Test
    public void testMakeHeaders() {
        verifyHeaders(oper.makeHeaders());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMakeRequest_testGetVserverLink() throws Exception {
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
    public void testGetVserverLink() throws Exception {
        oper.setProperty(OperationProperties.AAI_VSERVER_LINK, MY_LINK);
        assertEquals(MY_LINK, oper.getVserverLink());
    }

    @Test
    public void testSetOutcome() {
        outcome = oper.setOutcome(params.makeOutcome(), OperationResult.SUCCESS, null, null);
        assertNull(outcome.getResponse());

        outcome = oper.setOutcome(params.makeOutcome(), OperationResult.SUCCESS, null, "{}");
        assertTrue(outcome.getResponse() instanceof AaiCqResponse);
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


    private OperationResult getResult(CompletableFuture<OperationOutcome> future2)
                    throws InterruptedException, ExecutionException, TimeoutException {

        executor.runAll(100);
        assertTrue(future2.isDone());

        return future2.get().getResult();
    }
}
