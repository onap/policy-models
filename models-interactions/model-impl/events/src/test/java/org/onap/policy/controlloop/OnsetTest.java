/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.time.Instant;
import org.junit.Test;

public class OnsetTest {

    @Test
    public void testConstructors() {
        VirtualControlLoopEvent event = new VirtualControlLoopEvent();
        event.setClosedLoopEventStatus(ControlLoopEventStatus.ONSET);
        event.setClosedLoopAlarmStart(Instant.now());
        event.setClosedLoopAlarmEnd(Instant.now());

        Onset onset = new Onset(event);
        assertEquals(onset, event);
        assertEquals(event.getClosedLoopAlarmStart(), onset.getClosedLoopAlarmStart());
        assertEquals(ControlLoopEventStatus.ONSET, onset.getClosedLoopEventStatus());
        assertEquals(event.getClosedLoopAlarmEnd(), onset.getClosedLoopAlarmEnd());

        onset.setClosedLoopAlarmEnd(Instant.now());
        assertNotEquals(onset, event);

        assertEquals(new Onset(onset), onset);
    }

    @Test
    public void testSetClosedLoopEventStatus() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Onset().setClosedLoopEventStatus(ControlLoopEventStatus.ABATED));
    }
}