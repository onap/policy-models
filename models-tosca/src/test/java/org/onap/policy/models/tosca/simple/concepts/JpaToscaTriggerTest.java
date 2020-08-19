/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.Date;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;

/**
 * DAO test for ToscaTrigger.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaTriggerTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";
    private static final String EVENT_TYPE = "EventType";
    private static final String ACTION = "Action";
    private static final String A_METHOD = "A Method";
    private static final String A_DESCRIPTION = "A Description";
    private static final String VERSION_001 = "0.0.1";

    @Test
    public void testTriggerPojo() {
        assertNotNull(new JpaToscaTrigger());
        assertNotNull(new JpaToscaTrigger(new PfReferenceKey()));
        assertNotNull(new JpaToscaTrigger(new PfReferenceKey(), EVENT_TYPE, ACTION));
        assertNotNull(new JpaToscaTrigger(new JpaToscaTrigger()));

        assertThatThrownBy(() -> new JpaToscaTrigger((PfReferenceKey) null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTrigger(null, null, null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTrigger(null, EVENT_TYPE, null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTrigger(null, EVENT_TYPE, ACTION)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTrigger(null, null, ACTION)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTrigger(new PfReferenceKey(), null, null))
                .hasMessageMatching("eventType is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaTrigger(new PfReferenceKey(), EVENT_TYPE, null))
                .hasMessageMatching("action is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaTrigger(new PfReferenceKey(), null, ACTION))
                .hasMessageMatching("eventType is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaTrigger((JpaToscaTrigger) null)).isInstanceOf(NullPointerException.class);

        PfConceptKey tparentKey = new PfConceptKey("tParentKey", VERSION_001);
        PfReferenceKey tkey = new PfReferenceKey(tparentKey, "trigger0");
        JpaToscaTrigger tdt = new JpaToscaTrigger(tkey, EVENT_TYPE, ACTION);

        JpaToscaTimeInterval schedule =
                new JpaToscaTimeInterval(new PfReferenceKey(tkey, "sched"), new Date(), new Date());
        tdt.setSchedule(schedule);

        JpaToscaEventFilter targetFilter =
                new JpaToscaEventFilter(new PfReferenceKey(tkey, "filter"), new PfConceptKey("NodeName", VERSION_001));
        tdt.setTargetFilter(targetFilter);

        JpaToscaConstraintLogical lsc = new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, "hello");
        tdt.setCondition(lsc);
        assertEquals(lsc, tdt.getCondition());
        tdt.setConstraint(lsc);
        assertEquals(lsc, tdt.getConstraint());

        tdt.setPeriod(Duration.ZERO);
        assertEquals(Duration.ZERO, tdt.getPeriod());

        tdt.setDescription(A_DESCRIPTION);
        assertEquals(A_DESCRIPTION, tdt.getDescription());

        tdt.setMethod(A_METHOD);
        assertEquals(A_METHOD, tdt.getMethod());

        JpaToscaTrigger tdtClone0 = new JpaToscaTrigger(tdt);
        assertEquals(tdt, tdtClone0);
        assertEquals(0, tdt.compareTo(tdtClone0));

        JpaToscaTrigger tdtClone1 = new JpaToscaTrigger(tdt);
        assertEquals(tdt, tdtClone1);
        assertEquals(0, tdt.compareTo(tdtClone1));

        assertEquals(-1, tdt.compareTo(null));
        assertEquals(0, tdt.compareTo(tdt));
        assertNotEquals(0, tdt.compareTo(tdt.getKey()));

        PfReferenceKey otherDtKey = new PfReferenceKey("otherDt", VERSION_001, "OtherTrigger");
        JpaToscaTrigger otherDt = new JpaToscaTrigger(otherDtKey);

        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setKey(tkey);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setDescription(A_DESCRIPTION);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setEventType(EVENT_TYPE);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setSchedule(schedule);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setTargetFilter(targetFilter);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setCondition(lsc);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setConstraint(lsc);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setPeriod(Duration.ZERO);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setMethod(A_METHOD);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setAction(ACTION);
        assertEquals(0, tdt.compareTo(otherDt));

        otherDt.setEvaluations(100);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setEvaluations(0);
        assertEquals(0, tdt.compareTo(otherDt));

        assertEquals(4, tdt.getKeys().size());
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
        tdt.setDescription(A_DESCRIPTION);
        assertTrue(tdt.validate(new PfValidationResult()).isValid());

        tdt.setEvaluations(-1);
        assertFalse(tdt.validate(new PfValidationResult()).isValid());
        tdt.setEvaluations(100);
        assertTrue(tdt.validate(new PfValidationResult()).isValid());

        tdt.setMethod(null);
        assertTrue(tdt.validate(new PfValidationResult()).isValid());
        tdt.setMethod("");
        assertFalse(tdt.validate(new PfValidationResult()).isValid());
        tdt.setMethod(A_METHOD);
        assertTrue(tdt.validate(new PfValidationResult()).isValid());

        assertThatThrownBy(() -> tdt.validate(null)).hasMessageMatching("resultIn is marked .*on.*ull but is null");
    }
}
