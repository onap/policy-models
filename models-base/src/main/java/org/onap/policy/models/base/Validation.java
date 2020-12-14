/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;

/**
 * Utility class for validating Policy models.
 */
public final class Validation {
    private Validation() {
        // Cannot be subclassed
    }

    /**
     * Validates a field's value against a regular expression.
     *
     * @param fieldName field name
     * @param value field value
     * @param regex regular expression that should match the value
     * @return a result, if the value does not match the regular expression, {@code null}
     *         otherwise
     */
    public static ValidationResult validateRegEx(String fieldName, String value, String regex) {
        if (value == null) {
            return new ObjectValidationResult(fieldName, value, ValidationStatus.INVALID, PfConceptKey.IS_NULL);
        }

        if (!value.matches(regex)) {
            return new ObjectValidationResult(fieldName, value, ValidationStatus.INVALID,
                            "does not match regular expression " + regex);
        }

        return null;
    }

    /**
     * Validates that the key and value of a Map entry are neither null nor empty.
     *
     * @param result validation results are added here
     * @param entry map entry to be validated
     */
    public static void validateNotBlank(BeanValidationResult result, Map.Entry<String, String> entry) {
        validateNotBlank(result, "key", entry.getKey());
        validateNotBlank(result, "value for " + entry.getKey(), entry.getValue());
    }

    /**
     * Validates that a value is neither null nor empty.
     *
     * @param result validation results are added here
     * @param subName name of the sub-object
     * @param subObject the sub-object
     */
    public static void validateNotBlank(BeanValidationResult result, String subName, String subObject) {
        if (StringUtils.isBlank(subObject)) {
            result.addResult(new ObjectValidationResult(subName, subObject, ValidationStatus.INVALID,
                            PfConcept.IS_NULL));
        }
    }

    /**
     * Validates that a sub-object within the bean is not {@code null}.
     *
     * @param result validation results are added here
     * @param subName name of the sub-object
     * @param subObject the sub-object
     * @param checkRef {@code true} if the reference is to be checked
     * @param checkValue {@code true} if the value's validateNotNull() method should be
     *        invoked
     */
    public static <T extends Validated> void validateNotNull(BeanValidationResult result, String subName, T subObject,
                    boolean checkRef, boolean checkValue) {
        if (checkRef) {
            result.validateNotNull(subName, subObject);
        }

        if (checkValue && subObject != null) {
            result.addResult(subObject.validateNotNull(subName));
        }
    }

    /**
     * Validates that the items within a sub-object are not {@code null}.
     *
     * @param result validation results are added here
     * @param subName name of the sub-object
     * @param subObject list of sub-objects
     */
    public static <T extends Validated> void validateItemsNotNull(BeanValidationResult result, String subName,
                    List<T> subObject) {
        if (subObject == null) {
            return;
        }

        for (T item: subObject) {
            result.validateNotNull(subName, item);
            if (item != null) {
                result.addResult(item.validateNotNull(subName));
            }
        }
    }

    /**
     * Validates a sub-object.
     *
     * @param result validation results are added here
     * @param subName name of the sub-object
     * @param subObject list of sub-objects
     * @param checkRef {@code true} if the item reference is to be checked for
     *        {@code null}
     */
    public static <T extends Validated> void validateItem(BeanValidationResult result, String subName,
                    T subObject, boolean checkRef) {
        if (checkRef) {
            result.validateNotNull(subName, subObject);
        }

        if (subObject != null) {
            result.addResult(subObject.validate(subName));
        }
    }

    /**
     * Validates the items within a sub-object.
     *
     * @param result validation results are added here
     * @param subName name of the sub-object
     * @param subObject list of sub-objects
     * @param checkRef {@code true} if each item reference is to be checked for
     *        {@code null}
     */
    public static <T extends Validated> void validateItems(BeanValidationResult result, String subName,
                    Collection<T> subObject, boolean checkRef) {
        if (subObject == null) {
            return;
        }

        for (T item : subObject) {
            validateItem(result, subName, item, checkRef);
        }
    }

    /**
     * Validates the items within a sub-object.
     *
     * @param output validation results are added here
     * @param subName name of the sub-object
     * @param subObject list of sub-objects
     * @param checkRef {@code true} if each item reference is to be checked for
     *        {@code null}
     */
    public static <T extends Validated> void validateItems(BeanValidationResult output, String subName,
                    Map<String, T> subObject, boolean checkRef) {
        if (subObject == null) {
            return;
        }

        BeanValidationResult result = new BeanValidationResult(subName, subObject);

        for (Entry<String, T> entry : subObject.entrySet()) {
            result.validateNotNull("key", entry.getKey());
            validateItem(result, "value for " + entry.getKey(), entry.getValue(), checkRef);
        }

        if (!result.isClean()) {
            output.addResult(result);
        }
    }
}
