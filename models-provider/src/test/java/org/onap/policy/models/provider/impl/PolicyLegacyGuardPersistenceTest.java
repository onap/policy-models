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

import java.util.Base64;
import java.util.Map;

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
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test persistence of monitoring policies to and from the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PolicyLegacyGuardPersistenceTest {
    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyLegacyGuardPersistenceTest.class);

    private StandardCoder standardCoder;

    private PolicyModelsProvider databaseProvider;

    // @formatter:off
    private String[] policyInputResourceNames = {
        "policies/vDNS.policy.guard.frequency.input.json",
        "policies/vDNS.policy.guard.minmax.input.json"
    };

    private String[] policyOutputResourceNames = {
        "policies/vDNS.policy.guard.frequency.output.json",
        "policies/vDNS.policy.guard.minmax.output.json"
    };
    // @formatter:on

    /**
     * Initialize provider.
     *
     * @throws PfModelException on exceptions in the tests
     */
    @Before
    public void setupParameters() throws PfModelException {
        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();
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
    public void testPolicyPersistence() {
        try {
            for (int i = 0; i < policyInputResourceNames.length; i++) {
                String policyInputString = ResourceUtils.getResourceAsString(policyInputResourceNames[i]);
                String policyOutputString = ResourceUtils.getResourceAsString(policyOutputResourceNames[i]);
                testJsonStringPolicyPersistence(policyInputString, policyOutputString);
            }
        } catch (Exception exc) {
            LOGGER.warn("error processing policies", exc);
            fail("test should not throw an exception");
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
        LegacyGuardPolicyInput gip = standardCoder.decode(policyInputString, LegacyGuardPolicyInput.class);

        assertNotNull(gip);

        Map<String, LegacyGuardPolicyOutput> createdGopm = databaseProvider.createGuardPolicy(gip);
        assertEquals(gip.getPolicyId(), createdGopm.keySet().iterator().next());
        assertEquals(gip.getContent(),
                createdGopm.get(gip.getPolicyId()).getProperties().values().iterator().next());

        Map<String, LegacyGuardPolicyOutput> gotGopm = databaseProvider.getGuardPolicy(gip.getPolicyId());
        assertEquals(gip.getPolicyId(), gotGopm.keySet().iterator().next());
        assertEquals(gip.getContent(),
                gotGopm.get(gip.getPolicyId()).getProperties().values().iterator().next());

        String actualRetrievedJson = standardCoder.encode(gotGopm);

        // All of this dash/underscore stuff is to avoid a checkstyle error around escaping unicode characters
        assertEquals(policyOutputString.replaceAll("\\s+", ""), actualRetrievedJson.replaceAll("\\s+", ""));
    }
}
