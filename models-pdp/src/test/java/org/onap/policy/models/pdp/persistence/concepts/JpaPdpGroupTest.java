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
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.pdp.testconcepts.DummyJpaPdpSubgroupChild;

/**
 * Test the {@link JpaPdpGroupSubGroup} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class JpaPdpGroupTest {

    private static final String NULL_KEY_ERROR = "key is marked @NonNull but is null";
    private static final String PDP_GROUP0 = "PDPGroup0";
    private static final String VERSION = "1.0.0";

    @Test
    public void testJpaPdpGroup() {
        assertThatThrownBy(() -> {
            new JpaPdpGroup((JpaPdpGroup) null);
        }).hasMessage("copyConcept is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpGroup((PfConceptKey) null);
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup((PdpGroup) null);
        }).hasMessage("authorativeConcept is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpGroup((JpaPdpGroup) null);
        }).hasMessage("copyConcept is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpGroup(null, null, null);
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup(new PfConceptKey(), null, null);
        }).hasMessage("pdpGroupState is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpGroup(new PfConceptKey(), PdpState.PASSIVE, null);
        }).hasMessage("pdpSubGroups is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new JpaPdpGroup(null, PdpState.PASSIVE, null);
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup(null, PdpState.PASSIVE, new ArrayList<>());
        }).hasMessage(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup(null, null, new ArrayList<>());
        }).hasMessage(NULL_KEY_ERROR);

        assertNotNull(new JpaPdpGroup((new PfConceptKey())));
        assertNotNull(new JpaPdpGroup((new JpaPdpGroup())));

        PdpGroup testPdpGroup = new PdpGroup();
        testPdpGroup.setName(PDP_GROUP0);
        testPdpGroup.setPdpSubgroups(new ArrayList<>());
        JpaPdpGroup testJpaPdpGroup = new JpaPdpGroup();
        testJpaPdpGroup.setKey(null);

        testJpaPdpGroup.setKey(new PfConceptKey());

        testPdpGroup.setVersion(VERSION);
        testJpaPdpGroup.fromAuthorative(testPdpGroup);

        assertEquals(PDP_GROUP0, testJpaPdpGroup.getKey().getName());
        testJpaPdpGroup.setKey(PfConceptKey.getNullKey());
        testJpaPdpGroup.fromAuthorative(testPdpGroup);

        assertThatThrownBy(() -> {
            testJpaPdpGroup.fromAuthorative(null);
        }).hasMessage("pdpGroup is marked @NonNull but is null");

        testJpaPdpGroup.setKey(new PfConceptKey(PDP_GROUP0, VERSION));
        testJpaPdpGroup.fromAuthorative(testPdpGroup);

        assertThatThrownBy(() -> {
            testJpaPdpGroup.copyTo(null);
        }).hasMessage("target is marked @NonNull but is null");

        assertEquals(PDP_GROUP0, testJpaPdpGroup.getKey().getName());
        assertEquals(PDP_GROUP0, new JpaPdpGroup(testPdpGroup).getKey().getName());
        assertEquals(PDP_GROUP0, ((PfConceptKey) new JpaPdpGroup(testPdpGroup).getKeys().get(0)).getName());

        testJpaPdpGroup.clean();
        assertEquals(PDP_GROUP0, testJpaPdpGroup.getKey().getName());

        assertThatThrownBy(() -> {
            testJpaPdpGroup.validate(null);
        }).hasMessage("resultIn is marked @NonNull but is null");

        assertFalse(testJpaPdpGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpGroup.setPdpGroupState(PdpState.PASSIVE);
        assertTrue(testJpaPdpGroup.validate(new PfValidationResult()).isOk());

        testJpaPdpGroup.setKey(PfConceptKey.getNullKey());
        assertFalse(testJpaPdpGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpGroup.setKey(new PfConceptKey("PdpGroup0", VERSION));
        assertTrue(testJpaPdpGroup.validate(new PfValidationResult()).isOk());

        testJpaPdpGroup.setDescription("   ");
        assertFalse(testJpaPdpGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpGroup.setDescription("  A Description ");
        assertTrue(testJpaPdpGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpGroup.setDescription(null);
        assertTrue(testJpaPdpGroup.validate(new PfValidationResult()).isOk());

        testJpaPdpGroup.setProperties(new LinkedHashMap<>());
        testJpaPdpGroup.getProperties().put(null, null);
        assertFalse(testJpaPdpGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpGroup.getProperties().remove(null);
        assertTrue(testJpaPdpGroup.validate(new PfValidationResult()).isOk());

        testJpaPdpGroup.setProperties(new LinkedHashMap<>());
        testJpaPdpGroup.getProperties().put("NullKey", null);
        assertFalse(testJpaPdpGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpGroup.getProperties().remove("NullKey");
        assertTrue(testJpaPdpGroup.validate(new PfValidationResult()).isOk());

        List<JpaPdpSubGroup> jpaPdpSubgroups = testJpaPdpGroup.getPdpSubGroups();
        assertNotNull(jpaPdpSubgroups);
        testJpaPdpGroup.setPdpSubGroups(null);
        assertFalse(testJpaPdpGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpGroup.setPdpSubGroups(new ArrayList<>());
        assertTrue(testJpaPdpGroup.validate(new PfValidationResult()).isOk());
        testJpaPdpGroup.setPdpSubGroups(jpaPdpSubgroups);
        assertTrue(testJpaPdpGroup.validate(new PfValidationResult()).isOk());

        JpaPdpGroup otherJpaPdpGroup = new JpaPdpGroup(testJpaPdpGroup);
        assertEquals(0, testJpaPdpGroup.compareTo(otherJpaPdpGroup));
        assertEquals(-1, testJpaPdpGroup.compareTo(null));
        assertEquals(0, testJpaPdpGroup.compareTo(testJpaPdpGroup));
        assertFalse(testJpaPdpGroup.compareTo(new DummyJpaPdpSubgroupChild()) == 0);

        testJpaPdpGroup.getKey().setName("OtherName");
        assertEquals(-1, testJpaPdpGroup.compareTo(otherJpaPdpGroup));
        testJpaPdpGroup.getKey().setName("PdpGroup0");
        assertEquals(0, testJpaPdpGroup.compareTo(otherJpaPdpGroup));

        JpaPdpSubGroup anotherPdpSubgroup =
                new JpaPdpSubGroup(new PfReferenceKey(testJpaPdpGroup.getKey(), "AnotherPdpSubgroup"));
        testJpaPdpGroup.getPdpSubGroups().add(anotherPdpSubgroup);
        assertNotEquals(0, testJpaPdpGroup.compareTo(otherJpaPdpGroup));
        testJpaPdpGroup.getPdpSubGroups().remove(anotherPdpSubgroup);
        assertEquals(0, testJpaPdpGroup.compareTo(otherJpaPdpGroup));

        testJpaPdpGroup.setPdpGroupState(PdpState.ACTIVE);
        assertNotEquals(0, testJpaPdpGroup.compareTo(otherJpaPdpGroup));
        testJpaPdpGroup.setPdpGroupState(PdpState.PASSIVE);
        assertEquals(0, testJpaPdpGroup.compareTo(otherJpaPdpGroup));

        testJpaPdpGroup.setDescription("A Description");
        assertNotEquals(0, testJpaPdpGroup.compareTo(otherJpaPdpGroup));
        testJpaPdpGroup.setDescription(null);
        assertEquals(0, testJpaPdpGroup.compareTo(otherJpaPdpGroup));

        testJpaPdpGroup.getProperties().put("AnotherProperty", "Some String");
        assertNotEquals(0, testJpaPdpGroup.compareTo(otherJpaPdpGroup));
        testJpaPdpGroup.getProperties().remove("AnotherProperty");
        assertEquals(0, testJpaPdpGroup.compareTo(otherJpaPdpGroup));

        PdpGroup psg = testJpaPdpGroup.toAuthorative();
        assertEquals(0, psg.getProperties().size());

        testJpaPdpGroup.setProperties(new LinkedHashMap<>());
        psg = testJpaPdpGroup.toAuthorative();
        assertEquals(0, psg.getProperties().size());

        testJpaPdpGroup.setProperties(null);
        psg = testJpaPdpGroup.toAuthorative();
        assertNull(psg.getProperties());
        testJpaPdpGroup.setProperties(new LinkedHashMap<>());

        testJpaPdpGroup.clean();
        testJpaPdpGroup.getProperties().put(" PropKey ", " Prop Value ");
        testJpaPdpGroup.clean();
        assertEquals("PropKey", testJpaPdpGroup.getProperties().keySet().iterator().next());
        assertEquals("Prop Value", testJpaPdpGroup.getProperties().get("PropKey"));
        testJpaPdpGroup.setDescription(" A Description ");
        testJpaPdpGroup.clean();
        assertEquals("A Description", testJpaPdpGroup.getDescription());

        assertEquals(1, testJpaPdpGroup.getKeys().size());
        testJpaPdpGroup.getPdpSubGroups().add(anotherPdpSubgroup);
        assertEquals(2, testJpaPdpGroup.getKeys().size());
        testJpaPdpGroup.clean();
        assertEquals(2, testJpaPdpGroup.getKeys().size());
    }
}
