/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
import org.yaml.snakeyaml.Yaml;

/**
 * Test the {@link SimpleToscaProvider} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class SimpleToscaProviderTest {
    private static final String TEMPLATE_IS_NULL = "^serviceTemplate is marked .*on.*ull but is null$";
    private static final String VCPE_INPUT_JSON = "policies/vCPE.policy.monitoring.input.tosca.json";
    private static final String DAO_IS_NULL = "^dao is marked .*on.*ull but is null$";
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
        daoParameters.setPluginClass(DefaultPfDao.class.getName());

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
    public void testCreateUpdateGetDeleteDataType() throws PfModelException {
        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();

        PfConceptKey dataType0Key = new PfConceptKey("DataType0", "0.0.1");
        JpaToscaDataType dataType0 = new JpaToscaDataType();
        dataType0.setKey(dataType0Key);
        serviceTemplate.setDataTypes(new JpaToscaDataTypes());
        serviceTemplate.getDataTypes().getConceptMap().put(dataType0Key, dataType0);

        JpaToscaServiceTemplate createdServiceTemplate =
                new SimpleToscaProvider().createDataTypes(pfDao, serviceTemplate);

        assertEquals(1, createdServiceTemplate.getDataTypes().getConceptMap().size());
        assertEquals(dataType0, createdServiceTemplate.getDataTypes().get(dataType0Key));
        assertEquals(null, createdServiceTemplate.getDataTypes().get(dataType0Key).getDescription());

        dataType0.setDescription("Updated Description");

        JpaToscaServiceTemplate updatedServiceTemplate =
                new SimpleToscaProvider().updateDataTypes(pfDao, serviceTemplate);

        assertEquals(dataType0, updatedServiceTemplate.getDataTypes().get(dataType0Key));
        assertEquals("Updated Description", updatedServiceTemplate.getDataTypes().get(dataType0Key).getDescription());

        JpaToscaServiceTemplate gotServiceTemplate =
                new SimpleToscaProvider().getDataTypes(pfDao, dataType0Key.getName(), dataType0Key.getVersion());

        assertEquals(dataType0, gotServiceTemplate.getDataTypes().get(dataType0Key));
        assertEquals("Updated Description", gotServiceTemplate.getDataTypes().get(dataType0Key).getDescription());

        JpaToscaServiceTemplate deletedServiceTemplate = new SimpleToscaProvider().deleteDataType(pfDao, dataType0Key);

        assertEquals(dataType0, deletedServiceTemplate.getDataTypes().get(dataType0Key));
        assertEquals("Updated Description", deletedServiceTemplate.getDataTypes().get(dataType0Key).getDescription());

        JpaToscaServiceTemplate doesNotExistServiceTemplate =
                new SimpleToscaProvider().deleteDataType(pfDao, dataType0Key);

        assertEquals(null, doesNotExistServiceTemplate.getDataTypes().get(dataType0Key));
    }

    @Test
    public void testCreateUpdateGetDeletePolicyType() throws PfModelException {
        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();

        PfConceptKey dataType0Key = new PfConceptKey("DataType0", "0.0.1");
        JpaToscaDataType dataType0 = new JpaToscaDataType();
        dataType0.setKey(dataType0Key);
        serviceTemplate.setDataTypes(new JpaToscaDataTypes());
        serviceTemplate.getDataTypes().getConceptMap().put(dataType0Key, dataType0);

        PfConceptKey policyType0Key = new PfConceptKey("PolicyType0", "0.0.1");
        JpaToscaPolicyType policyType0 = new JpaToscaPolicyType();
        policyType0.setKey(policyType0Key);
        serviceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());
        serviceTemplate.getPolicyTypes().getConceptMap().put(policyType0Key, policyType0);

        JpaToscaServiceTemplate createdServiceTemplate =
                new SimpleToscaProvider().createPolicyTypes(pfDao, serviceTemplate);

        assertEquals(1, createdServiceTemplate.getPolicyTypes().getConceptMap().size());
        assertEquals(policyType0, createdServiceTemplate.getPolicyTypes().get(policyType0Key));
        assertEquals(null, createdServiceTemplate.getPolicyTypes().get(policyType0Key).getDescription());

        policyType0.setDescription("Updated Description");

        JpaToscaServiceTemplate updatedServiceTemplate =
                new SimpleToscaProvider().updatePolicyTypes(pfDao, serviceTemplate);

        assertEquals(policyType0, updatedServiceTemplate.getPolicyTypes().get(policyType0Key));
        assertEquals("Updated Description",
                updatedServiceTemplate.getPolicyTypes().get(policyType0Key).getDescription());

        JpaToscaServiceTemplate gotServiceTemplate =
                new SimpleToscaProvider().getPolicyTypes(pfDao, policyType0Key.getName(), policyType0Key.getVersion());

        assertEquals(policyType0, gotServiceTemplate.getPolicyTypes().get(policyType0Key));
        assertEquals("Updated Description", gotServiceTemplate.getPolicyTypes().get(policyType0Key).getDescription());

        JpaToscaServiceTemplate deletedServiceTemplate =
                new SimpleToscaProvider().deletePolicyType(pfDao, policyType0Key);

        assertEquals(policyType0, deletedServiceTemplate.getPolicyTypes().get(policyType0Key));
        assertEquals("Updated Description",
                deletedServiceTemplate.getPolicyTypes().get(policyType0Key).getDescription());

        JpaToscaServiceTemplate doesNotExistServiceTemplate =
                new SimpleToscaProvider().deletePolicyType(pfDao, policyType0Key);

        assertEquals(null, doesNotExistServiceTemplate.getPolicyTypes().get(policyType0Key));
    }

    @Test
    public void testCreateUpdateGetDeletePolicyTypeWithDataType() throws PfModelException {
        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();

        PfConceptKey policyType0Key = new PfConceptKey("PolicyType0", "0.0.1");
        JpaToscaPolicyType policyType0 = new JpaToscaPolicyType();
        policyType0.setKey(policyType0Key);
        serviceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());
        serviceTemplate.getPolicyTypes().getConceptMap().put(policyType0Key, policyType0);

        JpaToscaServiceTemplate createdServiceTemplate =
                new SimpleToscaProvider().createPolicyTypes(pfDao, serviceTemplate);

        assertEquals(policyType0, createdServiceTemplate.getPolicyTypes().get(policyType0Key));
        assertEquals(null, createdServiceTemplate.getPolicyTypes().get(policyType0Key).getDescription());

        policyType0.setDescription("Updated Description");

        JpaToscaServiceTemplate updatedServiceTemplate =
                new SimpleToscaProvider().updatePolicyTypes(pfDao, serviceTemplate);

        assertEquals(policyType0, updatedServiceTemplate.getPolicyTypes().get(policyType0Key));
        assertEquals("Updated Description",
                updatedServiceTemplate.getPolicyTypes().get(policyType0Key).getDescription());

        JpaToscaServiceTemplate gotServiceTemplate =
                new SimpleToscaProvider().getPolicyTypes(pfDao, policyType0Key.getName(), policyType0Key.getVersion());

        assertEquals(policyType0, gotServiceTemplate.getPolicyTypes().get(policyType0Key));
        assertEquals("Updated Description", gotServiceTemplate.getPolicyTypes().get(policyType0Key).getDescription());

        JpaToscaServiceTemplate deletedServiceTemplate =
                new SimpleToscaProvider().deletePolicyType(pfDao, policyType0Key);

        assertEquals(policyType0, deletedServiceTemplate.getPolicyTypes().get(policyType0Key));
        assertEquals("Updated Description",
                deletedServiceTemplate.getPolicyTypes().get(policyType0Key).getDescription());

        JpaToscaServiceTemplate doesNotExistServiceTemplate =
                new SimpleToscaProvider().deletePolicyType(pfDao, policyType0Key);

        assertEquals(null, doesNotExistServiceTemplate.getPolicyTypes().get(policyType0Key));
    }

    @Test
    public void testPoliciesGet() throws Exception {
        ToscaServiceTemplate toscaServiceTemplate =
                standardCoder.decode(ResourceUtils.getResourceAsString(VCPE_INPUT_JSON), ToscaServiceTemplate.class);

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
        ToscaServiceTemplate toscaServiceTemplate =
                standardCoder.decode(ResourceUtils.getResourceAsString(VCPE_INPUT_JSON), ToscaServiceTemplate.class);

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
        ToscaServiceTemplate toscaServiceTemplate =
                standardCoder.decode(ResourceUtils.getResourceAsString(VCPE_INPUT_JSON), ToscaServiceTemplate.class);

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
        ToscaServiceTemplate toscaServiceTemplate =
                standardCoder.decode(ResourceUtils.getResourceAsString(VCPE_INPUT_JSON), ToscaServiceTemplate.class);

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
            new SimpleToscaProvider().getDataTypes(null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createDataTypes(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createDataTypes(null, new JpaToscaServiceTemplate());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createDataTypes(pfDao, null);
        }).hasMessageMatching(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updateDataTypes(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updateDataTypes(null, new JpaToscaServiceTemplate());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updateDataTypes(pfDao, null);
        }).hasMessageMatching(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deleteDataType(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deleteDataType(null, new PfConceptKey());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deleteDataType(pfDao, null);
        }).hasMessageMatching("^dataTypeKey is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getPolicyTypes(null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicyTypes(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicyTypes(null, new JpaToscaServiceTemplate());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicyTypes(pfDao, null);
        }).hasMessageMatching(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicyTypes(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicyTypes(null, new JpaToscaServiceTemplate());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicyTypes(pfDao, null);
        }).hasMessageMatching(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicyType(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicyType(null, new PfConceptKey());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicyType(pfDao, null);
        }).hasMessageMatching("^policyTypeKey is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getPolicies(null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicies(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicies(null, new JpaToscaServiceTemplate());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicies(pfDao, null);
        }).hasMessageMatching(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicies(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicies(null, new JpaToscaServiceTemplate());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().updatePolicies(pfDao, null);
        }).hasMessageMatching(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicy(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicy(null, new PfConceptKey());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicy(pfDao, null);
        }).hasMessageMatching("^policyKey is marked .*on.*ull but is null$");
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
