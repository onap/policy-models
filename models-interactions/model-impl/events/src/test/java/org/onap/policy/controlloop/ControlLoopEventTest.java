/*-
 * ============LICENSE_START=======================================================
 * controlloop
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ControlLoopEventTest {

    private class TestControlLoopEvent extends ControlLoopEvent {
        private static final long serialVersionUID = 1L;

        public TestControlLoopEvent() {
            super();
        }

        public TestControlLoopEvent(ControlLoopEvent event) {
            super(event);
        }
    }

    @Test
    void test() {
        ControlLoopEvent event = new TestControlLoopEvent();

        assertEquals("1.0.2", event.getVersion());

        event = new TestControlLoopEvent(null);
        assertEquals("1.0.2", event.getVersion());

        event.setClosedLoopControlName("name");
        assertEquals("name", event.getClosedLoopControlName());

        event.setClosedLoopEventClient("client");
        assertEquals("client", event.getClosedLoopEventClient());

        event.setClosedLoopEventStatus(ControlLoopEventStatus.ONSET);
        assertEquals(ControlLoopEventStatus.ONSET, event.getClosedLoopEventStatus());

        event.setFrom("from");
        assertEquals("from", event.getFrom());

        event.setPolicyName("policyname");
        assertEquals("policyname", event.getPolicyName());

        event.setPolicyScope("scope");
        assertEquals("scope", event.getPolicyScope());

        event.setPolicyVersion("1");
        assertEquals("1", event.getPolicyVersion());

        UUID id = UUID.randomUUID();
        event.setRequestId(id);
        assertEquals(id, event.getRequestId());

        event.setTarget("target");
        assertEquals("target", event.getTarget());

        event.setTargetType(ControlLoopTargetType.VF);
        assertEquals(ControlLoopTargetType.VF, event.getTargetType());

        event.setVersion("foo");
        assertEquals("foo", event.getVersion());

        ControlLoopEvent event2 = new TestControlLoopEvent(event);
        assertTrue(event2.isEventStatusValid());

        event2.setClosedLoopEventStatus(null);
        assertFalse(event2.isEventStatusValid());
    }
}
