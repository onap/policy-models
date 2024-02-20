/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2023-2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.TreeMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfReferenceKey;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.provider.AuthorativeToscaProvider;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaConstraint;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicies;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaProperty;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTopologyTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaTrigger;
import org.yaml.snakeyaml.Yaml;

/**
 * Test the {@link SimpleToscaProvider} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class SimpleToscaProviderTest {
    private static final String TEMPLATE_IS_NULL = "^serviceTemplate is marked .*on.*ull but is null$";
    private static final String INCOMING_TEMPLATE_IS_NULL = "^incomingServiceTemplate is marked .*on.*ull but is null$";
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
        jdbcProperties.setProperty("jakarta.persistence.jdbc.user", "policy");
        jdbcProperties.setProperty("jakarta.persistence.jdbc.password", "P01icY");

        if (System.getProperty("USE-MARIADB") != null) {
            jdbcProperties.setProperty("jakarta.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
            jdbcProperties.setProperty("jakarta.persistence.jdbc.url", "jdbc:mariadb://localhost:3306/policy");
        } else {
            jdbcProperties.setProperty("jakarta.persistence.jdbc.driver", "org.h2.Driver");
            jdbcProperties.setProperty("jakarta.persistence.jdbc.url", "jdbc:h2:mem:SimpleToscaProviderTest");
        }

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
        PfConceptKey dataType0Key = new PfConceptKey("DataType0", "0.0.1");
        JpaToscaDataType dataType0 = new JpaToscaDataType();
        dataType0.setKey(dataType0Key);
        dataType0.setConstraints(new ArrayList<JpaToscaConstraint>());
        dataType0.setMetadata(new TreeMap<String, String>());
        dataType0.setProperties(new LinkedHashMap<String, JpaToscaProperty>());

        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
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

        assertThatThrownBy(() -> new SimpleToscaProvider().deleteDataType(pfDao, new PfConceptKey("IDontExist:0.0.1")))
            .hasMessage("data type IDontExist:0.0.1 not found");

        JpaToscaServiceTemplate deletedServiceTemplate = new SimpleToscaProvider().deleteDataType(pfDao, dataType0Key);

        assertEquals(dataType0, deletedServiceTemplate.getDataTypes().get(dataType0Key));
        assertEquals("Updated Description", deletedServiceTemplate.getDataTypes().get(dataType0Key).getDescription());

        // Create the data type again
        new SimpleToscaProvider().createDataTypes(pfDao, serviceTemplate);

        updatedServiceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());
        JpaToscaPolicyType pt0 = new JpaToscaPolicyType(new PfConceptKey("pt0:0.0.1"));
        updatedServiceTemplate.getPolicyTypes().getConceptMap().put(pt0.getKey(), pt0);
        new SimpleToscaProvider().createPolicyTypes(pfDao, updatedServiceTemplate);

        deletedServiceTemplate = new SimpleToscaProvider().deleteDataType(pfDao, dataType0Key);

        assertEquals(dataType0, deletedServiceTemplate.getDataTypes().get(dataType0Key));
        assertEquals("Updated Description", deletedServiceTemplate.getDataTypes().get(dataType0Key).getDescription());

        assertThatThrownBy(() -> new SimpleToscaProvider().deleteDataType(pfDao, dataType0Key))
            .hasMessage("no data types found");

        // Create the data type again
        new SimpleToscaProvider().createDataTypes(pfDao, serviceTemplate);

        JpaToscaPolicyType pt0v2 = new JpaToscaPolicyType(new PfConceptKey("pt0:0.0.2"));
        JpaToscaProperty prop0 = new JpaToscaProperty(new PfReferenceKey(pt0v2.getKey(), "prop0"));
        prop0.setType(dataType0Key);
        pt0v2.setProperties(new LinkedHashMap<>());
        pt0v2.getProperties().put(prop0.getKey().getLocalName(), prop0);
        updatedServiceTemplate.getPolicyTypes().getConceptMap().put(pt0v2.getKey(), pt0v2);
        new SimpleToscaProvider().createPolicyTypes(pfDao, updatedServiceTemplate);

        assertThatThrownBy(() -> new SimpleToscaProvider().deleteDataType(pfDao, dataType0Key))
            .hasMessage("data type DataType0:0.0.1 is in use, it is referenced in policy type pt0:0.0.2");

        JpaToscaDataType dataType0v2 = new JpaToscaDataType(new PfConceptKey("DataType0:0.0.2"));
        dataType0v2.setConstraints(new ArrayList<JpaToscaConstraint>());
        dataType0v2.setMetadata(new TreeMap<String, String>());
        dataType0v2.setProperties(new LinkedHashMap<String, JpaToscaProperty>());
        updatedServiceTemplate.getDataTypes().getConceptMap().put(dataType0v2.getKey(), dataType0v2);
        new SimpleToscaProvider().createDataTypes(pfDao, updatedServiceTemplate);

        deletedServiceTemplate = new SimpleToscaProvider().deleteDataType(pfDao, dataType0v2.getKey());

        assertEquals(dataType0v2, deletedServiceTemplate.getDataTypes().get(dataType0v2.getKey()));
        assertNull(deletedServiceTemplate.getDataTypes().get(dataType0v2.getKey()).getDescription());

        assertThatThrownBy(() -> new SimpleToscaProvider().deleteDataType(pfDao, dataType0Key))
            .hasMessage("data type DataType0:0.0.1 is in use, it is referenced in policy type pt0:0.0.2");

        JpaToscaDataType dataType1 = new JpaToscaDataType(new PfConceptKey("DataType1:0.0.3"));
        JpaToscaProperty prop1 = new JpaToscaProperty(new PfReferenceKey(dataType1.getKey(), "prop1"));
        prop1.setType(dataType0v2.getKey());
        dataType1.setProperties(new LinkedHashMap<>());
        dataType1.getProperties().put(prop1.getKey().getLocalName(), prop1);
        updatedServiceTemplate.getDataTypes().getConceptMap().put(dataType1.getKey(), dataType1);
        new SimpleToscaProvider().createDataTypes(pfDao, updatedServiceTemplate);

        assertThatThrownBy(() -> new SimpleToscaProvider().deleteDataType(pfDao, dataType0v2.getKey()))
            .hasMessage("data type DataType0:0.0.2 is in use, it is referenced in data type DataType1:0.0.3");
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
        policyType0.setMetadata(new TreeMap<String, String>());
        policyType0.setProperties(new LinkedHashMap<String, JpaToscaProperty>());
        policyType0.setTargets(new ArrayList<PfConceptKey>());
        policyType0.setTriggers(new ArrayList<JpaToscaTrigger>());
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

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deletePolicyType(pfDao, new PfConceptKey("IDontExist:0.0.1"));
        }).hasMessage("policy type IDontExist:0.0.1 not found");

        JpaToscaPolicyType pt1 = new JpaToscaPolicyType(new PfConceptKey("pt1:0.0.2"));
        pt1.setDerivedFrom(policyType0Key);
        serviceTemplate.getPolicyTypes().getConceptMap().put(pt1.getKey(), pt1);
        new SimpleToscaProvider().createPolicyTypes(pfDao, serviceTemplate);

        assertThatThrownBy(() -> new SimpleToscaProvider().deletePolicyType(pfDao, policyType0Key))
            .hasMessage("policy type PolicyType0:0.0.1 is in use, it is referenced in policy type pt1:0.0.2");

        serviceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());

        JpaToscaPolicy p0 = new JpaToscaPolicy(new PfConceptKey("p0:0.0.1"));
        p0.setType(policyType0Key);
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(p0.getKey(), p0);

        JpaToscaPolicy p1 = new JpaToscaPolicy(new PfConceptKey("p1:0.0.1"));
        p1.setType(pt1.getKey());
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(p1.getKey(), p1);
        new SimpleToscaProvider().createPolicies(pfDao, serviceTemplate);

        assertThatThrownBy(() -> new SimpleToscaProvider().deletePolicyType(pfDao, policyType0Key))
            .hasMessage("policy type PolicyType0:0.0.1 is in use, it is referenced in policy type pt1:0.0.2");

        assertThatThrownBy(() -> new SimpleToscaProvider().deletePolicyType(pfDao, pt1.getKey()))
            .hasMessage("policy type pt1:0.0.2 is in use, it is referenced in policy p1:0.0.1");

        new SimpleToscaProvider().deletePolicy(pfDao, p1.getKey());

        new SimpleToscaProvider().deletePolicyType(pfDao, pt1.getKey());

        new SimpleToscaProvider().deletePolicy(pfDao, p0.getKey());

        JpaToscaServiceTemplate deletedServiceTemplate =
            new SimpleToscaProvider().deletePolicyType(pfDao, policyType0Key);

        assertEquals(policyType0, deletedServiceTemplate.getPolicyTypes().get(policyType0Key));
        assertEquals("Updated Description",
            deletedServiceTemplate.getPolicyTypes().get(policyType0Key).getDescription());

        assertThatThrownBy(() -> new SimpleToscaProvider().deletePolicyType(pfDao, policyType0Key))
            .hasMessage("no policy types found");

        JpaToscaServiceTemplate newServiceTemplate =
            new SimpleToscaProvider().createPolicyTypes(pfDao, serviceTemplate);
        assertEquals(serviceTemplate, newServiceTemplate);
    }

    @Test
    public void testCreateUpdateGetDeletePolicyTypeWithDataType() throws PfModelException {
        PfConceptKey policyType0Key = new PfConceptKey("PolicyType0", "0.0.1");
        JpaToscaPolicyType policyType0 = new JpaToscaPolicyType();
        policyType0.setKey(policyType0Key);
        policyType0.setMetadata(new TreeMap<String, String>());
        policyType0.setProperties(new LinkedHashMap<String, JpaToscaProperty>());
        policyType0.setTargets(new ArrayList<PfConceptKey>());
        policyType0.setTriggers(new ArrayList<JpaToscaTrigger>());

        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
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

        assertThatThrownBy(() -> new SimpleToscaProvider().deletePolicyType(pfDao, policyType0Key))
            .hasMessage("no policy types found");
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

        assertEquals(originalServiceTemplate.getTopologyTemplate().getPolicies(),
            createdServiceTemplate.getTopologyTemplate().getPolicies());

        PfConceptKey policyKey = new PfConceptKey("onap.restart.tca:1.0.0");

        JpaToscaServiceTemplate gotServiceTemplate =
            new SimpleToscaProvider().getPolicies(pfDao, policyKey.getName(), policyKey.getVersion());

        assertEquals(0, originalServiceTemplate.getTopologyTemplate().getPolicies().get(policyKey)
            .compareTo(gotServiceTemplate.getTopologyTemplate().getPolicies().get(policyKey)));

        JpaToscaServiceTemplate deletedServiceTemplate = new SimpleToscaProvider().deletePolicy(pfDao, policyKey);
        assertEquals(1, deletedServiceTemplate.getTopologyTemplate().getPolicies().getConceptMap().size());
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

        assertEquals(originalServiceTemplate.getTopologyTemplate().getPolicies(),
            createdServiceTemplate.getTopologyTemplate().getPolicies());
    }

    @Test
    public void testPolicyCreateTypeAndVersion() throws Exception {
        ToscaServiceTemplate toscaServiceTemplate =
            standardCoder.decode(ResourceUtils.getResourceAsString(VCPE_INPUT_JSON), ToscaServiceTemplate.class);

        createPolicyTypes();

        ToscaPolicy toscaPolicy =
            toscaServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().values().iterator().next();

        JpaToscaServiceTemplate originalServiceTemplate = new JpaToscaServiceTemplate();

        final String originalPolicyType = toscaPolicy.getType();
        final String originalPolicyTypeVersion = toscaPolicy.getTypeVersion();
        toscaPolicy.setType(null);
        toscaPolicy.setTypeVersion(null);

        assertThatThrownBy(() -> {
            originalServiceTemplate.fromAuthorative(toscaServiceTemplate);
        }).hasMessage("Type not specified, the type of this TOSCA entity must be specified in the type field");

        toscaPolicy.setType("IDontExist");
        assertThatThrownBy(() -> {
            originalServiceTemplate.fromAuthorative(toscaServiceTemplate);
        }).hasMessage("Version not specified, the version of this TOSCA entity must be "
            + "specified in the type_version field");

        toscaPolicy.setTypeVersion("hello");
        assertThatThrownBy(() -> {
            originalServiceTemplate.fromAuthorative(toscaServiceTemplate);
        }).hasMessageContaining("value \"hello\", does not match regular expression");

        toscaPolicy.setTypeVersion("99.100.101");
        originalServiceTemplate.fromAuthorative(toscaServiceTemplate);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().createPolicies(pfDao, originalServiceTemplate);
        }).hasMessageContaining("policy type").hasMessageContaining("IDontExist:99.100.101")
            .hasMessageContaining(Validated.NOT_FOUND);

        toscaPolicy.setType("IDontExist");
        originalServiceTemplate.fromAuthorative(toscaServiceTemplate);

        toscaPolicy.setType(null);

        assertThatThrownBy(() -> {
            originalServiceTemplate.fromAuthorative(toscaServiceTemplate);
        }).hasMessage("Type not specified, the type of this TOSCA entity must be specified in the type field");

        toscaPolicy.setType(originalPolicyType);
        toscaPolicy.setTypeVersion(originalPolicyTypeVersion);

        originalServiceTemplate.fromAuthorative(toscaServiceTemplate);
        JpaToscaServiceTemplate createdServiceTemplate =
            new SimpleToscaProvider().createPolicies(pfDao, originalServiceTemplate);
        assertEquals(originalServiceTemplate.getTopologyTemplate().getPolicies(),
            createdServiceTemplate.getTopologyTemplate().getPolicies());
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

        assertEquals(originalServiceTemplate.getTopologyTemplate(), createdServiceTemplate.getTopologyTemplate());

        PfConceptKey policyKey = new PfConceptKey("onap.restart.tca:1.0.0");

        assertThatThrownBy(() -> new SimpleToscaProvider().deletePolicy(pfDao, new PfConceptKey("IDontExist:0.0.1")))
            .hasMessage("policy IDontExist:0.0.1 not found");

        JpaToscaServiceTemplate deletedServiceTemplate = new SimpleToscaProvider().deletePolicy(pfDao, policyKey);

        assertEquals(0, originalServiceTemplate.getTopologyTemplate().getPolicies().get(policyKey)
            .compareTo(deletedServiceTemplate.getTopologyTemplate().getPolicies().get(policyKey)));

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getPolicies(pfDao, policyKey.getName(), policyKey.getVersion());
        }).hasMessage("policies for onap.restart.tca:1.0.0 do not exist");

        assertThatThrownBy(() -> new SimpleToscaProvider().deletePolicy(pfDao, policyKey))
            .hasMessage("no policies found");

        new SimpleToscaProvider().createPolicies(pfDao, originalServiceTemplate);
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
    public void testGetServiceTemplate() throws PfModelException {
        assertThatThrownBy(() -> new SimpleToscaProvider().getServiceTemplate(pfDao))
            .hasMessage("service template not found in database");
    }

    @Test
    public void testAppendToServiceTemplate() throws PfModelException {
        JpaToscaServiceTemplate serviceTemplateFragment = new JpaToscaServiceTemplate();
        serviceTemplateFragment.setPolicyTypes(new JpaToscaPolicyTypes());
        JpaToscaPolicyType badPt = new JpaToscaPolicyType();
        serviceTemplateFragment.getPolicyTypes().getConceptMap().put(badPt.getKey(), badPt);

        assertThatThrownBy(() -> new SimpleToscaProvider().appendToServiceTemplate(pfDao, serviceTemplateFragment))
            .hasMessageContaining("key on concept entry").hasMessageContaining("NULL:0.0.0")
            .hasMessageContaining(Validated.IS_A_NULL_KEY);
    }

    @Test
    public void testGetDataTypesCornerCases() throws PfModelException {
        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getDataTypes(pfDao, "hello", "0.0.1");
        }).hasMessageMatching("service template not found in database");

        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());
        JpaToscaPolicyType pt0 = new JpaToscaPolicyType(new PfConceptKey("p0:0.0.1"));
        serviceTemplate.getPolicyTypes().getConceptMap().put(pt0.getKey(), pt0);

        new SimpleToscaProvider().createPolicyTypes(pfDao, serviceTemplate);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getDataTypes(pfDao, "hello", "0.0.1");
        }).hasMessageMatching("data types for hello:0.0.1 do not exist");

        serviceTemplate.setDataTypes(new JpaToscaDataTypes());

        JpaToscaDataType dt01 = new JpaToscaDataType(new PfConceptKey("dt0:0.0.1"));
        dt01.setConstraints(new ArrayList<JpaToscaConstraint>());
        dt01.setMetadata(new TreeMap<String, String>());
        dt01.setProperties(new LinkedHashMap<String, JpaToscaProperty>());
        serviceTemplate.getDataTypes().getConceptMap().put(dt01.getKey(), dt01);

        new SimpleToscaProvider().createDataTypes(pfDao, serviceTemplate);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getDataTypes(pfDao, "hello", "0.0.1");
        }).hasMessageMatching("data types for hello:0.0.1 do not exist");

        JpaToscaServiceTemplate gotSt =
            new SimpleToscaProvider().getDataTypes(pfDao, dt01.getName(), dt01.getVersion());

        assertEquals(dt01, gotSt.getDataTypes().get(dt01.getKey()));
        assertEquals(dt01, gotSt.getDataTypes().get(dt01.getName()));
        assertEquals(dt01, gotSt.getDataTypes().get(dt01.getName(), null));
        assertEquals(dt01, gotSt.getDataTypes().get(dt01.getName(), dt01.getVersion()));
        assertEquals(1, gotSt.getDataTypes().getAll(null).size());
        assertEquals(1, gotSt.getDataTypes().getAll(null, null).size());
        assertEquals(1, gotSt.getDataTypes().getAll(dt01.getName(), null).size());
        assertEquals(1, gotSt.getDataTypes().getAll(dt01.getName(), dt01.getVersion()).size());

        JpaToscaDataType dt02 = new JpaToscaDataType(new PfConceptKey("dt0:0.0.2"));
        dt02.setConstraints(new ArrayList<JpaToscaConstraint>());
        dt02.setMetadata(new TreeMap<String, String>());
        dt02.setProperties(new LinkedHashMap<String, JpaToscaProperty>());
        serviceTemplate.getDataTypes().getConceptMap().put(dt02.getKey(), dt02);

        new SimpleToscaProvider().createDataTypes(pfDao, serviceTemplate);
        gotSt = new SimpleToscaProvider().getDataTypes(pfDao, dt01.getName(), null);

        assertEquals(dt01, gotSt.getDataTypes().get(dt01.getKey()));
        assertEquals(dt02, gotSt.getDataTypes().get(dt01.getName()));
        assertEquals(dt02, gotSt.getDataTypes().get(dt01.getName(), null));
        assertEquals(dt01, gotSt.getDataTypes().get(dt01.getName(), dt01.getVersion()));
        assertEquals(dt02, gotSt.getDataTypes().get(dt01.getName(), dt02.getVersion()));
        assertEquals(2, gotSt.getDataTypes().getAll(null).size());
        assertEquals(2, gotSt.getDataTypes().getAll(null, null).size());
        assertEquals(2, gotSt.getDataTypes().getAll(dt01.getName(), null).size());
        assertEquals(1, gotSt.getDataTypes().getAll(dt01.getName(), dt02.getVersion()).size());
    }

    @Test
    public void testGetPolicyTypesCornerCases() throws PfModelException {
        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getPolicyTypes(pfDao, "hello", "0.0.1");
        }).hasMessageMatching("service template not found in database");

        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setDataTypes(new JpaToscaDataTypes());
        JpaToscaDataType dt0 = new JpaToscaDataType(new PfConceptKey("dt0:0.0.1"));
        serviceTemplate.getDataTypes().getConceptMap().put(dt0.getKey(), dt0);

        new SimpleToscaProvider().createDataTypes(pfDao, serviceTemplate);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getPolicyTypes(pfDao, "hello", "0.0.1");
        }).hasMessageMatching("policy types for hello:0.0.1 do not exist");

        serviceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());

        JpaToscaPolicyType pt01 = new JpaToscaPolicyType(new PfConceptKey("p0:0.0.1"));
        pt01.setMetadata(new TreeMap<String, String>());
        pt01.setProperties(new LinkedHashMap<String, JpaToscaProperty>());
        pt01.setTargets(new ArrayList<PfConceptKey>());
        pt01.setTriggers(new ArrayList<JpaToscaTrigger>());
        serviceTemplate.getPolicyTypes().getConceptMap().put(pt01.getKey(), pt01);

        new SimpleToscaProvider().createPolicyTypes(pfDao, serviceTemplate);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getPolicyTypes(pfDao, "hello", "0.0.1");
        }).hasMessageMatching("policy types for hello:0.0.1 do not exist");

        JpaToscaServiceTemplate gotSt =
            new SimpleToscaProvider().getPolicyTypes(pfDao, pt01.getName(), pt01.getVersion());

        assertEquals(pt01, gotSt.getPolicyTypes().get(pt01.getKey()));
        assertEquals(pt01, gotSt.getPolicyTypes().get(pt01.getName()));
        assertEquals(pt01, gotSt.getPolicyTypes().get(pt01.getName(), null));
        assertEquals(pt01, gotSt.getPolicyTypes().get(pt01.getName(), pt01.getVersion()));
        assertEquals(1, gotSt.getPolicyTypes().getAll(null).size());
        assertEquals(1, gotSt.getPolicyTypes().getAll(null, null).size());
        assertEquals(1, gotSt.getPolicyTypes().getAll(pt01.getName(), null).size());
        assertEquals(1, gotSt.getPolicyTypes().getAll(pt01.getName(), pt01.getVersion()).size());

        JpaToscaPolicyType pt02 = new JpaToscaPolicyType(new PfConceptKey("p0:0.0.2"));
        pt02.setMetadata(new TreeMap<String, String>());
        pt02.setProperties(new LinkedHashMap<String, JpaToscaProperty>());
        pt02.setTargets(new ArrayList<PfConceptKey>());
        pt02.setTriggers(new ArrayList<JpaToscaTrigger>());
        serviceTemplate.getPolicyTypes().getConceptMap().put(pt02.getKey(), pt02);

        new SimpleToscaProvider().createPolicyTypes(pfDao, serviceTemplate);
        gotSt = new SimpleToscaProvider().getPolicyTypes(pfDao, pt01.getName(), null);

        assertEquals(pt01, gotSt.getPolicyTypes().get(pt01.getKey()));
        assertEquals(pt02, gotSt.getPolicyTypes().get(pt01.getName()));
        assertEquals(pt02, gotSt.getPolicyTypes().get(pt01.getName(), null));
        assertEquals(pt01, gotSt.getPolicyTypes().get(pt01.getName(), pt01.getVersion()));
        assertEquals(pt02, gotSt.getPolicyTypes().get(pt01.getName(), pt02.getVersion()));
        assertEquals(2, gotSt.getPolicyTypes().getAll(null).size());
        assertEquals(2, gotSt.getPolicyTypes().getAll(null, null).size());
        assertEquals(2, gotSt.getPolicyTypes().getAll(pt01.getName(), null).size());
        assertEquals(1, gotSt.getPolicyTypes().getAll(pt01.getName(), pt02.getVersion()).size());
    }

    @Test
    public void testGetPoliciesCornerCases() throws PfModelException {
        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getPolicies(pfDao, "hello", "0.0.1");
        }).hasMessageMatching("service template not found in database");

        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setDataTypes(new JpaToscaDataTypes());
        JpaToscaDataType dt0 = new JpaToscaDataType(new PfConceptKey("dt0:0.0.1"));
        dt0.setConstraints(new ArrayList<JpaToscaConstraint>());
        dt0.setMetadata(new TreeMap<String, String>());
        dt0.setProperties(new LinkedHashMap<String, JpaToscaProperty>());
        serviceTemplate.getDataTypes().getConceptMap().put(dt0.getKey(), dt0);

        new SimpleToscaProvider().createDataTypes(pfDao, serviceTemplate);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getPolicies(pfDao, "hello", "0.0.1");
        }).hasMessageMatching("policies for hello:0.0.1 do not exist");

        serviceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());

        JpaToscaPolicyType pt01 = new JpaToscaPolicyType(new PfConceptKey("pt0:0.0.1"));
        pt01.setMetadata(new TreeMap<String, String>());
        pt01.setProperties(new LinkedHashMap<String, JpaToscaProperty>());
        pt01.setTargets(new ArrayList<PfConceptKey>());
        pt01.setTriggers(new ArrayList<JpaToscaTrigger>());
        serviceTemplate.getPolicyTypes().getConceptMap().put(pt01.getKey(), pt01);

        serviceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate());
        serviceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies());

        JpaToscaPolicy p01 = new JpaToscaPolicy(new PfConceptKey("p0:0.0.1"));
        p01.setType(pt01.getKey());
        p01.setMetadata(new TreeMap<String, String>());
        p01.setProperties(new LinkedHashMap<String, String>());
        p01.setTargets(new ArrayList<PfConceptKey>());
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(p01.getKey(), p01);

        new SimpleToscaProvider().createPolicies(pfDao, serviceTemplate);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getPolicies(pfDao, "hello", "0.0.1");
        }).hasMessageMatching("policies for hello:0.0.1 do not exist");

        JpaToscaServiceTemplate gotSt = new SimpleToscaProvider().getPolicies(pfDao, p01.getName(), p01.getVersion());

        assertEquals(0, p01.compareTo(gotSt.getTopologyTemplate().getPolicies().get(p01.getKey())));
        assertEquals(0, p01.compareTo(gotSt.getTopologyTemplate().getPolicies().get(p01.getName())));
        assertEquals(0, p01.compareTo(gotSt.getTopologyTemplate().getPolicies().get(p01.getName(), null)));
        assertEquals(0, p01.compareTo(gotSt.getTopologyTemplate().getPolicies().get(p01.getName(), p01.getVersion())));
        assertEquals(1, gotSt.getTopologyTemplate().getPolicies().getAll(null).size());
        assertEquals(1, gotSt.getTopologyTemplate().getPolicies().getAll(null, null).size());
        assertEquals(1, gotSt.getTopologyTemplate().getPolicies().getAll(p01.getName(), null).size());
        assertEquals(1, gotSt.getTopologyTemplate().getPolicies().getAll(p01.getName(), p01.getVersion()).size());

        JpaToscaPolicy p02 = new JpaToscaPolicy(new PfConceptKey("p0:0.0.2"));
        p02.setType(pt01.getKey());
        p02.setType(pt01.getKey());
        p02.setMetadata(new TreeMap<String, String>());
        p02.setProperties(new LinkedHashMap<String, String>());
        p02.setTargets(new ArrayList<PfConceptKey>());
        serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap().put(p02.getKey(), p02);

        new SimpleToscaProvider().createPolicies(pfDao, serviceTemplate);
        gotSt = new SimpleToscaProvider().getPolicies(pfDao, p01.getName(), null);

        assertEquals(p01, gotSt.getTopologyTemplate().getPolicies().get(p01.getKey()));
        assertEquals(p02, gotSt.getTopologyTemplate().getPolicies().get(p01.getName()));
        assertEquals(p02, gotSt.getTopologyTemplate().getPolicies().get(p01.getName(), null));
        assertEquals(p01, gotSt.getTopologyTemplate().getPolicies().get(p01.getName(), p01.getVersion()));
        assertEquals(p02, gotSt.getTopologyTemplate().getPolicies().get(p01.getName(), p02.getVersion()));
        assertEquals(2, gotSt.getTopologyTemplate().getPolicies().getAll(null).size());
        assertEquals(2, gotSt.getTopologyTemplate().getPolicies().getAll(null, null).size());
        assertEquals(2, gotSt.getTopologyTemplate().getPolicies().getAll(p01.getName(), null).size());
        assertEquals(1, gotSt.getTopologyTemplate().getPolicies().getAll(p01.getName(), p02.getVersion()).size());
    }

    @Test
    public void testNonNullsDataType() {
        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getServiceTemplate(null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().appendToServiceTemplate(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().appendToServiceTemplate(null, new JpaToscaServiceTemplate());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().appendToServiceTemplate(pfDao, null);
        }).hasMessageMatching("^incomingServiceTemplateFragment is marked .*on.*ull but is null$");

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
        }).hasMessageMatching(INCOMING_TEMPLATE_IS_NULL);

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
    }

    @Test
    public void testNotNullsPolicyTypes() {
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
        }).hasMessageMatching(INCOMING_TEMPLATE_IS_NULL);

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
        }).hasMessageMatching(INCOMING_TEMPLATE_IS_NULL);

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

    @Test
    public void testDeleteServiceTemplate() throws PfModelException {
        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deleteServiceTemplate(null);
        }).hasMessageMatching("^dao is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            new SimpleToscaProvider().deleteServiceTemplate(pfDao);
        }).hasMessage("service template not found in database");

        PfConceptKey dataType0Key = new PfConceptKey("DataType0", "0.0.1");
        JpaToscaDataType dataType0 = new JpaToscaDataType();
        dataType0.setKey(dataType0Key);
        dataType0.setConstraints(new ArrayList<JpaToscaConstraint>());
        dataType0.setMetadata(new TreeMap<String, String>());
        dataType0.setProperties(new LinkedHashMap<String, JpaToscaProperty>());

        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();
        serviceTemplate.setDataTypes(new JpaToscaDataTypes());
        serviceTemplate.getDataTypes().getConceptMap().put(dataType0Key, dataType0);

        JpaToscaServiceTemplate createdServiceTemplate =
            new SimpleToscaProvider().createDataTypes(pfDao, serviceTemplate);

        assertEquals(1, createdServiceTemplate.getDataTypes().getConceptMap().size());
        assertEquals(dataType0, createdServiceTemplate.getDataTypes().get(dataType0Key));
        assertEquals(null, createdServiceTemplate.getDataTypes().get(dataType0Key).getDescription());

        JpaToscaServiceTemplate deletedServiceTemplate = new SimpleToscaProvider().deleteServiceTemplate(pfDao);

        assertEquals(dataType0, deletedServiceTemplate.getDataTypes().get(dataType0Key));
    }

    @Test
    public void testNullParameters() {
        assertThatThrownBy(() -> {
            new SimpleToscaProvider().getCascadedDataTypes(null, null, null);
        }).hasMessageMatching("^dbServiceTemplate is marked .*on.*ull but is null$");
    }

    private void createPolicyTypes() throws CoderException, PfModelException {
        Object yamlObject =
            new Yaml().load(ResourceUtils.getResourceAsString("policytypes/onap.policies.monitoring.tcagen2.yaml"));
        String yamlAsJsonString = new StandardCoder().encode(yamlObject);

        ToscaServiceTemplate toscaServiceTemplatePolicyType =
            standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplatePolicyType);
        new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplatePolicyType);
    }
}
