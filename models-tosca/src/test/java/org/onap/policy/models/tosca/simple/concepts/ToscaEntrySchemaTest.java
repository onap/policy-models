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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.simple.concepts.ToscaConstraint;
import org.onap.policy.models.tosca.simple.concepts.ToscaConstraintLogical.Operation;
import org.onap.policy.models.tosca.simple.concepts.ToscaConstraintLogicalString;
import org.onap.policy.models.tosca.simple.concepts.ToscaEntrySchema;

/**
 * DAO test for ToscaEntrySchema.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaEntrySchemaTest {

    @Test
    public void testEntrySchemaPojo() {
        assertNotNull(new ToscaEntrySchema());
        assertNotNull(new ToscaEntrySchema(new PfReferenceKey()));
        assertNotNull(new ToscaEntrySchema(new PfReferenceKey(), new PfConceptKey()));
        assertNotNull(new ToscaEntrySchema(new ToscaEntrySchema()));

        try {
            new ToscaEntrySchema((PfReferenceKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaEntrySchema((ToscaEntrySchema) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfReferenceKey esKey = new PfReferenceKey("entrySchemaParent", "0.0.1", "entrySchema");
        PfConceptKey typeKey = new PfConceptKey("type", "0.0.1");
        ToscaEntrySchema tes = new ToscaEntrySchema(esKey, typeKey);

        tes.setDescription("A Description");
        assertEquals("A Description", tes.getDescription());

        List<ToscaConstraint> constraints = new ArrayList<>();
        ToscaConstraintLogicalString lsc =
                new ToscaConstraintLogicalString(new PfReferenceKey(esKey, "sc"), Operation.EQ, "hello");
        constraints.add(lsc);
        tes.setConstraints(constraints);
        assertEquals(constraints, tes.getConstraints());

        ToscaEntrySchema tdtClone0 = new ToscaEntrySchema(tes);
        assertEquals(tes, tdtClone0);
        assertEquals(0, tes.compareTo(tdtClone0));

        ToscaEntrySchema tdtClone1 = new ToscaEntrySchema();
        tes.copyTo(tdtClone1);
        assertEquals(tes, tdtClone1);
        assertEquals(0, tes.compareTo(tdtClone1));

        assertEquals(-1, tes.compareTo(null));
        assertEquals(0, tes.compareTo(tes));
        assertFalse(tes.compareTo(tes.getKey()) == 0);

        PfReferenceKey otherEsKey = new PfReferenceKey("entrySchemaParent", "0.0.1", "otherEntrySchema");
        ToscaEntrySchema otherEs = new ToscaEntrySchema(otherEsKey);

        assertFalse(tes.compareTo(otherEs) == 0);
        otherEs.setKey(esKey);
        assertFalse(tes.compareTo(otherEs) == 0);
        otherEs.setType(typeKey);
        assertFalse(tes.compareTo(otherEs) == 0);
        otherEs.setDescription("A Description");
        assertFalse(tes.compareTo(otherEs) == 0);
        otherEs.setConstraints(constraints);
        assertEquals(0, tes.compareTo(otherEs));

        try {
            tes.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(3, tes.getKeys().size());
        assertEquals(2, new ToscaEntrySchema().getKeys().size());

        new ToscaEntrySchema().clean();
        tes.clean();
        assertEquals(tdtClone0, tes);

        assertFalse(new ToscaEntrySchema().validate(new PfValidationResult()).isValid());
        assertTrue(tes.validate(new PfValidationResult()).isValid());

        tes.setType(PfConceptKey.getNullKey());
        assertFalse(tes.validate(new PfValidationResult()).isValid());
        tes.setType(null);
        assertFalse(tes.validate(new PfValidationResult()).isValid());
        tes.setType(typeKey);
        assertTrue(tes.validate(new PfValidationResult()).isValid());

        tes.setDescription("");;
        assertFalse(tes.validate(new PfValidationResult()).isValid());
        tes.setDescription("A Description");
        assertTrue(tes.validate(new PfValidationResult()).isValid());

        tes.getConstraints().add(null);
        assertFalse(tes.validate(new PfValidationResult()).isValid());
        tes.getConstraints().remove(null);
        assertTrue(tes.validate(new PfValidationResult()).isValid());

        try {
            tes.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }
    }
}
