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
import static org.junit.Assert.fail;

import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.pap.concepts.PdpGroups;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicy;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;

/**
 * Test the dummy models provider implementation.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DummyPolicyModelsProviderTest {

    @Test
    public void testProvider() throws Exception {
        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();
        parameters.setImplementation(DummyPolicyModelsProviderImpl.class.getCanonicalName());
        parameters.setDatabaseUrl("jdbc:dummy");
        parameters.setPersistenceUnit("dummy");

        PolicyModelsProvider dummyProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        dummyProvider.init();

        ToscaServiceTemplate serviceTemplate = dummyProvider.getPolicies(new PfConceptKey());
        assertNotNull(serviceTemplate);
        assertEquals("onap.vcpe.tca:1.0.0",
                serviceTemplate.getTopologyTemplate().getPolicies().get("onap.vcpe.tca").getId());

        dummyProvider.close();
    }

    @Test
    public void testProviderMethods() throws Exception {
        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();
        parameters.setImplementation(DummyPolicyModelsProviderImpl.class.getCanonicalName());
        parameters.setDatabaseUrl("jdbc:dummy");
        parameters.setPersistenceUnit("dummy");

        PolicyModelsProvider dummyProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);
        dummyProvider.init();

        assertNotNull(dummyProvider.getPolicyTypes(new PfConceptKey()));
        assertNotNull(dummyProvider.createPolicyTypes(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.updatePolicyTypes(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.deletePolicyTypes(new PfConceptKey()));

        assertNotNull(dummyProvider.getPolicies(new PfConceptKey()));
        assertNotNull(dummyProvider.createPolicies(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.updatePolicies(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.deletePolicies(new PfConceptKey()));

        assertNotNull(dummyProvider.getOperationalPolicy("policy_id"));
        assertNotNull(dummyProvider.createOperationalPolicy(new LegacyOperationalPolicy()));
        assertNotNull(dummyProvider.updateOperationalPolicy(new LegacyOperationalPolicy()));
        assertNotNull(dummyProvider.deleteOperationalPolicy("policy_id"));

        assertNotNull(dummyProvider.getGuardPolicy("policy_id"));
        assertNotNull(dummyProvider.createGuardPolicy(new LegacyGuardPolicy()));
        assertNotNull(dummyProvider.updateGuardPolicy(new LegacyGuardPolicy()));
        assertNotNull(dummyProvider.deleteGuardPolicy("policy_id"));

        assertNotNull(dummyProvider.getPdpGroups("filter"));
        assertNotNull(dummyProvider.createPdpGroups(new PdpGroups()));
        assertNotNull(dummyProvider.updatePdpGroups(new PdpGroups()));
        assertNotNull(dummyProvider.deletePdpGroups("filter"));

        try {
            dummyProvider.getPolicyTypes(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyTypeKey is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.createPolicyTypes(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("serviceTemplate is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.updatePolicyTypes(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("serviceTemplate is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.deletePolicyTypes(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyTypeKey is marked @NonNull but is null", npe.getMessage());
        }

        try {
            dummyProvider.getPolicies(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyKey is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.createPolicies(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("serviceTemplate is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.updatePolicies(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("serviceTemplate is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.deletePolicies(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyKey is marked @NonNull but is null", npe.getMessage());
        }

        try {
            dummyProvider.getOperationalPolicy(null);


            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyId is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.createOperationalPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("legacyOperationalPolicy is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.updateOperationalPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("legacyOperationalPolicy is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.deleteOperationalPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyId is marked @NonNull but is null", npe.getMessage());
        }

        try {
            dummyProvider.getGuardPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyId is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.createGuardPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("legacyGuardPolicy is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.updateGuardPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("legacyGuardPolicy is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.deleteGuardPolicy(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("policyId is marked @NonNull but is null", npe.getMessage());
        }

        try {


            dummyProvider.getPdpGroups(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("pdpGroupFilter is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.createPdpGroups(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("pdpGroups is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.updatePdpGroups(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("pdpGroups is marked @NonNull but is null", npe.getMessage());
        }
        try {
            dummyProvider.deletePdpGroups(null);
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("pdpGroupFilter is marked @NonNull but is null", npe.getMessage());
        }

        dummyProvider.close();
    }

    @Test
    public void testDummyResponse() {
        DummyPolicyModelsProviderSubImpl resp = null;

        try {
            resp = new DummyPolicyModelsProviderSubImpl(new PolicyModelsProviderParameters());
            resp.getBadDummyResponse1();
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("error serializing object", npe.getMessage());
        } finally {
            if (resp != null) {
                resp.close();
            }
        }

        try {
            resp = new DummyPolicyModelsProviderSubImpl(new PolicyModelsProviderParameters());
            resp.getBadDummyResponse2();
            fail("test should throw an exception");
        } catch (Exception npe) {
            assertEquals("fileName is marked @NonNull but is null", npe.getMessage());
        } finally {
            if (resp != null) {
                resp.close();
            }
        }
    }
}
