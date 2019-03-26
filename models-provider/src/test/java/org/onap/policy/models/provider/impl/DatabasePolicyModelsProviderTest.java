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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Base64;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.pap.concepts.PdpGroups;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicy;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
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
        try {
            databaseProvider.init();
            fail("test should throw an exception");
        } catch (Exception pfme) {
            assertEquals("could not connect to database with URL \"jdbc://www.acmecorp.nonexist\"", pfme.getMessage());
        }
        parameters.setDatabaseUrl("jdbc:h2:mem:testdb");

        try {
            databaseProvider.init();
            databaseProvider.close();
        } catch (Exception pfme) {
            fail("test shold not throw an exception here");
        }

        parameters.setPersistenceUnit("WileECoyote");
        try {
            databaseProvider.init();
            fail("test should throw an exception");
        } catch (Exception pfme) {
            assertEquals("could not create Data Access Object (DAO) using url "
                    + "\"jdbc:h2:mem:testdb\" and persistence unit \"WileECoyote\"", pfme.getMessage());
        }
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

        try {
            DatabasePolicyModelsProviderImpl databaseProviderImpl = (DatabasePolicyModelsProviderImpl) databaseProvider;
            databaseProvider.init();
            databaseProviderImpl.setConnection(new DummyConnection());
            databaseProvider.close();
            fail("test should throw an exception");
        } catch (Exception pfme) {
            assertEquals("could not close connection to database with URL \"jdbc:h2:mem:testdb\"", pfme.getMessage());
        }
    }

    @Test
    public void testProviderMethodsNull() throws Exception {
        PolicyModelsProvider databaseProvider =
                new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);
        databaseProvider.init();

        try {
            databaseProvider.getPolicyTypes(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyTypeKey is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.createPolicyTypes(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("serviceTemplate is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.updatePolicyTypes(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("serviceTemplate is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.deletePolicyTypes(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyTypeKey is marked @NonNull but is null", npe.getMessage());
        }

        try {
            databaseProvider.getPolicies(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyKey is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.createPolicies(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("serviceTemplate is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.updatePolicies(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("serviceTemplate is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.deletePolicies(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyKey is marked @NonNull but is null", npe.getMessage());
        }

        try {
            databaseProvider.getOperationalPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyId is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.createOperationalPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("legacyOperationalPolicy is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.updateOperationalPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("legacyOperationalPolicy is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.deleteOperationalPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyId is marked @NonNull but is null", npe.getMessage());
        }

        try {
            databaseProvider.getGuardPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyId is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.createGuardPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("legacyGuardPolicy is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.updateGuardPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("legacyGuardPolicy is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.deleteGuardPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyId is marked @NonNull but is null", npe.getMessage());
        }

        try {
            databaseProvider.getPdpGroups(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("pdpGroupFilter is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.createPdpGroups(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("pdpGroups is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.updatePdpGroups(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("pdpGroups is marked @NonNull but is null", npe.getMessage());
        }
        try {
            databaseProvider.deletePdpGroups(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("pdpGroupFilter is marked @NonNull but is null", npe.getMessage());
        }

        databaseProvider.close();
    }

    @Test
    public void testProviderMethodsNotInit() throws Exception {
        PolicyModelsProvider databaseProvider =
                new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);
        try {
            databaseProvider.getPolicyTypes(new PfConceptKey());
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policy models provider is not initilaized", npe.getMessage());
        }
    }

    @Test
    public void testProviderMethods() {
        try (PolicyModelsProvider databaseProvider =
                new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters)) {
            databaseProvider.init();

            try {
                databaseProvider.getPolicyTypes(new PfConceptKey());
                fail("test should throw an exception");
            } catch (Exception npe) {
                assertEquals("policy type not found: NULL:0.0.0", npe.getMessage());
            }
            try {
                databaseProvider.createPolicyTypes(new ToscaServiceTemplate());
            } catch (Exception npe) {
                assertEquals("no policy types specified on service template", npe.getMessage());
            }
            try {
                databaseProvider.updatePolicyTypes(new ToscaServiceTemplate());
            } catch (Exception npe) {
                assertEquals("no policy types specified on service template", npe.getMessage());
            }
            try {
                databaseProvider.deletePolicyTypes(new PfConceptKey());
                fail("test should throw an exception");
            } catch (Exception npe) {
                assertEquals("policy type not found: NULL:0.0.0", npe.getMessage());
            }

            try {
                databaseProvider.getPolicies(new PfConceptKey());
                fail("test should throw an exception");
            } catch (Exception npe) {
                assertEquals("policy not found: NULL:0.0.0", npe.getMessage());
            }
            try {
                databaseProvider.createPolicies(new ToscaServiceTemplate());
            } catch (Exception npe) {
                assertEquals("topology template not specified on service template", npe.getMessage());
            }
            try {
                databaseProvider.updatePolicies(new ToscaServiceTemplate());
            } catch (Exception npe) {
                assertEquals("topology template not specified on service template", npe.getMessage());
            }
            try {
                databaseProvider.deletePolicies(new PfConceptKey());
                fail("test should throw an exception");
            } catch (Exception npe) {
                assertEquals("policy not found: NULL:0.0.0", npe.getMessage());
            }

            try {
                assertNull(databaseProvider.getOperationalPolicy("policy_id"));
                fail("test should throw an exception");
            } catch (Exception npe) {
                assertEquals("no policy found for policy ID: policy_id", npe.getMessage());
            }
            try {
                assertNull(databaseProvider.createOperationalPolicy(new LegacyOperationalPolicy()));
                fail("test should throw an exception");
            } catch (Exception npe) {
                assertEquals("name is marked @NonNull but is null", npe.getMessage());
            }
            try {
                assertNull(databaseProvider.updateOperationalPolicy(new LegacyOperationalPolicy()));
                fail("test should throw an exception");
            } catch (Exception npe) {
                assertEquals("no policy found for policy ID: null", npe.getMessage());
            }
            try {
                assertNull(databaseProvider.deleteOperationalPolicy("policy_id"));
                fail("test should throw an exception");
            } catch (Exception npe) {
                assertEquals("no policy found for policy ID: policy_id", npe.getMessage());
            }

            assertNull(databaseProvider.getGuardPolicy("policy_id"));
            assertNull(databaseProvider.createGuardPolicy(new LegacyGuardPolicy()));
            assertNull(databaseProvider.updateGuardPolicy(new LegacyGuardPolicy()));
            assertNull(databaseProvider.deleteGuardPolicy("policy_id"));

            assertNotNull(databaseProvider.getPdpGroups("filter"));
            assertNotNull(databaseProvider.createPdpGroups(new PdpGroups()));
            assertNotNull(databaseProvider.updatePdpGroups(new PdpGroups()));
            assertNotNull(databaseProvider.deletePdpGroups("filter"));

        } catch (Exception exc) {
            LOGGER.warn("test should not throw an exception", exc);
            fail("test should not throw an exception");
        }
    }
}
