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

import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Set;

import lombok.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.yaml.snakeyaml.Yaml;

/**
 * Test persistence of monitoring policies to and from the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PolicyLegacyOperationalPersistenceTest {
    private StandardCoder standardCoder;

    private PolicyModelsProvider databaseProvider;

    // @formatter:off
    private String[] policyInputResourceNames = {
        "policies/vCPE.policy.operational.input.json",
        "policies/vDNS.policy.operational.input.json",
        "policies/vFirewall.policy.operational.input.json"
    };

    private String[] policyOutputResourceNames = {
        "policies/vCPE.policy.operational.output.json",
        "policies/vDNS.policy.operational.output.json",
        "policies/vFirewall.policy.operational.output.json"
    };
    // @formatter:on

    /**
     * Initialize provider.
     *
     * @throws PfModelException on exceptions in the tests
     * @throws CoderException on JSON encoding and decoding errors
     */
    @Before
    public void setupParameters() throws Exception {
        // H2, use "org.mariadb.jdbc.Driver" and "jdbc:mariadb://localhost:3306/policy" for locally installed MariaDB

        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();
        parameters.setDatabaseDriver("org.h2.Driver");
        parameters.setDatabaseUrl("jdbc:h2:mem:testdb");
        parameters.setDatabaseUser("policy");
        parameters.setDatabasePassword(Base64.getEncoder().encodeToString("P01icY".getBytes()));
        parameters.setPersistenceUnit("ToscaConceptTest");

        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        createPolicyTypes();
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
    public void testPolicyPersistence() throws Exception {
        for (int i = 0; i < policyInputResourceNames.length; i++) {
            String policyInputString = ResourceUtils.getResourceAsString(policyInputResourceNames[i]);
            String policyOutputString = ResourceUtils.getResourceAsString(policyOutputResourceNames[i]);
            testJsonStringPolicyPersistence(policyInputString, policyOutputString);
        }
    }

    /**
     * Check persistence of a policy.
     *
     * @param policyInputString the policy as a string
     * @param policyOutputString the expected output string
     * @throws Exception any exception thrown
     */
    public void testJsonStringPolicyPersistence(@NonNull final String policyInputString,
            final String policyOutputString) throws Exception {
        LegacyOperationalPolicy lop = standardCoder.decode(policyInputString, LegacyOperationalPolicy.class);

        assertNotNull(lop);

        LegacyOperationalPolicy createdLop = databaseProvider.createOperationalPolicy(lop);
        assertEquals(createdLop, lop);

        LegacyOperationalPolicy gotLop = databaseProvider.getOperationalPolicy(lop.getPolicyId(), null);
        assertEquals(gotLop, lop);

        LegacyOperationalPolicy updatedLop = databaseProvider.updateOperationalPolicy(lop);
        assertEquals(gotLop, updatedLop);

        LegacyOperationalPolicy deletedLop = databaseProvider.deleteOperationalPolicy(lop.getPolicyId(), "1");
        assertEquals(gotLop, deletedLop);

        String actualRetrievedJson = standardCoder.encode(gotLop);

        // All of this dash/underscore stuff is to avoid a checkstyle error around escaping unicode characters
        assertEquals(
                policyOutputString.replaceAll("\\s+", "").replaceAll("u0027", "_-_-_-_").replaceAll("\\\\_-_-_-_", "'"),
                actualRetrievedJson.replaceAll("\\s+", "").replaceAll("u0027", "_-_-_-_").replaceAll("\\\\_-_-_-_",
                        "'"));
    }

    private void createPolicyTypes() throws CoderException, PfModelException, URISyntaxException {
        Set<String> policyTypeResources = ResourceUtils.getDirectoryContents("policytypes");

        for (String policyTyoeResource : policyTypeResources) {
            Object yamlObject = new Yaml().load(ResourceUtils.getResourceAsString(policyTyoeResource));
            String yamlAsJsonString = new StandardCoder().encode(yamlObject);

            ToscaServiceTemplate toscaServiceTemplatePolicyType =
                    standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

            assertNotNull(toscaServiceTemplatePolicyType);
            databaseProvider.createPolicyTypes(toscaServiceTemplatePolicyType);
        }
    }
}
