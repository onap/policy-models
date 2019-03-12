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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.junit.Test;

public class PfReferenceKeyTest {

    @Test
    public void testPfReferenceKey() {
        assertNotNull(new PfReferenceKey());
        assertNotNull(new PfReferenceKey(new PfConceptKey()));
        assertNotNull(new PfReferenceKey(new PfConceptKey(), "LocalName"));
        assertNotNull(new PfReferenceKey(new PfReferenceKey()));
        assertNotNull(new PfReferenceKey(new PfReferenceKey(), "LocalName"));
        assertNotNull(new PfReferenceKey(new PfConceptKey(), "ParentLocalName", "LocalName"));
        assertNotNull(new PfReferenceKey("ParentKeyName", "0.0.1", "LocalName"));
        assertNotNull(new PfReferenceKey("ParentKeyName", "0.0.1", "ParentLocalName", "LocalName"));
        assertNotNull(new PfReferenceKey("ParentKeyName:0.0.1:ParentLocalName:LocalName"));
        assertEquals(PfReferenceKey.getNullKey().getKey(), PfReferenceKey.getNullKey());
        assertEquals("NULL:0.0.0:NULL:NULL", PfReferenceKey.getNullKey().getId());

        try {
            new PfReferenceKey(new PfConceptKey(), null);
            fail("test should throw an exception");
        } catch (Exception exc) {
            assertEquals("parameter \"localName\" is null", exc.getMessage());
        }

        PfReferenceKey testReferenceKey = new PfReferenceKey();
        testReferenceKey.setParentConceptKey(new PfConceptKey("PN", "0.0.1"));
        assertEquals("PN:0.0.1", testReferenceKey.getParentConceptKey().getId());

        assertEquals(1, testReferenceKey.getKeys().size());
        assertFalse(testReferenceKey.isNullKey());

        testReferenceKey.setParentReferenceKey(new PfReferenceKey("PN", "0.0.1", "LN"));
        assertEquals("PN:0.0.1:NULL:LN", testReferenceKey.getParentReferenceKey().getId());

        testReferenceKey.setParentKeyName("NPKN");
        assertEquals("NPKN", testReferenceKey.getParentKeyName());

        testReferenceKey.setParentKeyVersion("0.0.1");
        assertEquals("0.0.1", testReferenceKey.getParentKeyVersion());

        testReferenceKey.setParentLocalName("NPKLN");
        assertEquals("NPKLN", testReferenceKey.getParentLocalName());

        testReferenceKey.setLocalName("NLN");
        assertEquals("NLN", testReferenceKey.getLocalName());

        assertFalse(testReferenceKey.isCompatible(PfConceptKey.getNullKey()));
        assertFalse(testReferenceKey.isCompatible(PfReferenceKey.getNullKey()));
        assertTrue(testReferenceKey.isCompatible(testReferenceKey));

        assertEquals(PfKey.Compatibility.DIFFERENT, testReferenceKey.getCompatibility(PfConceptKey.getNullKey()));
        assertEquals(PfKey.Compatibility.DIFFERENT, testReferenceKey.getCompatibility(PfReferenceKey.getNullKey()));
        assertEquals(PfKey.Compatibility.IDENTICAL, testReferenceKey.getCompatibility(testReferenceKey));

        PfValidationResult result = new PfValidationResult();
        result = testReferenceKey.validate(result);
        assertEquals(PfValidationResult.ValidationResult.VALID, result.getValidationResult());

        testReferenceKey.clean();

        PfReferenceKey clonedReferenceKey = new PfReferenceKey(testReferenceKey);
        assertEquals("PfReferenceKey(parentKeyName=NPKN, parentKeyVersion=0.0.1, parentLocalName=NPKLN, localName=NLN)",
                clonedReferenceKey.toString());

        assertFalse(testReferenceKey.hashCode() == 0);

        assertTrue(testReferenceKey.equals(testReferenceKey));
        assertTrue(testReferenceKey.equals(clonedReferenceKey));
        assertFalse(testReferenceKey.equals("Hello"));
        assertFalse(testReferenceKey.equals(new PfReferenceKey("PKN", "0.0.2", "PLN", "LN")));
        assertFalse(testReferenceKey.equals(new PfReferenceKey("NPKN", "0.0.2", "PLN", "LN")));
        assertFalse(testReferenceKey.equals(new PfReferenceKey("NPKN", "0.0.1", "PLN", "LN")));
        assertFalse(testReferenceKey.equals(new PfReferenceKey("NPKN", "0.0.1", "NPLN", "LN")));
        assertTrue(testReferenceKey.equals(new PfReferenceKey("NPKN", "0.0.1", "NPKLN", "NLN")));

        assertEquals(0, testReferenceKey.compareTo(testReferenceKey));
        assertEquals(0, testReferenceKey.compareTo(clonedReferenceKey));
        assertNotEquals(0, testReferenceKey.compareTo(new PfConceptKey()));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("PKN", "0.0.2", "PLN", "LN")));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", "0.0.2", "PLN", "LN")));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", "0.0.1", "PLN", "LN")));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", "0.0.1", "NPLN", "LN")));
        assertEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", "0.0.1", "NPKLN", "NLN")));

        assertFalse(testReferenceKey.equals(null));

        try {
            testReferenceKey.copyTo(null);
            fail("test should throw an exception here");
        } catch (Exception iae) {
            assertEquals("target may not be null", iae.getMessage());
        }

        try {
            testReferenceKey.copyTo(new PfConceptKey("Key", "0.0.1"));
            fail("test should throw an exception here");
        } catch (Exception iae) {
            assertEquals("org.onap.policy.models.base.PfConceptKey"
                    + " is not an instance of org.onap.policy.models.base.PfReferenceKey", iae.getMessage());
        }

        PfReferenceKey targetRefKey = new PfReferenceKey();
        assertEquals(testReferenceKey, testReferenceKey.copyTo(targetRefKey));
    }

    @Test
    public void testValidation() {
        PfReferenceKey testReferenceKey = new PfReferenceKey();
        testReferenceKey.setParentConceptKey(new PfConceptKey("PN", "0.0.1"));
        assertEquals("PN:0.0.1", testReferenceKey.getParentConceptKey().getId());

        try {
            Field parentNameField = testReferenceKey.getClass().getDeclaredField("parentKeyName");
            parentNameField.setAccessible(true);
            parentNameField.set(testReferenceKey, "Parent Name");
            PfValidationResult validationResult = new PfValidationResult();
            testReferenceKey.validate(validationResult);
            parentNameField.set(testReferenceKey, "ParentName");
            parentNameField.setAccessible(false);
            assertEquals(
                    "parentKeyName invalid-parameter parentKeyName with value Parent Name "
                            + "does not match regular expression [A-Za-z0-9\\-_\\.]+",
                    validationResult.getMessageList().get(0).getMessage());
        } catch (Exception validationException) {
            fail("test should not throw an exception");
        }

        try {
            Field parentVersionField = testReferenceKey.getClass().getDeclaredField("parentKeyVersion");
            parentVersionField.setAccessible(true);
            parentVersionField.set(testReferenceKey, "Parent Version");
            PfValidationResult validationResult = new PfValidationResult();
            testReferenceKey.validate(validationResult);
            parentVersionField.set(testReferenceKey, "0.0.1");
            parentVersionField.setAccessible(false);
            assertEquals(
                    "parentKeyVersion invalid-parameter parentKeyVersion with value Parent Version "
                            + "does not match regular expression [A-Za-z0-9.]+",
                    validationResult.getMessageList().get(0).getMessage());
        } catch (Exception validationException) {
            fail("test should not throw an exception");
        }

        try {
            Field parentLocalNameField = testReferenceKey.getClass().getDeclaredField("parentLocalName");
            parentLocalNameField.setAccessible(true);
            parentLocalNameField.set(testReferenceKey, "Parent Local Name");
            PfValidationResult validationResult = new PfValidationResult();
            testReferenceKey.validate(validationResult);
            parentLocalNameField.set(testReferenceKey, "ParentLocalName");
            parentLocalNameField.setAccessible(false);
            assertEquals(
                    "parentLocalName invalid-parameter parentLocalName with value "
                            + "Parent Local Name does not match regular expression [A-Za-z0-9\\-_\\.]+|^$",
                    validationResult.getMessageList().get(0).getMessage());
        } catch (Exception validationException) {
            fail("test should not throw an exception");
        }

        try {
            Field localNameField = testReferenceKey.getClass().getDeclaredField("localName");
            localNameField.setAccessible(true);
            localNameField.set(testReferenceKey, "Local Name");
            PfValidationResult validationResult = new PfValidationResult();
            testReferenceKey.validate(validationResult);
            localNameField.set(testReferenceKey, "LocalName");
            localNameField.setAccessible(false);
            assertEquals(
                    "localName invalid-parameter localName with value Local Name "
                            + "does not match regular expression [A-Za-z0-9\\-_\\.]+|^$",
                    validationResult.getMessageList().get(0).getMessage());
        } catch (Exception validationException) {
            fail("test should not throw an exception");
        }
    }
}
