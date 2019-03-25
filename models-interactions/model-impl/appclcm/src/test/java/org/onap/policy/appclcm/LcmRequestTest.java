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

package org.onap.policy.appclcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class LcmRequestTest {

    @Test
    public void testLcmRequest() {
        LcmRequest request = new LcmRequest();
        assertNotNull(request);
        assertNotEquals(0, request.hashCode());

        LcmCommonHeader commonHeader = new LcmCommonHeader();

        request.setCommonHeader(commonHeader);
        assertEquals(commonHeader, request.getCommonHeader());

        request.setAction("Go to Oz");
        assertEquals("Go to Oz", request.getAction());

        Map<String, String> actionIdentifiers = new HashMap<>();
        actionIdentifiers.put("North", "Good Witch");
        actionIdentifiers.put("West", "Bad Witch");

        request.setActionIdentifiers(actionIdentifiers);
        assertEquals(actionIdentifiers, request.getActionIdentifiers());

        request.setPayload("The Emerald City");
        assertEquals("The Emerald City", request.getPayload());

        assertNotEquals(0, request.hashCode());

        assertEquals("Request [commonHeader=CommonHeader [timeStamp=", request.toString().substring(0, 46));

        LcmRequest copiedLcmRequest = new LcmRequest();
        copiedLcmRequest.setCommonHeader(request.getCommonHeader());
        copiedLcmRequest.setAction(request.getAction());
        copiedLcmRequest.setActionIdentifiers(request.getActionIdentifiers());
        copiedLcmRequest.setPayload(request.getPayload());

        assertTrue(request.equals(request));
        assertTrue(request.equals(copiedLcmRequest));
        assertFalse(request.equals(null));
        assertFalse(request.equals("Hello"));

        request.setCommonHeader(null);
        assertFalse(request.equals(copiedLcmRequest));
        copiedLcmRequest.setCommonHeader(null);
        assertTrue(request.equals(copiedLcmRequest));
        request.setCommonHeader(commonHeader);
        assertFalse(request.equals(copiedLcmRequest));
        copiedLcmRequest.setCommonHeader(commonHeader);
        assertTrue(request.equals(copiedLcmRequest));

        request.setAction(null);
        assertFalse(request.equals(copiedLcmRequest));
        copiedLcmRequest.setAction(null);
        assertTrue(request.equals(copiedLcmRequest));
        request.setAction("Go to Oz");
        assertFalse(request.equals(copiedLcmRequest));
        copiedLcmRequest.setAction("Go to Oz");
        assertTrue(request.equals(copiedLcmRequest));

        request.setActionIdentifiers(null);
        assertFalse(request.equals(copiedLcmRequest));
        copiedLcmRequest.setActionIdentifiers(null);
        assertTrue(request.equals(copiedLcmRequest));
        request.setActionIdentifiers(actionIdentifiers);
        assertFalse(request.equals(copiedLcmRequest));
        copiedLcmRequest.setActionIdentifiers(actionIdentifiers);
        assertTrue(request.equals(copiedLcmRequest));

        request.setPayload(null);
        assertFalse(request.equals(copiedLcmRequest));
        copiedLcmRequest.setPayload(null);
        assertTrue(request.equals(copiedLcmRequest));
        request.setPayload("The Emerald City");
        assertFalse(request.equals(copiedLcmRequest));
        copiedLcmRequest.setPayload("The Emerald City");
        assertTrue(request.equals(copiedLcmRequest));
    }
}
