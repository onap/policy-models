/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2024 Nordix Foundation.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;

class PfReferenceKeyTest {

    private static final String PARENT_LOCAL_NAME = "ParentLocalName";
    private static final String NPKLN = "NPKLN";
    private static final String LOCAL_NAME = "LocalName";
    private static final String VERSION002 = "0.0.2";
    private static final String VERSION001 = "0.0.1";

    @Test
    void testPfReferenceKeyNotNull() {
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
    }

    @Test
    void testPfReferenceKey() {
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
            .hasMessageMatching("^otherKey is marked .*on.*ull but is null$");

        assertFalse(testReferenceKey.isCompatible(PfConceptKey.getNullKey()));
        assertFalse(testReferenceKey.isCompatible(PfReferenceKey.getNullKey()));
        assertTrue(testReferenceKey.isCompatible(testReferenceKey));

        assertEquals(PfKey.Compatibility.DIFFERENT, testReferenceKey.getCompatibility(PfConceptKey.getNullKey()));
        assertEquals(PfKey.Compatibility.DIFFERENT, testReferenceKey.getCompatibility(PfReferenceKey.getNullKey()));
        assertEquals(PfKey.Compatibility.IDENTICAL, testReferenceKey.getCompatibility(testReferenceKey));

        assertTrue(testReferenceKey.validate("").isValid());
    }

    @Test
    void testMultiplePfReferenceKey() {
        PfReferenceKey testReferenceKey = setTestReferenceKey();
        testReferenceKey.clean();

        PfReferenceKey clonedReferenceKey = new PfReferenceKey(testReferenceKey);
        assertEquals("PfReferenceKey(parentKeyName=NPKN, parentKeyVersion=0.0.1, parentLocalName=NPKLN, localName=NLN)",
            clonedReferenceKey.toString());

        assertNotEquals(0, testReferenceKey.hashCode());

        assertEquals(testReferenceKey, (Object) testReferenceKey);
        assertEquals(testReferenceKey, clonedReferenceKey);
        assertNotEquals(testReferenceKey, (Object) "Hello");
        assertNotEquals(testReferenceKey, new PfReferenceKey("PKN", VERSION002, "PLN", "LN"));
        assertNotEquals(testReferenceKey, new PfReferenceKey("NPKN", VERSION002, "PLN", "LN"));
        assertNotEquals(testReferenceKey, new PfReferenceKey("NPKN", VERSION001, "PLN", "LN"));
        assertNotEquals(testReferenceKey, new PfReferenceKey("NPKN", VERSION001, "NPLN", "LN"));
        assertEquals(testReferenceKey, new PfReferenceKey("NPKN", VERSION001, NPKLN, "NLN"));

        assertEquals(0, testReferenceKey.compareTo(testReferenceKey));
        assertEquals(0, testReferenceKey.compareTo(clonedReferenceKey));
        assertNotEquals(0, testReferenceKey.compareTo(new PfConceptKey()));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("PKN", VERSION002, "PLN", "LN")));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", VERSION002, "PLN", "LN")));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", VERSION001, "PLN", "LN")));
        assertNotEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", VERSION001, "NPLN", "LN")));
        assertEquals(0, testReferenceKey.compareTo(new PfReferenceKey("NPKN", VERSION001, NPKLN, "NLN")));

        assertNotNull(testReferenceKey);

        assertThatThrownBy(() -> new PfReferenceKey((PfReferenceKey) null)).isInstanceOf(NullPointerException.class);

        assertEquals(testReferenceKey, new PfReferenceKey(testReferenceKey));
    }

    @Test
    void testValidation() throws Exception {
        PfReferenceKey testReferenceKey = new PfReferenceKey();
        testReferenceKey.setParentConceptKey(new PfConceptKey("PN", VERSION001));
        assertEquals("PN:0.0.1", testReferenceKey.getParentConceptKey().getId());

        Field parentNameField = testReferenceKey.getClass().getDeclaredField("parentKeyName");
        parentNameField.setAccessible(true);
        parentNameField.set(testReferenceKey, "Parent Name");
        ValidationResult validationResult = testReferenceKey.validate("");
        parentNameField.set(testReferenceKey, "ParentName");
        parentNameField.setAccessible(false);
        assertThat(validationResult.getResult()).contains("\"parentKeyName\"")
                        .contains("does not match regular expression " + PfKey.NAME_REGEXP);

        Field parentVersionField = testReferenceKey.getClass().getDeclaredField("parentKeyVersion");
        parentVersionField.setAccessible(true);
        parentVersionField.set(testReferenceKey, "Parent Version");
        ValidationResult validationResult2 = testReferenceKey.validate("");
        parentVersionField.set(testReferenceKey, VERSION001);
        parentVersionField.setAccessible(false);
        assertThat(validationResult2.getResult()).contains("\"parentKeyVersion\"")
            .contains("does not match regular expression " + PfKey.VERSION_REGEXP);

        Field parentLocalNameField = testReferenceKey.getClass().getDeclaredField("parentLocalName");
        parentLocalNameField.setAccessible(true);
        parentLocalNameField.set(testReferenceKey, "Parent Local Name");
        ValidationResult validationResult3 = testReferenceKey.validate("");
        parentLocalNameField.set(testReferenceKey, PARENT_LOCAL_NAME);
        parentLocalNameField.setAccessible(false);
        assertThat(validationResult3.getResult()).contains("\"parentLocalName\"")
            .contains("does not match regular expression [A-Za-z0-9\\-_\\.]+|^$");

        Field localNameField = testReferenceKey.getClass().getDeclaredField("localName");
        localNameField.setAccessible(true);
        localNameField.set(testReferenceKey, "Local Name");
        ValidationResult validationResult4 = testReferenceKey.validate("");
        localNameField.set(testReferenceKey, LOCAL_NAME);
        localNameField.setAccessible(false);
        assertThat(validationResult4.getResult()).contains("\"localName\"")
        .contains("does not match regular expression [A-Za-z0-9\\-_\\.]+|^$");
    }

    private PfReferenceKey setTestReferenceKey() {
        PfReferenceKey testReferenceKey = new PfReferenceKey();
        testReferenceKey.setParentConceptKey(new PfConceptKey("PN", VERSION001));
        testReferenceKey.setParentReferenceKey(new PfReferenceKey("PN", VERSION001, "LN"));
        testReferenceKey.setParentKeyName("NPKN");
        testReferenceKey.setParentKeyVersion(VERSION001);
        testReferenceKey.setParentLocalName(NPKLN);
        testReferenceKey.setLocalName("NLN");

        return testReferenceKey;
    }
}
