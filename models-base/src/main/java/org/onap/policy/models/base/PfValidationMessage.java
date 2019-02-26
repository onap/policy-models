/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
 * A validation message is created for each validation observation observed during validation of a
 * concept. The message holds the key and the class of the concept on which the observation was made
 * as well as the type of observation and a message describing the observation.
 */
public class PfValidationMessage {
    private final PfKey observedKey;
    private ValidationResult validationResult = ValidationResult.VALID;
    private final String observedClass;
    private final String message;

    /**
     * Create an validation observation with the given fields.
     *
     * @param observedKey the key of the class on which the validation observation was made
     * @param observedClass the class on which the validation observation was made
     * @param validationResult the type of observation made
     * @param message a message describing the observation
     */
    public PfValidationMessage(final PfKey observedKey, final Class<?> observedClass,
            final ValidationResult validationResult, final String message) {
        Assertions.argumentNotNull(observedKey, "observedKey may not be null");
        Assertions.argumentNotNull(observedClass, "observedClass may not be null");
        Assertions.argumentNotNull(validationResult, "validationResult may not be null");
        Assertions.argumentNotNull(message, "message may not be null");

        this.observedKey = observedKey;
        this.observedClass = observedClass.getCanonicalName();
        this.validationResult = validationResult;
        this.message = message;
    }

    /**
     * Gets the key of the observation.
     *
     * @return the key of the observation
     */
    public PfKey getObservedKey() {
        return observedKey;
    }

    /**
     * Gets the observed class.
     *
     * @return the observed class
     */
    public String getObservedClass() {
        return observedClass;
    }

    /**
     * Gets the type of observation made.
     *
     * @return the type of observation made
     */
    public ValidationResult getValidationResult() {
        return validationResult;
    }

    /**
     * Get a description of the observation.
     *
     * @return the observation description
     */
    public String getMessage() {
        return message;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return observedKey.toString() + ':' + observedClass + ':' + validationResult.name() + ':' + message;
    }
}
