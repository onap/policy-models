/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;

/**
 * Test of the {@link PdpGroupFilter} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PdpGroupFilterTest {
    private static final String TYPE3 = "policy.type.3";
    private static final String TYPE2 = "policy.type.2";
    private static final String TYPE1 = "policy.type.1";
    private static final String TYPE0 = "policy.type.0";
    private static final String POLICY3 = "Policy3";
    private static final String POLICY2 = "Policy2";
    private static final String POLICY1 = "Policy1";
    private static final String POLICY0 = "Policy0";
    private static final String NONEXISTANT_NAME = "Nonexistant";
    private static final String VERSION999 = "9.9.9";
    private static final String VERSION789 = "7.8.9";
    private static final String VERSION456 = "4.5.6";
    private static final String VERSION123 = "1.2.3";
    private static final String VERSION012 = "0.1.2";
    private List<PdpGroup> pdpGroupList;

    /**
     * Set up a PDP group list for filtering.
     *
     * @throws CoderException on JSON decoding errors
     */
    @Before
    public void setupPdpGroupList() throws CoderException {
        String originalJson = ResourceUtils.getResourceAsString("testdata/PdpGroupsForFiltering.json");
        PdpGroups pdpGroups = new StandardCoder().decode(originalJson, PdpGroups.class);
        pdpGroupList = pdpGroups.getGroups();
    }

    @Test
    public void testNullList() {
        PdpGroupFilter filter = PdpGroupFilter.builder().build();

        assertThatThrownBy(() -> {
            filter.filter(null);
        }).hasMessage("originalList is marked @NonNull but is null");
    }

    @Test
    public void testFilterNothing() {
        PdpGroupFilter filter = PdpGroupFilter.builder().build();

        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertTrue(filteredList.containsAll(pdpGroupList));
    }

    @Test
    public void testFilterName() {
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
    public void testFilterPdpGroupState() {
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
    public void testFilterPdpType() {
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
    public void testFilterPdpState() {
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
    public void testFilterPolicyType() {
        List<ToscaPolicyTypeIdentifier> identifierList = new ArrayList<>();

        identifierList.add(new ToscaPolicyTypeIdentifier(NONEXISTANT_NAME, VERSION123));
        PdpGroupFilter filter =
                PdpGroupFilter.builder().policyTypeList(identifierList).build();
        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE0, VERSION123));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(4, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE1, VERSION456));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(4, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE2, VERSION789));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE3, VERSION012));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaPolicyTypeIdentifier(NONEXISTANT_NAME, VERSION123));
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE0, VERSION999));
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE0, VERSION123));
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE1, VERSION456));
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE2, VERSION789));
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE3, VERSION012));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(5, filteredList.size());

        filter = PdpGroupFilter.builder().policyTypeList(identifierList).matchPolicyTypesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE0, VERSION123));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).matchPolicyTypesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE0, VERSION123));
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE1, VERSION456));
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE2, VERSION789));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).matchPolicyTypesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE0, VERSION123));
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE1, VERSION456));
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE3, VERSION012));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).matchPolicyTypesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE1, VERSION456));
        identifierList.add(new ToscaPolicyTypeIdentifier(TYPE3, VERSION012));
        filter = PdpGroupFilter.builder().policyTypeList(identifierList).matchPolicyTypesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());
    }

    @Test
    public void testFilterPolicy() {
        List<ToscaPolicyIdentifier> identifierList = new ArrayList<>();

        identifierList.add(new ToscaPolicyIdentifier(NONEXISTANT_NAME, VERSION123));
        PdpGroupFilter filter =
                PdpGroupFilter.builder().policyList(identifierList).build();
        List<PdpGroup> filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaPolicyIdentifier(POLICY0, VERSION999));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaPolicyIdentifier(POLICY0, VERSION456));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(4, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaPolicyIdentifier(POLICY1, VERSION456));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaPolicyIdentifier(POLICY2, VERSION456));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(2, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaPolicyIdentifier(POLICY3, VERSION123));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());
        identifierList.clear();

        identifierList.add(new ToscaPolicyIdentifier(NONEXISTANT_NAME, VERSION123));
        identifierList.add(new ToscaPolicyIdentifier(POLICY0, VERSION999));
        identifierList.add(new ToscaPolicyIdentifier(POLICY0, VERSION456));
        identifierList.add(new ToscaPolicyIdentifier(POLICY1, VERSION456));
        identifierList.add(new ToscaPolicyIdentifier(POLICY2, VERSION456));
        identifierList.add(new ToscaPolicyIdentifier(POLICY3, VERSION123));
        filter = PdpGroupFilter.builder().policyList(identifierList).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(5, filteredList.size());

        filter = PdpGroupFilter.builder().policyList(identifierList).matchPoliciesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(0, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaPolicyIdentifier(POLICY0, VERSION456));
        filter = PdpGroupFilter.builder().policyList(identifierList).matchPoliciesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(3, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaPolicyIdentifier(POLICY0, VERSION456));
        identifierList.add(new ToscaPolicyIdentifier(POLICY1, VERSION456));
        filter = PdpGroupFilter.builder().policyList(identifierList).matchPoliciesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaPolicyIdentifier(POLICY2, VERSION456));
        filter = PdpGroupFilter.builder().policyList(identifierList).matchPoliciesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());

        identifierList.clear();
        identifierList.add(new ToscaPolicyIdentifier(POLICY2, VERSION456));
        identifierList.add(new ToscaPolicyIdentifier(POLICY3, VERSION123));
        filter = PdpGroupFilter.builder().policyList(identifierList).matchPoliciesExactly(true).build();
        filteredList = filter.filter(pdpGroupList);
        assertEquals(1, filteredList.size());
    }
}
