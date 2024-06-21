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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;
import org.onap.policy.models.tosca.authorative.concepts.ToscaDataType;

/**
 * DAO test for JpaToscaDatatype.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class JpaToscaDataTypeTest {

    private static final String VERSION_001 = "0.0.1";

    @Test
    void testDataTypePojo() {
        assertNotNull(new JpaToscaDataType());
        assertNotNull(new JpaToscaDataType(new PfConceptKey()));
        assertNotNull(new JpaToscaDataType(new JpaToscaDataType()));
        assertNotNull(new JpaToscaDataType(new ToscaDataType()));

        assertThatThrownBy(() -> {
            new JpaToscaDataType((PfConceptKey) null);
        }).hasMessageMatching("key is marked .*on.*ull but is null");

        assertThatThrownBy(() -> new JpaToscaDataType((JpaToscaDataType) null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testDataTypeProperties() {
        PfConceptKey dtKey = new PfConceptKey("tdt", VERSION_001);
        JpaToscaDataType tdt = new JpaToscaDataType(dtKey);

        List<JpaToscaConstraint> constraints = new ArrayList<>();
        JpaToscaConstraintLogical lsc = new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, "hello");
        constraints.add(lsc);
        tdt.setConstraints(constraints);
        assertEquals(constraints, tdt.getConstraints());

        Map<String, JpaToscaProperty> properties = new LinkedHashMap<>();
        JpaToscaProperty tp =
                new JpaToscaProperty(new PfReferenceKey(dtKey, "pr"), new PfConceptKey("type", VERSION_001));
        properties.put(tp.getKey().getLocalName(), tp);
        tdt.setProperties(properties);
        assertEquals(properties, tdt.getProperties());

        JpaToscaDataType tdtClone0 = new JpaToscaDataType(tdt);
        assertEquals(tdt, tdtClone0);
        assertEquals(0, tdt.compareTo(tdtClone0));

        JpaToscaDataType tdtClone1 = new JpaToscaDataType(tdt);
        assertEquals(tdt, tdtClone1);
        assertEquals(0, tdt.compareTo(tdtClone1));

        assertEquals(-1, tdt.compareTo(null));
        assertEquals(0, tdt.compareTo(tdt));
        assertNotEquals(0, tdt.compareTo(tdt.getKey()));

        PfConceptKey otherDtKey = new PfConceptKey("otherDt", VERSION_001);
        JpaToscaDataType otherDt = new JpaToscaDataType(otherDtKey);

        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setKey(dtKey);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setConstraints(constraints);
        assertNotEquals(0, tdt.compareTo(otherDt));
        otherDt.setProperties(properties);
        assertEquals(0, tdt.compareTo(otherDt));

        assertEquals(3, tdt.getKeys().size());
        assertEquals(1, new JpaToscaDataType().getKeys().size());

        new JpaToscaDataType().clean();
        tdt.clean();
        assertEquals(tdtClone0, tdt);

        assertFalse(new JpaToscaDataType().validate("").isValid());
        validateJpaToscaDataTypeOperations(tdt);

        assertThatThrownBy(() -> {
            tdt.validate(null);
        }).hasMessageMatching("fieldName is marked .*on.*ull but is null");
    }

    private void validateJpaToscaDataTypeOperations(JpaToscaDataType tdt) {
        assertTrue(tdt.validate("").isValid());

        tdt.getConstraints().add(null);
        assertFalse(tdt.validate("").isValid());
        tdt.getConstraints().remove(null);
        assertTrue(tdt.validate("").isValid());

        tdt.getProperties().put(null, null);
        assertFalse(tdt.validate("").isValid());
        tdt.getProperties().remove(null);
        assertTrue(tdt.validate("").isValid());
    }

    @Test
    void testDataTypeConstraints() {
        ToscaDataType dat = new ToscaDataType();
        dat.setName("name");
        dat.setVersion("1.2.3");
        dat.setConstraints(new ArrayList<>());
        ToscaConstraint constraint = new ToscaConstraint();
        constraint.setEqual("EqualTo");
        dat.getConstraints().add(constraint);

        JpaToscaDataType tdta = new JpaToscaDataType();
        tdta.fromAuthorative(dat);
        assertEquals("name", tdta.getKey().getName());

        ToscaDataType datOut = tdta.toAuthorative();
        assertNotNull(datOut);
    }

    @Test
    void testGetReferencedDataTypes() {
        JpaToscaDataType dt0 = new JpaToscaDataType(new PfConceptKey("dt0", "0.0.1"));

        assertTrue(dt0.getReferencedDataTypes().isEmpty());

        dt0.setProperties(new LinkedHashMap<>());
        assertTrue(dt0.getReferencedDataTypes().isEmpty());

        JpaToscaProperty prop0 = new JpaToscaProperty(new PfReferenceKey(dt0.getKey(), "prop0"));
        prop0.setType(new PfConceptKey("string", PfKey.NULL_KEY_VERSION));
        assertTrue(prop0.validate("").isValid());

        dt0.getProperties().put(prop0.getKey().getLocalName(), prop0);
        assertTrue(dt0.getReferencedDataTypes().isEmpty());

        JpaToscaProperty prop1 = new JpaToscaProperty(new PfReferenceKey(dt0.getKey(), "prop1"));
        prop1.setType(new PfConceptKey("the.property.Type0", "0.0.1"));
        assertTrue(prop1.validate("").isValid());

        dt0.getProperties().put(prop1.getKey().getLocalName(), prop1);
        assertEquals(1, dt0.getReferencedDataTypes().size());

        JpaToscaProperty prop2 = new JpaToscaProperty(new PfReferenceKey(dt0.getKey(), "prop2"));
        prop2.setType(new PfConceptKey("the.property.Type0", "0.0.1"));
        assertTrue(prop2.validate("").isValid());

        dt0.getProperties().put(prop2.getKey().getLocalName(), prop2);
        assertEquals(1, dt0.getReferencedDataTypes().size());

        JpaToscaProperty prop3 = new JpaToscaProperty(new PfReferenceKey(dt0.getKey(), "prop4"));
        prop3.setType(new PfConceptKey("the.property.Type1", "0.0.1"));
        prop3.setEntrySchema(new JpaToscaSchemaDefinition());
        prop3.getEntrySchema().setType(new PfConceptKey("the.property.Type3", "0.0.1"));
        assertTrue(prop3.validate("").isValid());

        dt0.getProperties().put(prop3.getKey().getLocalName(), prop3);
        assertEquals(3, dt0.getReferencedDataTypes().size());

        JpaToscaProperty prop4 = new JpaToscaProperty(new PfReferenceKey(dt0.getKey(), "prop4"));
        prop4.setType(new PfConceptKey("the.property.Type1", "0.0.1"));
        prop4.setEntrySchema(new JpaToscaSchemaDefinition());
        prop4.getEntrySchema().setType(new PfConceptKey("the.property.Type2", "0.0.1"));
        assertTrue(prop4.validate("").isValid());

        dt0.getProperties().put(prop4.getKey().getLocalName(), prop4);
        assertEquals(3, dt0.getReferencedDataTypes().size());
    }
}
