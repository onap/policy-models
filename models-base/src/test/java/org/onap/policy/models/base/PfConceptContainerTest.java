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

package org.onap.policy.models.base;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.Test;
import org.onap.policy.models.base.testconcepts.DummyPfConcept;
import org.onap.policy.models.base.testconcepts.DummyPfConceptContainer;
import org.onap.policy.models.base.testconcepts.DummyPfConceptSub;

/**
 * Test the PfCOnceptCOntainer class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PfConceptContainerTest {

    @Test
    public void test() {
        DummyPfConceptContainer container = new DummyPfConceptContainer();
        assertNotNull(container);

        container = new DummyPfConceptContainer();
        assertNotNull(container);

        container = new DummyPfConceptContainer(new PfConceptKey());
        assertNotNull(container);

        container = new DummyPfConceptContainer(new PfConceptKey(), new TreeMap<PfConceptKey, DummyPfConcept>());
        assertNotNull(container);

        try {
            container = new DummyPfConceptContainer((PfConceptKey) null, null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        try {
            container = new DummyPfConceptContainer(new PfConceptKey(), null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("conceptMap is marked @NonNull but is null", exc.getMessage());
        }

        try {
            container = new DummyPfConceptContainer(null, new TreeMap<PfConceptKey, DummyPfConcept>());
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("key is marked @NonNull but is null", exc.getMessage());
        }

        container.getKey().setName("Dummy");
        DummyPfConceptContainer clonedContainer = new DummyPfConceptContainer(container);
        assertNotNull(clonedContainer);
        assertEquals("Dummy", clonedContainer.getKey().getName());

        try {
            DummyPfConceptContainer conceptContainter = null;
            container = new DummyPfConceptContainer(conceptContainter);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        List<PfKey> keyList = container.getKeys();
        assertEquals(1, keyList.size());

        PfConceptKey conceptKey = new PfConceptKey("Key", "0.0.1");
        Map<PfConceptKey, DummyPfConcept> conceptMap = new TreeMap<>();
        conceptMap.put(conceptKey, new DummyPfConcept(conceptKey));

        container.setConceptMap(conceptMap);
        keyList = container.getKeys();
        assertEquals(2, keyList.size());

        clonedContainer = new DummyPfConceptContainer(container);
        assertNotNull(clonedContainer);
        assertEquals("Dummy", clonedContainer.getKey().getName());
        assertEquals(2, clonedContainer.getKeys().size());

        assertEquals(clonedContainer, container);
        container.clean();
        assertEquals(clonedContainer, container);

        PfValidationResult result = new PfValidationResult();
        result = container.validate(result);
        assertTrue(result.isOk());

        assertEquals(0, container.compareTo(clonedContainer));

        try {
            container.copyTo(null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("target is marked @NonNull but is null", exc.getMessage());
        }

        assertFalse(container.compareTo(null) == 0);
        assertEquals(0, container.compareTo(container));
        assertFalse(container.compareTo(conceptKey) == 0);

        DummyPfConceptContainer testContainer = new DummyPfConceptContainer(container);
        testContainer.getKey().setVersion("0.0.2");
        assertFalse(container.compareTo(testContainer) == 0);
        testContainer.getKey().setVersion(container.getKey().getVersion());
        assertEquals(0, container.compareTo(testContainer));

        PfConceptKey testConceptKey = new PfConceptKey("TestKey", "0.0.1");
        testContainer.getConceptMap().put(testConceptKey, new DummyPfConcept(testConceptKey));
        assertFalse(container.compareTo(testContainer) == 0);

        try {
            container.validate(null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("resultIn is marked @NonNull but is null", exc.getMessage());
        }

        DummyPfConceptContainer validateContainer = new DummyPfConceptContainer();
        assertFalse(validateContainer.validate(new PfValidationResult()).isOk());
        validateContainer.setKey(new PfConceptKey("VCKey", "0.0.1"));
        assertFalse(validateContainer.validate(new PfValidationResult()).isOk());

        validateContainer.getConceptMap().put(testConceptKey, new DummyPfConcept(testConceptKey));
        assertTrue(validateContainer.validate(new PfValidationResult()).isOk());

        validateContainer.getConceptMap().put(PfConceptKey.getNullKey(), new DummyPfConcept(PfConceptKey.getNullKey()));
        assertFalse(validateContainer.validate(new PfValidationResult()).isOk());
        validateContainer.getConceptMap().remove(PfConceptKey.getNullKey());
        assertTrue(validateContainer.validate(new PfValidationResult()).isOk());

        validateContainer.getConceptMap().put(testConceptKey, null);
        assertFalse(validateContainer.validate(new PfValidationResult()).isOk());
        validateContainer.getConceptMap().put(testConceptKey, new DummyPfConcept(testConceptKey));
        assertTrue(validateContainer.validate(new PfValidationResult()).isOk());

        validateContainer.getConceptMap().put(testConceptKey, new DummyPfConcept(conceptKey));
        assertFalse(validateContainer.validate(new PfValidationResult()).isOk());
        validateContainer.getConceptMap().put(testConceptKey, new DummyPfConcept(testConceptKey));
        assertTrue(validateContainer.validate(new PfValidationResult()).isOk());

        assertEquals(conceptKey, container.get(conceptKey).getKey());
        assertEquals(conceptKey, container.get(conceptKey.getName()).getKey());
        assertEquals(conceptKey, container.get(conceptKey.getName(), conceptKey.getVersion()).getKey());

        Set<DummyPfConcept> returnSet = container.getAll(conceptKey.getName());
        assertEquals(conceptKey, returnSet.iterator().next().getKey());

        returnSet = container.getAll(conceptKey.getName(), conceptKey.getVersion());
        assertEquals(conceptKey, returnSet.iterator().next().getKey());

        container.getConceptMap().put(conceptKey, new DummyPfConceptSub(conceptKey));

        DummyPfConceptContainer exceptionOnCopyContainer = new DummyPfConceptContainer();
        try {
            container.copyTo(exceptionOnCopyContainer);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals(
                    "Failed to create a clone of class \"org.onap.policy.models.base.testconcepts.DummyPfConceptSub\"",
                    exc.getMessage());
        }
    }

    @Test(expected = NullPointerException.class)
    public void testnullKey() {
        PfConceptKey nullKey = null;
        new DummyPfConceptContainer(nullKey);
    }
}
