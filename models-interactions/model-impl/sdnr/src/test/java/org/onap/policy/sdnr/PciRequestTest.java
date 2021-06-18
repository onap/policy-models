/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class PciRequestTest {

    private static final String MODIFY = "Modify";

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

        request.setAction(MODIFY);
        assertEquals(MODIFY, request.getAction());

        assertNotEquals(0, request.hashCode());

        assertEquals("PciRequest(commonHeader=PciCommonHeader(timeStamp=", request.toString().substring(0, 50));

        PciRequest copiedPciRequest = new PciRequest();
        copiedPciRequest.setCommonHeader(request.getCommonHeader());
        copiedPciRequest.setAction(request.getAction());
        copiedPciRequest.setPayload(request.getPayload());

        assertEquals(request, (Object) request);
        assertEquals(request, copiedPciRequest);
        assertNotEquals(request, null);
        assertNotEquals(request, (Object) "Hello");

        request.setCommonHeader(null);
        assertNotEquals(request, copiedPciRequest);
        copiedPciRequest.setCommonHeader(null);
        assertEquals(request, copiedPciRequest);
        request.setCommonHeader(commonHeader);
        assertNotEquals(request, copiedPciRequest);
        copiedPciRequest.setCommonHeader(commonHeader);
        assertEquals(request, copiedPciRequest);

        request.setAction(null);
        assertNotEquals(request, copiedPciRequest);
        copiedPciRequest.setAction(null);
        assertEquals(request, copiedPciRequest);
        request.setAction(MODIFY);
        assertNotEquals(request, copiedPciRequest);
        copiedPciRequest.setAction(MODIFY);
        assertEquals(request, copiedPciRequest);

        request.setPayload(null);
        assertNotEquals(request, copiedPciRequest);
        copiedPciRequest.setPayload(null);
        assertEquals(request, copiedPciRequest);
        request.setPayload(requestPayload);
        assertNotEquals(request, copiedPciRequest);
        copiedPciRequest.setPayload(requestPayload);
        assertEquals(request, copiedPciRequest);
    }
}
