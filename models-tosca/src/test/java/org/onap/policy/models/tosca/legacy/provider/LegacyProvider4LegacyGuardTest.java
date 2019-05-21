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

import java.util.Map;
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
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyContent;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;
import org.yaml.snakeyaml.Yaml;

/**
 * Test the {@link LegacyProvider} class for legacy guard policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyProvider4LegacyGuardTest {
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
            new LegacyProvider().getGuardPolicy(null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().getGuardPolicy(null, null, "");
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().getGuardPolicy(pfDao, null, null);
        }).hasMessage("policyId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().getGuardPolicy(pfDao, "I Dont Exist", null);
        }).hasMessage("no policy found for policy: I Dont Exist:null");

        createPolicyTypes();

        LegacyGuardPolicyInput originalGip = standardCoder.decode(
                ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.input.json"),
                LegacyGuardPolicyInput.class);

        assertNotNull(originalGip);

        Map<String, LegacyGuardPolicyOutput> createdGopm = new LegacyProvider().createGuardPolicy(pfDao, originalGip);

        assertEquals(originalGip.getPolicyId(), createdGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                createdGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        Map<String, LegacyGuardPolicyOutput> gotGopm =
                new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId(), null);

        assertEquals(originalGip.getPolicyId(), gotGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                gotGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        String expectedJsonOutput =
                ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.output.json");
        String actualJsonOutput = standardCoder.encode(gotGopm);

        assertEquals(expectedJsonOutput.replaceAll("\\s+", ""), actualJsonOutput.replaceAll("\\s+", ""));

        gotGopm = new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId(), "1");

        assertEquals(originalGip.getPolicyId(), gotGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                gotGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        actualJsonOutput = standardCoder.encode(gotGopm);

        assertEquals(expectedJsonOutput.replaceAll("\\s+", ""), actualJsonOutput.replaceAll("\\s+", ""));

        assertThatThrownBy(() -> {
            new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId(), "2");
        }).hasMessage("no policy found for policy: guard.frequency.scaleout:2");
    }

    @Test
    public void testPolicyCreate() throws Exception {
        assertThatThrownBy(() -> {
            new LegacyProvider().createGuardPolicy(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().createGuardPolicy(null, new LegacyGuardPolicyInput());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().createGuardPolicy(pfDao, null);
        }).hasMessage("legacyGuardPolicy is marked @NonNull but is null");

        createPolicyTypes();

        LegacyGuardPolicyInput originalGip = standardCoder.decode(
                ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.input.json"),
                LegacyGuardPolicyInput.class);

        assertNotNull(originalGip);

        Map<String, LegacyGuardPolicyOutput> createdGopm = new LegacyProvider().createGuardPolicy(pfDao, originalGip);

        assertEquals(originalGip.getPolicyId(), createdGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                createdGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        Map<String, LegacyGuardPolicyOutput> gotGopm =
                new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId(), null);

        assertEquals(originalGip.getPolicyId(), gotGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                gotGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        String expectedJsonOutput =
                ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.output.json");
        String actualJsonOutput = standardCoder.encode(gotGopm);

        assertEquals(expectedJsonOutput.replaceAll("\\s+", ""), actualJsonOutput.replaceAll("\\s+", ""));
    }

    @Test
    public void testPolicyCreateBad() throws Exception {
        assertThatThrownBy(() -> {
            new LegacyProvider().createGuardPolicy(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().createGuardPolicy(null, new LegacyGuardPolicyInput());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().createGuardPolicy(pfDao, null);
        }).hasMessage("legacyGuardPolicy is marked @NonNull but is null");

        createPolicyTypes();

        LegacyGuardPolicyInput originalGip = standardCoder.decode(
                ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.input.json"),
                LegacyGuardPolicyInput.class);

        assertNotNull(originalGip);

        originalGip.setPolicyId("i.do.not.exist");

        assertThatThrownBy(() -> {
            new LegacyProvider().createGuardPolicy(pfDao, originalGip);
        }).hasMessage("policy type for guard policy \"i.do.not.exist\" unknown");
    }

    @Test
    public void testPolicyUpdate() throws Exception {
        assertThatThrownBy(() -> {
            new LegacyProvider().updateGuardPolicy(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().updateGuardPolicy(null, new LegacyGuardPolicyInput());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().updateGuardPolicy(pfDao, null);
        }).hasMessage("legacyGuardPolicy is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().updateGuardPolicy(pfDao, new LegacyGuardPolicyInput());
        }).hasMessage("policy type for guard policy \"null\" unknown");

        createPolicyTypes();

        LegacyGuardPolicyInput originalGip = standardCoder.decode(
                ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.input.json"),
                LegacyGuardPolicyInput.class);

        assertNotNull(originalGip);

        Map<String, LegacyGuardPolicyOutput> createdGopm = new LegacyProvider().createGuardPolicy(pfDao, originalGip);
        assertEquals(originalGip.getPolicyId(), createdGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                createdGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        Map<String, LegacyGuardPolicyOutput> gotGopm =
                new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId(), null);

        assertEquals(originalGip.getPolicyId(), gotGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                gotGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        originalGip.getContent().setRecipe("Roast Turkey");
        Map<String, LegacyGuardPolicyOutput> updatedGp = new LegacyProvider().updateGuardPolicy(pfDao, originalGip);
        assertEquals(originalGip.getPolicyId(), updatedGp.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                updatedGp.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        Map<String, LegacyGuardPolicyOutput> gotUpdatedGopm =
                new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId(), null);
        assertEquals(originalGip.getPolicyId(), gotUpdatedGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                gotUpdatedGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());
        assertEquals("Roast Turkey",
                gotUpdatedGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next().getRecipe());
    }


    @Test
    public void testPoliciesDelete() throws Exception {
        assertThatThrownBy(() -> {
            new LegacyProvider().deleteGuardPolicy(null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().deleteGuardPolicy(null, null, "");
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().deleteGuardPolicy(null, "", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().deleteGuardPolicy(null, "", "");
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().deleteGuardPolicy(pfDao, null, null);
        }).hasMessage("policyId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().deleteGuardPolicy(pfDao, null, "");
        }).hasMessage("policyId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().deleteGuardPolicy(pfDao, "", null);
        }).hasMessage("policyVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new LegacyProvider().deleteGuardPolicy(pfDao, "IDontExist", "");
        }).hasMessage("no policy found for policy: IDontExist:");

        createPolicyTypes();

        LegacyGuardPolicyInput originalGip = standardCoder.decode(
                ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.input.json"),
                LegacyGuardPolicyInput.class);

        assertNotNull(originalGip);

        Map<String, LegacyGuardPolicyOutput> createdGopm = new LegacyProvider().createGuardPolicy(pfDao, originalGip);
        assertEquals(originalGip.getPolicyId(), createdGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                createdGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        Map<String, LegacyGuardPolicyOutput> gotGopm =
                new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId(), null);

        assertEquals(originalGip.getPolicyId(), gotGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                gotGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        String expectedJsonOutput =
                ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.output.json");
        String actualJsonOutput = standardCoder.encode(gotGopm);

        assertEquals(expectedJsonOutput.replaceAll("\\s+", ""), actualJsonOutput.replaceAll("\\s+", ""));

        assertThatThrownBy(() -> {
            new LegacyProvider().deleteGuardPolicy(pfDao, originalGip.getPolicyId(), null);
        }).hasMessage("policyVersion is marked @NonNull but is null");

        Map<String, LegacyGuardPolicyOutput> deletedGopm =
                new LegacyProvider().deleteGuardPolicy(pfDao, originalGip.getPolicyId(), "1");
        assertEquals(originalGip.getPolicyId(), deletedGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                deletedGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        assertThatThrownBy(() -> {
            new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId(), null);
        }).hasMessage("no policy found for policy: guard.frequency.scaleout:null");

        LegacyGuardPolicyInput otherGip = new LegacyGuardPolicyInput();
        otherGip.setPolicyId("guard.blacklist.b0");
        otherGip.setPolicyVersion("1");
        otherGip.setContent(new LegacyGuardPolicyContent());

        Map<String, LegacyGuardPolicyOutput> createdOtherGopm = new LegacyProvider().createGuardPolicy(pfDao, otherGip);
        assertEquals(otherGip.getPolicyId(), createdOtherGopm.keySet().iterator().next());
        assertEquals(otherGip.getContent(),
                createdOtherGopm.get(otherGip.getPolicyId()).getProperties().values().iterator().next());

        assertThatThrownBy(() -> {
            new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId(), null);
        }).hasMessage("no policy found for policy: guard.frequency.scaleout:null");
    }

    private void createPolicyTypes() throws CoderException, PfModelException {
        Object yamlObject = new Yaml().load(
                ResourceUtils.getResourceAsString("policytypes/onap.policies.controlloop.guard.FrequencyLimiter.yaml"));
        String yamlAsJsonString = new StandardCoder().encode(yamlObject);

        ToscaServiceTemplate toscaServiceTemplatePolicyType =
                standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplatePolicyType);
        new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplatePolicyType);

        yamlObject = new Yaml()
                .load(ResourceUtils.getResourceAsString("policytypes/onap.policies.controlloop.guard.Blacklist.yaml"));
        yamlAsJsonString = new StandardCoder().encode(yamlObject);

        toscaServiceTemplatePolicyType = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplatePolicyType);
        new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplatePolicyType);
    }
}
