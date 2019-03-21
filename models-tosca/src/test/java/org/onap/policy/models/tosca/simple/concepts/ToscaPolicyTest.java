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
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;

/**
 * DAO test for ToscaPolicy.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class ToscaPolicyTest {

    @Test
    public void testPolicyPojo() {
        assertNotNull(new ToscaPolicy());
        assertNotNull(new ToscaPolicy(new PfConceptKey()));
        assertNotNull(new ToscaPolicy(new PfConceptKey(), new PfConceptKey()));
        assertNotNull(new ToscaPolicy(new ToscaPolicy()));

        try {
            new ToscaPolicy((PfConceptKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaPolicy(null, null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaPolicy(new PfConceptKey(), null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("type is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaPolicy(null, new PfConceptKey());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new ToscaPolicy((ToscaPolicy) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfConceptKey tpKey = new PfConceptKey("tdt", "0.0.1");
        PfConceptKey ptKey = new PfConceptKey("policyType", "0.0.1");
        ToscaPolicy tp = new ToscaPolicy(tpKey, ptKey);

        Map<String, String> propertyMap = new HashMap<>();
        propertyMap.put("Property", "Property Value");
        tp.setProperties(propertyMap);
        assertEquals(propertyMap, tp.getProperties());

        List<PfConceptKey> targets = new ArrayList<>();
        PfConceptKey target = new PfConceptKey("target", "0.0.1");
        targets.add(target);
        tp.setTargets(targets);
        assertEquals(targets, tp.getTargets());

        ToscaPolicy tdtClone0 = new ToscaPolicy(tp);
        assertEquals(tp, tdtClone0);
        assertEquals(0, tp.compareTo(tdtClone0));

        ToscaPolicy tdtClone1 = new ToscaPolicy();
        tp.copyTo(tdtClone1);
        assertEquals(tp, tdtClone1);
        assertEquals(0, tp.compareTo(tdtClone1));

        assertEquals(-1, tp.compareTo(null));
        assertEquals(0, tp.compareTo(tp));
        assertFalse(tp.compareTo(tp.getKey()) == 0);

        PfConceptKey otherDtKey = new PfConceptKey("otherDt", "0.0.1");
        ToscaPolicy otherDt = new ToscaPolicy(otherDtKey);

        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setKey(tpKey);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setType(ptKey);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setProperties(propertyMap);
        assertFalse(tp.compareTo(otherDt) == 0);
        otherDt.setTargets(targets);
        assertEquals(0, tp.compareTo(otherDt));

        try {
            tp.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(3, tp.getKeys().size());
        assertEquals(2, new ToscaPolicy().getKeys().size());

        new ToscaPolicy().clean();
        tp.clean();
        assertEquals(tdtClone0, tp);

        assertFalse(new ToscaPolicy().validate(new PfValidationResult()).isValid());
        System.err.println(tp.validate(new PfValidationResult()));
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

        try {
            tp.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }
    }
}