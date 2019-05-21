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

import java.lang.reflect.Field;

import org.junit.Test;

import org.onap.policy.models.base.PfKey.Compatibility;
import org.onap.policy.models.base.testconcepts.DummyPfConcept;
import org.onap.policy.models.base.testconcepts.DummyPfKey;

public class PfKeyTest {

    @Test
    public void testConceptKey() {
        try {
            new PfConceptKey("some bad key id");
            fail("This test should throw an exception");
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "parameter \"id\": value \"some bad key id\", "
                            + "does not match regular expression \"[A-Za-z0-9\\-_\\.]+:[0-9].[0-9].[0-9]\"",
                    e.getMessage());
        }

        try {
            new PfConceptKey((PfConceptKey) null);
            fail("This test should throw an exception");
        } catch (Exception e) {
            assertEquals("copyConcept is marked @NonNull but is null", e.getMessage());
        }

        PfConceptKey someKey0 = new PfConceptKey();
        assertEquals(PfConceptKey.getNullKey(), someKey0);

        PfConceptKey someKey1 = new PfConceptKey("name", "0.0.1");
        PfConceptKey someKey2 = new PfConceptKey(someKey1);
        PfConceptKey someKey3 = new PfConceptKey(someKey1.getId());
        assertEquals(someKey1, someKey2);
        assertEquals(someKey1, someKey3);
        assertFalse(someKey1.isNullVersion());

        assertEquals(someKey2, someKey1.getKey());
        assertEquals(1, someKey1.getKeys().size());

        someKey0.setName("zero");
        someKey0.setVersion("0.0.2");

        someKey3.setVersion("0.0.2");

        PfConceptKey someKey4 = new PfConceptKey(someKey1);
        someKey4.setVersion("0.1.2");

        PfConceptKey someKey4a = new PfConceptKey(someKey1);
        someKey4a.setVersion("0");

        PfConceptKey someKey5 = new PfConceptKey(someKey1);
        someKey5.setVersion("1.2.2");

        PfConceptKey someKey6 = new PfConceptKey(someKey1);
        someKey6.setVersion("3");

        assertEquals("name:0.1.2", someKey4.getId());

        PfConcept pfc = new DummyPfConcept();
        assertEquals(PfConceptKey.getNullKey().getId(), pfc.getId());

        assertTrue(PfConceptKey.getNullKey().matchesId(pfc.getId()));

        assertTrue(PfConceptKey.getNullKey().isNullKey());

        try {
            PfConceptKey.getNullKey().matchesId(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("id is marked @NonNull but is null", exc.getMessage());
        }

        assertEquals(Compatibility.DIFFERENT, someKey0.getCompatibility(new DummyPfKey()));
        assertEquals(Compatibility.DIFFERENT, someKey0.getCompatibility(someKey1));
        assertEquals(Compatibility.IDENTICAL, someKey2.getCompatibility(someKey1));
        assertEquals(Compatibility.PATCH, someKey3.getCompatibility(someKey1));
        assertEquals(Compatibility.MINOR, someKey4.getCompatibility(someKey1));
        assertEquals(Compatibility.PATCH, someKey4a.getCompatibility(someKey1));
        assertEquals(Compatibility.PATCH, someKey1.getCompatibility(someKey4a));
        assertEquals(Compatibility.MAJOR, someKey5.getCompatibility(someKey1));
        assertEquals(Compatibility.MAJOR, someKey6.getCompatibility(someKey1));

        assertTrue(someKey1.isCompatible(someKey2));
        assertTrue(someKey1.isCompatible(someKey3));
        assertTrue(someKey1.isCompatible(someKey4));
        assertFalse(someKey1.isCompatible(someKey0));
        assertFalse(someKey1.isCompatible(someKey5));
        assertFalse(someKey1.isCompatible(new DummyPfKey()));

        assertEquals(PfValidationResult.ValidationResult.VALID,
                someKey0.validate(new PfValidationResult()).getValidationResult());
        assertEquals(PfValidationResult.ValidationResult.VALID,
                someKey1.validate(new PfValidationResult()).getValidationResult());
        assertEquals(PfValidationResult.ValidationResult.VALID,
                someKey2.validate(new PfValidationResult()).getValidationResult());
        assertEquals(PfValidationResult.ValidationResult.VALID,
                someKey3.validate(new PfValidationResult()).getValidationResult());
        assertEquals(PfValidationResult.ValidationResult.VALID,
                someKey4.validate(new PfValidationResult()).getValidationResult());
        assertEquals(PfValidationResult.ValidationResult.VALID,
                someKey5.validate(new PfValidationResult()).getValidationResult());
        assertEquals(PfValidationResult.ValidationResult.VALID,
                someKey6.validate(new PfValidationResult()).getValidationResult());

        someKey0.clean();
        assertNotNull(someKey0.toString());

        PfConceptKey someKey7 = new PfConceptKey(someKey1);
        assertEquals(244799191, someKey7.hashCode());
        assertEquals(0, someKey7.compareTo(someKey1));
        assertEquals(-12, someKey7.compareTo(someKey0));

        try {
            someKey0.compareTo(null);
            fail("test should throw an exception here");
        } catch (NullPointerException e) {
            assertEquals("otherObj is marked @NonNull but is null", e.getMessage());
        }

        assertEquals(0, someKey0.compareTo(someKey0));
        assertEquals(266127751, someKey0.compareTo(new DummyPfKey()));

        assertFalse(someKey0.equals(null));
        assertTrue(someKey0.equals(someKey0));
        assertFalse(((PfKey) someKey0).equals(new DummyPfKey()));
    }

    @Test
    public void testNullArguments() {
        try {
            new PfConceptKey((String) null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("id is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new PfConceptKey((PfConceptKey) null);
            fail("id is marked @NonNull but is null");
        } catch (Exception exc) {
            assertEquals("copyConcept is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new PfConceptKey(null, null);
            fail("id is marked @NonNull but is null");
        } catch (Exception exc) {
            assertEquals("name is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new PfConceptKey("name", null);
            fail("id is marked @NonNull but is null");
        } catch (Exception exc) {
            assertEquals("version is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new PfConceptKey(null, "0.0.1");
            fail("id is marked @NonNull but is null");
        } catch (Exception exc) {
            assertEquals("name is marked @NonNull but is null", exc.getMessage());
        }

        try {
            PfConceptKey key = new PfConceptKey("AKey", "0.0.1");
            key.isCompatible(null);
            fail("id is marked @NonNull but is null");
        } catch (Exception exc) {
            assertEquals("otherKey is marked @NonNull but is null", exc.getMessage());
        }
    }

    @Test
    public void testValidation() {
        PfConceptKey testKey = new PfConceptKey("TheKey", "0.0.1");
        assertEquals("TheKey:0.0.1", testKey.getId());

        try {
            Field nameField = testKey.getClass().getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(testKey, "Key Name");
            PfValidationResult validationResult = new PfValidationResult();
            testKey.validate(validationResult);
            nameField.set(testKey, "TheKey");
            nameField.setAccessible(false);
            assertEquals(
                    "name invalid-parameter name with value Key Name "
                            + "does not match regular expression [A-Za-z0-9\\-_\\.]+",
                    validationResult.getMessageList().get(0).getMessage());
        } catch (Exception validationException) {
            fail("test should not throw an exception");
        }

        try {
            Field versionField = testKey.getClass().getDeclaredField("version");
            versionField.setAccessible(true);
            versionField.set(testKey, "Key Version");
            PfValidationResult validationResult = new PfValidationResult();
            testKey.validate(validationResult);
            versionField.set(testKey, "0.0.1");
            versionField.setAccessible(false);
            assertEquals(
                    "version invalid-parameter version with value Key Version "
                            + "does not match regular expression [0-9.]+",
                    validationResult.getMessageList().get(0).getMessage());
        } catch (Exception validationException) {
            fail("test should not throw an exception");
        }
    }

    @Test
    public void testkeynewerThan() {
        PfConceptKey key1 = new PfConceptKey("Key1", "1.2.3");

        try {
            key1.isNewerThan(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("otherKey is marked @NonNull but is null", exc.getMessage());
        }

        try {
            key1.isNewerThan(new PfReferenceKey());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("org.onap.policy.models.base.PfReferenceKey is not "
                    + "an instance of org.onap.policy.models.base.PfConceptKey", exc.getMessage());
        }

        assertFalse(key1.isNewerThan(key1));

        PfConceptKey key1a = new PfConceptKey("Key1a", "1.2.3");
        assertFalse(key1.isNewerThan(key1a));

        PfConceptKey key1b = new PfConceptKey("Key0", "1.2.3");
        assertTrue(key1.isNewerThan(key1b));

        key1a.setName("Key1");
        assertFalse(key1.isNewerThan(key1a));

        key1a.setVersion("0.2.3");
        assertTrue(key1.isNewerThan(key1a));
        key1a.setVersion("2.2.3");
        assertFalse(key1.isNewerThan(key1a));
        key1a.setVersion("1.2.3");
        assertFalse(key1.isNewerThan(key1a));

        key1a.setVersion("1.1.3");
        assertTrue(key1.isNewerThan(key1a));
        key1a.setVersion("1.3.3");
        assertFalse(key1.isNewerThan(key1a));
        key1a.setVersion("1.2.3");
        assertFalse(key1.isNewerThan(key1a));

        key1a.setVersion("1.2.2");
        assertTrue(key1.isNewerThan(key1a));
        key1a.setVersion("1.2.4");
        assertFalse(key1.isNewerThan(key1a));
        key1a.setVersion("1.2.3");
        assertFalse(key1.isNewerThan(key1a));

        key1.setVersion("1");
        assertFalse(key1.isNewerThan(key1a));
        key1a.setVersion("1");
        assertFalse(key1.isNewerThan(key1a));

        PfReferenceKey refKey = new PfReferenceKey();

        try {
            refKey.isNewerThan(null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("otherKey is marked @NonNull but is null", exc.getMessage());
        }

        try {
            refKey.isNewerThan(new PfConceptKey());
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("org.onap.policy.models.base.PfConceptKey is not "
                    + "an instance of org.onap.policy.models.base.PfReferenceKey", exc.getMessage());
        }

        assertFalse(refKey.isNewerThan(refKey));
    }

    @Test
    public void testmajorMinorPatch() {
        PfConceptKey key = new PfConceptKey("Key", "1");
        assertEquals(1, key.getMajorVersion());
        assertEquals(0, key.getMinorVersion());
        assertEquals(0, key.getPatchVersion());

        key = new PfConceptKey("Key", "1.2");
        assertEquals(1, key.getMajorVersion());
        assertEquals(2, key.getMinorVersion());
        assertEquals(0, key.getPatchVersion());

        key = new PfConceptKey("Key", "1.2.3");
        assertEquals(1, key.getMajorVersion());
        assertEquals(2, key.getMinorVersion());
        assertEquals(3, key.getPatchVersion());
    }
}
