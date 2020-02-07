/*-
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

package org.onap.policy.controlloop.actorserviceprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import org.junit.Before;
import org.junit.Test;

public class CallbackManagerTest {

    private CallbackManager mgr;

    @Before
    public void setUp() {
        mgr = new CallbackManager();
    }

    @Test
    public void testCanStart_testGetStartTime() {
        // null until canXxx() is called
        assertNull(mgr.getStartTime());

        assertTrue(mgr.canStart());

        Instant time = mgr.getStartTime();
        assertNotNull(time);
        assertNull(mgr.getEndTime());

        // false for now on
        assertFalse(mgr.canStart());
        assertFalse(mgr.canStart());

        assertEquals(time, mgr.getStartTime());
    }

    @Test
    public void testCanEnd_testGetEndTime() {
        // null until canXxx() is called
        assertNull(mgr.getEndTime());
        assertNull(mgr.getEndTime());

        assertTrue(mgr.canEnd());

        Instant time = mgr.getEndTime();
        assertNotNull(time);
        assertNull(mgr.getStartTime());

        // false for now on
        assertFalse(mgr.canEnd());
        assertFalse(mgr.canEnd());

        assertEquals(time, mgr.getEndTime());
    }

    @Test
    public void testRun() {
        mgr.run();

        assertNotNull(mgr.getStartTime());
        assertNotNull(mgr.getEndTime());

        assertFalse(mgr.canStart());
        assertFalse(mgr.canEnd());
    }
}
