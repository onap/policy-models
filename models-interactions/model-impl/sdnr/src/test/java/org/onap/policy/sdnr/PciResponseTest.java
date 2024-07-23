/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PciResponseTest {

    Status status = new Status(0, "");

    String responsePayload = "";
    String requestPayload = "";


    @Test
    void testHashCode() {
        PciResponse response = new PciResponse();
        assertNotEquals(0, response.hashCode());
        response.setCommonHeader(new PciCommonHeader());
        assertNotEquals(0, response.hashCode());
        response.setPayload(responsePayload);
        assertNotEquals(0, response.hashCode());
        response.setStatus(null);
        assertNotEquals(0, response.hashCode());
    }

    @Test
    void testPciResponse() {
        PciResponse response = new PciResponse();
        assertNull(response.getCommonHeader());
        assertNull(response.getPayload());
        assertNotNull(response.getStatus());
    }

    @Test
    void testToString() {
        PciResponse response = new PciResponse();
        assertFalse(response.toString().isEmpty());
    }

    @Test
    void testEqualsObject() {
        PciResponse response = new PciResponse();
        assertEquals(response, response);
        assertNotNull(response);
        assertNotEquals(response, new Object());

        PciResponse response2 = new PciResponse();
        assertEquals(response, response2);

        response.setCommonHeader(new PciCommonHeader());
        assertNotEquals(response, response2);
        response2.setCommonHeader(response.getCommonHeader());
        assertEquals(response, response2);

        response.setPayload(responsePayload);
        assertNotEquals(response, response2);
        response2.setPayload(response.getPayload());
        assertEquals(response, response2);

        response.setCommonHeader(null);
        assertNotEquals(response, response2);
        response2.setCommonHeader(null);
        assertEquals(response, response2);

        response.setPayload(null);
        assertNotEquals(response, response2);
        response2.setPayload(response.getPayload());
        assertEquals(response, response2);

        response.setStatus(null);
        assertNotEquals(response, response2);
        response2.setStatus(response.getStatus());
        assertEquals(response, response2);

        Status stat = new Status();
        stat.setCode(5);
        response.setStatus(stat);
        response2.setStatus(new Status());
        assertNotEquals(response, response2);
    }

    @Test
    void testResponseRequest() {
        PciRequest request = new PciRequest();
        request.setCommonHeader(new PciCommonHeader());
        request.setPayload(requestPayload);

        PciResponse response = new PciResponse(request);

        assertEquals(response.getCommonHeader(), request.getCommonHeader());
    }

}
