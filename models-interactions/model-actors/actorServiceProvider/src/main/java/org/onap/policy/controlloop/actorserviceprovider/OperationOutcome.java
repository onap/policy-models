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

package org.onap.policy.controlloop.actorserviceprovider;

import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outcome of an operation.
 */
@Data
@AllArgsConstructor
public class OperationOutcome {
    private static final Logger logger = LoggerFactory.getLogger(OperationOutcome.class);

    public static final String SUCCESS_MSG = "Success";
    public static final String FAILED_MSG = "Failed";

    private PolicyResult status;
    private String message;

    /**
     * Constructs the object, using a default message based on the status.
     *
     * @param status result status
     */
    public OperationOutcome(PolicyResult status) {
        this.status = status;
        this.message = (status == PolicyResult.SUCCESS ? SUCCESS_MSG : FAILED_MSG);
    }

    /**
     * Gets a function that maps an exception to an outcome. This is intended for use in
     * methods of CompletableFuture.
     *
     * @param actor actor name
     * @param operation operation name
     *
     * @return a function that maps an exception to an outcome
     */
    public Function<Throwable, OperationOutcome> fromException(String actor, String operation) {
        return thrown -> {
            logger.warn("operation {}.{} threw an exception", actor, operation, thrown);

            // TODO FAILURE_GUARD
            PolicyResult result = (thrown.getCause() instanceof TimeoutException ? PolicyResult.FAILURE_TIMEOUT
                            : PolicyResult.FAILURE_EXCEPTION);

            return new OperationOutcome(result, thrown.getMessage());
        };
    }
}
