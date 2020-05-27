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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.sdnc.SdncRequest;
import org.onap.policy.sdnc.SdncResponse;

public class RerouteOperationTest extends BasicSdncOperation {

    private RerouteOperation oper;

    public RerouteOperationTest() {
        super(DEFAULT_ACTOR, RerouteOperation.NAME);
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
    @Before
    public void setUp() throws Exception {
        super.setUp();
        oper = new RerouteOperation(params, config);
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    public void testSuccess() throws Exception {
        HttpParams opParams = HttpParams.builder().clientName(MY_CLIENT)
                        .path("GENERIC-RESOURCE-API:network-topology-operation").build();
        config = new HttpConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());

        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();
        oper = new RerouteOperation(params, config);

        outcome = oper.start().get();
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof SdncResponse);
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(RerouteOperation.NAME, oper.getName());
    }

    @Test
    public void testMakeRequest() throws Exception {
        oper.generateSubRequestId(1);
        SdncRequest request = oper.makeRequest(1);
        assertEquals("my-service", request.getNsInstanceId());
        assertEquals(REQ_ID, request.getRequestId());
        assertEquals("/my-path/", request.getUrl());
        assertEquals(oper.getSubRequestId(), request.getHealRequest().getRequestHeaderInfo().getSvcRequestId());

        verifyRequest("reroute.json", request, IGNORE_FIELDS);

        verifyMissing(RerouteOperation.SERVICE_ID_KEY, "service", RerouteOperation::new);
        verifyMissing(RerouteOperation.NETWORK_ID_KEY, "network", RerouteOperation::new);

        // perform the operation
        makeContext();
        verifyRequest("reroute.json", verifyOperation(oper), IGNORE_FIELDS);
    }

    @Override
    protected Map<String, String> makeEnrichment() {
        return Map.of(RerouteOperation.SERVICE_ID_KEY, "my-service", RerouteOperation.NETWORK_ID_KEY, "my-network");
    }
}
