/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import java.util.List;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import org.apache.commons.lang3.ObjectUtils;
import org.onap.policy.models.base.PfObjectFilter;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;

/**
 * Filter class for searches for {@link PdpGroup} instances. If any fields are null, they are ignored.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Builder
@Data
public class PdpGroupFilter implements PfObjectFilter<PdpGroup> {
    public static final String LATEST_VERSION = "LATEST";

    // Regular expression
    private String name;

    // Regular Expression, set to LATEST_VERRSION to get the latest version
    private String version;

    private PdpState groupState;

    // Regular expression
    private String pdpType;

    // Set regular expressions on fields to match policy type names and versions
    private ToscaPolicyTypeIdentifier policyType;

    // Set regular expressions on fields to match policy names and versions
    private ToscaPolicyIdentifier policy;

    private PdpState pdpState;

    @Override
    public List<PdpGroup> filter(@NonNull final List<PdpGroup> originalList) {

        // @formatter:off
        List<PdpGroup> returnList = originalList.stream()
                .filter(p -> filterOnRegexp(p.getName(),    name))
                .filter(p -> version.equals(LATEST_VERSION) || filterOnRegexp(p.getVersion(), version))
                .filter(p -> ObjectUtils.compare(p.getPdpGroupState(), groupState) == 0)
                .filter(p -> filterOnPdpType(p, pdpType))
                .filter(p -> filterOnPolicyType(p, policyType))
                .filter(p -> filterOnPolicy(p, policy))
                .filter(p -> filterOnPdpState(p, pdpState))
                .collect(Collectors.toList());
        // @formatter:off

        if (LATEST_VERSION.equals(version)) {
            returnList = this.latestVersionFilter(returnList);
        }

        return returnList;
    }

    /**
     * Filter PDP groups on PDP type.
     *
     * @param pdpGroup the PDP group to check
     * @param pdpType the PDP type to check for
     * @return true if the filter should let this PDP group through
     */
    private boolean filterOnPdpType(final PdpGroup pdpGroup, final String pdpType) {
        if (pdpType == null) {
            return true;
        }

        for (PdpSubGroup pdpSubGroup: pdpGroup.getPdpSubgroups()) {
            if (pdpSubGroup.getPdpType().equals(pdpType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Filter PDP groups on policy type.
     *
     * @param pdpGroup the PDP group to check
     * @param policyTypeFilter the policy type regular expressions to check for
     * @return true if the filter should let this PDP group through
     */
    private boolean filterOnPolicyType(final PdpGroup pdpGroup, final ToscaPolicyTypeIdentifier policyTypeFiler) {
        if (policyTypeFiler == null) {
            return true;
        }

        for (PdpSubGroup pdpSubGroup: pdpGroup.getPdpSubgroups()) {
            for (ToscaPolicyTypeIdentifier foundPolicyType : pdpSubGroup.getSupportedPolicyTypes()) {
                if (foundPolicyType.getName().matches(policyTypeFiler.getName())
                        && foundPolicyType.getVersion().matches(policyTypeFiler.getVersion())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Filter PDP groups on policy.
     *
     * @param pdpGroup the PDP group to check
     * @param policyFilter the policy regular expressions to check for
     * @return true if the filter should let this PDP group through
     */
    private boolean filterOnPolicy(final PdpGroup pdpGroup, final ToscaPolicyIdentifier policyFiler) {
        if (policyFiler == null) {
            return true;
        }

        for (PdpSubGroup pdpSubGroup: pdpGroup.getPdpSubgroups()) {
            for (ToscaPolicyIdentifier foundPolicy : pdpSubGroup.getPolicies()) {
                if (foundPolicy.getName().matches(policyFiler.getName())
                        && foundPolicy.getVersion().matches(policyFiler.getVersion())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Filter PDP groups on PDP state.
     *
     * @param pdpGroup the PDP group to check
     * @param policyFilter the policy regular expressions to check for
     * @return true if the filter should let this PDP group through
     */
    private boolean filterOnPdpState(final PdpGroup pdpGroup, final PdpState pdpState) {
        if (pdpState == null) {
            return true;
        }

        for (PdpSubGroup pdpSubGroup: pdpGroup.getPdpSubgroups()) {
            for (Pdp pdp : pdpSubGroup.getPdpInstances()) {
                if (pdpState.equals(pdp.getPdpState())) {
                    return true;
                }
            }
        }

        return false;
    }
}
