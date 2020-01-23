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

package org.onap.policy.controlloop.actorserviceprovider.controlloop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.controlloop.VirtualControlLoopEvent;

public class ControlLoopEventContextTest {

    private VirtualControlLoopEvent event;
    private ControlLoopEventContext context;

    @Before
    public void setUp() {
        event = new VirtualControlLoopEvent();
        context = new ControlLoopEventContext(event);
    }

    @Test
    public void testControlLoopEventContext() {
        assertSame(event, context.getEvent());
    }

    @Test
    public void testContains_testGetProperty_testSetProperty() {
        context.setProperty("abc", "a string");
        context.setProperty("def", 100);

        assertFalse(context.contains("ghi"));

        String strValue = context.getProperty("abc");
        assertEquals("a string", strValue);

        int intValue = context.getProperty("def");
        assertEquals(100, intValue);
    }
}
