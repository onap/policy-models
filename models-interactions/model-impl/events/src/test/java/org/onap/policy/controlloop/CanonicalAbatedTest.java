/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class CanonicalAbatedTest {

    @Test
    public void testConstructors() {
        CanonicalAbated abated1 = new CanonicalAbated(new VirtualControlLoopEvent());
        abated1.setRequestId(UUID.randomUUID());
        abated1.setClosedLoopAlarmStart(Instant.now());
        abated1.setClosedLoopAlarmEnd(Instant.now());

        CanonicalAbated abated2 = new CanonicalAbated(new Abated());
        abated2.setRequestId(UUID.randomUUID());
        abated2.setClosedLoopAlarmStart(Instant.ofEpochSecond(Instant.now().getEpochSecond() + 1));
        abated2.setClosedLoopAlarmEnd(Instant.ofEpochSecond(Instant.now().getEpochSecond() + 1));

        CanonicalAbated abated3 = new CanonicalAbated(abated2);

        assertEquals(abated1, abated2);
        assertEquals(abated1, abated3);
        assertEquals(ControlLoopEventStatus.ABATED, abated1.getClosedLoopEventStatus());
        assertEquals(ControlLoopEventStatus.ABATED, abated2.getClosedLoopEventStatus());
        assertEquals(ControlLoopEventStatus.ABATED, abated3.getClosedLoopEventStatus());

        assertNotEquals(abated1.getRequestId(), abated2.getRequestId());
        assertNotEquals(abated1.getClosedLoopAlarmStart(), abated2.getClosedLoopAlarmStart());
        assertNotEquals(abated1.getClosedLoopAlarmEnd(), abated2.getClosedLoopAlarmEnd());

        abated2.setFrom("here");
        assertNotEquals(abated1, abated2);
    }
}