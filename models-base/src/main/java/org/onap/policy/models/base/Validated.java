/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import com.google.re2j.Pattern;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;

/**
 * Classes that can be validated. This can be used as a super class or as a stand-alone
 * utility class.
 */
public abstract class Validated {
    public static final String IS_BLANK = "is blank";
    public static final String IS_A_NULL_KEY = "is a null key";
    public static final String IS_NULL = "is null";
    public static final String NOT_DEFINED = "not defined";
    public static final String NOT_FOUND = "not found";

    public static final String KEY_TOKEN = "key";
    public static final String VALUE_TOKEN = "value";

    /**
     * Validates the fields of the object.
     *
     * @param fieldName name of the field containing this
     * @return the result, or {@code null}
     */
    public abstract ValidationResult validate(String fieldName);

    /**
     * Adds a result indicating that a value is invalid.
     *
     * @param result where to put the result
     * @param fieldName name of the field containing the value
     * @param value the field's value
     * @param errorMessage the error message
     */
    public static void addResult(@NonNull BeanValidationResult result, @NonNull String fieldName, Object value,
                    @NonNull String errorMessage) {
        result.addResult(
                        new ObjectValidationResult(fieldName, getKeyId(value), ValidationStatus.INVALID, errorMessage));
    }

    /**
     * Makes a result that indicates a value is invalid, because it is null.
     *
     * @param fieldName name of the field containing the value
     * @param value the field's value
     * @return a result indicating the value is invalid
     */
    public static ValidationResult makeNullResult(@NonNull String fieldName, Object value) {
        return new ObjectValidationResult(fieldName, getKeyId(value), ValidationStatus.INVALID, IS_NULL);
    }

    /**
     * Validates a value, if is not {@code null}, by invoking it's validate() method.
     *
     * @param result where to put the result
     * @param fieldName name of the field containing the value
     * @param value the field's value
     */
    public static void validateOptional(@NonNull BeanValidationResult result, @NonNull String fieldName,
                    Validated value) {
        if (value != null) {
            result.addResult(value.validate(fieldName));
        }
    }

    /**
     * Validates that a value is not {@code null}. If the value is a subclass of this
     * class, then it's {@link #validate(String)} method is invoked, too.
     *
     * @param fieldName name of the field containing the value
     * @param value the field's value
     * @return a result, or {@code null}
     */
    public static ValidationResult validateNotNull(@NonNull String fieldName, Object value) {
        if (value == null) {
            return new ObjectValidationResult(fieldName, value, ValidationStatus.INVALID, IS_NULL);
        }

        if (value instanceof Validated) {
            return ((Validated) value).validate(fieldName);
        }

        return null;
    }

    /**
     * Validates that a value is not "blank" (i.e., empty). value.
     *
     * @param fieldName name of the field containing the value
     * @param value the field's value
     * @param checkNull {@code true} if to validate that the value is not {@code null}
     * @return a result, or {@code null}
     */
    public static ValidationResult validateNotBlank(@NonNull String fieldName, String value, boolean checkNull) {
        if (value == null && !checkNull) {
            return null;
        }

        if (StringUtils.isBlank(value)) {
            return new ObjectValidationResult(fieldName, value, ValidationStatus.INVALID, IS_BLANK);
        }

        return null;
    }

    /**
     * Validates that a value matches regular expression.
     *
     * @param fieldName name of the field containing the value
     * @param value the field's value
     * @param pattern regular expression to be matched
     * @return a result, or {@code null}
     */
    public static ValidationResult validateRegex(@NonNull String fieldName, String value, @NonNull String pattern) {
        if (value == null) {
            return makeNullResult(fieldName, value);
        }

        if (!Pattern.matches(pattern, value)) {
            return new ObjectValidationResult(fieldName, value, ValidationStatus.INVALID,
                            "does not match regular expression " + pattern);
        }

        return null;
    }

    /**
     * Validates a key, ensuring that it isn't null and that it's structurally sound.
     *
     * @param fieldName name of the field containing the key
     * @param key the field's value
     * @return a result, or {@code null}
     */
    public static ValidationResult validateKeyNotNull(@NonNull String fieldName, PfKey key) {
        if (key == null) {
            return new ObjectValidationResult(fieldName, key, ValidationStatus.INVALID, IS_A_NULL_KEY);
        }

        if (key.isNullKey()) {
            return new ObjectValidationResult(fieldName, key.getId(), ValidationStatus.INVALID, IS_A_NULL_KEY);
        }

        return key.validate(fieldName);
    }

    /**
     * Validates a key's version, ensuring that it isn't null.
     *
     * @param fieldName name of the field containing the key
     * @param key the field's value
     * @return a result, or {@code null}
     */
    public static BeanValidationResult validateKeyVersionNotNull(@NonNull String fieldName, PfConceptKey key) {
        if (key != null && key.isNullVersion()) {
            BeanValidationResult result = new BeanValidationResult(fieldName, key);
            result.addResult(makeNullResult(PfKeyImpl.VERSION_TOKEN, key.getVersion()));
            return result;
        }

        return null;
    }

    /**
     * Generates a function to validate that a value is not below a minimum.
     *
     * @param min minimum value allowed
     * @param allowedValue {@code null} or an allowed value outside the range
     * @param checkRef {@code true} to generate an error if the value is {@code null}
     * @return a function to validate that a value is not below a minimum
     */
    public static BiFunction<String, Integer, ValidationResult> validateMin(int min, Integer allowedValue,
                    boolean checkRef) {
        return (name, value) -> validateMin(name, value, min, allowedValue, checkRef);
    }

    /**
     * Validates that a value is not below a minimum.
     *
     * @param fieldName name of the field containing the key
     * @param value the field's value
     * @param min minimum value allowed
     * @param allowedValue {@code null} or an allowed value outside the range
     * @param checkRef {@code true} to generate an error if the value is {@code null}
     * @return a result, or {@code null}
     */
    public static ValidationResult validateMin(@NonNull String fieldName, Integer value, int min, Integer allowedValue,
                    boolean checkRef) {
        if (value == null) {
            if (checkRef) {
                return makeNullResult(fieldName, value);
            }

            return null;
        }

        if (value < min && !value.equals(allowedValue)) {
            return new ObjectValidationResult(fieldName, value, ValidationStatus.INVALID,
                            "is below the minimum value: " + min);
        }

        return null;
    }

    /**
     * Validates the items in a list.
     *
     * @param result where to add the results
     * @param fieldName name of the field containing the list
     * @param list the field's list (may be {@code null})
     * @param checker function to validate in individual item in the list
     */
    public static <T> void validateList(@NonNull BeanValidationResult result, @NonNull String fieldName,
                    Collection<T> list, @NonNull BiFunction<String, T, ValidationResult> checker) {
        if (list == null) {
            return;
        }

        BeanValidationResult result2 = new BeanValidationResult(fieldName, list);

        int count = 0;
        for (T value : list) {
            result2.addResult(checker.apply(String.valueOf(count++), value));
        }

        if (!result2.isClean()) {
            result.addResult(result2);
        }
    }

    /**
     * Validates the items in a map.
     *
     * @param result where to add the results
     * @param fieldName name of the field containing the list
     * @param map the field's map (may be {@code null})
     * @param checker function to validate in individual item in the list
     */
    public static <T> void validateMap(@NonNull BeanValidationResult result, @NonNull String fieldName,
                    Map<String, T> map, @NonNull Function<Map.Entry<String, T>, ValidationResult> checker) {
        if (map == null) {
            return;
        }

        BeanValidationResult result2 = new BeanValidationResult(fieldName, map);

        for (Entry<String, T> entry : map.entrySet()) {
            result2.addResult(checker.apply(entry));
        }

        if (!result2.isClean()) {
            result.addResult(result2);
        }
    }

    /**
     * Validates a Map entry, ensuring that neither the key nor the value are "blank"
     * (i.e., empty or {@code null}).
     *
     * @param entry entry to be validated
     * @return a result, or {@code null}
     */
    public static BeanValidationResult validateEntryNotBlankNotBlank(Map.Entry<String, String> entry) {
        BeanValidationResult result = new BeanValidationResult("" + entry.getKey(), entry.getKey());

        if (StringUtils.isBlank(entry.getKey())) {
            Validated.addResult(result, KEY_TOKEN, entry.getKey(), IS_BLANK);
        }

        if (StringUtils.isBlank(entry.getValue())) {
            Validated.addResult(result, VALUE_TOKEN, entry.getValue(), IS_BLANK);
        }

        return (result.isClean() ? null : result);
    }

    /**
     * Validates a Map entry, ensuring that the key is not "blank" (i.e., empty or
     * {@code null}) and the value is not {@code null}.
     *
     * @param entry entry to be validated
     * @return a result, or {@code null}
     */
    public static BeanValidationResult validateEntryNotBlankNotNull(Map.Entry<String, String> entry) {
        BeanValidationResult result = new BeanValidationResult("" + entry.getKey(), entry.getKey());

        if (StringUtils.isBlank(entry.getKey())) {
            Validated.addResult(result, KEY_TOKEN, entry.getKey(), IS_BLANK);
        }

        if (entry.getValue() == null) {
            result.addResult(makeNullResult(VALUE_TOKEN, entry.getValue()));
        }

        return (result.isClean() ? null : result);
    }

    /**
     * Validates a Map entry, ensuring that neither the key nor the value are
     * {@code null}. If the value is a subclass of this class, then it's
     * {@link #validate(String)} method is invoked.
     *
     * @param entry entry to be validated
     * @return a result, or {@code null}
     */
    public static <V> BeanValidationResult validateEntryValueNotNull(Map.Entry<String, V> entry) {
        BeanValidationResult result = new BeanValidationResult("" + entry.getKey(), entry.getKey());

        if (entry.getKey() == null) {
            result.addResult(makeNullResult(KEY_TOKEN, entry.getKey()));
        }

        V value = entry.getValue();
        if (value == null) {
            result.addResult(makeNullResult(VALUE_TOKEN, value));
        } else if (value instanceof Validated) {
            result.addResult(((Validated) value).validate(VALUE_TOKEN));
        }

        return (result.isClean() ? null : result);
    }

    /**
     * Gets a key's ID, if the value is a {@link PfKey}.
     *
     * @param value value from which to get the ID
     * @return the value's ID, if it's a key, the original value otherwise
     */
    private static Object getKeyId(Object value) {
        return (value instanceof PfKey ? ((PfKey) value).getId() : value);
    }
}
