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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.onap.policy.controlloop.util.Serialization;

public class ControlLoopNotificationTest {

    private class TestControlLoopNotification extends ControlLoopNotification {
        private static final long serialVersionUID = 1L;

        public TestControlLoopNotification() {
            super();
        }

        public TestControlLoopNotification(ControlLoopEvent event) {
            super(event);
        }
    }

    @Test
    public void test() {
        ControlLoopNotification notification = new TestControlLoopNotification();

        assertEquals("1.0.2", notification.getVersion());

        notification.setClosedLoopControlName("name");
        assertEquals("name", notification.getClosedLoopControlName());

        notification.setClosedLoopEventClient("client");
        assertEquals("client", notification.getClosedLoopEventClient());

        notification.setFrom("from");
        assertEquals("from", notification.getFrom());

        notification.setHistory(Collections.emptyList());
        assertTrue(notification.getHistory().isEmpty());

        notification.setMessage("message");
        assertEquals("message", notification.getMessage());

        notification.setNotification(ControlLoopNotificationType.ACTIVE);
        assertEquals(ControlLoopNotificationType.ACTIVE, notification.getNotification());

        ZonedDateTime time = ZonedDateTime.now(ZoneOffset.UTC);
        notification.setNotificationTime(time);
        assertEquals(time, notification.getNotificationTime());

        notification.setOpsClTimer(Integer.valueOf(1000));
        assertEquals(Integer.valueOf(1000), notification.getOpsClTimer());

        notification.setPolicyName("name");
        assertEquals("name", notification.getPolicyName());

        notification.setPolicyScope("scope");
        assertEquals("scope", notification.getPolicyScope());

        notification.setPolicyVersion("1");
        assertEquals("1", notification.getPolicyVersion());

        UUID id = UUID.randomUUID();
        notification.setRequestId(id);
        assertEquals(id, notification.getRequestId());

        notification.setTarget("target");
        assertEquals("target", notification.getTarget());

        notification.setTargetType(ControlLoopTargetType.VFC);
        assertEquals(ControlLoopTargetType.VFC, notification.getTargetType());

        VirtualControlLoopEvent event = new VirtualControlLoopEvent();
        event.setClosedLoopControlName("controlloop");

        TestControlLoopNotification notification2 = new TestControlLoopNotification(event);
        assertEquals("controlloop", notification2.getClosedLoopControlName());

        notification2.setVersion("1");
        assertEquals("1", notification2.getVersion());

        String json = Serialization.gsonPretty.toJson(notification);

        TestControlLoopNotification notification3 = Serialization.gson.fromJson(json,
                TestControlLoopNotification.class);

        //
        // There is no equals for the class - chose not to create one
        //
        assertEquals(notification.getRequestId(), notification3.getRequestId());

    }
}
