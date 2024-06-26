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

package org.onap.policy.controlloop.actor.sdnc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.sdnc.SdncResponse;

@ExtendWith(MockitoExtension.class)
class RerouteOperationTest extends BasicSdncOperation {
    private static final String MY_SERVICE = "my-service";
    private static final String MY_NETWORK = "my-network";

    private RerouteOperation oper;

    RerouteOperationTest() {
        super(DEFAULT_ACTOR, RerouteOperation.NAME);
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
     * Set up.
     */
    @Override
    @BeforeEach
     void setUp() throws Exception {
        super.setUp();
        oper = new RerouteOperation(params, config);
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
     void testSuccess() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT)
                        .path("GENERIC-RESOURCE-API:network-topology-operation").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();
        oper = new RerouteOperation(params, config);

        oper.setProperty(OperationProperties.ENRICHMENT_SERVICE_ID, MY_SERVICE);
        oper.setProperty(OperationProperties.ENRICHMENT_NETWORK_ID, MY_NETWORK);

        outcome = oper.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertInstanceOf(SdncResponse.class, outcome.getResponse());
    }

    @Test
     void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(RerouteOperation.NAME, oper.getName());
    }

    @Test
     void testGetPropertyNames() {
        // @formatter:off
        assertThat(oper.getPropertyNames()).isEqualTo(
                        List.of(
                            OperationProperties.ENRICHMENT_SERVICE_ID,
                            OperationProperties.ENRICHMENT_NETWORK_ID));
        // @formatter:on
    }

    @Test
     void testMakeRequest() throws Exception {
        oper.setProperty(OperationProperties.ENRICHMENT_SERVICE_ID, MY_SERVICE);
        oper.setProperty(OperationProperties.ENRICHMENT_NETWORK_ID, MY_NETWORK);

        verifyRequest("reroute.json", verifyOperation(oper), IGNORE_FIELDS);
    }
}
