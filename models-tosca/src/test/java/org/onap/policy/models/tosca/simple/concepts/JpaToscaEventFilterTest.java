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

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;

/**
 * DAO test for ToscaEventFilter.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaEventFilterTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";
    private static final String A_REQUREMENT = "A Requrement";
    private static final String A_CAPABILITY = "A Capability";
    private static final String VERSION_001 = "0.0.1";

    @Test
    public void testEventFilterPojo() {
        assertNotNull(new JpaToscaEventFilter());
        assertNotNull(new JpaToscaEventFilter(new PfReferenceKey()));
        assertNotNull(new JpaToscaEventFilter(new PfReferenceKey(), new PfConceptKey()));
        assertNotNull(new JpaToscaEventFilter(new JpaToscaEventFilter()));

        assertThatThrownBy(() -> new JpaToscaEventFilter((PfReferenceKey) null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaEventFilter(null, null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaEventFilter(null, new PfConceptKey())).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaEventFilter(new PfReferenceKey(), null))
                .hasMessageMatching("node is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaEventFilter((JpaToscaEventFilter) null))
                .isInstanceOf(NullPointerException.class);

        PfConceptKey efParentKey = new PfConceptKey("tParentKey", VERSION_001);
        PfReferenceKey efKey = new PfReferenceKey(efParentKey, "trigger0");
        PfConceptKey nodeKey = new PfConceptKey("tParentKey", VERSION_001);
        JpaToscaEventFilter tef = new JpaToscaEventFilter(efKey, nodeKey);

        tef.setRequirement(A_REQUREMENT);
        assertEquals(A_REQUREMENT, tef.getRequirement());

        tef.setCapability(A_CAPABILITY);
        assertEquals(A_CAPABILITY, tef.getCapability());

        JpaToscaEventFilter tdtClone0 = new JpaToscaEventFilter(tef);
        assertEquals(tef, tdtClone0);
        assertEquals(0, tef.compareTo(tdtClone0));

        JpaToscaEventFilter tdtClone1 = new JpaToscaEventFilter(tef);
        assertEquals(tef, tdtClone1);
        assertEquals(0, tef.compareTo(tdtClone1));

        assertEquals(-1, tef.compareTo(null));
        assertEquals(0, tef.compareTo(tef));
        assertNotEquals(0, tef.compareTo(tef.getKey()));

        PfReferenceKey otherDtKey = new PfReferenceKey("otherDt", VERSION_001, "OtherEventFilter");
        JpaToscaEventFilter otherDt = new JpaToscaEventFilter(otherDtKey);

        assertNotEquals(0, tef.compareTo(otherDt));
        otherDt.setKey(efKey);
        assertNotEquals(0, tef.compareTo(otherDt));
        otherDt.setNode(nodeKey);
        assertNotEquals(0, tef.compareTo(otherDt));
        otherDt.setRequirement(A_REQUREMENT);
        assertNotEquals(0, tef.compareTo(otherDt));
        otherDt.setCapability(A_CAPABILITY);
        assertEquals(0, tef.compareTo(otherDt));

        assertEquals(2, tef.getKeys().size());
        assertEquals(2, new JpaToscaEventFilter().getKeys().size());

        new JpaToscaEventFilter().clean();
        tef.clean();
        assertEquals(tdtClone0, tef);

        assertFalse(new JpaToscaEventFilter().validate(new PfValidationResult()).isValid());
        assertTrue(tef.validate(new PfValidationResult()).isValid());

        tef.setRequirement(null);
        assertTrue(tef.validate(new PfValidationResult()).isValid());
        tef.setRequirement("");
        assertFalse(tef.validate(new PfValidationResult()).isValid());
        tef.setRequirement(A_REQUREMENT);
        assertTrue(tef.validate(new PfValidationResult()).isValid());

        tef.setCapability(null);
        assertTrue(tef.validate(new PfValidationResult()).isValid());
        tef.setCapability("");
        assertFalse(tef.validate(new PfValidationResult()).isValid());
        tef.setCapability(A_CAPABILITY);
        assertTrue(tef.validate(new PfValidationResult()).isValid());

        tef.setNode(null);
        assertFalse(tef.validate(new PfValidationResult()).isValid());
        tef.setNode(PfConceptKey.getNullKey());
        assertFalse(tef.validate(new PfValidationResult()).isValid());
        tef.setNode(nodeKey);
        assertTrue(tef.validate(new PfValidationResult()).isValid());

        assertThatThrownBy(() -> tef.validate(null)).hasMessageMatching("resultIn is marked .*on.*ull but is null");
    }
}
