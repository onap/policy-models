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

package org.onap.policy.controlloop.actor.sdnc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
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
class BandwidthOnDemandOperationTest extends BasicSdncOperation {
    private static final String MY_SERVICE = "my-service";
    private static final String MY_VNF = "my-vnf";
    private static final String MY_BANDWIDTH = "my-bandwidth";
    private static final String MY_CHANGE_TIME = "my-change-time";

    private BandwidthOnDemandOperation oper;

    BandwidthOnDemandOperationTest() {
        super(DEFAULT_ACTOR, BandwidthOnDemandOperation.NAME);
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
        oper = new BandwidthOnDemandOperation(params, config);
    }

    @Test
     void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(BandwidthOnDemandOperation.NAME, oper.getName());
    }

    @Test
     void testGetPropertyNames() {
        // @formatter:off
        assertThat(oper.getPropertyNames()).isEqualTo(
                        List.of(
                            OperationProperties.ENRICHMENT_SERVICE_ID,
                            OperationProperties.ENRICHMENT_BANDWIDTH,
                            OperationProperties.ENRICHMENT_BANDWIDTH_CHANGE_TIME,
                            OperationProperties.ENRICHMENT_VNF_ID));

        // @formatter:on
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
     void testSuccess() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT)
                        .path("GENERIC-RESOURCE-API:vf-module-topology-operation").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();
        oper = new BandwidthOnDemandOperation(params, config);

        oper.setProperty(OperationProperties.ENRICHMENT_SERVICE_ID, MY_SERVICE);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH, MY_BANDWIDTH);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH_CHANGE_TIME, MY_CHANGE_TIME);
        oper.setProperty(OperationProperties.ENRICHMENT_VNF_ID, MY_VNF);

        outcome = oper.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertInstanceOf(SdncResponse.class, outcome.getResponse());
    }

    @Test
     void testMakeRequest() throws Exception {
        oper.setProperty(OperationProperties.ENRICHMENT_SERVICE_ID, MY_SERVICE);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH, MY_BANDWIDTH);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH_CHANGE_TIME, MY_CHANGE_TIME);
        oper.setProperty(OperationProperties.ENRICHMENT_VNF_ID, MY_VNF);

        verifyRequest("bod.json", verifyOperation(oper), IGNORE_FIELDS);
    }

    /*
     * Tests makeRequest() when a property is missing.
     */

    @Test
     void testMakeRequestMissingBandwidth() throws Exception {
        oper = new BandwidthOnDemandOperation(params, config);
        oper.setProperty(OperationProperties.ENRICHMENT_SERVICE_ID, MY_SERVICE);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH_CHANGE_TIME, MY_CHANGE_TIME);
        oper.setProperty(OperationProperties.ENRICHMENT_VNF_ID, MY_VNF);

        oper.generateSubRequestId(1);
        outcome.setSubRequestId(oper.getSubRequestId());

        assertThatIllegalStateException().isThrownBy(() -> oper.makeRequest(1))
                        .withMessageContaining("missing bandwidth from enrichment data");
    }

    @Test
     void testMakeRequestMissingBandwidthChangeTime() throws Exception {
        oper = new BandwidthOnDemandOperation(params, config);
        oper.setProperty(OperationProperties.ENRICHMENT_SERVICE_ID, MY_SERVICE);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH, MY_BANDWIDTH);
        oper.setProperty(OperationProperties.ENRICHMENT_VNF_ID, MY_VNF);

        oper.generateSubRequestId(1);
        outcome.setSubRequestId(oper.getSubRequestId());

        assertThatIllegalStateException().isThrownBy(() -> oper.makeRequest(1))
                        .withMessageContaining("missing bandwidth change time from enrichment data");
    }

    @Test
     void testMakeRequestMissingVnfId() throws Exception {
        oper = new BandwidthOnDemandOperation(params, config);
        oper.setProperty(OperationProperties.ENRICHMENT_SERVICE_ID, MY_SERVICE);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH, MY_BANDWIDTH);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH_CHANGE_TIME, MY_CHANGE_TIME);

        oper.generateSubRequestId(1);
        outcome.setSubRequestId(oper.getSubRequestId());

        assertThatIllegalStateException().isThrownBy(() -> oper.makeRequest(1))
                        .withMessageContaining("missing VNF id from enrichment data");
    }
}
