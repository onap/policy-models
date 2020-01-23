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
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;

/**
 * Parameters used by OperationManagers that connect to a server via DMaaP.
 */
@Data
public class TopicParams {

    private static final String SOURCE_FIELD = "source";
    private static final String TARGET_FIELD = "target";
    private static final String TIMEOUT_SEC_FIELD = "timeoutSec";

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
     * should wait forever.
     */
    private long timeoutSec;

    /**
     * Validates the publisher parameters.
     *
     * @param resultName name of the result
     *
     * @return the validation result
     */
    public ValidationResult validatePublisher(String resultName) {
        BeanValidationResult result = new BeanValidationResult(resultName, this);
        result.validateNotNull(TARGET_FIELD, target);
        result.addResult(validateTimeout());

        return result;
    }

    /**
     * Validates the subscriber parameters.
     *
     * @param resultName name of the result
     *
     * @return the validation result
     */
    public ValidationResult validateSubscriber(String resultName) {
        BeanValidationResult result = new BeanValidationResult(resultName, this);
        result.validateNotNull(SOURCE_FIELD, source);
        result.addResult(validateTimeout());

        return result;
    }

    /**
     * Validates both the publisher and the subscriber parameters.
     *
     * @param resultName name of the result
     *
     * @return the validation result
     */
    public ValidationResult validate(String resultName) {
        BeanValidationResult result = new BeanValidationResult(resultName, this);
        result.validateNotNull(TARGET_FIELD, target);
        result.validateNotNull(SOURCE_FIELD, source);
        result.addResult(validateTimeout());

        return result;
    }

    private ValidationResult validateTimeout() {
        if (timeoutSec < 0) {
            return new ObjectValidationResult(TIMEOUT_SEC_FIELD, timeoutSec, ValidationStatus.INVALID, "is negative");
        }

        return null;
    }
}
