/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;

/**
 * Test the {@link JpaToscaConstraintLogical} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class JpaToscaConstraintLogicalTest {

    private static final String HELLO = "Hello";

    @Test
    void testLogicalConstraint() {
        ToscaConstraint c0 = new ToscaConstraint();
        c0.setEqual(HELLO);
        JpaToscaConstraintLogical jc0 = new JpaToscaConstraintLogical(c0);
        assertEquals(c0, jc0.toAuthorative());

        ToscaConstraint c1 = new ToscaConstraint();
        c1.setGreaterOrEqual(HELLO);
        JpaToscaConstraintLogical jc1 = new JpaToscaConstraintLogical(c1);
        assertEquals(c1, jc1.toAuthorative());

        ToscaConstraint c2 = new ToscaConstraint();
        c2.setGreaterThan(HELLO);
        JpaToscaConstraintLogical jc2 = new JpaToscaConstraintLogical(c2);
        assertEquals(c2, jc2.toAuthorative());

        ToscaConstraint c3 = new ToscaConstraint();
        c3.setLessOrEqual(HELLO);
        JpaToscaConstraintLogical jc3 = new JpaToscaConstraintLogical(c3);
        assertEquals(c3, jc3.toAuthorative());

        ToscaConstraint c4 = new ToscaConstraint();
        c4.setLessThan(HELLO);
        JpaToscaConstraintLogical jc4 = new JpaToscaConstraintLogical(c4);
        assertEquals(c4, jc4.toAuthorative());

        ToscaConstraint c5 = new ToscaConstraint();
        JpaToscaConstraintLogical jc5 = new JpaToscaConstraintLogical(c5);
        assertNull(jc5.toAuthorative());

        assertThatThrownBy(() -> jc0.compareTo(null)).isInstanceOf(NullPointerException.class);
        assertEquals(0, jc0.compareTo(jc0));
        assertNotEquals(0, jc0.compareTo(new JpaToscaConstraintValidValues(new ArrayList<>())));
        assertEquals(-2, jc0.compareTo(jc1));
    }
}
