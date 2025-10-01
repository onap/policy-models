/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2023-2025 Nordix Foundation.
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

package org.onap.policy.models.provider.impl;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTypedEntityFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test persistence of monitoring policies to and from the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class PolicyToscaPersistenceTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PolicyToscaPersistenceTest.class);

    private static final YamlJsonTranslator yamlJsonTranslator = new YamlJsonTranslator();
    private final StandardCoder standardCoder = new StandardCoder();

    private static PolicyModelsProvider databaseProvider;

    /**
     * Initialize provider.
     *
     * @throws PfModelException on exceptions in the tests
     * @throws CoderException on JSON encoding and decoding errors
     */
    @BeforeAll
    static void setupParameters() throws Exception {

        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();

        parameters.setDatabaseDriver("org.h2.Driver");
        parameters.setDatabaseUrl("jdbc:h2:mem:PolicyToscaPersistenceTest");

        parameters.setDatabaseUser("policy");
        parameters.setDatabasePassword("P01icY");
        parameters.setPersistenceUnit("ToscaConceptTest");


        databaseProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);

        createPolicyTypes();
    }

    @AfterAll
    static void teardown() throws Exception {
        databaseProvider.close();
    }

    @Test
    void testToscaPolicyPersistence() throws Exception {
        Set<String> policyResources = ResourceUtils.getDirectoryContents("policies");

        for (String policyResource : policyResources) {
            if (!policyResource.contains("\\.tosca\\.")) {
                continue;
            }

            String policyString = ResourceUtils.getResourceAsString(policyResource);

            if (policyResource.endsWith("yaml")) {
                testPolicyPersistence(yamlJsonTranslator.fromYaml(policyString, ToscaServiceTemplate.class));
            } else {
                testPolicyPersistence(standardCoder.decode(policyString, ToscaServiceTemplate.class));
            }
        }
    }

    @Test
    void testHpaPolicyTypeGet() throws PfModelException {
        long getStartTime = System.currentTimeMillis();
        ToscaServiceTemplate hpaServiceTemplate =
                databaseProvider.getPolicyTypes("onap.policies.optimization.resource.HpaPolicy", "1.0.0");
        LOGGER.trace("HPA policy normal get time (ms): {}", System.currentTimeMillis() - getStartTime);

        assertEquals(3, hpaServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(5, hpaServiceTemplate.getDataTypesAsMap().size());

        getStartTime = System.currentTimeMillis();
        ToscaEntityFilter<ToscaPolicyType> hpaFilter = ToscaEntityFilter.<ToscaPolicyType>builder()
                .name("onap.policies.optimization.resource.HpaPolicy").version("1.0.0").build();
        hpaServiceTemplate = databaseProvider.getFilteredPolicyTypes(hpaFilter);
        LOGGER.trace("HPA policy filter name version get time (ms): {}", System.currentTimeMillis() - getStartTime);

        assertEquals(3, hpaServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(5, hpaServiceTemplate.getDataTypesAsMap().size());

        getStartTime = System.currentTimeMillis();
        hpaFilter = ToscaEntityFilter.<ToscaPolicyType>builder().name("onap.policies.optimization.resource.HpaPolicy")
                .build();
        hpaServiceTemplate = databaseProvider.getFilteredPolicyTypes(hpaFilter);
        LOGGER.trace("HPA policy filter name only get time (ms): {}", System.currentTimeMillis() - getStartTime);

        assertEquals(3, hpaServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(5, hpaServiceTemplate.getDataTypesAsMap().size());
    }

    @Test
    void testNamingPolicyGet() throws PfModelException {
        String policyYamlString = ResourceUtils.getResourceAsString("policies/sdnc.policy.naming.input.tosca.yaml");
        ToscaServiceTemplate serviceTemplate =
                yamlJsonTranslator.fromYaml(policyYamlString, ToscaServiceTemplate.class);

        long createStartTime = System.currentTimeMillis();
        databaseProvider.createPolicies(serviceTemplate);
        LOGGER.trace("Naming policy create time (ms): {}", System.currentTimeMillis() - createStartTime);

        long getStartTime = System.currentTimeMillis();
        ToscaServiceTemplate namingServiceTemplate =
                databaseProvider.getPolicies("SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP", "1.0.0");
        LOGGER.trace("Naming policy normal get time (ms): {}", System.currentTimeMillis() - getStartTime);

        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, namingServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(3, namingServiceTemplate.getDataTypesAsMap().size());

        getStartTime = System.currentTimeMillis();
        ToscaTypedEntityFilter<ToscaPolicy> filter = ToscaTypedEntityFilter.<ToscaPolicy>builder()
                .name("SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP").version("1.0.0").build();
        namingServiceTemplate = databaseProvider.getFilteredPolicies(filter);
        LOGGER.trace("Naming policy filtered get time (ms): {}", System.currentTimeMillis() - getStartTime);

        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, namingServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(3, namingServiceTemplate.getDataTypesAsMap().size());

        getStartTime = System.currentTimeMillis();
        filter = ToscaTypedEntityFilter.<ToscaPolicy>builder().name("SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP").build();
        namingServiceTemplate = databaseProvider.getFilteredPolicies(filter);
        LOGGER.trace("Naming policy filtered name only get time (ms): {}", System.currentTimeMillis() - getStartTime);

        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, namingServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(3, namingServiceTemplate.getDataTypesAsMap().size());

        long deleteStartTime = System.currentTimeMillis();
        ToscaServiceTemplate deletedServiceTemplate =
                databaseProvider.deletePolicy("SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP", "1.0.0");
        LOGGER.trace("Naming policy delete time (ms): {}", System.currentTimeMillis() - deleteStartTime);

        assertEquals(1, deletedServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
    }

    @Test
    void testNamingPolicyVersions() throws PfModelException {
        String policyYamlString = ResourceUtils.getResourceAsString("policies/sdnc.policy.naming.input.tosca.yaml");
        ToscaServiceTemplate serviceTemplate =
                yamlJsonTranslator.fromYaml(policyYamlString, ToscaServiceTemplate.class);

        // Create policy types and data types
        List<Map<String, ToscaPolicy>> policyMapList = serviceTemplate.getToscaTopologyTemplate().getPolicies();
        databaseProvider.createPolicies(serviceTemplate);

        // Clear the policy map list so we start from afresh with versions
        ToscaPolicy namingPolicy = policyMapList.get(0).values().iterator().next();
        policyMapList.clear();

        // Create 21 more versions of the policy
        for (int i = 2; i < 22; i++) {
            ToscaPolicy clonedNamingPolicy = new ToscaPolicy(namingPolicy);
            clonedNamingPolicy.setVersion(i + ".0.0");
            Map<String, ToscaPolicy> policyMap = new LinkedHashMap<>(1);
            policyMap.put(clonedNamingPolicy.getName(), clonedNamingPolicy);
            policyMapList.add(policyMap);
        }

        databaseProvider.createPolicies(serviceTemplate);

        for (int i = 1; i < 22; i++) {
            ToscaServiceTemplate namingServiceTemplate =
                    databaseProvider.getPolicies("SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP", i + ".0.0");
            assertEquals(i + ".0.0", namingServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).values()
                    .iterator().next().getVersion());

            ToscaTypedEntityFilter<ToscaPolicy> policyFilter = ToscaTypedEntityFilter.<ToscaPolicy>builder()
                    .name("SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP").version(i + ".0.0").build();
            namingServiceTemplate = databaseProvider.getFilteredPolicies(policyFilter);
            assertEquals(i + ".0.0", namingServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).values()
                    .iterator().next().getVersion());
        }

        ToscaTypedEntityFilter<ToscaPolicy> policyFilter = ToscaTypedEntityFilter.<ToscaPolicy>builder()
                .name("SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP").version(ToscaTypedEntityFilter.LATEST_VERSION).build();
        ToscaServiceTemplate namingServiceTemplate = databaseProvider.getFilteredPolicies(policyFilter);
        assertEquals("21.0.0", namingServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).values().iterator()
                .next().getVersion());

        for (int i = 1; i < 22; i++) {
            ToscaServiceTemplate deletedServiceTemplate =
                    databaseProvider.deletePolicy("SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP", i + ".0.0");
            assertEquals(1, deletedServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        }
    }

    /**
     * Check persistence of a policy.
     *
     * @param serviceTemplate the service template containing the policy
     * @throws Exception any exception thrown
     */
    void testPolicyPersistence(@NonNull final ToscaServiceTemplate serviceTemplate) throws Exception {
        assertNotNull(serviceTemplate);

        CountDownLatch threadCountDownLatch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                assertThatCode(() -> databaseProvider.createPolicies(serviceTemplate)).doesNotThrowAnyException();
                assertThatCode(() -> databaseProvider.updatePolicies(serviceTemplate)).doesNotThrowAnyException();
                threadCountDownLatch.countDown();
            }).start();
        }

        threadCountDownLatch.await(10, TimeUnit.SECONDS);

        for (Map<String, ToscaPolicy> policyMap : serviceTemplate.getToscaTopologyTemplate().getPolicies()) {
            for (ToscaPolicy policy : policyMap.values()) {
                ToscaServiceTemplate gotToscaServiceTemplate =
                        databaseProvider.getPolicies(policy.getName(), policy.getVersion());

                assertEquals(policy.getType(), gotToscaServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0)
                        .get(policy.getName()).getType());

                gotToscaServiceTemplate =
                        databaseProvider.getFilteredPolicies(ToscaTypedEntityFilter.<ToscaPolicy>builder().build());

                assertEquals(policy.getType(),
                        getToscaPolicyFromMapList(gotToscaServiceTemplate.getToscaTopologyTemplate().getPolicies(),
                                policy.getName()).getType());

                gotToscaServiceTemplate = databaseProvider.getFilteredPolicies(ToscaTypedEntityFilter
                        .<ToscaPolicy>builder().name(policy.getName()).version(policy.getVersion()).build());

                assertEquals(policy.getType(), gotToscaServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0)
                        .get(policy.getName()).getType());
            }
        }
    }

    private ToscaPolicy getToscaPolicyFromMapList(List<Map<String, ToscaPolicy>> toscaPolicyMapList,
            String policyName) {
        ToscaPolicy toscaPolicy = new ToscaPolicy();
        for (Map<String, ToscaPolicy> policyMap : toscaPolicyMapList) {
            toscaPolicy = policyMap.get(policyName);
            if (toscaPolicy != null) {
                break;
            }
        }
        return toscaPolicy;
    }

    private static void createPolicyTypes() throws PfModelException {
        Set<String> policyTypeResources = ResourceUtils.getDirectoryContents("policytypes");

        for (String policyTypeResource : policyTypeResources) {
            String policyTypeYamlString = ResourceUtils.getResourceAsString(policyTypeResource);
            ToscaServiceTemplate toscaServiceTemplatePolicyType =
                    yamlJsonTranslator.fromYaml(policyTypeYamlString, ToscaServiceTemplate.class);

            assertNotNull(toscaServiceTemplatePolicyType);
            databaseProvider.createPolicyTypes(toscaServiceTemplatePolicyType);
        }
    }
}
