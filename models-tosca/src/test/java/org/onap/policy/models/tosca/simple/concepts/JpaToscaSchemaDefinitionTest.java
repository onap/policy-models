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

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;

/**
 * DAO test for ToscaEntrySchema.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaSchemaDefinitionTest {

    private static final String A_DESCRIPTION = "A Description";

    @Test
    public void testEntrySchemaPojo() {
        assertNotNull(new JpaToscaSchemaDefinition(new PfConceptKey()));
        assertNotNull(new JpaToscaSchemaDefinition(new JpaToscaSchemaDefinition(new PfConceptKey())));

        assertThatThrownBy(() -> new JpaToscaSchemaDefinition((PfConceptKey) null))
                .hasMessageMatching("type is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaSchemaDefinition((JpaToscaSchemaDefinition) null))
                .hasMessageMatching("copyConcept is marked .*on.*ull but is null");

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

        assertTrue(new JpaToscaSchemaDefinition(typeKey).validate("test").isClean());
        assertTrue(tes.validate("test").isClean());

        tes.setType(PfConceptKey.getNullKey());
        assertFalse(tes.validate("test").isValid());
        tes.setType(null);
        assertFalse(tes.validate("test").isValid());
        tes.setType(typeKey);
        assertTrue(tes.validate("test").isClean());

        tes.setDescription("");

        assertFalse(tes.validate("test").isValid());
        tes.setDescription(A_DESCRIPTION);
        assertTrue(tes.validate("test").isClean());

        tes.getConstraints().add(null);
        assertFalse(tes.validate("test").isValid());
        tes.getConstraints().remove(null);
        assertTrue(tes.validate("test").isClean());

        assertThatThrownBy(() -> tes.validate(null)).hasMessageMatching("fieldName is marked .*on.*ull but is null");
    }
}
