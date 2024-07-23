/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.common.parameters.ValueValidator;
import org.onap.policy.models.base.validation.annotations.PfMin;
import org.onap.policy.models.base.validation.annotations.VerifyKey;

public class PfValidator extends BeanValidator {

    @Override
    protected void addValidators(ValueValidator validator) {
        super.addValidators(validator);

        validator.addAnnotation(VerifyKey.class, this::verKey);
        validator.addAnnotation(PfMin.class, this::verPfMin);
    }

    /**
     * Verifies that the value is >= the minimum value.
     *
     * @param result where to add the validation result
     * @param fieldName field whose value is being verified
     * @param annot annotation against which the value is being verified
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verPfMin(BeanValidationResult result, String fieldName, PfMin annot, Object value) {
        if (!(value instanceof Number num)) {
            return true;
        }

        if (num.longValue() == annot.allowed()) {
            // this value is always allowed
            return true;
        }

        return verMin(result, fieldName, annot.value(), value);
    }

    /**
     * Invokes the value's {@link Validated#validate(String) validate()} method, if the
     * value is of type {@link Validated}.
     */
    @Override
    public boolean verCascade(BeanValidationResult result, String fieldName, Object value) {
        if (value instanceof Validated validated) {
            ValidationResult result2 = validated.validate(fieldName);
            if (result2 == null) {
                return true;
            }

            if (!result2.isClean()) {
                result.addResult(result2);
            }

            return result2.isValid();
        }

        return super.verCascade(result, fieldName, value);
    }

    /**
     * Validates a key.
     *
     * @param result where to add the validation result
     * @param fieldName name of the field containing the key
     * @param annot validation annotations for the key
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verKey(BeanValidationResult result, String fieldName, VerifyKey annot, Object value) {
        if (!(value instanceof PfKey pfkey)) {
            return true;
        }

        if (annot.keyNotNull() && pfkey.isNullKey()) {
            result.addResult(fieldName, xlate(pfkey), ValidationStatus.INVALID, Validated.IS_A_NULL_KEY);
            return false;
        }

        if (annot.valid()) {
            verCascade(result, fieldName, value);
        }

        if (!(pfkey instanceof PfKeyImpl keyimpl)) {
            return true;
        }

        var result2 = new BeanValidationResult(fieldName, value);

        if (annot.nameNotNull() && keyimpl.isNullName()) {
            result2.addResult("name", pfkey.getName(), ValidationStatus.INVALID, Validated.IS_NULL);
        }

        if (annot.versionNotNull() && keyimpl.isNullVersion()) {
            result2.addResult("version", pfkey.getVersion(), ValidationStatus.INVALID, Validated.IS_NULL);
        }

        if (!result2.isClean()) {
            result.addResult(result2);
        }

        return result2.isValid();
    }

    @Override
    public Object xlate(Object value) {
        return (value instanceof PfKey pfKey ? pfKey.getId() : value);
    }
}
