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

package org.onap.policy.models.pdp.persistence.provider;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
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
import org.onap.policy.models.pdp.persistence.provider.PdpProvider;
import org.onap.policy.models.tosca.simple.provider.SimpleToscaProvider;

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
    public void testGroupsGet() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().getPdpGroups(null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().getPdpGroups(null, null, "version");
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().getPdpGroups(null, "name", "version");
        }).hasMessage("dao is marked @NonNull but is null");

        String originalJson = ResourceUtils.getResourceAsString("testdata/PdpGroups0.json");
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

    @Test
    public void testGroupsCreate() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().createPdpGroups(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().createPdpGroups(null, new ArrayList<>());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().createPdpGroups(pfDao, null);
        }).hasMessage("pdpGroups is marked @NonNull but is null");

        String originalJson = ResourceUtils.getResourceAsString("testdata/PdpGroups0.json");
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

    @Test
    public void testGroupsCreateNoPdp() throws Exception {
        String originalJson = ResourceUtils.getResourceAsString("testdata/PdpGroupsNoPDPs.json");

        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        PdpGroups createdPdpGroups0 = new PdpGroups();
        createdPdpGroups0.setGroups(new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()));
        assertNotEquals(pdpGroups0, createdPdpGroups0);
        pdpGroups0.getGroups().get(0).getPdpSubgroups().get(0).setPdpInstances(new ArrayList<>());
        String originalTweakedJson = standardCoder.encode(pdpGroups0);
        String createdJson = standardCoder.encode(createdPdpGroups0);
        assertEquals(originalTweakedJson.replaceAll("\\s+", ""), createdJson.replaceAll("\\s+", ""));

        PdpGroups gotPdpGroups0 = new PdpGroups();
        gotPdpGroups0.setGroups(new PdpProvider().getPdpGroups(pfDao, "TestPdpGroup", "1.2.3"));

        String gotJson = standardCoder.encode(gotPdpGroups0);
        assertEquals(originalTweakedJson.replaceAll("\\s+", ""), gotJson.replaceAll("\\s+", ""));
    }

    @Test
    public void testGroupsUpdate() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpGroups(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpGroups(null, new ArrayList<>());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpGroups(pfDao, null);
        }).hasMessage("pdpGroups is marked @NonNull but is null");

        String originalJson = ResourceUtils.getResourceAsString("testdata/PdpGroups0.json");
        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        PdpGroups createdPdpGroups0 = new PdpGroups();
        createdPdpGroups0.setGroups(new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()));
        String createdJson = standardCoder.encode(createdPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), createdJson.replaceAll("\\s+", ""));

        PdpGroups gotPdpGroups0 = new PdpGroups();
        gotPdpGroups0.setGroups(new PdpProvider().getPdpGroups(pfDao, "PdpGroup0", "1.2.3"));

        String gotJson = standardCoder.encode(gotPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), gotJson.replaceAll("\\s+", ""));

        String updateJson = ResourceUtils.getResourceAsString("testdata/PdpGroups0Update.json");
        PdpGroups updatePdpGroups0 = standardCoder.decode(updateJson, PdpGroups.class);

        PdpGroups updatedPdpGroups0 = new PdpGroups();
        updatedPdpGroups0.setGroups(new PdpProvider().updatePdpGroups(pfDao, updatePdpGroups0.getGroups()));

        List<Pdp> beforePdpInstances = updatePdpGroups0.getGroups().get(0).getPdpSubgroups().get(0).getPdpInstances();
        List<Pdp> afterPdpInstances = updatedPdpGroups0.getGroups().get(0).getPdpSubgroups().get(0).getPdpInstances();
        assertTrue(beforePdpInstances.containsAll(afterPdpInstances));
    }

    @Test
    public void testPoliciesDelete() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(null, null, "version");
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(null, "name", "version");
        }).hasMessage("dao is marked @NonNull but is null");

        String originalJson = ResourceUtils.getResourceAsString("testdata/PdpGroups0.json");
        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        PdpGroups createdPdpGroups0 = new PdpGroups();
        createdPdpGroups0.setGroups(new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()));
        String createdJson = standardCoder.encode(createdPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), createdJson.replaceAll("\\s+", ""));

        PdpGroups gotPdpGroups0 = new PdpGroups();
        gotPdpGroups0.setGroups(new PdpProvider().getPdpGroups(pfDao, "PdpGroup0", "1.2.3"));

        String gotJson = standardCoder.encode(gotPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), gotJson.replaceAll("\\s+", ""));

        PdpGroup deletedPdpGroup = new PdpProvider().deletePdpGroup(pfDao, "PdpGroup0", "1.2.3");

        assertEquals(createdPdpGroups0.getGroups().get(0), deletedPdpGroup);

        assertEquals(0, new PdpProvider().getPdpGroups(pfDao, "PdpGroup0", "1.2.3").size());

        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(pfDao, "PdpGroup0", "1.2.3");
        }).hasMessage("delete of PDP group \"PdpGroup0:1.2.3\" failed, PDP group does not exist");
    }
}
