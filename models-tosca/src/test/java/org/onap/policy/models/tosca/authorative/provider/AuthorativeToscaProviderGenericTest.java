/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 Nordix Foundation.
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

package org.onap.policy.models.tosca.authorative.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
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
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.yaml.snakeyaml.Yaml;

/**
 * Test of the {@link AuthorativeToscaProvider} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class AuthorativeToscaProviderGenericTest {
    private static final String POLICY_NO_VERSION_VERSION1 = "onap.policies.NoVersion:0.0.1";
    private static final String POLICY_NO_VERSION = "onap.policies.NoVersion";
    private static final String DAO_IS_NULL = "^dao is marked .*on.*ull but is null$";
    private static final String VERSION_001 = "0.0.1";
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
        String yamlString = ResourceUtils.getResourceAsString("src/test/resources/onap.policies.NoVersion.yaml");

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

        if (System.getProperty("USE-MARIADB") != null) {
            jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_DRIVER, "org.mariadb.jdbc.Driver");
            jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL, "jdbc:mariadb://localhost:3306/policy");
        } else {
            jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_DRIVER, "org.h2.Driver");
            jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL,
                            "jdbc:h2:mem:AuthorativeToscaProviderGenericTest");
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
    public void testCreateGetDelete() throws Exception {
        assertThatThrownBy(() -> {
            new AuthorativeToscaProvider().getServiceTemplateList(null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
                new AuthorativeToscaProvider().createServiceTemplate(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_NO_VERSION_VERSION1);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(createdPolicyType.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        List<ToscaServiceTemplate> gotServiceTemplateList =
                new AuthorativeToscaProvider().getServiceTemplateList(pfDao, null, null);

        ToscaPolicyType gotPolicyType = gotServiceTemplateList.get(0).getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        List<ToscaPolicyType> gotPolicyTypeList =
                new AuthorativeToscaProvider().getPolicyTypeList(pfDao, POLICY_NO_VERSION, VERSION_001);
        assertEquals(2, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));

        gotPolicyTypeList = new AuthorativeToscaProvider().getPolicyTypeList(pfDao, POLICY_NO_VERSION, null);
        assertEquals(2, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));

        gotPolicyTypeList = new AuthorativeToscaProvider().getPolicyTypeList(pfDao, null, null);
        assertEquals(2, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));

        gotPolicyTypeList = new AuthorativeToscaProvider().getPolicyTypeList(pfDao, null, VERSION_001);
        assertEquals(2, gotPolicyTypeList.size());
        assertEquals(true, beforePolicyType.getName().equals(gotPolicyType.getName()));

        assertThatThrownBy(() -> new AuthorativeToscaProvider().getPolicyTypeList(new DefaultPfDao(), POLICY_NO_VERSION,
                VERSION_001)).hasMessageContaining("Policy Framework DAO has not been initialized");

        assertTrue(new AuthorativeToscaProvider().getPolicyTypeList(pfDao, "i.dont.Exist", VERSION_001).isEmpty());
    }

    @Test
    public void testDeleteByName() throws CoderException, PfModelException {
        final var toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        final var name = RandomStringUtils.randomAlphabetic(5);
        final var version = "2.3.4";

        assertNotNull(toscaServiceTemplate);
        toscaServiceTemplate.setName(name);
        toscaServiceTemplate.setVersion(version);
        final var serviceTemplate = new AuthorativeToscaProvider().createServiceTemplate(pfDao, toscaServiceTemplate);
        final var actual = List.of(serviceTemplate);

        final var deleteServiceTemplate = new AuthorativeToscaProvider().deleteServiceTemplate(pfDao, name);

        assertThat(deleteServiceTemplate).isEqualTo(actual);
    }

    @Test
    public void testDeleteByNameAndVersion() throws CoderException, PfModelException {
        final var toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        final var name = RandomStringUtils.randomAlphabetic(5);
        final var version = "2.3.4";

        assertNotNull(toscaServiceTemplate);
        toscaServiceTemplate.setName(name);
        toscaServiceTemplate.setVersion(version);
        final var serviceTemplate = new AuthorativeToscaProvider().createServiceTemplate(pfDao, toscaServiceTemplate);

        final var deleteServiceTemplate = new AuthorativeToscaProvider().deleteServiceTemplate(pfDao, name, version);

        assertThat(deleteServiceTemplate).isEqualTo(serviceTemplate);
    }

    @Test
    public void testGetServiceTemplateListByName() throws CoderException, PfModelException {
        final var toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        final var name = RandomStringUtils.randomAlphabetic(5);
        final var version = "2.3.4";

        assertNotNull(toscaServiceTemplate);
        toscaServiceTemplate.setName(name);
        toscaServiceTemplate.setVersion(version);
        final var serviceTemplate = new AuthorativeToscaProvider().createServiceTemplate(pfDao, toscaServiceTemplate);
        final var actual = List.of(serviceTemplate);

        final var deleteServiceTemplate = new AuthorativeToscaProvider().getServiceTemplateList(pfDao, name);

        assertThat(deleteServiceTemplate).isEqualTo(actual);
    }

    @Test
    public void testGetServiceTemplateListByNameNotFound() throws CoderException, PfModelException {
        final var toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        final var name = RandomStringUtils.randomAlphabetic(5);
        final var nameNotFound = RandomStringUtils.randomAlphabetic(5);
        final var version = "2.3.4";

        assertNotNull(toscaServiceTemplate);
        toscaServiceTemplate.setName(name);
        toscaServiceTemplate.setVersion(version);
        new AuthorativeToscaProvider().createServiceTemplate(pfDao, toscaServiceTemplate);

        final var deleteServiceTemplate = new AuthorativeToscaProvider().getServiceTemplateList(pfDao, nameNotFound);

        assertThat(deleteServiceTemplate).isEmpty();
    }

    @Test
    public void testGetServiceTemplateListByNameAndVersion() throws CoderException, PfModelException {
        final var toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        final var name = RandomStringUtils.randomAlphabetic(5);
        final var version = "2.3.4";

        assertNotNull(toscaServiceTemplate);
        toscaServiceTemplate.setName(name);
        toscaServiceTemplate.setVersion(version);
        final var serviceTemplate = new AuthorativeToscaProvider().createServiceTemplate(pfDao, toscaServiceTemplate);
        final var actual = List.of(serviceTemplate);

        final var deleteServiceTemplate = new AuthorativeToscaProvider().getServiceTemplateList(pfDao, name, version);

        assertThat(deleteServiceTemplate).isEqualTo(actual);
    }

    @Test
    public void testGetServiceTemplateListByNameAndVersionNameNotFound() throws CoderException, PfModelException {
        final var toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        final var name = RandomStringUtils.randomAlphabetic(5);
        final var nameNotFound = RandomStringUtils.randomAlphabetic(5);
        final var version = "2.3.4";

        assertNotNull(toscaServiceTemplate);
        toscaServiceTemplate.setName(name);
        toscaServiceTemplate.setVersion(version);
        new AuthorativeToscaProvider().createServiceTemplate(pfDao, toscaServiceTemplate);

        final var deleteServiceTemplate = new AuthorativeToscaProvider().getServiceTemplateList(pfDao, nameNotFound,
            version);

        assertThat(deleteServiceTemplate).isEmpty();
    }

    @Test
    public void testGetServiceTemplateListByNameAndVersionNotFound() throws CoderException, PfModelException {
        final var toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        final var name = RandomStringUtils.randomAlphabetic(5);
        final var version = "2.3.4";
        final var versionNotFound = "9.8.9";

        assertNotNull(toscaServiceTemplate);
        toscaServiceTemplate.setName(name);
        toscaServiceTemplate.setVersion(version);
        new AuthorativeToscaProvider().createServiceTemplate(pfDao, toscaServiceTemplate);

        final var deleteServiceTemplate = new AuthorativeToscaProvider().getServiceTemplateList(pfDao, name,
            versionNotFound);

        assertThat(deleteServiceTemplate).isEmpty();
    }

    @Test
    public void testNullParameters() throws Exception {
        assertThatThrownBy(() -> new AuthorativeToscaProvider().getServiceTemplateList(null, null, null))
                .hasMessageMatching("^dao is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().createServiceTemplate(null, null))
                .hasMessageMatching("^dao is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().createServiceTemplate(pfDao, null))
                .hasMessageMatching("^serviceTemplate is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().createServiceTemplate(null, new ToscaServiceTemplate()))
                .hasMessageMatching("^dao is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deleteServiceTemplate(null, null, null))
                .hasMessageMatching("^dao is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deleteServiceTemplate(null, null, "0.0.1"))
                .hasMessageMatching("^dao is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deleteServiceTemplate(null, "Dummy", null))
                .hasMessageMatching("^dao is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deleteServiceTemplate(null, "Dummy", "0.0.1"))
                .hasMessageMatching("^dao is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deleteServiceTemplate(pfDao, null, null))
                .hasMessageMatching("^name is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deleteServiceTemplate(pfDao, null, "0.0.1"))
                .hasMessageMatching("^name is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> new AuthorativeToscaProvider().deleteServiceTemplate(pfDao, "Dummy", null))
                .hasMessageMatching("^version is marked .*on.*ull but is null$");
    }
}
