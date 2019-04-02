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

public class ResponseTest {

    @Test
    public void testResonse() {
        Response response = new Response();
        assertNotNull(response);
        assertNotNull(new Response(new Request()));
        assertNotEquals(0, response.hashCode());

        CommonHeader commonHeader = new CommonHeader();

        Request request = new Request();
        request.setCommonHeader(commonHeader);
        assertNotNull(new Response(request));

        response.setCommonHeader(commonHeader);
        assertEquals(commonHeader, response.getCommonHeader());

        ResponseStatus status = new ResponseStatus();
        response.setStatus(status);
        assertEquals(status, response.getStatus());

        Map<String, Object> payload = new HashMap<>();
        payload.put("North", "Good Witch");
        payload.put("West", "Bad Witch");

        response.setPayload(payload);
        assertEquals(payload, response.getPayload());

        assertNotEquals(0, response.hashCode());

        assertEquals("Response [CommonHeader=CommonHeader [TimeStamp=", response.toString().substring(0, 47));

        Response copiedResponse = new Response();
        copiedResponse.setCommonHeader(response.getCommonHeader());
        copiedResponse.setStatus(response.getStatus());
        copiedResponse.setPayload(response.getPayload());

        assertTrue(response.equals(response));
        assertTrue(response.equals(copiedResponse));
        assertFalse(response.equals(null));
        assertFalse(response.equals("Hello"));

        response.setCommonHeader(null);
        assertFalse(response.equals(copiedResponse));
        copiedResponse.setCommonHeader(null);
        assertTrue(response.equals(copiedResponse));
        response.setCommonHeader(commonHeader);
        assertFalse(response.equals(copiedResponse));
        copiedResponse.setCommonHeader(commonHeader);
        assertTrue(response.equals(copiedResponse));

        response.setStatus(null);
        assertFalse(response.equals(copiedResponse));
        copiedResponse.setStatus(null);
        assertTrue(response.equals(copiedResponse));
        response.setStatus(status);
        assertFalse(response.equals(copiedResponse));
        copiedResponse.setStatus(status);
        assertTrue(response.equals(copiedResponse));

        response.setPayload(new HashMap<String, Object>());
        assertFalse(response.equals(copiedResponse));
        copiedResponse.setPayload(new HashMap<String, Object>());
        assertTrue(response.equals(copiedResponse));
        response.setPayload(payload);
        assertFalse(response.equals(copiedResponse));
        copiedResponse.setPayload(payload);
        assertTrue(response.equals(copiedResponse));
    }
}
