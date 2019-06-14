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

package org.onap.policy.models.tosca.authorative.provider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;
import org.yaml.snakeyaml.Yaml;

/**
 * Test of the {@link AuthorativeToscaProvider} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class AuthorativeToscaProviderPolicyTest {
    private PfDao pfDao;
    private StandardCoder standardCoder;

    /**
     * Set up the DAO towards the database.
     *
     * @throws Exception on database errors
     */
    @Before
    public void setupDao() throws Exception {
        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getCanonicalName());

        daoParameters.setPersistenceUnit("ToscaConceptTest");

        Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_USER, "policy");
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_PASSWORD, "P01icY");

        // H2, use "org.mariadb.jdbc.Driver" and "jdbc:mariadb://localhost:3306/policy" for locally installed MariaDB
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_DRIVER, "org.h2.Driver");
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:mem:testdb");

        daoParameters.setJdbcProperties(jdbcProperties);

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
    public void teardown() throws Exception {
        pfDao.close();
    }

    @Test
    public void testPoliciesGet() throws Exception {
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getPolicies(null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getPolicyList(null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        createPolicyTypes();

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json"),
                ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicies(pfDao, toscaServiceTemplate);

        PfConceptKey policyKey = new PfConceptKey("onap.restart.tca:1.0.0");

        ToscaPolicy beforePolicy =
                toscaServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        ToscaPolicy createdPolicy =
                createdServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, createdPolicy));
        assertTrue(beforePolicy.getType().equals(createdPolicy.getType()));

        ToscaServiceTemplate gotServiceTemplate =
                new AuthorativeToscaProvider().getPolicies(pfDao, policyKey.getName(), policyKey.getVersion());

        ToscaPolicy gotPolicy =
                gotServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicy));
        assertTrue(beforePolicy.getType().equals(gotPolicy.getType()));

        List<ToscaPolicy> gotPolicyList =
                new AuthorativeToscaProvider().getPolicyList(pfDao, "onap.restart.tca", "1.0.0");
        assertEquals(1, gotPolicyList.size());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicyList.get(0)));

        gotPolicyList = new AuthorativeToscaProvider().getPolicyList(pfDao, "onap.restart.tca", null);
        assertEquals(1, gotPolicyList.size());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicyList.get(0)));

        gotPolicyList = new AuthorativeToscaProvider().getPolicyList(pfDao, null, null);
        assertEquals(1, gotPolicyList.size());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicyList.get(0)));

        gotPolicyList = new AuthorativeToscaProvider().getPolicyList(pfDao, null, "1.0.0");
        assertEquals(1, gotPolicyList.size());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicyList.get(0)));

        gotPolicyList = new AuthorativeToscaProvider().getPolicyList(pfDao, "Nonexistant", "1.0.0");
        assertEquals(0, gotPolicyList.size());
    }

    @Test
    public void testPoliciesGetFiltered() throws Exception {
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicies(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicies(null, ToscaPolicyFilter.builder().build());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicies(pfDao, null);
        }).hasMessage("filter is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicyList(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicyList(null, ToscaPolicyFilter.builder().build());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getFilteredPolicyList(pfDao, null);
        }).hasMessage("filter is marked @NonNull but is null");

        createPolicyTypes();

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json"),
                ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicies(pfDao, toscaServiceTemplate);

        PfConceptKey policyKey = new PfConceptKey("onap.restart.tca:1.0.0");

        ToscaPolicy beforePolicy =
                toscaServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        ToscaPolicy createdPolicy =
                createdServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, createdPolicy));
        assertTrue(beforePolicy.getType().equals(createdPolicy.getType()));

        ToscaServiceTemplate gotServiceTemplate =
                new AuthorativeToscaProvider().getFilteredPolicies(pfDao, ToscaPolicyFilter.builder().build());

        ToscaPolicy gotPolicy =
                gotServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicy));
        assertTrue(beforePolicy.getType().equals(gotPolicy.getType()));

        gotServiceTemplate = new AuthorativeToscaProvider().getFilteredPolicies(pfDao,
                ToscaPolicyFilter.builder().name(policyKey.getName()).build());

        gotPolicy = gotServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicy));
        assertTrue(beforePolicy.getType().equals(gotPolicy.getType()));

        gotServiceTemplate = new AuthorativeToscaProvider().getFilteredPolicies(pfDao,
                ToscaPolicyFilter.builder().name(policyKey.getName()).version("1.0.0").build());

        gotPolicy = gotServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicy));
        assertTrue(beforePolicy.getType().equals(gotPolicy.getType()));

        List<ToscaPolicy> gotPolicyList =
                new AuthorativeToscaProvider().getPolicyList(pfDao, "onap.restart.tca", "1.0.0");
        assertEquals(1, gotPolicyList.size());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicyList.get(0)));

        gotPolicyList =
                new AuthorativeToscaProvider().getFilteredPolicyList(pfDao, ToscaPolicyFilter.builder().build());
        assertEquals(1, gotPolicyList.size());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicyList.get(0)));

        gotPolicyList = new AuthorativeToscaProvider().getFilteredPolicyList(pfDao,
                ToscaPolicyFilter.builder().name(policyKey.getName()).build());
        assertEquals(1, gotPolicyList.size());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicyList.get(0)));

        gotPolicyList = new AuthorativeToscaProvider().getFilteredPolicyList(pfDao,
                ToscaPolicyFilter.builder().name(policyKey.getName()).version("1.0.0").build());
        assertEquals(1, gotPolicyList.size());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, gotPolicyList.get(0)));
    }

    @Test
    public void testPolicyCreate() throws Exception {
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicies(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicies(null, new ToscaServiceTemplate());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicies(pfDao, null);
        }).hasMessage("serviceTemplate is marked @NonNull but is null");

        createPolicyTypes();

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json"),
                ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicies(pfDao, toscaServiceTemplate);

        PfConceptKey policyKey = new PfConceptKey("onap.restart.tca:1.0.0");

        ToscaPolicy beforePolicy =
                toscaServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        ToscaPolicy createdPolicy =
                createdServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, createdPolicy));
        assertTrue(beforePolicy.getType().equals(createdPolicy.getType()));
    }

    @Test
    public void testPolicyUpdate() throws Exception {
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicies(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().updatePolicies(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().updatePolicies(null, new ToscaServiceTemplate());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().updatePolicies(pfDao, null);
        }).hasMessage("serviceTemplate is marked @NonNull but is null");

        createPolicyTypes();

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json"),
                ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicies(pfDao, toscaServiceTemplate);

        PfConceptKey policyKey = new PfConceptKey("onap.restart.tca:1.0.0");

        ToscaPolicy beforePolicy =
                toscaServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        ToscaPolicy createdPolicy =
                createdServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, createdPolicy));
        assertTrue(beforePolicy.getType().equals(createdPolicy.getType()));

        ToscaServiceTemplate updatedServiceTemplate =
                new AuthorativeToscaProvider().updatePolicies(pfDao, toscaServiceTemplate);

        ToscaPolicy updatedPolicy =
                updatedServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, updatedPolicy));
        assertTrue(beforePolicy.getType().equals(updatedPolicy.getType()));
    }

    @Test
    public void testPoliciesDelete() throws Exception {
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicy(null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicy(null, null, "version");
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicy(null, "name", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicy(null, "name", "version");
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicy(pfDao, null, null);
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicy(pfDao, null, "version");
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicy(pfDao, "name", null);
        }).hasMessage("version is marked @NonNull but is null");

        createPolicyTypes();

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json"),
                ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicies(pfDao, toscaServiceTemplate);

        PfConceptKey policyKey = new PfConceptKey("onap.restart.tca:1.0.0");

        ToscaPolicy beforePolicy =
                toscaServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        ToscaPolicy createdPolicy =
                createdServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, createdPolicy));
        assertTrue(beforePolicy.getType().equals(createdPolicy.getType()));

        ToscaServiceTemplate deletedServiceTemplate =
                new AuthorativeToscaProvider().deletePolicy(pfDao, policyKey.getName(), policyKey.getVersion());

        ToscaPolicy deletedPolicy =
                deletedServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, createdPolicy));
        assertTrue(beforePolicy.getType().equals(deletedPolicy.getType()));

        ToscaServiceTemplate gotServiceTemplate =
                new AuthorativeToscaProvider().getPolicies(pfDao, policyKey.getName(), policyKey.getVersion());

        assertTrue(gotServiceTemplate.getToscaTopologyTemplate().getPolicies().isEmpty());
    }

    @Test
    public void testAssertPoliciesExist() throws PfModelException {
        ToscaServiceTemplate testServiceTemplate = new ToscaServiceTemplate();

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().deletePolicy(pfDao, "name", null);
        }).hasMessage("version is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicies(pfDao, testServiceTemplate);
        }).hasMessage("topology template not specified on service template");

        testServiceTemplate.setToscaTopologyTemplate(new ToscaTopologyTemplate());
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicies(pfDao, testServiceTemplate);
        }).hasMessage("no policies specified on topology template of service template");

        testServiceTemplate.getToscaTopologyTemplate().setPolicies(new ArrayList<>());
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().createPolicies(pfDao, testServiceTemplate);
        }).hasMessage("An incoming list of concepts must have at least one entry");
    }

    @Test
    public void testEntityMaps() throws CoderException, PfModelException {
        Object yamlObject = new Yaml().load(
                ResourceUtils.getResourceAsString("policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.yaml"));
        String yamlAsJsonString = new StandardCoder().encode(yamlObject);

        ToscaServiceTemplate toscaServiceTemplatePolicyType =
                standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplatePolicyType);
        new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplatePolicyType);

        assertEquals(3, toscaServiceTemplatePolicyType.getDataTypesAsMap().size());
        assertEquals(2, toscaServiceTemplatePolicyType.getPolicyTypesAsMap().size());

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json"),
                ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createPolicies(pfDao, toscaServiceTemplate);

        PfConceptKey policyKey = new PfConceptKey("onap.restart.tca:1.0.0");

        ToscaPolicy beforePolicy =
                toscaServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        ToscaPolicy createdPolicy =
                createdServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0).get(policyKey.getName());
        assertEquals(0, beforePolicy.compareNameVersion(beforePolicy, createdPolicy));
        assertTrue(beforePolicy.getType().equals(createdPolicy.getType()));

        assertEquals(1, toscaServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, createdServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());

        Map<String, ToscaPolicy> policyMapItem = createdServiceTemplate.getToscaTopologyTemplate().getPolicies().get(0);
        createdServiceTemplate.getToscaTopologyTemplate().getPolicies().add(policyMapItem);

        assertThatThrownBy(() -> {
            createdServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap();
        }).hasMessageContaining("list of map of entities contains more than one entity with key");
    }

    private void createPolicyTypes() throws CoderException, PfModelException {
        Object yamlObject = new Yaml().load(
                ResourceUtils.getResourceAsString("policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.yaml"));
        String yamlAsJsonString = new StandardCoder().encode(yamlObject);

        ToscaServiceTemplate toscaServiceTemplatePolicyType =
                standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplatePolicyType);
        new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplatePolicyType);
    }
}
