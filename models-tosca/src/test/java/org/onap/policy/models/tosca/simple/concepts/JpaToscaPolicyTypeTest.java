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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaEntityType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaProperty;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTrigger;

/**
 * DAO test for ToscaPolicyType.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaPolicyTypeTest {
    @Test
    public void testPolicyTypePojo() {
        assertNotNull(new JpaToscaPolicyType());
        assertNotNull(new JpaToscaPolicyType(new PfConceptKey()));
        assertNotNull(new JpaToscaPolicyType(new JpaToscaPolicyType()));

        try {
            new JpaToscaPolicyType((PfConceptKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaPolicyType((JpaToscaPolicyType) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey ptKey = new PfConceptKey("tdt", "0.0.1");
        JpaToscaPolicyType tpt = new JpaToscaPolicyType(ptKey);

        PfConceptKey derivedFromKey = new PfConceptKey("deriveFrom", "0.0.1");
        tpt.setDerivedFrom(derivedFromKey);

        Map<String, String> metadata = new HashMap<>();
        metadata.put("key", "value");
        tpt.setMetadata(metadata);
        assertEquals(metadata, tpt.getMetadata());

        tpt.setDescription("A Description");

        PfConceptKey propTypeKey = new PfConceptKey("propType", "0.0.1");
        Map<String, JpaToscaProperty> properties = new LinkedHashMap<>();
        JpaToscaProperty tp = new JpaToscaProperty(new PfReferenceKey(ptKey, "aProp"), propTypeKey);
        properties.put(tp.getKey().getLocalName(), tp);
        tpt.setProperties(properties);
        assertEquals(properties, tpt.getProperties());

        List<PfConceptKey> targets = new ArrayList<>();
        PfConceptKey target = new PfConceptKey("target", "0.0.1");
        targets.add(target);
        tpt.setTargets(targets);
        assertEquals(targets, tpt.getTargets());

        List<JpaToscaTrigger> triggers = new ArrayList<>();
        JpaToscaTrigger trigger = new JpaToscaTrigger(new PfReferenceKey(ptKey, "aTrigger"), "EventType", "Action");
        triggers.add(trigger);
        tpt.setTriggers(triggers);
        assertEquals(triggers, tpt.getTriggers());

        JpaToscaPolicyType tdtClone0 = new JpaToscaPolicyType(tpt);
        assertEquals(tpt, tdtClone0);
        assertEquals(0, tpt.compareTo(tdtClone0));

        JpaToscaPolicyType tdtClone1 = new JpaToscaPolicyType();
        tpt.copyTo(tdtClone1);
        assertEquals(tpt, tdtClone1);
        assertEquals(0, tpt.compareTo(tdtClone1));

        assertEquals(-1, tpt.compareTo(null));
        assertEquals(0, tpt.compareTo(tpt));
        assertFalse(tpt.compareTo(tpt.getKey()) == 0);

        PfConceptKey otherDtKey = new PfConceptKey("otherDt", "0.0.1");
        JpaToscaPolicyType otherDt = new JpaToscaPolicyType(otherDtKey);

        assertFalse(tpt.compareTo(otherDt) == 0);
        otherDt.setKey(ptKey);
        assertFalse(tpt.compareTo(otherDt) == 0);
        otherDt.setDerivedFrom(derivedFromKey);
        assertFalse(tpt.compareTo(otherDt) == 0);
        otherDt.setMetadata(metadata);
        assertFalse(tpt.compareTo(otherDt) == 0);
        otherDt.setDescription("A Description");
        assertFalse(tpt.compareTo(otherDt) == 0);
        otherDt.setProperties(properties);
        assertFalse(tpt.compareTo(otherDt) == 0);
        otherDt.setTargets(targets);
        assertFalse(tpt.compareTo(otherDt) == 0);
        otherDt.setTriggers(triggers);
        assertEquals(0, tpt.compareTo(otherDt));

        try {
            tpt.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(6, tpt.getKeys().size());
        assertEquals(1, new JpaToscaPolicyType().getKeys().size());

        new JpaToscaPolicyType().clean();
        tpt.clean();
        assertEquals(tdtClone0, tpt);

        assertFalse(new JpaToscaPolicyType().validate(new PfValidationResult()).isValid());
        assertTrue(tpt.validate(new PfValidationResult()).isValid());

        tpt.getProperties().put(null, null);
        assertFalse(tpt.validate(new PfValidationResult()).isValid());
        tpt.getProperties().remove(null);
        assertTrue(tpt.validate(new PfValidationResult()).isValid());

        tpt.getTargets().add(null);
        assertFalse(tpt.validate(new PfValidationResult()).isValid());
        tpt.getTargets().remove(null);
        assertTrue(tpt.validate(new PfValidationResult()).isValid());

        tpt.getTriggers().add(null);
        assertFalse(tpt.validate(new PfValidationResult()).isValid());
        tpt.getTriggers().remove(null);
        assertTrue(tpt.validate(new PfValidationResult()).isValid());

        tpt.getMetadata().put(null, null);
        assertFalse(tpt.validate(new PfValidationResult()).isValid());
        tpt.getMetadata().remove(null);
        assertTrue(tpt.validate(new PfValidationResult()).isValid());

        tpt.getMetadata().put("nullKey", null);
        assertFalse(tpt.validate(new PfValidationResult()).isValid());
        tpt.getMetadata().remove("nullKey");
        assertTrue(tpt.validate(new PfValidationResult()).isValid());

        tpt.setDescription("");;
        assertFalse(tpt.validate(new PfValidationResult()).isValid());
        tpt.setDescription("A Description");
        assertTrue(tpt.validate(new PfValidationResult()).isValid());

        tpt.setDerivedFrom(PfConceptKey.getNullKey());
        assertFalse(tpt.validate(new PfValidationResult()).isValid());
        tpt.setDerivedFrom(derivedFromKey);
        assertTrue(tpt.validate(new PfValidationResult()).isValid());

        try {
            tpt.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaEntityType<ToscaPolicy>((PfConceptKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaEntityType<ToscaPolicy>((JpaToscaEntityType<ToscaPolicy>) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        JpaToscaEntityType<ToscaPolicy> tet = new JpaToscaEntityType<ToscaPolicy>(tpt.getKey());
        assertEquals(-1, tet.compareTo(null));
        assertEquals(0, tet.compareTo(tet));
        assertFalse(tet.compareTo(tet.getKey()) == 0);
    }
}
