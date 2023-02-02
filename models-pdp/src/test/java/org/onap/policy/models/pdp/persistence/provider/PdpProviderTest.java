/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021,2023 Nordix Foundation.
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

package org.onap.policy.models.pdp.persistence.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.pdp.concepts.Pdp;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.pdp.concepts.PdpGroups;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus.PdpPolicyStatusBuilder;
import org.onap.policy.models.pdp.concepts.PdpPolicyStatus.State;
import org.onap.policy.models.pdp.concepts.PdpStatistics;
import org.onap.policy.models.pdp.concepts.PdpSubGroup;
import org.onap.policy.models.pdp.enums.PdpHealthStatus;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifierOptVersion;
import org.onap.policy.models.tosca.simple.provider.SimpleToscaProvider;

/**
 * Test the {@link SimpleToscaProvider} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class PdpProviderTest {
    private static final String PDP_GROUPS0_JSON = "testdata/PdpGroups0.json";
    private static final String PDP_TYPE_IS_NULL = "pdpType is marked .*ull but is null";
    private static final String SUBGROUP_IS_NULL = "pdpSubGroup is marked .*ull but is null";
    private static final String GROUP_IS_NULL = "pdpGroupName is marked .*ull but is null";
    private static final String DAO_IS_NULL = "dao is marked .*ull but is null";
    private static final String PDP_GROUP0 = "PdpGroup0";
    private static final String GROUP_A = "groupA";
    private static final String GROUP_B = "groupB";
    private static final ToscaConceptIdentifier MY_POLICY = new ToscaConceptIdentifier("MyPolicy", "1.2.3");
    private static final ToscaConceptIdentifier MY_POLICY2 = new ToscaConceptIdentifier("MyPolicyB", "2.3.4");

    private PfDao pfDao;
    private StandardCoder standardCoder;
    private PdpPolicyStatusBuilder statusBuilder;

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
        jdbcProperties.setProperty("javax.persistence.jdbc.user", "policy");
        jdbcProperties.setProperty("javax.persistence.jdbc.password", "P01icY");

        if (System.getProperty("USE-MARIADB") != null) {
            jdbcProperties.setProperty("javax.persistence.jdbc.driver", "org.mariadb.jdbc.Driver");
            jdbcProperties.setProperty("javax.persistence.jdbc.url", "jdbc:mariadb://localhost:3306/policy");
        } else {
            jdbcProperties.setProperty("javax.persistence.jdbc.driver", "org.h2.Driver");
            jdbcProperties.setProperty("javax.persistence.jdbc.url", "jdbc:h2:mem:PdpProviderTest");
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

    /**
     * Set up Policy Status builder.
     */
    @Before
    public void setupBuilder() {
        ToscaConceptIdentifier policyType = new ToscaConceptIdentifier("MyPolicyType", "1.2.4");

        statusBuilder = PdpPolicyStatus.builder().deploy(true).pdpType("MyPdpType").policy(MY_POLICY)
            .policyType(policyType).state(State.SUCCESS);
    }

    @After
    public void teardown() {
        pfDao.close();
    }

    @Test
    public void testGroupsGet() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().getPdpGroups(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().getPdpGroups(null, "name");
        }).hasMessageMatching(DAO_IS_NULL);

        String originalJson = ResourceUtils.getResourceAsString(PDP_GROUPS0_JSON);
        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        PdpGroups createdPdpGroups0 = new PdpGroups();
        createdPdpGroups0.setGroups(new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()));
        String createdJson = standardCoder.encode(createdPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), createdJson.replaceAll("\\s+", ""));

        PdpGroups gotPdpGroups0 = new PdpGroups();
        gotPdpGroups0.setGroups(new PdpProvider().getPdpGroups(pfDao, PDP_GROUP0));

        String gotJson = standardCoder.encode(gotPdpGroups0);

        assertEquals(originalJson.replaceAll("\\s+", ""), gotJson.replaceAll("\\s+", ""));
    }

    @Test
    public void testFilteredPdpGroupGet() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().getFilteredPdpGroups(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().getFilteredPdpGroups(null, PdpGroupFilter.builder().build());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().getFilteredPdpGroups(pfDao, null);
        }).hasMessageMatching("filter is marked .*ull but is null");

        String originalJson = ResourceUtils.getResourceAsString("testdata/PdpGroupsForFiltering.json");
        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        assertEquals(5, new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()).size());

        List<ToscaConceptIdentifier> policyTypeList = new ArrayList<>();
        policyTypeList.add(new ToscaConceptIdentifier("policy.type.0", "1.2.3"));

        List<ToscaConceptIdentifier> policyList = new ArrayList<>();
        policyList.add(new ToscaConceptIdentifier("Policy0", "4.5.6"));

        // @formatter:off
        final PdpGroupFilter filter = PdpGroupFilter.builder()
                .groupState(PdpState.PASSIVE)
                .name(PDP_GROUP0)
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
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().createPdpGroups(null, new ArrayList<>());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().createPdpGroups(pfDao, null);
        }).hasMessageMatching("pdpGroups is marked .*ull but is null");

        String originalJson = ResourceUtils.getResourceAsString(PDP_GROUPS0_JSON);
        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        PdpGroups createdPdpGroups0 = new PdpGroups();
        createdPdpGroups0.setGroups(new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()));
        String createdJson = standardCoder.encode(createdPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), createdJson.replaceAll("\\s+", ""));

        PdpGroups gotPdpGroups0 = new PdpGroups();
        gotPdpGroups0.setGroups(new PdpProvider().getPdpGroups(pfDao, PDP_GROUP0));

        String gotJson = standardCoder.encode(gotPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), gotJson.replaceAll("\\s+", ""));

        pdpGroups0.getGroups().get(0).setPdpGroupState(null);
        assertThatThrownBy(() -> {
            new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups());
        }).hasMessageContaining("PDP group").hasMessageContaining("pdpGroupState")
            .hasMessageContaining(Validated.IS_NULL);
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
        gotPdpGroups0.setGroups(new PdpProvider().getPdpGroups(pfDao, "TestPdpGroup"));

        String gotJson = standardCoder.encode(gotPdpGroups0);
        assertEquals(originalTweakedJson.replaceAll("\\s+", ""), gotJson.replaceAll("\\s+", ""));
    }

    @Test
    public void testGroupsUpdate() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpGroups(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpGroups(null, new ArrayList<>());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpGroups(pfDao, null);
        }).hasMessageMatching("pdpGroups is marked .*ull but is null");

        String originalJson = ResourceUtils.getResourceAsString(PDP_GROUPS0_JSON);
        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        PdpGroups createdPdpGroups0 = new PdpGroups();
        createdPdpGroups0.setGroups(new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()));
        String createdJson = standardCoder.encode(createdPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), createdJson.replaceAll("\\s+", ""));

        PdpGroups gotPdpGroups0 = new PdpGroups();
        gotPdpGroups0.setGroups(new PdpProvider().getPdpGroups(pfDao, PDP_GROUP0));

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
        }).hasMessageContaining("PDP group").hasMessageContaining("pdpGroupState")
            .hasMessageContaining(Validated.IS_NULL);
    }

    @Test
    public void testPoliciesDelete() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(null, "name");
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(pfDao, null);
        }).hasMessageMatching("name is marked .*ull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(pfDao, "name");
        }).hasMessage("delete of PDP group \"name:0.0.0\" failed, PDP group does not exist");

        String originalJson = ResourceUtils.getResourceAsString(PDP_GROUPS0_JSON);
        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        PdpGroups createdPdpGroups0 = new PdpGroups();
        createdPdpGroups0.setGroups(new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()));
        String createdJson = standardCoder.encode(createdPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), createdJson.replaceAll("\\s+", ""));

        PdpGroups gotPdpGroups0 = new PdpGroups();
        gotPdpGroups0.setGroups(new PdpProvider().getPdpGroups(pfDao, PDP_GROUP0));

        String gotJson = standardCoder.encode(gotPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), gotJson.replaceAll("\\s+", ""));

        PdpGroup deletedPdpGroup = new PdpProvider().deletePdpGroup(pfDao, PDP_GROUP0);

        assertEquals(createdPdpGroups0.getGroups().get(0), deletedPdpGroup);

        assertEquals(0, new PdpProvider().getPdpGroups(pfDao, PDP_GROUP0).size());

        assertThatThrownBy(() -> {
            new PdpProvider().deletePdpGroup(pfDao, PDP_GROUP0);
        }).hasMessage("delete of PDP group \"PdpGroup0:0.0.0\" failed, PDP group does not exist");
    }

    @Test
    public void testPdpSubgroupUpdate() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, null, new PdpSubGroup());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, "name", null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(null, "name", new PdpSubGroup());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, null, null);
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, null, new PdpSubGroup());
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, "name", null);
        }).hasMessageMatching(SUBGROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, "name", new PdpSubGroup());
        }).hasMessage("parameter \"localName\" is null");

        String originalJson = ResourceUtils.getResourceAsString(PDP_GROUPS0_JSON);
        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        PdpGroups createdPdpGroups0 = new PdpGroups();
        createdPdpGroups0.setGroups(new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()));
        String createdJson = standardCoder.encode(createdPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), createdJson.replaceAll("\\s+", ""));

        PdpGroups gotPdpGroups0 = new PdpGroups();
        gotPdpGroups0.setGroups(new PdpProvider().getPdpGroups(pfDao, PDP_GROUP0));

        String gotJson = standardCoder.encode(gotPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), gotJson.replaceAll("\\s+", ""));

        PdpSubGroup existingSubGroup = gotPdpGroups0.getGroups().get(0).getPdpSubgroups().get(0);
        existingSubGroup.setCurrentInstanceCount(10);
        existingSubGroup.setDesiredInstanceCount(10);
        new PdpProvider().updatePdpSubGroup(pfDao, PDP_GROUP0, existingSubGroup);

        List<PdpGroup> afterUpdatePdpGroups = new PdpProvider().getPdpGroups(pfDao, PDP_GROUP0);
        assertEquals(10, afterUpdatePdpGroups.get(0).getPdpSubgroups().get(0).getCurrentInstanceCount());
        assertEquals(10, afterUpdatePdpGroups.get(0).getPdpSubgroups().get(0).getDesiredInstanceCount());

        existingSubGroup.setDesiredInstanceCount(-1);
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpSubGroup(pfDao, PDP_GROUP0, existingSubGroup);
        }).hasMessageContaining("PDP sub group").hasMessageContaining("desiredInstanceCount")
            .hasMessageContaining("below the minimum value");
        existingSubGroup.setDesiredInstanceCount(10);
    }

    @Test
    public void testPdpUpdate() throws Exception {
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, null, new Pdp());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, "TYPE", null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, null, "TYPE", new Pdp());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", null, new Pdp());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", "TYPE", null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(null, "name", "TYPE", new Pdp());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, null, null);
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, null, new Pdp());
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, "TYPE", null);
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, null, "TYPE", new Pdp());
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", null, null);
        }).hasMessageMatching(SUBGROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", null, new Pdp());
        }).hasMessageMatching(SUBGROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", "TYPE", null);
        }).hasMessageMatching("pdp is marked .*ull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, "name", "TYPE", new Pdp());
        }).hasMessage("parameter \"localName\" is null");

        String originalJson = ResourceUtils.getResourceAsString(PDP_GROUPS0_JSON);
        PdpGroups pdpGroups0 = standardCoder.decode(originalJson, PdpGroups.class);

        PdpGroups createdPdpGroups0 = new PdpGroups();
        createdPdpGroups0.setGroups(new PdpProvider().createPdpGroups(pfDao, pdpGroups0.getGroups()));
        String createdJson = standardCoder.encode(createdPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), createdJson.replaceAll("\\s+", ""));

        PdpGroups gotPdpGroups0 = new PdpGroups();
        gotPdpGroups0.setGroups(new PdpProvider().getPdpGroups(pfDao, PDP_GROUP0));

        String gotJson = standardCoder.encode(gotPdpGroups0);
        assertEquals(originalJson.replaceAll("\\s+", ""), gotJson.replaceAll("\\s+", ""));

        Pdp existingPdp = gotPdpGroups0.getGroups().get(0).getPdpSubgroups().get(0).getPdpInstances().get(0);
        existingPdp.setPdpState(PdpState.TEST);
        existingPdp.setHealthy(PdpHealthStatus.TEST_IN_PROGRESS);
        new PdpProvider().updatePdp(pfDao, PDP_GROUP0, "APEX", existingPdp);

        List<PdpGroup> afterUpdatePdpGroups = new PdpProvider().getPdpGroups(pfDao, PDP_GROUP0);
        assertEquals(PdpState.TEST,
            afterUpdatePdpGroups.get(0).getPdpSubgroups().get(0).getPdpInstances().get(0).getPdpState());
        assertEquals(PdpHealthStatus.TEST_IN_PROGRESS,
            afterUpdatePdpGroups.get(0).getPdpSubgroups().get(0).getPdpInstances().get(0).getHealthy());

        existingPdp.setMessage("");
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdp(pfDao, PDP_GROUP0, "APEX", existingPdp);
        }).hasMessageContaining("PDP").hasMessageContaining("message").hasMessageContaining(Validated.IS_BLANK);
        existingPdp.setMessage("A Message");
    }

    @Test
    public void testGetPdpStatistics() throws PfModelException {
        assertThatThrownBy(() -> {
            new PdpProvider().getPdpStatistics(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().getPdpStatistics(null, "name");
        }).hasMessageMatching(DAO_IS_NULL);

        assertEquals(0, new PdpProvider().getPdpStatistics(pfDao, "name").size());
    }

    @Test
    public void testUpdatePdpStatisticsDao() throws PfModelException {
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, null, new PdpStatistics());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, "inst", null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, null, "inst", new PdpStatistics());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "TYPE", null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "TYPE", null, new PdpStatistics());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "TYPE", "inst", null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, null, "TYPE", "inst", new PdpStatistics());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, null, new PdpStatistics());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, "inst", null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", null, "inst", new PdpStatistics());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "TYPE", null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "TYPE", null, new PdpStatistics());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "TYPE", "inst", null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(null, "name", "TYPE", "inst", new PdpStatistics());
        }).hasMessageMatching(DAO_IS_NULL);
    }

    @Test
    public void testUpdatePdpStatisticsGroup() throws PfModelException {
        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, null, null);
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, null, new PdpStatistics());
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, "inst", null);
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, null, "inst", new PdpStatistics());
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "TYPE", null, null);
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "TYPE", null, new PdpStatistics());
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "TYPE", "inst", null);
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, null, "TYPE", "inst", new PdpStatistics());
        }).hasMessageMatching(GROUP_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, null, null);
        }).hasMessageMatching(PDP_TYPE_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, null, new PdpStatistics());
        }).hasMessageMatching(PDP_TYPE_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, "inst", null);
        }).hasMessageMatching(PDP_TYPE_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", null, "inst", new PdpStatistics());
        }).hasMessageMatching(PDP_TYPE_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", "TYPE", null, null);
        }).hasMessageMatching("pdpInstanceId is marked .*ull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", "TYPE", null, new PdpStatistics());
        }).hasMessageMatching("pdpInstanceId is marked .*ull but is null");

        assertThatThrownBy(() -> {
            new PdpProvider().updatePdpStatistics(pfDao, "name", "TYPE", "inst", null);
        }).hasMessageMatching("pdpStatistics is marked .*ull but is null");

        new PdpProvider().updatePdpStatistics(pfDao, "name", "TYPE", "inst", new PdpStatistics());
    }

    @Test
    public void testGetAllPolicyStatusPfDao() throws PfModelException {
        assertThatThrownBy(() -> {
            new PdpProvider().getAllPolicyStatus(null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThat(new PdpProvider().getAllPolicyStatus(pfDao)).isEmpty();

        PdpProvider provider = loadDeployments();
        assertThat(provider.getAllPolicyStatus(pfDao)).hasSize(5);
    }

    private PdpProvider loadDeployments() {
        PdpProvider provider = new PdpProvider();

        // same name, different version
        final ToscaConceptIdentifier policy3 = new ToscaConceptIdentifier(MY_POLICY.getName(), "10.20.30");

        PdpPolicyStatus id1 = statusBuilder.pdpGroup(GROUP_A).pdpId("pdp1").policy(MY_POLICY).build();
        PdpPolicyStatus id2 = statusBuilder.pdpGroup(GROUP_A).pdpId("pdp2").policy(MY_POLICY2).build();
        PdpPolicyStatus id3 = statusBuilder.pdpGroup(GROUP_A).pdpId("pdp3").policy(policy3).build();
        PdpPolicyStatus id4 = statusBuilder.pdpGroup(GROUP_B).pdpId("pdp4").policy(MY_POLICY).build();
        PdpPolicyStatus id5 = statusBuilder.pdpGroup(GROUP_B).pdpId("pdp5").policy(MY_POLICY2).build();
        provider.cudPolicyStatus(pfDao, List.of(id1, id2, id3, id4, id5), null, null);

        return provider;
    }

    @Test
    public void testGetAllPolicyStatusPfDaoToscaConceptIdentifierOptVersion() throws PfModelException {
        assertThatThrownBy(() -> {
            new PdpProvider().getAllPolicyStatus(null, new ToscaConceptIdentifierOptVersion("somePdp", null));
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().getAllPolicyStatus(pfDao, null);
        }).hasMessageContaining("policy").hasMessageContaining("null");

        assertThat(new PdpProvider().getAllPolicyStatus(pfDao, new ToscaConceptIdentifierOptVersion("somePdp", null)))
            .isEmpty();

        PdpProvider provider = loadDeployments();
        assertThat(provider.getAllPolicyStatus(pfDao, new ToscaConceptIdentifierOptVersion(MY_POLICY))).hasSize(2);
        assertThat(provider.getAllPolicyStatus(pfDao, new ToscaConceptIdentifierOptVersion(MY_POLICY.getName(), null)))
            .hasSize(3);
    }

    @Test
    public void testGetGroupPolicyStatus() throws PfModelException {
        assertThatThrownBy(() -> {
            new PdpProvider().getGroupPolicyStatus(null, "someGroup");
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpProvider().getGroupPolicyStatus(pfDao, null);
        }).hasMessageContaining("group").hasMessageContaining("null");

        assertThat(new PdpProvider().getGroupPolicyStatus(pfDao, PDP_GROUP0)).isEmpty();

        PdpProvider provider = loadDeployments();
        assertThat(provider.getGroupPolicyStatus(pfDao, GROUP_A)).hasSize(3);
    }

    @Test
    public void cudPolicyStatus() throws PfModelException {
        PdpProvider prov = new PdpProvider();

        assertThatThrownBy(() -> prov.cudPolicyStatus(null, List.of(), List.of(), List.of()))
            .hasMessageMatching(DAO_IS_NULL);

        // null collections should be OK
        assertThatCode(() -> prov.cudPolicyStatus(pfDao, null, null, null)).doesNotThrowAnyException();
    }

    @Test
    public void cudPolicyStatus_Create() throws PfModelException {
        PdpProvider prov = new PdpProvider();

        PdpPolicyStatus idx = statusBuilder.pdpGroup(GROUP_A).pdpId("idX").build();
        PdpPolicyStatus idy = statusBuilder.pdpGroup(GROUP_A).pdpId("idY").build();
        PdpPolicyStatus idz = statusBuilder.pdpGroup(GROUP_B).pdpId("idZ").build();
        prov.cudPolicyStatus(pfDao, List.of(idx, idy), null, null);
        prov.cudPolicyStatus(pfDao, List.of(idz), null, null);

        List<PdpPolicyStatus> records = prov.getGroupPolicyStatus(pfDao, GROUP_A);
        assertThat(records).hasSize(2);

        Collections.sort(records, (rec1, rec2) -> rec1.getPdpId().compareTo(rec2.getPdpId()));
        assertThat(records.get(0)).isEqualTo(idx);
        assertThat(records.get(1)).isEqualTo(idy);

        records = prov.getGroupPolicyStatus(pfDao, GROUP_B);
        assertThat(records).hasSize(1);
        assertThat(records.get(0)).isEqualTo(idz);
    }

    @Test
    public void cudPolicyStatus_Update() throws PfModelException {
        PdpProvider prov = new PdpProvider();

        PdpPolicyStatus idw = statusBuilder.pdpGroup(GROUP_A).pdpId("wId").build();
        PdpPolicyStatus idx = statusBuilder.pdpGroup(GROUP_A).pdpId("xId").build();
        PdpPolicyStatus idy = statusBuilder.pdpGroup(GROUP_A).pdpId("yId").build();
        PdpPolicyStatus idz = statusBuilder.pdpGroup(GROUP_A).pdpId("zId").build();
        prov.cudPolicyStatus(pfDao, List.of(idw, idx, idy, idz), null, null);

        assertThat(prov.getGroupPolicyStatus(pfDao, GROUP_A)).hasSize(4);

        /*
         * Now update some records.
         */
        idx.setState(State.FAILURE);
        idz.setState(State.WAITING);
        prov.cudPolicyStatus(pfDao, null, List.of(idx, idz), null);
        List<PdpPolicyStatus> records = prov.getGroupPolicyStatus(pfDao, GROUP_A);
        assertThat(records).hasSize(4);

        Collections.sort(records, (rec1, rec2) -> rec1.getPdpId().compareTo(rec2.getPdpId()));
        assertThat(records.get(0)).isEqualTo(idw);
        assertThat(records.get(1)).isEqualTo(idx);
        assertThat(records.get(2)).isEqualTo(idy);
        assertThat(records.get(3)).isEqualTo(idz);
    }

    @Test
    public void cudPolicyStatus_Delete() throws PfModelException {
        PdpProvider prov = new PdpProvider();

        PdpPolicyStatus idw = statusBuilder.pdpGroup(GROUP_A).pdpId("idW").build();
        PdpPolicyStatus idx = statusBuilder.pdpGroup(GROUP_A).pdpId("idX").build();
        PdpPolicyStatus idy = statusBuilder.pdpGroup(GROUP_A).pdpId("idY").build();
        PdpPolicyStatus idz = statusBuilder.pdpGroup(GROUP_A).pdpId("idZ").build();
        prov.cudPolicyStatus(pfDao, List.of(idw, idx, idy, idz), null, null);

        assertThat(prov.getGroupPolicyStatus(pfDao, GROUP_A)).hasSize(4);

        /*
         * Delete some records and then check again.
         */
        prov.cudPolicyStatus(pfDao, null, null, List.of(idw, idy));

        List<PdpPolicyStatus> records = prov.getGroupPolicyStatus(pfDao, GROUP_A);
        assertThat(records).hasSize(2);

        Collections.sort(records, (rec1, rec2) -> rec1.getPdpId().compareTo(rec2.getPdpId()));
        assertThat(records.get(0)).isEqualTo(idx);
        assertThat(records.get(1)).isEqualTo(idz);
    }

    @Test
    public void testFromAuthorativeStatus() throws PfModelException {
        PdpProvider prov = new PdpProvider();

        assertThatCode(() -> prov.cudPolicyStatus(pfDao, null, null, null)).doesNotThrowAnyException();

        PdpPolicyStatus ida = statusBuilder.pdpGroup(GROUP_A).pdpId("idA").build();
        PdpPolicyStatus idb = statusBuilder.pdpGroup(GROUP_A).pdpId("idB").build();
        PdpPolicyStatus idc = statusBuilder.pdpGroup(GROUP_A).pdpId("idC").build();
        PdpPolicyStatus idd = statusBuilder.pdpGroup(GROUP_A).pdpId("idD").build();

        // make a couple invalid records
        idb.setState(null);
        idd.setState(null);

        List<PdpPolicyStatus> list = List.of(ida, idb, idc, idd);

        // @formatter:off
        assertThatCode(() -> prov.cudPolicyStatus(pfDao, list, null, null))
            .isInstanceOf(PfModelRuntimeException.class)
            .hasMessageContaining("1").hasMessageContaining("3")
            .hasMessageNotContaining("0").hasMessageNotContaining("2");
        // @formatter:on

        assertThat(prov.getGroupPolicyStatus(pfDao, GROUP_A)).isEmpty();
    }
}
