/*-
 * ============LICENSE_START=======================================================
 * appc
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

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

        /*
         * Disabling sonar to test equals().
         */
        assertEquals(commonHeader, commonHeader);           // NOSONAR
        assertEquals(commonHeader, copiedCommonHeader);
        assertNotEquals(commonHeader, null);
        assertNotEquals(commonHeader, "Hello");             // NOSONAR

        CommonHeader clonedCommonHeader = new CommonHeader(commonHeader);
        clonedCommonHeader.setApiVer(commonHeader.getApiVer());
        clonedCommonHeader.setTimeStamp(commonHeader.getTimeStamp());

        assertEquals(commonHeader, clonedCommonHeader);

        commonHeader.setApiVer(null);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setApiVer(null);
        assertEquals(commonHeader, copiedCommonHeader);
        commonHeader.setApiVer(KANSAS);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setApiVer(KANSAS);
        assertEquals(commonHeader, copiedCommonHeader);

        commonHeader.setFlags(null);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setFlags(null);
        assertEquals(commonHeader, copiedCommonHeader);
        commonHeader.setFlags(flagSet);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setFlags(flagSet);
        assertEquals(commonHeader, copiedCommonHeader);

        commonHeader.setOriginatorId(null);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setOriginatorId(null);
        assertEquals(commonHeader, copiedCommonHeader);
        commonHeader.setOriginatorId(DOROTHY);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setOriginatorId(DOROTHY);
        assertEquals(commonHeader, copiedCommonHeader);

        commonHeader.setRequestId(null);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setRequestId(null);
        assertEquals(commonHeader, copiedCommonHeader);
        commonHeader.setRequestId(requestId);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setRequestId(requestId);
        assertEquals(commonHeader, copiedCommonHeader);

        commonHeader.setRequestTrack(null);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setRequestTrack(null);
        assertEquals(commonHeader, copiedCommonHeader);
        commonHeader.setRequestTrack(requestTrackSet);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setRequestTrack(requestTrackSet);
        assertEquals(commonHeader, copiedCommonHeader);

        commonHeader.setSubRequestId(null);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setSubRequestId(null);
        assertEquals(commonHeader, copiedCommonHeader);
        commonHeader.setSubRequestId(CAN_I_GO_HOME);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setSubRequestId(CAN_I_GO_HOME);
        assertEquals(commonHeader, copiedCommonHeader);

        commonHeader.setTimeStamp(null);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setTimeStamp(null);
        assertEquals(commonHeader, copiedCommonHeader);
        commonHeader.setTimeStamp(timestamp);
        assertNotEquals(commonHeader, copiedCommonHeader);
        copiedCommonHeader.setTimeStamp(timestamp);
        assertEquals(commonHeader, copiedCommonHeader);
    }
}
