/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.GsonBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;
import org.yaml.snakeyaml.Yaml;

/**
 * Test of the {@link AuthorativeToscaProvider} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class AuthorativeToscaProviderPolicyTypeTest {
    private static final String VERSION = "version";
    private static final String POLICY_AFFINITY_VERSION0 = "onap.policies.optimization.AffinityPolicy:0.0.0";
    private static final String POLICY_AFFINITY = "onap.policies.optimization.AffinityPolicy";
    private static final String MISSING_POLICY_TYPES = "no policy types specified on service template";
    private static final String DAO_IS_NULL = "dao is marked @NonNull but is null";
    private static final String VERSION_000 = "0.0.0";
    private static String yamlAsJsonString;
    private PfDao pfDao;
    private StandardCoder standardCoder;


    /**
     * Read the policy type definition.
     *
     * @throws Exception on errors
     */
    @BeforeClass
    public static void readPolicyDefinition() {
        String yamlString =
                ResourceUtils.getResourceAsString("policytypes/onap.policies.optimization.AffinityPolicy.yaml");

        Object yamlObject = new Yaml().load(yamlString);
        yamlAsJsonString = new GsonBuilder().setPrettyPrinting().create().toJson(yamlObject);
    }

    /**
     * Set up the DAO towards the database.
     *
     * @throws Exception on database errors
     */
    @Before
    public void setupDao() throws Exception {
        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getName());

        daoParameters.setPersistenceUnit("ToscaConceptTest");

        Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_USER, "policy");
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_PASSWORD, "P01icY");

        // H2, use "org.mariadb.jdbc.Driver" and "jdbc:mariadb://localhost:3306/policy" for locally installed MariaDB
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_DRIVER, "org.h2.Driver");
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:mem:testdb");

        daoParameters.setJdbcProperties(jdbcProperties );

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);
    }

    /**
     * Set up GSON.
     */
    @Before
    public void setupGson() {
        standardCoder = new StandardCoder();
    }

    @After
    public void teardown() {
        pfDao.close();
    }

    @Test
    public void testPolicyTypesGet() throws Exception {
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getPolicyTypes(null, null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getPolicyList(null, null, null);
        }).hasMessage(DAO_IS_NULL);

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_AFFINITY_VERSION0);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(createdPolicyType.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        ToscaServiceTemplate gotServiceTemplate = new AuthorativeToscaProvider().getPolicyTypes(pfDao,
                policyTypeKey.getName(), policyTypeKey.getVersion());

        ToscaPolicyType gotPolicyType = gotServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        List<ToscaPolicyType> gotPolicyTypeList = new AuthorativeToscaProvider().getPolicyTypeList(pfDao,
                POLICY_AFFINITY, VERSION_000);
        assertEquals(1, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));

        gotPolicyTypeList = new AuthorativeToscaProvider().getPolicyTypeList(pfDao,
                POLICY_AFFINITY, null);
        assertEquals(1, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));

        gotPolicyTypeList = new AuthorativeToscaProvider().getPolicyTypeList(pfDao, null, null);
        assertEquals(1, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));

        gotPolicyTypeList = new AuthorativeToscaProvider().getPolicyTypeList(pfDao, null, VERSION_000);
        assertEquals(1, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));
    }


    @Test
    public void testPolicyTypesGetFiltered() throws Exception {
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicyTypes(null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicyTypes(null, ToscaPolicyTypeFilter.builder().build());
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicyTypes(pfDao, null);
        }).hasMessage("filter is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicyTypeList(null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicyTypeList(null, ToscaPolicyTypeFilter.builder().build());
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao, null);
        }).hasMessage("filter is marked @NonNull but is null");

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_AFFINITY_VERSION0);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(createdPolicyType.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        ToscaServiceTemplate gotServiceTemplate =
                new AuthorativeToscaProvider().getFilteredPolicyTypes(pfDao, ToscaPolicyTypeFilter.builder().build());

        ToscaPolicyType gotPolicyType = gotServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), gotPolicyType.getDescription()));

        gotServiceTemplate = new AuthorativeToscaProvider().getFilteredPolicyTypes(pfDao,
                ToscaPolicyTypeFilter.builder().name(policyTypeKey.getName()).build());

        gotPolicyType = gotServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), gotPolicyType.getDescription()));

        gotServiceTemplate = new AuthorativeToscaProvider().getFilteredPolicyTypes(pfDao,
                ToscaPolicyTypeFilter.builder().name(policyTypeKey.getName()).version(VERSION_000).build());

        gotPolicyType = gotServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), gotPolicyType.getDescription()));

        List<ToscaPolicyType> gotPolicyTypeList = new AuthorativeToscaProvider().getPolicyTypeList(pfDao,
                POLICY_AFFINITY, VERSION_000);
        assertEquals(1, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));

        gotPolicyTypeList = new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao,
                ToscaPolicyTypeFilter.builder().build());
        assertEquals(1, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));

        gotPolicyTypeList = new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao,
                ToscaPolicyTypeFilter.builder().name(policyTypeKey.getName()).build());
        assertEquals(1, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));

        gotPolicyTypeList = new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao,
                ToscaPolicyTypeFilter.builder().name(policyTypeKey.getName()).version(VERSION_000).build());
        assertEquals(1, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));

        gotPolicyTypeList = new AuthorativeToscaProvider().getFilteredPolicyTypeList(pfDao,
                ToscaPolicyTypeFilter.builder().version("1.0.0").build());
        assertEquals(0, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));
    }

    @Test
    public void testPolicyTypesCreate() throws Exception {
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicyTypes(null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicyTypes(null, new ToscaServiceTemplate());
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicyTypes(pfDao, null);
        }).hasMessage("serviceTemplate is marked @NonNull but is null");

        ToscaServiceTemplate testToscaServiceTemplate = new ToscaServiceTemplate();
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicyTypes(pfDao, testToscaServiceTemplate);
        }).hasMessage(MISSING_POLICY_TYPES);

        testToscaServiceTemplate.setPolicyTypes(new LinkedHashMap<>());
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicyTypes(pfDao, testToscaServiceTemplate);
        }).hasMessage("An incoming list of concepts must have at least one entry");

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_AFFINITY_VERSION0);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(createdPolicyType.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));
    }

    @Test
    public void testPolicyTypesUpdate() throws Exception {
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicyTypes(null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().updatePolicyTypes(null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().updatePolicyTypes(null, new ToscaServiceTemplate());
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().updatePolicyTypes(pfDao, null);
        }).hasMessage("serviceTemplate is marked @NonNull but is null");

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_AFFINITY_VERSION0);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(createdPolicyType.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        ToscaServiceTemplate updatedServiceTemplate =
                new AuthorativeToscaProvider().updatePolicyTypes(pfDao, toscaServiceTemplate);

        ToscaPolicyType updatedPolicy = updatedServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(updatedPolicy.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), updatedPolicy.getDescription()));
    }

    @Test
    public void testPolicyTypesDelete() throws Exception {
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicyType(null, null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicyType(null, null, VERSION);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicyType(null, "name", null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicyType(null, "name", VERSION);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicyType(pfDao, null, null);
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicyType(pfDao, null, VERSION);
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicyType(pfDao, "name", null);
        }).hasMessage("version is marked @NonNull but is null");

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_AFFINITY_VERSION0);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(createdPolicyType.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        ToscaServiceTemplate deletedServiceTemplate = new AuthorativeToscaProvider().deletePolicyType(pfDao,
                policyTypeKey.getName(), policyTypeKey.getVersion());

        ToscaPolicyType deletedPolicy = deletedServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(deletedPolicy.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), deletedPolicy.getDescription()));

        ToscaServiceTemplate gotServiceTemplate = new AuthorativeToscaProvider().getPolicyTypes(pfDao,
                policyTypeKey.getName(), policyTypeKey.getVersion());

        assertTrue(gotServiceTemplate.getPolicyTypes().isEmpty());
    }

    @Test
    public void testAssertPoliciesExist() {
        ToscaServiceTemplate testServiceTemplate = new ToscaServiceTemplate();

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicyType(pfDao, "name", null);
        }).hasMessage("version is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicyTypes(pfDao, testServiceTemplate);
        }).hasMessage(MISSING_POLICY_TYPES);

        testServiceTemplate.setToscaTopologyTemplate(new ToscaTopologyTemplate());
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicyTypes(pfDao, testServiceTemplate);
        }).hasMessage(MISSING_POLICY_TYPES);

        testServiceTemplate.setPolicyTypes(new LinkedHashMap<>());
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicyTypes(pfDao, testServiceTemplate);
        }).hasMessage("An incoming list of concepts must have at least one entry");
    }
}
