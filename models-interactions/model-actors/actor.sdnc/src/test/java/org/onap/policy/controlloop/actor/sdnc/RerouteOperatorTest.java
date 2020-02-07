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
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.sdnc.SdncRequest;

public class RerouteOperatorTest extends BasicOperator {

    private RerouteOperator oper;


    /**
     * Set up.
     */
    @Before
    public void setUp() {
        makeContext();
        oper = new RerouteOperator(ACTOR);
    }

    @Test
    public void testRerouteOperator() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(RerouteOperator.NAME, oper.getName());
    }

    @Test
    public void testConstructRequest() throws CoderException {
        SdncRequest request = oper.constructRequest(context);
        assertEquals("my-service", request.getNsInstanceId());
        assertEquals(REQ_ID, request.getRequestId());
        assertEquals(RerouteOperator.URI, request.getUrl());
        assertNotNull(request.getHealRequest().getRequestHeaderInfo().getSvcRequestId());

        String json = new StandardCoder().encode(request, true);
        String expected = ResourceUtils.getResourceAsString("reroute.json");

        // strip request id, because it changes each time
        final String stripper = "svc-request-id[^,]*";
        json = json.replaceFirst(stripper, "").trim();
        expected = expected.replaceFirst(stripper, "").trim();

        assertEquals(expected, json);

        verifyMissing(oper, RerouteOperator.SERVICE_ID_KEY, "service");
        verifyMissing(oper, RerouteOperator.NETWORK_ID_KEY, "network");
    }

    @Override
    protected Map<String, String> makeEnrichment() {
        return Map.of(RerouteOperator.SERVICE_ID_KEY, "my-service", RerouteOperator.NETWORK_ID_KEY, "my-network");
    }
}
