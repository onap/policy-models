/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2020,2022 AT&T Intellectual Property. All rights reserved.
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
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * Filter class for searches for {@link PdpGroup} instances. If any fields are null, they are ignored.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
@Builder
@Data
public class PdpGroupFilter implements PfObjectFilter<PdpGroup> {
    // Name to find
    private String name;

    // State to find
    private PdpState groupState;

    // PDP type to find
    private String pdpType;

    // Set regular expressions on fields to match policy type names and versions
    private List<ToscaConceptIdentifier> policyTypeList;

    // If set, only PDP groups where policy types are matched exactly are returned
    @Builder.Default
    private boolean matchPolicyTypesExactly = false;

    // Set regular expressions on fields to match policy names and versions
    private List<ToscaConceptIdentifier> policyList;

    // If set, only PDP groups where policies are matched exactly are returned
    @Builder.Default
    private boolean matchPoliciesExactly = false;

    // If set, only PDP groups with PDPs in this state are returned
    private PdpState pdpState;

    @Override
    public List<PdpGroup> filter(@NonNull final List<PdpGroup> originalList) {

        // @formatter:off
        return originalList.stream()
                .filter(p -> filterString(p.getName(), name))
                .filter(p -> groupState == null || ObjectUtils.compare(p.getPdpGroupState(), groupState) == 0)
                .filter(p -> filterOnPdpType(p, pdpType))
                .filter(p -> filterOnPolicyTypeList(p, policyTypeList, matchPolicyTypesExactly))
                .filter(p -> filterOnPolicyList(p, policyList, matchPoliciesExactly))
                .filter(p -> filterOnPdpState(p, pdpState))
                .collect(Collectors.toList());
        // @formatter:on
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

        for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
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
     * @param typeFilter the policy type regular expressions to check for
     * @param matchPolicyTypesExactly if true, only PDP groups where policy types are matched exactly are returned
     * @return true if the filter should let this PDP group through
     */
    private boolean filterOnPolicyTypeList(final PdpGroup pdpGroup, final List<ToscaConceptIdentifier> typeFilter,
            final boolean matchPolicyTypesExactly) {
        if (typeFilter == null) {
            return true;
        }

        for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
            if (matchPolicyTypesExactly) {
                if (areListsIdentical(pdpSubGroup.getSupportedPolicyTypes(), typeFilter)) {
                    return true;
                }
            } else if (findSupportedPolicyType(pdpSubGroup.getSupportedPolicyTypes(), typeFilter)) {
                return true;
            }
        }

        return false;

    }

    /**
     * Find a single supported type.
     *
     * @param supportedPolicyTypes supported types
     * @param typeFilter the list of types, one of which we wish to find supported by
     *        the list we are searching
     * @return true if one element of the elements to find is supported by an element on
     *         the list we searched
     */
    private boolean findSupportedPolicyType(List<ToscaConceptIdentifier> supportedPolicyTypes,
                    List<ToscaConceptIdentifier> typeFilter) {
        for (ToscaConceptIdentifier supportedPolicyType : supportedPolicyTypes) {
            String supName = supportedPolicyType.getName();
            if (supName.endsWith(".*")) {
                String substr = supName.substring(0, supName.length() - 1);
                if (typeFilter.stream().anyMatch(type -> type.getName().startsWith(substr))) {
                    return true;
                }
            } else if (typeFilter.contains(supportedPolicyType)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Filter PDP groups on policy.
     *
     * @param pdpGroup the PDP group to check
     * @param policyFilter the policy regular expressions to check for
     * @param matchPoliciesExactly if true, only PDP groups where ps are matched exactly are returned
     * @return true if the filter should let this PDP group through
     */
    private boolean filterOnPolicyList(final PdpGroup pdpGroup, final List<ToscaConceptIdentifier> policyFilter,
            final boolean matchPoliciesExactly) {
        if (policyFilter == null) {
            return true;
        }

        for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
            if (matchPoliciesExactly) {
                if (areListsIdentical(pdpSubGroup.getPolicies(), policyFilter)) {
                    return true;
                }
            } else if (findSingleElement(pdpSubGroup.getPolicies(), policyFilter)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Filter PDP groups on PDP state.
     *
     * @param pdpGroup the PDP group to check
     * @param pdpState the state to check for
     * @return true if the filter should let this PDP group through
     */
    private boolean filterOnPdpState(final PdpGroup pdpGroup, final PdpState pdpState) {
        if (pdpState == null) {
            return true;
        }

        for (PdpSubGroup pdpSubGroup : pdpGroup.getPdpSubgroups()) {
            for (Pdp pdp : pdpSubGroup.getPdpInstances()) {
                if (pdpState.equals(pdp.getPdpState())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if two lists have identical content.
     *
     * @param leftList the left list
     * @param rightList the right list
     * @return true if the lists are identical
     */
    private <T> boolean areListsIdentical(final List<T> leftList, List<T> rightList) {
        return leftList.equals(rightList);
    }

    /**
     * Find a single element of a list in a list.
     *
     * @param listToSearch the list in which we are searching for elements
     * @param listOfElementsToFind the list of elements, one of which we wish to find on the list we are searching
     * @return true if one element of the elements to find is found on the list we searched
     */
    private <T> boolean findSingleElement(final List<T> listToSearch, List<T> listOfElementsToFind) {
        for (Object elementToFind : listOfElementsToFind) {
            if (listToSearch.contains(elementToFind)) {
                return true;
            }
        }

        return false;
    }
}
