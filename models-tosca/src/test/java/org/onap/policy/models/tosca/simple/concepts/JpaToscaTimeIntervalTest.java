/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2025 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2024 Nordix Foundation
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;

/**
 * DAO test for ToscaTimeInterval.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class JpaToscaTimeIntervalTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";

    @Test
    void testTimeIntervalPojo() {
        assertNotNull(new JpaToscaTimeInterval());
        assertNotNull(new JpaToscaTimeInterval(new PfReferenceKey()));
        assertNotNull(new JpaToscaTimeInterval(new PfReferenceKey(), Instant.now(), Instant.now()));
        assertNotNull(new JpaToscaTimeInterval(new JpaToscaTimeInterval()));

        assertThatThrownBy(() -> new JpaToscaTimeInterval((PfReferenceKey) null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTimeInterval(null, null, null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTimeInterval(null, null, Instant.now())).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTimeInterval(null, Instant.now(), null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTimeInterval(null, Instant.now(), Instant.now()))
                .hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTimeInterval(new PfReferenceKey(), null, null))
                .hasMessageMatching("startTime is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaTimeInterval(new PfReferenceKey(), null, Instant.now()))
                .hasMessageMatching("startTime is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaTimeInterval(new PfReferenceKey(), Instant.now(), null))
                .hasMessageMatching("endTime is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaServiceTemplate((JpaToscaServiceTemplate) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testTimeInterval() {
        PfConceptKey ttiParentKey = new PfConceptKey("tParentKey", "0.0.1");
        PfReferenceKey ttiKey = new PfReferenceKey(ttiParentKey, "trigger0");
        Instant startTime = Instant.ofEpochSecond(1000);
        Instant endTime = Instant.ofEpochSecond(2000);
        JpaToscaTimeInterval tti = new JpaToscaTimeInterval(ttiKey, startTime, endTime);

        JpaToscaTimeInterval tdtClone0 = new JpaToscaTimeInterval(tti);
        assertEquals(tti, tdtClone0);
        assertEquals(0, tti.compareTo(tdtClone0));

        JpaToscaTimeInterval tdtClone1 = new JpaToscaTimeInterval(tti);
        assertEquals(tti, tdtClone1);
        assertEquals(0, tti.compareTo(tdtClone1));

        assertEquals(-1, tti.compareTo(null));
        assertEquals(0, tti.compareTo(tti));
        assertNotEquals(0, tti.compareTo(tti.getKey()));

        PfReferenceKey otherDtKey = new PfReferenceKey("otherDt", "0.0.1", "OtherTimeInterval");
        JpaToscaTimeInterval otherDt = new JpaToscaTimeInterval(otherDtKey);

        assertNotEquals(0, tti.compareTo(otherDt));
        otherDt.setKey(ttiKey);
        assertNotEquals(0, tti.compareTo(otherDt));
        otherDt.setStartTime(Timestamp.from(startTime));
        assertNotEquals(0, tti.compareTo(otherDt));
        otherDt.setEndTime(Timestamp.from(endTime));
        assertEquals(0, tti.compareTo(otherDt));

        assertEquals(1, tti.getKeys().size());
        assertEquals(1, new JpaToscaTimeInterval().getKeys().size());

        new JpaToscaTimeInterval().clean();
        tti.clean();
        assertEquals(tdtClone0, tti);
    }

    @Test
    void testTimeIntervalValidation() {
        Instant startTime = Instant.ofEpochSecond(1000);
        Instant endTime = Instant.ofEpochSecond(2000);
        JpaToscaTimeInterval tti = setUpJpaToscaTimeInterval(startTime, endTime);
        assertFalse(new JpaToscaTimeInterval().validate("").isValid());
        assertTrue(tti.validate("").isValid());

        tti.setStartTime(null);
        assertFalse(tti.validate("").isValid());

        tti.setStartTime(Timestamp.from(endTime.plusSeconds(1)));
        assertFalse(tti.validate("").isValid());
        tti.setStartTime(Timestamp.from(startTime));
        assertTrue(tti.validate("").isValid());

        tti.setEndTime(null);
        assertFalse(tti.validate("").isValid());
        tti.setEndTime(Timestamp.from(startTime.minusSeconds(1)));
        assertFalse(tti.validate("").isValid());
        tti.setEndTime(Timestamp.from(endTime));
        assertTrue(tti.validate("").isValid());

        assertThatThrownBy(() -> tti.validate(null)).hasMessageMatching("fieldName is marked .*on.*ull but is null");
    }

    private JpaToscaTimeInterval setUpJpaToscaTimeInterval(Instant startTime, Instant endTime) {
        PfConceptKey ttiParentKey = new PfConceptKey("tParentKey", "0.0.1");
        PfReferenceKey ttiKey = new PfReferenceKey(ttiParentKey, "trigger0");

        return new JpaToscaTimeInterval(ttiKey, startTime, endTime);
    }
}
