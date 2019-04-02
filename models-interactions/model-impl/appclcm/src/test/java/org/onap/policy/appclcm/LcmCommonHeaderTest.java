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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

public class LcmCommonHeaderTest {

    @Test
    public void testLcmCommonHeader() {
        LcmCommonHeader commonHeader = new LcmCommonHeader();
        assertNotNull(commonHeader);
        assertNotNull(new LcmCommonHeader(commonHeader));
        assertNotEquals(0, commonHeader.hashCode());

        commonHeader.setApiVer("Kansas");
        assertEquals("Kansas", commonHeader.getApiVer());

        Map<String, String> flagMap = new HashMap<>();
        commonHeader.setFlags(flagMap);
        assertEquals(flagMap, commonHeader.getFlags());

        commonHeader.setOriginatorId("Dorothy");
        assertEquals("Dorothy", commonHeader.getOriginatorId());

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

        LcmCommonHeader copiedLcmCommonHeader = new LcmCommonHeader();
        copiedLcmCommonHeader.setApiVer(commonHeader.getApiVer());
        copiedLcmCommonHeader.setFlags(commonHeader.getFlags());
        copiedLcmCommonHeader.setOriginatorId(commonHeader.getOriginatorId());
        copiedLcmCommonHeader.setRequestId(commonHeader.getRequestId());
        copiedLcmCommonHeader.setSubRequestId(commonHeader.getSubRequestId());
        copiedLcmCommonHeader.setTimeStamp(commonHeader.getTimeStamp());

        assertTrue(commonHeader.equals(commonHeader));
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));
        assertFalse(commonHeader.equals(null));
        assertFalse(commonHeader.equals("Hello"));

        LcmCommonHeader clonedLcmCommonHeader = new LcmCommonHeader(commonHeader);
        clonedLcmCommonHeader.setApiVer(commonHeader.getApiVer());
        clonedLcmCommonHeader.setTimeStamp(commonHeader.getTimeStamp());

        assertTrue(commonHeader.equals(clonedLcmCommonHeader));

        commonHeader.setApiVer(null);
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setApiVer(null);
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));
        commonHeader.setApiVer("Kansas");
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setApiVer("Kansas");
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));

        commonHeader.setFlags(null);
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setFlags(null);
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));
        commonHeader.setFlags(flagMap);
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setFlags(flagMap);
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));

        commonHeader.setOriginatorId(null);
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setOriginatorId(null);
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));
        commonHeader.setOriginatorId("Dorothy");
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setOriginatorId("Dorothy");
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));

        commonHeader.setRequestId(null);
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setRequestId(null);
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));
        commonHeader.setRequestId(requestId);
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setRequestId(requestId);
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));

        commonHeader.setSubRequestId(null);
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setSubRequestId(null);
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));
        commonHeader.setSubRequestId("Can I go home?");
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setSubRequestId("Can I go home?");
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));

        commonHeader.setTimeStamp(null);
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setTimeStamp(null);
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));
        commonHeader.setTimeStamp(timestamp);
        assertFalse(commonHeader.equals(copiedLcmCommonHeader));
        copiedLcmCommonHeader.setTimeStamp(timestamp);
        assertTrue(commonHeader.equals(copiedLcmCommonHeader));
    }
}
