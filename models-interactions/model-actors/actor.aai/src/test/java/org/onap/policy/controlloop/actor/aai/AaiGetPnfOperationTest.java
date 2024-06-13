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

package org.onap.policy.controlloop.actor.aai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.client.InvocationCallback;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;

@ExtendWith(MockitoExtension.class)
class AaiGetPnfOperationTest extends BasicAaiOperation {
    private static final String INPUT_FIELD = "input";
    private static final String TEXT = "my-text";

    private AaiGetPnfOperation oper;

    AaiGetPnfOperationTest() {
        super(AaiConstants.ACTOR_NAME, AaiGetPnfOperation.NAME);
    }

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        initBeforeClass();
    }

    @AfterAll
    static void tearDownAfterClass() {
        destroyAfterClass();
    }

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() throws Exception {
        super.setUpBasic();
        oper = new AaiGetPnfOperation(params, config);
        oper.setProperty(OperationProperties.AAI_TARGET_ENTITY, TARGET_ENTITY);
    }

    @Test
    void testConstructor() {
        assertEquals(AaiConstants.ACTOR_NAME, oper.getActorName());
        assertEquals(AaiGetPnfOperation.NAME, oper.getName());
    }

    @Test
    void testGetPropertyNames() {
        assertThat(oper.getPropertyNames()).isEqualTo(List.of(OperationProperties.AAI_TARGET_ENTITY));
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    void testSuccess() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT).path("v16/network/pnfs/pnf").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();
        oper = new AaiGetPnfOperation(params, config);
        oper.setProperty(OperationProperties.AAI_TARGET_ENTITY, "demo-pnf");

        outcome = oper.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertInstanceOf(StandardCoderObject.class, outcome.getResponse());
    }

    /**
     * Tests "failure" case with simulator.
     */
    @Test
    void testFailure() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT).path("v16/network/pnfs/pnf").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();
        oper = new AaiGetPnfOperation(params, config);
        oper.setProperty(OperationProperties.AAI_TARGET_ENTITY, "getFail");

        outcome = oper.start().get();
        assertEquals(OperationResult.FAILURE, outcome.getResult());
    }

    @Test
    @SuppressWarnings("unchecked")
    void testStartOperationAsync_testStartQueryAsync() throws Exception {

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

        assertEquals(OperationResult.SUCCESS, future2.get().getResult());

        assertEquals("1", future2.get().getSubRequestId());
    }

    /**
     * Tests startOperationAsync() when there's a failure.
     */
    @Test
    @SuppressWarnings("unchecked")
    void testStartOperationAsyncFailure() throws Exception {

        when(rawResponse.getStatus()).thenReturn(500);
        when(rawResponse.readEntity(String.class)).thenReturn("");

        when(webAsync.get(any(InvocationCallback.class))).thenAnswer(provideResponse(rawResponse));

        CompletableFuture<OperationOutcome> future2 = oper.startOperationAsync(1, outcome);
        assertFalse(future2.isDone());

        executor.runAll(100);
        assertTrue(future2.isDone());

        assertEquals(OperationResult.FAILURE, future2.get().getResult());
    }

    /**
     * Tests startOperationAsync() when a property is missing.
     */
    @Test
    void testStartOperationAsyncMissingProperty() throws Exception {
        oper = new AaiGetPnfOperation(params, config);

        oper.generateSubRequestId(1);
        outcome.setSubRequestId(oper.getSubRequestId());

        assertThatIllegalStateException().isThrownBy(() -> oper.startOperationAsync(1, outcome))
                        .withMessageContaining("missing target entity");
    }

    @Test
    void testGetKey() {
        assertEquals("AAI.Pnf." + TARGET_ENTITY, AaiGetPnfOperation.getKey(TARGET_ENTITY));
    }
}
