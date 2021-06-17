/*-
 * ============LICENSE_START=======================================================
 * appc
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
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

        assertThat(commonHeader.toString()).startsWith("CommonHeader(timeStamp=");

        CommonHeader copiedCommonHeader = new CommonHeader();
        copiedCommonHeader.setApiVer(commonHeader.getApiVer());
        copiedCommonHeader.setFlags(commonHeader.getFlags());
        copiedCommonHeader.setOriginatorId(commonHeader.getOriginatorId());
        copiedCommonHeader.setRequestId(commonHeader.getRequestId());
        copiedCommonHeader.setRequestTrack(commonHeader.getRequestTrack());
        copiedCommonHeader.setSubRequestId(commonHeader.getSubRequestId());
        copiedCommonHeader.setTimeStamp(commonHeader.getTimeStamp());

        assertEquals(commonHeader, (Object) commonHeader);
        assertEquals(commonHeader, copiedCommonHeader);
        assertNotEquals(commonHeader, null);
        assertNotEquals(commonHeader, (Object) "Hello");

        CommonHeader clonedCommonHeader = new CommonHeader(commonHeader);
        clonedCommonHeader.setApiVer(commonHeader.getApiVer());
        clonedCommonHeader.setTimeStamp(commonHeader.getTimeStamp());

        assertEquals(commonHeader, clonedCommonHeader);

        checkField(KANSAS, CommonHeader::setApiVer);
        checkField(flagSet, CommonHeader::setFlags);
        checkField(DOROTHY, CommonHeader::setOriginatorId);
        checkField(requestId, CommonHeader::setRequestId);
        checkField(requestTrackSet, CommonHeader::setRequestTrack);
        checkField(CAN_I_GO_HOME, CommonHeader::setSubRequestId);
        checkField(timestamp, CommonHeader::setTimeStamp);
    }

    private <T> void checkField(T value, BiConsumer<CommonHeader, T> setter) {
        CommonHeader header1 = new CommonHeader();
        CommonHeader header2 = new CommonHeader(header1);

        setter.accept(header2, null);

        setter.accept(header1, value);
        assertNotEquals(header1, header2);

        setter.accept(header2, value);
        assertEquals(header1, header2);

        setter.accept(header1, null);
        assertNotEquals(header1, header2);
    }
}
