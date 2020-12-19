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

import java.util.Map;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.EntryValidator;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.common.parameters.ValueValidator;
import org.onap.policy.models.base.validation.annotations.Key;
import org.onap.policy.models.base.validation.annotations.PfEntries;
import org.onap.policy.models.base.validation.annotations.PfItems;
import org.onap.policy.models.base.validation.annotations.PfMin;

public class PfValidator extends BeanValidator {

    @Override
    protected void addValidators(ValueValidator validator) {
        super.addValidators(validator);

        validator.addAnnotation(PfItems.class, this::verCollection);
        validator.addAnnotation(PfEntries.class, this::verMap);
        validator.addAnnotation(Key.class, this::verKey);
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
        if (!(value instanceof Number)) {
            return true;
        }

        Number num = (Number) value;
        if (num.longValue() == annot.allowed()) {
            // this value is always allowed
            return true;
        }

        return verMin(result, fieldName, annot.value(), value);
    }

    /**
     * Validates the items in a Map.
     *
     * @param result where to add the validation result
     * @param fieldName name of the field containing the collection
     * @param annot validation annotations for individual entries
     * @param value value to be verified
     * @return {@code true} if the next check should be performed, {@code false} otherwise
     */
    public boolean verMap(BeanValidationResult result, String fieldName, PfEntries annot, Object value) {

        if (!(value instanceof Map)) {
            return true;
        }

        EntryValidator entryValidator = makeEntryValidator(annot.key(), annot.value());

        return verMap(result, fieldName, entryValidator, value);
    }

    /**
     * Invokes the value's {@link Validated#validate(String) validate()} method, if the
     * value is of type {@link Validated}.
     */
    @Override
    public boolean verCascade(BeanValidationResult result, String fieldName, Object value) {
        if (value instanceof Validated) {
            ValidationResult result2 = ((Validated) value).validate(fieldName);
            result.addResult(result2);
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
    public boolean verKey(BeanValidationResult result, String fieldName, Key annot, Object value) {
        if (!(value instanceof PfKey)) {
            return true;
        }

        PfKey pfkey = (PfKey) value;
        if (annot.keyNotNull() && pfkey.isNullKey()) {
            result.addResult(new ObjectValidationResult(fieldName, xlate(pfkey), ValidationStatus.INVALID,
                            Validated.IS_A_NULL_KEY));
            return false;
        }

        if (!(pfkey instanceof PfKeyImpl)) {
            return true;
        }

        BeanValidationResult result2 = new BeanValidationResult(fieldName, value);

        PfKeyImpl keyimpl = (PfKeyImpl) pfkey;

        if (annot.nameNotNull() && keyimpl.isNullName()) {
            result2.addResult(new ObjectValidationResult("name", pfkey.getName(), ValidationStatus.INVALID,
                            Validated.IS_NULL));
        }

        if (annot.versionNotNull() && keyimpl.isNullVersion()) {
            result2.addResult(new ObjectValidationResult("version", pfkey.getVersion(), ValidationStatus.INVALID,
                            Validated.IS_NULL));
        }

        if (!result2.isClean()) {
            result.addResult(result2);
        }

        return result2.isValid();
    }

    @Override
    public Object xlate(Object value) {
        return (value instanceof PfKey ? ((PfKey) value).getId() : value);
    }
}
