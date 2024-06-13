/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class PciResponseCodeTest {

    @Test
    public void testPciResponseCode() {
        assertNull(PciResponseCode.toResponseValue(0));

        assertEquals(PciResponseCode.ACCEPTED, PciResponseCode.toResponseValue(100));
        assertEquals(PciResponseCode.SUCCESS, PciResponseCode.toResponseValue(200));
        assertEquals(PciResponseCode.REJECT, PciResponseCode.toResponseValue(300));
        assertEquals(PciResponseCode.ERROR, PciResponseCode.toResponseValue(400));
        assertEquals(PciResponseCode.FAILURE, PciResponseCode.toResponseValue(450));
        assertEquals(PciResponseCode.FAILURE, PciResponseCode.toResponseValue(401));
        assertEquals(PciResponseCode.FAILURE, PciResponseCode.toResponseValue(406));
        assertEquals(PciResponseCode.PARTIAL_SUCCESS, PciResponseCode.toResponseValue(500));
        assertEquals(PciResponseCode.PARTIAL_FAILURE, PciResponseCode.toResponseValue(501));
        assertEquals(PciResponseCode.PARTIAL_FAILURE, PciResponseCode.toResponseValue(599));

        assertEquals("100", new PciResponseCode(100).toString());
        assertEquals("200", new PciResponseCode(200).toString());
        assertEquals("300", new PciResponseCode(300).toString());
        assertEquals("400", new PciResponseCode(400).toString());
        assertEquals("450", new PciResponseCode(450).toString());
        assertEquals("500", new PciResponseCode(500).toString());
        assertEquals("510", new PciResponseCode(510).toString());

        assertEquals(300, new PciResponseCode(300).getCode());
    }
}
