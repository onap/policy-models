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

import java.util.Date;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;

/**
 * DAO test for ToscaTimeInterval.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaTimeIntervalTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";

    @Test
    public void testTimeIntervalPojo() {
        assertNotNull(new JpaToscaTimeInterval());
        assertNotNull(new JpaToscaTimeInterval(new PfReferenceKey()));
        assertNotNull(new JpaToscaTimeInterval(new PfReferenceKey(), new Date(), new Date()));
        assertNotNull(new JpaToscaTimeInterval(new JpaToscaTimeInterval()));

        assertThatThrownBy(() -> new JpaToscaTimeInterval((PfReferenceKey) null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTimeInterval(null, null, null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTimeInterval(null, null, new Date())).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTimeInterval(null, new Date(), null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTimeInterval(null, new Date(), new Date()))
                .hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaTimeInterval(new PfReferenceKey(), null, null))
                .hasMessageMatching("startTime is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaTimeInterval(new PfReferenceKey(), null, new Date()))
                .hasMessageMatching("startTime is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaTimeInterval(new PfReferenceKey(), new Date(), null))
                .hasMessageMatching("endTime is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaServiceTemplate((JpaToscaServiceTemplate) null))
                .isInstanceOf(NullPointerException.class);

        PfConceptKey ttiParentKey = new PfConceptKey("tParentKey", "0.0.1");
        PfReferenceKey ttiKey = new PfReferenceKey(ttiParentKey, "trigger0");
        Date startTime = new Date(1000);
        Date endTime = new Date(2000);
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
        otherDt.setStartTime(startTime);
        assertNotEquals(0, tti.compareTo(otherDt));
        otherDt.setEndTime(endTime);
        assertEquals(0, tti.compareTo(otherDt));

        assertEquals(1, tti.getKeys().size());
        assertEquals(1, new JpaToscaTimeInterval().getKeys().size());

        new JpaToscaTimeInterval().clean();
        tti.clean();
        assertEquals(tdtClone0, tti);

        assertFalse(new JpaToscaTimeInterval().validate(new PfValidationResult()).isValid());
        assertTrue(tti.validate(new PfValidationResult()).isValid());

        tti.setStartTime(null);
        assertFalse(tti.validate(new PfValidationResult()).isValid());
        tti.setStartTime(new Date(endTime.getTime() + 1));
        assertFalse(tti.validate(new PfValidationResult()).isValid());
        tti.setStartTime(startTime);
        assertTrue(tti.validate(new PfValidationResult()).isValid());

        tti.setEndTime(null);
        assertFalse(tti.validate(new PfValidationResult()).isValid());
        tti.setEndTime(new Date(startTime.getTime() - 1));
        assertFalse(tti.validate(new PfValidationResult()).isValid());
        tti.setEndTime(endTime);
        assertTrue(tti.validate(new PfValidationResult()).isValid());

        assertThatThrownBy(() -> tti.validate(null)).hasMessageMatching("resultIn is marked .*on.*ull but is null");
    }
}
