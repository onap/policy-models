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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import org.junit.Test;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pap.persistence.provider.PolicyAuditProvider.AuditFilter;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.persistence.provider.PdpFilterParameters;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTypedEntityFilter;

/**
 * Test the dummy models provider implementation.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DummyPolicyModelsProviderTest {

    private static final String VERSION = "version";

    @Test
    public void testProvider() throws Exception {
        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();
        parameters.setImplementation(DummyPolicyModelsProviderImpl.class.getName());
        parameters.setDatabaseUrl("jdbc:dummy");
        parameters.setPersistenceUnit("dummy");

        try (PolicyModelsProvider dummyProvider =
                new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters)) {

            dummyProvider.init();

            ToscaServiceTemplate serviceTemplate = dummyProvider.getPolicies("onap.vcpe.tca", "1.0.0");
            assertNotNull(serviceTemplate);
            assertEquals("onap.policies.monitoring.cdap.tca.hi.lo.app",
                    serviceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get("onap.vcpe.tca").getType());
        }
    }

    @Test
    public void testProviderMethods() throws Exception {
        PolicyModelsProvider dummyProvider = setUpDummyProvider();
        dummyProvider.init();

        assertNotNull(dummyProvider.getPolicyTypes("name", VERSION));
        assertNotNull(dummyProvider.getFilteredPolicyTypes(ToscaEntityFilter.<ToscaPolicyType>builder().build()));
        assertNotNull(dummyProvider.getPolicyTypeList("name", VERSION));
        assertNotNull(dummyProvider.getFilteredPolicyTypeList(ToscaEntityFilter.<ToscaPolicyType>builder().build()));
        assertNotNull(dummyProvider.createPolicyTypes(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.updatePolicyTypes(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.deletePolicyType("name", VERSION));

        assertNotNull(dummyProvider.getPolicies("name", VERSION));
        assertNotNull(dummyProvider.getFilteredPolicies(ToscaTypedEntityFilter.<ToscaPolicy>builder().build()));
        assertNotNull(dummyProvider.getPolicyList("name", VERSION));
        assertNotNull(dummyProvider.getFilteredPolicyList(ToscaTypedEntityFilter.<ToscaPolicy>builder().build()));
        assertNotNull(dummyProvider.createPolicies(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.updatePolicies(new ToscaServiceTemplate()));
        assertNotNull(dummyProvider.deletePolicy("name", VERSION));

        assertTrue(dummyProvider.getPdpGroups("name").isEmpty());
        assertTrue(dummyProvider.getFilteredPdpGroups(PdpGroupFilter.builder().build()).isEmpty());
        assertTrue(dummyProvider.createPdpGroups(new ArrayList<>()).isEmpty());
        assertTrue(dummyProvider.updatePdpGroups(new ArrayList<>()).isEmpty());
        assertNull(dummyProvider.deletePdpGroup("name"));
    }

    @Test
    public void testProviderMethodsParameters() throws Exception {
        PolicyModelsProvider dummyProvider = setUpDummyProvider();
        dummyProvider.init();

        dummyProvider.updatePdpSubGroup("name", new PdpSubGroup());
        dummyProvider.updatePdp("name", "type", new Pdp());
        dummyProvider.updatePdpStatistics(new ArrayList<>());
        assertTrue(dummyProvider.getPdpStatistics("name", null).isEmpty());

        assertTrue(
                dummyProvider.getFilteredPdpStatistics(
                                PdpFilterParameters.builder().name("name")
                                .startTime(Instant.now()).endTime(Instant.now()).build()).isEmpty());
        assertTrue(dummyProvider.createPdpStatistics(null).isEmpty());
        assertTrue(dummyProvider.updatePdpStatistics(null).isEmpty());
        assertTrue(dummyProvider.deletePdpStatistics(null, Instant.now()).isEmpty());

        assertThat(dummyProvider.getAllPolicyStatus()).isEmpty();
        assertThat(dummyProvider.getAllPolicyStatus(new ToscaConceptIdentifierOptVersion("MyPolicy",
            null))).isEmpty();
        assertThat(dummyProvider.getGroupPolicyStatus("name")).isEmpty();
        assertThatCode(() -> dummyProvider.cudPolicyStatus(null, null,
            null)).doesNotThrowAnyException();
        assertThatCode(() -> dummyProvider.createAuditRecords(null)).doesNotThrowAnyException();
        assertThat(dummyProvider.getAuditRecords(AuditFilter.builder().recordNum(10).build())).isEmpty();
    }

    @Test
    public void testDummyResponse() {
        try (DummyPolicyModelsProviderSubImpl resp =
                new DummyPolicyModelsProviderSubImpl(new PolicyModelsProviderParameters())) {
            assertThatThrownBy(resp::getBadDummyResponse1).hasMessage("error serializing object");
        }

        try (DummyPolicyModelsProviderSubImpl resp =
                new DummyPolicyModelsProviderSubImpl(new PolicyModelsProviderParameters())) {
            assertThatThrownBy(resp::getBadDummyResponse2).hasMessage("error serializing object");
        }
    }

    private PolicyModelsProvider setUpDummyProvider() throws PfModelException {
        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();
        parameters.setImplementation(DummyPolicyModelsProviderImpl.class.getName());
        parameters.setDatabaseUrl("jdbc:dummy");
        parameters.setPersistenceUnit("dummy");

        PolicyModelsProvider dummyProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);
        return dummyProvider;
    }
}
