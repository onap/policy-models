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
import static org.junit.Assert.assertTrue;

import java.util.Base64;
import java.util.List;
import java.util.Set;

import lombok.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.yaml.snakeyaml.Yaml;

/**
 * Test persistence of monitoring policies to and from the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PolicyTypePersistenceTest {
    private StandardCoder standardCoder;

    private PolicyModelsProvider databaseProvider;

    /**
     * Initialize provider.
     *
     * @throws PfModelException on exceptions in the tests
     */
    @Before
    public void setupParameters() throws PfModelException {
        // H2, use "org.mariadb.jdbc.Driver" and "jdbc:mariadb://localhost:3306/policy" for locally installed MariaDB

        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();
        parameters.setDatabaseDriver("org.h2.Driver");
        parameters.setDatabaseUrl("jdbc:h2:mem:testdb");
        parameters.setDatabaseUser("policy");
        parameters.setDatabasePassword(Base64.getEncoder().encodeToString("P01icY".getBytes()));
        parameters.setPersistenceUnit("ToscaConceptTest");

        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);
    }

    /**
     * Set up standard coder.
     */
    @Before
    public void setupStandardCoder() {
        standardCoder = new StandardCoder();
    }

    @After
    public void teardown() throws Exception {
        databaseProvider.close();
    }

    @Test
    public void testPolicyTypePersistence() throws Exception {
        Set<String> policyTypeDirectoryContents = ResourceUtils.getDirectoryContents("policytypes");

        for (String policyTypeFilePath : policyTypeDirectoryContents) {
            String policyTypeString = ResourceUtils.getResourceAsString(policyTypeFilePath);
            testYamlStringPolicyTypePersistence(policyTypeString);
        }
    }

    private void testYamlStringPolicyTypePersistence(final String policyTypeString) throws Exception {
        Object yamlObject = new Yaml().load(policyTypeString);
        String yamlAsJsonString = new StandardCoder().encode(yamlObject);

        testJsonStringPolicyTypePersistence(yamlAsJsonString);
    }

    /**
     * Check persistence of a policy.
     *
     * @param policyTypeString the policy as a string
     * @throws Exception any exception thrown
     */
    public void testJsonStringPolicyTypePersistence(@NonNull final String policyTypeString) throws Exception {
        ToscaServiceTemplate serviceTemplate = standardCoder.decode(policyTypeString, ToscaServiceTemplate.class);

        assertNotNull(serviceTemplate);
        ToscaPolicyType inPolicyType = serviceTemplate.getPolicyTypes().values().iterator().next();

        databaseProvider.createPolicyTypes(serviceTemplate);
        checkPolicyTypePersistence(inPolicyType);

        databaseProvider.updatePolicyTypes(serviceTemplate);
        checkPolicyTypePersistence(inPolicyType);
    }

    private void checkPolicyTypePersistence(ToscaPolicyType inPolicyType) throws PfModelException {
        List<ToscaPolicyType> policyTypeList =
                databaseProvider.getPolicyTypeList(inPolicyType.getName(), inPolicyType.getVersion());

        policyTypeList = databaseProvider.getFilteredPolicyTypeList(ToscaPolicyTypeFilter.builder()
                .name(inPolicyType.getName()).version(inPolicyType.getVersion()).build());

        assertEquals(1, policyTypeList.size());
        assertEquals(inPolicyType.getName(), policyTypeList.get(0).getName());

        policyTypeList = databaseProvider
                .getFilteredPolicyTypeList(ToscaPolicyTypeFilter.builder().name(inPolicyType.getName()).build());

        assertEquals(1, policyTypeList.size());
        assertEquals(inPolicyType.getName(), policyTypeList.get(0).getName());

        policyTypeList = databaseProvider.getFilteredPolicyTypeList(ToscaPolicyTypeFilter.builder().build());
        assertTrue(policyTypeList.size() <= 3);
        assertEquals(inPolicyType.getName(), policyTypeList.get(0).getName());

        for (ToscaPolicyType policyType : databaseProvider.getPolicyTypeList(null, null)) {
            databaseProvider.deletePolicyType(policyType.getName(), policyType.getVersion());
        }
    }
}
