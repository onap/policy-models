/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2024 Nordix Foundation.
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

package org.onap.policy.models.pdp.persistence.concepts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.pdp.testconcepts.DummyJpaPdpSubgroupChild;

/**
 * Test the {@link JpaPdpGroupSubGroup} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class JpaPdpGroupTest {

    private static final String NULL_ERROR = " is marked .*ull but is null";
    private static final String NULL_KEY_ERROR = "key" + NULL_ERROR;
    private static final String PDP_GROUP0 = "PDPGroup0";
    private static final String VERSION = "1.0.0";

    @Test
    void testJpaPdpGroup() {
        assertThatThrownBy(() -> {
            new JpaPdpGroup((JpaPdpGroup) null);
        }).hasMessageMatching("copyConcept" + NULL_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup((PfConceptKey) null);
        }).hasMessageMatching(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup((PdpGroup) null);
        }).hasMessageMatching("authorativeConcept" + NULL_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup((JpaPdpGroup) null);
        }).hasMessageMatching("copyConcept" + NULL_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup(null, null, null);
        }).hasMessageMatching(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup(new PfConceptKey(), null, null);
        }).hasMessageMatching("pdpGroupState" + NULL_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup(new PfConceptKey(), PdpState.PASSIVE, null);
        }).hasMessageMatching("pdpSubGroups" + NULL_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup(null, PdpState.PASSIVE, null);
        }).hasMessageMatching(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup(null, PdpState.PASSIVE, new ArrayList<>());
        }).hasMessageMatching(NULL_KEY_ERROR);

        assertThatThrownBy(() -> {
            new JpaPdpGroup(null, null, new ArrayList<>());
        }).hasMessageMatching(NULL_KEY_ERROR);

        assertNotNull(new JpaPdpGroup((new PfConceptKey())));
        assertNotNull(new JpaPdpGroup((new JpaPdpGroup())));
    }

    @Test
    void testPdpGroupSet() {
        PdpGroup testPdpGroup = new PdpGroup();
        testPdpGroup.setName(PDP_GROUP0);
        testPdpGroup.setPdpSubgroups(new ArrayList<>());

        JpaPdpGroup testJpaPdpGroup = setUpSmallJpaPdpGroup();

        assertEquals(PDP_GROUP0, testJpaPdpGroup.getKey().getName());

        assertThatThrownBy(() -> {
            testJpaPdpGroup.fromAuthorative(null);
        }).hasMessageMatching("pdpGroup" + NULL_ERROR);

        testJpaPdpGroup.setKey(new PfConceptKey(PDP_GROUP0, VERSION));
        testJpaPdpGroup.fromAuthorative(testPdpGroup);

        assertThatThrownBy(() -> new JpaPdpGroup((JpaPdpGroup) null)).isInstanceOf(NullPointerException.class);

        assertEquals(PDP_GROUP0, testJpaPdpGroup.getKey().getName());
        assertEquals(PDP_GROUP0, new JpaPdpGroup(testPdpGroup).getKey().getName());
        assertEquals(PDP_GROUP0, ((PfConceptKey) new JpaPdpGroup(testPdpGroup).getKeys().get(0)).getName());

        testJpaPdpGroup.clean();
        assertEquals(PDP_GROUP0, testJpaPdpGroup.getKey().getName());
    }

    @Test
    void testPdpGroupValidation() {
        JpaPdpGroup testJpaPdpGroup = setUpSmallJpaPdpGroup();

        assertThatThrownBy(() -> {
            testJpaPdpGroup.validate(null);
        }).hasMessageMatching("fieldName" + NULL_ERROR);

        assertFalse(testJpaPdpGroup.validate("").isValid());
        testJpaPdpGroup.setPdpGroupState(PdpState.PASSIVE);
        assertTrue(testJpaPdpGroup.validate("").isValid());

        testJpaPdpGroup.setKey(PfConceptKey.getNullKey());
        assertFalse(testJpaPdpGroup.validate("").isValid());
        testJpaPdpGroup.setKey(new PfConceptKey("PdpGroup0", VERSION));
        assertTrue(testJpaPdpGroup.validate("").isValid());

        testJpaPdpGroup.setDescription("   ");
        assertFalse(testJpaPdpGroup.validate("").isValid());
        testJpaPdpGroup.setDescription("  A Description ");
        assertTrue(testJpaPdpGroup.validate("").isValid());
        testJpaPdpGroup.setDescription(null);
        assertTrue(testJpaPdpGroup.validate("").isValid());

        testJpaPdpGroup.setProperties(new LinkedHashMap<>());
        testJpaPdpGroup.getProperties().put(null, null);
        assertFalse(testJpaPdpGroup.validate("").isValid());
        testJpaPdpGroup.getProperties().remove(null);
        assertTrue(testJpaPdpGroup.validate("").isValid());

        testJpaPdpGroup.setProperties(new LinkedHashMap<>());
        testJpaPdpGroup.getProperties().put("NullKey", null);
        assertFalse(testJpaPdpGroup.validate("").isValid());
        testJpaPdpGroup.getProperties().remove("NullKey");
        assertTrue(testJpaPdpGroup.validate("").isValid());
    }

    @Test
    void testPdpSubgroups() {
        JpaPdpGroup testJpaPdpGroup = setUpJpaPdpGroup();

        List<JpaPdpSubGroup> jpaPdpSubgroups = testJpaPdpGroup.getPdpSubGroups();
        assertNotNull(jpaPdpSubgroups);
        testJpaPdpGroup.setPdpSubGroups(null);
        assertFalse(testJpaPdpGroup.validate("").isValid());
        testJpaPdpGroup.setPdpSubGroups(new ArrayList<>());
        assertTrue(testJpaPdpGroup.validate("").isValid());
        testJpaPdpGroup.setPdpSubGroups(jpaPdpSubgroups);
        assertTrue(testJpaPdpGroup.validate("").isValid());

        JpaPdpGroup otherJpaPdpGroup = new JpaPdpGroup(testJpaPdpGroup);
        assertEquals(0, testJpaPdpGroup.compareTo(otherJpaPdpGroup));
        assertEquals(-1, testJpaPdpGroup.compareTo(null));
        assertEquals(0, testJpaPdpGroup.compareTo(testJpaPdpGroup));
        assertNotEquals(0, testJpaPdpGroup.compareTo(new DummyJpaPdpSubgroupChild()));

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
    }

    @Test
    void testPdpGroupsProperties() {
        JpaPdpGroup testJpaPdpGroup = setUpJpaPdpGroup();

        testJpaPdpGroup.getProperties().put(" PropKey ", " Prop Value ");
        testJpaPdpGroup.clean();
        assertEquals("PropKey", testJpaPdpGroup.getProperties().keySet().iterator().next());
        assertEquals("Prop Value", testJpaPdpGroup.getProperties().get("PropKey"));
        testJpaPdpGroup.setDescription(" A Description ");
        testJpaPdpGroup.clean();
        assertEquals("A Description", testJpaPdpGroup.getDescription());

        JpaPdpSubGroup anotherPdpSubgroup =
                new JpaPdpSubGroup(new PfReferenceKey(testJpaPdpGroup.getKey(), "AnotherPdpSubgroup"));

        assertEquals(1, testJpaPdpGroup.getKeys().size());
        testJpaPdpGroup.getPdpSubGroups().add(anotherPdpSubgroup);
        assertEquals(2, testJpaPdpGroup.getKeys().size());
        testJpaPdpGroup.clean();
        assertEquals(2, testJpaPdpGroup.getKeys().size());

        assertEquals(testJpaPdpGroup, new JpaPdpGroup(testJpaPdpGroup));
    }

    private JpaPdpGroup setUpSmallJpaPdpGroup() {
        PdpGroup testPdpGroup = new PdpGroup();
        testPdpGroup.setName(PDP_GROUP0);
        testPdpGroup.setPdpSubgroups(new ArrayList<>());
        testPdpGroup.setVersion(VERSION);

        JpaPdpGroup testJpaPdpGroup = new JpaPdpGroup();
        testJpaPdpGroup.setKey(new PfConceptKey(PDP_GROUP0, VERSION));
        testJpaPdpGroup.fromAuthorative(testPdpGroup);
        testJpaPdpGroup.clean();

        return testJpaPdpGroup;
    }

    private JpaPdpGroup setUpJpaPdpGroup() {
        JpaPdpGroup testJpaPdpGroup = setUpSmallJpaPdpGroup();

        testJpaPdpGroup.setKey(new PfConceptKey("PdpGroup0", VERSION));
        testJpaPdpGroup.setDescription(null);
        testJpaPdpGroup.setPdpGroupState(PdpState.PASSIVE);
        testJpaPdpGroup.setProperties(new LinkedHashMap<>());
        testJpaPdpGroup.clean();

        return testJpaPdpGroup;
    }
}
