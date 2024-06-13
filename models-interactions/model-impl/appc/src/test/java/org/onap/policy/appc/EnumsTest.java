/*-
 * ============LICENSE_START=======================================================
 * appc
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2024 Nordix Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class EnumsTest {

    @Test
    void testResponseCode() {
        assertEquals(5, ResponseCode.values().length);

        assertNull(ResponseCode.toResponseCode(0));

        assertEquals(ResponseCode.ACCEPT, ResponseCode.toResponseCode(100));
        assertEquals(ResponseCode.ERROR, ResponseCode.toResponseCode(200));
        assertEquals(ResponseCode.REJECT, ResponseCode.toResponseCode(300));
        assertEquals(ResponseCode.SUCCESS, ResponseCode.toResponseCode(400));
        assertEquals(ResponseCode.FAILURE, ResponseCode.toResponseCode(500));

        assertEquals(100, ResponseCode.ACCEPT.getValue());
        assertEquals(200, ResponseCode.ERROR.getValue());
        assertEquals(300, ResponseCode.REJECT.getValue());
        assertEquals(400, ResponseCode.SUCCESS.getValue());
        assertEquals(500, ResponseCode.FAILURE.getValue());

        assertEquals("100", ResponseCode.ACCEPT.toString());
        assertEquals("200", ResponseCode.ERROR.toString());
        assertEquals("300", ResponseCode.REJECT.toString());
        assertEquals("400", ResponseCode.SUCCESS.toString());
        assertEquals("500", ResponseCode.FAILURE.toString());
    }

    @Test
    void testResponseValue() {
        assertEquals(5, ResponseValue.values().length);

        assertNull(ResponseValue.toResponseValue("Dorothy"));
        assertNull(ResponseValue.toResponseValue(null));

        assertEquals(ResponseValue.ACCEPT, ResponseValue.toResponseValue("ACCEPT"));
        assertEquals(ResponseValue.ERROR, ResponseValue.toResponseValue("ERROR"));
        assertEquals(ResponseValue.REJECT, ResponseValue.toResponseValue("REJECT"));
        assertEquals(ResponseValue.SUCCESS, ResponseValue.toResponseValue("SUCCESS"));
        assertEquals(ResponseValue.FAILURE, ResponseValue.toResponseValue("FAILURE"));

        assertEquals("ACCEPT", ResponseValue.ACCEPT.toString());
        assertEquals("ERROR", ResponseValue.ERROR.toString());
        assertEquals("REJECT", ResponseValue.REJECT.toString());
        assertEquals("SUCCESS", ResponseValue.SUCCESS.toString());
        assertEquals("FAILURE", ResponseValue.FAILURE.toString());
    }
}
