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

package org.onap.policy.controlloop.actor.sdnc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.sdnc.SdncRequest;
import org.onap.policy.sdnc.SdncResponse;

public class BandwidthOnDemandOperationTest extends BasicSdncOperation {
    private static final String MY_SERVICE = "my-service";
    private static final String MY_VNF = "my-vnf";
    private static final String MY_BANDWIDTH = "my-bandwidth";
    private static final String MY_CHANGE_TIME = "my-change-time";

    private BandwidthOnDemandOperation oper;

    public BandwidthOnDemandOperationTest() {
        super(DEFAULT_ACTOR, BandwidthOnDemandOperation.NAME);
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
     * Set up.
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        oper = new BandwidthOnDemandOperation(params, config);
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(BandwidthOnDemandOperation.NAME, oper.getName());
    }

    @Test
    public void testGetPropertyNames() {
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
    public void testSuccess() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT)
                        .path("GENERIC-RESOURCE-API:vf-module-topology-operation").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).preprocessed(true).build();
        oper = new BandwidthOnDemandOperation(params, config);

        oper.setProperty(OperationProperties.ENRICHMENT_SERVICE_ID, MY_SERVICE);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH, MY_BANDWIDTH);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH_CHANGE_TIME, MY_CHANGE_TIME);
        oper.setProperty(OperationProperties.ENRICHMENT_VNF_ID, MY_VNF);

        outcome = oper.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof SdncResponse);
    }

    @Test
    public void testMakeRequest() throws Exception {
        oper.generateSubRequestId(1);
        SdncRequest request = oper.makeRequest(1);
        assertEquals(MY_SERVICE, request.getNsInstanceId());
        assertEquals(REQ_ID, request.getRequestId());
        assertEquals("/my-path/", request.getUrl());
        assertEquals(oper.getSubRequestId(), request.getHealRequest().getRequestHeaderInfo().getSvcRequestId());

        verifyRequest("bod.json", request, IGNORE_FIELDS);

        verifyMissing(BandwidthOnDemandOperation.SERVICE_ID_KEY, "service", BandwidthOnDemandOperation::new);

        // perform the operation
        makeContext();
        verifyRequest("bod.json", verifyOperation(oper), IGNORE_FIELDS);
    }

    @Test
    public void testMakeRequestViaProperties() throws Exception {
        // clear the enrichment data and remake the operation
        event.setAai(null);
        context = new ControlLoopEventContext(event);
        params = params.toBuilder().context(context).build();
        oper = new BandwidthOnDemandOperation(params, config);

        oper.setProperty(OperationProperties.ENRICHMENT_SERVICE_ID, MY_SERVICE);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH, MY_BANDWIDTH);
        oper.setProperty(OperationProperties.ENRICHMENT_BANDWIDTH_CHANGE_TIME, MY_CHANGE_TIME);
        oper.setProperty(OperationProperties.ENRICHMENT_VNF_ID, MY_VNF);

        verifyRequest("bod.json", verifyOperation(oper), IGNORE_FIELDS);
    }

    @Override
    protected Map<String, String> makeEnrichment() {
        return Map.of(BandwidthOnDemandOperation.SERVICE_ID_KEY, MY_SERVICE, BandwidthOnDemandOperation.BANDWIDTH,
                        MY_BANDWIDTH, BandwidthOnDemandOperation.BANDWIDTH_CHANGE_TIME, MY_CHANGE_TIME,
                        BandwidthOnDemandOperation.VNF_ID, MY_VNF);
    }
}
