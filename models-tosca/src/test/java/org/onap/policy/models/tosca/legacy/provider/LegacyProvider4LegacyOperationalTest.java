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

package org.onap.policy.models.tosca.legacy.provider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.provider.AuthorativeToscaProvider;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.yaml.snakeyaml.Yaml;

/**
 * Test the {@link LegacyProvider} class for legacy operational policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyProvider4LegacyOperationalTest {
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

        daoParameters.setJdbcProperties(jdbcProperties );

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);
    }

    /**
     * Set up standard coder.
     */
    @Before
    public void setupStandardCoder() {
        standardCoder = new StandardCoder();
    }

    @After
    public void teardown() throws Exception {
        pfDao.close();
    }

    @Test
    public void testPoliciesGet() throws Exception {
        assertThatThrownBy(() -> {
            new LegacyProvider().getOperationalPolicy(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().getOperationalPolicy(null, "");
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().getOperationalPolicy(pfDao, null);
        }).hasMessage("policyId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().getOperationalPolicy(pfDao, "I Dont Exist");
        }).hasMessage("no policy found for policy ID: I Dont Exist");

        createPolicyTypes();

        LegacyOperationalPolicy originalLop =
                standardCoder.decode(ResourceUtils.getResourceAsString("policies/vCPE.policy.operational.input.json"),
                        LegacyOperationalPolicy.class);

        assertNotNull(originalLop);

        LegacyOperationalPolicy createdLop = new LegacyProvider().createOperationalPolicy(pfDao, originalLop);

        assertEquals(originalLop, createdLop);

        LegacyOperationalPolicy gotLop = new LegacyProvider().getOperationalPolicy(pfDao, originalLop.getPolicyId());

        assertEquals(gotLop, originalLop);

        String expectedJsonOutput = ResourceUtils.getResourceAsString("policies/vCPE.policy.operational.output.json");
        String actualJsonOutput = standardCoder.encode(gotLop);

        assertEquals(expectedJsonOutput.replaceAll("\\s+", ""), actualJsonOutput.replaceAll("\\s+", ""));

        LegacyOperationalPolicy createdLopV2 = new LegacyProvider().createOperationalPolicy(pfDao, originalLop);
        LegacyOperationalPolicy gotLopV2 = new LegacyProvider().getOperationalPolicy(pfDao, originalLop.getPolicyId());
        assertEquals(gotLopV2, createdLopV2);
    }

    @Test
    public void testPolicyCreate() throws Exception {
        assertThatThrownBy(() -> {
            new LegacyProvider().createOperationalPolicy(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().createOperationalPolicy(null, new LegacyOperationalPolicy());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().createOperationalPolicy(pfDao, null);
        }).hasMessage("legacyOperationalPolicy is marked @NonNull but is null");

        createPolicyTypes();

        LegacyOperationalPolicy originalLop =
                standardCoder.decode(ResourceUtils.getResourceAsString("policies/vCPE.policy.operational.input.json"),
                        LegacyOperationalPolicy.class);

        assertNotNull(originalLop);

        LegacyOperationalPolicy createdLop = new LegacyProvider().createOperationalPolicy(pfDao, originalLop);

        assertEquals(originalLop, createdLop);

        LegacyOperationalPolicy gotLop = new LegacyProvider().getOperationalPolicy(pfDao, originalLop.getPolicyId());

        assertEquals(gotLop, originalLop);

        String expectedJsonOutput = ResourceUtils.getResourceAsString("policies/vCPE.policy.operational.output.json");
        String actualJsonOutput = standardCoder.encode(gotLop);

        assertEquals(expectedJsonOutput.replaceAll("\\s+", ""), actualJsonOutput.replaceAll("\\s+", ""));
    }

    @Test
    public void testPolicyUpdate() throws Exception {
        assertThatThrownBy(() -> {
            new LegacyProvider().updateOperationalPolicy(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().updateOperationalPolicy(null, new LegacyOperationalPolicy());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().updateOperationalPolicy(pfDao, null);
        }).hasMessage("legacyOperationalPolicy is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().updateOperationalPolicy(pfDao, new LegacyOperationalPolicy());
        }).hasMessage("no policy found for policy ID: null");

        createPolicyTypes();

        LegacyOperationalPolicy originalLop =
                standardCoder.decode(ResourceUtils.getResourceAsString("policies/vCPE.policy.operational.input.json"),
                        LegacyOperationalPolicy.class);

        assertNotNull(originalLop);

        LegacyOperationalPolicy createdLop = new LegacyProvider().createOperationalPolicy(pfDao, originalLop);
        assertEquals(originalLop, createdLop);

        LegacyOperationalPolicy gotLop = new LegacyProvider().getOperationalPolicy(pfDao, originalLop.getPolicyId());
        assertEquals(gotLop, originalLop);

        originalLop.setContent("Some New Content");
        LegacyOperationalPolicy updatedLop = new LegacyProvider().updateOperationalPolicy(pfDao, originalLop);
        assertEquals(originalLop, updatedLop);

        LegacyOperationalPolicy gotUpdatedLop =
                new LegacyProvider().getOperationalPolicy(pfDao, originalLop.getPolicyId());
        assertEquals(gotUpdatedLop, originalLop);
        assertEquals("Some New Content", gotUpdatedLop.getContent());
    }

    @Test
    public void testPoliciesDelete() throws Exception {
        assertThatThrownBy(() -> {
            new LegacyProvider().deleteOperationalPolicy(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().deleteOperationalPolicy(null, "");

        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().deleteOperationalPolicy(pfDao, null);
        }).hasMessage("policyId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().deleteOperationalPolicy(pfDao, "I Dont Exist");
        }).hasMessage("no policy found for policy ID: I Dont Exist");

        createPolicyTypes();

        LegacyOperationalPolicy originalLop =
                standardCoder.decode(ResourceUtils.getResourceAsString("policies/vCPE.policy.operational.input.json"),
                        LegacyOperationalPolicy.class);

        assertNotNull(originalLop);

        LegacyOperationalPolicy createdLop = new LegacyProvider().createOperationalPolicy(pfDao, originalLop);
        assertEquals(originalLop, createdLop);

        LegacyOperationalPolicy gotLop = new LegacyProvider().getOperationalPolicy(pfDao, originalLop.getPolicyId());

        assertEquals(gotLop, originalLop);

        String expectedJsonOutput = ResourceUtils.getResourceAsString("policies/vCPE.policy.operational.output.json");
        String actualJsonOutput = standardCoder.encode(gotLop);

        assertEquals(expectedJsonOutput.replaceAll("\\s+", ""), actualJsonOutput.replaceAll("\\s+", ""));

        LegacyOperationalPolicy deletedLop =
                new LegacyProvider().deleteOperationalPolicy(pfDao, originalLop.getPolicyId());
        assertEquals(originalLop, deletedLop);

        assertThatThrownBy(() -> {
            new LegacyProvider().getOperationalPolicy(pfDao, originalLop.getPolicyId());
        }).hasMessage("no policy found for policy ID: operational.restart");

        LegacyOperationalPolicy otherLop = new LegacyOperationalPolicy();
        otherLop.setPolicyId("another-policy");
        otherLop.setPolicyVersion("1");
        otherLop.setContent("content");

        LegacyOperationalPolicy createdOtherLop = new LegacyProvider().createOperationalPolicy(pfDao, otherLop);
        assertEquals(otherLop, createdOtherLop);

        assertThatThrownBy(() -> {
            new LegacyProvider().getOperationalPolicy(pfDao, originalLop.getPolicyId());
        }).hasMessage("no policy found for policy ID: operational.restart");
    }

    private void createPolicyTypes() throws CoderException, PfModelException {
        Object yamlObject = new Yaml().load(
                ResourceUtils.getResourceAsString("policytypes/onap.policies.controlloop.Operational.yaml"));
        String yamlAsJsonString = new StandardCoder().encode(yamlObject);

        ToscaServiceTemplate toscaServiceTemplatePolicyType =
                standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplatePolicyType);
        new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplatePolicyType);
    }
}
