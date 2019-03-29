/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.tosca.simple.concepts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Duration;
import java.util.Date;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintLogical.Operation;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintLogicalString;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaEventFilter;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTimeInterval;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTrigger;

/**
 * DAO test for ToscaTrigger.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaTriggerTest {

    @Test
    public void testTriggerPojo() {
        assertNotNull(new JpaToscaTrigger());
        assertNotNull(new JpaToscaTrigger(new PfReferenceKey()));
        assertNotNull(new JpaToscaTrigger(new PfReferenceKey(), "EventType", "Action"));
        assertNotNull(new JpaToscaTrigger(new JpaToscaTrigger()));

        try {
            new JpaToscaTrigger((PfReferenceKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaTrigger(null, null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaTrigger(null, "EventType", null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaTrigger(null, "EventType", "Action");
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaTrigger(null, null, "Action");
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaTrigger(new PfReferenceKey(), null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("eventType is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaTrigger(new PfReferenceKey(), "EventType", null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("action is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaTrigger(new PfReferenceKey(), null, "Action");
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("eventType is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaTrigger((JpaToscaTrigger) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey tparentKey = new PfConceptKey("tParentKey", "0.0.1");
        PfReferenceKey tkey = new PfReferenceKey(tparentKey, "trigger0");
        JpaToscaTrigger tdt = new JpaToscaTrigger(tkey, "EventType", "Action");

        JpaToscaTimeInterval schedule =
                new JpaToscaTimeInterval(new PfReferenceKey(tkey, "sched"), new Date(), new Date());
        tdt.setSchedule(schedule);

        JpaToscaEventFilter targetFilter =
                new JpaToscaEventFilter(new PfReferenceKey(tkey, "filter"), new PfConceptKey("NodeName", "0.0.1"));
        tdt.setTargetFilter(targetFilter);

        JpaToscaConstraintLogicalString lsc =
                new JpaToscaConstraintLogicalString(new PfReferenceKey(tkey, "sc"), Operation.EQ, "hello");
        tdt.setCondition(lsc);
        assertEquals(lsc, tdt.getCondition());
        tdt.setConstraint(lsc);
        assertEquals(lsc, tdt.getConstraint());

        tdt.setPeriod(Duration.ZERO);
        assertEquals(Duration.ZERO, tdt.getPeriod());

        tdt.setDescription("A Description");
        assertEquals("A Description", tdt.getDescription());

        tdt.setMethod("A Method");
        assertEquals("A Method", tdt.getMethod());

        JpaToscaTrigger tdtClone0 = new JpaToscaTrigger(tdt);
        assertEquals(tdt, tdtClone0);
        assertEquals(0, tdt.compareTo(tdtClone0));

        JpaToscaTrigger tdtClone1 = new JpaToscaTrigger();
        tdt.copyTo(tdtClone1);
        assertEquals(tdt, tdtClone1);
        assertEquals(0, tdt.compareTo(tdtClone1));

        assertEquals(-1, tdt.compareTo(null));
        assertEquals(0, tdt.compareTo(tdt));
        assertFalse(tdt.compareTo(tdt.getKey()) == 0);

        PfReferenceKey otherDtKey = new PfReferenceKey("otherDt", "0.0.1", "OtherTrigger");
        JpaToscaTrigger otherDt = new JpaToscaTrigger(otherDtKey);

        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setKey(tkey);
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setDescription("A Description");
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setEventType("EventType");
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setSchedule(schedule);
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setTargetFilter(targetFilter);
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setCondition(lsc);
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setConstraint(lsc);
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setPeriod(Duration.ZERO);
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setMethod("A Method");
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setAction("Action");
        assertEquals(0, tdt.compareTo(otherDt));

        otherDt.setEvaluations(100);
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setEvaluations(0);
        assertEquals(0, tdt.compareTo(otherDt));

        try {
            tdt.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(6, tdt.getKeys().size());
        assertEquals(1, new JpaToscaTrigger().getKeys().size());

        new JpaToscaTrigger().clean();
        tdt.clean();
        assertEquals(tdtClone0, tdt);

        assertFalse(new JpaToscaTrigger().validate(new PfValidationResult()).isValid());
        assertTrue(tdt.validate(new PfValidationResult()).isValid());

        tdt.setDescription(null);
        assertTrue(tdt.validate(new PfValidationResult()).isValid());
        tdt.setDescription("");
        assertFalse(tdt.validate(new PfValidationResult()).isValid());
        tdt.setDescription("A Description");
        assertTrue(tdt.validate(new PfValidationResult()).isValid());

        tdt.setEvaluations(-1);
        assertFalse(tdt.validate(new PfValidationResult()).isValid());
        tdt.setEvaluations(100);
        assertTrue(tdt.validate(new PfValidationResult()).isValid());

        tdt.setMethod(null);
        assertTrue(tdt.validate(new PfValidationResult()).isValid());
        tdt.setMethod("");
        assertFalse(tdt.validate(new PfValidationResult()).isValid());
        tdt.setMethod("A Method");
        assertTrue(tdt.validate(new PfValidationResult()).isValid());

        try {
            tdt.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }
    }
}
