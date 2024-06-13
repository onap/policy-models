/*-
 * ============LICENSE_START=======================================================
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

package org.onap.policy.guard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import org.junit.jupiter.api.Test;

class OperationsHistoryTest {

    @Test
    void test() {
        OperationsHistory dao = new OperationsHistory();

        dao.setActor("my-actor");
        dao.setClosedLoopName("cl-name");
        Date endDate = new Date();
        dao.setEndtime(endDate);
        dao.setId(100L);
        dao.setMessage("my-message");
        dao.setOperation("my-operation");
        dao.setOutcome("my-outcome");
        dao.setRequestId("my-request");
        Date startDate = new Date(endDate.getTime() - 1);
        dao.setStarttime(startDate);
        dao.setSubrequestId("my-sub");
        dao.setTarget("my-target");

        assertEquals("my-actor", dao.getActor());
        assertEquals("cl-name", dao.getClosedLoopName());
        assertEquals(endDate, dao.getEndtime());
        assertEquals(100L, dao.getId().longValue());
        assertEquals("my-message", dao.getMessage());
        assertEquals("my-operation", dao.getOperation());
        assertEquals("my-outcome", dao.getOutcome());
        assertEquals("my-request", dao.getRequestId());
        assertEquals(startDate, dao.getStarttime());
        assertEquals("my-sub", dao.getSubrequestId());
        assertEquals("my-target", dao.getTarget());

        assertTrue(dao.toString().startsWith("OperationsHistory"));

        int hc = dao.hashCode();
        dao.setId(101L);
        assertNotEquals(hc, dao.hashCode());

        assertEquals(dao, (Object) dao);
        assertNotEquals(dao, new OperationsHistory());
    }
}
