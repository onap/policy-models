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

package org.onap.policy.controlloop.actorserviceprovider.parameters;

import lombok.Getter;
import org.onap.policy.common.parameters.ValidationResult;

/**
 * Parameter runtime exception, with an associated validation result. This is used to
 * throw an exception while passing a validation result up the chain.
 * <p/>
 * Note: the validation result is <i>not</i> included in the exception message.
 */
public class ParameterValidationRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    @Getter
    private final transient ValidationResult result;


    public ParameterValidationRuntimeException(ValidationResult result) {
        this.result = result;
    }

    public ParameterValidationRuntimeException(String message, ValidationResult result) {
        super(message);
        this.result = result;
    }

    public ParameterValidationRuntimeException(Throwable cause, ValidationResult result) {
        super(cause);
        this.result = result;
    }

    public ParameterValidationRuntimeException(String message, Throwable cause, ValidationResult result) {
        super(message, cause);
        this.result = result;
    }
}
