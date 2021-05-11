/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 Nordix Foundation.
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

package org.onap.policy.models.tosca.simple.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Properties;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaDataTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicyTypes;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;

/**
 * Test the {@link SimpleToscaProvider} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class SimpleToscaServiceTemplateProviderTest {
    private static final String TEMPLATE_IS_NULL = "^serviceTemplate is marked .*on.*ull but is null$";
    private static final String DAO_IS_NULL = "^dao is marked .*on.*ull but is null$";
    private static final String VERSION_IS_NULL = "^version is marked .*on.*ull but is null$";
    private static final String NAME_IS_NULL = "^conceptName is marked .*on.*ull but is null$";

    private PfDao pfDao;

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
                            "jdbc:h2:mem:SimpleToscaServiceTemplateProviderTest");
        }

        daoParameters.setJdbcProperties(jdbcProperties);

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);
    }

    @After
    public void teardown() {
        pfDao.close();
    }

    @Test
    public void testCreateUpdateGetDeleteDataType() throws PfModelException {
        JpaToscaServiceTemplate serviceTemplate = new JpaToscaServiceTemplate();

        JpaToscaServiceTemplate dbServiceTemplate =
            new SimpleToscaServiceTemplateProvider().write(pfDao, serviceTemplate);

        assertEquals(serviceTemplate, dbServiceTemplate);

        JpaToscaServiceTemplate readServiceTemplate = new SimpleToscaServiceTemplateProvider().read(pfDao);
        assertEquals(serviceTemplate, readServiceTemplate);

        assertNull(readServiceTemplate.getDataTypes());

        PfConceptKey dataType0Key = new PfConceptKey("DataType0", "0.0.1");
        JpaToscaDataType dataType0 = new JpaToscaDataType();
        dataType0.setKey(dataType0Key);
        serviceTemplate.setDataTypes(new JpaToscaDataTypes());
        serviceTemplate.getDataTypes().getConceptMap().put(dataType0Key, dataType0);

        dbServiceTemplate = new SimpleToscaServiceTemplateProvider().write(pfDao, serviceTemplate);
        assertEquals(serviceTemplate, dbServiceTemplate);

        readServiceTemplate = new SimpleToscaServiceTemplateProvider().read(pfDao);
        assertEquals(serviceTemplate, readServiceTemplate);

        assertEquals(1, readServiceTemplate.getDataTypes().getConceptMap().size());
        assertEquals(dataType0, readServiceTemplate.getDataTypes().get(dataType0Key));
        assertNull(readServiceTemplate.getDataTypes().get(dataType0Key).getDescription());

        dataType0.setDescription("Updated Description");

        dbServiceTemplate = new SimpleToscaServiceTemplateProvider().write(pfDao, serviceTemplate);
        assertEquals(serviceTemplate, dbServiceTemplate);

        readServiceTemplate = new SimpleToscaServiceTemplateProvider().read(pfDao);
        assertEquals(serviceTemplate, readServiceTemplate);

        assertEquals(dataType0, readServiceTemplate.getDataTypes().get(dataType0Key));
        assertEquals("Updated Description", readServiceTemplate.getDataTypes().get(dataType0Key).getDescription());

        PfConceptKey policyType0Key = new PfConceptKey("PolicyType0", "0.0.1");

        JpaToscaPolicyType policyType0 = new JpaToscaPolicyType();

        policyType0.setKey(policyType0Key);
        serviceTemplate.setPolicyTypes(new JpaToscaPolicyTypes());

        serviceTemplate.getPolicyTypes().getConceptMap().put(policyType0Key, policyType0);

        dbServiceTemplate = new SimpleToscaServiceTemplateProvider().write(pfDao, serviceTemplate);
        assertEquals(serviceTemplate, dbServiceTemplate);

        readServiceTemplate = new SimpleToscaServiceTemplateProvider().read(pfDao);
        assertEquals(serviceTemplate, readServiceTemplate);

        JpaToscaServiceTemplate deletedServiceTemplate = new SimpleToscaServiceTemplateProvider().delete(pfDao);
        assertEquals(serviceTemplate, deletedServiceTemplate);

        readServiceTemplate = new SimpleToscaServiceTemplateProvider().read(pfDao);
        assertNull(readServiceTemplate);
    }

    @Test
    public void readByName() throws PfModelException {
        final var name = RandomStringUtils.randomAlphabetic(4);
        final var version = "2.3.4";
        final var dbServiceTemplate = ServiceTemplateUtils.prepareDbServiceTemplate(pfDao, name, version);

        final var actual = new SimpleToscaServiceTemplateProvider()
            .readByName(pfDao, name);

        final var expected = List.of(dbServiceTemplate);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void readByNameNotFound() throws PfModelException {
        final var name = RandomStringUtils.randomAlphabetic(4);
        final var version = "2.3.4";
        ServiceTemplateUtils.prepareDbServiceTemplate(pfDao, name, version);
        final var conceptName = RandomStringUtils.randomAlphanumeric(3, 5);
        final var actual = new SimpleToscaServiceTemplateProvider()
            .readByName(pfDao, conceptName);

        assertThat(actual).isEmpty();
    }

    @Test
    public void readByNameAndVersion() throws PfModelException {
        final var name = RandomStringUtils.randomAlphabetic(4);
        final var version = "2.3.4";
        final var dbServiceTemplate = ServiceTemplateUtils.prepareDbServiceTemplate(pfDao, name, version);

        final var actual = new SimpleToscaServiceTemplateProvider()
            .readByNameAndVersion(pfDao, name, version);

        final var expected = List.of(dbServiceTemplate);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void readByNameAndVersionNameNotFound() throws PfModelException {
        final var name = RandomStringUtils.randomAlphabetic(4);
        final var version = "2.3.4";
        final var notFoundName = RandomStringUtils.randomAlphabetic(9);
        ServiceTemplateUtils.prepareDbServiceTemplate(pfDao, name, version);

        final var actual = new SimpleToscaServiceTemplateProvider()
            .readByNameAndVersion(pfDao, notFoundName, version);

        assertThat(actual).isEmpty();
    }

    @Test
    public void readByNameAndVersionNotFound() throws PfModelException {
        final var name = RandomStringUtils.randomAlphabetic(4);
        final var version = "2.3.4";
        final var notFoundVersion = "9.0.0";
        ServiceTemplateUtils.prepareDbServiceTemplate(pfDao, name, version);

        final var actual = new SimpleToscaServiceTemplateProvider()
            .readByNameAndVersion(pfDao, name, notFoundVersion);

        assertThat(actual).isEmpty();
    }

    @Test
    public void deleteByName() throws PfModelException {
        final var name = RandomStringUtils.randomAlphabetic(4);
        final var version = "2.3.4";
        final var jpaToscaServiceTemplate = ServiceTemplateUtils.prepareDbServiceTemplate(pfDao, name, version);

        final var expected = List.of(jpaToscaServiceTemplate);

        final var actual = new SimpleToscaServiceTemplateProvider()
            .delete(pfDao, name);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void deleteByNameNotFound() throws PfModelException {
        final var name = RandomStringUtils.randomAlphabetic(4);
        final var notFoundName = RandomStringUtils.randomAlphabetic(4);
        final var version = "2.3.4";
        ServiceTemplateUtils.prepareDbServiceTemplate(pfDao, name, version);

        final var actual = new SimpleToscaServiceTemplateProvider()
            .delete(pfDao, notFoundName);

        assertThat(actual).isEmpty();
    }

    @Test
    public void deleteByNameAndVersion() throws PfModelException {
        final var name = RandomStringUtils.randomAlphabetic(4);
        final var version = "2.3.4";

        final var jpaToscaServiceTemplate = ServiceTemplateUtils.prepareDbServiceTemplate(pfDao, name, version);

        final var actual = new SimpleToscaServiceTemplateProvider()
            .delete(pfDao, name, version);

        assertThat(actual).isEqualTo(jpaToscaServiceTemplate);
    }

    @Test
    public void deleteByNameAndVersionNameNotFound() throws PfModelException {
        final var name = RandomStringUtils.randomAlphabetic(4);
        final var notPresentName = RandomStringUtils.randomAlphabetic(9);
        final var version = "2.3.4";
        ServiceTemplateUtils.prepareDbServiceTemplate(pfDao, name, version);

        final var actual = new SimpleToscaServiceTemplateProvider()
            .delete(pfDao, notPresentName, version);

        assertThat(actual).isNull();
    }

    @Test
    public void deleteByNameAndVersionNotFound() throws PfModelException {
        final var name = RandomStringUtils.randomAlphabetic(4);
        final var version = "9.0.0";
        final var notPresentVersion = "2.3.4";
        ServiceTemplateUtils.prepareDbServiceTemplate(pfDao, name, version);

        final var actual = new SimpleToscaServiceTemplateProvider()
            .delete(pfDao, name, notPresentVersion);

        assertThat(actual).isNull();
    }

    @Test
    public void testNonNulls() {
        assertThatThrownBy(() -> {
            new SimpleToscaServiceTemplateProvider().write(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaServiceTemplateProvider().write(pfDao, null);
        }).hasMessageMatching(TEMPLATE_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaServiceTemplateProvider().write(null, new JpaToscaServiceTemplate());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaServiceTemplateProvider().read(null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new SimpleToscaServiceTemplateProvider().delete(null);
        }).hasMessageMatching(DAO_IS_NULL);
    }

    @Test
    public void testReadByNameNonNulls() {
        assertThatThrownBy(() -> new SimpleToscaServiceTemplateProvider().readByName(null, ""))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new SimpleToscaServiceTemplateProvider().readByName(pfDao, null))
            .hasMessageMatching(NAME_IS_NULL);
    }

    @Test
    public void testReadByNameAndVersionNonNulls() {
        assertThatThrownBy(() -> new SimpleToscaServiceTemplateProvider().readByNameAndVersion(null, "", ""))
            .hasMessageMatching(DAO_IS_NULL);
    }

    @Test
    public void testDeleteByNameNonNulls() {
        assertThatThrownBy(() -> new SimpleToscaServiceTemplateProvider().delete(null, ""))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> new SimpleToscaServiceTemplateProvider().delete(pfDao, null))
            .hasMessageMatching(NAME_IS_NULL);
    }

    @Test
    public void testDeleteByNameAndVersionNonNulls() {
        assertThatThrownBy(
            () -> new SimpleToscaServiceTemplateProvider().delete(null, "", "")
        ).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(
            () -> new SimpleToscaServiceTemplateProvider().delete(pfDao, null, "")
        ).hasMessageMatching(NAME_IS_NULL);

        assertThatThrownBy(
            () -> new SimpleToscaServiceTemplateProvider().delete(pfDao, "", null)
        ).hasMessageMatching(VERSION_IS_NULL);
    }
}
