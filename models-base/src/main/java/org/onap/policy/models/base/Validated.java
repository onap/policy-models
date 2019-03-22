/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import lombok.NonNull;
import org.onap.policy.common.utils.validation.Assertions;
import org.onap.policy.models.base.PfValidationResult.ValidationResult;

/**
 * Classes that can be validated. This can be used as a super class or as a stand-alone
 * utility class.
 */
public class Validated {

    /**
     * Validates the fields of the object. The default method simply returns the result.
     *
     * @param result where to place the result
     * @return the result
     */
    public PfValidationResult validate(@NonNull PfValidationResult result) {
        return result;
    }

    /**
     * Validates that a field value is not null.
     *
     * @param container the object that contains the field
     * @param fieldName name of the field to be validated
     * @param value value to be validated
     * @param result where to place the result
     * @return the result
     */
    public PfValidationResult validateNotNull(@NonNull Object container, @NonNull String fieldName, Object value,
                    @NonNull PfValidationResult result) {

        if (value == null) {
            addError(container, fieldName, result, "null");
        }

        return result;
    }

    /**
     * Validates that the name and version of a concept key do not have the null default
     * values.
     *
     * @param value value to be validated
     * @param result where to place the result
     * @return the result
     */
    public PfValidationResult validateNotNull(@NonNull PfConceptKey value, @NonNull PfValidationResult result) {

        if (PfConceptKey.NULL_KEY_NAME.equals(value.getName())) {
            addError(value, "name", result, "null");
        }

        if (PfConceptKey.NULL_KEY_VERSION.equals(value.getVersion())) {
            addError(value, "version", result, "null");
        }

        return result;
    }

    /**
     * Validates the contents of a field, verifying that it matches a pattern, if it is
     * non-null.
     *
     * @param container the object that contains the field
     * @param fieldName name of the field to be validated
     * @param value value to be validated
     * @param pattern pattern used to validate the value
     * @param result where to place the result
     * @return the result
     */
    public PfValidationResult validateText(@NonNull Object container, @NonNull String fieldName, String value,
                    @NonNull String pattern, @NonNull PfValidationResult result) {

        if (value != null) {
            addError(container, fieldName, result,
                            Assertions.getStringParameterValidationMessage(fieldName, value, pattern));
        }

        return result;
    }

    /**
     * Validates the contents of a property field, verifying that the keys ands values are
     * non-null.
     *
     * @param container the object that contains the field
     * @param fieldName name of the field to be validated
     * @param properties properties to be validated
     * @param resultIn where to place the result
     * @return the result
     */
    public <T> PfValidationResult validatePropertiesNotNull(@NonNull Object container, @NonNull String fieldName,
                    Map<String, T> properties, @NonNull PfValidationResult resultIn) {

        PfValidationResult result = resultIn;

        if (properties == null) {
            return result;
        }

        for (Entry<String, T> ent : properties.entrySet()) {
            String key = ent.getKey();
            String keyName = fieldName + "." + key;
            result = validateNotNull(container, keyName, key, result);

            result = validateNotNull(container, keyName, ent.getValue(), result);
        }

        return result;
    }

    /**
     * Validates the items in a collection field are non-null.
     *
     * @param container the object that contains the field
     * @param fieldName name of the field to be validated
     * @param collection collection whose items are to be validated
     * @param resultIn where to place the result
     * @return the result
     */
    public <T> PfValidationResult validateCollectionNotNull(@NonNull Object container, @NonNull String fieldName,
                    Collection<T> collection, @NonNull PfValidationResult resultIn) {

        PfValidationResult result = resultIn;

        if (collection == null) {
            return result;
        }

        String prefix = fieldName + ".";
        int count = 0;

        for (T item : collection) {
            result = validateNotNull(container, prefix + count, item, result);
            ++count;
        }

        return result;
    }

    /**
     * Invokes the "validate()" method on each item in a collection field, if the item is
     * non-null.
     *
     * @param container the object that contains the field
     * @param fieldName name of the field to be validated
     * @param collection collection whose items are to be validated
     * @param result where to place the result
     * @return the result
     */
    public <T extends Validated> PfValidationResult validateCollection(@NonNull Object container,
                    @NonNull String fieldName, Collection<T> collection, @NonNull PfValidationResult result) {

        if (collection == null) {
            return result;
        }

        for (T item : collection) {
            if (item != null) {
                result = item.validate(result);
            }
        }

        return result;
    }

    /**
     * Invokes the "validate()" method on each item in a concept collection field, if the
     * item is non-null.
     *
     * @param container the object that contains the field
     * @param fieldName name of the field to be validated
     * @param collection collection whose items are to be validated
     * @param result where to place the result
     * @return the result
     */
    public <T extends PfConcept> PfValidationResult validateConceptCollection(@NonNull Object container,
                    @NonNull String fieldName, Collection<T> collection, @NonNull PfValidationResult result) {

        if (collection == null) {
            return result;
        }

        for (T item : collection) {
            if (item != null) {
                result = item.validate(result);
            }
        }

        return result;
    }

    /**
     * Adds an error message to the validation result.
     *
     * @param container the object that contains the field
     * @param fieldName name of the field to be validated
     * @param result where to place the result
     * @param errmsg the error message to be added, or {@code null} if nothing to add
     */
    public void addError(@NonNull Object container, @NonNull String fieldName, @NonNull PfValidationResult result,
                    String errmsg) {
        if (errmsg != null) {
            result.addValidationMessage(new PfValidationMessage(makeKey(container), container.getClass(),
                            ValidationResult.INVALID, fieldName + " invalid-" + errmsg));
        }
    }

    /**
     * Makes a PfKey suitable for insertion into a validation message. Note: the
     * "toString()" method of the key simply invokes container.toString();
     *
     * @param container the container object for which the key should be made
     * @return a key for the container
     */
    public PfKey makeKey(@NonNull Object container) {

        return new PfConceptKey() {
            private static final long serialVersionUID = 1L;

            @Override
            public String toString() {
                return container.toString();
            }
        };
    }
}
