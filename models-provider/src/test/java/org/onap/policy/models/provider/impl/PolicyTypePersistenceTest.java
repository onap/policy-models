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

import com.google.gson.GsonBuilder;

import java.util.Base64;
import java.util.List;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Test persistence of monitoring policies to and from the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PolicyTypePersistenceTest {
    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyTypePersistenceTest.class);

    private StandardCoder standardCoder;

    private PolicyModelsProvider databaseProvider;

    // @formatter:off
    private String[] policyTypeResourceNames = {
        "policytypes/onap.policies.controlloop.Operational.yaml",
        "policytypes/onap.policies.optimization.DistancePolicy.yaml",
        "policytypes/onap.policies.optimization.VnfPolicy.yaml",
        "policytypes/onap.policies.optimization.PciPolicy.yaml",
        "policytypes/onap.policies.optimization.OptimizationPolicy.yaml",
        "policytypes/onap.policies.controlloop.guard.Blacklist.yaml",
        "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server.yaml",
        "policytypes/onap.policies.optimization.HpaPolicy.yaml",
        "policytypes/onap.policies.optimization.Vim_fit.yaml",
        "policytypes/onap.policies.optimization.SubscriberPolicy.yaml",
        "policytypes/onap.policies.optimization.AffinityPolicy.yaml",
        "policytypes/onap.policies.optimization.QueryPolicy.yaml",
        "policytypes/onap.policies.controlloop.guard.MinMax.yaml",
        "policytypes/onap.policies.controlloop.guard.FrequencyLimiter.yaml",
        "policytypes/onap.policies.Optimization.yaml",
        "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.yaml"
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
        parameters.setDatabaseDriver("org.h2.Driver");
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
    public void testPolicyTypePersistence() {
        try {
            for (String policyTypeResourceName : policyTypeResourceNames) {
                String policyTypeString = ResourceUtils.getResourceAsString(policyTypeResourceName);

                if (policyTypeResourceName.endsWith("yaml")) {
                    testYamlStringPolicyTypePersistence(policyTypeString);
                } else {
                    testJsonStringPolicyTypePersistence(policyTypeString);
                }
            }
        } catch (Exception exc) {
            LOGGER.warn("error processing policy types", exc);
            fail("test should not throw an exception");
        }
    }

    private void testYamlStringPolicyTypePersistence(final String policyTypeString) throws Exception {
        Object yamlObject = new Yaml().load(policyTypeString);
        String yamlAsJsonString = new GsonBuilder().setPrettyPrinting().create().toJson(yamlObject);

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
        databaseProvider.updatePolicyTypes(serviceTemplate);

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
        assertTrue(policyTypeList.size() <= 2);
        assertEquals(inPolicyType.getName(), policyTypeList.get(0).getName());

        for (ToscaPolicyType policyType: databaseProvider.getPolicyTypeList(null, null)) {
            databaseProvider.deletePolicyType(policyType.getName(), policyType.getVersion());
        }
    }
}
