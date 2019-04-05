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
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraint;

/**
 * DAO test for ToscaProperty.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaPropertyTest {

    @Test
    public void testPropertyPojo() {
        assertNotNull(new JpaToscaProperty());
        assertNotNull(new JpaToscaProperty(new PfReferenceKey()));
        assertNotNull(new JpaToscaProperty(new PfReferenceKey(), new PfConceptKey()));
        assertNotNull(new JpaToscaProperty(new JpaToscaProperty()));

        try {
            new JpaToscaProperty((PfReferenceKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaProperty(null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaProperty(null, new PfConceptKey());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaProperty(new PfReferenceKey(), null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("type is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaProperty((JpaToscaProperty) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey pparentKey = new PfConceptKey("tParentKey", "0.0.1");
        PfReferenceKey pkey = new PfReferenceKey(pparentKey, "trigger0");
        PfConceptKey ptypeKey = new PfConceptKey("TTypeKey", "0.0.1");
        JpaToscaProperty tp = new JpaToscaProperty(pkey, ptypeKey);

        tp.setDescription("A Description");
        assertEquals("A Description", tp.getDescription());

        tp.setRequired(false);
        assertFalse(tp.isRequired());

        tp.setDefaultValue("defaultKey");

        tp.setStatus(ToscaProperty.Status.SUPPORTED);

        List<JpaToscaConstraint> constraints = new ArrayList<>();
        JpaToscaConstraintLogical lsc = new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, "hello");
        constraints.add(lsc);
        tp.setConstraints(constraints);
        assertEquals(constraints, tp.getConstraints());

        PfConceptKey typeKey = new PfConceptKey("type", "0.0.1");
        JpaToscaEntrySchema tes = new JpaToscaEntrySchema(typeKey);
        tp.setEntrySchema(tes);

        JpaToscaProperty tdtClone0 = new JpaToscaProperty(tp);
        assertEquals(tp, tdtClone0);
        assertEquals(0, tp.compareTo(tdtClone0));

        JpaToscaProperty tdtClone1 = new JpaToscaProperty();
        tp.copyTo(tdtClone1);
        assertEquals(tp, tdtClone1);
        assertEquals(0, tp.compareTo(tdtClone1));

        assertEquals(-1, tp.compareTo(null));
        assertEquals(0, tp.compareTo(tp));
        assertFalse(tp.compareTo(tp.getKey()) == 0);

        PfReferenceKey otherDtKey = new PfReferenceKey("otherDt", "0.0.1", "OtherProperty");
        JpaToscaProperty otherDt = new JpaToscaProperty(otherDtKey);

        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setKey(pkey);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setType(ptypeKey);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setDescription("A Description");
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setRequired(false);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setDefaultValue("defaultKey");
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

        assertEquals(3, tp.getKeys().size());
        assertEquals(2, new JpaToscaProperty().getKeys().size());

        new JpaToscaProperty().clean();
        tp.clean();
        assertEquals(tdtClone0, tp);

        assertFalse(new JpaToscaProperty().validate(new PfValidationResult()).isValid());
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
        tp.setDefaultValue("defaultKey");
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.getConstraints().add(null);
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.getConstraints().remove(null);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        try {
            tp.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }
    }
}
