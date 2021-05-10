/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2021 AT&T Intellectual Property.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.pdp.concepts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.pdp.concepts.DeploymentSubGroup.Action;

/**
 * Batch modification of a deployment group, which groups multiple DeploymentSubGroup
 * entities together for a particular domain.
 */
@Data
@NoArgsConstructor
public class DeploymentGroup {
    private static final String SUBGROUP_FIELD = "deploymentSubgroups";

    private String name;
    private List<DeploymentSubGroup> deploymentSubgroups;

    /**
     * Constructs the object, making a deep copy from the source.
     *
     * @param source source from which to copy fields
     */
    public DeploymentGroup(@NonNull DeploymentGroup source) {
        this.name = source.name;
        this.deploymentSubgroups =
                        PfUtils.mapList(source.deploymentSubgroups, DeploymentSubGroup::new, new ArrayList<>(0));
    }

    /**
     * Validates that appropriate fields are populated for an incoming call to the PAP
     * REST API.
     *
     * @return the validation result
     */
    public ValidationResult validatePapRest() {
        var result = new BeanValidationResult("group", this);

        result.validateNotNull("name", name);
        result.validateNotNullList(SUBGROUP_FIELD, deploymentSubgroups, DeploymentSubGroup::validatePapRest);

        if (deploymentSubgroups != null && deploymentSubgroups.isEmpty()) {
            result.addResult(SUBGROUP_FIELD, deploymentSubgroups, ValidationStatus.INVALID, "is empty");
        }

        checkDuplicateSubgroups(result);

        return result;
    }

    /**
     * Checks for duplicate subgroups.
     *
     * @param result where to place validation results
     */
    private void checkDuplicateSubgroups(BeanValidationResult result) {
        if (deploymentSubgroups == null || !result.isValid()) {
            return;
        }

        /*
         * Verify that if a subgroup appears more than once, then the second appearance is
         * not a PATCH, as that would overwrite anything that has appeared before.
         */
        Map<String, Action> pdpType2action = new HashMap<>();

        for (DeploymentSubGroup subgrp : deploymentSubgroups) {
            var action = subgrp.getAction();

            pdpType2action.compute(subgrp.getPdpType(), (pdpType, curact) -> {

                if (curact != null && action == Action.PATCH) {
                    var subResult = new BeanValidationResult(pdpType, pdpType);
                    subResult.addResult("action", action, ValidationStatus.INVALID,
                                    "incompatible with previous action: " + curact);
                    var subResult2 = new BeanValidationResult(SUBGROUP_FIELD, subgrp);
                    subResult2.addResult(subResult);
                    result.addResult(subResult2);
                }

                return action;
            });
        }
    }
}
