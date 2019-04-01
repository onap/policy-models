/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 *
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved
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

package org.onap.policy.so;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;

import org.junit.Test;

public class SoAsyncRequestStatusTest {

    @Test
    public void testConstructor() {
        SoAsyncRequestStatus obj = new SoAsyncRequestStatus();

        assertTrue(obj.getCorrelator() == null);
        assertTrue(obj.getFinishTime() == null);
        assertTrue(obj.getInstanceReferences() == null);
        assertTrue(obj.getRequestId() == null);
        assertTrue(obj.getRequestScope() == null);
        assertTrue(obj.getRequestStatus() == null);
        assertTrue(obj.getStartTime() == null);
    }

    @Test
    public void testSetGet() {
        SoAsyncRequestStatus obj = new SoAsyncRequestStatus();

        obj.setCorrelator("correlator");
        assertEquals("correlator", obj.getCorrelator());

        LocalDateTime finishTime = LocalDateTime.now();
        obj.setFinishTime(finishTime);
        assertEquals(finishTime, obj.getFinishTime());

        SoInstanceReferences instanceReferences = new SoInstanceReferences();
        obj.setInstanceReferences(instanceReferences);
        assertEquals(instanceReferences, obj.getInstanceReferences());

        obj.setRequestId("requestId");
        assertEquals("requestId", obj.getRequestId());

        obj.setRequestScope("requestScope");
        assertEquals("requestScope", obj.getRequestScope());

        SoRequestStatus requestStatus = new SoRequestStatus();
        obj.setRequestStatus(requestStatus);
        assertEquals(requestStatus, obj.getRequestStatus());

        obj.setRequestType("requestType");
        assertEquals("requestType", obj.getRequestType());

        LocalDateTime startTime = LocalDateTime.now();
        obj.setStartTime(startTime);
        assertEquals(startTime, obj.getStartTime());

    }
}
