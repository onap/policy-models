/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2018-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.so;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SoResponseWrapperTest {

    private static final String REQ_ID = "reqID";

    @Test
    void testConstructor() {
        SoResponse response = new SoResponse();
        SoResponseWrapper obj = new SoResponseWrapper(response, REQ_ID);

        assertEquals(response, obj.getSoResponse());
        assertEquals(REQ_ID, obj.getRequestId());
    }

    @Test
    void testSetGet() {
        SoResponse response = new SoResponse();
        SoResponseWrapper obj = new SoResponseWrapper(response, REQ_ID);

        SoResponse response2 = new SoResponse();
        response2.setHttpResponseCode(2008);
        obj.setSoResponse(response2);
        assertEquals(response2, obj.getSoResponse());

        obj.setRequestId("id2");
        assertEquals("id2", obj.getRequestId());
    }

    @Test
    void testSoResponseWrapperMethods() {
        String requestId = UUID.randomUUID().toString();
        SoResponse response = new SoResponse();

        SoResponseWrapper responseWrapper = new SoResponseWrapper(response, requestId);
        assertNotNull(responseWrapper);
        assertNotEquals(0, responseWrapper.hashCode());

        assertEquals(response, responseWrapper.getSoResponse());

        assertNotEquals(0, responseWrapper.hashCode());

        assertEquals("SOResponseWrapper [SOResponse=org.onap.policy.", responseWrapper.toString().substring(0,  46));

        SoResponseWrapper identicalResponseWrapper = new SoResponseWrapper(response, requestId);

        assertEquals(responseWrapper,  (Object) responseWrapper);
        assertEquals(responseWrapper,  identicalResponseWrapper);
        assertNotNull(responseWrapper);
        assertNotEquals(responseWrapper, (Object) "AString");

        assertEquals(new SoResponseWrapper(null, null), new SoResponseWrapper(null, null));
        assertNotEquals(new SoResponseWrapper(null, null), identicalResponseWrapper);

        assertNotEquals(0, new SoResponseWrapper(null, null).hashCode());

        identicalResponseWrapper.setSoResponse(new SoResponse());
        assertNotEquals(responseWrapper,  identicalResponseWrapper);
        identicalResponseWrapper.setSoResponse(response);
        assertEquals(responseWrapper,  identicalResponseWrapper);

        identicalResponseWrapper.setRequestId(UUID.randomUUID().toString());
        assertNotEquals(responseWrapper,  identicalResponseWrapper);
        identicalResponseWrapper.setRequestId(requestId);
        assertEquals(responseWrapper,  identicalResponseWrapper);

        responseWrapper.setRequestId(null);
        assertNotEquals(responseWrapper,  identicalResponseWrapper);
        identicalResponseWrapper.setRequestId(null);
        assertEquals(responseWrapper,  identicalResponseWrapper);
        responseWrapper.setRequestId(requestId);
        assertNotEquals(responseWrapper,  identicalResponseWrapper);
        identicalResponseWrapper.setRequestId(requestId);
        assertEquals(responseWrapper,  identicalResponseWrapper);
    }
}
