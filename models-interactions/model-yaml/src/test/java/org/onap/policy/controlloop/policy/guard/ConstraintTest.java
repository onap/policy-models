/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.policy.guard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ConstraintTest {

    private static final String TIME_WINDOW_VALUE = "timeWindowValue";
    private static final String TIME_WINDOW_KEY = "timeWindowKey";
    private static final String BLACKLIST_ITEM = "blacklist item";

    @Test
    public void testConstraint() {
        Constraint constraint = new Constraint();

        assertNull(constraint.getFreq_limit_per_target());
        assertNull(constraint.getTime_window());
        assertNull(constraint.getActive_time_range());
        assertNull(constraint.getBlacklist());
    }

    @Test
    public void testGetAndSetFreq_limit_per_target() {
        Integer freqLimitPerTarget = 10;
        Constraint constraint = new Constraint();
        constraint.setFreq_limit_per_target(freqLimitPerTarget);
        assertEquals(freqLimitPerTarget, constraint.getFreq_limit_per_target());
    }

    @Test
    public void testGetAndSetTime_window() {
        Map<String, String> timeWindow = new HashMap<>();
        timeWindow.put(TIME_WINDOW_KEY, TIME_WINDOW_VALUE);
        Constraint constraint = new Constraint();
        constraint.setTime_window(timeWindow);
        assertEquals(timeWindow, constraint.getTime_window());
    }

    @Test
    public void testGetAndSetActive_time_range() {
        Map<String, String> activeTimeRange = new HashMap<>();
        activeTimeRange.put(TIME_WINDOW_KEY, TIME_WINDOW_VALUE);
        Constraint constraint = new Constraint();
        constraint.setActive_time_range(activeTimeRange);;
        assertEquals(activeTimeRange, constraint.getActive_time_range());
    }

    @Test
    public void testGetAndSetBlacklist() {
        List<String> blacklist = new ArrayList<>();
        blacklist.add(BLACKLIST_ITEM);
        Constraint constraint = new Constraint();
        constraint.setBlacklist(blacklist);
        assertEquals(blacklist, constraint.getBlacklist());
    }

    @Test
    public void testConstraintIntegerMapOfStringString() {
        Integer freqLimitPerTarget = 10;
        Map<String, String> timeWindow = new HashMap<>();

        Constraint constraint = new Constraint(freqLimitPerTarget, timeWindow);

        assertEquals(freqLimitPerTarget, constraint.getFreq_limit_per_target());
        assertEquals(timeWindow, constraint.getTime_window());
        assertNull(constraint.getActive_time_range());
        assertNull(constraint.getBlacklist());
    }

    @Test
    public void testConstraintListOfString() {
        List<String> blacklist = new ArrayList<>();
        blacklist.add(BLACKLIST_ITEM);
        Constraint constraint = new Constraint(blacklist);

        assertNull(constraint.getFreq_limit_per_target());
        assertNull(constraint.getTime_window());
        assertNull(constraint.getActive_time_range());
        assertEquals(blacklist, constraint.getBlacklist());
    }

    @Test
    public void testConstraintIntegerMapOfStringStringListOfString() {
        Integer freqLimitPerTarget = 10;
        Map<String, String> timeWindow = new HashMap<>();
        List<String> blacklist = new ArrayList<>();
        blacklist.add(BLACKLIST_ITEM);
        Constraint constraint = new Constraint(freqLimitPerTarget, timeWindow, blacklist);

        assertEquals(freqLimitPerTarget, constraint.getFreq_limit_per_target());
        assertEquals(timeWindow, constraint.getTime_window());
        assertNull(constraint.getActive_time_range());
        assertEquals(blacklist, constraint.getBlacklist());
    }

    @Test
    public void testConstraintIntegerMapOfStringStringMapOfStringString() {
        Integer freqLimitPerTarget = 10;
        Map<String, String> timeWindow = new HashMap<>();
        Map<String, String> activeTimeRange = new HashMap<>();
        activeTimeRange.put(TIME_WINDOW_KEY, TIME_WINDOW_VALUE);
        Constraint constraint = new Constraint(freqLimitPerTarget, timeWindow, activeTimeRange);

        assertEquals(freqLimitPerTarget, constraint.getFreq_limit_per_target());
        assertEquals(timeWindow, constraint.getTime_window());
        assertEquals(activeTimeRange, constraint.getActive_time_range());
        assertNull(constraint.getBlacklist());

    }

    @Test
    public void testConstraintIntegerMapOfStringStringMapOfStringStringListOfString() {
        Integer freqLimitPerTarget = 10;
        Map<String, String> timeWindow = new HashMap<>();
        Map<String, String> activeTimeRange = new HashMap<>();
        activeTimeRange.put(TIME_WINDOW_KEY, TIME_WINDOW_VALUE);
        List<String> blacklist = new ArrayList<>();
        blacklist.add(BLACKLIST_ITEM);
        Constraint constraint = new Constraint(freqLimitPerTarget, timeWindow, activeTimeRange, blacklist);

        assertEquals(freqLimitPerTarget, constraint.getFreq_limit_per_target());
        assertEquals(timeWindow, constraint.getTime_window());
        assertEquals(activeTimeRange, constraint.getActive_time_range());
        assertEquals(blacklist, constraint.getBlacklist());
    }

    @Test
    public void testConstraintConstraint() {
        Integer freqLimitPerTarget = 10;
        Map<String, String> timeWindow = new HashMap<>();
        Map<String, String> activeTimeRange = new HashMap<>();
        activeTimeRange.put(TIME_WINDOW_KEY, TIME_WINDOW_VALUE);
        List<String> blacklist = new ArrayList<>();
        blacklist.add(BLACKLIST_ITEM);
        Constraint constraint1 = new Constraint(freqLimitPerTarget, timeWindow, activeTimeRange, blacklist);
        Constraint constraint2 = new Constraint(constraint1);

        assertEquals(freqLimitPerTarget, constraint2.getFreq_limit_per_target());
        assertEquals(timeWindow, constraint2.getTime_window());
        assertEquals(activeTimeRange, constraint2.getActive_time_range());
        assertEquals(blacklist, constraint2.getBlacklist());
    }

    @Test
    public void testIsValid() {
        Integer freqLimitPerTarget = 10;
        final Map<String, String> timeWindow = new HashMap<>();

        Constraint constraint = new Constraint();
        assertTrue(constraint.isValid());

        constraint.setFreq_limit_per_target(freqLimitPerTarget);
        assertFalse(constraint.isValid());

        constraint.setTime_window(timeWindow);
        assertTrue(constraint.isValid());

        constraint.setFreq_limit_per_target(null);
        assertFalse(constraint.isValid());
    }

    @Test
    public void testToString() {
        Integer freqLimitPerTarget = 10;
        Map<String, String> timeWindow = new HashMap<>();
        Map<String, String> activeTimeRange = new HashMap<>();
        activeTimeRange.put(TIME_WINDOW_KEY, TIME_WINDOW_VALUE);
        List<String> blacklist = new ArrayList<>();
        blacklist.add(BLACKLIST_ITEM);
        Constraint constraint = new Constraint(freqLimitPerTarget, timeWindow, activeTimeRange, blacklist);

        assertEquals(constraint.toString(), "Constraint [freq_limit_per_target=" + freqLimitPerTarget + ", time_window="
                + timeWindow + ", active_time_range=" + activeTimeRange + ", blacklist=" + blacklist + "]");
    }

    @Test
    public void testEquals() {
        Integer freqLimitPerTarget = 10;
        final Map<String, String> timeWindow = new HashMap<>();
        final Map<String, String> activeTimeRange = new HashMap<>();
        List<String> blacklist = new ArrayList<>();
        blacklist.add(BLACKLIST_ITEM);

        Constraint constraint1 = new Constraint();
        Constraint constraint2 = new Constraint();
        assertTrue(constraint1.equals(constraint2));

        constraint1.setFreq_limit_per_target(freqLimitPerTarget);
        assertFalse(constraint1.equals(constraint2));
        constraint2.setFreq_limit_per_target(freqLimitPerTarget);
        assertTrue(constraint1.equals(constraint2));
        assertEquals(constraint1.hashCode(), constraint2.hashCode());

        constraint1.setTime_window(timeWindow);
        assertFalse(constraint1.equals(constraint2));
        constraint2.setTime_window(timeWindow);
        assertTrue(constraint1.equals(constraint2));
        assertEquals(constraint1.hashCode(), constraint2.hashCode());

        constraint1.setActive_time_range(activeTimeRange);
        assertFalse(constraint1.equals(constraint2));
        constraint2.setActive_time_range(activeTimeRange);
        assertTrue(constraint1.equals(constraint2));
        assertEquals(constraint1.hashCode(), constraint2.hashCode());

        constraint1.setBlacklist(blacklist);
        assertFalse(constraint1.equals(constraint2));
        constraint2.setBlacklist(blacklist);
        assertTrue(constraint1.equals(constraint2));
        assertEquals(constraint1.hashCode(), constraint2.hashCode());
    }

    @Test
    public void testEqualsSameObject() {
        Constraint constraint = new Constraint();
        assertTrue(constraint.equals(constraint));
    }

    @Test
    public void testEqualsNull() {
        Constraint constraint = new Constraint();
        assertFalse(constraint.equals(null));
    }

    @Test
    public void testEqualsInstanceOfDiffClass() {
        Constraint constraint = new Constraint();
        assertFalse(constraint.equals(""));
    }

}
