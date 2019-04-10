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

package org.onap.policy.models.provider.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabasePolicyModelsProviderTest.class);

    PolicyModelsProviderParameters parameters;

    /**
     * Initialize parameters.
     */
    @Before
    public void setupParameters() {
        parameters = new PolicyModelsProviderParameters();
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

        assertThatThrownBy(() -> {
            databaseProvider.close();
            databaseProvider.init();
        }).hasMessage("could not connect to database with URL \"jdbc://www.acmecorp.nonexist\"");

        parameters.setDatabaseUrl("jdbc:h2:mem:testdb");

        try {
            databaseProvider.init();
            databaseProvider.close();
        } catch (Exception pfme) {
            fail("test shold not throw an exception here");
        }

        parameters.setPersistenceUnit("WileECoyote");

        String errorMessage = "could not create Data Access Object (DAO) using url "
                + "\"jdbc:h2:mem:testdb\" and persistence unit \"WileECoyote\"";
        assertThatThrownBy(() -> {
            databaseProvider.init();
        }).hasMessage(errorMessage);

        parameters.setPersistenceUnit("ToscaConceptTest");

        try {
            databaseProvider.init();
            databaseProvider.close();
        } catch (Exception pfme) {
            fail("test shold not throw an exception here");
        }

        assertThatThrownBy(() -> {
            databaseProvider.init();
            databaseProvider.init();
        }).hasMessage("provider is already initialized");

        try {
            databaseProvider.close();
        } catch (Exception pfme) {
            fail("test shold not throw an exception here");
        }

        try {
            databaseProvider.close();
        } catch (Exception pfme) {
            fail("test shold not throw an exception here");
        }

        assertThatThrownBy(() -> {
            DatabasePolicyModelsProviderImpl databaseProviderImpl = (DatabasePolicyModelsProviderImpl) databaseProvider;
            databaseProvider.init();
            databaseProviderImpl.setConnection(new DummyConnection());
            databaseProvider.close();
        }).hasMessage("could not close connection to database with URL \"jdbc:h2:mem:testdb\"");
    }

    @Test
    public void testProviderMethodsNull() throws Exception {
        PolicyModelsProvider databaseProvider =
                new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        assertThatThrownBy(() -> {
            databaseProvider.getFilteredPolicyTypes(null);
        }).hasMessage("filter is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.getFilteredPolicyTypeList(null);
        }).hasMessage("filter is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.createPolicyTypes(null);
        }).hasMessage("serviceTemplate is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePolicyTypes(null);
        }).hasMessage("serviceTemplate is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicyType(null, null);
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicyType("aaa", null);
        }).hasMessage("version is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicyType(null, "aaa");
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.getFilteredPolicies(null);
        }).hasMessage("filter is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.getFilteredPolicyList(null);
        }).hasMessage("filter is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.createPolicies(null);
        }).hasMessage("serviceTemplate is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePolicies(null);
        }).hasMessage("serviceTemplate is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicy(null, null);
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicy(null, "aaa");
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deletePolicy("aaa", null);
        }).hasMessage("version is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.getOperationalPolicy(null);
        }).hasMessage("policyId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.createOperationalPolicy(null);
        }).hasMessage("legacyOperationalPolicy is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updateOperationalPolicy(null);
        }).hasMessage("legacyOperationalPolicy is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deleteOperationalPolicy(null);
        }).hasMessage("policyId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.getGuardPolicy(null);
        }).hasMessage("policyId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.createGuardPolicy(null);
        }).hasMessage("legacyGuardPolicy is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updateGuardPolicy(null);
        }).hasMessage("legacyGuardPolicy is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deleteGuardPolicy(null);
        }).hasMessage("policyId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.getFilteredPdpGroups(null);
        }).hasMessage("filter is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.createPdpGroups(null);
        }).hasMessage("pdpGroups is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpGroups(null);
        }).hasMessage("pdpGroups is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpSubGroup(null, null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpSubGroup(null, null, new PdpSubGroup());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpSubGroup(null, "version", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpSubGroup(null, "version", new PdpSubGroup());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpSubGroup("name", null, null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpSubGroup("name", null, new PdpSubGroup());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpSubGroup("name", "version", null);
        }).hasMessage("pdpSubGroup is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, null, null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, null, null, new Pdp());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, null, "sub", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, null, "sub", new Pdp());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, "version", null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, "version", null, new Pdp());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, "version", "sub", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp(null, "version", "sub", new Pdp());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp("name", null, null, null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp("name", null, null, new Pdp());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp("name", null, "sub", null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp("name", null, "sub", new Pdp());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp("name", "version", null, null);
        }).hasMessage("pdpSubGroup is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp("name", "version", null, new Pdp());
        }).hasMessage("pdpSubGroup is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdp("name", "version", "sub", null);
        }).hasMessage("pdp is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deletePdpGroup(null, null);
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deletePdpGroup(null, "version");
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deletePdpGroup("name", null);
        }).hasMessage("version is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, null, null, null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, null, null, null, new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, null, null, "Instance", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, null, null, "Instance", new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, null, "type", null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, null, "type", null, new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, null, "type", "Instance", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, null, "type", "Instance", new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, "ver", null, null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, "ver", null, null, new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, "ver", null, "Instance", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, "ver", null, "Instance", new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, "ver", "type", null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, "ver", "type", null, new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, "ver", "type", "Instance", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics(null, "ver", "type", "Instance", new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", null, null, null, null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", null, null, null, new PdpStatistics());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", null, null, "Instance", null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", null, null, "Instance", new PdpStatistics());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", null, "type", null, null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", null, "type", null, new PdpStatistics());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", null, "type", "Instance", null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", null, "type", "Instance", new PdpStatistics());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", "ver", null, null, null);
        }).hasMessage("pdpType is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", "ver", null, null, new PdpStatistics());
        }).hasMessage("pdpType is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", "ver", null, "Instance", null);
        }).hasMessage("pdpType is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", "ver", null, "Instance", new PdpStatistics());
        }).hasMessage("pdpType is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", "ver", "type", null, null);
        }).hasMessage("pdpInstanceId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", "ver", "type", null, new PdpStatistics());
        }).hasMessage("pdpInstanceId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpStatistics("name", "ver", "type", "Instance", null);
        }).hasMessage("pdpStatistics is marked @NonNull but is null");

        databaseProvider.close();
    }

    @Test
    public void testProviderMethodsNotInit() throws Exception {
        PolicyModelsProvider databaseProvider =
                new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        databaseProvider.close();

        assertThatThrownBy(() -> {
            databaseProvider.getPolicyTypes("name", "version");
        }).hasMessage("policy models provider is not initilaized");
    }

    @Test
    public void testProviderMethods() {
        try (PolicyModelsProvider databaseProvider =
                new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters)) {

            assertEquals(0, databaseProvider.getPolicyTypes("name", "version").getPolicyTypes().get(0).size());
            assertEquals(0, databaseProvider.getPolicyTypeList("name", "version").size());
            assertEquals(0, databaseProvider.getFilteredPolicyTypes(ToscaPolicyTypeFilter.builder().build())
                    .getPolicyTypes().get(0).size());
            assertEquals(0, databaseProvider.getFilteredPolicyTypeList(ToscaPolicyTypeFilter.builder().build()).size());

            assertThatThrownBy(() -> {
                databaseProvider.createPolicyTypes(new ToscaServiceTemplate());
            }).hasMessage("no policy types specified on service template");

            assertThatThrownBy(() -> {
                databaseProvider.updatePolicyTypes(new ToscaServiceTemplate());
            }).hasMessage("no policy types specified on service template");

            assertEquals(0, databaseProvider.deletePolicyType("name", "version").getPolicyTypes().get(0).size());

            assertEquals(0, databaseProvider.deletePolicyType("name", "version").getPolicyTypes().get(0).size());

            assertEquals(0, databaseProvider.getPolicies("name", "version").getToscaTopologyTemplate().getPolicies()
                    .get(0).size());
            assertEquals(0, databaseProvider.getPolicyList("name", "version").size());
            assertEquals(0, databaseProvider.getFilteredPolicies(ToscaPolicyFilter.builder().build())
                    .getToscaTopologyTemplate().getPolicies().get(0).size());
            assertEquals(0, databaseProvider.getFilteredPolicyList(ToscaPolicyFilter.builder().build()).size());

            assertThatThrownBy(() -> {
                databaseProvider.createPolicies(new ToscaServiceTemplate());
            }).hasMessage("topology template not specified on service template");

            assertThatThrownBy(() -> {
                databaseProvider.updatePolicies(new ToscaServiceTemplate());
            }).hasMessage("topology template not specified on service template");

            assertEquals(0, databaseProvider.deletePolicy("Policy", "0.0.0").getToscaTopologyTemplate().getPolicies()
                    .get(0).size());

            assertThatThrownBy(() -> {
                databaseProvider.getOperationalPolicy("policy_id");
            }).hasMessage("no policy found for policy ID: policy_id");

            assertThatThrownBy(() -> {
                databaseProvider.createOperationalPolicy(new LegacyOperationalPolicy());
            }).hasMessage("name is marked @NonNull but is null");

            assertThatThrownBy(() -> {
                databaseProvider.updateOperationalPolicy(new LegacyOperationalPolicy());
            }).hasMessage("no policy found for policy ID: null");

            assertThatThrownBy(() -> {
                databaseProvider.deleteOperationalPolicy("policy_id");
            }).hasMessage("no policy found for policy ID: policy_id");

            assertThatThrownBy(() -> {
                databaseProvider.getGuardPolicy("policy_id");
            }).hasMessage("no policy found for policy ID: policy_id");

            assertThatThrownBy(() -> {
                databaseProvider.createGuardPolicy(new LegacyGuardPolicyInput());
            }).hasMessage("policy type for guard policy \"null\" unknown");

            assertThatThrownBy(() -> {
                databaseProvider.updateGuardPolicy(new LegacyGuardPolicyInput());
            }).hasMessage("policy type for guard policy \"null\" unknown");

            assertThatThrownBy(() -> {
                databaseProvider.deleteGuardPolicy("policy_id");
            }).hasMessage("no policy found for policy ID: policy_id");

            assertEquals(0, databaseProvider.getPdpGroups("name", "version").size());
            assertEquals(0, databaseProvider.getFilteredPdpGroups(PdpGroupFilter.builder().build()).size());

            assertNotNull(databaseProvider.createPdpGroups(new ArrayList<>()));
            assertNotNull(databaseProvider.updatePdpGroups(new ArrayList<>()));

            PdpGroup pdpGroup = new PdpGroup();
            pdpGroup.setName("group");
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
            assertEquals(1, databaseProvider.getPdpGroups("group", "1.2.3").size());

            pdpSubGroup.setDesiredInstanceCount(234);
            databaseProvider.updatePdpSubGroup("group", "1.2.3", pdpSubGroup);
            assertEquals(234, databaseProvider.getPdpGroups("group", "1.2.3").get(0).getPdpSubgroups()
                    .get(0).getDesiredInstanceCount());

            assertEquals("Hello", databaseProvider.getPdpGroups("group", "1.2.3").get(0).getPdpSubgroups()
                    .get(0).getPdpInstances().get(0).getMessage());
            pdp.setMessage("Howdy");
            databaseProvider.updatePdp("group", "1.2.3", "type", pdp);
            assertEquals("Howdy", databaseProvider.getPdpGroups("group", "1.2.3").get(0).getPdpSubgroups()
                    .get(0).getPdpInstances().get(0).getMessage());

            assertThatThrownBy(() -> {
                databaseProvider.deletePdpGroup("name", "version");
            }).hasMessage("delete of PDP group \"name:version\" failed, PDP group does not exist");

            assertEquals(pdpGroup.getName(), databaseProvider.deletePdpGroup("group", "1.2.3").getName());

            assertEquals(0, databaseProvider.getPdpStatistics(null, null).size());

            databaseProvider.updatePdpStatistics("group", "1.2.3", "type", "type-0", new PdpStatistics());
        } catch (Exception exc) {
            LOGGER.warn("test should not throw an exception", exc);
            fail("test should not throw an exception");
        }
    }
}
