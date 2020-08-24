/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
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

package org.onap.policy.models.provider.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * Test the database models provider implementation.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DatabasePolicyModelsProviderTest {
    private static final String NAME = "name";

    private static final String TEMPLATE_IS_NULL = "^serviceTemplate is marked .*on.*ull but is null$";

    private static final String SUBGROUP_IS_NULL = "^pdpSubGroup is marked .*on.*ull but is null$";

    private static final String GROUP_IS_NULL = "^pdpGroupName is marked .*on.*ull but is null$";

    private static final String NAME_IS_NULL = "^name is marked .*on.*ull but is null$";

    private static final String FILTER_IS_NULL = "^filter is marked .*on.*ull but is null$";

    private static final String GROUP = "group";

    private static final String VERSION_100 = "1.0.0";

    private static final Date TIMESTAMP = new Date();

    private static final String ORDER = "DESC";

    PolicyModelsProviderParameters parameters;

    /**
     * Initialize parameters.
     */
    @Before
    public void setupParameters() {
        parameters = new PolicyModelsProviderParameters();
        parameters.setDatabaseDriver("org.h2.Driver");
        parameters.setDatabaseUrl("jdbc:h2:mem:testdb");
        parameters.setDatabaseUser("policy");
        parameters.setDatabasePassword(Base64.getEncoder().encodeToString("P01icY".getBytes()));
        parameters.setPersistenceUnit("ToscaConceptTest");
    }

    @Test
    public void testInitAndClose() throws Exception {
        assertThatThrownBy(() -> {
            new DatabasePolicyModelsProviderImpl(null);
        }).hasMessageMatching("^parameters is marked .*on.*ull but is null$");

        PolicyModelsProvider databaseProvider =
            new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        parameters.setDatabaseUrl("jdbc://www.acmecorp.nonexist");

        databaseProvider.close();
        databaseProvider.init();

        databaseProvider.close();

        parameters.setDatabaseUrl("jdbc:h2:mem:testdb");

        parameters.setPersistenceUnit("WileECoyote");

        assertThatThrownBy(databaseProvider::init).hasMessageContaining("could not create Data Access Object (DAO)");

        parameters.setPersistenceUnit("ToscaConceptTest");

        databaseProvider.init();
        databaseProvider.close();

        assertThatThrownBy(() -> {
            databaseProvider.init();
            databaseProvider.init();
        }).hasMessage("provider is already initialized");

        databaseProvider.close();

        databaseProvider.close();
    }

    @Test
    public void testProviderMethodsNull() throws Exception {

        PolicyModelsProvider databaseProvider =
            new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        assertThatThrownBy(() -> {
            databaseProvider.getFilteredPolicyTypes(null);
        }).hasMessageMatching(FILTER_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.getFilteredPolicyTypeList(null);
        }).hasMessageMatching(FILTER_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.createPolicyTypes(null);
        }).hasMessageMatching(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.updatePolicyTypes(null);
        }).hasMessageMatching(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicyType(null, null);
        }).hasMessageMatching(NAME_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicyType("aaa", null);
        }).hasMessageMatching("^version is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicyType(null, "aaa");
        }).hasMessageMatching(NAME_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.getFilteredPolicies(null);
        }).hasMessageMatching(FILTER_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.getFilteredPolicyList(null);
        }).hasMessageMatching(FILTER_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.createPolicies(null);
        }).hasMessageMatching(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.updatePolicies(null);
        }).hasMessageMatching(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicy(null, null);
        }).hasMessageMatching(NAME_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicy(null, "aaa");
        }).hasMessageMatching(NAME_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicy("aaa", null);
        }).hasMessageMatching("^version is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            databaseProvider.getFilteredPdpGroups(null);
        }).hasMessageMatching(FILTER_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.createPdpGroups(null);
        }).hasMessageMatching("^pdpGroups is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpGroups(null);
        }).hasMessageMatching("^pdpGroups is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpSubGroup(null, null);
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpSubGroup(null, new PdpSubGroup());
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpSubGroup(NAME, null);
        }).hasMessageMatching(SUBGROUP_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpSubGroup(NAME, new PdpSubGroup());
        }).hasMessage("parameter \"localName\" is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, null, null);
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, null, new Pdp());
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, "sub", null);
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, "sub", new Pdp());
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(NAME, null, null);
        }).hasMessageMatching(SUBGROUP_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(NAME, null, new Pdp());
        }).hasMessageMatching(SUBGROUP_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(NAME, "sub", null);
        }).hasMessageMatching("^pdp is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(NAME, "sub", new Pdp());
        }).hasMessage("parameter \"localName\" is null");

        assertThatThrownBy(() -> {
            databaseProvider.deletePdpGroup(null);
        }).hasMessageMatching(NAME_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.getFilteredPdpStatistics(NAME, null, "sub", TIMESTAMP, TIMESTAMP, ORDER, 0);
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            databaseProvider.createPdpStatistics(null);
        }).hasMessageMatching("^pdpStatisticsList is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null);
        }).hasMessageMatching("^pdpStatisticsList is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            databaseProvider.deletePdpStatistics(null, TIMESTAMP);
        }).hasMessageMatching(NAME_IS_NULL);

        databaseProvider.close();
    }

    @Test
    public void testProviderMethodsNotInit() throws Exception {
        PolicyModelsProvider databaseProvider =
            new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        databaseProvider.close();

        assertThatThrownBy(() -> {
            databaseProvider.getPolicyTypes(NAME, "version");
        }).hasMessage("policy models provider is not initilaized");
    }

    @Test
    public void testProviderMethods() throws PfModelException {
        PolicyModelsProvider databaseProvider =
            new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        assertThatThrownBy(() -> databaseProvider.getPolicyTypes(NAME, VERSION_100))
            .hasMessage("service template not found in database");

        assertTrue(databaseProvider.getPolicyTypeList(NAME, VERSION_100).isEmpty());

        assertThatThrownBy(() -> databaseProvider.getFilteredPolicyTypes(ToscaPolicyTypeFilter.builder().build()))
            .hasMessage("service template not found in database");

        assertTrue(databaseProvider.getFilteredPolicyTypeList(ToscaPolicyTypeFilter.builder().build()).isEmpty());

        assertThatThrownBy(() -> {
            databaseProvider.createPolicyTypes(new ToscaServiceTemplate());
        }).hasMessage("no policy types specified on service template");

        assertThatThrownBy(() -> {
            databaseProvider.updatePolicyTypes(new ToscaServiceTemplate());
        }).hasMessage("no policy types specified on service template");

        assertThatThrownBy(() -> databaseProvider.deletePolicyType(NAME, VERSION_100))
            .hasMessage("service template not found in database");

        assertThatThrownBy(() -> databaseProvider.getPolicies(NAME, VERSION_100))
            .hasMessage("service template not found in database");

        assertTrue(databaseProvider.getPolicyList(NAME, VERSION_100).isEmpty());

        assertThatThrownBy(() -> databaseProvider.getFilteredPolicies(ToscaPolicyFilter.builder().build()))
            .hasMessage("service template not found in database");

        assertTrue(databaseProvider.getFilteredPolicyList(ToscaPolicyFilter.builder().build()).isEmpty());

        assertThatThrownBy(() -> {
            databaseProvider.createPolicies(new ToscaServiceTemplate());
        }).hasMessage("topology template not specified on service template");

        assertThatThrownBy(() -> {
            databaseProvider.updatePolicies(new ToscaServiceTemplate());
        }).hasMessage("topology template not specified on service template");

        assertThatThrownBy(() -> databaseProvider.deletePolicy("Policy", "0.0.0").getToscaTopologyTemplate())
            .hasMessage("service template not found in database");

        assertEquals(0, databaseProvider.getPdpGroups(NAME).size());
        assertEquals(0, databaseProvider.getFilteredPdpGroups(PdpGroupFilter.builder().build()).size());

        assertNotNull(databaseProvider.createPdpGroups(new ArrayList<>()));
        assertNotNull(databaseProvider.updatePdpGroups(new ArrayList<>()));

        PdpGroup pdpGroup = new PdpGroup();
        pdpGroup.setName(GROUP);
        pdpGroup.setVersion("1.2.3");
        pdpGroup.setPdpGroupState(PdpState.ACTIVE);
        pdpGroup.setPdpSubgroups(new ArrayList<>());
        List<PdpGroup> groupList = new ArrayList<>();
        groupList.add(pdpGroup);

        PdpSubGroup pdpSubGroup = new PdpSubGroup();
        pdpSubGroup.setPdpType("type");
        pdpSubGroup.setDesiredInstanceCount(123);
        pdpSubGroup.setSupportedPolicyTypes(new ArrayList<>());
        pdpSubGroup.getSupportedPolicyTypes().add(new ToscaPolicyTypeIdentifier("type", "7.8.9"));
        pdpGroup.getPdpSubgroups().add(pdpSubGroup);

        Pdp pdp = new Pdp();
        pdp.setInstanceId("type-0");
        pdp.setMessage("Hello");
        pdp.setPdpState(PdpState.ACTIVE);
        pdp.setHealthy(PdpHealthStatus.UNKNOWN);
        pdpSubGroup.setPdpInstances(new ArrayList<>());
        pdpSubGroup.getPdpInstances().add(pdp);

        PdpStatistics pdpStatistics = new PdpStatistics();
        pdpStatistics.setPdpInstanceId(NAME);
        pdpStatistics.setTimeStamp(new Date());
        pdpStatistics.setPdpGroupName(GROUP);
        pdpStatistics.setPdpSubGroupName("type");
        ArrayList<PdpStatistics> statisticsArrayList = new ArrayList<>();
        statisticsArrayList.add(pdpStatistics);

        assertEquals(123,
            databaseProvider.createPdpGroups(groupList).get(0).getPdpSubgroups().get(0).getDesiredInstanceCount());
        assertEquals(1, databaseProvider.getPdpGroups(GROUP).size());

        pdpSubGroup.setDesiredInstanceCount(234);
        databaseProvider.updatePdpSubGroup(GROUP, pdpSubGroup);
        assertEquals(234,
            databaseProvider.getPdpGroups(GROUP).get(0).getPdpSubgroups().get(0).getDesiredInstanceCount());

        assertEquals("Hello",
            databaseProvider.getPdpGroups(GROUP).get(0).getPdpSubgroups().get(0).getPdpInstances().get(0).getMessage());
        pdp.setMessage("Howdy");
        databaseProvider.updatePdp(GROUP, "type", pdp);
        assertEquals("Howdy",
            databaseProvider.getPdpGroups(GROUP).get(0).getPdpSubgroups().get(0).getPdpInstances().get(0).getMessage());

        assertThatThrownBy(() -> {
            databaseProvider.deletePdpGroup(NAME);
        }).hasMessage("delete of PDP group \"name:0.0.0\" failed, PDP group does not exist");

        assertEquals(pdpGroup.getName(), databaseProvider.deletePdpGroup(GROUP).getName());

        assertEquals(0, databaseProvider.getPdpStatistics(null, null).size());
        assertEquals(1, databaseProvider.createPdpStatistics(statisticsArrayList).size());
        assertEquals(1, databaseProvider.updatePdpStatistics(statisticsArrayList).size());

        assertEquals(NAME, databaseProvider.getPdpStatistics(null, null).get(0).getPdpInstanceId());
        assertEquals(NAME, databaseProvider.getFilteredPdpStatistics(null, GROUP, null, null, null, ORDER, 0).get(0)
            .getPdpInstanceId());
        assertEquals(0,
            databaseProvider.getFilteredPdpStatistics(null, GROUP, null, new Date(), null, ORDER, 0).size());
        assertEquals(NAME, databaseProvider.getFilteredPdpStatistics(null, GROUP, null, null, new Date(), ORDER, 0)
            .get(0).getPdpInstanceId());
        assertEquals(0,
            databaseProvider.getFilteredPdpStatistics(null, GROUP, null, new Date(), new Date(), ORDER, 0).size());

        assertEquals(NAME, databaseProvider.getFilteredPdpStatistics(NAME, GROUP, null, null, null, ORDER, 0).get(0)
            .getPdpInstanceId());
        assertEquals(0,
            databaseProvider.getFilteredPdpStatistics(NAME, GROUP, null, new Date(), new Date(), ORDER, 0).size());

        assertEquals(NAME, databaseProvider.getFilteredPdpStatistics(NAME, GROUP, "type", null, null, ORDER, 0).get(0)
            .getPdpInstanceId());
        assertEquals(0,
            databaseProvider.getFilteredPdpStatistics(NAME, GROUP, "type", new Date(), new Date(), ORDER, 0).size());

        assertEquals(NAME, databaseProvider.getFilteredPdpStatistics(NAME, GROUP, "type", null, null, ORDER, 1).get(0)
            .getPdpInstanceId());
        assertEquals(NAME, databaseProvider.getFilteredPdpStatistics(NAME, GROUP, "type", null, null, ORDER, 5).get(0)
            .getPdpInstanceId());
        assertEquals(0,
            databaseProvider.getFilteredPdpStatistics(NAME, GROUP, "type", new Date(), new Date(), ORDER, 5).size());

        assertEquals(NAME, databaseProvider.deletePdpStatistics(NAME, null).get(0).getPdpInstanceId());
        assertEquals(0, databaseProvider.getPdpStatistics(null, null).size());

        databaseProvider.close();
    }

    @Test
    public void testDeletePolicyDeployedInSubgroup() throws PfModelException {
        List<ToscaPolicyIdentifier> policies = new ArrayList<>();

        policies.add(new ToscaPolicyIdentifier("p0", "0.0.1"));
        policies.add(new ToscaPolicyIdentifier("p1", "0.0.1"));

        List<ToscaPolicyTypeIdentifier> supportedPolicyTypes = new ArrayList<>();
        supportedPolicyTypes.add(new ToscaPolicyTypeIdentifier("pt2", "0.0.1"));

        PdpSubGroup subGroup = new PdpSubGroup();
        subGroup.setPdpType("pdpType");
        subGroup.setSupportedPolicyTypes(supportedPolicyTypes);
        subGroup.setPolicies(policies);

        List<PdpSubGroup> pdpSubgroups = new ArrayList<>();
        pdpSubgroups.add(subGroup);

        PdpGroup pdpGroup = new PdpGroup();
        pdpGroup.setName("pdpGroup");
        pdpGroup.setPdpGroupState(PdpState.PASSIVE);
        pdpGroup.setPdpSubgroups(pdpSubgroups);

        List<PdpGroup> pdpGroups = new ArrayList<>();
        pdpGroups.add(pdpGroup);

        PolicyModelsProvider databaseProvider =
            new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        databaseProvider.createPdpGroups(pdpGroups);

        assertThatThrownBy(() -> databaseProvider.deletePolicy("p0", "0.0.1"))
            .hasMessageContaining("policy is in use, it is deployed in PDP group pdpGroup subgroup pdpType");

        assertThatThrownBy(() -> databaseProvider.deletePolicy("p3", "0.0.1"))
            .hasMessageContaining("service template not found in database");

        databaseProvider.close();
    }

    @Test
    public void testDeletePolicyTypeSupportedInSubgroup() throws PfModelException {
        List<ToscaPolicyTypeIdentifier> supportedPolicyTypes = new ArrayList<>();
        supportedPolicyTypes.add(new ToscaPolicyTypeIdentifier("pt1", "0.0.1"));
        supportedPolicyTypes.add(new ToscaPolicyTypeIdentifier("pt2", "0.0.1"));

        PdpSubGroup subGroup = new PdpSubGroup();
        subGroup.setPdpType("pdpType");
        subGroup.setSupportedPolicyTypes(supportedPolicyTypes);

        List<PdpSubGroup> pdpSubgroups = new ArrayList<>();
        pdpSubgroups.add(subGroup);

        PdpGroup pdpGroup = new PdpGroup();
        pdpGroup.setName("pdpGroup");
        pdpGroup.setPdpGroupState(PdpState.PASSIVE);
        pdpGroup.setPdpSubgroups(pdpSubgroups);

        List<PdpGroup> pdpGroups = new ArrayList<>();
        pdpGroups.add(pdpGroup);

        PolicyModelsProvider databaseProvider =
            new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        databaseProvider.createPdpGroups(pdpGroups);

        assertThatThrownBy(() -> databaseProvider.deletePolicyType("pt2", "0.0.1"))
            .hasMessageContaining("policy type is in use, it is referenced in PDP group pdpGroup subgroup pdpType");

        assertThatThrownBy(() -> databaseProvider.deletePolicyType("pt0", "0.0.1"))
            .hasMessageContaining("service template not found in database");

        databaseProvider.close();
    }
}
