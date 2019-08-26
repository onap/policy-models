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
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaProperty;

/**
 * DAO test for ToscaProperty.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaPropertyTest {

    private static final String KEY_IS_NULL = "key is marked @NonNull but is null";
    private static final String DEFAULT_KEY = "defaultKey";
    private static final String A_DESCRIPTION = "A Description";
    private static final String VERSION_001 = "0.0.1";

    @Test
    public void testPropertyPojo() {
        assertNotNull(new JpaToscaProperty());
        assertNotNull(new JpaToscaProperty(new PfReferenceKey()));
        assertNotNull(new JpaToscaProperty(new PfReferenceKey(), new PfConceptKey()));
        assertNotNull(new JpaToscaProperty(new JpaToscaProperty()));

        assertThatThrownBy(() -> new JpaToscaProperty((PfReferenceKey) null)).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaProperty(null, null)).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaProperty(null, new PfConceptKey())).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaProperty(new PfReferenceKey(), null))
                        .hasMessage("type is marked @NonNull but is null");

        PfConceptKey pparentKey = new PfConceptKey("tParentKey", VERSION_001);
        PfReferenceKey pkey = new PfReferenceKey(pparentKey, "trigger0");
        PfConceptKey ptypeKey = new PfConceptKey("TTypeKey", VERSION_001);
        JpaToscaProperty tp = new JpaToscaProperty(pkey, ptypeKey);

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

        JpaToscaProperty tdtClone0 = new JpaToscaProperty(tp);
        assertEquals(tp, tdtClone0);
        assertEquals(0, tp.compareTo(tdtClone0));

        JpaToscaProperty tdtClone1 = new JpaToscaProperty(tp);
        assertEquals(tp, tdtClone1);
        assertEquals(0, tp.compareTo(tdtClone1));

        assertEquals(-1, tp.compareTo(null));
        assertEquals(0, tp.compareTo(tp));
        assertFalse(tp.compareTo(tp.getKey()) == 0);

        PfReferenceKey otherDtKey = new PfReferenceKey("otherDt", VERSION_001, "OtherProperty");
        JpaToscaProperty otherDt = new JpaToscaProperty(otherDtKey);

        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setKey(pkey);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setType(ptypeKey);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setDescription(A_DESCRIPTION);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setRequired(false);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setDefaultValue(DEFAULT_KEY);
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

        assertThatThrownBy(() -> tp.validate(null)).hasMessage("resultIn is marked @NonNull but is null");
    }
}
