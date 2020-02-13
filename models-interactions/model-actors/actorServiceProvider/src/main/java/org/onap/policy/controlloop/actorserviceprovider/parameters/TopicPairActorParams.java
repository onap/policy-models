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
import lombok.Builder;
import lombok.Data;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;

/**
 * Parameters used by Actors whose Operators use a pair of Topics, one to publish requests
 * and the other to receive responses.
 */
@NotNull
@NotBlank
@Data
@Builder
public class TopicPairActorParams {

    /**
     * This contains the default parameters that are used when an operation doesn't
     * specify them. Note: each operation to be used must still have an entry in
     * {@link #operation}, even if it's empty. Otherwise, the given operation will not be
     * started.
     */
    private TopicPairParams defaults;

    /**
     * Maps an operation name to its individual parameters.
     */
    private Map<String, Map<String, Object>> operation;


    /**
     * Validates the parameters.
     *
     * @param name name of the object containing these parameters
     * @return "this"
     * @throws IllegalArgumentException if the parameters are invalid
     */
    public TopicPairActorParams doValidation(String name) {
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
        BeanValidationResult result = new BeanValidator().validateTop(resultName, this);

        if (defaults != null) {
            result.addResult(defaults.validate("defaults"));
        }

        // @formatter:off
        result.validateMap("operation", operation,
            (result2, entry) -> result2.validateNotNull(entry.getKey(), entry.getValue()));
        // @formatter:on

        return result;
    }
}
