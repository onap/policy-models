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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;

/**
 * DAO test for ToscaPolicy.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaPolicyTest {

    private static final String KEY_IS_NULL = "key is marked @NonNull but is null";
    private static final String VERSION_001 = "0.0.1";

    @Test
    public void testPolicyPojo() {
        assertNotNull(new JpaToscaPolicy());
        assertNotNull(new JpaToscaPolicy(new PfConceptKey()));
        assertNotNull(new JpaToscaPolicy(new PfConceptKey(), new PfConceptKey()));
        assertNotNull(new JpaToscaPolicy(new JpaToscaPolicy()));

        ToscaPolicy pol = new ToscaPolicy();
        pol.setType("type");
        assertNotNull(new JpaToscaPolicy(pol));

        assertThatThrownBy(() -> {
            new JpaToscaPolicy((PfConceptKey) null);
        }).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> {
            new JpaToscaPolicy(null, null);
        }).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> {
            new JpaToscaPolicy(new PfConceptKey(), null);
        }).hasMessage("type is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaToscaPolicy(null, new PfConceptKey());
        }).hasMessage(KEY_IS_NULL);

        assertThatThrownBy(() -> new JpaToscaPolicy((JpaToscaPolicy) null))
                        .isInstanceOf(NullPointerException.class);

        PfConceptKey tpKey = new PfConceptKey("tdt", VERSION_001);
        PfConceptKey ptKey = new PfConceptKey("policyType", VERSION_001);
        JpaToscaPolicy tp = new JpaToscaPolicy(tpKey, ptKey);

        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("Property", "\"Property Value\"");
        tp.setProperties(propertyMap);
        assertEquals(propertyMap, tp.getProperties());

        List<PfConceptKey> targets = new ArrayList<>();
        PfConceptKey target = new PfConceptKey("target", VERSION_001);
        targets.add(target);
        tp.setTargets(targets);
        assertEquals(targets, tp.getTargets());

        JpaToscaPolicy tdtClone0 = new JpaToscaPolicy(tp);
        assertEquals(tp, tdtClone0);
        assertEquals(0, tp.compareTo(tdtClone0));

        JpaToscaPolicy tdtClone1 = new JpaToscaPolicy(tp);
        assertEquals(tp, tdtClone1);
        assertEquals(0, tp.compareTo(tdtClone1));

        assertEquals(-1, tp.compareTo(null));
        assertEquals(0, tp.compareTo(tp));
        assertFalse(tp.compareTo(tp.getKey()) == 0);

        PfConceptKey otherDtKey = new PfConceptKey("otherDt", VERSION_001);
        JpaToscaPolicy otherDt = new JpaToscaPolicy(otherDtKey);

        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setKey(tpKey);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setType(ptKey);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setProperties(propertyMap);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setTargets(targets);
        assertEquals(0, tp.compareTo(otherDt));

        assertEquals(3, tp.getKeys().size());
        assertEquals(2, new JpaToscaPolicy().getKeys().size());

        new JpaToscaPolicy().clean();
        tp.clean();
        assertEquals(tdtClone0, tp);

        assertFalse(new JpaToscaPolicy().validate(new PfValidationResult()).isValid());
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.getProperties().put(null, null);
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.getProperties().remove(null);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.getProperties().put("Key", null);
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.getProperties().remove("Key");
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.getProperties().put(null, "Value");
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.getProperties().remove(null);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        tp.getTargets().add(null);
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.getTargets().remove(null);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        PfConceptKey tpTypeKey = tp.getKey();
        assertNotNull(tpTypeKey);
        tp.setType(null);
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.setType(PfConceptKey.getNullKey());
        assertFalse(tp.validate(new PfValidationResult()).isValid());
        tp.setType(tpTypeKey);
        assertTrue(tp.validate(new PfValidationResult()).isValid());

        assertThatThrownBy(() -> {
            tp.validate(null);
        }).hasMessage("resultIn is marked @NonNull but is null");

        assertNotNull(tp.toAuthorative());
        tp.getType().setVersion(PfKey.NULL_KEY_VERSION);
        assertNotNull(tp.toAuthorative());
        tp.setProperties(null);
        assertNotNull(tp.toAuthorative());

        assertThatThrownBy(() -> {
            tp.fromAuthorative(null);
        }).hasMessage("toscaPolicy is marked @NonNull but is null");

        pol = new ToscaPolicy();
        pol.setName("policy");
        pol.setVersion("1.2.3");
        pol.setType("poltype");
        pol.setTypeVersion("2.2.3");
        tp.fromAuthorative(pol);
        assertEquals("2.2.3", tp.getType().getVersion());
    }
}
