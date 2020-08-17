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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.sdnc.SdncHealRequest;
import org.onap.policy.sdnc.SdncHealRequestHeaderInfo;
import org.onap.policy.sdnc.SdncRequest;

public class SdncOperationTest extends BasicSdncOperation {

    private static final String MY_URI = "my-uri";

    private SdncRequest request;
    private SdncOperation oper;

    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        request = new SdncRequest();
        request.setUrl(MY_URI);

        SdncHealRequest healRequest = new SdncHealRequest();
        request.setHealRequest(healRequest);

        SdncHealRequestHeaderInfo headerInfo = new SdncHealRequestHeaderInfo();
        healRequest.setRequestHeaderInfo(headerInfo);
        headerInfo.setSvcRequestId(SUB_REQ_ID);

        oper = new SdncOperation(params, config) {
            @Override
            protected SdncRequest makeRequest(int attempt) {
                return request;
            }
        };
    }

    @Test
    public void testSdncOperator() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
    }

    @Test
    public void testStartPreprocessorAsync() {
        assertNotNull(oper.startPreprocessorAsync());
    }

    @Test
    public void testStartOperationAsync_testStartRequestAsync() throws Exception {
        verifyOperation(oper);
    }

    @Test
    public void testIsSuccess() {
        // success case
        response.getResponseOutput().setResponseCode("200");
        assertTrue(oper.isSuccess(null, response));

        // failure code
        response.getResponseOutput().setResponseCode("555");
        assertFalse(oper.isSuccess(null, response));

        // null code
        response.getResponseOutput().setResponseCode(null);
        assertFalse(oper.isSuccess(null, response));

        // null output
        response.setResponseOutput(null);
        assertFalse(oper.isSuccess(null, response));
    }

    @Test
    public void testGetOptProperty() {
        // in neither property nor enrichment
        assertNull(oper.getOptProperty("propA", "propA2"));

        // both - should choose the property
        remakeOper(Map.of("propB2", "valueB2"));
        oper.setProperty("propB", "valueB");
        assertEquals("valueB", oper.getOptProperty("propB", "propB2"));

        // both - should choose the property, even if it's null
        remakeOper(Map.of("propC2", "valueC2"));
        oper.setProperty("propC", null);
        assertNull(oper.getOptProperty("propC", "propC2"));

        // only in enrichment data
        remakeOper(Map.of("propD2", "valueD2"));
        assertEquals("valueD2", oper.getOptProperty("propD", "propD2"));
    }

    /**
     * Remakes the operation, with the specified A&AI enrichment data.
     *
     * @param aai A&AI enrichment data
     */
    private void remakeOper(Map<String, String> aai) {
        event.setAai(aai);
        context = new ControlLoopEventContext(event);
        params = params.toBuilder().context(context).build();

        oper = new SdncOperation(params, config) {
            @Override
            protected SdncRequest makeRequest(int attempt) {
                return request;
            }
        };
    }

    @Override
    protected Map<String, String> makeEnrichment() {
        return new TreeMap<>();
    }
}
