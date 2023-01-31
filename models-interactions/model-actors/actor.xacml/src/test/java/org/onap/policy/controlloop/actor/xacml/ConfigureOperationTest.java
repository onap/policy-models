/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.xacml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.function.Consumer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.common.endpoints.event.comm.bus.internal.BusTopicParams;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.controlloop.actor.test.BasicHttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;

@RunWith(MockitoJUnitRunner.class)
public class ConfigureOperationTest extends BasicHttpOperation {

    @Mock
    private Consumer<OperationOutcome> started;
    @Mock
    private Consumer<OperationOutcome> completed;

    private DecisionConfig operConfig;
    private ConfigureOperation oper;

    /**
     * Starts the simulator.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        org.onap.policy.simulators.Util.buildXacmlSim();

        BusTopicParams clientParams = BusTopicParams.builder().clientName(MY_CLIENT).basePath("policy/pdpx/v1/")
                        .hostname("localhost").managed(true).port(org.onap.policy.simulators.Util.XACMLSIM_SERVER_PORT)
                        .build();
        HttpClientFactoryInstance.getClientFactory().build(clientParams);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        HttpClientFactoryInstance.getClientFactory().destroy();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        super.setUpBasic();

        operConfig = mock(DecisionConfig.class);
        lenient().when(operConfig.makeRequest()).thenAnswer(args -> {
            DecisionRequest req = new DecisionRequest();
            req.setAction("guard");
            req.setOnapComponent("my-onap-component");
            req.setOnapInstance("my-onap-instance");
            req.setOnapName("my-onap-name");
            return req;
        });

        config = operConfig;
        initConfig();

        params = params.toBuilder().startCallback(started).completeCallback(completed).build();

        oper = new ConfigureOperation(params, config);
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    public void testSuccess() throws Exception {
        DecisionParams opParams =
                        DecisionParams.builder().clientName(MY_CLIENT).path("decision").action("configure").build();
        config = new DecisionConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().payload(Map.of("policy-id", "test-policy")).retry(0).timeoutSec(5)
                        .executor(blockingExecutor).build();
        oper = new ConfigureOperation(params, config);

        outcome = oper.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());

        DecisionResponse response = outcome.getResponse();
        assertTrue(response instanceof DecisionResponse);
        assertNotNull(response.getPolicies());
        assertThat(response.getPolicies()).containsKey("test-policy");
    }

    /**
     * Tests "failure" case with simulator.
     */
    @Test
    public void testFailure() throws Exception {
        DecisionParams opParams =
                        DecisionParams.builder().clientName(MY_CLIENT).path("decision").action("configure").build();
        config = new DecisionConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().payload(Map.of("policy-id", "nonexistent")).retry(0).timeoutSec(5)
                        .executor(blockingExecutor).build();
        oper = new ConfigureOperation(params, config);

        outcome = oper.start().get();
        assertEquals(OperationResult.FAILURE, outcome.getResult());

        DecisionResponse response = outcome.getResponse();
        assertTrue(response instanceof DecisionResponse);
        assertNotNull(response.getPolicies());
        assertThat(response.getPolicies()).isEmpty();
    }

}
