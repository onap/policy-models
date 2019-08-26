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

package org.onap.policy.models.base;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import org.junit.Test;

public class PfReferenceKeyTest {

    private static final String PARENT_LOCAL_NAME = "ParentLocalName";
    private static final String NPKLN = "NPKLN";
    private static final String LOCAL_NAME = "LocalName";
    private static final String VERSION002 = "0.0.2";
    private static final String VERSION001 = "0.0.1";

    @Test
    public void testPfReferenceKey() {
        assertNotNull(new PfReferenceKey());
        assertNotNull(new PfReferenceKey(new PfConceptKey()));
        assertNotNull(new PfReferenceKey(new PfConceptKey(), LOCAL_NAME));
        assertNotNull(new PfReferenceKey(new PfReferenceKey()));
        assertNotNull(new PfReferenceKey(new PfReferenceKey(), LOCAL_NAME));
        assertNotNull(new PfReferenceKey(new PfConceptKey(), PARENT_LOCAL_NAME, LOCAL_NAME));
        assertNotNull(new PfReferenceKey("ParentKeyName", VERSION001, LOCAL_NAME));
        assertNotNull(new PfReferenceKey("ParentKeyName", VERSION001, PARENT_LOCAL_NAME, LOCAL_NAME));
        assertNotNull(new PfReferenceKey("ParentKeyName:0.0.1:ParentLocalName:LocalName"));
        assertEquals(PfReferenceKey.getNullKey().getKey(), PfReferenceKey.getNullKey());
        assertEquals("NULL:0.0.0:NULL:NULL", PfReferenceKey.getNullKey().getId());

        assertThatThrownBy(() -> new PfReferenceKey(new PfConceptKey(), null))
                        .hasMessage("parameter \"localName\" is null");

        PfReferenceKey testReferenceKey = new PfReferenceKey();
        testReferenceKey.setParentConceptKey(new PfConceptKey("PN", VERSION001));
        assertEquals("PN:0.0.1", testReferenceKey.getParentConceptKey().getId());

        assertEquals(0, testReferenceKey.getMajorVersion());
        assertEquals(0, testReferenceKey.getMinorVersion());
        assertEquals(1, testReferenceKey.getPatchVersion());

        assertEquals(1, testReferenceKey.getKeys().size());
        assertFalse(testReferenceKey.isNullKey());

        testReferenceKey.setParentReferenceKey(new PfReferenceKey("PN", VERSION001, "LN"));
        assertEquals("PN:0.0.1:NULL:LN", testReferenceKey.getParentReferenceKey().getId());

        testReferenceKey.setParentKeyName("NPKN");
        assertEquals("NPKN", testReferenceKey.getParentKeyName());

        testReferenceKey.setParentKeyVersion(VERSION001);
        assertEquals(VERSION001, testReferenceKey.getParentKeyVersion());

        testReferenceKey.setParentLocalName(NPKLN);
        assertEquals(NPKLN, testReferenceKey.getParentLocalName());

        testReferenceKey.setLocalName("NLN");
        assertEquals("NLN", testReferenceKey.getLocalName());

        assertThatThrownBy(() -> testReferenceKey.isCompatible(null))
                        .hasMessage("otherKey is marked @NonNull but is null");

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
        assertFalse(testReferenceKey.equals(new PfReferenceKey("PKN", VERSION002, "PLN", "LN")));
        assertFalse(testReferenceKey.equals(new PfReferenceKey("NPKN", VERSION002, "PLN", "LN")));
        assertFalse(testReferenceKey.equals(new PfReferenceKey("NPKN", VERSION001, "PLN", "LN")));
        assertFalse(testReferenceKey.equals(new PfReferenceKey("NPKN", VERSION001, "NPLN", "LN")));
        assertTrue(testReferenceKey.equals(new PfReferenceKey("NPKN", VERSION001, NPKLN, "NLN")));

        assertEquals(0, testReferenceKey.compareTo(testReferenceKey));
        assertEquals(0, testReferenceKey.compareTo(clonedReferenceKey));
        assertNotEquals(0, testReferenceKey.compareTo(new PfConceptKey()));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("PKN", VERSION002, "PLN", "LN")));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", VERSION002, "PLN", "LN")));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", VERSION001, "PLN", "LN")));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", VERSION001, "NPLN", "LN")));
        assertEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", VERSION001, NPKLN, "NLN")));

        assertFalse(testReferenceKey.equals(null));

        assertThatThrownBy(() -> new PfReferenceKey((PfReferenceKey) null)).isInstanceOf(NullPointerException.class);

        assertEquals(testReferenceKey, new PfReferenceKey(testReferenceKey));
    }

    @Test
    public void testValidation() throws Exception {
        PfReferenceKey testReferenceKey = new PfReferenceKey();
        testReferenceKey.setParentConceptKey(new PfConceptKey("PN", VERSION001));
        assertEquals("PN:0.0.1", testReferenceKey.getParentConceptKey().getId());

        Field parentNameField = testReferenceKey.getClass().getDeclaredField("parentKeyName");
        parentNameField.setAccessible(true);
        parentNameField.set(testReferenceKey, "Parent Name");
        PfValidationResult validationResult = new PfValidationResult();
        testReferenceKey.validate(validationResult);
        parentNameField.set(testReferenceKey, "ParentName");
        parentNameField.setAccessible(false);
        assertEquals(
                "parentKeyName invalid-parameter parentKeyName with value Parent Name "
                        + "does not match regular expression " + PfKey.NAME_REGEXP,
                validationResult.getMessageList().get(0).getMessage());

        Field parentVersionField = testReferenceKey.getClass().getDeclaredField("parentKeyVersion");
        parentVersionField.setAccessible(true);
        parentVersionField.set(testReferenceKey, "Parent Version");
        PfValidationResult validationResult2 = new PfValidationResult();
        testReferenceKey.validate(validationResult2);
        parentVersionField.set(testReferenceKey, VERSION001);
        parentVersionField.setAccessible(false);
        assertEquals(
                "parentKeyVersion invalid-parameter parentKeyVersion with value Parent Version "
                        + "does not match regular expression " + PfKey.VERSION_REGEXP,
                validationResult2.getMessageList().get(0).getMessage());

        Field parentLocalNameField = testReferenceKey.getClass().getDeclaredField("parentLocalName");
        parentLocalNameField.setAccessible(true);
        parentLocalNameField.set(testReferenceKey, "Parent Local Name");
        PfValidationResult validationResult3 = new PfValidationResult();
        testReferenceKey.validate(validationResult3);
        parentLocalNameField.set(testReferenceKey, PARENT_LOCAL_NAME);
        parentLocalNameField.setAccessible(false);
        assertEquals(
                "parentLocalName invalid-parameter parentLocalName with value "
                        + "Parent Local Name does not match regular expression [A-Za-z0-9\\-_\\.]+|^$",
                validationResult3.getMessageList().get(0).getMessage());

        Field localNameField = testReferenceKey.getClass().getDeclaredField("localName");
        localNameField.setAccessible(true);
        localNameField.set(testReferenceKey, "Local Name");
        PfValidationResult validationResult4 = new PfValidationResult();
        testReferenceKey.validate(validationResult4);
        localNameField.set(testReferenceKey, LOCAL_NAME);
        localNameField.setAccessible(false);
        assertEquals(
                "localName invalid-parameter localName with value Local Name "
                        + "does not match regular expression [A-Za-z0-9\\-_\\.]+|^$",
                validationResult4.getMessageList().get(0).getMessage());
    }
}
