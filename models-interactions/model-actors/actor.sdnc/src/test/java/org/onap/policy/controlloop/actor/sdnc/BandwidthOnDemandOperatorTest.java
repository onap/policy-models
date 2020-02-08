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
import static org.junit.Assert.assertNotNull;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.sdnc.SdncRequest;

public class BandwidthOnDemandOperatorTest extends BasicSdncOperator {

    private BandwidthOnDemandOperator oper;


    /**
     * Set up.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();
        oper = new BandwidthOnDemandOperator(DEFAULT_ACTOR);
    }

    @Test
    public void testBandwidthOnDemandOperator() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(BandwidthOnDemandOperator.NAME, oper.getName());
    }

    @Test
    public void testConstructRequest() throws Exception {
        SdncRequest request = oper.makeRequest(params, 1);
        assertEquals("my-service", request.getNsInstanceId());
        assertEquals(REQ_ID, request.getRequestId());
        assertEquals(BandwidthOnDemandOperator.URI, request.getUrl());
        assertNotNull(request.getHealRequest().getRequestHeaderInfo().getSvcRequestId());

        verifyRequest("bod.json", request);

        verifyMissing(oper, BandwidthOnDemandOperator.SERVICE_ID_KEY, "service");

        // perform the operation
        makeContext();
        verifyRequest("bod.json", verifyOperation(oper));
    }

    @Override
    protected Map<String, String> makeEnrichment() {
        return Map.of(BandwidthOnDemandOperator.SERVICE_ID_KEY, "my-service", BandwidthOnDemandOperator.VNF_ID,
                        "my-vnf");
    }
}
