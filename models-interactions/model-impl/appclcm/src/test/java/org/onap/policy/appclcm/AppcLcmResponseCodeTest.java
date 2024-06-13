/*-
 * ============LICENSE_START=======================================================
 * appclcm
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AppcLcmResponseCodeTest {

    @Test
    void testAppcLcmResponseCode() {
        assertNull(AppcLcmResponseCode.toResponseValue(0));

        assertEquals(AppcLcmResponseCode.ACCEPTED, AppcLcmResponseCode.toResponseValue(100));
        assertEquals(AppcLcmResponseCode.ERROR, AppcLcmResponseCode.toResponseValue(200));
        assertEquals(AppcLcmResponseCode.REJECT, AppcLcmResponseCode.toResponseValue(300));
        assertEquals(AppcLcmResponseCode.SUCCESS, AppcLcmResponseCode.toResponseValue(400));
        assertEquals(AppcLcmResponseCode.FAILURE, AppcLcmResponseCode.toResponseValue(450));
        assertEquals(AppcLcmResponseCode.FAILURE, AppcLcmResponseCode.toResponseValue(401));
        assertEquals(AppcLcmResponseCode.FAILURE, AppcLcmResponseCode.toResponseValue(406));
        assertEquals(AppcLcmResponseCode.PARTIAL_SUCCESS, AppcLcmResponseCode.toResponseValue(500));
        assertEquals(AppcLcmResponseCode.PARTIAL_FAILURE, AppcLcmResponseCode.toResponseValue(501));
        assertEquals(AppcLcmResponseCode.PARTIAL_FAILURE, AppcLcmResponseCode.toResponseValue(599));

        assertEquals("100", new AppcLcmResponseCode(100).toString());
        assertEquals("200", new AppcLcmResponseCode(200).toString());
        assertEquals("300", new AppcLcmResponseCode(300).toString());
        assertEquals("400", new AppcLcmResponseCode(400).toString());
        assertEquals("450", new AppcLcmResponseCode(450).toString());
        assertEquals("500", new AppcLcmResponseCode(500).toString());
        assertEquals("510", new AppcLcmResponseCode(510).toString());

        assertEquals(300, new AppcLcmResponseCode(300).getCode());
    }
}
