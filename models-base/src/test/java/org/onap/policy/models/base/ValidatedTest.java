/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.base;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.junit.Test;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;

public class ValidatedTest {
    private static final @NonNull String MY_FIELD = "myField";
    private static final @NonNull String Q_KEY = "\"" + Validated.KEY_TOKEN + "\"";
    private static final @NonNull String Q_VALUE = "\"" + Validated.VALUE_TOKEN + "\"";
    private static final String NOT_SAME = "not same";
    private static final String TEXT = "some text";
    private static final String OTHER = "other text";
    private static final String NAME = "myKey";
    private static final String VERSION = "1.0.0";
    private static final String BLANKS = "\t \n";

    @Test
    public void testAddResult() {
        BeanValidationResult result = new BeanValidationResult("", this);
        Validated.addResult(result, MY_FIELD, TEXT, "some message");
        assertThat(result.getResult()).contains(MY_FIELD).contains(TEXT).contains("some message");

        assertThatThrownBy(() -> Validated.addResult(null, MY_FIELD, TEXT, OTHER))
                        .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Validated.addResult(result, null, TEXT, OTHER))
                        .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.addResult(result, MY_FIELD, null, OTHER)).doesNotThrowAnyException();

        assertThatThrownBy(() -> Validated.addResult(result, MY_FIELD, TEXT, null))
                        .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testMakeNullResult() {
        ValidationResult rnull = Validated.makeNullResult(MY_FIELD, TEXT);
        assertEquals(MY_FIELD, rnull.getName());
        assertThat(rnull.getResult()).contains(MY_FIELD).contains(TEXT).contains(Validated.IS_NULL);
        assertFalse(rnull.isValid());

        assertThatThrownBy(() -> Validated.makeNullResult(null, TEXT)).isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.makeNullResult(MY_FIELD, null)).doesNotThrowAnyException();
    }

    @Test
    public void testValidateOptional() {
        BeanValidationResult result = new BeanValidationResult("", this);
        Validated.validateOptional(result, MY_FIELD, null);
        assertTrue(result.isClean());

        Validated.validateOptional(result, MY_FIELD, new MyString(TEXT));
        assertTrue(result.isClean());

        Validated.validateOptional(result, MY_FIELD, new MyString(OTHER));
        assertThat(result.getResult()).contains(MY_FIELD).contains(OTHER).contains(NOT_SAME);

        assertThatThrownBy(() -> Validated.validateOptional(null, MY_FIELD, new MyString(TEXT)))
                        .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Validated.validateOptional(result, null, new MyString(TEXT)))
                        .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.validateOptional(result, MY_FIELD, null)).doesNotThrowAnyException();
    }

    @Test
    public void testValidateNotNull() {
        assertThat(Validated.validateNotNull(MY_FIELD, TEXT)).isNull();

        assertThat(Validated.validateNotNull(MY_FIELD, null).getResult()).contains(MY_FIELD)
                        .contains(Validated.IS_NULL);

        // should invoke the value's validate() method, which should return success
        assertThat(Validated.validateNotNull(MY_FIELD, new MyString(TEXT))).isNull();

        // should invoke the value's validate() method, which should return failure
        assertThat(Validated.validateNotNull(MY_FIELD, new MyString(OTHER)).getResult()).contains(MY_FIELD)
                        .contains(NOT_SAME);

        assertThatThrownBy(() -> Validated.validateNotNull(null, TEXT)).isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.validateNotNull(MY_FIELD, null)).doesNotThrowAnyException();
    }

    @Test
    public void testValidateNotBlank() {
        assertThat(Validated.validateNotBlank(MY_FIELD, TEXT, false)).isNull();
        assertThat(Validated.validateNotBlank(MY_FIELD, TEXT, true)).isNull();

        assertThat(Validated.validateNotBlank(MY_FIELD, null, false)).isNull();
        assertThat(Validated.validateNotBlank(MY_FIELD, null, true).getResult()).contains(MY_FIELD)
                        .contains(Validated.IS_BLANK);

        assertThat(Validated.validateNotBlank(MY_FIELD, "", false).getResult()).contains(MY_FIELD)
                        .contains(Validated.IS_BLANK);
        assertThat(Validated.validateNotBlank(MY_FIELD, "", true).getResult()).contains(MY_FIELD)
                        .contains(Validated.IS_BLANK);

        assertThatThrownBy(() -> Validated.validateNotBlank(null, TEXT, false))
                        .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.validateNotBlank(MY_FIELD, null, false)).doesNotThrowAnyException();
    }

    @Test
    public void testValidateRegex() {
        assertThat(Validated.validateRegex(MY_FIELD, "hello", ".*ll.*")).isNull();

        assertThat(Validated.validateRegex(MY_FIELD, "hello", "[x-z]").getResult()).contains(MY_FIELD).contains("hello")
                        .contains("does not match regular expression [x-z]");

        assertThatThrownBy(() -> Validated.validateRegex(null, "hello", "ll")).isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.validateRegex(MY_FIELD, null, "ll")).doesNotThrowAnyException();

        assertThatThrownBy(() -> Validated.validateRegex(MY_FIELD, "hello", null))
                        .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testValidateKeyNotNull() throws CoderException {
        assertThat(Validated.validateKeyNotNull(MY_FIELD, new PfConceptKey(NAME, VERSION)).getResult()).isNull();
        assertThat(Validated.validateKeyNotNull(MY_FIELD, new PfConceptKey(NAME, PfConceptKey.NULL_KEY_VERSION))
                        .getResult()).isNull();
        assertThat(Validated.validateKeyNotNull(MY_FIELD, new PfConceptKey(PfConceptKey.NULL_KEY_NAME, VERSION))
                        .getResult()).isNull();

        // key is null
        assertThat(Validated.validateKeyNotNull(MY_FIELD, new PfConceptKey()).getResult()).contains(MY_FIELD)
                        .doesNotContain("\"name\"").doesNotContain("\"version\"").contains(Validated.IS_A_NULL_KEY);

        /*
         * Key is not null, but key.validate() should fail due to an invalid version.
         * Note: have to create the key by decoding from json, as the class will prevent
         * an invalid version from being assigned.
         */
        PfConceptKey key = new StandardCoder().decode("{'name':'myKey','version':'bogus'}".replace('\'', '"'),
                        PfConceptKey.class);
        assertThat(Validated.validateKeyNotNull(MY_FIELD, key).getResult()).contains(MY_FIELD).contains("version")
                        .contains("does not match regular expression");

        // null parameter tests
        assertThatThrownBy(() -> Validated.validateKeyNotNull(null, new PfConceptKey()))
                        .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.validateKeyNotNull(MY_FIELD, null)).doesNotThrowAnyException();
    }

    @Test
    public void testValidateKeyVersionNotNull() {
        assertThat(Validated.validateKeyVersionNotNull(MY_FIELD, null)).isNull();

        assertThat(Validated.validateKeyVersionNotNull(MY_FIELD, new PfConceptKey(NAME, VERSION))).isNull();

        assertThat(Validated.validateKeyVersionNotNull(MY_FIELD, new PfConceptKey(NAME, PfConceptKey.NULL_KEY_VERSION))
                        .getResult()).contains(MY_FIELD).contains("version").contains(Validated.IS_NULL);

        assertThatThrownBy(() -> Validated.validateKeyVersionNotNull(null, new PfConceptKey()))
                        .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.validateKeyVersionNotNull(MY_FIELD, null)).doesNotThrowAnyException();
    }

    @Test
    public void testValidateMinIntIntegerBoolean_testValidateMinStringIntegerIntIntegerBoolean() {
        /*
         * No "special" value, don't check the reference.
         */
        BiFunction<String, Integer, ValidationResult> func = Validated.validateMin(10, null, false);
        assertThat(func.apply(MY_FIELD, null)).isNull();

        // exact match
        assertThat(func.apply(MY_FIELD, 10)).isNull();

        assertThat(func.apply(MY_FIELD, 20)).isNull();

        assertThat(func.apply(MY_FIELD, 9).getResult()).contains(MY_FIELD).contains("9")
                        .contains("is below the minimum value: 10");

        /*
         * "special" value, don't check the reference.
         */
        func = Validated.validateMin(10, 7, false);
        assertThat(func.apply(MY_FIELD, null)).isNull();

        // exact match
        assertThat(func.apply(MY_FIELD, 10)).isNull();

        assertThat(func.apply(MY_FIELD, 20)).isNull();

        // special value - should be ok
        assertThat(func.apply(MY_FIELD, 7)).isNull();

        assertThat(func.apply(MY_FIELD, 9).getResult()).contains(MY_FIELD).contains("9")
                        .contains("is below the minimum value: 10");

        /*
         * Check the reference (i.e., generate an error if the value is null).
         */
        func = Validated.validateMin(10, null, true);
        assertThat(func.apply(MY_FIELD, null).getResult()).contains(MY_FIELD).contains(Validated.IS_NULL);

        // exact match
        assertThat(func.apply(MY_FIELD, 10)).isNull();

        assertThat(func.apply(MY_FIELD, 20)).isNull();

        assertThat(func.apply(MY_FIELD, 9).getResult()).contains(MY_FIELD).contains("9")
                        .contains("is below the minimum value: 10");


        BiFunction<String, Integer, ValidationResult> func2 = func;
        assertThatThrownBy(() -> func2.apply(null, 30)).isInstanceOf(NullPointerException.class);

        assertThatCode(() -> func2.apply(MY_FIELD, null)).doesNotThrowAnyException();
    }

    @Test
    public void testValidateList() {
        BeanValidationResult result = new BeanValidationResult("", this);
        Validated.validateList(result, MY_FIELD, null, Validated::validateNotNull);
        assertThat(result.getResult()).isNull();

        result = new BeanValidationResult("", this);
        Validated.validateList(result, MY_FIELD, List.of(TEXT, OTHER), Validated::validateNotNull);
        assertThat(result.getResult()).isNull();

        List<String> list = new ArrayList<>();
        list.add(TEXT);
        list.add(null);
        list.add(OTHER);
        list.add(null);
        result = new BeanValidationResult("", this);
        Validated.validateList(result, MY_FIELD, list, Validated::validateNotNull);
        assertThat(result.getResult()).doesNotContain("0").contains("1").doesNotContain("2").contains("3")
                        .contains(Validated.IS_NULL);

        BeanValidationResult result2 = result;
        assertThatThrownBy(() -> Validated.validateList(null, MY_FIELD, List.of(), Validated::validateNotNull))
                        .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Validated.validateList(result2, null, List.of(), Validated::validateNotNull))
                        .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.validateList(result2, MY_FIELD, null, Validated::validateNotNull))
                        .doesNotThrowAnyException();

        assertThatThrownBy(() -> Validated.validateList(result2, MY_FIELD, List.of(), null))
                        .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testValidateMap() {
        BeanValidationResult result = new BeanValidationResult("", this);
        Validated.validateMap(result, MY_FIELD, null, Validated::validateEntryNotBlankNotBlank);
        assertThat(result.getResult()).isNull();

        result = new BeanValidationResult("", this);
        Validated.validateMap(result, MY_FIELD, Map.of("abc", TEXT, "def", OTHER),
                        Validated::validateEntryNotBlankNotBlank);
        assertThat(result.getResult()).isNull();

        // invalid values
        Map<String, String> map = new HashMap<>();
        map.put("ghi", TEXT);
        map.put("jkl", "");
        map.put("mno", OTHER);
        map.put("pqr", "");
        result = new BeanValidationResult("", this);
        Validated.validateMap(result, MY_FIELD, map, Validated::validateEntryNotBlankNotBlank);
        assertThat(result.getResult()).doesNotContain("abc").contains("jkl").doesNotContain("mno").contains("pqr")
                        .contains(Q_VALUE).doesNotContain(Q_KEY).contains(Validated.IS_BLANK);

        // invalid keys
        map = new HashMap<>();
        map.put("stu", TEXT);
        map.put("", TEXT);
        map.put("vwx", OTHER);
        map.put(null, OTHER);
        result = new BeanValidationResult("", this);
        Validated.validateMap(result, MY_FIELD, map, Validated::validateEntryNotBlankNotBlank);
        assertThat(result.getResult()).doesNotContain("stu").contains("\"\"").doesNotContain("vwx").contains("null")
                        .contains(Q_KEY).doesNotContain(Q_VALUE).contains(Validated.IS_BLANK);

        BeanValidationResult result2 = result;
        Map<String, String> map2 = map;
        assertThatThrownBy(() -> Validated.validateMap(null, MY_FIELD, map2, Validated::validateEntryNotBlankNotBlank))
                        .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> Validated.validateMap(result2, null, map2, Validated::validateEntryNotBlankNotBlank))
                        .isInstanceOf(NullPointerException.class);

        assertThatCode(() -> Validated.validateMap(result2, MY_FIELD, null, Validated::validateEntryNotBlankNotBlank))
                        .doesNotThrowAnyException();

        assertThatThrownBy(() -> Validated.validateMap(result2, MY_FIELD, map2, null))
                        .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testValidateEntryNotBlankNotBlank() {
        assertThat(Validated.validateEntryNotBlankNotBlank(makeEntry(TEXT, OTHER))).isNull();

        // try invalid values for the key
        assertThat(Validated.validateEntryNotBlankNotBlank(makeEntry(null, OTHER)).getResult()).contains(Q_KEY)
                        .contains(Validated.IS_BLANK).doesNotContain(Q_VALUE);

        assertThat(Validated.validateEntryNotBlankNotBlank(makeEntry(BLANKS, OTHER)).getResult()).contains(Q_KEY)
                        .contains(Validated.IS_BLANK).doesNotContain(Q_VALUE);

        // try invalid values for the value
        assertThat(Validated.validateEntryNotBlankNotBlank(makeEntry(TEXT, null)).getResult()).contains(Q_VALUE)
                        .contains(Validated.IS_BLANK).doesNotContain(Q_KEY);

        assertThat(Validated.validateEntryNotBlankNotBlank(makeEntry(TEXT, BLANKS)).getResult()).contains(Q_VALUE)
                        .contains(Validated.IS_BLANK).doesNotContain(Q_KEY);

        // both invalid
        assertThat(Validated.validateEntryNotBlankNotBlank(makeEntry(BLANKS, BLANKS)).getResult()).contains(Q_KEY)
                        .contains(Q_VALUE);
    }

    @Test
    public void testValidateEntryNotBlankNotNull() {
        assertThat(Validated.validateEntryNotBlankNotNull(makeEntry(TEXT, OTHER))).isNull();

        // try invalid values for the key
        assertThat(Validated.validateEntryNotBlankNotNull(makeEntry(null, OTHER)).getResult()).contains(Q_KEY)
                        .contains(Validated.IS_BLANK).doesNotContain(Q_VALUE);

        assertThat(Validated.validateEntryNotBlankNotNull(makeEntry(BLANKS, OTHER)).getResult()).contains(Q_KEY)
                        .contains(Validated.IS_BLANK).doesNotContain(Q_VALUE);

        // try invalid values for the value
        assertThat(Validated.validateEntryNotBlankNotNull(makeEntry(TEXT, null)).getResult()).contains(Q_VALUE)
                        .contains(Validated.IS_NULL).doesNotContain(Q_KEY);

        // blanks should have no impact for the value
        assertThat(Validated.validateEntryNotBlankNotNull(makeEntry(TEXT, BLANKS))).isNull();

        // both invalid
        assertThat(Validated.validateEntryNotBlankNotNull(makeEntry(BLANKS, null)).getResult()).contains(Q_KEY)
                        .contains(Q_VALUE);
    }

    @Test
    public void testValidateEntryValueNotNull() {
        assertThat(Validated.validateEntryValueNotNull(makeEntry(TEXT, OTHER))).isNull();

        // blanks should have no impact
        assertThat(Validated.validateEntryValueNotNull(makeEntry(BLANKS, OTHER))).isNull();
        assertThat(Validated.validateEntryNotBlankNotNull(makeEntry(TEXT, BLANKS))).isNull();

        assertThat(Validated.validateEntryValueNotNull(makeEntry(null, OTHER)).getResult()).contains(Q_KEY)
                        .contains(Validated.IS_NULL).doesNotContain(Q_VALUE);

        assertThat(Validated.validateEntryValueNotNull(makeEntry(TEXT, null)).getResult()).contains(Q_VALUE)
                        .contains(Validated.IS_NULL).doesNotContain(Q_KEY);

        // should invoke the value's validate() method, which should return success
        assertThat(Validated.validateEntryValueNotNull(makeEntry(TEXT, new MyString(TEXT)))).isNull();

        // should invoke the value's validate() method, which should return failure
        assertThat(Validated.validateEntryValueNotNull(makeEntry(TEXT, new MyString(OTHER))).getResult())
                        .contains(Q_VALUE).contains(NOT_SAME).doesNotContain(Q_KEY);

        // both invalid
        assertThat(Validated.validateEntryValueNotNull(makeEntry(null, null)).getResult()).contains(Q_KEY)
                        .contains(Q_VALUE);
    }

    @Test
    public void testGetKeyId() {
        // not a key field - should just use the given value
        BeanValidationResult result = new BeanValidationResult("", this);
        Validated.addResult(result, MY_FIELD, TEXT, "some message");
        assertThat(result.getResult()).contains(MY_FIELD).contains(TEXT).contains("some message");

        // repeat with a key field - should use the key's ID
        result = new BeanValidationResult("", this);
        Validated.addResult(result, MY_FIELD, new PfConceptKey(NAME, VERSION), "some message");
        assertThat(result.getResult()).contains(MY_FIELD).contains("myKey:1.0.0").contains("some message");
    }

    private <V> Map.Entry<String, V> makeEntry(String key, V value) {
        Map<String, V> map = new HashMap<>();
        map.put(key, value);
        return map.entrySet().iterator().next();
    }

    @AllArgsConstructor
    private static class MyString extends Validated {
        private final String text;

        @Override
        public ValidationResult validate(String fieldName) {
            if (TEXT.equals(text)) {
                return null;
            }

            return new ObjectValidationResult(fieldName, text, ValidationStatus.INVALID, NOT_SAME);
        }
    }
}
