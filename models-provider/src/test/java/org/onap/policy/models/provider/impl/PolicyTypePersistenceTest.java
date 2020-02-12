/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Base64;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * Test persistence of monitoring policies to and from the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PolicyTypePersistenceTest {
    private YamlJsonTranslator yamlTranslator = new YamlJsonTranslator();
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

    @After
    public void teardown() throws Exception {
        databaseProvider.close();
    }

    @Test
    public void testPolicyTypePersistence() throws Exception {
        Set<String> policyTypeDirectoryContents = ResourceUtils.getDirectoryContents("policytypes");

        ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();

        for (String policyTypeFilePath : policyTypeDirectoryContents) {
            String policyTypeString = ResourceUtils.getResourceAsString(policyTypeFilePath);

            ToscaServiceTemplate foundPolicyTypeSt =
                    yamlTranslator.fromYaml(policyTypeString, ToscaServiceTemplate.class);

            serviceTemplate.setDerivedFrom(foundPolicyTypeSt.getDerivedFrom());
            serviceTemplate.setDescription(foundPolicyTypeSt.getDescription());
            serviceTemplate.setMetadata(foundPolicyTypeSt.getMetadata());
            serviceTemplate.setName(foundPolicyTypeSt.getName());
            serviceTemplate.setToscaDefinitionsVersion(foundPolicyTypeSt.getToscaDefinitionsVersion());
            serviceTemplate.setToscaTopologyTemplate(foundPolicyTypeSt.getToscaTopologyTemplate());
            serviceTemplate.setVersion(foundPolicyTypeSt.getVersion());

            if (foundPolicyTypeSt.getDataTypes() != null) {
                if (serviceTemplate.getDataTypes() == null) {
                    serviceTemplate.setDataTypes(foundPolicyTypeSt.getDataTypes());
                } else {
                    serviceTemplate.getDataTypes().putAll(foundPolicyTypeSt.getDataTypes());
                }
            }

            if (serviceTemplate.getPolicyTypes() == null) {
                serviceTemplate.setPolicyTypes(foundPolicyTypeSt.getPolicyTypes());
            } else {
                serviceTemplate.getPolicyTypes().putAll(foundPolicyTypeSt.getPolicyTypes());
            }
        }

        assertThatCode(() -> databaseProvider.createPolicyTypes(serviceTemplate)).doesNotThrowAnyException();

        ToscaEntityKey resourceOptimizationPtKey =
                new ToscaEntityKey("onap.policies.optimization.resource.OptimizationPolicy", "1.0.0");

        ToscaServiceTemplate resOptPolicyTypeSt = databaseProvider.getPolicyTypes(resourceOptimizationPtKey.getName(),
                resourceOptimizationPtKey.getVersion());

        assertEquals(3, resOptPolicyTypeSt.getPolicyTypesAsMap().size());
        assertTrue(resOptPolicyTypeSt.getPolicyTypesAsMap().containsKey(resourceOptimizationPtKey));

        ToscaEntityKey resourcePtKey = new ToscaEntityKey("onap.policies.optimization.Resource", "1.0.0");
        assertTrue(resOptPolicyTypeSt.getPolicyTypesAsMap().containsKey(resourcePtKey));

        ToscaEntityKey optimizationPtKey = new ToscaEntityKey("onap.policies.Optimization", "1.0.0");
        assertTrue(resOptPolicyTypeSt.getPolicyTypesAsMap().containsKey(optimizationPtKey));

        assertEquals(2, resOptPolicyTypeSt.getDataTypesAsMap().size());
    }
}
