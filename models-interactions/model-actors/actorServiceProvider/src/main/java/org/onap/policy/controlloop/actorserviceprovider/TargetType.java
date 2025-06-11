/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2025 OpenInfra Foundation Europe. All rights reserved.
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

public enum TargetType {
    VM("VM"), PNF("PNF"), VNF("VNF"), VFMODULE("VFMODULE");

    private final String target;

    TargetType(String targetType) {
        this.target = targetType;
    }

    @Override
    public String toString() {
        return this.target;
    }

    /**
     * Converts a string to TargetType object if it matches.
     *
     * @param targetType String
     * @return TargetType object
     */
    public static TargetType toTargetType(String targetType) {
        if (targetType == null) {
            return null;
        }
        if (targetType.equalsIgnoreCase(VM.toString())) {
            return VM;
        }
        if (targetType.equalsIgnoreCase(PNF.toString())) {
            return PNF;
        }
        if (targetType.equalsIgnoreCase(VNF.toString())) {
            return VNF;
        }
        if (targetType.equalsIgnoreCase(VFMODULE.toString())) {
            return VFMODULE;
        }

        return null;
    }
}
