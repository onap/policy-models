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

public enum OperationResult {
    /**
     * Operation was successful.
     */
    SUCCESS("Success"),
    /**
     * Operation failed.
     */
    FAILURE("Failure"),
    /**
     * Operation failed due to maximum retries being met.
     */
    FAILURE_RETRIES("Failure_Retries"),
    /**
     * Operation failed due to timeout occurring.
     */
    FAILURE_TIMEOUT("Failure_Timeout"),
    /**
     * Operation failed due to an exception.
     */
    FAILURE_EXCEPTION("Failure_Exception"),
    /**
     * Operation failed since Guard did not permit.
     */
    FAILURE_GUARD("Failure_Guard")
    ;

    private String result;

    private OperationResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return this.result;
    }

    /**
     * Convert to a result.
     *
     * @param result result string
     * @return Result object
     */
    public static OperationResult toResult(String result) {
        if (result.equalsIgnoreCase(SUCCESS.toString())) {
            return SUCCESS;
        }
        if (result.equalsIgnoreCase(FAILURE.toString())) {
            return FAILURE;
        }
        if (result.equalsIgnoreCase(FAILURE_RETRIES.toString())) {
            return FAILURE_RETRIES;
        }
        if (result.equalsIgnoreCase(FAILURE_TIMEOUT.toString())) {
            return FAILURE_TIMEOUT;
        }
        if (result.equalsIgnoreCase(FAILURE_EXCEPTION.toString())) {
            return FAILURE_EXCEPTION;
        }
        if (result.equalsIgnoreCase(FAILURE_GUARD.toString())) {
            return FAILURE_GUARD;
        }
        return null;
    }
}
