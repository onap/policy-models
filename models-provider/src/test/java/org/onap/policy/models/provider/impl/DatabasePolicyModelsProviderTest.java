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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Base64;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.pdp.concepts.PdpGroups;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
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
        PolicyModelsProvider databaseProvider =
                new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        parameters.setDatabaseUrl("jdbc://www.acmecorp.nonexist");

        assertThatThrownBy(() -> {
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
        databaseProvider.init();

        assertThatThrownBy(() -> {
            databaseProvider.getPolicyTypes(null, null);
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.getPolicyTypes("aaa", null);
        }).hasMessage("version is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.getPolicyTypes(null, "aaa");
        }).hasMessage("name is marked @NonNull but is null");

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
            databaseProvider.getPolicies(null, null);
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.getPolicies(null, "aaa");
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.getPolicies("aaa", null);
        }).hasMessage("version is marked @NonNull but is null");

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
            databaseProvider.createPdpGroups(null);
        }).hasMessage("pdpGroups is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.updatePdpGroups(null);
        }).hasMessage("pdpGroups is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            databaseProvider.deletePdpGroup(null, null);
        }).hasMessage("name is marked @NonNull but is null");

        databaseProvider.close();

    }

    @Test
    public void testProviderMethodsNotInit() throws Exception {
        PolicyModelsProvider databaseProvider =
                new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        assertThatThrownBy(() -> {
            databaseProvider.getPolicyTypes("name", "version");
        }).hasMessage("policy models provider is not initilaized");
    }

    @Test
    public void testProviderMethods() {
        try (PolicyModelsProvider databaseProvider =
                new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters)) {
            databaseProvider.init();

            assertThatThrownBy(() -> {
                databaseProvider.getPolicyTypes("name", "version");
            }).hasMessage("policy type not found: name:version");

            assertThatThrownBy(() -> {
                databaseProvider.createPolicyTypes(new ToscaServiceTemplate());
            }).hasMessage("no policy types specified on service template");

            assertThatThrownBy(() -> {
                databaseProvider.updatePolicyTypes(new ToscaServiceTemplate());
            }).hasMessage("no policy types specified on service template");

            assertThatThrownBy(() -> {
                databaseProvider.deletePolicyType("name", "version");
            }).hasMessage("policy type not found: name:version");

            assertThatThrownBy(() -> {
                databaseProvider.getPolicies("name", "version");
            }).hasMessage("policy not found: name:version");

            assertThatThrownBy(() -> {
                databaseProvider.createPolicies(new ToscaServiceTemplate());
            }).hasMessage("topology template not specified on service template");

            assertThatThrownBy(() -> {
                databaseProvider.updatePolicies(new ToscaServiceTemplate());
            }).hasMessage("topology template not specified on service template");

            assertThatThrownBy(() -> {
                databaseProvider.deletePolicy("name", "version");
            }).hasMessage("policy not found: name:version");

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

            assertNotNull(databaseProvider.getPdpGroups("name", "version"));
            assertNotNull(databaseProvider.createPdpGroups(new PdpGroups()));
            assertNotNull(databaseProvider.updatePdpGroups(new PdpGroups()));
            assertNotNull(databaseProvider.deletePdpGroup("name", "version"));

        } catch (Exception exc) {
            LOGGER.warn("test should not throw an exception", exc);
            fail("test should not throw an exception");
        }
    }
}
