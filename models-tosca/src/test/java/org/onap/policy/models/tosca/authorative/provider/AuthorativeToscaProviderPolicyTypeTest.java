/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2023-2025 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.tosca.authorative.provider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.GsonBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;
import org.yaml.snakeyaml.Yaml;

/**
 * Test of the {@link AuthorativeToscaProvider} class.
 */
class AuthorativeToscaProviderPolicyTypeTest {
    private static final String VERSION = "version";
    private static final String POLICY_NO_VERSION_VERSION1 = "onap.policies.NoVersion:0.0.1";
    private static final String POLICY_NO_VERSION = "onap.policies.NoVersion";
    private static final String MISSING_POLICY_TYPES = "no policy types specified on service template";
    private static final String DAO_IS_NULL = "^dao is marked .*on.*ull but is null$";
    private static final String VERSION_001 = "0.0.1";
    private static String yamlAsJsonString;
    private PfDao pfDao;
    private StandardCoder standardCoder;

    /**
     * Read the policy type definition.
     *
     */
    @BeforeAll
    static void readPolicyDefinition() {
        String yamlString = ResourceUtils.getResourceAsString("src/test/resources/onap.policies.NoVersion.yaml");

        Object yamlObject = new Yaml().load(yamlString);
        yamlAsJsonString = new GsonBuilder().setPrettyPrinting().create().toJson(yamlObject);
    }

    /**
     * Set up the DAO towards the database.
     *
     * @throws Exception on database errors
     */
    @BeforeEach
    void setupDao() throws Exception {
        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getName());

        daoParameters.setPersistenceUnit("ToscaConceptTest");

        Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty("jakarta.persistence.jdbc.user", "policy");
        jdbcProperties.setProperty("jakarta.persistence.jdbc.password", "P01icY");
        jdbcProperties.setProperty("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        jdbcProperties.setProperty("jakarta.persistence.jdbc.url",
            "jdbc:h2:mem:AuthorativeToscaProviderPolicyTypeTest");
        daoParameters.setJdbcProperties(jdbcProperties);

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);
    }

    /**
     * Set up GSON.
     */
    @BeforeEach
    void setupGson() {
        standardCoder = new StandardCoder();
    }

    @AfterEach
    void teardown() {
        pfDao.close();
    }

    @Test
    void testPolicyTypesGet() throws Exception {
        assertThatThrownBy(() -> new AuthorativeToscaProvider().getPolicyTypes(null, null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().getPolicyList(null, null, null))
            .hasMessageMatching(DAO_IS_NULL);

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_NO_VERSION_VERSION1);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(beforePolicyType.getName(), createdPolicyType.getName());
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        ToscaServiceTemplate gotServiceTemplate = new AuthorativeToscaProvider().getPolicyTypes(pfDao,
                policyTypeKey.getName(), policyTypeKey.getVersion());

        ToscaPolicyType gotPolicyType = gotServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(beforePolicyType.getName(), gotPolicyType.getName());
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        List<ToscaPolicyType> gotPolicyTypeList =
                new AuthorativeToscaProvider().getPolicyTypeList(pfDao, POLICY_NO_VERSION, VERSION_001);
        assertEquals(2, gotPolicyTypeList.size());
        assertEquals(beforePolicyType.getName(), gotPolicyType.getName());

        gotPolicyTypeList = new AuthorativeToscaProvider().getPolicyTypeList(pfDao, POLICY_NO_VERSION, null);
        assertEquals(2, gotPolicyTypeList.size());
        assertEquals(beforePolicyType.getName(), gotPolicyType.getName());

        gotPolicyTypeList = new AuthorativeToscaProvider().getPolicyTypeList(pfDao, null, null);
        assertEquals(2, gotPolicyTypeList.size());
        assertEquals(beforePolicyType.getName(), gotPolicyType.getName());

        gotPolicyTypeList = new AuthorativeToscaProvider().getPolicyTypeList(pfDao, null, VERSION_001);
        assertEquals(2, gotPolicyTypeList.size());
        assertEquals(beforePolicyType.getName(), gotPolicyType.getName());

        assertThatThrownBy(() -> new AuthorativeToscaProvider().getPolicyTypeList(new DefaultPfDao(), POLICY_NO_VERSION,
                VERSION_001)).hasMessageContaining("Policy Framework DAO has not been initialized");

        assertTrue(new AuthorativeToscaProvider().getPolicyTypeList(pfDao, "i.dont.Exist", VERSION_001).isEmpty());
    }

    @Test
    void testPolicyTypesGetFiltered() throws Exception {
        assertThatThrownBy(() -> new AuthorativeToscaProvider().getFilteredPolicyTypes(null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().getFilteredPolicyTypes(null,
                ToscaEntityFilter.<ToscaPolicyType>builder().build())).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().getFilteredPolicyTypes(pfDao, null))
            .hasMessageMatching("^filter is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().getFilteredPolicyTypeList(null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().getFilteredPolicyTypeList(null,
                ToscaEntityFilter.<ToscaPolicyType>builder().build()))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao, null))
            .hasMessageMatching("^filter is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().getFilteredPolicyTypeList(new DefaultPfDao(),
                ToscaEntityFilter.<ToscaPolicyType>builder().name("i.dont.Exist").build()))
                        .hasMessageContaining("Policy Framework DAO has not been initialized");

        assertTrue(new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao,
                ToscaEntityFilter.<ToscaPolicyType>builder().name("i.dont.Exist").build()).isEmpty());

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_NO_VERSION_VERSION1);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(beforePolicyType.getName(), createdPolicyType.getName());
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        ToscaServiceTemplate gotServiceTemplate = new AuthorativeToscaProvider().getFilteredPolicyTypes(pfDao,
                ToscaEntityFilter.<ToscaPolicyType>builder().build());

        ToscaPolicyType gotPolicyType = gotServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        checkEqualsNameDescription(beforePolicyType, gotPolicyType);

        gotServiceTemplate = new AuthorativeToscaProvider().getFilteredPolicyTypes(pfDao,
                ToscaEntityFilter.<ToscaPolicyType>builder().name(policyTypeKey.getName()).build());

        gotPolicyType = gotServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        checkEqualsNameDescription(beforePolicyType, gotPolicyType);

        gotServiceTemplate = new AuthorativeToscaProvider().getFilteredPolicyTypes(pfDao, ToscaEntityFilter
                .<ToscaPolicyType>builder().name(policyTypeKey.getName()).version(VERSION_001).build());

        gotPolicyType = gotServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        checkEqualsNameDescription(beforePolicyType, gotPolicyType);

        List<ToscaPolicyType> gotPolicyTypeList =
                new AuthorativeToscaProvider().getPolicyTypeList(pfDao, POLICY_NO_VERSION, VERSION_001);
        assertEquals(2, gotPolicyTypeList.size());
        assertEquals(beforePolicyType.getName(), gotPolicyType.getName());

        gotPolicyTypeList = new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao,
                ToscaEntityFilter.<ToscaPolicyType>builder().build());
        assertEquals(2, gotPolicyTypeList.size());
        assertEquals(beforePolicyType.getName(), gotPolicyType.getName());

        gotPolicyTypeList = new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao,
                ToscaEntityFilter.<ToscaPolicyType>builder().name(policyTypeKey.getName()).build());
        assertEquals(1, gotPolicyTypeList.size());
        assertEquals(beforePolicyType.getName(), gotPolicyType.getName());

        gotPolicyTypeList = new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao, ToscaEntityFilter
                .<ToscaPolicyType>builder().name(policyTypeKey.getName()).version(VERSION_001).build());
        assertEquals(1, gotPolicyTypeList.size());
        assertEquals(beforePolicyType.getName(), gotPolicyType.getName());

        gotPolicyTypeList = new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao,
                ToscaEntityFilter.<ToscaPolicyType>builder().version("1.0.0").build());
        assertEquals(1, gotPolicyTypeList.size());
        assertEquals(beforePolicyType.getName(), gotPolicyType.getName());
    }

    private void checkEqualsNameDescription(ToscaPolicyType beforePolicyType, ToscaPolicyType gotPolicyType) {
        assertEquals(beforePolicyType.getName(), gotPolicyType.getName());
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), gotPolicyType.getDescription()));
    }

    @Test
    void testPolicyTypesCreate() throws Exception {
        assertThatThrownBy(() -> new AuthorativeToscaProvider().createPolicyTypes(null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().createPolicyTypes(null, new ToscaServiceTemplate()))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().createPolicyTypes(pfDao, null))
            .hasMessageMatching("^serviceTemplate is marked .*on.*ull but is null$");

        ToscaServiceTemplate testToscaServiceTemplate = new ToscaServiceTemplate();
        assertThatThrownBy(() -> new AuthorativeToscaProvider().createPolicyTypes(pfDao, testToscaServiceTemplate))
            .hasMessage(MISSING_POLICY_TYPES);

        testToscaServiceTemplate.setPolicyTypes(new LinkedHashMap<>());
        assertThatThrownBy(() -> new AuthorativeToscaProvider().createPolicyTypes(pfDao, testToscaServiceTemplate))
            .hasMessage("An incoming list of concepts must have at least one entry");

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_NO_VERSION_VERSION1);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(beforePolicyType.getName(), createdPolicyType.getName());
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));
    }

    @Test
    void testPolicyTypesUpdate() throws Exception {
        assertThatThrownBy(() -> new AuthorativeToscaProvider().createPolicyTypes(null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().updatePolicyTypes(null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().updatePolicyTypes(null, new ToscaServiceTemplate()))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().updatePolicyTypes(pfDao, null))
            .hasMessageMatching("^serviceTemplate is marked .*on.*ull but is null$");

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_NO_VERSION_VERSION1);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(beforePolicyType.getName(), createdPolicyType.getName());
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        ToscaServiceTemplate updatedServiceTemplate =
                new AuthorativeToscaProvider().updatePolicyTypes(pfDao, toscaServiceTemplate);

        ToscaPolicyType updatedPolicy = updatedServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(beforePolicyType.getName(), updatedPolicy.getName());
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), updatedPolicy.getDescription()));
    }

    @Test
    void testPolicyTypesDelete() throws Exception {
        assertThatThrownBy(() -> new AuthorativeToscaProvider().deletePolicyType(null, null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deletePolicyType(null, null, VERSION))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deletePolicyType(null, "name", null))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deletePolicyType(null, "name", VERSION))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deletePolicyType(pfDao, null, null))
            .hasMessageMatching("^name is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deletePolicyType(pfDao, null, VERSION))
            .hasMessageMatching("^name is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deletePolicyType(pfDao, "name", null))
            .hasMessageMatching("^version is marked .*on.*ull but is null$");

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_NO_VERSION_VERSION1);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(beforePolicyType.getName(), createdPolicyType.getName());
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        ToscaServiceTemplate deletedServiceTemplate = new AuthorativeToscaProvider().deletePolicyType(pfDao,
                policyTypeKey.getName(), policyTypeKey.getVersion());

        ToscaPolicyType deletedPolicy = deletedServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(beforePolicyType.getName(), deletedPolicy.getName());
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), deletedPolicy.getDescription()));

        assertThatThrownBy(() -> new AuthorativeToscaProvider()
            .getPolicyTypes(pfDao, policyTypeKey.getName(), policyTypeKey.getVersion()))
            .hasMessage("policy types for onap.policies.NoVersion:0.0.1 do not exist");
    }

    @Test
    void testAssertPoliciesExist() {
        ToscaServiceTemplate testServiceTemplate = new ToscaServiceTemplate();

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deletePolicyType(pfDao, "name", null))
            .hasMessageMatching("^version is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().createPolicyTypes(pfDao, testServiceTemplate))
            .hasMessage(MISSING_POLICY_TYPES);

        testServiceTemplate.setToscaTopologyTemplate(new ToscaTopologyTemplate());
        assertThatThrownBy(() -> new AuthorativeToscaProvider().createPolicyTypes(pfDao, testServiceTemplate))
            .hasMessage(MISSING_POLICY_TYPES);

        testServiceTemplate.setPolicyTypes(new LinkedHashMap<>());
        assertThatThrownBy(() -> new AuthorativeToscaProvider().createPolicyTypes(pfDao, testServiceTemplate))
            .hasMessage("An incoming list of concepts must have at least one entry");
    }

    @Test
    void testNullParameters() {
        assertThatThrownBy(() -> new AuthorativeToscaProvider().getPolicyTypeList(null, null, null))
                .hasMessageMatching("^dao is marked .*on.*ull but is null$");
    }
}
