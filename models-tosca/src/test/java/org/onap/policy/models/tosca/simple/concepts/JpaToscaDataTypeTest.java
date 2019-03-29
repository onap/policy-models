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
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraint;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintLogical.Operation;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraintLogicalString;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaProperty;

/**
 * DAO test for ToscaDatatype.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaDataTypeTest {

    @Test
    public void testDataTypePojo() {
        assertNotNull(new JpaToscaDataType());
        assertNotNull(new JpaToscaDataType(new PfConceptKey()));
        assertNotNull(new JpaToscaDataType(new JpaToscaDataType()));

        try {
            new JpaToscaDataType((PfConceptKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaDataType((JpaToscaDataType) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey dtKey = new PfConceptKey("tdt", "0.0.1");
        JpaToscaDataType tdt = new JpaToscaDataType(dtKey);

        List<JpaToscaConstraint> constraints = new ArrayList<>();
        JpaToscaConstraintLogicalString lsc =
                new JpaToscaConstraintLogicalString(new PfReferenceKey(dtKey, "sc"), Operation.EQ, "hello");
        constraints.add(lsc);
        tdt.setConstraints(constraints);
        assertEquals(constraints, tdt.getConstraints());

        List<JpaToscaProperty> properties = new ArrayList<>();
        JpaToscaProperty tp = new JpaToscaProperty(new PfReferenceKey(dtKey, "pr"), new PfConceptKey("type", "0.0.1"));
        properties.add(tp);
        tdt.setProperties(properties);
        assertEquals(properties, tdt.getProperties());

        JpaToscaDataType tdtClone0 = new JpaToscaDataType(tdt);
        assertEquals(tdt, tdtClone0);
        assertEquals(0, tdt.compareTo(tdtClone0));

        JpaToscaDataType tdtClone1 = new JpaToscaDataType();
        tdt.copyTo(tdtClone1);
        assertEquals(tdt, tdtClone1);
        assertEquals(0, tdt.compareTo(tdtClone1));

        assertEquals(-1, tdt.compareTo(null));
        assertEquals(0, tdt.compareTo(tdt));
        assertFalse(tdt.compareTo(tdt.getKey()) == 0);

        PfConceptKey otherDtKey = new PfConceptKey("otherDt", "0.0.1");
        JpaToscaDataType otherDt = new JpaToscaDataType(otherDtKey);

        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setKey(dtKey);
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setConstraints(constraints);
        assertFalse(tdt.compareTo(otherDt) == 0);
        otherDt.setProperties(properties);
        assertEquals(0, tdt.compareTo(otherDt));

        try {
            tdt.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(4, tdt.getKeys().size());
        assertEquals(1, new JpaToscaDataType().getKeys().size());

        new JpaToscaDataType().clean();
        tdt.clean();
        assertEquals(tdtClone0, tdt);

        assertFalse(new JpaToscaDataType().validate(new PfValidationResult()).isValid());
        assertTrue(tdt.validate(new PfValidationResult()).isValid());

        tdt.getConstraints().add(null);
        assertFalse(tdt.validate(new PfValidationResult()).isValid());
        tdt.getConstraints().remove(null);
        assertTrue(tdt.validate(new PfValidationResult()).isValid());

        tdt.getProperties().add(null);
        assertFalse(tdt.validate(new PfValidationResult()).isValid());
        tdt.getProperties().remove(null);
        assertTrue(tdt.validate(new PfValidationResult()).isValid());

        try {
            tdt.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }

    }
}
