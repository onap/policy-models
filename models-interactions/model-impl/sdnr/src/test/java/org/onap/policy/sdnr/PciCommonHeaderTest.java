/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
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

package org.onap.policy.sdnr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

public class PciCommonHeaderTest {

    @Test
    public void testPciCommonHeader() {
        PciCommonHeader commonHeader = new PciCommonHeader();
        assertNotNull(commonHeader);
        assertNotNull(new PciCommonHeader(commonHeader));
        assertNotEquals(0, commonHeader.hashCode());

        commonHeader.setApiVer("Kansas");
        assertEquals("Kansas", commonHeader.getApiVer());

        Map<String, String> flagMap = new HashMap<>();
        commonHeader.setFlags(flagMap);
        assertEquals(flagMap, commonHeader.getFlags());

        Map<String, String> requestMap = new HashMap<>();
        commonHeader.setRequestTrack(requestMap);
        assertEquals(requestMap, commonHeader.getRequestTrack());

        UUID requestId = UUID.randomUUID();
        commonHeader.setRequestId(requestId);
        assertEquals(requestId, commonHeader.getRequestId());

        commonHeader.setSubRequestId("Can I go home?");
        assertEquals("Can I go home?", commonHeader.getSubRequestId());

        Instant timestamp = Instant.now();
        commonHeader.setTimeStamp(timestamp);
        assertEquals(timestamp, commonHeader.getTimeStamp());

        assertNotEquals(0, commonHeader.hashCode());

        assertEquals("CommonHeader [timeStamp=", commonHeader.toString().substring(0, 24));

        PciCommonHeader copiedPciCommonHeader = new PciCommonHeader();
        copiedPciCommonHeader.setApiVer(commonHeader.getApiVer());
        copiedPciCommonHeader.setFlags(commonHeader.getFlags());
        copiedPciCommonHeader.setRequestId(commonHeader.getRequestId());
        copiedPciCommonHeader.setSubRequestId(commonHeader.getSubRequestId());
        copiedPciCommonHeader.setTimeStamp(commonHeader.getTimeStamp());

        assertTrue(commonHeader.equals(commonHeader));
        assertTrue(commonHeader.equals(copiedPciCommonHeader));
        assertFalse(commonHeader.equals(null));
        assertFalse(commonHeader.equals("Hello"));

        PciCommonHeader clonedPciCommonHeader = new PciCommonHeader(commonHeader);
        clonedPciCommonHeader.setApiVer(commonHeader.getApiVer());
        clonedPciCommonHeader.setTimeStamp(commonHeader.getTimeStamp());

        assertTrue(commonHeader.equals(clonedPciCommonHeader));

        commonHeader.setApiVer(null);
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setApiVer(null);
        assertTrue(commonHeader.equals(copiedPciCommonHeader));
        commonHeader.setApiVer("Kansas");
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setApiVer("Kansas");
        assertTrue(commonHeader.equals(copiedPciCommonHeader));

        commonHeader.setFlags(null);
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setFlags(null);
        assertTrue(commonHeader.equals(copiedPciCommonHeader));
        commonHeader.setFlags(flagMap);
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setFlags(flagMap);
        assertTrue(commonHeader.equals(copiedPciCommonHeader));

        commonHeader.setRequestTrack(null);
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setRequestTrack(null);
        assertTrue(commonHeader.equals(copiedPciCommonHeader));
        commonHeader.setRequestTrack(requestMap);
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setRequestTrack(requestMap);
        assertTrue(commonHeader.equals(copiedPciCommonHeader));


        commonHeader.setRequestId(null);
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setRequestId(null);
        assertTrue(commonHeader.equals(copiedPciCommonHeader));
        commonHeader.setRequestId(requestId);
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setRequestId(requestId);
        assertTrue(commonHeader.equals(copiedPciCommonHeader));

        commonHeader.setSubRequestId(null);
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setSubRequestId(null);
        assertTrue(commonHeader.equals(copiedPciCommonHeader));
        commonHeader.setSubRequestId("Can I go home?");
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setSubRequestId("Can I go home?");
        assertTrue(commonHeader.equals(copiedPciCommonHeader));

        commonHeader.setTimeStamp(null);
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setTimeStamp(null);
        assertTrue(commonHeader.equals(copiedPciCommonHeader));
        commonHeader.setTimeStamp(timestamp);
        assertFalse(commonHeader.equals(copiedPciCommonHeader));
        copiedPciCommonHeader.setTimeStamp(timestamp);
        assertTrue(commonHeader.equals(copiedPciCommonHeader));
    }
}
