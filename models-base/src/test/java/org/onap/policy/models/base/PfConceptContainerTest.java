/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2023, 2024 Nordix Foundation.
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

package org.onap.policy.models.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.onap.policy.models.base.testconcepts.DummyAuthorativeConcept;
import org.onap.policy.models.base.testconcepts.DummyBadPfConceptContainer;
import org.onap.policy.models.base.testconcepts.DummyPfConcept;
import org.onap.policy.models.base.testconcepts.DummyPfConceptContainer;
import org.onap.policy.models.base.testconcepts.DummyPfConceptSub;

class PfConceptContainerTest {

    private static final String NAME0 = "name0";
    private static final String NAME1 = "name1";
    private static final String NAME2 = "name2";
    private static final String NAME3 = "name3";
    private static final String ID3 = "name3:0.0.1";
    private static final String VERSION0_0_1 = "0.0.1";
    private static final String KEY_IS_NULL = "^key is marked .*on.*ull but is null$";
    private static final String DUMMY_VALUE = "Dummy";

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void testConceptContainer() {
        DummyPfConceptContainer container = new DummyPfConceptContainer();
        assertNotNull(container);

        container = new DummyPfConceptContainer();
        assertNotNull(container);

        container = new DummyPfConceptContainer(new PfConceptKey());
        assertNotNull(container);

        container = new DummyPfConceptContainer(new PfConceptKey(), new TreeMap<>());
        assertNotNull(container);

        assertThatThrownBy(() -> new PfConceptContainer(null, null)).hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new DummyPfConceptContainer(null, null))
            .hasMessageMatching(KEY_IS_NULL);

        assertThatThrownBy(() -> new DummyPfConceptContainer(new PfConceptKey(), null))
            .hasMessageMatching("^conceptMap is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new DummyPfConceptContainer(null, new TreeMap<>()))
            .hasMessageMatching(KEY_IS_NULL);
    }

    @Test
    void testNamedConceptContainer() {
        DummyPfConceptContainer container = new DummyPfConceptContainer();
        container.getKey().setName(DUMMY_VALUE);
        DummyPfConceptContainer clonedContainer = new DummyPfConceptContainer(container);
        assertNotNull(clonedContainer);
        assertEquals(DUMMY_VALUE, clonedContainer.getKey().getName());

        assertThatThrownBy(() -> new DummyPfConceptContainer((DummyPfConceptContainer) null))
            .hasMessageMatching("^copyConcept is marked .*on.*ull but is null$");

        List<PfKey> keyList = container.getKeys();
        assertEquals(1, keyList.size());

        PfConceptKey conceptKey = new PfConceptKey("Key", VERSION0_0_1);
        Map<PfConceptKey, DummyPfConcept> conceptMap = new TreeMap<>();
        conceptMap.put(conceptKey, new DummyPfConcept(conceptKey));

        container.setConceptMap(conceptMap);
        keyList = container.getKeys();
        assertEquals(2, keyList.size());

        clonedContainer = new DummyPfConceptContainer(container);
        assertNotNull(clonedContainer);
        assertEquals(DUMMY_VALUE, clonedContainer.getKey().getName());
        assertEquals(2, clonedContainer.getKeys().size());

        assertEquals(clonedContainer, container);
        container.clean();
        assertEquals(clonedContainer, container);

        assertThat(container.validate("").getResult()).isNull();

        assertEquals(0, container.compareTo(clonedContainer));

        assertThatThrownBy(() -> new DummyPfConceptContainer((DummyPfConceptContainer) null))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> container.compareTo(null)).isInstanceOf(NullPointerException.class);

        assertEquals(0, container.compareTo(container));
        assertNotEquals(0, container.compareTo(conceptKey));

        DummyPfConceptContainer testContainer = new DummyPfConceptContainer(container);
        testContainer.getKey().setVersion("0.0.2");
        assertNotEquals(0, container.compareTo(testContainer));
        testContainer.getKey().setVersion(container.getKey().getVersion());
        assertEquals(0, container.compareTo(testContainer));

        PfConceptKey testConceptKey = new PfConceptKey("TestKey", VERSION0_0_1);
        testContainer.getConceptMap().put(testConceptKey, new DummyPfConcept(testConceptKey));
        assertNotEquals(0, container.compareTo(testContainer));
    }

    @Test
    void testValidationContainer() {
        DummyPfConceptContainer container = new DummyPfConceptContainer();
        PfConceptKey conceptKey = new PfConceptKey("Key", VERSION0_0_1);
        Map<PfConceptKey, DummyPfConcept> conceptMap = new TreeMap<>();
        conceptMap.put(conceptKey, new DummyPfConcept(conceptKey));
        container.setConceptMap(conceptMap);

        final DummyPfConceptContainer container3 = container;
        assertThatThrownBy(() -> container3.validate(null))
            .hasMessageMatching("^fieldName is marked .*on.*ull but is null$");

        DummyPfConceptContainer validateContainer = new DummyPfConceptContainer();
        assertFalse(validateContainer.validate("").isValid());
        validateContainer.setKey(new PfConceptKey("VCKey", VERSION0_0_1));
        assertTrue(validateContainer.validate("").isValid());

        PfConceptKey testConceptKey = new PfConceptKey("TestKey", VERSION0_0_1);
        validateContainer.getConceptMap().put(testConceptKey, new DummyPfConcept(testConceptKey));
        assertTrue(validateContainer.validate("").isValid());

        validateContainer.getConceptMap().put(PfConceptKey.getNullKey(), new DummyPfConcept(PfConceptKey.getNullKey()));
        assertFalse(validateContainer.validate("").isValid());
        validateContainer.getConceptMap().remove(PfConceptKey.getNullKey());
        assertTrue(validateContainer.validate("").isValid());

        validateContainer.getConceptMap().put(testConceptKey, null);
        assertFalse(validateContainer.validate("").isValid());
        validateContainer.getConceptMap().put(testConceptKey, new DummyPfConcept(testConceptKey));
        assertTrue(validateContainer.validate("").isValid());

        validateContainer.getConceptMap().put(testConceptKey, new DummyPfConcept(conceptKey));
        assertFalse(validateContainer.validate("").isValid());
        validateContainer.getConceptMap().put(testConceptKey, new DummyPfConcept(testConceptKey));
        assertTrue(validateContainer.validate("").isValid());
    }

    @Test
    void testSetContainer() {
        DummyPfConceptContainer container = new DummyPfConceptContainer();
        PfConceptKey conceptKey = new PfConceptKey("Key", VERSION0_0_1);
        Map<PfConceptKey, DummyPfConcept> conceptMap = new TreeMap<>();
        conceptMap.put(conceptKey, new DummyPfConcept(conceptKey));
        container.setConceptMap(conceptMap);

        assertEquals(conceptKey, container.get(conceptKey).getKey());
        assertEquals(conceptKey, container.get(conceptKey.getName()).getKey());
        assertEquals(conceptKey, container.get(conceptKey.getName(), conceptKey.getVersion()).getKey());

        Set<DummyPfConcept> returnSet = container.getAll(conceptKey.getName());
        assertEquals(conceptKey, returnSet.iterator().next().getKey());

        returnSet = container.getAll(conceptKey.getName(), conceptKey.getVersion());
        assertEquals(conceptKey, returnSet.iterator().next().getKey());

        returnSet = container.getAllNamesAndVersions(conceptKey.getName(), conceptKey.getVersion());
        assertEquals(conceptKey, returnSet.iterator().next().getKey());
        returnSet = container.getAllNamesAndVersions(null, conceptKey.getVersion());
        assertEquals(conceptKey, returnSet.iterator().next().getKey());
        returnSet = container.getAllNamesAndVersions(null, null);
        assertEquals(conceptKey, returnSet.iterator().next().getKey());
        returnSet = container.getAllNamesAndVersions(conceptKey.getName(), null);
        assertEquals(conceptKey, returnSet.iterator().next().getKey());
        returnSet = container.getAllNamesAndVersions(conceptKey.getName(), "0.0.0");
        assertEquals(conceptKey, returnSet.iterator().next().getKey());
        returnSet = container.getAllNamesAndVersions("IDontExist", "1.0.0");
        assertTrue(returnSet.isEmpty());

        container.getConceptMap().put(conceptKey, new DummyPfConceptSub(conceptKey));

        PfConceptKey anotherKey = new PfConceptKey(conceptKey);
        assertEquals(conceptKey, container.get(anotherKey).getKey());
        anotherKey.setVersion(PfKey.NULL_KEY_VERSION);
        assertEquals(conceptKey, container.get(anotherKey).getKey());
    }


    @Test
    void testAuthorative() {
        Map<String, DummyAuthorativeConcept> dacMap = new LinkedHashMap<>();
        dacMap.put(NAME0, new DummyAuthorativeConcept(NAME0, "1.2.3", "Hello"));
        dacMap.put(NAME1, new DummyAuthorativeConcept("IncorrectName", PfKey.NULL_KEY_VERSION, "Hi"));
        dacMap.put(NAME2, new DummyAuthorativeConcept(NAME2, "1.2.3", "Howdy"));
        dacMap.put(ID3, new DummyAuthorativeConcept(NAME3, "9.9.9", "Ciao"));
        dacMap.put("name4:1.2.3", new DummyAuthorativeConcept(null, null, "Slan"));
        dacMap.put("name5", new DummyAuthorativeConcept(null, null, "Bye"));

        List<Map<String, DummyAuthorativeConcept>> authorativeList = new ArrayList<>();
        authorativeList.add(dacMap);

        DummyPfConceptContainer container = new DummyPfConceptContainer();

        assertThatThrownBy(() -> container.fromAuthorative(authorativeList))
            .hasMessage("Key name1:0.0.0 field name1 does not match the value IncorrectName in the concept field");

        dacMap.put(NAME1, new DummyAuthorativeConcept(NAME1, PfKey.NULL_KEY_VERSION, "Hi"));

        assertThatThrownBy(() -> container.fromAuthorative(authorativeList))
            .hasMessage("Key name3:0.0.1 field 0.0.1 does not match the value 9.9.9 in the concept field");

        dacMap.put(ID3, new DummyAuthorativeConcept(NAME3, "0.0.1", "Ciao"));

        container.fromAuthorative(authorativeList);

        assertEquals("Hello", container.getConceptMap().get(new PfConceptKey("name0:1.2.3")).getDescription());
        assertEquals("Hi", container.getConceptMap().get(new PfConceptKey("name1:0.0.0")).getDescription());
        assertEquals("Howdy", container.getConceptMap().get(new PfConceptKey("name2:1.2.3")).getDescription());
        assertEquals("Ciao", container.getConceptMap().get(new PfConceptKey("name3:0.0.1")).getDescription());
        assertEquals("name4", container.getConceptMap().get(new PfConceptKey("name4:1.2.3")).getName());
        assertEquals("1.2.3", container.getConceptMap().get(new PfConceptKey("name4:1.2.3")).getVersion());
        assertEquals("0.0.0", container.getConceptMap().get(new PfConceptKey("name5:0.0.0")).getVersion());

        List<Map<String, DummyAuthorativeConcept>> outMapList = container.toAuthorative();

        assertEquals(dacMap.get(NAME0), outMapList.get(0).get(NAME0));
        assertEquals(dacMap.get(NAME1).getDescription(), outMapList.get(1).get(NAME1).getDescription());
        assertEquals(dacMap.get(NAME2), outMapList.get(2).get(NAME2));
        assertEquals(dacMap.get(NAME3), outMapList.get(2).get(NAME3));

        List<DummyAuthorativeConcept> outConceptList = container.toAuthorativeList();
        assertEquals("Hello", outConceptList.get(0).getDescription());
        assertEquals("Hi", outConceptList.get(1).getDescription());
        assertEquals("Howdy", outConceptList.get(2).getDescription());
        assertEquals("Ciao", outConceptList.get(3).getDescription());
        assertEquals("name4", outConceptList.get(4).getName());
        assertEquals("1.2.3", outConceptList.get(4).getVersion());
        assertEquals("0.0.0", outConceptList.get(5).getVersion());

        DummyBadPfConceptContainer badContainer = new DummyBadPfConceptContainer();
        assertThatThrownBy(() -> badContainer.fromAuthorative(authorativeList))
            .hasMessage("failed to instantiate instance of container concept class");

        authorativeList.clear();
        assertThatThrownBy(() -> container.fromAuthorative(authorativeList))
            .hasMessage("An incoming list of concepts must have at least one entry");
    }

    @Test
    void testNullKey() {
        assertThrows(NullPointerException.class,
            () -> {
                PfConceptKey nullKey = null;
                new DummyPfConceptContainer(nullKey);
            });
    }
}
