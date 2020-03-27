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

import lombok.Data;
import lombok.experimental.SuperBuilder;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;

/**
 * Parameters used by Operators that connect to a server via HTTP.
 */
@NotNull
@Data
@SuperBuilder(toBuilder = true)
public class HttpParams {

    /**
     * Name of the HttpClient, as found in the HttpClientFactory.
     */
    @NotBlank
    private String clientName;

    /**
     * URI path.
     */
    private String path;

    /**
     * Amount of time, in seconds, to wait for the HTTP request to complete.
     * <p/>
     * Note: this should NOT have a default value, as it receives its default value from
     * {@link HttpActorParams}.
     */
    @Min(1)
    private int timeoutSec;


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
