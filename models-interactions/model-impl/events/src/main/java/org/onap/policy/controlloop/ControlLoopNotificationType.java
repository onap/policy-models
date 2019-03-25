/*-
 * ============LICENSE_START=======================================================
 * controlloop
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.controlloop;

public enum ControlLoopNotificationType {
    ACTIVE("ACTIVE"), REJECTED("REJECTED"), OPERATION("OPERATION"), OPERATION_SUCCESS(
            "OPERATION: SUCCESS"), OPERATION_FAILURE("OPERATION: FAILURE"), FINAL_FAILURE(
                    "FINAL: FAILURE"), FINAL_SUCCESS("FINAL: SUCCESS"), FINAL_OPENLOOP("FINAL: OPENLOOP");

    private String type;

    private ControlLoopNotificationType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return this.type;
    }

    /**
     * Convert a String type to a ControlLoopNotificationType.
     * 
     * @param type the String type
     * @return the ControlLoopNotificationType
     */
    public static ControlLoopNotificationType toType(String type) {
        if (ACTIVE.toString().equals(type)) {
            return ACTIVE;
        }
        if (REJECTED.toString().equals(type)) {
            return REJECTED;
        }
        if (OPERATION.toString().equals(type)) {
            return OPERATION;
        }
        if (OPERATION_SUCCESS.toString().equals(type)) {
            return OPERATION_SUCCESS;
        }
        if (OPERATION_FAILURE.toString().equals(type)) {
            return OPERATION_FAILURE;
        }
        if (FINAL_FAILURE.toString().equals(type)) {
            return FINAL_FAILURE;
        }
        if (FINAL_SUCCESS.toString().equals(type)) {
            return FINAL_SUCCESS;
        }
        if (FINAL_OPENLOOP.toString().equals(type)) {
            return FINAL_OPENLOOP;
        }
        return null;
    }

}
