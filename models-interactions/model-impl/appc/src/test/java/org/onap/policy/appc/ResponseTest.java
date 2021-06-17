/*-
 * ============LICENSE_START=======================================================
 * appc
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

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

        assertThat(response.toString()).startsWith("Response(commonHeader=CommonHeader(timeStamp=");

        Response copiedResponse = new Response();
        copiedResponse.setCommonHeader(response.getCommonHeader());
        copiedResponse.setStatus(response.getStatus());
        copiedResponse.setPayload(response.getPayload());

        assertEquals(response, (Object) response);
        assertEquals(response, copiedResponse);
        assertNotEquals(response, null);
        assertNotEquals(response, (Object) "Hello");

        response.setCommonHeader(null);
        assertNotEquals(response, copiedResponse);
        copiedResponse.setCommonHeader(null);
        assertEquals(response, copiedResponse);
        response.setCommonHeader(commonHeader);
        assertNotEquals(response, copiedResponse);
        copiedResponse.setCommonHeader(commonHeader);
        assertEquals(response, copiedResponse);

        response.setStatus(null);
        assertNotEquals(response, copiedResponse);
        copiedResponse.setStatus(null);
        assertEquals(response, copiedResponse);
        response.setStatus(status);
        assertNotEquals(response, copiedResponse);
        copiedResponse.setStatus(status);
        assertEquals(response, copiedResponse);

        response.setPayload(new HashMap<String, Object>());
        assertNotEquals(response, copiedResponse);
        copiedResponse.setPayload(new HashMap<String, Object>());
        assertEquals(response, copiedResponse);
        response.setPayload(payload);
        assertNotEquals(response, copiedResponse);
        copiedResponse.setPayload(payload);
        assertEquals(response, copiedResponse);
    }
}
