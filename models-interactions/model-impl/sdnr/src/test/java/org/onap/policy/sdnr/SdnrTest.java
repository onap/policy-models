/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.onap.policy.sdnr.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SdnrTest {

    private static final String CORRELATION_ID = "664be3d2-6c12-4f4b-a3e7-c349acced200";

    private static final Logger logger = LoggerFactory.getLogger(SdnrTest.class);

    private static PciRequestWrapper messageRequest;
    private static PciResponseWrapper messageResponse;

    static {
        /*
         * Construct an SDNR Request to be Serialized
         */
        messageRequest = new PciRequestWrapper();
        messageRequest.setCorrelationId(CORRELATION_ID + "-" + "1");
        messageRequest.setRpcName("restart");
        messageRequest.setType("request");

        messageResponse = new PciResponseWrapper();
        messageResponse.setCorrelationId(CORRELATION_ID + "-" + "1");
        messageResponse.setRpcName("restart");
        messageResponse.setType("response");

        PciRequest sdnrRequest = new PciRequest();

        sdnrRequest.setAction("ModifyConfig");

        PciCommonHeader commonHeader = new PciCommonHeader();
        commonHeader.setRequestId(UUID.fromString(CORRELATION_ID));
        commonHeader.setSubRequestId("1");

        sdnrRequest.setCommonHeader(commonHeader);

        sdnrRequest.setPayload(null);

        messageRequest.setBody(sdnrRequest);

        /*
         * Construct an SDNR Response to be Serialized
         */
        PciResponse sdnrResponse = new PciResponse(sdnrRequest);
        sdnrResponse.getStatus().setCode(400);
        sdnrResponse.getStatus().setValue("Restart Successful");
        sdnrResponse.setPayload(null);

        messageResponse.setBody(sdnrResponse);
    }

    @Test
    void testRequestSerialization() {

        /*
         * Use the gson serializer to obtain json
         */
        String jsonRequest = Serialization.gson.toJson(messageRequest, PciRequestWrapper.class);
        assertNotNull(jsonRequest);

        /*
         * The serializer should have added an extra sub-tag called "input" that wraps the request
         */
        assertTrue(jsonRequest.contains("input"));

        /*
         * The common-header, request-id, and sub-request-id should exist
         */
        assertTrue(jsonRequest.contains("CommonHeader"));
        assertTrue(jsonRequest.contains("RequestID"));
        assertTrue(jsonRequest.contains("SubRequestID"));

        /*
         * The action sub-tag should exist
         */
        assertTrue(jsonRequest.contains("Action"));

        logger.debug("Request as JSON: " + jsonRequest + "\n\n");
    }

    @Test
    void testRequestDeserialization() {

        /*
         * Convert the PCI request object into json so we have a string of json to use for testing
         */
        String jsonRequest = Serialization.gson.toJson(messageRequest, PciRequestWrapper.class);

        /*
         * Use the serializer to convert the json string into a java object
         */
        PciRequestWrapper pciRequestWrapper = Serialization.gson.fromJson(jsonRequest, PciRequestWrapper.class);
        assertNotNull(pciRequestWrapper);
        assertEquals(messageRequest, pciRequestWrapper);

        /*
         * The type of the Message wrapper should be request
         */
        assertEquals("request", messageRequest.getType());

        /*
         * The Message wrapper must have a body as that is the true SDNR request
         */
        assertNotNull(messageRequest.getBody());
        PciRequest sdnrRequest = messageRequest.getBody();
        assertNotNull(sdnrRequest);

        /*
         * The common header should not be null
         */
        assertNotNull(sdnrRequest.getCommonHeader());

        /*
         * The action should not be null and should be set to restart
         */
        assertNotNull(sdnrRequest.getAction());
        assertEquals("ModifyConfig", sdnrRequest.getAction());

        logger.debug("Request as a Java Object: \n" + sdnrRequest.toString() + "\n\n");
    }

    @Test
    void testResponseSerialization() {

        /*
         * Use the serializer to convert the object into json
         */
        String jsonResponse = Serialization.gson.toJson(messageResponse, PciResponseWrapper.class);
        assertNotNull(jsonResponse);

        /*
         * The serializer should have added an extra sub-tag called "input" that wraps the request
         */
        assertTrue(jsonResponse.contains("output"));

        /*
         * The response should contain a common-header, request-id, sub-request-id, and status
         */
        assertTrue(jsonResponse.contains("CommonHeader"));
        assertTrue(jsonResponse.contains("RequestID"));
        assertTrue(jsonResponse.contains("SubRequestID"));
        assertTrue(jsonResponse.contains("Status"));

        logger.debug("Response as JSON: " + jsonResponse + "\n\n");
    }

    @Test
    void testResponseDeserialization() {
        /*
         * Convert the PCI response object into json so we have a string of json to use for testing
         */
        String jsonResponse = Serialization.gson.toJson(messageResponse, PciResponseWrapper.class);

        /*
         * Use the serializer to convert the json string into a java object
         */
        PciResponseWrapper pciResponseWrapper = Serialization.gson.fromJson(jsonResponse, PciResponseWrapper.class);
        assertNotNull(pciResponseWrapper);
        assertEquals(messageResponse, pciResponseWrapper);

        /*
         * The type of the Message wrapper should be response
         */
        assertEquals("response", messageResponse.getType());

        /*
         * The Message wrapper must have a body as that is the true SDNR response
         */
        assertNotNull(messageResponse.getBody());
        PciResponse sdnrResponse = messageResponse.getBody();
        assertNotNull(sdnrResponse);

        /*
         * The common header should not be null
         */
        assertNotNull(sdnrResponse.getCommonHeader());

        /*
         * The status should not be null and the status code should be 400
         */
        assertNotNull(sdnrResponse.getStatus());
        assertEquals(400, sdnrResponse.getStatus().getCode());

        logger.debug("Response as a Java Object: \n" + sdnrResponse.toString() + "\n\n");
    }
}
