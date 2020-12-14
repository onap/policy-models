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
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.assertj.core.api.AbstractStringAssert;
import org.junit.Test;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;

public class ValidationTest {

    private static final @NonNull String MY_FIELD = "myField";
    private static final CharSequence Q_FIELD = "\"" + MY_FIELD + "\"";
    private static final String HELLO = "hello";
    private static final String NULL_MSG = "null value";

    @Test
    public void testRefNotNull() {
        Function<Integer, ValidationResult> func = Validation.refNotNull(MY_FIELD);

        // valid - should return null
        assertNull(func.apply(10));

        // null value is invalid
        assertThat(func.apply(null).getResult()).contains(Q_FIELD).contains(PfConcept.IS_NULL);
    }

    @Test
    public void testValidateRegEx() {
        assertNull(Validation.validateRegEx(MY_FIELD, HELLO, ".*ll.*"));
        assertThat(Validation.validateRegEx(MY_FIELD, HELLO, "xyz").getResult()).contains(Q_FIELD)
                        .contains("does not match regular expression xyz");
        assertThat(Validation.validateRegEx(MY_FIELD, null, "xyz").getResult()).contains(Q_FIELD)
                        .contains(PfConceptKey.IS_NULL);
    }

    @Test
    public void testValidateNotBlankBeanValidationResultEntryOfStringString() {
        Map<String, String> map = Map.of("abc", "def", "", "blank-key", "blank-value", "");

        validate(result -> Validation.validateNotBlank(result, getEntry(map, "abc"))).isNull();

        validate(result -> Validation.validateNotBlank(result, getEntry(map, ""))).contains("key")
                        .contains("INVALID, is null");

        validate(result -> Validation.validateNotBlank(result, getEntry(map, "blank-value")))
                        .contains("value for blank-value").contains("INVALID, is null");
    }

    private Entry<String, String> getEntry(Map<String, String> map, String key) {
        for (Entry<String, String> ent : map.entrySet()) {
            if (ent.getKey().equals(key)) {
                return ent;
            }
        }

        return null;
    }

    @Test
    public void testValidateNotBlankBeanValidationResultStringString() {
        validate(result -> Validation.validateNotBlank(result, MY_FIELD, HELLO)).isNull();

        validate(result -> Validation.validateNotBlank(result, MY_FIELD, "")).contains(Q_FIELD)
                        .contains("INVALID, is null");

        validate(result -> Validation.validateNotBlank(result, MY_FIELD, null)).contains(Q_FIELD)
                        .contains("INVALID, is null");
    }

    @Test
    public void testValidateNotNull() {
        validate(result -> Validation.validateNotNull(result, MY_FIELD, null, false)).isNull();

        validate(result -> Validation.validateNotNull(result, MY_FIELD, null, true)).contains(Q_FIELD)
                        .contains(PfConcept.IS_NULL).doesNotContain(NULL_MSG);

        MyValidated valnull = new MyValidated(null);
        validate(result -> Validation.validateNotNull(result, MY_FIELD, valnull, true)).contains(Q_FIELD)
                        .doesNotContain(PfConcept.IS_NULL).contains(NULL_MSG);

        MyValidated valok = new MyValidated("some text");
        validate(result -> Validation.validateNotNull(result, MY_FIELD, valok, true)).isNull();
    }

    @Test
    public void testValidateItemsNotNull() {
        validate(result -> Validation.validateItemsNotNull(result, MY_FIELD, (List<Validated>) null)).isNull();

        // empty list
        List<MyValidated> lst = List.of();
        validate(result -> Validation.validateItemsNotNull(result, MY_FIELD, lst)).isNull();

        // all items valid
        List<MyValidated> lst2 = List.of(new MyValidated("some-value"), new MyValidated("another-value"));
        validate(result -> Validation.validateItemsNotNull(result, MY_FIELD, lst2)).isNull();

        // null item in the list
        List<MyValidated> lst3 = new ArrayList<>();
        lst3.add(new MyValidated("some-value"));
        lst3.add(null);
        lst3.add(new MyValidated("another-value"));
        validate(result -> Validation.validateItemsNotNull(result, MY_FIELD, lst3)).contains(Q_FIELD)
                        .contains(PfConcept.IS_NULL);

        // some items invalid
        List<MyValidated> lst4 = List.of(new MyValidated("a-value"), new MyValidated(null), new MyValidated("c-value"));
        validate(result -> Validation.validateItemsNotNull(result, MY_FIELD, lst4)).contains(Q_FIELD)
                        .contains(NULL_MSG);
        validate(result -> Validation.validateItemsNotNull(result, MY_FIELD, lst4)).contains(Q_FIELD)
                        .contains(NULL_MSG);
    }

    @Test
    public void testValidateItem() {
        validate(result -> Validation.validateItem(result, MY_FIELD, null, false)).isNull();

        validate(result -> Validation.validateItem(result, MY_FIELD, null, true)).contains(Q_FIELD)
                        .contains(PfConcept.IS_NULL);

        MyValidated valnull = new MyValidated(null);
        validate(result -> Validation.validateItem(result, MY_FIELD, valnull, true)).doesNotContain(PfConcept.IS_NULL)
                        .contains(Q_FIELD).contains(NULL_MSG);
        validate(result -> Validation.validateItem(result, MY_FIELD, valnull, false)).doesNotContain(PfConcept.IS_NULL)
                        .contains(Q_FIELD).contains(NULL_MSG);

        MyValidated valok = new MyValidated("some text");
        validate(result -> Validation.validateItem(result, MY_FIELD, valok, true)).isNull();
        validate(result -> Validation.validateItem(result, MY_FIELD, valok, false)).isNull();
    }

    @Test
    public void testValidateItemsBeanValidationResultStringListOfTBoolean() {
        validate(result -> Validation.validateItems(result, MY_FIELD, (List<Validated>) null)).isNull();

        // empty list
        List<MyValidated> lst = List.of();
        validate(result -> Validation.validateItems(result, MY_FIELD, lst)).isNull();

        // all items valid
        List<MyValidated> lst2 = List.of(new MyValidated("some-value"), new MyValidated("another-value"));
        validate(result -> Validation.validateItems(result, MY_FIELD, lst2)).isNull();

        // some items invalid
        List<MyValidated> lst3 = List.of(new MyValidated("a-value"), new MyValidated(null), new MyValidated("c-value"));
        validate(result -> Validation.validateItems(result, MY_FIELD, lst3)).contains(Q_FIELD)
                        .contains(NULL_MSG);
    }

    @Test
    public void testValidateItemsBeanValidationResultStringMapOfStringTBoolean() {
        validate(result -> Validation.validateItems(result, MY_FIELD, (Map<String, Validated>) null)).isNull();

        // empty map
        Map<String, MyValidated> map = Map.of();
        validate(result -> Validation.validateItems(result, MY_FIELD, map)).isNull();

        // all items valid
        Map<String, MyValidated> map2 = Map.of("some-key", new MyValidated("some-value"), "some-value",
                        new MyValidated("another-value"));
        validate(result -> Validation.validateItems(result, MY_FIELD, map2)).isNull();

        // some items invalid
        Map<String, MyValidated> map3 = Map.of("a-key", new MyValidated("a-value"), "null-value", new MyValidated(null),
                        "c-key", new MyValidated("c-value"));
        validate(result -> Validation.validateItems(result, MY_FIELD, map3)).contains(Q_FIELD)
                        .contains("value for null-value").contains(NULL_MSG);

        // null key in map
        Map<String, MyValidated> map4 = new HashMap<>();
        map4.put(null, new MyValidated(HELLO));
        validate(result -> Validation.validateItems(result, MY_FIELD, map4)).contains(Q_FIELD).contains("key")
                        .doesNotContain("value for").contains(PfConcept.IS_NULL);
    }


    private AbstractStringAssert<?> validate(Consumer<BeanValidationResult> function) {
        BeanValidationResult result = new BeanValidationResult("", "");
        function.accept(result);
        return assertThat(result.getResult());
    }


    @AllArgsConstructor
    private static class MyValidated implements Validated {
        @Getter
        private final String value;

        @Override
        public ValidationResult validate(@NonNull String fieldName) {
            if (value != null) {
                return null;
            }

            return new ObjectValidationResult(fieldName, value, ValidationStatus.INVALID, NULL_MSG);
        }
    }
}
