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

package org.onap.policy.controlloop.actor.xacml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.controlloop.actor.test.BasicHttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;
import org.onap.policy.simulators.XacmlSimulatorJaxRs;

@ExtendWith(MockitoExtension.class)
 class DecisionOperationTest extends BasicHttpOperation {
    private static final List<String> PROPERTY_NAMES = List.of("prop-A", "prop-B");

    @Mock
    private Consumer<OperationOutcome> started;
    @Mock
    private Consumer<OperationOutcome> completed;

    private DecisionConfig guardConfig;
    private MyOper oper;

    /**
     * Starts the simulator.
     */
    @BeforeAll
     static void setUpBeforeClass() throws Exception {
        org.onap.policy.simulators.Util.buildXacmlSim();

        BusTopicParams clientParams = BusTopicParams.builder().clientName(MY_CLIENT).basePath("policy/pdpx/v1/")
                        .hostname("localhost").managed(true).port(org.onap.policy.simulators.Util.XACMLSIM_SERVER_PORT)
                        .build();
        HttpClientFactoryInstance.getClientFactory().build(clientParams);
    }

    @AfterAll
     static void tearDownAfterClass() {
        HttpClientFactoryInstance.getClientFactory().destroy();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    /**
     * Sets up.
     */
    @BeforeEach
     void setUp() throws Exception {
        super.setUpBasic();

        guardConfig = mock(DecisionConfig.class);
        Mockito.lenient().when(guardConfig.makeRequest()).thenAnswer(args -> {
            DecisionRequest req = new DecisionRequest();
            req.setAction("guard");
            req.setOnapComponent("my-onap-component");
            req.setOnapInstance("my-onap-instance");
            req.setOnapName("my-onap-name");
            return req;
        });

        config = guardConfig;
        initConfig();

        params = params.toBuilder().startCallback(started).completeCallback(completed).build();

        oper = new MyOper(params, config);
    }

    /**
     * Tests with simulator.
     */
    @Test
     void testSimulator() throws Exception {
        DecisionParams opParams = DecisionParams.builder().clientName(MY_CLIENT).path("decision").build();
        config = new DecisionConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor)
                        .payload(Map.of("clname", XacmlSimulatorJaxRs.DENY_CLNAME)).build();
        oper = new MyOper(params, config);

        outcome = oper.start().get();
        assertEquals(OperationResult.FAILURE, outcome.getResult());
        assertInstanceOf(DecisionResponse.class, outcome.getResponse());
    }

    @Test
     void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
    }

    @Test
     void testGetPropertyNames() {
        assertThat(oper.getPropertyNames()).isEqualTo(PROPERTY_NAMES);
    }

    @Test
     void testStartOperationAsync() throws Exception {
        CompletableFuture<OperationOutcome> future2 = oper.start();
        executor.runAll(100);
        assertFalse(future2.isDone());

        DecisionResponse resp = new DecisionResponse();
        resp.setStatus(GuardOperation.PERMIT);
        when(rawResponse.readEntity(String.class)).thenReturn(Util.translate("", resp, String.class));

        verify(client).post(callbackCaptor.capture(), any(), requestCaptor.capture(), any());
        callbackCaptor.getValue().completed(rawResponse);

        executor.runAll(100);
        assertTrue(future2.isDone());

        outcome = future2.get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals(resp, outcome.getResponse());

        assertNotNull(oper.getSubRequestId());
        assertEquals(oper.getSubRequestId(), future2.get().getSubRequestId());
    }

    /**
     * Tests startOperationAsync() when the guard is disabled.
     */
    @Test
     void testStartOperationAsyncDisabled() throws Exception {
        // indicate that it's disabled
        when(guardConfig.isDisabled()).thenReturn(true);

        CompletableFuture<OperationOutcome> future2 = oper.start();
        executor.runAll(100);

        verify(client, never()).post(any(), any(), any(), any());

        // should already be done
        assertTrue(future2.isDone());

        outcome = future2.get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertNull(outcome.getResponse());

        // ensure callbacks were invoked
        verify(started).accept(any());
        verify(completed).accept(any());
    }

    private class MyOper extends DecisionOperation {

        MyOper(ControlLoopOperationParams params, HttpConfig config) {
            super(params, config, PROPERTY_NAMES);
        }

        @Override
        protected DecisionRequest makeRequest() {
            return guardConfig.makeRequest();
        }
    }
}
