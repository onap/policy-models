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

import lombok.Builder;
import lombok.Data;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;

/**
 * Parameters used by Operators that connect to a server via DMaaP.
 */
@NotNull
@NotBlank
@Data
@Builder(toBuilder = true)
public class TopicParams {

    /**
     * Name of the target topic end point to which requests should be published.
     */
    private String target;

    /**
     * Source topic end point, from which to read responses.
     */
    private String source;

    /**
     * Amount of time, in seconds to wait for the response, where zero indicates that it
     * should wait forever. The default is zero.
     */
    @Min(0)
    @Builder.Default
    private long timeoutSec = 0;

    /**
     * Validates both the publisher and the subscriber parameters.
     *
     * @param resultName name of the result
     *
     * @return the validation result
     */
    public ValidationResult validate(String resultName) {
        return new BeanValidator().validateTop(resultName, this);
    }
}
