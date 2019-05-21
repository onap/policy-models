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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

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

        ToscaServiceTemplate serviceTemplate = dummyProvider.getPolicies("onap.vcpe.tca", "1.0.0");
        assertNotNull(serviceTemplate);
        assertEquals("onap.policies.monitoring.cdap.tca.hi.lo.app",
                serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get("onap.vcpe.tca").getType());

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

        assertNotNull(dummyProvider.getPolicyTypes("name", "version"));
        assertNotNull(dummyProvider.getFilteredPolicyTypes(ToscaPolicyTypeFilter.builder().build()));
        assertNotNull(dummyProvider.getPolicyTypeList("name", "version"));
        assertNotNull(dummyProvider.getFilteredPolicyTypeList(ToscaPolicyTypeFilter.builder().build()));
        assertNotNull(dummyProvider.createPolicyTypes(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.updatePolicyTypes(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.deletePolicyType("name", "version"));

        assertNotNull(dummyProvider.getPolicies("name", "version"));
        assertNotNull(dummyProvider.getFilteredPolicies(ToscaPolicyFilter.builder().build()));
        assertNotNull(dummyProvider.getPolicyList("name", "version"));
        assertNotNull(dummyProvider.getFilteredPolicyList(ToscaPolicyFilter.builder().build()));
        assertNotNull(dummyProvider.createPolicies(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.updatePolicies(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.deletePolicy("name", "version"));

        assertNotNull(dummyProvider.getOperationalPolicy("policy_id", "1"));
        assertNotNull(dummyProvider.createOperationalPolicy(new LegacyOperationalPolicy()));
        assertNotNull(dummyProvider.updateOperationalPolicy(new LegacyOperationalPolicy()));
        assertNotNull(dummyProvider.deleteOperationalPolicy("policy_id", "1"));

        assertNotNull(dummyProvider.getGuardPolicy("policy_id", "1"));
        assertNotNull(dummyProvider.createGuardPolicy(new LegacyGuardPolicyInput()));
        assertNotNull(dummyProvider.updateGuardPolicy(new LegacyGuardPolicyInput()));
        assertNotNull(dummyProvider.deleteGuardPolicy("policy_id", "1"));

        assertTrue(dummyProvider.getPdpGroups("name").isEmpty());
        assertTrue(dummyProvider.getFilteredPdpGroups(PdpGroupFilter.builder().build()).isEmpty());
        assertTrue(dummyProvider.createPdpGroups(new ArrayList<>()).isEmpty());
        assertTrue(dummyProvider.updatePdpGroups(new ArrayList<>()).isEmpty());
        assertNull(dummyProvider.deletePdpGroup("name"));

        dummyProvider.updatePdpSubGroup("name", new PdpSubGroup());
        dummyProvider.updatePdp("name", "type", new Pdp());
        dummyProvider.updatePdpStatistics("name", "type", "type-0", new PdpStatistics());
        assertTrue(dummyProvider.getPdpStatistics("name").isEmpty());

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
            assertEquals("error serializing object", npe.getMessage());
        } finally {
            if (resp != null) {
                resp.close();
            }
        }
    }
}
