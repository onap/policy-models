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

package org.onap.policy.models.pdp.persistence.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.testconcepts.DummyJpaPdpSubgroupChild;

/**
 * Test the {@link JpaPdpSubGroupSubGroup} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaPdpSubGroupTest {

    private static final String NULL_KEY_ERROR = "key is marked @NonNull but is null";
    private static final String PDP_A = "PDP-A";

    @Test
    public void testJpaPdpSubGroup() {
        assertThatThrownBy(() -> {
            new JpaPdpSubGroup((JpaPdpSubGroup) null);
        }).hasMessage("copyConcept is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup((PfReferenceKey) null);
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup((PdpSubGroup) null);
        }).hasMessage("authorativeConcept is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(null, null, null, null);
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(new PfReferenceKey(), null, null, null);
        }).hasMessage("supportedPolicyTypes is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(new PfReferenceKey(), new ArrayList<>(), null, null);
        }).hasMessage("policies is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(null, new ArrayList<>(), null, null);
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(null, new ArrayList<>(), new ArrayList<>(), null);
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(null, null, new ArrayList<>(), null);
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(null, null, null, new ArrayList<>());
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(new PfReferenceKey(), null, null, new ArrayList<>());
        }).hasMessage("supportedPolicyTypes is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(new PfReferenceKey(), new ArrayList<>(), null, new ArrayList<>());
        }).hasMessage("policies is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(null, new ArrayList<>(), null, new ArrayList<>());
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpSubGroup(null, null, new ArrayList<>(), null);
        }).hasMessage(NULL_KEY_ERROR);

        assertNotNull(new JpaPdpSubGroup((new PfReferenceKey())));

        PdpSubGroup testPdpSubgroup = new PdpSubGroup();
        testPdpSubgroup.setPdpType(PDP_A);
        JpaPdpSubGroup testJpaPdpSubGroup = new JpaPdpSubGroup();
        testJpaPdpSubGroup.setKey(null);
        testJpaPdpSubGroup.fromAuthorative(testPdpSubgroup);
        assertEquals(PDP_A, testJpaPdpSubGroup.getKey().getLocalName());
        testJpaPdpSubGroup.setKey(PfReferenceKey.getNullKey());
        testJpaPdpSubGroup.fromAuthorative(testPdpSubgroup);

        assertThatThrownBy(() -> {
            testJpaPdpSubGroup.fromAuthorative(null);
        }).hasMessage("pdpSubgroup is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            testJpaPdpSubGroup.copyTo(null);
        }).hasMessage("target is marked @NonNull but is null");

        assertEquals(PDP_A, testJpaPdpSubGroup.getKey().getLocalName());
        assertEquals(PDP_A, new JpaPdpSubGroup(testPdpSubgroup).getKey().getLocalName());
        assertEquals(PDP_A, ((PfReferenceKey) new JpaPdpSubGroup(testPdpSubgroup).getKeys().get(0)).getLocalName());

        testJpaPdpSubGroup.clean();
        assertEquals(PDP_A, testJpaPdpSubGroup.getKey().getLocalName());

        assertThatThrownBy(() -> {
            testJpaPdpSubGroup.validate(null);
        }).hasMessage("resultIn is marked @NonNull but is null");

        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).toString()
                .contains("INVALID:parent of key is a null key"));

        testJpaPdpSubGroup.getKey().setParentConceptKey(new PfConceptKey("Parent:1.0.0"));
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).toString()
                .contains("INVALID:parent of key is a null key"));
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).toString()
                .contains("INVALID:a PDP subgroup must support at least one policy type"));

        testJpaPdpSubGroup.setSupportedPolicyTypes(new ArrayList<>());
        testJpaPdpSubGroup.getSupportedPolicyTypes().add(new PfConceptKey("APolicyType:1.0.0"));
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).toString()
                .contains("INVALID:a PDP subgroup must support at least one policy type"));

        PfReferenceKey savedKey = testJpaPdpSubGroup.getKey();
        testJpaPdpSubGroup.setKey(PfReferenceKey.getNullKey());
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.setKey(savedKey);
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());

        testJpaPdpSubGroup.setProperties(new LinkedHashMap<>());
        testJpaPdpSubGroup.getProperties().put(null, null);
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.getProperties().remove(null);
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());

        testJpaPdpSubGroup.setProperties(new LinkedHashMap<>());
        testJpaPdpSubGroup.getProperties().put("NullKey", null);
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.getProperties().remove("NullKey");
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());

        testJpaPdpSubGroup.setDesiredInstanceCount(-1);
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.setDesiredInstanceCount(0);
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.setProperties(null);

        testJpaPdpSubGroup.setCurrentInstanceCount(-1);
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.setCurrentInstanceCount(0);
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.setProperties(null);
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());

        List<PfConceptKey> supportedPolicyTypes = testJpaPdpSubGroup.getSupportedPolicyTypes();
        assertNotNull(supportedPolicyTypes);
        testJpaPdpSubGroup.setSupportedPolicyTypes(null);
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.setSupportedPolicyTypes(new ArrayList<>());
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.setSupportedPolicyTypes(supportedPolicyTypes);
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());

        List<PfConceptKey> supportedPolicies = testJpaPdpSubGroup.getPolicies();
        assertNotNull(supportedPolicies);
        testJpaPdpSubGroup.setPolicies(null);
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.setPolicies(new ArrayList<>());
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.setPolicies(supportedPolicies);
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());

        List<JpaPdp> pdpInstances = testJpaPdpSubGroup.getPdpInstances();
        assertNotNull(pdpInstances);
        testJpaPdpSubGroup.setPdpInstances(null);
        assertFalse(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.setPdpInstances(new ArrayList<>());
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpSubGroup.setPdpInstances(pdpInstances);
        assertTrue(testJpaPdpSubGroup.validate(new PfValidationResult()).isOk());

        JpaPdpSubGroup otherJpaPdpSubGroup = new JpaPdpSubGroup(testJpaPdpSubGroup);
        assertEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));
        assertEquals(-1, testJpaPdpSubGroup.compareTo(null));
        assertEquals(0, testJpaPdpSubGroup.compareTo(testJpaPdpSubGroup));
        assertFalse(testJpaPdpSubGroup.compareTo(new DummyJpaPdpSubgroupChild()) == 0);

        testJpaPdpSubGroup.getKey().setParentKeyName("Parent1");
        assertEquals(1, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));
        testJpaPdpSubGroup.getKey().setParentKeyName("Parent");
        assertEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));

        testJpaPdpSubGroup.setCurrentInstanceCount(1);
        assertEquals(1, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));
        testJpaPdpSubGroup.setCurrentInstanceCount(0);
        assertEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));

        testJpaPdpSubGroup.setDesiredInstanceCount(1);
        assertEquals(1, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));
        testJpaPdpSubGroup.setDesiredInstanceCount(0);
        assertEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));

        PfConceptKey anotherPolicyType = new PfConceptKey("AnotherPolicyType", "1.0.0");
        testJpaPdpSubGroup.getSupportedPolicyTypes().add(anotherPolicyType);
        assertNotEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));
        testJpaPdpSubGroup.getSupportedPolicyTypes().remove(anotherPolicyType);
        assertEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));

        PfConceptKey anotherPolicy = new PfConceptKey("AnotherPolicy", "1.0.0");
        testJpaPdpSubGroup.getPolicies().add(anotherPolicy);
        assertNotEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));
        testJpaPdpSubGroup.getPolicies().remove(anotherPolicy);
        assertEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));

        JpaPdp anotherPdp = new JpaPdp(new PfReferenceKey(testJpaPdpSubGroup.getKey(), "AnotherPdp"));
        testJpaPdpSubGroup.getPdpInstances().add(anotherPdp);
        assertNotEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));
        testJpaPdpSubGroup.getPdpInstances().remove(anotherPdp);
        assertEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));

        testJpaPdpSubGroup.setProperties(new LinkedHashMap<>());
        testJpaPdpSubGroup.getProperties().put("AnotherProperty", "Some String");
        assertNotEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));
        testJpaPdpSubGroup.getProperties().remove("AnotherProperty");
        testJpaPdpSubGroup.setProperties(null);
        assertEquals(0, testJpaPdpSubGroup.compareTo(otherJpaPdpSubGroup));

        PdpSubGroup psg = testJpaPdpSubGroup.toAuthorative();
        assertNull(psg.getProperties());

        testJpaPdpSubGroup.setProperties(new LinkedHashMap<>());
        psg = testJpaPdpSubGroup.toAuthorative();
        assertEquals(0, psg.getProperties().size());

        testJpaPdpSubGroup.getPolicies().add(new PfConceptKey("APolicy:1.0.0"));
        testJpaPdpSubGroup.getPdpInstances().add(new JpaPdp());

        testJpaPdpSubGroup.getProperties().put(" PropKey ", " Prop Value ");
        testJpaPdpSubGroup.clean();
        assertEquals("PropKey", testJpaPdpSubGroup.getProperties().keySet().iterator().next());
        assertEquals("Prop Value", testJpaPdpSubGroup.getProperties().get("PropKey"));

        assertEquals(4, testJpaPdpSubGroup.getKeys().size());
    }
}
