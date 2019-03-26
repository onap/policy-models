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
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Base64;

import lombok.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.serialization.ToscaServiceTemplateMessageBodyHandler;
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

    private Gson gson;

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
        databaseProvider.init();
    }

    /**
     * Set up GSON.
     */
    @Before
    public void setupGson() {
        gson = new ToscaServiceTemplateMessageBodyHandler().getGson();
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
        ToscaServiceTemplate serviceTemplate = gson.fromJson(policyString, ToscaServiceTemplate.class);

        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        databaseProvider.createPolicies(serviceTemplate);

        for (PfConceptKey policyKey : serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().keySet()) {
            ToscaPolicy incomingPolicy = serviceTemplate.getTopologyTemplate().getPolicies().get(policyKey);
            ToscaPolicy databasePolicy =
                    databaseProvider.getPolicies(policyKey).getTopologyTemplate().getPolicies().get(policyKey);
            assertEquals(incomingPolicy, databasePolicy);
        }
    }
}
