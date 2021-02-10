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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
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

        assertEquals(request, (Object) request);
        assertEquals(request, copiedRequest);
        assertNotEquals(request, null);
        assertNotEquals(request, (Object) "Hello");

        checkField(commonHeader, Request::setCommonHeader);
        checkField(GO_TO_OZ, Request::setAction);
        checkField(WIZARD, Request::setObjectId);
        checkField("Oz", Request::setTargetId);
        checkField(payload, Request::setPayload);
    }

    private <T> void checkField(T value, BiConsumer<Request, T> setter) {
        Request request1 = new Request();
        Request request2 = new Request();

        setter.accept(request2, null);

        setter.accept(request1, value);
        assertNotEquals(request1, request2);

        setter.accept(request2, value);
        assertEquals(request1, request2);

        setter.accept(request1, null);
        assertNotEquals(request1, request2);
    }
}
