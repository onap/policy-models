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

package org.onap.policy.models.tosca.simple.provider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.provider.AuthorativeToscaProvider;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
import org.yaml.snakeyaml.Yaml;

/**
 * Test the {@link SimpleToscaProvider} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class SimpleToscaProviderTest {
    private static final String TEMPLATE_IS_NULL = "serviceTemplate is marked @NonNull but is null";
    private static final String VCPE_INPUT_JSON = "policies/vCPE.policy.monitoring.input.tosca.json";
    private static final String DAO_IS_NULL = "dao is marked @NonNull but is null";
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
    public void teardown() {
        pfDao.close();
    }

    @Test
    public void testPoliciesGet() throws Exception {
        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString(VCPE_INPUT_JSON),
                ToscaServiceTemplate.class);

        createPolicyTypes();

        JpaToscaServiceTemplate originalServiceTemplate = new JpaToscaServiceTemplate();
        originalServiceTemplate.fromAuthorative(toscaServiceTemplate);

        assertNotNull(originalServiceTemplate);
        JpaToscaServiceTemplate createdServiceTemplate =
                new SimpleToscaProvider().createPolicies(pfDao, originalServiceTemplate);

        assertEquals(originalServiceTemplate, createdServiceTemplate);

        PfConceptKey policyKey = new PfConceptKey("onap.restart.tca:1.0.0");

        JpaToscaServiceTemplate gotServiceTemplate =
                new SimpleToscaProvider().getPolicies(pfDao, policyKey.getName(), policyKey.getVersion());

        assertEquals(originalServiceTemplate.getTopologyTemplate().getPolicies().get(policyKey),
                gotServiceTemplate.getTopologyTemplate().getPolicies().get(policyKey));

    }

    @Test
    public void testPolicyCreate() throws Exception {
        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString(VCPE_INPUT_JSON),
                ToscaServiceTemplate.class);

        createPolicyTypes();

        JpaToscaServiceTemplate originalServiceTemplate = new JpaToscaServiceTemplate();
        originalServiceTemplate.fromAuthorative(toscaServiceTemplate);

        assertNotNull(originalServiceTemplate);
        JpaToscaServiceTemplate createdServiceTemplate =
                new SimpleToscaProvider().createPolicies(pfDao, originalServiceTemplate);

        assertEquals(originalServiceTemplate, createdServiceTemplate);
    }

    @Test
    public void testPolicyUpdate() throws Exception {
        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString(VCPE_INPUT_JSON),
                ToscaServiceTemplate.class);

        createPolicyTypes();

        JpaToscaServiceTemplate originalServiceTemplate = new JpaToscaServiceTemplate();
        originalServiceTemplate.fromAuthorative(toscaServiceTemplate);

        assertNotNull(originalServiceTemplate);
        JpaToscaServiceTemplate updatedServiceTemplate =
                new SimpleToscaProvider().updatePolicies(pfDao, originalServiceTemplate);

        assertEquals(originalServiceTemplate, updatedServiceTemplate);
    }

    @Test
    public void testPoliciesDelete() throws Exception {
        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString(VCPE_INPUT_JSON),
                ToscaServiceTemplate.class);

        createPolicyTypes();

        JpaToscaServiceTemplate originalServiceTemplate = new JpaToscaServiceTemplate();
        originalServiceTemplate.fromAuthorative(toscaServiceTemplate);

        assertNotNull(originalServiceTemplate);
        JpaToscaServiceTemplate createdServiceTemplate =
                new SimpleToscaProvider().createPolicies(pfDao, originalServiceTemplate);

        assertEquals(originalServiceTemplate, createdServiceTemplate);

        PfConceptKey policyKey = new PfConceptKey("onap.restart.tca:1.0.0");

        JpaToscaServiceTemplate deletedServiceTemplate =
                new SimpleToscaProvider().deletePolicy(pfDao, new PfConceptKey(policyKey));

        assertEquals(originalServiceTemplate.getTopologyTemplate().getPolicies().get(policyKey),
                deletedServiceTemplate.getTopologyTemplate().getPolicies().get(policyKey));

        assertTrue(new SimpleToscaProvider().getPolicies(pfDao, policyKey.getName(), policyKey.getVersion())
                .getTopologyTemplate().getPolicies().getConceptMap().isEmpty());
    }

    @Test
    public void testAssertPoliciesExist() {
        JpaToscaServiceTemplate testServiceTemplate = new JpaToscaServiceTemplate();

        assertThatThrownBy(() -> new SimpleToscaProvider().createPolicies(pfDao, testServiceTemplate))
                        .hasMessage("topology template not specified on service template");

        testServiceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        assertThatThrownBy(() -> new SimpleToscaProvider().createPolicies(pfDao, testServiceTemplate))
                        .hasMessage("no policies specified on topology template of service template");

        testServiceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());
        assertThatThrownBy(() -> new SimpleToscaProvider().createPolicies(pfDao, testServiceTemplate))
                        .hasMessage("list of policies specified on topology template of service template is empty");
    }

    @Test
    public void testNonNulls() {
        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getPolicyTypes(null, null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicyTypes(null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicyTypes(null, new JpaToscaServiceTemplate());
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicyTypes(pfDao, null);
        }).hasMessage(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicyTypes(null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicyTypes(null, new JpaToscaServiceTemplate());
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicyTypes(pfDao, null);
        }).hasMessage(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicyType(null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicyType(null, new PfConceptKey());
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicyType(pfDao, null);
        }).hasMessage("policyTypeKey is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getPolicies(null, null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicies(null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicies(null, new JpaToscaServiceTemplate());
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicies(pfDao, null);
        }).hasMessage(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicies(null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicies(null, new JpaToscaServiceTemplate());
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicies(pfDao, null);
        }).hasMessage(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicy(null, null);
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicy(null, new PfConceptKey());
        }).hasMessage(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicy(pfDao, null);
        }).hasMessage("policyKey is marked @NonNull but is null");
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
