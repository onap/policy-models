/*-
 * ============LICENSE_START=======================================================
 * appclcm
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.appclcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.UUID;

import org.junit.Test;
import org.onap.policy.appclcm.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppcLcmTest {

    private static final String VNF_ID_KEY = "vnf-id";

    private static final String RESTART = "restart";

    private static final String CORRELATION_ID = "664be3d2-6c12-4f4b-a3e7-c349acced200";

    private static final Logger logger = LoggerFactory.getLogger(AppcLcmTest.class);

    private static AppcLcmDmaapWrapper dmaapRequest;
    private static AppcLcmDmaapWrapper dmaapResponse;

    static {
        /*
         * Construct an APPCLCM Request to be Serialized
         */
        dmaapRequest = new AppcLcmDmaapWrapper();
        dmaapRequest.setCorrelationId(CORRELATION_ID + "-" + "1");
        dmaapRequest.setRpcName(RESTART);
        dmaapRequest.setType("request");

        dmaapResponse = new AppcLcmDmaapWrapper();
        dmaapResponse.setCorrelationId(CORRELATION_ID + "-" + "1");
        dmaapResponse.setRpcName(RESTART);
        dmaapResponse.setType("response");

        AppcLcmInput appcRequest = new AppcLcmInput();

        appcRequest.setAction(RESTART);

        HashMap<String, String> actionIdentifiers = new HashMap<>();
        actionIdentifiers.put(VNF_ID_KEY, "trial-vnf-003");
        actionIdentifiers.put("vserver-id", "08f6c1f9-99e7-49f3-a662-c62b9f200d79");

        appcRequest.setActionIdentifiers(actionIdentifiers);

        AppcLcmCommonHeader commonHeader = new AppcLcmCommonHeader();
        commonHeader.setRequestId(UUID.fromString(CORRELATION_ID));
        commonHeader.setSubRequestId("1");
        commonHeader.setOriginatorId(CORRELATION_ID);

        appcRequest.setCommonHeader(commonHeader);

        appcRequest.setPayload(null);

        AppcLcmBody dmaapRequestBody = new AppcLcmBody();
        dmaapRequestBody.setInput(appcRequest);

        dmaapRequest.setBody(dmaapRequestBody);

        /*
         * Construct an APPCLCM Response to be Serialized
         */
        AppcLcmOutput appcResponse = new AppcLcmOutput(appcRequest);
        appcResponse.getStatus().setCode(400);
        appcResponse.getStatus().setMessage("Restart Successful");
        appcResponse.setPayload(null);

        AppcLcmBody dmaapResponseBody = new AppcLcmBody();
        dmaapResponseBody.setOutput(appcResponse);

        dmaapResponse.setBody(dmaapResponseBody);
    }

    @Test
    public void testRequestSerialization() {

        /*
         * Use the gson serializer to obtain json
         */
        String jsonRequest = Serialization.gson.toJson(dmaapRequest, AppcLcmDmaapWrapper.class);
        assertNotNull(jsonRequest);

        /*
         * The serializer should have added an extra sub-tag called "input" that wraps the request
         */
        assertTrue(jsonRequest.contains("input"));

        /*
         * The common-header, request-id, and sub-request-id should exist
         */
        assertTrue(jsonRequest.contains("common-header"));
        assertTrue(jsonRequest.contains("request-id"));
        assertTrue(jsonRequest.contains("sub-request-id"));

        /*
         * action-identifiers should exist and contain a vnf-id
         */
        assertTrue(jsonRequest.contains("action-identifiers"));
        assertTrue(jsonRequest.contains(VNF_ID_KEY));

        /*
         * The action sub-tag should exist
         */
        assertTrue(jsonRequest.contains("action"));

        logger.debug("Request as JSON: " + jsonRequest + "\n\n");
    }

    @Test
    public void testRequestDeserialization() {

        /*
         * Convert the LCM request object into json so we have a string of json to use for testing
         */
        String jsonRequest = Serialization.gson.toJson(dmaapRequest, AppcLcmDmaapWrapper.class);

        /*
         * Use the serializer to convert the json string into a java object
         */
        AppcLcmDmaapWrapper req = Serialization.gson.fromJson(jsonRequest, AppcLcmDmaapWrapper.class);
        assertNotNull(req);

        /*
         * The type of the DMAAP wrapper should be request
         */
        assertEquals("request", req.getType());

        /*
         * The DMAAP wrapper must have a body as that is the true APPC request
         */
        assertNotNull(req.getBody());
        AppcLcmInput appcRequest = req.getBody().getInput();
        assertNotNull(appcRequest);

        /*
         * The common header should not be null
         */
        assertNotNull(appcRequest.getCommonHeader());

        /*
         * The action should not be null and should be set to restart
         */
        assertNotNull(appcRequest.getAction());
        assertEquals(RESTART, appcRequest.getAction());

        /*
         * The action-identifiers should not be null and should contain a vnf-id
         */
        assertNotNull(appcRequest.getActionIdentifiers());
        assertNotNull(appcRequest.getActionIdentifiers().get(VNF_ID_KEY));

        logger.debug("Request as a Java Object: \n" + appcRequest.toString() + "\n\n");
    }

    @Test
    public void testResponseSerialization() {

        /*
         * Use the serializer to convert the object into json
         */
        String jsonResponse = Serialization.gson.toJson(dmaapResponse, AppcLcmDmaapWrapper.class);
        assertNotNull(jsonResponse);

        /*
         * The serializer should have added an extra sub-tag called "input" that wraps the request
         */
        assertTrue(jsonResponse.contains("output"));

        /*
         * The response should contain a common-header, request-id, sub-request-id, and status
         */
        assertTrue(jsonResponse.contains("common-header"));
        assertTrue(jsonResponse.contains("request-id"));
        assertTrue(jsonResponse.contains("sub-request-id"));
        assertTrue(jsonResponse.contains("status"));

        logger.debug("Response as JSON: " + jsonResponse + "\n\n");
    }

    @Test
    public void testResponseDeserialization() {
        /*
         * Convert the LCM response object into json so we have a string of json to use for testing
         */
        String jsonResponse = Serialization.gson.toJson(dmaapResponse, AppcLcmDmaapWrapper.class);

        /*
         * Use the serializer to convert the json string into a java object
         */
        AppcLcmDmaapWrapper resp = Serialization.gson.fromJson(jsonResponse, AppcLcmDmaapWrapper.class);
        assertNotNull(resp);

        /*
         * The type of the DMAAP wrapper should be response
         */
        assertEquals("response", resp.getType());

        /*
         * The DMAAP wrapper must have a body as that is the true APPC response
         */
        assertNotNull(resp.getBody());
        AppcLcmOutput appcResponse = resp.getBody().getOutput();
        assertNotNull(appcResponse);

        /*
         * The common header should not be null
         */
        assertNotNull(appcResponse.getCommonHeader());

        /*
         * The status should not be null and the status code should be 400
         */
        assertNotNull(appcResponse.getStatus());
        assertEquals(400, appcResponse.getStatus().getCode());

        logger.debug("Response as a Java Object: \n" + appcResponse.toString() + "\n\n");
    }
}
