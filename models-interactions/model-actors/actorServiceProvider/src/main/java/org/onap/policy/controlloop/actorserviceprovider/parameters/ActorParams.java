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

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.controlloop.actorserviceprovider.Util;

/**
 * Superclass for Actor parameters that have default values in "this" object, and
 * operation-specific values in {@link #operations}.
 */
@Getter
@Setter
@EqualsAndHashCode
public class ActorParams {
    /**
     * Name of the "operations" field contained within actor parameters.
     */
    public static final String OPERATIONS_FIELD = "operations";

    /**
     * Maps the operation name to its parameters.
     */
    @NotNull
    protected Map<String, Map<String, Object>> operations;


    /**
     * Extracts a specific operation's parameters from "this".
     *
     * @param name name of the item containing "this"
     * @return a function to extract an operation's parameters from "this". Note: the
     *         returned function is not thread-safe
     */
    public Function<String, Map<String, Object>> makeOperationParameters(String name) {

        Map<String, Object> defaultParams = Util.translateToMap(name, this);
        defaultParams.remove(OPERATIONS_FIELD);

        return operationName -> {
            Map<String, Object> specificParams = operations.get(operationName);
            if (specificParams == null) {
                return null;
            }

            // start with a copy of defaults and overlay with specific
            Map<String, Object> subparams = new TreeMap<>(defaultParams);
            subparams.putAll(specificParams);

            return Util.translateToMap(name + "." + operationName, subparams);
        };
    }

    /**
     * Validates the parameters.
     *
     * @param name name of the object containing these parameters
     * @return "this"
     * @throws IllegalArgumentException if the parameters are invalid
     */
    public ActorParams doValidation(String name) {
        ValidationResult result = validate(name);
        if (!result.isValid()) {
            throw new ParameterValidationRuntimeException("invalid parameters", result);
        }

        return this;
    }

    /**
     * Validates the parameters.
     *
     * @param resultName name of the result
     *
     * @return the validation result
     */
    public ValidationResult validate(String resultName) {
        return new BeanValidator().validateTop(resultName, this);
    }
}
