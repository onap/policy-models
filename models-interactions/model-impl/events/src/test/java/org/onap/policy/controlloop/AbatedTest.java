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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AbatedTest {

    @Test
    void testConstructors() {
        VirtualControlLoopEvent event = new VirtualControlLoopEvent();
        event.setClosedLoopEventStatus(ControlLoopEventStatus.ABATED);
        event.setClosedLoopAlarmStart(Instant.now());
        event.setClosedLoopAlarmEnd(Instant.now());

        Abated abated = new Abated(event);
        assertEquals(abated, event);
        assertEquals(event.getClosedLoopAlarmStart(), abated.getClosedLoopAlarmStart());
        assertEquals(ControlLoopEventStatus.ABATED, abated.getClosedLoopEventStatus());

        abated.setClosedLoopAlarmEnd(Instant.ofEpochSecond(Instant.now().getEpochSecond() + 1));
        assertNotEquals(abated, event);

        assertEquals(new Abated(abated), abated);
    }

    @Test
    void testSetClosedLoopEventStatus() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Abated().setClosedLoopEventStatus(ControlLoopEventStatus.ONSET));
    }
}