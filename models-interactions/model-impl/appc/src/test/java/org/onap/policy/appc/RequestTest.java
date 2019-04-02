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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class RequestTest {

    @Test
    public void testRequest() {
        Request request = new Request();
        assertNotNull(request);
        assertNotEquals(0, request.hashCode());

        CommonHeader commonHeader = new CommonHeader();

        request.setCommonHeader(commonHeader);
        assertEquals(commonHeader, request.getCommonHeader());

        request.setAction("Go to Oz");
        assertEquals("Go to Oz", request.getAction());

        request.setObjectId("Wizard");
        assertEquals("Wizard", request.getObjectId());

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

        assertTrue(request.equals(request));
        assertTrue(request.equals(copiedRequest));
        assertFalse(request.equals(null));
        assertFalse(request.equals("Hello"));

        request.setCommonHeader(null);
        assertFalse(request.equals(copiedRequest));
        copiedRequest.setCommonHeader(null);
        assertTrue(request.equals(copiedRequest));
        request.setCommonHeader(commonHeader);
        assertFalse(request.equals(copiedRequest));
        copiedRequest.setCommonHeader(commonHeader);
        assertTrue(request.equals(copiedRequest));

        request.setAction(null);
        assertFalse(request.equals(copiedRequest));
        copiedRequest.setAction(null);
        assertTrue(request.equals(copiedRequest));
        request.setAction("Go to Oz");
        assertFalse(request.equals(copiedRequest));
        copiedRequest.setAction("Go to Oz");
        assertTrue(request.equals(copiedRequest));

        request.setObjectId(null);
        assertFalse(request.equals(copiedRequest));
        copiedRequest.setObjectId(null);
        assertTrue(request.equals(copiedRequest));
        request.setObjectId("Wizard");
        assertFalse(request.equals(copiedRequest));
        copiedRequest.setObjectId("Wizard");
        assertTrue(request.equals(copiedRequest));

        request.setTargetId(null);
        assertFalse(request.equals(copiedRequest));
        copiedRequest.setTargetId(null);
        assertTrue(request.equals(copiedRequest));
        request.setTargetId("Oz");
        assertFalse(request.equals(copiedRequest));
        copiedRequest.setTargetId("Oz");
        assertTrue(request.equals(copiedRequest));

        request.setPayload(new HashMap<String, Object>());
        assertFalse(request.equals(copiedRequest));
        copiedRequest.setPayload(new HashMap<String, Object>());
        assertTrue(request.equals(copiedRequest));
        request.setPayload(payload);
        assertFalse(request.equals(copiedRequest));
        copiedRequest.setPayload(payload);
        assertTrue(request.equals(copiedRequest));
    }
}
