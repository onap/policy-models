/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021, 2023-2025 Nordix Foundation.
 *  Modifications Copyright (C) 2020 Bell Canada. All rights reserved.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.provider.revisionhierarchy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.common.utils.resources.TextFileUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

class HierarchyFetchTest {

    private static PolicyModelsProviderParameters parameters;

    private static PolicyModelsProvider databaseProvider;

    @BeforeAll
    static void beforeSetupParameters() {
        parameters = new PolicyModelsProviderParameters();
        parameters.setDatabaseDriver("org.h2.Driver");
        parameters.setDatabaseUrl("jdbc:h2:mem:HierarchyFetchTest");
        parameters.setDatabaseUser("policy");
        parameters.setDatabasePassword("P01icY");
        parameters.setPersistenceUnit("ToscaConceptTest");
    }

    /**
     * Closes the DB.
     */
    @AfterAll
    static void tearDown() throws PfModelException {
        if (databaseProvider != null) {
            databaseProvider.close();
        }
    }

    @Test
    void testMultipleVersions() throws Exception {
        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        ToscaServiceTemplate serviceTemplate = new YamlJsonTranslator().fromYaml(
            TextFileUtils
                .getTextFileAsString("src/test/resources/servicetemplates/MultipleRevisionServiceTemplate.yaml"),
            ToscaServiceTemplate.class);

        assertThatCode(() -> databaseProvider.createPolicies(serviceTemplate)).doesNotThrowAnyException();

        ToscaServiceTemplate fetchedServiceTemplate = databaseProvider.getPolicies(null, null);

        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertLatestPolicyTypesAreReturned(fetchedServiceTemplate);
        assertEquals(12, countReturnedPolicies(fetchedServiceTemplate));

        fetchedServiceTemplate = databaseProvider.getPolicyTypes(null, null);
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertLatestPolicyTypesAreReturned(fetchedServiceTemplate);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0", null);
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(1, fetchedServiceTemplate.getPolicyTypes().size());
        ToscaPolicyType fetchedPolicyType = fetchedServiceTemplate.getPolicyTypes().values().iterator().next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0",
            "3.0.0", fetchedServiceTemplate, 3);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0", "1.0.0");
        assertOldDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(1, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyType = fetchedServiceTemplate.getPolicyTypes().values().iterator().next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0",
            "1.0.0", fetchedServiceTemplate, 1);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0", "1.1.0");
        assertOldDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(1, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyType = fetchedServiceTemplate.getPolicyTypes().values().iterator().next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0",
            "2.0.0", fetchedServiceTemplate, 1);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0", "1.2.0");
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(1, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyType = fetchedServiceTemplate.getPolicyTypes().values().iterator().next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0",
            "3.0.0", fetchedServiceTemplate, 1);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1", null);
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(2, fetchedServiceTemplate.getPolicyTypes().size());
        Iterator<ToscaPolicyType> fetchedPolicyTypeIterator =
            fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1",
            "3.0.0", fetchedServiceTemplate, 3);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1", "1.0.0");
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(2, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyTypeIterator = fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1",
            "1.0.0", fetchedServiceTemplate, 1);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1", "1.1.0");
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(2, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyTypeIterator = fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1",
            "2.0.0", fetchedServiceTemplate, 1);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1", "1.2.0");
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(2, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyTypeIterator = fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1",
            "3.0.0", fetchedServiceTemplate, 1);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1.2", null);
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(3, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyTypeIterator = fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2",
            "3.0.0", fetchedServiceTemplate, 3);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1.2", "1.0.0");
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(3, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyTypeIterator = fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2",
            "1.0.0", fetchedServiceTemplate, 1);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1.2", "1.1.0");
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(3, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyTypeIterator = fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2",
            "2.0.0", fetchedServiceTemplate, 1);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1.2", "1.2.0");
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(3, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyTypeIterator = fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2",
            "3.0.0", fetchedServiceTemplate, 1);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1.2.3", null);
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(4, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyTypeIterator = fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2.3",
            "3.0.0", fetchedServiceTemplate, 3);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1.2.3", "1.0.0");
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(4, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyTypeIterator = fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2.3",
            "1.0.0", fetchedServiceTemplate, 1);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1.2.3", "1.1.0");
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(4, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyTypeIterator = fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2.3",
            "2.0.0", fetchedServiceTemplate, 1);

        fetchedServiceTemplate = databaseProvider.getPolicies("onap.policies.PolicyLevel0.1.2.3", "1.2.0");
        assertLatestDataTypesAreReturned(fetchedServiceTemplate);
        assertEquals(4, fetchedServiceTemplate.getPolicyTypes().size());
        fetchedPolicyTypeIterator = fetchedServiceTemplate.getPolicyTypes().values().iterator();
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkNameVersion(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2", "3.0.0");
        fetchedPolicyType = fetchedPolicyTypeIterator.next();
        checkEqualsPolicyType(fetchedPolicyType, "onap.policies.PolicyTypeLevel0.1.2.3",
            "3.0.0", fetchedServiceTemplate, 1);

        databaseProvider.close();
    }

    private void checkNameVersion(ToscaPolicyType fetchedPolicyType, String name, String ver) {
        assertEquals(name, fetchedPolicyType.getName());
        assertEquals(ver, fetchedPolicyType.getVersion());
    }

    private void checkEqualsPolicyType(ToscaPolicyType fetchedPolicyType, String name, String ver,
        ToscaServiceTemplate fetchedServiceTemplate, int policies) {
        checkNameVersion(fetchedPolicyType, name, ver);
        assertEquals(policies, countReturnedPolicies(fetchedServiceTemplate));
    }

    private void assertOldDataTypesAreReturned(final ToscaServiceTemplate fetchedServiceTemplate) {
        assertEquals(6, fetchedServiceTemplate.getDataTypes().size());
        assertEquals("1.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType0").getVersion());
        assertEquals("1.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType1").getVersion());
        assertEquals("1.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType2").getVersion());
        assertEquals("1.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType3").getVersion());
        assertEquals("1.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType4").getVersion());
        assertEquals("3.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType5").getVersion());
    }

    private void assertLatestDataTypesAreReturned(final ToscaServiceTemplate fetchedServiceTemplate) {
        assertEquals(7, fetchedServiceTemplate.getDataTypes().size());
        assertEquals("1.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType0").getVersion());
        assertEquals("1.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType1").getVersion());
        assertEquals("1.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType2").getVersion());
        assertEquals("1.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType3").getVersion());
        assertEquals("1.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType4").getVersion());
        assertEquals("3.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType5").getVersion());
        assertEquals("2.0.0", fetchedServiceTemplate.getDataTypes().get("policy.data.DataType6").getVersion());
    }

    private void assertLatestPolicyTypesAreReturned(final ToscaServiceTemplate fetchedServiceTemplate) {
        assertEquals(4, fetchedServiceTemplate.getPolicyTypes().size());

        for (ToscaPolicyType policyType : fetchedServiceTemplate.getPolicyTypes().values()) {
            assertEquals("3.0.0", policyType.getVersion());
        }
    }

    private int countReturnedPolicies(final ToscaServiceTemplate fetchedServiceTemplate) {
        List<Map<String, ToscaPolicy>> policyMapList = fetchedServiceTemplate.getToscaTopologyTemplate().getPolicies();

        int totalPolicies = 0;
        for (Map<String, ToscaPolicy> policyMap : policyMapList) {
            totalPolicies += policyMap.size();
        }

        return totalPolicies;
    }
}
