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

package org.onap.policy.models.simulators;

import lombok.Getter;
import org.onap.policy.common.endpoints.parameters.RestServerParameters;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;

@Getter
public class ClassRestServerParameters extends RestServerParameters {
    private String providerClass;

    /**
     * Validates the parameters.
     *
     * @param containerName name of the parameter container
     * @return the validation result
     */
    public ValidationResult validate(String containerName) {
        // not using a BeanValidator because username and password are not required
        if (providerClass == null) {
            return new ObjectValidationResult("providerClass", providerClass, ValidationStatus.INVALID, "is null");
        }

        return new ObjectValidationResult("providerClass", providerClass);
    }
}
