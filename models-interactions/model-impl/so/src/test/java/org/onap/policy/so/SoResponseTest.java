/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SoResponseTest {

    @Test
    void testConstructor() {
        SoResponse obj = new SoResponse();

        assertEquals(0, obj.getHttpResponseCode());
        assertNull(obj.getRequest());
        assertNull(obj.getRequestError());
        assertNull(obj.getRequestReferences());
    }

    @Test
    void testSetGet() {
        SoResponse obj = new SoResponse();

        obj.setHttpResponseCode(2008);
        assertEquals(2008, obj.getHttpResponseCode());

        SoRequest request = new SoRequest();
        obj.setRequest(request);
        assertEquals(request, obj.getRequest());

        SoRequestError requestError = new SoRequestError();
        obj.setRequestError(requestError);
        assertEquals(requestError, obj.getRequestError());

        SoRequestReferences requestReferences = new SoRequestReferences();
        obj.setRequestReferences(requestReferences);
        assertEquals(requestReferences, obj.getRequestReferences());
    }
}
