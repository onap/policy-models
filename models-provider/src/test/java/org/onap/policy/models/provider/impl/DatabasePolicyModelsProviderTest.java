/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pap.concepts.PolicyAudit;
import org.onap.policy.models.pap.concepts.PolicyAudit.AuditAction;
import org.onap.policy.models.pap.persistence.provider.PolicyAuditProvider.AuditFilter;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.pdp.persistence.provider.PdpFilterParameters;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTypedEntityFilter;

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

    private static final Instant TIMESTAMP = Instant.EPOCH;

    private static final String ORDER = "DESC";

    private PolicyModelsProviderParameters parameters;

    private PolicyModelsProvider databaseProvider;

    /**
     * Initialize parameters.
     */
    @Before
    public void setupParameters() {
        parameters = new PolicyModelsProviderParameters();
        parameters.setDatabaseDriver("org.h2.Driver");
        parameters.setDatabaseUrl("jdbc:h2:mem:DatabasePolicyModelsProviderTest");
        parameters.setDatabaseUser("policy");
        parameters.setDatabasePassword("P01icY");
        parameters.setPersistenceUnit("ToscaConceptTest");
    }

    /**
     * Closes the DB.
     */
    @After
    public void tearDown() throws PfModelException {
        if (databaseProvider != null) {
            databaseProvider.close();
        }
    }

    @Test
    public void testInitAndClose() throws Exception {
        assertThatThrownBy(() -> {
            new DatabasePolicyModelsProviderImpl(null);
        }).hasMessageMatching("^parameters is marked .*on.*ull but is null$");

        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        parameters.setDatabaseUrl("jdbc://www.acmecorp.nonexist");

        databaseProvider.close();
        databaseProvider.init();

        databaseProvider.close();

        parameters.setDatabaseUrl("jdbc:h2:mem:DatabasePolicyModelsProviderTest");

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

        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

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
    }

    @Test
    public void testProviderMethodsNullGroup() throws Exception {

        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

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
        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        databaseProvider.close();

        assertThatThrownBy(() -> {
            databaseProvider.getPolicyTypes(NAME, "version");
        }).hasMessage("policy models provider is not initilaized");
    }

    @Test
    public void testProviderMethods() throws PfModelException {
        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        assertThatThrownBy(() -> databaseProvider.getPolicyTypes(NAME, VERSION_100))
                .hasMessage("service template not found in database");

        assertTrue(databaseProvider.getPolicyTypeList(NAME, VERSION_100).isEmpty());

        assertThatThrownBy(
            () -> databaseProvider.getFilteredPolicyTypes(ToscaEntityFilter.<ToscaPolicyType>builder().build()))
                        .hasMessage("service template not found in database");

        assertTrue(databaseProvider.getFilteredPolicyTypeList(ToscaEntityFilter.<ToscaPolicyType>builder().build())
                .isEmpty());

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

        assertThatThrownBy(
            () -> databaseProvider.getFilteredPolicies(ToscaTypedEntityFilter.<ToscaPolicy>builder().build()))
                        .hasMessage("service template not found in database");

        assertTrue(databaseProvider.getFilteredPolicyList(ToscaTypedEntityFilter.<ToscaPolicy>builder().build())
                .isEmpty());

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
    }

    @Test
    public void testProviderMethodsInGroups() throws PfModelException {
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
        pdpSubGroup.getSupportedPolicyTypes().add(new ToscaConceptIdentifier("type", "7.8.9"));
        pdpGroup.getPdpSubgroups().add(pdpSubGroup);

        Pdp pdp = new Pdp();
        pdp.setInstanceId("type-0");
        pdp.setMessage("Hello");
        pdp.setPdpState(PdpState.ACTIVE);
        pdp.setHealthy(PdpHealthStatus.UNKNOWN);
        pdpSubGroup.setPdpInstances(new ArrayList<>());
        pdpSubGroup.getPdpInstances().add(pdp);

        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        assertEquals(123,
                databaseProvider.createPdpGroups(groupList).get(0).getPdpSubgroups().get(0).getDesiredInstanceCount());
        assertEquals(1, databaseProvider.getPdpGroups(GROUP).size());

        pdpSubGroup.setDesiredInstanceCount(234);
        databaseProvider.updatePdpSubGroup(GROUP, pdpSubGroup);
        assertEquals(234,
                databaseProvider.getPdpGroups(GROUP).get(0).getPdpSubgroups().get(0).getDesiredInstanceCount());

        assertEquals("Hello", databaseProvider.getPdpGroups(GROUP).get(0).getPdpSubgroups().get(0).getPdpInstances()
                .get(0).getMessage());
        pdp.setMessage("Howdy");
        databaseProvider.updatePdp(GROUP, "type", pdp);
        assertEquals("Howdy", databaseProvider.getPdpGroups(GROUP).get(0).getPdpSubgroups().get(0).getPdpInstances()
                .get(0).getMessage());

        assertThatThrownBy(() -> {
            databaseProvider.deletePdpGroup(NAME);
        }).hasMessage("delete of PDP group \"name:0.0.0\" failed, PDP group does not exist");

        assertEquals(pdpGroup.getName(), databaseProvider.deletePdpGroup(GROUP).getName());

        List<PdpStatistics> statisticsArrayList = makePdpStatisticsList();

        assertThat(databaseProvider.getFilteredPdpStatistics(PdpFilterParameters.builder().build())).isEmpty();
        assertThat(databaseProvider.createPdpStatistics(statisticsArrayList)).hasSize(1);
        assertThat(databaseProvider.updatePdpStatistics(statisticsArrayList)).hasSize(1);
    }

    @Test
    public void testProviderMethodsStatistics() throws PfModelException {
        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);
        databaseProvider.createPdpStatistics(makePdpStatisticsList());

        assertEquals(NAME, databaseProvider.getFilteredPdpStatistics(PdpFilterParameters.builder().build()).get(0)
                        .getPdpInstanceId());
        assertEquals(NAME, databaseProvider.getFilteredPdpStatistics(
                        PdpFilterParameters.builder().group(GROUP).build()).get(0).getPdpInstanceId());
        assertEquals(0, databaseProvider.getFilteredPdpStatistics(
                        PdpFilterParameters.builder().group(GROUP).startTime(Instant.now()).build()).size());
        assertEquals(NAME, databaseProvider
                        .getFilteredPdpStatistics(PdpFilterParameters.builder().group(GROUP).endTime(TIMESTAMP).build())
                        .get(0).getPdpInstanceId());
        assertEquals(0, databaseProvider.getFilteredPdpStatistics(PdpFilterParameters.builder().group(GROUP)
                        .startTime(Instant.now()).endTime(Instant.now()).build()).size());

        assertEquals(NAME, databaseProvider
                        .getFilteredPdpStatistics(PdpFilterParameters.builder().name(NAME).group(GROUP).build()).get(0)
                        .getPdpInstanceId());
        assertEquals(0, databaseProvider.getFilteredPdpStatistics(PdpFilterParameters.builder().name(NAME).group(GROUP)
                        .startTime(Instant.now()).endTime(Instant.now()).build()).size());

        assertEquals(NAME,
                        databaseProvider.getFilteredPdpStatistics(
                                        PdpFilterParameters.builder().name(NAME).group(GROUP).subGroup("type").build())
                                        .get(0).getPdpInstanceId());

        assertEquals(0, databaseProvider.getFilteredPdpStatistics(
                        PdpFilterParameters.builder().name(NAME).group(GROUP).subGroup("type")
                            .startTime(Instant.now()).endTime(Instant.now()).build()).size());

        assertEquals(NAME, databaseProvider.getFilteredPdpStatistics(
                        PdpFilterParameters.builder().name(NAME).group(GROUP).subGroup("type")
                            .sortOrder(ORDER).recordNum(1).build()).get(0).getPdpInstanceId());
        assertEquals(NAME, databaseProvider.getFilteredPdpStatistics(
                        PdpFilterParameters.builder().name(NAME).group(GROUP).subGroup("type")
                            .sortOrder(ORDER).recordNum(5).build()).get(0).getPdpInstanceId());
        assertEquals(0, databaseProvider.getFilteredPdpStatistics(
                        PdpFilterParameters.builder().name(NAME).group(GROUP).subGroup("type")
                            .startTime(Instant.now()).endTime(Instant.now())
                            .sortOrder(ORDER).recordNum(5).build()).size());

        assertEquals(NAME, databaseProvider.deletePdpStatistics(NAME, null).get(0).getPdpInstanceId());
        assertThat(databaseProvider.getFilteredPdpStatistics(PdpFilterParameters.builder().build())).isEmpty();

        assertThat(databaseProvider.getAllPolicyStatus()).isEmpty();
        assertThat(databaseProvider.getAllPolicyStatus(new ToscaConceptIdentifierOptVersion("MyPolicy", null)))
                .isEmpty();
        assertThat(databaseProvider.getGroupPolicyStatus(GROUP)).isEmpty();
        assertThatCode(() -> databaseProvider.cudPolicyStatus(null, null, null))
            .doesNotThrowAnyException();

        databaseProvider.close();
    }

    @Test
    public void testDeletePolicyDeployedInSubgroup() throws PfModelException {
        List<ToscaConceptIdentifier> policies = new ArrayList<>();

        policies.add(new ToscaConceptIdentifier("p0", "0.0.1"));
        policies.add(new ToscaConceptIdentifier("p1", "0.0.1"));

        List<ToscaConceptIdentifier> supportedPolicyTypes = new ArrayList<>();
        supportedPolicyTypes.add(new ToscaConceptIdentifier("pt2", "0.0.1"));

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

        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        databaseProvider.createPdpGroups(pdpGroups);

        assertThatThrownBy(() -> databaseProvider.deletePolicy("p0", "0.0.1"))
                .hasMessageContaining("policy is in use, it is deployed in PDP group pdpGroup subgroup pdpType");

        assertThatThrownBy(() -> databaseProvider.deletePolicy("p3", "0.0.1"))
                .hasMessageContaining("service template not found in database");

        databaseProvider.close();
    }

    @Test
    public void testDeletePolicyTypeSupportedInSubgroup() throws PfModelException {
        List<ToscaConceptIdentifier> supportedPolicyTypes = new ArrayList<>();
        supportedPolicyTypes.add(new ToscaConceptIdentifier("pt1", "0.0.1"));
        supportedPolicyTypes.add(new ToscaConceptIdentifier("pt2", "0.0.1"));

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

        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        databaseProvider.createPdpGroups(pdpGroups);

        assertThatThrownBy(() -> databaseProvider.deletePolicyType("pt2", "0.0.1"))
                .hasMessageContaining("policy type is in use, it is referenced in PDP group pdpGroup subgroup pdpType");

        assertThatThrownBy(() -> databaseProvider.deletePolicyType("pt0", "0.0.1"))
                .hasMessageContaining("service template not found in database");

        databaseProvider.close();
    }

    @Test
    public void testCreateAuditRecords() throws PfModelException {
        PolicyAudit audit = PolicyAudit.builder().action(AuditAction.DEPLOYMENT).pdpGroup(GROUP).pdpType(GROUP)
                .policy(new ToscaConceptIdentifier(NAME, VERSION_100)).user("user").build();

        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        databaseProvider.createAuditRecords(List.of(audit));
        List<PolicyAudit> createdAudits = databaseProvider.getAuditRecords(AuditFilter.builder().recordNum(10).build());
        assertThat(createdAudits).hasSize(1);

        List<PolicyAudit> emptyList = databaseProvider
                        .getAuditRecords(AuditFilter.builder().action(AuditAction.UNDEPLOYMENT).recordNum(10).build());
        assertThat(emptyList).isEmpty();

        assertThatThrownBy(() -> databaseProvider.createAuditRecords(null))
                .hasMessageContaining("audits is marked non-null but is null");

        databaseProvider.close();
    }

    private List<PdpStatistics> makePdpStatisticsList() {
        PdpStatistics pdpStatistics = new PdpStatistics();
        pdpStatistics.setPdpInstanceId(NAME);
        pdpStatistics.setTimeStamp(TIMESTAMP);
        pdpStatistics.setPdpGroupName(GROUP);
        pdpStatistics.setPdpSubGroupName("type");
        List<PdpStatistics> statisticsArrayList = List.of(pdpStatistics);
        return statisticsArrayList;
    }
}
