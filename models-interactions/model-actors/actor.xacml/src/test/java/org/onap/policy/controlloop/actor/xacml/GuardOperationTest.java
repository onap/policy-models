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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.parameters.topic.BusTopicParams;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actor.test.BasicHttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;
import org.onap.policy.simulators.XacmlSimulatorJaxRs;

@ExtendWith(MockitoExtension.class)
class GuardOperationTest extends BasicHttpOperation {

    @Mock
    private Consumer<OperationOutcome> started;
    @Mock
    private Consumer<OperationOutcome> completed;

    private DecisionConfig guardConfig;
    private GuardOperation oper;

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
    void setUp() {
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

        oper = new GuardOperation(params, config);
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    void testSuccess() throws Exception {
        DecisionParams opParams =
            DecisionParams.builder().clientName(MY_CLIENT).path("decision").action("guard").build();
        config = new DecisionConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();
        oper = new GuardOperation(params, config);

        outcome = oper.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertInstanceOf(DecisionResponse.class, outcome.getResponse());
    }

    /**
     * Tests "failure" case with simulator.
     */
    @Test
    void testFailure() throws Exception {
        DecisionParams opParams =
            DecisionParams.builder().clientName(MY_CLIENT).path("decision").action("guard").build();
        config = new DecisionConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor)
            .payload(Map.of("clname", XacmlSimulatorJaxRs.DENY_CLNAME)).build();
        oper = new GuardOperation(params, config);

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
        assertThat(oper.getPropertyNames()).isEmpty();
    }

    @Test
    void testMakeRequest() throws CoderException {
        oper.generateSubRequestId(2);

        verifyPayload("makeReqStd.json", makePayload());
        verifyPayload("makeReqDefault.json", new TreeMap<>());

        // null payload - start with fresh parameters and operation
        params = params.toBuilder().payload(null).build();
        oper = new GuardOperation(params, config);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.makeRequest());
    }

    private void verifyPayload(String expectedJsonFile, Map<String, Object> payload) throws CoderException {
        params.getPayload().clear();
        params.getPayload().putAll(payload);

        DecisionRequest request = oper.makeRequest();

        assertEquals("guard", request.getAction());
        assertEquals("my-onap-component", request.getOnapComponent());
        assertEquals("my-onap-instance", request.getOnapInstance());
        assertEquals("my-onap-name", request.getOnapName());
        assertNotNull(request.getRequestId());
        assertEquals(Map.of("guard", payload), request.getResource());

        verifyRequest(expectedJsonFile, request, "requestId");
    }

    @Test
    void testPostProcessResponse() {
        DecisionResponse response = new DecisionResponse();

        // null status
        response.setStatus(null);
        verifyOutcome(response, OperationResult.FAILURE, "response contains no status");

        // permit, mixed case
        response.setStatus("peRmit");
        verifyOutcome(response, OperationResult.SUCCESS, "peRmit");

        // indeterminate, mixed case
        response.setStatus("inDETerminate");
        verifyOutcome(response, OperationResult.SUCCESS, "inDETerminate");

        // deny, mixed case
        response.setStatus("deNY");
        verifyOutcome(response, OperationResult.FAILURE, "deNY");

        // unknown status
        response.setStatus("unknown");
        verifyOutcome(response, OperationResult.FAILURE, "unknown");
    }

    private void verifyOutcome(DecisionResponse response, OperationResult expectedResult, String expectedMessage) {
        oper.postProcessResponse(outcome, BASE_URI, rawResponse, response);
        assertEquals(expectedResult, outcome.getResult());
        assertEquals(expectedMessage, outcome.getMessage());
        assertSame(response, outcome.getResponse());
    }

    @Override
    protected Map<String, Object> makePayload() {
        return new TreeMap<>(Map.of("hello", "world", "abc", "123"));
    }
}
