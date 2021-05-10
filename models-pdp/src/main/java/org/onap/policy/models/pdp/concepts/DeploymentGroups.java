/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.pdp.concepts;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;

/**
 * Batch modification of a deployment groups via the PDP Group deployment REST API.
 */
@Getter
@Setter
@ToString
public class DeploymentGroups {
    private static final String GROUPS_FIELD = "groups";

    private List<DeploymentGroup> groups;

    /**
     * Validates that appropriate fields are populated for an incoming call to the PAP
     * REST API.
     *
     * @return the validation result
     */
    public ValidationResult validatePapRest() {
        var result = new BeanValidationResult(GROUPS_FIELD, this);

        result.validateNotNullList(GROUPS_FIELD, groups, DeploymentGroup::validatePapRest);
        if (!result.isValid()) {
            return result;
        }

        // verify that the same group doesn't appear more than once
        Set<String> sawGroup = new HashSet<>();
        for (DeploymentGroup group : groups) {
            String name = group.getName();
            if (sawGroup.contains(name)) {
                return new ObjectValidationResult(GROUPS_FIELD, name, ValidationStatus.INVALID, "duplicate group name");

            } else {
                sawGroup.add(name);
            }
        }

        return result;
    }
}
