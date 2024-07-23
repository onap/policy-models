/*
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2024 Nordix Foundation
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

import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;

/**
 * Classes that can be validated. This can be used as a super class or as a stand-alone
 * utility class.
 */
public class Validated {
    public static final String IS_BLANK = "is blank";
    public static final String IS_A_NULL_KEY = "is a null key";
    public static final String IS_NULL = "is null";
    public static final String NOT_DEFINED = "not defined";
    public static final String NOT_FOUND = "not found";

    public static final String KEY_TOKEN = "key";
    public static final String VALUE_TOKEN = "value";

    /**
     * Validates the fields of the object. The default method uses a {@link PfValidator}
     * to validate the object.
     *
     * @param fieldName name of the field containing this
     * @return the result, or {@code null}
     */
    public BeanValidationResult validate(@NonNull String fieldName) {
        return new PfValidator().validateTop(fieldName, this);
    }

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
        result.addResult(fieldName, getKeyId(value), ValidationStatus.INVALID, errorMessage);
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
     * Validates a key, ensuring that it isn't null and that it's structurally sound.
     *
     * @param result where to add the validation result
     * @param fieldName name of the field containing the key
     * @param key the field's value
     */
    public static void validateKeyNotNull(BeanValidationResult result, @NonNull String fieldName, PfKey key) {
        if (key == null) {
            result.addResult(fieldName, key, ValidationStatus.INVALID, IS_A_NULL_KEY);
            return;
        }

        if (key.isNullKey()) {
            result.addResult(fieldName, key.getId(), ValidationStatus.INVALID, IS_A_NULL_KEY);
            return;
        }

        result.addResult(key.validate(fieldName));
    }

    /**
     * Validates a key's version, ensuring that it isn't null.
     *
     * @param result where to add the validation result
     * @param fieldName name of the field containing the key
     * @param key the field's value
     */
    public static void validateKeyVersionNotNull(BeanValidationResult result, @NonNull String fieldName,
                    PfConceptKey key) {
        if (key != null && key.isNullVersion()) {
            var result2 = new BeanValidationResult(fieldName, key);
            result2.addResult(makeNullResult(PfKeyImpl.VERSION_TOKEN, key.getVersion()));
            result.addResult(result2);
        }
    }

    /**
     * Gets a key's ID, if the value is a {@link PfKey}.
     *
     * @param value value from which to get the ID
     * @return the value's ID, if it's a key, the original value otherwise
     */
    private static Object getKeyId(Object value) {
        return (value instanceof PfKey pfKey ? pfKey.getId() : value);
    }
}
