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

package org.onap.policy.models.provider.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
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
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the database models provider implementation.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DatabasePolicyModelsProviderTest {
    private static final String NAME = "name";

    private static final String TEMPLATE_IS_NULL = "serviceTemplate is marked @NonNull but is null";

    private static final String POLICY_ID_IS_NULL = "policyId is marked @NonNull but is null";

    private static final String PDP_TYPE_IS_NULL = "pdpType is marked @NonNull but is null";

    private static final String SUBGROUP_IS_NULL = "pdpSubGroup is marked @NonNull but is null";

    private static final String GROUP_IS_NULL = "pdpGroupName is marked @NonNull but is null";

    private static final String NAME_IS_NULL = "name is marked @NonNull but is null";

    private static final String FILTER_IS_NULL = "filter is marked @NonNull but is null";

    private static final String INSTANCE = "Instance";

    private static final String POLICY_ID = "policy_id";

    private static final String GROUP = "group";

    private static final String VERSION_100 = "1.0.0";

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePolicyModelsProviderTest.class);

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
        }).hasMessage("parameters is marked @NonNull but is null");

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

        try (PolicyModelsProvider databaseProvider =
                        new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters)) {

            assertThatThrownBy(() -> {
                databaseProvider.getFilteredPolicyTypes(null);
            }).hasMessage(FILTER_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.getFilteredPolicyTypeList(null);
            }).hasMessage(FILTER_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.createPolicyTypes(null);
            }).hasMessage(TEMPLATE_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePolicyTypes(null);
            }).hasMessage(TEMPLATE_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.deletePolicyType(null, null);
            }).hasMessage(NAME_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.deletePolicyType("aaa", null);
            }).hasMessage("version is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.deletePolicyType(null, "aaa");
            }).hasMessage(NAME_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.getFilteredPolicies(null);
            }).hasMessage(FILTER_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.getFilteredPolicyList(null);
            }).hasMessage(FILTER_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.createPolicies(null);
            }).hasMessage(TEMPLATE_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePolicies(null);
            }).hasMessage(TEMPLATE_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.deletePolicy(null, null);
            }).hasMessage(NAME_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.deletePolicy(null, "aaa");
            }).hasMessage(NAME_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.deletePolicy("aaa", null);
            }).hasMessage("version is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.getOperationalPolicy(null, null);
            }).hasMessage(POLICY_ID_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.getOperationalPolicy(null, "");
            }).hasMessage(POLICY_ID_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.getOperationalPolicy("", null);
            }).hasMessage("no policy found for policy: :null");

            assertThatThrownBy(() -> {
                databaseProvider.createOperationalPolicy(null);
            }).hasMessage("legacyOperationalPolicy is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.updateOperationalPolicy(null);
            }).hasMessage("legacyOperationalPolicy is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.deleteOperationalPolicy(null, null);
            }).hasMessage(POLICY_ID_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.deleteOperationalPolicy(null, "");
            }).hasMessage(POLICY_ID_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.deleteOperationalPolicy("", null);
            }).hasMessage("policyVersion is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.getGuardPolicy(null, null);
            }).hasMessage(POLICY_ID_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.getGuardPolicy(null, "");
            }).hasMessage(POLICY_ID_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.getGuardPolicy("", null);
            }).hasMessage("no policy found for policy: :null");

            assertThatThrownBy(() -> {
                databaseProvider.createGuardPolicy(null);
            }).hasMessage("legacyGuardPolicy is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.updateGuardPolicy(null);
            }).hasMessage("legacyGuardPolicy is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.deleteGuardPolicy(null, null);
            }).hasMessage(POLICY_ID_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.deleteGuardPolicy(null, "");
            }).hasMessage(POLICY_ID_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.deleteGuardPolicy("", null);
            }).hasMessage("policyVersion is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.getFilteredPdpGroups(null);
            }).hasMessage(FILTER_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.createPdpGroups(null);
            }).hasMessage("pdpGroups is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpGroups(null);
            }).hasMessage("pdpGroups is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpSubGroup(null, null);
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpSubGroup(null, new PdpSubGroup());
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpSubGroup(NAME, null);
            }).hasMessage(SUBGROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpSubGroup(NAME, new PdpSubGroup());
            }).hasMessage("parameter \"localName\" is null");

            assertThatThrownBy(() -> {
                databaseProvider.updatePdp(null, null, null);
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdp(null, null, new Pdp());
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdp(null, "sub", null);
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdp(null, "sub", new Pdp());
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdp(NAME, null, null);
            }).hasMessage(SUBGROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdp(NAME, null, new Pdp());
            }).hasMessage(SUBGROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdp(NAME, "sub", null);
            }).hasMessage("pdp is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.updatePdp(NAME, "sub", new Pdp());
            }).hasMessage("parameter \"localName\" is null");

            assertThatThrownBy(() -> {
                databaseProvider.deletePdpGroup(null);
            }).hasMessage(NAME_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(null, null, null, null);
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(null, null, null, new PdpStatistics());
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(null, null, INSTANCE, null);
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(null, null, INSTANCE, new PdpStatistics());
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(null, "type", null, null);
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(null, "type", null, new PdpStatistics());
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(null, "type", INSTANCE, null);
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(null, "type", INSTANCE, new PdpStatistics());
            }).hasMessage(GROUP_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(NAME, null, null, null);
            }).hasMessage(PDP_TYPE_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(NAME, null, null, new PdpStatistics());
            }).hasMessage(PDP_TYPE_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(NAME, null, INSTANCE, null);
            }).hasMessage(PDP_TYPE_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(NAME, null, INSTANCE, new PdpStatistics());
            }).hasMessage(PDP_TYPE_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(NAME, "type", null, null);
            }).hasMessage("pdpInstanceId is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(NAME, "type", null, new PdpStatistics());
            }).hasMessage("pdpInstanceId is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.updatePdpStatistics(NAME, "type", INSTANCE, null);
            }).hasMessage("pdpStatistics is marked @NonNull but is null");

            databaseProvider.updatePdpStatistics(NAME, "type", INSTANCE, new PdpStatistics());
        }
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
    public void testProviderMethods() {
        try (PolicyModelsProvider databaseProvider =
                new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters)) {

            assertTrue(databaseProvider.getPolicyTypes(NAME, VERSION_100).getPolicyTypes().isEmpty());
            assertTrue(databaseProvider.getPolicyTypeList(NAME, VERSION_100).isEmpty());
            assertEquals(0, databaseProvider.getFilteredPolicyTypes(ToscaPolicyTypeFilter.builder().build())
                    .getPolicyTypes().get(0).size());
            assertEquals(0, databaseProvider.getFilteredPolicyTypeList(ToscaPolicyTypeFilter.builder().build()).size());

            assertThatThrownBy(() -> {
                databaseProvider.createPolicyTypes(new ToscaServiceTemplate());
            }).hasMessage("no policy types specified on service template");

            assertThatThrownBy(() -> {
                databaseProvider.updatePolicyTypes(new ToscaServiceTemplate());
            }).hasMessage("no policy types specified on service template");

            assertTrue(databaseProvider.deletePolicyType(NAME, VERSION_100).getPolicyTypes().isEmpty());

            assertTrue(databaseProvider.deletePolicyType(NAME, VERSION_100).getPolicyTypes().isEmpty());

            assertTrue(
                    databaseProvider.getPolicies(NAME, VERSION_100).getToscaTopologyTemplate().getPolicies().isEmpty());
            assertTrue(databaseProvider.getPolicyList(NAME, VERSION_100).isEmpty());
            assertEquals(0, databaseProvider.getFilteredPolicies(ToscaPolicyFilter.builder().build())
                    .getToscaTopologyTemplate().getPolicies().get(0).size());
            assertEquals(0, databaseProvider.getFilteredPolicyList(ToscaPolicyFilter.builder().build()).size());

            assertThatThrownBy(() -> {
                databaseProvider.createPolicies(new ToscaServiceTemplate());
            }).hasMessage("topology template not specified on service template");

            assertThatThrownBy(() -> {
                databaseProvider.updatePolicies(new ToscaServiceTemplate());
            }).hasMessage("topology template not specified on service template");

            assertTrue(databaseProvider.deletePolicy("Policy", "0.0.0").getToscaTopologyTemplate().getPolicies()
                    .isEmpty());

            assertThatThrownBy(() -> {
                databaseProvider.getOperationalPolicy(POLICY_ID, null);
            }).hasMessage("no policy found for policy: policy_id:null");

            assertThatThrownBy(() -> {
                databaseProvider.getOperationalPolicy(POLICY_ID, "10");
            }).hasMessage("no policy found for policy: policy_id:10");

            assertThatThrownBy(() -> {
                databaseProvider.createOperationalPolicy(new LegacyOperationalPolicy());
            }).hasMessage(NAME_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.updateOperationalPolicy(new LegacyOperationalPolicy());
            }).hasMessage(NAME_IS_NULL);

            assertThatThrownBy(() -> {
                databaseProvider.deleteOperationalPolicy(POLICY_ID, "55");
            }).hasMessage("no policy found for policy: policy_id:55");

            assertThatThrownBy(() -> {
                databaseProvider.getGuardPolicy(POLICY_ID, null);
            }).hasMessage("no policy found for policy: policy_id:null");

            assertThatThrownBy(() -> {
                databaseProvider.getGuardPolicy(POLICY_ID, "6");
            }).hasMessage("no policy found for policy: policy_id:6");

            assertThatThrownBy(() -> {
                databaseProvider.createGuardPolicy(new LegacyGuardPolicyInput());
            }).hasMessage("policy type for guard policy \"null\" unknown");

            assertThatThrownBy(() -> {
                databaseProvider.updateGuardPolicy(new LegacyGuardPolicyInput());
            }).hasMessage("policy type for guard policy \"null\" unknown");

            assertThatThrownBy(() -> {
                databaseProvider.deleteGuardPolicy(POLICY_ID, "33");
            }).hasMessage("no policy found for policy: policy_id:33");

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

            assertEquals(123, databaseProvider.createPdpGroups(groupList).get(0).getPdpSubgroups().get(0)
                    .getDesiredInstanceCount());
            assertEquals(1, databaseProvider.getPdpGroups(GROUP).size());

            pdpSubGroup.setDesiredInstanceCount(234);
            databaseProvider.updatePdpSubGroup(GROUP, pdpSubGroup);
            assertEquals(234, databaseProvider.getPdpGroups(GROUP).get(0).getPdpSubgroups()
                    .get(0).getDesiredInstanceCount());

            assertEquals("Hello", databaseProvider.getPdpGroups(GROUP).get(0).getPdpSubgroups()
                    .get(0).getPdpInstances().get(0).getMessage());
            pdp.setMessage("Howdy");
            databaseProvider.updatePdp(GROUP, "type", pdp);
            assertEquals("Howdy", databaseProvider.getPdpGroups(GROUP).get(0).getPdpSubgroups()
                    .get(0).getPdpInstances().get(0).getMessage());

            assertThatThrownBy(() -> {
                databaseProvider.deletePdpGroup(NAME);
            }).hasMessage("delete of PDP group \"name:0.0.0\" failed, PDP group does not exist");

            assertEquals(pdpGroup.getName(), databaseProvider.deletePdpGroup(GROUP).getName());

            assertEquals(0, databaseProvider.getPdpStatistics(null).size());

            databaseProvider.updatePdpStatistics(GROUP, "type", "type-0", new PdpStatistics());
        } catch (Exception exc) {
            LOGGER.warn("test should not throw an exception", exc);
            fail("test should not throw an exception");
        }
    }
}
