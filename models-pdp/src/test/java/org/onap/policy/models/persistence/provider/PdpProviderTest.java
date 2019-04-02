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

package org.onap.policy.models.persistence.provider;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroups;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.concepts.PolicyTypeIdent;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.pdp.persistence.provider.PdpProvider;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * Test the {@link SimpleToscaProvider} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PdpProviderTest {
    private Connection connection;
    private PfDao pfDao;
    private StandardCoder standardCoder;


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
        standardCoder = new StandardCoder();
    }

    @After
    public void teardown() throws Exception {
        pfDao.close();
        connection.close();
    }

    @Test
    public void testPoliciesGet() throws Exception {
        /*
         * try { new PdpProvider().gePdpGroupst(null, null); fail("test should throw an exception here"); } catch
         * (Exception exc) { assertEquals("dao is marked @NonNull but is null", exc.getMessage()); }
         *
         * try { new SimpleToscaProvider().getPolicies(null, new PfConceptKey());
         * fail("test should throw an exception here"); } catch (Exception exc) {
         * assertEquals("dao is marked @NonNull but is null", exc.getMessage()); }
         *
         * try { new SimpleToscaProvider().getPolicies(pfDao, null); fail("test should throw an exception here"); }
         * catch (Exception exc) { assertEquals("policyKey is marked @NonNull but is null", exc.getMessage()); }
         */

        String originalJson = ResourceUtils.getResourceAsString("testdata/PdpGroups0.json");

        @SuppressWarnings("unchecked")
        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        PdpGroups createdPdpGroups0 = new PdpGroups();
        createdPdpGroups0.setGroups(new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()));
        String createdJson = standardCoder.encode(createdPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), createdJson.replaceAll("\\s+", ""));

        PdpGroups gotPdpGroups0 = new PdpGroups();
        gotPdpGroups0.setGroups(new PdpProvider().getPdpGroups(pfDao, "PdpGroup0", "1.2.3"));

        String gotJson = standardCoder.encode(gotPdpGroups0);

        assertEquals(originalJson.replaceAll("\\s+", ""), gotJson.replaceAll("\\s+", ""));

    }
    /*
     * @Test public void testPolicyCreate() throws Exception { try { new SimpleToscaProvider().createPolicies(null,
     * null); fail("test should throw an exception here"); } catch (Exception exc) {
     * assertEquals("dao is marked @NonNull but is null", exc.getMessage()); }
     *
     * try { new SimpleToscaProvider().createPolicies(null, new JpaToscaServiceTemplate());
     * fail("test should throw an exception here"); } catch (Exception exc) {
     * assertEquals("dao is marked @NonNull but is null", exc.getMessage()); }
     *
     * try { new SimpleToscaProvider().createPolicies(pfDao, null); fail("test should throw an exception here"); } catch
     * (Exception exc) { assertEquals("serviceTemplate is marked @NonNull but is null", exc.getMessage()); }
     *
     * ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
     * ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json"),
     * ToscaServiceTemplate.class);
     *
     * JpaToscaServiceTemplate originalServiceTemplate = new JpaToscaServiceTemplate();
     * originalServiceTemplate.fromAuthorative(toscaServiceTemplate);
     *
     * assertNotNull(originalServiceTemplate); JpaToscaServiceTemplate createdServiceTemplate = new
     * SimpleToscaProvider().createPolicies(pfDao, originalServiceTemplate);
     *
     * assertEquals(originalServiceTemplate, createdServiceTemplate); }
     *
     * @Test public void testPolicyUpdate() throws Exception { try { new SimpleToscaProvider().updatePolicies(null,
     * null); fail("test should throw an exception here"); } catch (Exception exc) {
     * assertEquals("dao is marked @NonNull but is null", exc.getMessage()); }
     *
     * try { new SimpleToscaProvider().updatePolicies(null, new JpaToscaServiceTemplate());
     * fail("test should throw an exception here"); } catch (Exception exc) {
     * assertEquals("dao is marked @NonNull but is null", exc.getMessage()); }
     *
     * try { new SimpleToscaProvider().updatePolicies(pfDao, null); fail("test should throw an exception here"); } catch
     * (Exception exc) { assertEquals("serviceTemplate is marked @NonNull but is null", exc.getMessage()); }
     *
     * ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
     * ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json"),
     * ToscaServiceTemplate.class);
     *
     * JpaToscaServiceTemplate originalServiceTemplate = new JpaToscaServiceTemplate();
     * originalServiceTemplate.fromAuthorative(toscaServiceTemplate);
     *
     * assertNotNull(originalServiceTemplate); JpaToscaServiceTemplate updatedServiceTemplate = new
     * SimpleToscaProvider().updatePolicies(pfDao, originalServiceTemplate);
     *
     * assertEquals(originalServiceTemplate, updatedServiceTemplate); }
     *
     * @Test public void testPoliciesDelete() throws Exception { try { new SimpleToscaProvider().deletePolicy(null,
     * null); fail("test should throw an exception here"); } catch (Exception exc) {
     * assertEquals("dao is marked @NonNull but is null", exc.getMessage()); }
     *
     * try { new SimpleToscaProvider().deletePolicy(null, new PfConceptKey());
     * fail("test should throw an exception here"); } catch (Exception exc) {
     * assertEquals("dao is marked @NonNull but is null", exc.getMessage()); }
     *
     * try { new SimpleToscaProvider().deletePolicy(pfDao, null); fail("test should throw an exception here"); } catch
     * (Exception exc) { assertEquals("policyKey is marked @NonNull but is null", exc.getMessage()); }
     *
     * ToscaServiceTemplate toscaServiceTemplate = standardCoder.decode(
     * ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json"),
     * ToscaServiceTemplate.class);
     *
     * JpaToscaServiceTemplate originalServiceTemplate = new JpaToscaServiceTemplate();
     * originalServiceTemplate.fromAuthorative(toscaServiceTemplate);
     *
     * assertNotNull(originalServiceTemplate); JpaToscaServiceTemplate createdServiceTemplate = new
     * SimpleToscaProvider().createPolicies(pfDao, originalServiceTemplate);
     *
     * assertEquals(originalServiceTemplate, createdServiceTemplate);
     *
     * PfConceptKey policyKey = new PfConceptKey("onap.restart.tca:1.0.0");
     *
     * JpaToscaServiceTemplate deletedServiceTemplate = new SimpleToscaProvider().deletePolicy(pfDao, new
     * PfConceptKey(policyKey));
     *
     * assertEquals(originalServiceTemplate.getTopologyTemplate().getPolicies().get(policyKey),
     * deletedServiceTemplate.getTopologyTemplate().getPolicies().get(policyKey));
     *
     * try { new SimpleToscaProvider().getPolicies(pfDao, new PfConceptKey(policyKey));
     * fail("test should throw an exception here"); } catch (Exception exc) {
     * assertEquals("policy not found: onap.restart.tca:1.0.0", exc.getMessage()); } }
     *
     * @Test public void testAssertPoliciesExist() throws PfModelException { JpaToscaServiceTemplate testServiceTemplate
     * = new JpaToscaServiceTemplate();
     *
     * try { new SimpleToscaProvider().createPolicies(pfDao, testServiceTemplate);
     * fail("test should throw an exception here"); } catch (Exception exc) {
     * assertEquals("topology template not specified on service template", exc.getMessage()); }
     *
     * testServiceTemplate.setTopologyTemplate(new JpaToscaTopologyTemplate()); try { new
     * SimpleToscaProvider().createPolicies(pfDao, testServiceTemplate); fail("test should throw an exception here"); }
     * catch (Exception exc) { assertEquals("no policies specified on topology template of service template",
     * exc.getMessage()); }
     *
     * testServiceTemplate.getTopologyTemplate().setPolicies(new JpaToscaPolicies()); try { new
     * SimpleToscaProvider().createPolicies(pfDao, testServiceTemplate); fail("test should throw an exception here"); }
     * catch (Exception exc) {
     * assertEquals("list of policies specified on topology template of service template is empty", exc.getMessage()); }
     *
     * }
     */
}
