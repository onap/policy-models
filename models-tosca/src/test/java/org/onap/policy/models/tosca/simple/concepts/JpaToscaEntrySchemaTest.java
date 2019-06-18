/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfValidationResult;

/**
 * DAO test for ToscaEntrySchema.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaEntrySchemaTest {

    private static final String A_DESCRIPTION = "A Description";

    @Test
    public void testEntrySchemaPojo() {
        assertNotNull(new JpaToscaEntrySchema(new PfConceptKey()));
        assertNotNull(new JpaToscaEntrySchema(new JpaToscaEntrySchema(new PfConceptKey())));

        assertThatThrownBy(() -> new JpaToscaEntrySchema((PfConceptKey) null))
                        .hasMessage("type is marked @NonNull but is null");

        assertThatThrownBy(() -> new JpaToscaEntrySchema((JpaToscaEntrySchema) null))
                        .hasMessage("copyConcept is marked @NonNull but is null");

        PfConceptKey typeKey = new PfConceptKey("type", "0.0.1");
        JpaToscaEntrySchema tes = new JpaToscaEntrySchema(typeKey);

        tes.setDescription(A_DESCRIPTION);
        assertEquals(A_DESCRIPTION, tes.getDescription());

        List<JpaToscaConstraint> constraints = new ArrayList<>();
        JpaToscaConstraintLogical lsc = new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, "hello");
        constraints.add(lsc);
        tes.setConstraints(constraints);
        assertEquals(constraints, tes.getConstraints());

        JpaToscaEntrySchema tdtClone0 = new JpaToscaEntrySchema(tes);
        assertEquals(tes, tdtClone0);
        assertEquals(0, tes.compareTo(tdtClone0));

        JpaToscaEntrySchema tdtClone1 = new JpaToscaEntrySchema(tes);
        assertEquals(tes, tdtClone1);
        assertEquals(0, tes.compareTo(tdtClone1));

        assertEquals(-1, tes.compareTo(null));
        assertEquals(0, tes.compareTo(tes));

        JpaToscaEntrySchema otherEs = new JpaToscaEntrySchema(typeKey);

        assertFalse(tes.compareTo(otherEs) == 0);
        otherEs.setType(typeKey);
        assertFalse(tes.compareTo(otherEs) == 0);
        otherEs.setDescription(A_DESCRIPTION);
        assertFalse(tes.compareTo(otherEs) == 0);
        otherEs.setConstraints(constraints);
        assertEquals(0, tes.compareTo(otherEs));

        assertThatThrownBy(() -> tes.copyTo(null)).hasMessage("target is marked @NonNull but is null");

        assertEquals(1, tes.getKeys().size());
        assertEquals(1, new JpaToscaEntrySchema(typeKey).getKeys().size());

        new JpaToscaEntrySchema(typeKey).clean();
        tes.clean();
        assertEquals(tdtClone0, tes);

        assertTrue(new JpaToscaEntrySchema(typeKey).validate(new PfValidationResult()).isValid());
        assertTrue(tes.validate(new PfValidationResult()).isValid());

        tes.setType(PfConceptKey.getNullKey());
        assertFalse(tes.validate(new PfValidationResult()).isValid());
        tes.setType(null);
        assertFalse(tes.validate(new PfValidationResult()).isValid());
        tes.setType(typeKey);
        assertTrue(tes.validate(new PfValidationResult()).isValid());

        tes.setDescription("");;
        assertFalse(tes.validate(new PfValidationResult()).isValid());
        tes.setDescription(A_DESCRIPTION);
        assertTrue(tes.validate(new PfValidationResult()).isValid());

        tes.getConstraints().add(null);
        assertFalse(tes.validate(new PfValidationResult()).isValid());
        tes.getConstraints().remove(null);
        assertTrue(tes.validate(new PfValidationResult()).isValid());

        assertThatThrownBy(() -> tes.validate(null)).hasMessage("resultIn is marked @NonNull but is null");
    }
}
