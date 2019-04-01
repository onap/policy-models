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
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class LcmResonseCodeTest {

    @Test
    public void testLcmResponseCode() {
        assertNull(LcmResponseCode.toResponseValue(0));

        assertEquals(LcmResponseCode.ACCEPTED, LcmResponseCode.toResponseValue(100));
        assertEquals(LcmResponseCode.ERROR, LcmResponseCode.toResponseValue(200));
        assertEquals(LcmResponseCode.REJECT, LcmResponseCode.toResponseValue(300));
        assertEquals(LcmResponseCode.SUCCESS, LcmResponseCode.toResponseValue(400));
        assertEquals(LcmResponseCode.FAILURE, LcmResponseCode.toResponseValue(450));
        assertEquals(LcmResponseCode.FAILURE, LcmResponseCode.toResponseValue(401));
        assertEquals(LcmResponseCode.FAILURE, LcmResponseCode.toResponseValue(406));
        assertEquals(LcmResponseCode.PARTIAL_SUCCESS, LcmResponseCode.toResponseValue(500));
        assertEquals(LcmResponseCode.PARTIAL_FAILURE, LcmResponseCode.toResponseValue(501));
        assertEquals(LcmResponseCode.PARTIAL_FAILURE, LcmResponseCode.toResponseValue(599));

        assertEquals("100", new LcmResponseCode(100).toString());
        assertEquals("200", new LcmResponseCode(200).toString());
        assertEquals("300", new LcmResponseCode(300).toString());
        assertEquals("400", new LcmResponseCode(400).toString());
        assertEquals("450", new LcmResponseCode(450).toString());
        assertEquals("500", new LcmResponseCode(500).toString());
        assertEquals("510", new LcmResponseCode(510).toString());

        assertEquals(300, new LcmResponseCode(300).getCode());
    }
}
