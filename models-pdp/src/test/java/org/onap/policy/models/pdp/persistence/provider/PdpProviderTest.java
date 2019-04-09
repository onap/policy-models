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
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpGroups;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.pdp.persistence.provider.PdpProvider;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeIdentifier;
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
    public void testFilteredPdpGroupGet() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().getFilteredPdpGroups(null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().getFilteredPdpGroups(null, PdpGroupFilter.builder().build());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().getFilteredPdpGroups(pfDao, null);
        }).hasMessage("filter is marked @NonNull but is null");

        String originalJson = ResourceUtils.getResourceAsString("testdata/PdpGroupsForFiltering.json");
        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        assertEquals(5, new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()).size());

        List<ToscaPolicyTypeIdentifier> policyTypeList = new ArrayList<>();
        policyTypeList.add(new ToscaPolicyTypeIdentifier("policy.type.0", "1.2.3"));

        List<ToscaPolicyIdentifier> policyList = new ArrayList<>();
        policyList.add(new ToscaPolicyIdentifier("Policy0", "4.5.6"));

        // @formatter:off
        final PdpGroupFilter filter = PdpGroupFilter.builder()
                .groupState(PdpState.PASSIVE)
                .name("PdpGroup0")
                .version("1.2.3")
                .matchPoliciesExactly(false)
                .matchPolicyTypesExactly(false)
                .pdpState(PdpState.PASSIVE)
                .pdpType("APEX")
                .policyTypeList(policyTypeList)
                .policyList(policyList)
                .build();
        // @formatter:on
        assertEquals(1, new PdpProvider().getFilteredPdpGroups(pfDao, filter).size());
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

        pdpGroups0.getGroups().get(0).setPdpGroupState(null);
        assertThatThrownBy(() -> {
            new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups());
        }).hasMessageContaining("INVALID:pdpGroupState may not be null");
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

        pdpGroups0.getGroups().get(0).setPdpGroupState(null);
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpGroups(pfDao, pdpGroups0.getGroups());
        }).hasMessageContaining("INVALID:pdpGroupState may not be null");
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
            new PdpProvider().deletePdpGroup(null, "name", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(null, "name", "version");
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(pfDao, null, "version");
        }).hasMessage("name is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(pfDao, "name", null);
        }).hasMessage("version is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(pfDao, "name", "version");
        }).hasMessage("delete of PDP group \"name:version\" failed, PDP group does not exist");

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

    @Test
    public void testPdpSubgroupUpdate() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, null, null, new PdpSubGroup());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, null, "version", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, null, "version", new PdpSubGroup());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, "name", null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, "name", null, new PdpSubGroup());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, "name", "version", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, "name", "version", new PdpSubGroup());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, null, null, new PdpSubGroup());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, null, "version", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, null, "version", new PdpSubGroup());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, "name", null, null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, "name", null, new PdpSubGroup());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, "name", "version", null);
        }).hasMessage("pdpSubGroup is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, "name", "version", new PdpSubGroup());
        }).hasMessage("parameter \"localName\" is null");

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

        PdpSubGroup existingSubGroup = gotPdpGroups0.getGroups().get(0).getPdpSubgroups().get(0);
        existingSubGroup.setCurrentInstanceCount(10);
        existingSubGroup.setDesiredInstanceCount(10);
        new PdpProvider().updatePdpSubGroup(pfDao, "PdpGroup0", "1.2.3", existingSubGroup);

        List<PdpGroup> afterUpdatePdpGroups = new PdpProvider().getPdpGroups(pfDao, "PdpGroup0", "1.2.3");
        assertEquals(10, afterUpdatePdpGroups.get(0).getPdpSubgroups().get(0).getCurrentInstanceCount());
        assertEquals(10, afterUpdatePdpGroups.get(0).getPdpSubgroups().get(0).getDesiredInstanceCount());

        existingSubGroup.setDesiredInstanceCount(-1);
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, "PdpGroup0", "1.2.3", existingSubGroup);
        }).hasMessageContaining("INVALID:the desired instance count of a PDP sub group may not be negative");
        existingSubGroup.setDesiredInstanceCount(10);

        existingSubGroup.setPdpType("Loooooooooooooooooooooooooooooooooooooooo"
                + "ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooongKey");
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, "PdpGroup0", "1.2.3", existingSubGroup);
        }).hasMessageContaining("Value too long for column");
        existingSubGroup.setPdpType("APEX");
    }

    @Test
    public void testPdpUpdate() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, null, null, new Pdp());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, null, "TYPE", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, null, "TYPE", new Pdp());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, "version", null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, "version", null, new Pdp());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, "version", "TYPE", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, "version", "TYPE", new Pdp());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", null, null, new Pdp());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", null, "TYPE", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", null, "TYPE", new Pdp());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", "version", null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", "version", null, new Pdp());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", "version", "TYPE", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", "version", "TYPE", new Pdp());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, null, null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, null, null, new Pdp());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, null, "TYPE", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, null, "TYPE", new Pdp());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, "version", null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, "version", null, new Pdp());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, "version", "TYPE", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, "version", "TYPE", new Pdp());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", null, null, null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", null, null, new Pdp());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", null, "TYPE", null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", null, "TYPE", new Pdp());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", "version", null, null);
        }).hasMessage("pdpSubGroup is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", "version", null, new Pdp());
        }).hasMessage("pdpSubGroup is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", "version", "TYPE", null);
        }).hasMessage("pdp is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", "version", "TYPE", new Pdp());
        }).hasMessage("parameter \"localName\" is null");

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

        Pdp existingPdp = gotPdpGroups0.getGroups().get(0).getPdpSubgroups().get(0).getPdpInstances().get(0);
        existingPdp.setPdpState(PdpState.TEST);
        existingPdp.setHealthy(PdpHealthStatus.TEST_IN_PROGRESS);
        new PdpProvider().updatePdp(pfDao, "PdpGroup0", "1.2.3", "APEX", existingPdp);

        List<PdpGroup> afterUpdatePdpGroups = new PdpProvider().getPdpGroups(pfDao, "PdpGroup0", "1.2.3");
        assertEquals(PdpState.TEST,
                afterUpdatePdpGroups.get(0).getPdpSubgroups().get(0).getPdpInstances().get(0).getPdpState());
        assertEquals(PdpHealthStatus.TEST_IN_PROGRESS,
                afterUpdatePdpGroups.get(0).getPdpSubgroups().get(0).getPdpInstances().get(0).getHealthy());

        existingPdp.setMessage("");
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "PdpGroup0", "1.2.3", "APEX", existingPdp);
        }).hasMessageContaining("INVALID:message may not be blank");
        existingPdp.setMessage("A Message");
    }

    @Test
    public void testGetPdpStatistics() throws PfModelException {
        assertThatThrownBy(() -> {
            new PdpProvider().getPdpStatistics(null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().getPdpStatistics(null, null, "version");
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().getPdpStatistics(null, "name", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertEquals(0, new PdpProvider().getPdpStatistics(pfDao, "name", "version").size());
    }

    @Test
    public void testUpdatePdpStatistics() throws PfModelException {
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, null, null, new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, null, "inst", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, null, "inst", new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, "TYPE", null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, "TYPE", null, new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, "TYPE", "inst", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, "TYPE", "inst", new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "version", null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "version", null, null, new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "version", null, "inst", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "version",  null, "inst", new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "version", "TYPE", null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "version", "TYPE", null, new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "version", "TYPE", "inst", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "version", "TYPE", "inst", new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, null, null, new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, null, "inst", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, null, "inst", new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, "TYPE", null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, "TYPE", null, new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, "TYPE", "inst", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, "TYPE", "inst", new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "version", null, null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "version", null, null, new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "version", null, "inst", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "version", null, "inst", new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "version", "TYPE", null, null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "version", "TYPE", null, new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "version", "TYPE", "inst", null);
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "version", "TYPE", "inst", new PdpStatistics());
        }).hasMessage("dao is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, null, null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, null, null, new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, null, "inst", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, null, "inst", new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, "TYPE", null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, "TYPE", null, new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, "TYPE", "inst", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, "TYPE", "inst", new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "version", null, null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "version", null, null, new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "version", null, "inst", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "version",  null, "inst", new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "version", "TYPE", null, null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "version", "TYPE", null, new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "version", "TYPE", "inst", null);
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "version", "TYPE", "inst", new PdpStatistics());
        }).hasMessage("pdpGroupName is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, null, null, null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, null, null, new PdpStatistics());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, null, "inst", null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, null, "inst", new PdpStatistics());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, "TYPE", null, null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, "TYPE", null, new PdpStatistics());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, "TYPE", "inst", null);
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, "TYPE", "inst", new PdpStatistics());
        }).hasMessage("pdpGroupVersion is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", "version", null, null, null);
        }).hasMessage("pdpType is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", "version", null, null, new PdpStatistics());
        }).hasMessage("pdpType is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", "version", null, "inst", null);
        }).hasMessage("pdpType is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", "version", null, "inst", new PdpStatistics());
        }).hasMessage("pdpType is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", "version", "TYPE", null, null);
        }).hasMessage("pdpInstanceId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", "version", "TYPE", null, new PdpStatistics());
        }).hasMessage("pdpInstanceId is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", "version", "TYPE", "inst", null);
        }).hasMessage("pdpStatistics is marked @NonNull but is null");

        new PdpProvider().updatePdpStatistics(pfDao, "name", "version", "TYPE", "inst", new PdpStatistics());
    }
}
