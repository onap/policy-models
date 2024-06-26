/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * Test of the {@link PdpGroupFilter} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class PdpGroupFilterTest {
    private static final String POLICY_TYPE3 = "policy.type.3";
    private static final String POLICY_TYPE2 = "policy.type.2";
    private static final String POLICY_TYPE1 = "policy.type.1";
    private static final String POLICY_TYPE0 = "policy.type.0";
    private static final String POLICY3 = "Policy3";
    private static final String POLICY2 = "Policy2";
    private static final String POLICY1 = "Policy1";
    private static final String POLICY0 = "Policy0";
    private static final String NON_EXISTANT = "Nonexistant";
    private static final String VERSION9 = "9.9.9";
    private static final String VERSION7 = "7.8.9";
    private static final String VERSION4 = "4.5.6";
    private static final String VERSION1 = "1.2.3";
    private static final String VERSION0 = "0.1.2";
    private List<PdpGroup> pdpGroupList;

    /**
     * Set up a PDP group list for filtering.
     *
     * @throws CoderException on JSON decoding errors
     */
    @BeforeEach
    void setupPdpGroupList() throws CoderException {
        String originalJson = ResourceUtils.getResourceAsString("testdata/PdpGroupsForFiltering.json");
        PdpGroups pdpGroups = new StandardCoder().decode(originalJson, PdpGroups.class);
        pdpGroupList = pdpGroups.getGroups();
    }

    @Test
    void testNullList() {
        PdpGroupFilter filter = PdpGroupFilter.builder().build();

        assertThatThrownBy(() -> {
            filter.filter(null);
        }).hasMessageMatching("originalList is marked .*ull but is null");
    }

    @Test
    void testFilterNothing() {
        PdpGroupFilter filter = PdpGroupFilter.builder().build();

        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertTrue(filteredList.containsAll(pdpGroupList));
    }

    @Test
    void testFilterName() {
        PdpGroupFilter filter = PdpGroupFilter.builder().name("PdpGroup0").build();
        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        filter = PdpGroupFilter.builder().name("PdpGroup1").build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        filter = PdpGroupFilter.builder().name("PdpGroup20").build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        filter = PdpGroupFilter.builder().build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(5, filteredList.size());

        filter = PdpGroupFilter.builder().name("PdpGroup0").build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        filter = PdpGroupFilter.builder().name("PdpGroup19").build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());
    }

    @Test
    void testFilterPdpGroupState() {
        PdpGroupFilter filter = PdpGroupFilter.builder().groupState(PdpState.ACTIVE).build();
        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        filter = PdpGroupFilter.builder().groupState(PdpState.PASSIVE).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());

        filter = PdpGroupFilter.builder().groupState(PdpState.TEST).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        filter = PdpGroupFilter.builder().groupState(PdpState.SAFE).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        filter = PdpGroupFilter.builder().groupState(PdpState.TERMINATED).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());
    }

    @Test
    void testFilterPdpType() {
        PdpGroupFilter filter = PdpGroupFilter.builder().pdpType("APEX").build();
        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertEquals(5, filteredList.size());

        filter = PdpGroupFilter.builder().pdpType("DROOLS").build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());

        filter = PdpGroupFilter.builder().pdpType("XACML").build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());
    }

    @Test
    void testFilterPdpState() {
        PdpGroupFilter filter = PdpGroupFilter.builder().pdpState(PdpState.ACTIVE).build();
        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertEquals(3, filteredList.size());

        filter = PdpGroupFilter.builder().pdpState(PdpState.PASSIVE).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(3, filteredList.size());

        filter = PdpGroupFilter.builder().pdpState(PdpState.SAFE).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());

        filter = PdpGroupFilter.builder().pdpState(PdpState.TEST).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());
    }

    @Test
    void testFilterPolicyType() {
        List<ToscaConceptIdentifier> identifierList = new ArrayList<>();

        identifierList.add(new ToscaConceptIdentifier(NON_EXISTANT, VERSION1));
        PdpGroupFilter filter =
                PdpGroupFilter.builder().policyTypeList(identifierList).build();
        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());
        identifierList.clear();

        // don't match wild cards
        identifierList.add(new ToscaConceptIdentifier(NON_EXISTANT, VERSION1));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        final List<PdpGroup> wildCards =
                        pdpGroupList.stream().map(this::makeWildCardPolicyTypes).collect(Collectors.toList());
        filteredList = filter.filter(wildCards);
        assertEquals(0, filteredList.size());
        identifierList.clear();

        // match wild cards
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE0, VERSION1));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        filteredList = filter.filter(wildCards);
        assertEquals(4, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE0, VERSION1));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(4, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE1, VERSION4));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(4, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE2, VERSION7));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE3, VERSION0));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaConceptIdentifier(NON_EXISTANT, VERSION1));
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE0, VERSION9));
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE0, VERSION1));
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE1, VERSION4));
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE2, VERSION7));
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE3, VERSION0));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(5, filteredList.size());

        filter = PdpGroupFilter.builder().policyTypeList(identifierList).matchPolicyTypesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE0, VERSION1));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).matchPolicyTypesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE0, VERSION1));
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE1, VERSION4));
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE2, VERSION7));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).matchPolicyTypesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE0, VERSION1));
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE1, VERSION4));
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE3, VERSION0));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).matchPolicyTypesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE1, VERSION4));
        identifierList.add(new ToscaConceptIdentifier(POLICY_TYPE3, VERSION0));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).matchPolicyTypesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());
    }

    /**
     * Makes a clone of a PdpGroup, changing all occurrences of supported policy type,
     * "policy.type.0", to a wild card type, "policy.type.*".
     *
     * @param group group to be cloned
     * @return a new PdpGroup containing wild card policy types
     */
    private PdpGroup makeWildCardPolicyTypes(PdpGroup group) {
        PdpGroup newGroup = new PdpGroup(group);

        for (PdpSubGroup subgroup : newGroup.getPdpSubgroups()) {
            for (ToscaConceptIdentifier subType : subgroup.getSupportedPolicyTypes()) {
                if (POLICY_TYPE0.equals(subType.getName())) {
                    subType.setName("policy.type.*");
                }
            }
        }

        return newGroup;
    }

    @Test
    void testFilterPolicy() {
        List<ToscaConceptIdentifier> identifierList = new ArrayList<>();

        identifierList.add(new ToscaConceptIdentifier(NON_EXISTANT, VERSION1));
        PdpGroupFilter filter =
                PdpGroupFilter.builder().policyList(identifierList).build();
        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaConceptIdentifier(POLICY0, VERSION9));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaConceptIdentifier(POLICY0, VERSION4));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(4, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaConceptIdentifier(POLICY1, VERSION4));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaConceptIdentifier(POLICY2, VERSION4));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaConceptIdentifier(POLICY3, VERSION1));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaConceptIdentifier(NON_EXISTANT, VERSION1));
        identifierList.add(new ToscaConceptIdentifier(POLICY0, VERSION9));
        identifierList.add(new ToscaConceptIdentifier(POLICY0, VERSION4));
        identifierList.add(new ToscaConceptIdentifier(POLICY1, VERSION4));
        identifierList.add(new ToscaConceptIdentifier(POLICY2, VERSION4));
        identifierList.add(new ToscaConceptIdentifier(POLICY3, VERSION1));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(5, filteredList.size());

        filter = PdpGroupFilter.builder().policyList(identifierList).matchPoliciesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaConceptIdentifier(POLICY0, VERSION4));
        filter = PdpGroupFilter.builder().policyList(identifierList).matchPoliciesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(3, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaConceptIdentifier(POLICY0, VERSION4));
        identifierList.add(new ToscaConceptIdentifier(POLICY1, VERSION4));
        filter = PdpGroupFilter.builder().policyList(identifierList).matchPoliciesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaConceptIdentifier(POLICY2, VERSION4));
        filter = PdpGroupFilter.builder().policyList(identifierList).matchPoliciesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaConceptIdentifier(POLICY2, VERSION4));
        identifierList.add(new ToscaConceptIdentifier(POLICY3, VERSION1));
        filter = PdpGroupFilter.builder().policyList(identifierList).matchPoliciesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());
    }
}
