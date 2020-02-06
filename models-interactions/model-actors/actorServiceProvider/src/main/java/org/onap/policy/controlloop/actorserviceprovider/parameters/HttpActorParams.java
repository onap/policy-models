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
import java.util.function.Function;
import lombok.Data;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.controlloop.actorserviceprovider.Util;

/**
 * Parameters used by Actors that connect to a server via HTTP. This contains the
 * parameters that are common to all of the operations. Only the path changes for each
 * operation, thus it includes a mapping from operation name to path.
 */
@Data
@NotNull
@NotBlank
public class HttpActorParams {

    /**
     * Name of the HttpClient, as found in the HttpClientFactory.
     */
    private String clientName;

    /**
     * Amount of time, in seconds to wait for the HTTP request to complete, where zero
     * indicates that it should wait forever. The default is zero.
     */
    @Min(0)
    private long timeoutSec = 0;

    /**
     * Maps the operation name to its URI path.
     */
    private Map<String, String> path;

    /**
     * Extracts a specific operation's parameters from "this".
     *
     * @param name name of the item containing "this"
     * @return a function to extract an operation's parameters from "this". Note: the
     *         returned function is not thread-safe
     */
    public Function<String, Map<String, Object>> makeOperationParameters(String name) {
        HttpParams subparams = HttpParams.builder().clientName(getClientName()).timeoutSec(getTimeoutSec()).build();

        return operation -> {
            String subpath = path.get(operation);
            if (subpath == null) {
                return null;
            }

            subparams.setPath(subpath);
            return Util.translateToMap(name + "." + operation, subparams);
        };
    }

    /**
     * Validates the parameters.
     *
     * @param name name of the object containing these parameters
     * @return "this"
     * @throws IllegalArgumentException if the parameters are invalid
     */
    public HttpActorParams doValidation(String name) {
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

        result.validateMap("path", path, (result2, entry) -> result2.validateNotNull(entry.getKey(), entry.getValue()));

        return result;
    }
}
