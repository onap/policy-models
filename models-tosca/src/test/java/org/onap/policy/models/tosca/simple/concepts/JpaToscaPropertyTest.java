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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;

/**
 * DAO test for ToscaProperty.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaPropertyTest {

    private static final String KEY_IS_NULL = "key is marked .*on.*ull but is null";
    private static final String DEFAULT_KEY = "defaultKey";
    private static final String A_DESCRIPTION = "A Description";
    private static final String VERSION_001 = "0.0.1";

    @Test
    public void testPropertyPojo() {
        assertNotNull(new JpaToscaProperty());
        assertNotNull(new JpaToscaProperty(new PfReferenceKey()));
        assertNotNull(new JpaToscaProperty(new PfReferenceKey(), new PfConceptKey()));
        assertNotNull(new JpaToscaProperty(new JpaToscaProperty()));

        assertThatThrownBy(() -> new JpaToscaProperty((PfReferenceKey) null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaProperty(null, null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaProperty(null, new PfConceptKey())).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaProperty(new PfReferenceKey(), null))
                .hasMessageMatching("type is marked .*on.*ull but is null");

        PfConceptKey pparentKey = new PfConceptKey("tParentKey", VERSION_001);
        PfReferenceKey pkey = new PfReferenceKey(pparentKey, "trigger0");
        PfConceptKey ptypeKey = new PfConceptKey("TTypeKey", VERSION_001);
        JpaToscaProperty tp = new JpaToscaProperty(pkey, ptypeKey);

        assertEquals(tp, new JpaToscaProperty(tp));

        tp.setDescription(A_DESCRIPTION);
        assertEquals(A_DESCRIPTION, tp.getDescription());

        tp.setRequired(false);
        assertFalse(tp.isRequired());

        tp.setDefaultValue(DEFAULT_KEY);

        tp.setStatus(ToscaProperty.Status.SUPPORTED);

        List<JpaToscaConstraint> constraints = new ArrayList<>();
        JpaToscaConstraintLogical lsc = new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, "hello");
        constraints.add(lsc);
        tp.setConstraints(constraints);
        assertEquals(constraints, tp.getConstraints());

        PfConceptKey typeKey = new PfConceptKey("type", VERSION_001);
        JpaToscaEntrySchema tes = new JpaToscaEntrySchema(typeKey);
        tp.setEntrySchema(tes);

        TreeMap<String, String> metadata = new TreeMap<>();
        metadata.put("metaA", "dataA");
        metadata.put("metaB", "dataB");
        tp.setMetadata(metadata);
        assertSame(metadata, tp.getMetadata());

        JpaToscaProperty tdtClone0 = new JpaToscaProperty(tp);
        assertEquals(tp, tdtClone0);
        assertEquals(0, tp.compareTo(tdtClone0));

        assertNotSame(tdtClone0.getMetadata(), tp.getMetadata());

        JpaToscaProperty tdtClone1 = new JpaToscaProperty(tp);
        assertEquals(tp, tdtClone1);
        assertEquals(0, tp.compareTo(tdtClone1));

        assertEquals(-1, tp.compareTo(null));
        assertEquals(0, tp.compareTo(tp));
        assertNotEquals(0, tp.compareTo(tp.getKey()));

        PfReferenceKey otherDtKey = new PfReferenceKey("otherDt", VERSION_001, "OtherProperty");
        JpaToscaProperty otherDt = new JpaToscaProperty(otherDtKey);

        assertNotEquals(0, tp.compareTo(otherDt));
        otherDt.setKey(pkey);
        assertNotEquals(0, tp.compareTo(otherDt));
        otherDt.setType(ptypeKey);
        assertNotEquals(0, tp.compareTo(otherDt));
        otherDt.setDescription(A_DESCRIPTION);
        assertNotEquals(0, tp.compareTo(otherDt));
        otherDt.setRequired(false);
        assertNotEquals(0, tp.compareTo(otherDt));
        otherDt.setDefaultValue(DEFAULT_KEY);
        assertNotEquals(0, tp.compareTo(otherDt));
        otherDt.setStatus(ToscaProperty.Status.SUPPORTED);
        assertNotEquals(0, tp.compareTo(otherDt));
        assertNotEquals(0, tp.compareTo(otherDt));
        otherDt.setConstraints(constraints);
        assertNotEquals(0, tp.compareTo(otherDt));
        otherDt.setEntrySchema(tes);
        assertEquals(0, tp.compareTo(otherDt));

        otherDt.setRequired(true);
        assertNotEquals(0, tp.compareTo(otherDt));
        otherDt.setRequired(false);
        assertEquals(0, tp.compareTo(otherDt));

        otherDt.setStatus(ToscaProperty.Status.UNSUPPORTED);
        assertNotEquals(0, tp.compareTo(otherDt));
        otherDt.setStatus(ToscaProperty.Status.SUPPORTED);
        assertEquals(0, tp.compareTo(otherDt));

        assertThatThrownBy(() -> new JpaToscaProperty((JpaToscaProperty) null))
                .isInstanceOf(NullPointerException.class);

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
        tp.setDescription(A_DESCRIPTION);
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
        tp.setDefaultValue("");
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.setDefaultValue(DEFAULT_KEY);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.getConstraints().add(null);
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.getConstraints().remove(null);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.setMetadata(null);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        assertThatThrownBy(() -> tp.validate(null)).hasMessageMatching("resultIn is marked .*on.*ull but is null");
    }

    @Test
    public void testToAuthorative_testFromAuthorative() {
        // check with empty structure
        JpaToscaProperty tp = new JpaToscaProperty();
        ToscaProperty auth = tp.toAuthorative();
        JpaToscaProperty tp2 = new JpaToscaProperty();
        tp2.fromAuthorative(auth);
        assertEquals(tp, tp2);

        // populate and try again
        PfConceptKey pparentKey = new PfConceptKey("tParentKey", VERSION_001);
        PfReferenceKey pkey = new PfReferenceKey(pparentKey, "trigger0");
        PfConceptKey ptypeKey = new PfConceptKey("TTypeKey", VERSION_001);
        tp = new JpaToscaProperty(pkey, ptypeKey);

        tp.setDescription(A_DESCRIPTION);
        tp.setRequired(true);
        tp.setDefaultValue(DEFAULT_KEY);
        tp.setStatus(ToscaProperty.Status.SUPPORTED);

        List<JpaToscaConstraint> constraints = new ArrayList<>();
        JpaToscaConstraintLogical lsc = new JpaToscaConstraintLogical(JpaToscaConstraintOperation.EQ, "hello");
        constraints.add(lsc);
        tp.setConstraints(constraints);

        PfConceptKey typeKey = new PfConceptKey("type", VERSION_001);
        JpaToscaEntrySchema tes = new JpaToscaEntrySchema(typeKey);
        tp.setEntrySchema(tes);

        TreeMap<String, String> metadata = new TreeMap<>();
        metadata.put("metaA", "dataA");
        metadata.put("metaB", "dataB");
        tp.setMetadata(metadata);

        auth = tp.toAuthorative();
        tp2 = new JpaToscaProperty();
        tp2.fromAuthorative(auth);

        // note: parent key info is not copied, so we manually copy it
        tp2.getKey().setParentConceptKey(tp.getKey().getParentConceptKey());

        assertEquals(tp.toString(), tp2.toString());
        assertEquals(tp, tp2);
    }
}
