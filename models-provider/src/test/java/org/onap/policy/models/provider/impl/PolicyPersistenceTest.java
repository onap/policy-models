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

import com.google.gson.GsonBuilder;

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
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Test persistence of monitoring policies to and from the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PolicyPersistenceTest {
    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyPersistenceTest.class);

    private StandardCoder standardCoder;

    private PolicyModelsProvider databaseProvider;

    // @formatter:off
    private String[] policyResourceNames = {
        "policies/vCPE.policy.monitoring.input.tosca.json",
        "policies/vCPE.policy.monitoring.input.tosca.yaml",
        "policies/vCPE.policy.operational.input.tosca.yaml",
        "policies/vDNS.policy.guard.frequency.input.tosca.json",
        "policies/vDNS.policy.guard.frequency.input.tosca.yaml",
        "policies/vDNS.policy.monitoring.input.tosca.json",
        "policies/vDNS.policy.monitoring.input.tosca.yaml",
        "policies/vDNS.policy.operational.input.tosca.yaml",
        "policies/vFirewall.policy.monitoring.input.tosca.json",
        "policies/vFirewall.policy.monitoring.input.tosca.yaml",
        "policies/vFirewall.policy.operational.input.tosca.json",
        "policies/vFirewall.policy.operational.input.tosca.yaml"
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
     * Set up GSON.
     */
    @Before
    public void setupGson() {
        standardCoder = new StandardCoder();
    }

    @After
    public void teardown() throws Exception {
        databaseProvider.close();
    }

    @Test
    public void testPolicyPersistence() {
        try {
            for (String policyResourceName : policyResourceNames) {
                String policyString = ResourceUtils.getResourceAsString(policyResourceName);

                if (policyResourceName.endsWith("yaml")) {
                    testYamlStringPolicyPersistence(policyString);
                } else {
                    testJsonStringPolicyPersistence(policyString);
                }
            }
        } catch (Exception exc) {
            LOGGER.warn("error processing policies", exc);
            fail("test should not throw an exception");
        }
    }

    private void testYamlStringPolicyPersistence(final String policyString) throws Exception {
        Object yamlObject = new Yaml().load(policyString);
        String yamlAsJsonString = new GsonBuilder().setPrettyPrinting().create().toJson(yamlObject);

        testJsonStringPolicyPersistence(yamlAsJsonString);
    }

    /**
     * Check persistence of a policy.
     *
     * @param policyString the policy as a string
     * @throws Exception any exception thrown
     */
    public void testJsonStringPolicyPersistence(@NonNull final String policyString) throws Exception {
        ToscaServiceTemplate serviceTemplate = standardCoder.decode(policyString, ToscaServiceTemplate.class);

        assertNotNull(serviceTemplate);

        databaseProvider.createPolicies(serviceTemplate);
        databaseProvider.updatePolicies(serviceTemplate);

        for (Map<String, ToscaPolicy> policyMap : serviceTemplate.getToscaTopologyTemplate().getPolicies()) {
            for (ToscaPolicy policy : policyMap.values()) {
                ToscaServiceTemplate gotToscaServiceTemplate =
                        databaseProvider.getPolicies(policy.getName(), policy.getVersion());

                assertEquals(gotToscaServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0)
                        .get(policy.getName()).getType(), policy.getType());

                gotToscaServiceTemplate = databaseProvider.getFilteredPolicies(ToscaPolicyFilter.builder().build());

                assertEquals(gotToscaServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0)
                        .get(policy.getName()).getType(), policy.getType());

                gotToscaServiceTemplate = databaseProvider.getFilteredPolicies(
                        ToscaPolicyFilter.builder().name(policy.getName()).version(policy.getVersion()).build());

                assertEquals(gotToscaServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0)
                        .get(policy.getName()).getType(), policy.getType());
            }
        }
    }
}
