/*-
 * ============LICENSE_START=======================================================
 * appc
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

package org.onap.policy.appc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class RequestTest {

    private static final String WIZARD = "Wizard";
    private static final String GO_TO_OZ = "Go to Oz";

    @Test
    public void testRequest() {
        Request request = new Request();
        assertNotNull(request);
        assertNotEquals(0, request.hashCode());

        CommonHeader commonHeader = new CommonHeader();

        request.setCommonHeader(commonHeader);
        assertEquals(commonHeader, request.getCommonHeader());

        request.setAction(GO_TO_OZ);
        assertEquals(GO_TO_OZ, request.getAction());

        request.setObjectId(WIZARD);
        assertEquals(WIZARD, request.getObjectId());

        request.setTargetId("Oz");
        assertEquals("Oz", request.getTargetId());

        Map<String, Object> payload = new HashMap<>();
        payload.put("North", "Good Witch");
        payload.put("West", "Bad Witch");

        request.setPayload(payload);
        assertEquals(payload, request.getPayload());

        assertNotEquals(0, request.hashCode());

        assertEquals("Request [CommonHeader=CommonHeader [TimeStamp=", request.toString().substring(0, 46));

        Request copiedRequest = new Request();
        copiedRequest.setCommonHeader(request.getCommonHeader());
        copiedRequest.setAction(request.getAction());
        copiedRequest.setObjectId(request.getObjectId());
        copiedRequest.setPayload(request.getPayload());
        copiedRequest.setTargetId(request.getTargetId());

        assertEquals(request, request);
        assertEquals(request, copiedRequest);
        assertNotEquals(null, request);
        assertNotEquals("Hello", request);

        request.setCommonHeader(null);
        assertNotEquals(request, copiedRequest);
        copiedRequest.setCommonHeader(null);
        assertEquals(request, copiedRequest);
        request.setCommonHeader(commonHeader);
        assertNotEquals(request, copiedRequest);
        copiedRequest.setCommonHeader(commonHeader);
        assertEquals(request, copiedRequest);

        request.setAction(null);
        assertNotEquals(request, copiedRequest);
        copiedRequest.setAction(null);
        assertEquals(request, copiedRequest);
        request.setAction(GO_TO_OZ);
        assertNotEquals(request, copiedRequest);
        copiedRequest.setAction(GO_TO_OZ);
        assertEquals(request, copiedRequest);

        request.setObjectId(null);
        assertNotEquals(request, copiedRequest);
        copiedRequest.setObjectId(null);
        assertEquals(request, copiedRequest);
        request.setObjectId(WIZARD);
        assertNotEquals(request, copiedRequest);
        copiedRequest.setObjectId(WIZARD);
        assertEquals(request, copiedRequest);

        request.setTargetId(null);
        assertNotEquals(request, copiedRequest);
        copiedRequest.setTargetId(null);
        assertEquals(request, copiedRequest);
        request.setTargetId("Oz");
        assertNotEquals(request, copiedRequest);
        copiedRequest.setTargetId("Oz");
        assertEquals(request, copiedRequest);

        request.setPayload(new HashMap<>());
        assertNotEquals(request, copiedRequest);
        copiedRequest.setPayload(new HashMap<>());
        assertEquals(request, copiedRequest);
        request.setPayload(payload);
        assertNotEquals(request, copiedRequest);
        copiedRequest.setPayload(payload);
        assertEquals(request, copiedRequest);
    }
}
