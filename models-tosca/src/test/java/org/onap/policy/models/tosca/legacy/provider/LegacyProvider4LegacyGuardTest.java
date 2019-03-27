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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyContent;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyOutput;

/**
 * Test the {@link LegacyProvider} class for legacy guard policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyProvider4LegacyGuardTest {
    private Connection connection;
    private PfDao pfDao;
    private Gson gson;


    /**
     * Set up the DAO towards the database.
     *
     * @throws Exception on database errors
     */
    @Before
    public void setupDao() throws Exception {
        // Use the JDBC UI "jdbc:h2:mem:testdb" to test towards the h2 database
        // Use the JDBC UI "jdbc:mariadb://localhost:3306/policy" to test towards a locally installed mariadb instance
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb", "policy", "P01icY");

        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getCanonicalName());

        // Use the persistence unit ToscaConceptTest to test towards the h2 database
        // Use the persistence unit ToscaConceptMariaDBTest to test towards a locally installed mariadb instance
        daoParameters.setPersistenceUnit("ToscaConceptTest");

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);
    }

    /**
     * Set up GSON.
     */
    @Before
    public void setupGson() {
        gson = new Gson();
    }

    @After
    public void teardown() throws Exception {
        pfDao.close();
        connection.close();
    }

    @Test
    public void testPoliciesGet() throws PfModelException {
        try {
            new LegacyProvider().getGuardPolicy(null, null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("dao is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new LegacyProvider().getGuardPolicy(null, "");
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("dao is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new LegacyProvider().getGuardPolicy(pfDao, null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("policyId is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new LegacyProvider().getGuardPolicy(pfDao, "I Dont Exist");
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("no policy found for policy ID: I Dont Exist", exc.getMessage());
        }

        LegacyGuardPolicyInput originalGip =
                gson.fromJson(ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.input.json"),
                        LegacyGuardPolicyInput.class);

        assertNotNull(originalGip);

        Map<String, LegacyGuardPolicyOutput> createdGopm = new LegacyProvider().createGuardPolicy(pfDao, originalGip);

        assertEquals(originalGip.getPolicyId(), createdGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                createdGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        Map<String, LegacyGuardPolicyOutput> gotGopm =
                new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId());

        assertEquals(originalGip.getPolicyId(), gotGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                gotGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        String expectedJsonOutput =
                ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.output.json");
        String actualJsonOutput = gson.toJson(gotGopm);

        assertEquals(expectedJsonOutput.replaceAll("\\s+", ""), actualJsonOutput.replaceAll("\\s+", ""));
    }

    @Test
    public void testPolicyCreate() throws PfModelException {
        try {
            new LegacyProvider().createGuardPolicy(null, null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("dao is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new LegacyProvider().createGuardPolicy(null, new LegacyGuardPolicyInput());
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("dao is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new LegacyProvider().createGuardPolicy(pfDao, null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("legacyGuardPolicy is marked @NonNull but is null", exc.getMessage());
        }

        LegacyGuardPolicyInput originalGip =
                gson.fromJson(ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.input.json"),
                        LegacyGuardPolicyInput.class);

        assertNotNull(originalGip);

        Map<String, LegacyGuardPolicyOutput> createdGopm = new LegacyProvider().createGuardPolicy(pfDao, originalGip);

        assertEquals(originalGip.getPolicyId(), createdGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                createdGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        Map<String, LegacyGuardPolicyOutput> gotGopm =
                new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId());

        assertEquals(originalGip.getPolicyId(), gotGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                gotGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        String expectedJsonOutput =
                ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.output.json");
        String actualJsonOutput = gson.toJson(gotGopm);

        assertEquals(expectedJsonOutput.replaceAll("\\s+", ""), actualJsonOutput.replaceAll("\\s+", ""));
    }


    @Test
    public void testPolicyUpdate() throws PfModelException {
        try {
            new LegacyProvider().updateGuardPolicy(null, null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("dao is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new LegacyProvider().updateGuardPolicy(null, new LegacyGuardPolicyInput());
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("dao is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new LegacyProvider().updateGuardPolicy(pfDao, null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("legacyGuardPolicy is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new LegacyProvider().updateGuardPolicy(pfDao, new LegacyGuardPolicyInput());
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("policy type for guard policy \"null\" unknown", exc.getMessage());
        }

        LegacyGuardPolicyInput originalGip =
                gson.fromJson(ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.input.json"),
                        LegacyGuardPolicyInput.class);

        assertNotNull(originalGip);

        Map<String, LegacyGuardPolicyOutput> createdGopm = new LegacyProvider().createGuardPolicy(pfDao, originalGip);
        assertEquals(originalGip.getPolicyId(), createdGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                createdGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        Map<String, LegacyGuardPolicyOutput> gotGopm =
                new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId());

        assertEquals(originalGip.getPolicyId(), gotGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                gotGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        originalGip.getContent().setRecipe("Roast Turkey");
        Map<String, LegacyGuardPolicyOutput> updatedGp = new LegacyProvider().updateGuardPolicy(pfDao, originalGip);
        assertEquals(originalGip.getPolicyId(), updatedGp.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                updatedGp.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        Map<String, LegacyGuardPolicyOutput> gotUpdatedGopm =
                new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId());
        assertEquals(originalGip.getPolicyId(), gotUpdatedGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                gotUpdatedGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());
        assertEquals("Roast Turkey",
                gotUpdatedGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next().getRecipe());
    }


    @Test
    public void testPoliciesDelete() throws PfModelException {
        try {
            new LegacyProvider().deleteGuardPolicy(null, null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("dao is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new LegacyProvider().deleteGuardPolicy(null, "");
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("dao is marked @NonNull but is null", exc.getMessage());
        }

        try {
            new LegacyProvider().deleteGuardPolicy(pfDao, null);
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("policyId is marked @NonNull but is null", exc.getMessage());
        }


        try {
            new LegacyProvider().deleteGuardPolicy(pfDao, "I Dont Exist");
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("no policy found for policy ID: I Dont Exist", exc.getMessage());
        }

        LegacyGuardPolicyInput originalGip =
                gson.fromJson(ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.input.json"),
                        LegacyGuardPolicyInput.class);

        assertNotNull(originalGip);

        Map<String, LegacyGuardPolicyOutput> createdGopm = new LegacyProvider().createGuardPolicy(pfDao, originalGip);
        assertEquals(originalGip.getPolicyId(), createdGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                createdGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        Map<String, LegacyGuardPolicyOutput> gotGopm =
                new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId());

        assertEquals(originalGip.getPolicyId(), gotGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                gotGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        String expectedJsonOutput =
                ResourceUtils.getResourceAsString("policies/vDNS.policy.guard.frequency.output.json");
        String actualJsonOutput = gson.toJson(gotGopm);

        assertEquals(expectedJsonOutput.replaceAll("\\s+", ""), actualJsonOutput.replaceAll("\\s+", ""));

        Map<String, LegacyGuardPolicyOutput> deletedGopm =
                new LegacyProvider().deleteGuardPolicy(pfDao, originalGip.getPolicyId());
        assertEquals(originalGip.getPolicyId(), deletedGopm.keySet().iterator().next());
        assertEquals(originalGip.getContent(),
                deletedGopm.get(originalGip.getPolicyId()).getProperties().values().iterator().next());

        try {
            new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId());
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("no policy found for policy ID: guard.frequency.scaleout", exc.getMessage());
        }

        LegacyGuardPolicyInput otherGip = new LegacyGuardPolicyInput();
        otherGip.setPolicyId("guard.blacklist");
        otherGip.setPolicyVersion("1");
        otherGip.setContent(new LegacyGuardPolicyContent());

        Map<String, LegacyGuardPolicyOutput> createdOtherGopm = new LegacyProvider().createGuardPolicy(pfDao, otherGip);
        assertEquals(otherGip.getPolicyId(), createdOtherGopm.keySet().iterator().next());
        assertEquals(otherGip.getContent(),
                createdOtherGopm.get(otherGip.getPolicyId()).getProperties().values().iterator().next());

        try {
            new LegacyProvider().getGuardPolicy(pfDao, originalGip.getPolicyId());
            fail("test should throw an exception here");
        } catch (Exception exc) {
            assertEquals("no policy found for policy ID: guard.frequency.scaleout", exc.getMessage());
        }
    }
}
