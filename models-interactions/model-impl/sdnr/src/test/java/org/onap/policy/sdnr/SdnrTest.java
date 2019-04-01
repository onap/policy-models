/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;
import org.onap.policy.sdnr.util.Serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdnrTest {

    private static final Logger logger = LoggerFactory.getLogger(SdnrTest.class);

    private static PciRequestWrapper dmaapRequest;
    private static PciResponseWrapper dmaapResponse;

    static {
        /*
         * Construct an SDNR Request to be Serialized
         */
        dmaapRequest = new PciRequestWrapper();
        dmaapRequest.setCorrelationId("664be3d2-6c12-4f4b-a3e7-c349acced200" + "-" + "1");
        dmaapRequest.setRpcName("restart");
        dmaapRequest.setType("request");

        dmaapResponse = new PciResponseWrapper();
        dmaapResponse.setCorrelationId("664be3d2-6c12-4f4b-a3e7-c349acced200" + "-" + "1");
        dmaapResponse.setRpcName("restart");
        dmaapResponse.setType("response");

        PciRequest sdnrRequest = new PciRequest();

        sdnrRequest.setAction("ModifyConfig");

        PciCommonHeader commonHeader = new PciCommonHeader();
        commonHeader.setRequestId(UUID.fromString("664be3d2-6c12-4f4b-a3e7-c349acced200"));
        commonHeader.setSubRequestId("1");

        sdnrRequest.setCommonHeader(commonHeader);

        sdnrRequest.setPayload(null);

        dmaapRequest.setBody(sdnrRequest);

        /*
         * Construct an SDNR Response to be Serialized
         */
        PciResponse sdnrResponse = new PciResponse(sdnrRequest);
        sdnrResponse.getStatus().setCode(400);
        sdnrResponse.getStatus().setValue("Restart Successful");
        sdnrResponse.setPayload(null);

        dmaapResponse.setBody(sdnrResponse);
    }

    @Test
    public void testRequestSerialization() {

        /*
         * Use the gson serializer to obtain json
         */
        String jsonRequest = Serialization.gson.toJson(dmaapRequest, PciRequestWrapper.class);
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
    public void testRequestDeserialization() {

        /*
         * Convert the PCI request object into json so we have a string of json to use for testing
         */
        String jsonRequest = Serialization.gson.toJson(dmaapRequest, PciRequestWrapper.class);

        /*
         * Use the serializer to convert the json string into a java object
         */
        PciRequestWrapper pciRequestWrapper = Serialization.gson.fromJson(jsonRequest, PciRequestWrapper.class);
        assertNotNull(pciRequestWrapper);
        assertEquals(dmaapRequest, pciRequestWrapper);

        /*
         * The type of the DMAAP wrapper should be request
         */
        assertEquals("request", dmaapRequest.getType());

        /*
         * The DMAAP wrapper must have a body as that is the true SDNR request
         */
        assertNotNull(dmaapRequest.getBody());
        PciRequest sdnrRequest = dmaapRequest.getBody();
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
    public void testResponseSerialization() {

        /*
         * Use the serializer to convert the object into json
         */
        String jsonResponse = Serialization.gson.toJson(dmaapResponse, PciResponseWrapper.class);
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
    public void testResponseDeserialization() {
        /*
         * Convert the PCI response object into json so we have a string of json to use for testing
         */
        String jsonResponse = Serialization.gson.toJson(dmaapResponse, PciResponseWrapper.class);

        /*
         * Use the serializer to convert the json string into a java object
         */
        PciResponseWrapper pciResponseWrapper = Serialization.gson.fromJson(jsonResponse, PciResponseWrapper.class);
        assertNotNull(pciResponseWrapper);
        assertEquals(dmaapResponse, pciResponseWrapper);

        /*
         * The type of the DMAAP wrapper should be response
         */
        assertEquals("response", dmaapResponse.getType());

        /*
         * The DMAAP wrapper must have a body as that is the true SDNR response
         */
        assertNotNull(dmaapResponse.getBody());
        PciResponse sdnrResponse = dmaapResponse.getBody();
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
