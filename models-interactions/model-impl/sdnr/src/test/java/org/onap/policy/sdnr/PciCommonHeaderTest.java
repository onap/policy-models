/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;

public class PciCommonHeaderTest {

    private static final String KANSAS = "Kansas";
    private static final String CAN_I_GO_HOME = "Can I go home?";

    @Test
    public void testPciCommonHeader() {
        PciCommonHeader commonHeader = new PciCommonHeader();
        assertNotNull(commonHeader);
        assertNotNull(new PciCommonHeader(commonHeader));
        assertNotEquals(0, commonHeader.hashCode());

        commonHeader.setApiVer(KANSAS);
        assertEquals(KANSAS, commonHeader.getApiVer());

        Map<String, String> flagMap = new HashMap<>();
        commonHeader.setFlags(flagMap);
        assertEquals(flagMap, commonHeader.getFlags());

        Map<String, String> requestMap = new HashMap<>();
        commonHeader.setRequestTrack(requestMap);
        assertEquals(requestMap, commonHeader.getRequestTrack());

        UUID requestId = UUID.randomUUID();
        commonHeader.setRequestId(requestId);
        assertEquals(requestId, commonHeader.getRequestId());

        commonHeader.setSubRequestId(CAN_I_GO_HOME);
        assertEquals(CAN_I_GO_HOME, commonHeader.getSubRequestId());

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

        assertEquals(commonHeader, (Object) commonHeader);
        assertEquals(commonHeader, copiedPciCommonHeader);
        assertNotEquals(commonHeader, null);
        assertNotEquals(commonHeader, (Object) "Hello");

        PciCommonHeader clonedPciCommonHeader = new PciCommonHeader(commonHeader);
        clonedPciCommonHeader.setApiVer(commonHeader.getApiVer());
        clonedPciCommonHeader.setTimeStamp(commonHeader.getTimeStamp());

        assertEquals(commonHeader, clonedPciCommonHeader);

        commonHeader.setApiVer(null);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setApiVer(null);
        assertEquals(commonHeader, copiedPciCommonHeader);
        commonHeader.setApiVer(KANSAS);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setApiVer(KANSAS);
        assertEquals(commonHeader, copiedPciCommonHeader);

        commonHeader.setFlags(null);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setFlags(null);
        assertEquals(commonHeader, copiedPciCommonHeader);
        commonHeader.setFlags(flagMap);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setFlags(flagMap);
        assertEquals(commonHeader, copiedPciCommonHeader);

        commonHeader.setRequestTrack(null);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setRequestTrack(null);
        assertEquals(commonHeader, copiedPciCommonHeader);
        commonHeader.setRequestTrack(requestMap);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setRequestTrack(requestMap);
        assertEquals(commonHeader, copiedPciCommonHeader);


        commonHeader.setRequestId(null);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setRequestId(null);
        assertEquals(commonHeader, copiedPciCommonHeader);
        commonHeader.setRequestId(requestId);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setRequestId(requestId);
        assertEquals(commonHeader, copiedPciCommonHeader);

        commonHeader.setSubRequestId(null);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setSubRequestId(null);
        assertEquals(commonHeader, copiedPciCommonHeader);
        commonHeader.setSubRequestId(CAN_I_GO_HOME);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setSubRequestId(CAN_I_GO_HOME);
        assertEquals(commonHeader, copiedPciCommonHeader);

        commonHeader.setTimeStamp(null);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setTimeStamp(null);
        assertEquals(commonHeader, copiedPciCommonHeader);
        commonHeader.setTimeStamp(timestamp);
        assertNotEquals(commonHeader, copiedPciCommonHeader);
        copiedPciCommonHeader.setTimeStamp(timestamp);
        assertEquals(commonHeader, copiedPciCommonHeader);
    }
}
