/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021, 2023-2025 Nordix Foundation.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2024 Nordix Foundation
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
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.yaml.snakeyaml.Yaml;

/**
 * Test of the {@link AuthorativeToscaProvider} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
class AuthorativeToscaProviderGenericTest {
    private static final String POLICY_NO_VERSION_VERSION1 = "onap.policies.NoVersion:0.0.1";
    private static final String POLICY_NO_VERSION = "onap.policies.NoVersion";
    private static final String DAO_IS_NULL = "^dao is marked .*on.*ull but is null$";
    private static final String VERSION_001 = "0.0.1";
    private static String yamlAsJsonString;
    private PfDao pfDao;
    private StandardCoder standardCoder;

    /**
     * Read the policy type definition.
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
            "jdbc:h2:mem:AuthorativeToscaProviderGenericTest");
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
    void testCreateGetDelete() throws Exception {
        assertThatThrownBy(() -> new AuthorativeToscaProvider().getServiceTemplateList(null, null, null))
            .hasMessageMatching(DAO_IS_NULL);

        ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplate);
        ToscaServiceTemplate createdServiceTemplate =
            new AuthorativeToscaProvider().createServiceTemplate(pfDao, toscaServiceTemplate);

        PfConceptKey policyTypeKey = new PfConceptKey(POLICY_NO_VERSION_VERSION1);

        ToscaPolicyType beforePolicyType = toscaServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        ToscaPolicyType createdPolicyType = createdServiceTemplate.getPolicyTypes().get(policyTypeKey.getName());
        assertEquals(beforePolicyType.getName(), createdPolicyType.getName());
        assertEquals(0, ObjectUtils.compare(beforePolicyType.getDescription(), createdPolicyType.getDescription()));

        List<ToscaServiceTemplate> gotServiceTemplateList =
            new AuthorativeToscaProvider().getServiceTemplateList(pfDao, null, null);

        ToscaPolicyType gotPolicyType = gotServiceTemplateList.get(0).getPolicyTypes().get(policyTypeKey.getName());
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

        ToscaServiceTemplate deletedServiceTemplate =
            new AuthorativeToscaProvider().deleteServiceTemplate(pfDao, "Dummy", "0.0.1");
        assertEquals(2, deletedServiceTemplate.getPolicyTypes().size());
    }

    @Test
    void testNullParameters() {
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
