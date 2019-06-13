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

package org.onap.policy.appc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

public class CommonHeaderTest {

    private static final String KANSAS = "Kansas";
    private static final String DOROTHY = "Dorothy";
    private static final String CAN_I_GO_HOME = "Can I go home?";

    @Test
    public void testCommonHeader() {
        CommonHeader commonHeader = new CommonHeader();
        assertNotNull(commonHeader);
        assertNotNull(new CommonHeader(commonHeader));
        assertNotEquals(0, commonHeader.hashCode());

        commonHeader.setApiVer(KANSAS);
        assertEquals(KANSAS, commonHeader.getApiVer());

        List<Map<String, String>> flagSet = new ArrayList<>();
        commonHeader.setFlags(flagSet);
        assertEquals(flagSet, commonHeader.getFlags());

        commonHeader.setOriginatorId(DOROTHY);
        assertEquals(DOROTHY, commonHeader.getOriginatorId());

        UUID requestId = UUID.randomUUID();
        commonHeader.setRequestId(requestId);
        assertEquals(requestId, commonHeader.getRequestId());

        List<String> requestTrackSet = new ArrayList<>();
        commonHeader.setRequestTrack(requestTrackSet);
        assertEquals(requestTrackSet, commonHeader.getRequestTrack());

        commonHeader.setSubRequestId(CAN_I_GO_HOME);
        assertEquals(CAN_I_GO_HOME, commonHeader.getSubRequestId());

        Instant timestamp = Instant.now();
        commonHeader.setTimeStamp(timestamp);
        assertEquals(timestamp, commonHeader.getTimeStamp());

        assertNotEquals(0, commonHeader.hashCode());

        assertEquals("CommonHeader [TimeStamp=", commonHeader.toString().substring(0, 24));

        CommonHeader copiedCommonHeader = new CommonHeader();
        copiedCommonHeader.setApiVer(commonHeader.getApiVer());
        copiedCommonHeader.setFlags(commonHeader.getFlags());
        copiedCommonHeader.setOriginatorId(commonHeader.getOriginatorId());
        copiedCommonHeader.setRequestId(commonHeader.getRequestId());
        copiedCommonHeader.setRequestTrack(commonHeader.getRequestTrack());
        copiedCommonHeader.setSubRequestId(commonHeader.getSubRequestId());
        copiedCommonHeader.setTimeStamp(commonHeader.getTimeStamp());

        assertTrue(commonHeader.equals(commonHeader));
        assertTrue(commonHeader.equals(copiedCommonHeader));
        assertFalse(commonHeader.equals(null));
        assertFalse(commonHeader.equals("Hello"));

        CommonHeader clonedCommonHeader = new CommonHeader(commonHeader);
        clonedCommonHeader.setApiVer(commonHeader.getApiVer());
        clonedCommonHeader.setTimeStamp(commonHeader.getTimeStamp());

        assertTrue(commonHeader.equals(clonedCommonHeader));

        commonHeader.setApiVer(null);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setApiVer(null);
        assertTrue(commonHeader.equals(copiedCommonHeader));
        commonHeader.setApiVer(KANSAS);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setApiVer(KANSAS);
        assertTrue(commonHeader.equals(copiedCommonHeader));

        commonHeader.setFlags(null);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setFlags(null);
        assertTrue(commonHeader.equals(copiedCommonHeader));
        commonHeader.setFlags(flagSet);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setFlags(flagSet);
        assertTrue(commonHeader.equals(copiedCommonHeader));

        commonHeader.setOriginatorId(null);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setOriginatorId(null);
        assertTrue(commonHeader.equals(copiedCommonHeader));
        commonHeader.setOriginatorId(DOROTHY);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setOriginatorId(DOROTHY);
        assertTrue(commonHeader.equals(copiedCommonHeader));

        commonHeader.setRequestId(null);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setRequestId(null);
        assertTrue(commonHeader.equals(copiedCommonHeader));
        commonHeader.setRequestId(requestId);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setRequestId(requestId);
        assertTrue(commonHeader.equals(copiedCommonHeader));

        commonHeader.setRequestTrack(null);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setRequestTrack(null);
        assertTrue(commonHeader.equals(copiedCommonHeader));
        commonHeader.setRequestTrack(requestTrackSet);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setRequestTrack(requestTrackSet);
        assertTrue(commonHeader.equals(copiedCommonHeader));

        commonHeader.setSubRequestId(null);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setSubRequestId(null);
        assertTrue(commonHeader.equals(copiedCommonHeader));
        commonHeader.setSubRequestId(CAN_I_GO_HOME);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setSubRequestId(CAN_I_GO_HOME);
        assertTrue(commonHeader.equals(copiedCommonHeader));

        commonHeader.setTimeStamp(null);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setTimeStamp(null);
        assertTrue(commonHeader.equals(copiedCommonHeader));
        commonHeader.setTimeStamp(timestamp);
        assertFalse(commonHeader.equals(copiedCommonHeader));
        copiedCommonHeader.setTimeStamp(timestamp);
        assertTrue(commonHeader.equals(copiedCommonHeader));
    }
}
