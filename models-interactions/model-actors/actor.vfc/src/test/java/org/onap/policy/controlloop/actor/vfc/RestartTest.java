/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.vfc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingParams;
import org.onap.policy.vfc.VfcRequest;
import org.onap.policy.vfc.VfcResponse;

@ExtendWith(MockitoExtension.class)
 class RestartTest extends BasicVfcOperation {
    private static final String TEST_SERVICE_INSTANCE_ID = "test-service-instance-id";
    private static final String TEST_VSERVER_ID = "test-vserver-id";
    private static final String TEST_VSERVER_NAME = "test-vserver-name";
    private static final String TEST_GENERIC_VNF_ID = "test-generic-vnf-id";

    private Restart restartOper;


    @BeforeAll
     static void setUpBeforeClass() throws Exception {
        initBeforeClass();
    }

    @AfterAll
     static void tearDownAfterClass() {
        destroyAfterClass();
    }

    /**
     * setup restart operation.
     */
    @BeforeEach
     void setup() throws Exception {
        super.setUp();

        restartOper = new Restart(params, config);

        loadProperties();
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
     void testSuccess() throws Exception {
        HttpPollingParams opParams = HttpPollingParams.builder().clientName(MY_CLIENT).path("ns").pollPath("jobs")
                        .maxPolls(1).build();
        config = new HttpPollingConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();

        restartOper = new Restart(params, config);

        loadProperties();

        // run the operation
        outcome = restartOper.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof VfcResponse);
    }

    @Test
     void testConstructor() {
        CompletableFuture<OperationOutcome> futureRes = restartOper.startOperationAsync(1, outcome);
        assertNotNull(futureRes);
        assertEquals(0, restartOper.getPollCount());
    }

    @Test
     void testGetPropertyNames() {
        // @formatter:off
        assertThat(restartOper.getPropertyNames()).isEqualTo(
                        List.of(
                            OperationProperties.ENRICHMENT_SERVICE_ID,
                            OperationProperties.ENRICHMENT_VSERVER_ID,
                            OperationProperties.ENRICHMENT_VSERVER_NAME,
                            OperationProperties.ENRICHMENT_GENERIC_VNF_ID));
        // @formatter:on
    }

    @Test
     void testMakeRequest() {
        Pair<String, VfcRequest> resultPair = restartOper.makeRequest();
        assertNotNull(resultPair.getLeft());
        assertNotNull(resultPair.getRight());
    }

    private void loadProperties() {
        restartOper.setProperty(OperationProperties.ENRICHMENT_SERVICE_ID, TEST_SERVICE_INSTANCE_ID);
        restartOper.setProperty(OperationProperties.ENRICHMENT_VSERVER_ID, TEST_VSERVER_ID);
        restartOper.setProperty(OperationProperties.ENRICHMENT_VSERVER_NAME, TEST_VSERVER_NAME);
        restartOper.setProperty(OperationProperties.ENRICHMENT_GENERIC_VNF_ID, TEST_GENERIC_VNF_ID);
    }
}
