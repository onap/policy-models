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
    public PfValidationResult validate(PfValidationResult result) {
        return result;
    }

    /**
     * Validates that a field value is not null.
     *
     * @param container the object that contains the field
     * @param fieldName name of the field to be validated
     * @param value value to be validated
     * @param result where to place the result
     */
    public void validateNotNull(Object container, String fieldName, String value, PfValidationResult result) {

        if (value == null) {
            result.addValidationMessage(new PfValidationMessage(makeKey(container), container.getClass(),
                            ValidationResult.INVALID, fieldName + " invalid-null"));
        }
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
     */
    public void validateText(Object container, String fieldName, String value, String pattern,
                    PfValidationResult result) {

        if (value != null) {
            String errmsg = Assertions.getStringParameterValidationMessage(fieldName, value, pattern);

            if (errmsg != null) {
                result.addValidationMessage(new PfValidationMessage(makeKey(container), container.getClass(),
                                ValidationResult.INVALID, fieldName + " invalid-" + errmsg));
            }
        }
    }

    /**
     * Makes a PfKey suitable for insertion into a validation message. Note: the
     * "toString()" method of the key simply invokes container.toString();
     *
     * @param container the container object for which the key should be made
     * @return a key for the container
     */
    public PfKey makeKey(Object container) {

        return new PfConceptKey() {
            private static final long serialVersionUID = 1L;

            @Override
            public String toString() {
                return container.toString();
            }
        };
    }
}
