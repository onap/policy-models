/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2021 Nordix Foundation.
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
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * A deployment (i.e., set of policies) for all PDPs of the same pdp type running within a
 * particular domain.
 */
@Data
@NoArgsConstructor
public class DeploymentSubGroup {

    public enum Action {
        POST,       // all listed policies are to be added
        DELETE,     // all listed policies are to be deleted
        PATCH       // update the deployment so that the policies match exactly
    }

    private String pdpType;
    private Action action;
    private List<ToscaConceptIdentifier> policies;

    /**
     * Constructs the object, making a deep copy from the source.
     *
     * @param source source from which to copy fields
     */
    public DeploymentSubGroup(@NonNull final DeploymentSubGroup source) {
        this.pdpType = source.pdpType;
        this.action = source.action;
        this.policies = PfUtils.mapList(source.policies, ToscaConceptIdentifier::new, new ArrayList<>(0));
    }

    /**
     * Validates that appropriate fields are populated for an incoming call to the PAP
     * REST API.
     *
     * @return the validation result
     */
    public ValidationResult validatePapRest() {
        var result = new BeanValidationResult("group", this);

        result.validateNotNull("pdpType", pdpType);
        result.validateNotNull("action", action);
        result.validateNotNullList("policies", policies, ToscaConceptIdentifier::validatePapRest);

        return result;
    }
}
