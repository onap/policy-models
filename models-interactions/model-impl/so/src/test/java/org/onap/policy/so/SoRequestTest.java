/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.so;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SoRequestTest {

    @Test
    void testConstructor() {
        SoRequest obj = new SoRequest();

        assertNull(obj.getFinishTime());
        assertNull(obj.getRequestDetails());
        assertNull(obj.getRequestId());
        assertNull(obj.getRequestScope());
        assertNull(obj.getRequestStatus());
        assertNull(obj.getRequestType());
        assertNull(obj.getStartTime());
    }

    @Test
    void testSetGet() {
        SoRequest obj = new SoRequest();

        LocalDateTime finishTime = LocalDateTime.now();
        obj.setFinishTime(finishTime);
        assertEquals(finishTime, obj.getFinishTime());

        UUID uuid = UUID.randomUUID();
        obj.setRequestId(uuid);
        assertEquals(uuid, obj.getRequestId());

        obj.setRequestScope("requestScope");
        assertEquals("requestScope", obj.getRequestScope());

        SoRequestStatus requestStatus = new SoRequestStatus();
        obj.setRequestStatus(requestStatus);
        assertEquals(requestStatus, obj.getRequestStatus());

        obj.setRequestType("requestType");
        assertEquals("requestType", obj.getRequestType());

        obj.setOperationType(SoOperationType.DELETE_VF_MODULE);
        assertEquals(SoOperationType.DELETE_VF_MODULE, obj.getOperationType());

        LocalDateTime startTime = LocalDateTime.now();
        obj.setStartTime(startTime.toString());
        assertEquals(startTime.toString(), obj.getStartTime());
    }
}
