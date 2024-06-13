/*-
 * ============LICENSE_START=======================================================
 * sdnr
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
 * Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

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

        assertEquals("PciCommonHeader(timeStamp=", commonHeader.toString().substring(0, 26));

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

        checkField(KANSAS, PciCommonHeader::setApiVer);
        checkField(flagMap, PciCommonHeader::setFlags);
        checkField(requestMap, PciCommonHeader::setRequestTrack);
        checkField(requestId, PciCommonHeader::setRequestId);
        checkField(CAN_I_GO_HOME, PciCommonHeader::setSubRequestId);
        checkField(timestamp, PciCommonHeader::setTimeStamp);
    }

    private <T> void checkField(T value, BiConsumer<PciCommonHeader, T> setter) {
        PciCommonHeader details1 = new PciCommonHeader();
        PciCommonHeader details2 = new PciCommonHeader(details1);

        setter.accept(details2, null);

        setter.accept(details1, value);
        assertNotEquals(details1, details2);

        setter.accept(details2, value);
        assertEquals(details1, details2);

        setter.accept(details1, null);
        assertNotEquals(details1, details2);
    }
}
