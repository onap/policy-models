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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PciRequestTest {

    @Test
    public void testPciRequest() {
        PciRequest request = new PciRequest();
        assertNotNull(request);
        assertNotEquals(0, request.hashCode());

        PciCommonHeader commonHeader = new PciCommonHeader();
        String requestPayload = "";

        request.setCommonHeader(commonHeader);
        assertEquals(commonHeader, request.getCommonHeader());

        request.setPayload(requestPayload);
        assertEquals(requestPayload, request.getPayload());

        request.setAction("Modify");
        assertEquals("Modify", request.getAction());

        assertNotEquals(0, request.hashCode());

        assertEquals("PciRequest[commonHeader=CommonHeader [timeStamp=", request.toString().substring(0, 48));

        PciRequest copiedPciRequest = new PciRequest();
        copiedPciRequest.setCommonHeader(request.getCommonHeader());
        copiedPciRequest.setAction(request.getAction());
        copiedPciRequest.setPayload(request.getPayload());

        assertTrue(request.equals(request));
        assertTrue(request.equals(copiedPciRequest));
        assertFalse(request.equals(null));
        assertFalse(request.equals("Hello"));

        request.setCommonHeader(null);
        assertFalse(request.equals(copiedPciRequest));
        copiedPciRequest.setCommonHeader(null);
        assertTrue(request.equals(copiedPciRequest));
        request.setCommonHeader(commonHeader);
        assertFalse(request.equals(copiedPciRequest));
        copiedPciRequest.setCommonHeader(commonHeader);
        assertTrue(request.equals(copiedPciRequest));

        request.setAction(null);
        assertFalse(request.equals(copiedPciRequest));
        copiedPciRequest.setAction(null);
        assertTrue(request.equals(copiedPciRequest));
        request.setAction("Modify");
        assertFalse(request.equals(copiedPciRequest));
        copiedPciRequest.setAction("Modify");
        assertTrue(request.equals(copiedPciRequest));

        request.setPayload(null);
        assertFalse(request.equals(copiedPciRequest));
        copiedPciRequest.setPayload(null);
        assertTrue(request.equals(copiedPciRequest));
        request.setPayload(requestPayload);
        assertFalse(request.equals(copiedPciRequest));
        copiedPciRequest.setPayload(requestPayload);
        assertTrue(request.equals(copiedPciRequest));
    }
}
