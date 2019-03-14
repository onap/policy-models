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

package org.onap.policy.models.tosca.concepts;

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
import org.onap.policy.models.tosca.concepts.ToscaConstraintLogical.Operation;

/**
 * DAO test for ToscaProperty.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaPropertyTest {

    @Test
    public void testPropertyPojo() {
        assertNotNull(new ToscaProperty());
        assertNotNull(new ToscaProperty(new PfReferenceKey()));
        assertNotNull(new ToscaProperty(new PfReferenceKey(), new PfConceptKey()));
        assertNotNull(new ToscaProperty(new ToscaProperty()));

        try {
            new ToscaProperty((PfReferenceKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaProperty(null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaProperty(null, new PfConceptKey());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaProperty(new PfReferenceKey(), null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("type is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaProperty((ToscaProperty) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey pparentKey = new PfConceptKey("tParentKey", "0.0.1");
        PfReferenceKey pkey = new PfReferenceKey(pparentKey, "trigger0");
        PfConceptKey ptypeKey = new PfConceptKey("TTypeKey", "0.0.1");
        ToscaProperty tp = new ToscaProperty(pkey, ptypeKey);

        tp.setDescription("A Description");
        assertEquals("A Description", tp.getDescription());

        tp.setRequired(false);
        assertFalse(tp.isRequired());

        PfConceptKey tdefaultKey = new PfConceptKey("defaultKey", "0.0.1");
        tp.setDefaultValue(tdefaultKey);

        tp.setStatus(ToscaProperty.Status.SUPPORTED);

        List<ToscaConstraint> constraints = new ArrayList<>();
        ToscaConstraintLogicalString lsc =
                new ToscaConstraintLogicalString(new PfReferenceKey(pkey, "sc"), Operation.EQ, "hello");
        constraints.add(lsc);
        tp.setConstraints(constraints);
        assertEquals(constraints, tp.getConstraints());

        PfReferenceKey esKey = new PfReferenceKey(pkey, "entrySchema");
        PfConceptKey typeKey = new PfConceptKey("type", "0.0.1");
        ToscaEntrySchema tes = new ToscaEntrySchema(esKey, typeKey);
        tp.setEntrySchema(tes);

        ToscaProperty tdtClone0 = new ToscaProperty(tp);
        assertEquals(tp, tdtClone0);
        assertEquals(0, tp.compareTo(tdtClone0));

        ToscaProperty tdtClone1 = new ToscaProperty();
        tp.copyTo(tdtClone1);
        assertEquals(tp, tdtClone1);
        assertEquals(0, tp.compareTo(tdtClone1));

        assertEquals(-1, tp.compareTo(null));
        assertEquals(0, tp.compareTo(tp));
        assertFalse(tp.compareTo(tp.getKey()) == 0);

        PfReferenceKey otherDtKey = new PfReferenceKey("otherDt", "0.0.1", "OtherProperty");
        ToscaProperty otherDt = new ToscaProperty(otherDtKey);

        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setKey(pkey);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setType(ptypeKey);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setDescription("A Description");
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setRequired(false);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setDefaultValue(tdefaultKey);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setStatus(ToscaProperty.Status.SUPPORTED);
        assertFalse(tp.compareTo(otherDt) == 0);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setConstraints(constraints);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setEntrySchema(tes);
        assertEquals(0, tp.compareTo(otherDt));

        otherDt.setRequired(true);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setRequired(false);
        assertEquals(0, tp.compareTo(otherDt));

        otherDt.setStatus(ToscaProperty.Status.UNSUPPORTED);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setStatus(ToscaProperty.Status.SUPPORTED);
        assertEquals(0, tp.compareTo(otherDt));

        try {
            tp.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(6, tp.getKeys().size());
        assertEquals(2, new ToscaProperty().getKeys().size());

        new ToscaProperty().clean();
        tp.clean();
        assertEquals(tdtClone0, tp);

        assertFalse(new ToscaProperty().validate(new PfValidationResult()).isValid());
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.setDescription(null);
        assertTrue(tp.validate(new PfValidationResult()).isValid());
        tp.setDescription("");
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.setDescription("A Description");
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.setType(null);
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.setType(typeKey);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.setType(PfConceptKey.getNullKey());
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.setType(typeKey);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.setDefaultValue(null);
        assertTrue(tp.validate(new PfValidationResult()).isValid());
        tp.setDefaultValue(tdefaultKey);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.setDefaultValue(PfConceptKey.getNullKey());
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.setDefaultValue(tdefaultKey);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.getConstraints().add(null);
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.getConstraints().remove(null);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        try {
            tp.setStatus(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("status is marked @NonNull but is null", exc.getMessage());
        }

        try {
            tp.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }
    }
}
