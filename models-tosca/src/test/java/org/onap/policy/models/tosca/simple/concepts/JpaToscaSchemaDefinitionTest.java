/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
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

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.PfConceptKey;

/**
 * DAO test for ToscaEntrySchema.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class JpaToscaSchemaDefinitionTest {

    private static final String A_DESCRIPTION = "A Description";

    @Test
    void testEntrySchemaNull() {
        assertNotNull(new JpaToscaSchemaDefinition(new PfConceptKey()));
        assertNotNull(new JpaToscaSchemaDefinition(new JpaToscaSchemaDefinition(new PfConceptKey())));

        assertThatThrownBy(() -> new JpaToscaSchemaDefinition((PfConceptKey) null))
                .hasMessageMatching("type is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaSchemaDefinition((JpaToscaSchemaDefinition) null))
                .hasMessageMatching("copyConcept is marked .*on.*ull but is null");
    }

    @Test
    void testEntrySchema() {
        PfConceptKey typeKey = new PfConceptKey("type", "0.0.1");
        JpaToscaSchemaDefinition tes = new JpaToscaSchemaDefinition(typeKey);

        tes.setDescription(A_DESCRIPTION);
        assertEquals(A_DESCRIPTION, tes.getDescription());

        List<JpaToscaConstraint> constraints = new ArrayList<>();
        JpaToscaConstraintLogical lsc = new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, "hello");
        constraints.add(lsc);
        tes.setConstraints(constraints);
        assertEquals(constraints, tes.getConstraints());

        JpaToscaSchemaDefinition tdtClone0 = new JpaToscaSchemaDefinition(tes);
        assertEquals(tes, tdtClone0);
        assertEquals(0, tes.compareTo(tdtClone0));

        JpaToscaSchemaDefinition tdtClone1 = new JpaToscaSchemaDefinition(tes);
        assertEquals(tes, tdtClone1);
        assertEquals(0, tes.compareTo(tdtClone1));

        assertEquals(-1, tes.compareTo(null));
        assertEquals(0, tes.compareTo(tes));

        JpaToscaSchemaDefinition otherEs = new JpaToscaSchemaDefinition(typeKey);

        assertNotEquals(0, tes.compareTo(otherEs));
        otherEs.setType(typeKey);
        assertNotEquals(0, tes.compareTo(otherEs));
        otherEs.setDescription(A_DESCRIPTION);
        assertNotEquals(0, tes.compareTo(otherEs));
        otherEs.setConstraints(constraints);
        assertEquals(0, tes.compareTo(otherEs));

        assertThatThrownBy(() -> tes.copyTo(null)).hasMessageMatching("target is marked .*on.*ull but is null");

        assertEquals(1, tes.getKeys().size());
        assertEquals(1, new JpaToscaSchemaDefinition(typeKey).getKeys().size());

        new JpaToscaSchemaDefinition(typeKey).clean();
        tes.clean();
        assertEquals(tdtClone0, tes);
    }

    @Test
    void testEntrySchemaValidation() {
        PfConceptKey typeKey = new PfConceptKey("type", "0.0.1");
        JpaToscaSchemaDefinition tes = setUpJpaToscaSchemaDefinition(typeKey);

        assertTrue(new JpaToscaSchemaDefinition(typeKey).validate("").isValid());
        assertTrue(tes.validate("").isValid());

        tes.setType(PfConceptKey.getNullKey());
        assertFalse(tes.validate("").isValid());
        tes.setType(null);
        assertFalse(tes.validate("").isValid());
        tes.setType(typeKey);
        assertTrue(tes.validate("").isValid());

        tes.setDescription("");

        assertFalse(tes.validate("").isValid());
        tes.setDescription(A_DESCRIPTION);
        assertTrue(tes.validate("").isValid());

        tes.getConstraints().add(null);
        assertFalse(tes.validate("").isValid());
        tes.getConstraints().remove(null);
        assertTrue(tes.validate("").isValid());

        assertThatThrownBy(() -> tes.validate(null)).hasMessageMatching("fieldName is marked .*on.*ull but is null");
    }

    private JpaToscaSchemaDefinition setUpJpaToscaSchemaDefinition(PfConceptKey typeKey) {
        JpaToscaSchemaDefinition tes = new JpaToscaSchemaDefinition(typeKey);
        tes.setDescription(A_DESCRIPTION);

        List<JpaToscaConstraint> constraints = new ArrayList<>();
        JpaToscaConstraintLogical lsc = new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, "hello");
        constraints.add(lsc);
        tes.setConstraints(constraints);

        return tes;
    }
}
