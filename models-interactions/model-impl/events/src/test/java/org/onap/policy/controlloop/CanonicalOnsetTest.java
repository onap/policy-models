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

class CanonicalOnsetTest {

    @Test
    void testConstructors() {
        CanonicalOnset onset1 = new CanonicalOnset(new VirtualControlLoopEvent());
        onset1.setRequestId(UUID.randomUUID());
        onset1.setClosedLoopAlarmStart(Instant.now());
        onset1.setClosedLoopAlarmEnd(Instant.now());

        CanonicalOnset onset2 = new CanonicalOnset(new Onset());
        onset2.setRequestId(UUID.randomUUID());
        onset2.setClosedLoopAlarmStart(Instant.ofEpochSecond(Instant.now().getEpochSecond() + 1));
        onset2.setClosedLoopAlarmEnd(Instant.ofEpochSecond(Instant.now().getEpochSecond() + 1));

        CanonicalOnset onset3 = new CanonicalOnset(onset2);

        assertEquals(onset1, onset2);
        assertEquals(onset1, onset3);
        assertEquals(ControlLoopEventStatus.ONSET, onset1.getClosedLoopEventStatus());
        assertEquals(ControlLoopEventStatus.ONSET, onset2.getClosedLoopEventStatus());
        assertEquals(ControlLoopEventStatus.ONSET, onset3.getClosedLoopEventStatus());

        assertNotEquals(onset1.getRequestId(), onset2.getRequestId());
        assertNotEquals(onset1.getClosedLoopAlarmStart(), onset2.getClosedLoopAlarmStart());
        assertNotEquals(onset1.getClosedLoopAlarmEnd(), onset2.getClosedLoopAlarmEnd());

        onset2.setFrom("here");
        assertNotEquals(onset1, onset2);
    }
}