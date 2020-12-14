/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.models.base.PfKey.Compatibility;
import org.onap.policy.models.base.testconcepts.DummyPfKey;

public class PfKeyImplTest {

    private static final String REGEX_ERROR = "does not match regular expression";
    private static final String OTHER_IS_NULL = "^otherKey is marked .*on.*ull but is null$";
    private static final String ID_IS_NULL = "^id is marked .*on.*ull but is null$";
    private static final String VERSION123 = "1.2.3";
    private static final String VERSION100 = "1.0.0";
    private static final String VERSION001 = "0.0.1";

    @Test
    public void testConceptKey() {
        assertThatIllegalArgumentException().isThrownBy(() -> new MyKey("some bad key id"))
            .withMessage("parameter \"id\": value \"some bad key id\", " + "does not match regular expression \""
                + PfKey.KEY_ID_REGEXP + "\"");

        assertThatThrownBy(() -> new MyKey((MyKey) null))
            .hasMessageMatching("^copyConcept is marked .*on.*ull but is null$");

        MyKey someKey0 = new MyKey();
        assertTrue(someKey0.isNullKey());
        assertEquals(new MyKey(PfKey.NULL_KEY_NAME, PfKey.NULL_KEY_VERSION), someKey0);

        MyKey someKey1 = new MyKey("name", VERSION001);
        MyKey someKey2 = new MyKey(someKey1);
        MyKey someKey3 = new MyKey(someKey1.getId());
        assertEquals(someKey1, someKey2);
        assertEquals(someKey1, someKey3);
        assertFalse(someKey1.isNullKey());
        assertFalse(someKey1.isNullVersion());

        assertEquals(someKey2, someKey1.getKey());
        assertEquals(1, someKey1.getKeys().size());

        someKey0.setName("zero");
        someKey0.setVersion("0.0.2");

        someKey3.setVersion("0.0.2");

        MyKey someKey4 = new MyKey(someKey1);
        someKey4.setVersion("0.1.2");

        MyKey someKey4a = new MyKey(someKey1);
        someKey4a.setVersion("0.0.0");

        MyKey someKey5 = new MyKey(someKey1);
        someKey5.setVersion("1.2.2");

        MyKey someKey6 = new MyKey(someKey1);
        someKey6.setVersion("3.0.0");

        assertEquals("name:0.1.2", someKey4.getId());

        assertThatThrownBy(() -> someKey0.getCompatibility(null)).isInstanceOf(NullPointerException.class)
            .hasMessageMatching("^otherKey is marked .*on.*ull but is null$");

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

        assertTrue(someKey0.validate("").isClean());
        assertTrue(someKey1.validate("").isClean());
        assertTrue(someKey2.validate("").isClean());
        assertTrue(someKey3.validate("").isClean());
        assertTrue(someKey4.validate("").isClean());
        assertTrue(someKey5.validate("").isClean());
        assertTrue(someKey6.validate("").isClean());

        someKey0.clean();
        assertNotNull(someKey0.toString());

        MyKey someKey7 = new MyKey(someKey1);
        assertEquals(244799191, someKey7.hashCode());
        assertEquals(0, someKey7.compareTo(someKey1));
        assertEquals(-12, someKey7.compareTo(someKey0));

        assertThatThrownBy(() -> someKey0.compareTo(null)).isInstanceOf(NullPointerException.class)
            .hasMessageMatching("^otherObj is marked .*on.*ull but is null$");

        assertEquals(0, someKey0.compareTo(someKey0));
        assertEquals(-36, someKey0.compareTo(new DummyPfKey()));

        assertNotEquals(someKey0, null);
        assertEquals(someKey0, (Object) someKey0);
        assertNotEquals(someKey0, (Object) new DummyPfKey());

        MyKey someKey8 = new MyKey();
        someKey8.setVersion(VERSION001);
        assertFalse(someKey8.isNullKey());

        someKey8.setVersion("10");
        assertEquals(0, someKey8.getMinorVersion());

        someKey8.setVersion("10.11");
        assertEquals(0, someKey8.getPatchVersion());
    }

    @Test
    public void testNullArguments() {
        assertThatThrownBy(() -> new MyKey((String) null)).hasMessageMatching(ID_IS_NULL);

        assertThatThrownBy(() -> new MyKey((MyKey) null))
            .hasMessageMatching("^copyConcept is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new MyKey(null, null)).hasMessageMatching("name is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new MyKey("name", null))
            .hasMessageMatching("^version is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new MyKey(null, VERSION001))
            .hasMessageMatching("^name is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new MyKey("AKey", VERSION001).isCompatible(null)).hasMessageMatching(OTHER_IS_NULL);
    }

    @Test
    public void testValidation() throws Exception {
        MyKey testKey = new MyKey("TheKey", VERSION001);
        assertEquals("TheKey:0.0.1", testKey.getId());

        Field nameField = testKey.getClass().getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(testKey, "Key Name");
        ValidationResult validationResult = testKey.validate("testKey");
        nameField.set(testKey, "TheKey");
        nameField.setAccessible(false);
        assertThat(validationResult.getResult()).contains("name")
                        .contains("INVALID, does not match regular expression " + PfKey.NAME_REGEXP);

        Field versionField = testKey.getClass().getDeclaredField("version");
        versionField.setAccessible(true);
        versionField.set(testKey, "Key Version");
        ValidationResult validationResult2 = testKey.validate("testKey");
        versionField.set(testKey, VERSION001);
        versionField.setAccessible(false);
        assertThat(validationResult2.getResult()).contains("version")
                        .contains("INVALID, does not match regular expression " + PfKey.VERSION_REGEXP);
    }

    @Test
    public void testkeynewerThan() {
        MyKey key1 = new MyKey("Key1", VERSION123);

        assertThatThrownBy(() -> key1.isNewerThan(null)).hasMessageMatching(OTHER_IS_NULL);

        assertThatThrownBy(() -> key1.isNewerThan(new PfReferenceKey())).hasMessage(
            "org.onap.policy.models.base.PfReferenceKey is not " + "an instance of " + PfKeyImpl.class.getName());

        assertFalse(key1.isNewerThan(key1));

        MyKey key1a = new MyKey("Key1a", VERSION123);
        assertFalse(key1.isNewerThan(key1a));

        MyKey key1b = new MyKey("Key0", VERSION123);
        assertTrue(key1.isNewerThan(key1b));

        key1a.setName("Key1");
        assertFalse(key1.isNewerThan(key1a));

        key1a.setVersion("0.2.3");
        assertTrue(key1.isNewerThan(key1a));
        key1a.setVersion("2.2.3");
        assertFalse(key1.isNewerThan(key1a));
        key1a.setVersion(VERSION123);
        assertFalse(key1.isNewerThan(key1a));

        key1a.setVersion("1.1.3");
        assertTrue(key1.isNewerThan(key1a));
        key1a.setVersion("1.3.3");
        assertFalse(key1.isNewerThan(key1a));
        key1a.setVersion(VERSION123);
        assertFalse(key1.isNewerThan(key1a));

        key1a.setVersion("1.2.2");
        assertTrue(key1.isNewerThan(key1a));
        key1a.setVersion("1.2.4");
        assertFalse(key1.isNewerThan(key1a));
        key1a.setVersion(VERSION123);
        assertFalse(key1.isNewerThan(key1a));

        key1.setVersion(VERSION100);
        assertFalse(key1.isNewerThan(key1a));
        key1a.setVersion(VERSION100);
        assertFalse(key1.isNewerThan(key1a));

        PfReferenceKey refKey = new PfReferenceKey();

        assertThatThrownBy(() -> refKey.isNewerThan(null)).hasMessageMatching(OTHER_IS_NULL);

        assertThatThrownBy(() -> refKey.isNewerThan(new MyKey()))
            .hasMessage(MyKey.class.getName() + " is not an instance of " + PfReferenceKey.class.getName());

        assertFalse(refKey.isNewerThan(refKey));
    }

    @Test
    public void testmajorMinorPatch() {
        MyKey key = new MyKey("Key", VERSION100);
        assertEquals(1, key.getMajorVersion());
        assertEquals(0, key.getMinorVersion());
        assertEquals(0, key.getPatchVersion());

        key = new MyKey("Key", "1.2.0");
        assertEquals(1, key.getMajorVersion());
        assertEquals(2, key.getMinorVersion());
        assertEquals(0, key.getPatchVersion());

        key = new MyKey("Key", VERSION123);
        assertEquals(1, key.getMajorVersion());
        assertEquals(2, key.getMinorVersion());
        assertEquals(3, key.getPatchVersion());
    }

    @Test
    public void testValidate() {
        MyKey goodKey = new MyKey("Key", VERSION001);
        MyKey nullName = new MyKey(PfKey.NULL_KEY_NAME, VERSION001);
        MyKey nullVersion = new MyKey("Key", PfKey.NULL_KEY_VERSION);
        MyKey nullKey = new MyKey(PfKey.NULL_KEY_NAME, PfKey.NULL_KEY_VERSION);

        assertThat(goodKey.validate("my-key").getResult()).isNull();
        assertThat(nullName.validate("my-key").getResult()).contains("\"name\"").contains(PfConceptKey.IS_NULL);
        assertThat(nullVersion.validate("my-key").getResult()).isNull();
        assertThat(nullKey.validate("my-key").getResult()).contains(PfConceptKey.IS_A_NULL_KEY)
                        .doesNotContain("name").doesNotContain("version");

        assertThatThrownBy(() -> goodKey.validate(null))
                        .hasMessageMatching("fieldName is marked .*on.*ull but is null");

        /*
         * Repeat tests, disallowing null versions.
         */
        assertThat(goodKey.validate("my-key", true).getResult()).isNull();
        assertThat(nullName.validate("my-key", true).getResult()).contains("\"name\"").contains(PfConceptKey.IS_NULL);
        assertThat(nullVersion.validate("my-key", true).getResult()).contains("\"version\"")
                        .contains(PfConceptKey.IS_NULL);
        assertThat(nullKey.validate("my-key", true).getResult()).contains(PfConceptKey.IS_A_NULL_KEY)
                        .doesNotContain("name").doesNotContain("version");

        assertThatThrownBy(() -> goodKey.validate(null, true))
                        .hasMessageMatching("fieldName is marked .*on.*ull but is null");
    }

    @Test
    public void testValidateName() {
        MyKey goodKey = new MyKey("Key", VERSION001);
        MyKey nullKey = new MyKey(PfKey.NULL_KEY_NAME, VERSION001);
        MyKey badKey = new MyKey("bad $%", VERSION001);

        assertThat(goodKey.validate("my-key").getResult()).isNull();
        assertThat(nullKey.validate("my-key", true).getResult()).contains("\"name\"").contains(PfConceptKey.IS_NULL);
        assertThat(badKey.validate("my-key").getResult()).contains("\"name\"").contains("bad")
                        .contains(REGEX_ERROR);
    }

    @Test
    public void testValidateVersion() {
        MyKey goodKey = new MyKey("Key", VERSION001);
        MyKey nullKey = new MyKey("Key", PfKey.NULL_KEY_VERSION);
        MyKey badKey = new MyKey("Key", "bad");

        // tests where version may be null
        assertThat(goodKey.validate("my-key").getResult()).isNull();
        assertThat(nullKey.validate("my-key").getResult()).isNull();
        assertThat(badKey.validate("my-key").getResult()).contains("\"version\"").contains("bad")
                        .contains(REGEX_ERROR);

        // tests where version may NOT be null
        assertThat(goodKey.validate("my-key", true).getResult()).isNull();
        assertThat(nullKey.validate("my-key", true).getResult()).contains("\"version\"")
                        .contains(PfConceptKey.IS_NULL);
        assertThat(badKey.validate("my-key", true).getResult()).contains("\"version\"").contains("bad")
                        .contains(REGEX_ERROR);
    }

    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = false)
    @NoArgsConstructor
    private static class MyKey extends PfKeyImpl {
        private static final long serialVersionUID = 1L;

        private String name;
        private String version;

        public MyKey(String name, String version) {
            super(name, version);
        }

        public MyKey(String id) {
            super(id);
        }

        public MyKey(MyKey myKey) {
            super(myKey);
        }
    }
}
