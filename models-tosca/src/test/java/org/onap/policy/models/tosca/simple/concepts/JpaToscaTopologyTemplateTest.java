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

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;

/**
 * DAO test for ToscaDatatype.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaToscaTopologyTemplateTest {

    @Test
    public void testTopologyTemplatePojo() {
        assertNotNull(new JpaToscaTopologyTemplate());
        assertNotNull(new JpaToscaTopologyTemplate(new PfReferenceKey()));
        assertNotNull(new JpaToscaTopologyTemplate(new JpaToscaTopologyTemplate()));
        assertNotNull(new JpaToscaTopologyTemplate(new ToscaTopologyTemplate()));

        try {
            new JpaToscaTopologyTemplate((PfReferenceKey) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new JpaToscaTopologyTemplate((JpaToscaTopologyTemplate) null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        PfReferenceKey tttKey = new PfReferenceKey("tst", "0.0.1", "ttt");
        JpaToscaTopologyTemplate ttt = new JpaToscaTopologyTemplate(tttKey);

        ttt.setDescription("A Description");
        assertEquals("A Description", ttt.getDescription());

        PfConceptKey policy0TypeKey = new PfConceptKey("Policy0Type", "0.0.1");
        PfConceptKey policy0Key = new PfConceptKey("Policy0", "0.0.1");

        JpaToscaPolicy policy0 = new JpaToscaPolicy(policy0Key, policy0TypeKey);
        PfConceptKey polsKey = new PfConceptKey("pols", "0.0.1");
        Map<PfConceptKey, JpaToscaPolicy> policyMap = new TreeMap<>();
        policyMap.put(policy0Key, policy0);
        JpaToscaPolicies policies = new JpaToscaPolicies(polsKey, policyMap);
        ttt.setPolicies(policies);

        JpaToscaTopologyTemplate tttClone0 = new JpaToscaTopologyTemplate(ttt);
        assertEquals(ttt, tttClone0);
        assertEquals(0, ttt.compareTo(tttClone0));

        JpaToscaTopologyTemplate tttClone1 = new JpaToscaTopologyTemplate();
        ttt.copyTo(tttClone1);
        assertEquals(ttt, tttClone1);
        assertEquals(0, ttt.compareTo(tttClone1));

        assertEquals(-1, ttt.compareTo(null));
        assertEquals(0, ttt.compareTo(ttt));
        assertFalse(ttt.compareTo(ttt.getKey()) == 0);

        PfReferenceKey otherDtKey = new PfReferenceKey("otherSt", "0.0.1", "otherDt");
        JpaToscaTopologyTemplate otherDt = new JpaToscaTopologyTemplate(otherDtKey);

        assertFalse(ttt.compareTo(otherDt) == 0);
        otherDt.setKey(tttKey);
        assertFalse(ttt.compareTo(otherDt) == 0);
        otherDt.setDescription("A Description");
        assertFalse(ttt.compareTo(otherDt) == 0);
        otherDt.setPolicies(policies);
        assertEquals(0, ttt.compareTo(otherDt));

        try {
            ttt.copyTo(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(4, ttt.getKeys().size());
        assertEquals(1, new JpaToscaTopologyTemplate().getKeys().size());

        new JpaToscaTopologyTemplate().clean();
        ttt.clean();
        assertEquals(tttClone0, ttt);

        assertTrue(new JpaToscaTopologyTemplate().validate(new PfValidationResult()).isValid());
        assertTrue(ttt.validate(new PfValidationResult()).isValid());

        ttt.setKey(PfReferenceKey.getNullKey());
        assertFalse(ttt.validate(new PfValidationResult()).isValid());
        ttt.setKey(tttKey);
        assertTrue(ttt.validate(new PfValidationResult()).isValid());

        ttt.setDescription(null);
        assertTrue(ttt.validate(new PfValidationResult()).isValid());
        ttt.setDescription("");
        assertFalse(ttt.validate(new PfValidationResult()).isValid());
        ttt.setDescription("A Description");
        assertTrue(ttt.validate(new PfValidationResult()).isValid());

        try {
            ttt.validate(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }
    }
}
