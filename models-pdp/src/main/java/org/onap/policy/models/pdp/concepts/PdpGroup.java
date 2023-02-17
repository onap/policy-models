/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property.
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.onap.policy.common.gson.annotation.GsonJsonIgnore;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.models.base.PfKey;
import org.onap.policy.models.base.PfNameVersion;
import org.onap.policy.models.base.PfUtils;
import org.onap.policy.models.pdp.enums.PdpState;

/**
 * Class to represent a PDPGroup, which groups multiple PDPSubGroup entities together for a particular domain.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
@Data
@NoArgsConstructor
public class PdpGroup implements PfNameVersion, Comparable<PdpGroup> {
    private static final String SUBGROUP_FIELD = "pdpSubgroups";

    private String name;
    private String description;
    private PdpState pdpGroupState;
    private Map<String, String> properties;
    private List<PdpSubGroup> pdpSubgroups;

    /*
     * Note: removed "@NotNull" annotation from the constructor argument, because it cannot be covered by a junit test,
     * as the superclass does the check and throws an exception first.
     */

    /**
     * Constructs the object, making a deep copy from the source.
     *
     * @param source source from which to copy fields
     */
    public PdpGroup(PdpGroup source) {
        this.name = source.name;
        this.description = source.description;
        this.pdpGroupState = source.pdpGroupState;
        this.properties = (source.properties == null ? null : new LinkedHashMap<>(source.properties));
        this.pdpSubgroups = PfUtils.mapList(source.pdpSubgroups, PdpSubGroup::new, new ArrayList<>(0));
    }

    @Override
    public int compareTo(final PdpGroup other) {
        return compareNameVersion(this, other);
    }

    /**
     * Validates that appropriate fields are populated for an incoming call to the PAP REST API.
     *
     * @param updateGroupFlow if the operation is pdp group update
     * @return the validation result
     */
    public ValidationResult validatePapRest(boolean updateGroupFlow) {
        var result = new BeanValidationResult("group", this);

        /*
         * Don't care about state, because we override it. Ok if description is null.
         */

        result.validateNotNull("name", name);
        result.validateNotNullList(SUBGROUP_FIELD, pdpSubgroups,
            (PdpSubGroup pdpSubGroup) -> pdpSubGroup.validatePapRest(updateGroupFlow));

        if (pdpSubgroups != null && pdpSubgroups.isEmpty()) {
            result.addResult(SUBGROUP_FIELD, pdpSubgroups, ValidationStatus.INVALID, "is empty");
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
        if (pdpSubgroups == null || !result.isValid()) {
            return;
        }

        // verify that the same subgroup doesn't appear more than once
        List<String> pdpTypes = pdpSubgroups.stream().map(PdpSubGroup::getPdpType).collect(Collectors.toList());
        if (pdpSubgroups.size() == new HashSet<>(pdpTypes).size()) {
            return;
        }

        // different sizes implies duplicates
        result.addResult(SUBGROUP_FIELD, pdpTypes, ValidationStatus.INVALID, "duplicate subgroups");
    }

    @Override
    @GsonJsonIgnore
    public String getVersion() {
        // We need to pass a version for keying in the database
        return PfKey.NULL_KEY_VERSION;
    }

    @Override
    @GsonJsonIgnore
    public void setVersion(String version) {
        // Just ignore any version that is set
    }
}
