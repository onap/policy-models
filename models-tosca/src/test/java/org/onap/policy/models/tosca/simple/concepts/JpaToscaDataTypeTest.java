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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConstraint;
import org.onap.policy.models.tosca.authorative.concepts.ToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraint;
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
        assertNotNull(new JpaToscaDataType(new ToscaDataType()));

        assertThatThrownBy(() -> {
            new JpaToscaDataType((PfConceptKey) null);
        }).hasMessage("key is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaToscaDataType((JpaToscaDataType) null);
        }).hasMessage("copyConcept is marked @NonNull but is null");

        PfConceptKey dtKey = new PfConceptKey("tdt", "0.0.1");
        JpaToscaDataType tdt = new JpaToscaDataType(dtKey);

        List<JpaToscaConstraint> constraints = new ArrayList<>();
        JpaToscaConstraintLogical lsc = new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, "hello");
        constraints.add(lsc);
        tdt.setConstraints(constraints);
        assertEquals(constraints, tdt.getConstraints());

        Map<String, JpaToscaProperty> properties = new LinkedHashMap<>();
        JpaToscaProperty tp = new JpaToscaProperty(new PfReferenceKey(dtKey, "pr"), new PfConceptKey("type", "0.0.1"));
        properties.put(tp.getKey().getLocalName(), tp);
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

        assertThatThrownBy(() -> {
            tdt.copyTo(null);
        }).hasMessage("target is marked @NonNull but is null");

        assertEquals(3, tdt.getKeys().size());
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

        tdt.getProperties().put(null, null);
        assertFalse(tdt.validate(new PfValidationResult()).isValid());
        tdt.getProperties().remove(null);
        assertTrue(tdt.validate(new PfValidationResult()).isValid());

        assertThatThrownBy(() -> {
            tdt.validate(null);
        }).hasMessage("resultIn is marked @NonNull but is null");

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
}
