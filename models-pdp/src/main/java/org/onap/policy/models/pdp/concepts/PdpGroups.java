/*
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2024 Nordix Foundation.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ObjectValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;

/**
 * Request deploy or update a set of groups via the PDP Group deployment REST API.
 */
@Getter
@Setter
@ToString
public class PdpGroups {
    private static final String GROUPS_FIELD = "groups";

    private List<PdpGroup> groups;

    /**
     * Get the contents of this class as a list of PDP group maps.
     *
     * @return the PDP groups in a list of maps
     */
    public List<Map<String, PdpGroup>> toMapList() {
        final Map<String, PdpGroup> pdpGroupMap = new LinkedHashMap<>();
        for (PdpGroup pdpGroup : groups) {
            pdpGroupMap.put(pdpGroup.getName() + ':' + pdpGroup.getVersion(), pdpGroup);
        }

        return Collections.singletonList(pdpGroupMap);
    }

    /**
     * Validates that appropriate fields are populated for an incoming call to the PAP
     * REST API.
     *
     * @return the validation result
     */
    public ValidationResult validatePapRest() {
        ValidationResult result = new BeanValidationResult(GROUPS_FIELD, this);
        ((BeanValidationResult) result).validateNotNullList(GROUPS_FIELD, groups,
            (PdpGroup pdpGroup) -> pdpGroup.validatePapRest(false));
        if (!result.isValid()) {
            return result;
        }

        // verify that the same group doesn't appear more than once
        return checkForDuplicateGroups(result);
    }

    /**
     * Validates that there are no duplicate PdpGroups with the same name.
     *
     * @param result the validation result
     * @return the validation result
     */
    public ValidationResult checkForDuplicateGroups(ValidationResult result) {
        if (null == groups) {
            result.setResult(ValidationStatus.INVALID, "is null");
        } else {
            List<String> names = groups.stream().map(PdpGroup::getName).toList();
            if (groups.size() != new HashSet<>(names).size()) {
                result =
                    new ObjectValidationResult(GROUPS_FIELD, names, ValidationStatus.INVALID, "duplicate group names");
            }
        }
        return result;
    }
}
