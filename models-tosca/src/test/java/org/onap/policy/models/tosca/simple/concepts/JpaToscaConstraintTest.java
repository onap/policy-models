/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;

/**
 * DAO test for ToscaConstraintLogicalString.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class JpaToscaConstraintTest {

    private static final String CONSTRAINT = "Constraint";

    @Test
    void testConstraintLogicalStringPojo() {
        assertNotNull(new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, CONSTRAINT));

        assertThatThrownBy(() -> new JpaToscaConstraintLogical((JpaToscaConstraintOperation) null, null))
                .hasMessageMatching("operation is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaConstraintLogical((JpaToscaConstraintOperation) null, "Hello"))
                .hasMessageMatching("operation is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, null))
                .hasMessageMatching("compareTo is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaConstraintInRange((List<String>) null))
                .hasMessageMatching("rangeValues is marked .*on.*ull but is null");

        assertNotNull(new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, CONSTRAINT));

        assertEquals(0, new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, "")
                .compareTo(new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, "")));

        JpaToscaConstraintOperation op = JpaToscaConstraintOperation.EQ;
        assertEquals("equal_to", op.getToscaToken());

        List<String> validValues = new ArrayList<>();
        validValues.add("hello");
        validValues.add("goodbye");
        JpaToscaConstraintValidValues cvv0 = new JpaToscaConstraintValidValues(validValues);
        assertThatThrownBy(() -> cvv0.compareTo(null)).isInstanceOf(NullPointerException.class);
        assertEquals(0, cvv0.compareTo(cvv0));
        assertNotEquals(0, cvv0.compareTo(new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, CONSTRAINT)));
        JpaToscaConstraintValidValues cvv1 = new JpaToscaConstraintValidValues(validValues);
        assertEquals(0, cvv0.compareTo(cvv1));

        cvv1.fromAuthorative(new ToscaConstraint());
        assertNotNull(cvv1.getValidValues());

        List<String> rangeValues = new ArrayList<>();
        rangeValues.add("hello");
        rangeValues.add("goodbye");
        JpaToscaConstraintInRange cir0 = new JpaToscaConstraintInRange(rangeValues);
        assertThatThrownBy(() -> cir0.compareTo(null)).isInstanceOf(NullPointerException.class);
        assertEquals(0, cir0.compareTo(cir0));
        assertNotEquals(0, cir0.compareTo(new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, CONSTRAINT)));
        JpaToscaConstraintInRange cir1 = new JpaToscaConstraintInRange(rangeValues);
        assertEquals(0, cir0.compareTo(cir1));

        ToscaConstraint tc0 = new ToscaConstraint();
        tc0.setRangeValues(rangeValues);
        JpaToscaConstraintInRange cir2 = new JpaToscaConstraintInRange(tc0);
        assertEquals(0, cir0.compareTo(cir2));

        cir1.fromAuthorative(new ToscaConstraint());
        assertNotNull(cir1.getRangeValues());

        ToscaConstraint tc1 = cir2.toAuthorative();
        assertEquals(tc0, tc1);
    }
}
