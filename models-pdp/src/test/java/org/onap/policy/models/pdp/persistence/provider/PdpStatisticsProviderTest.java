/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2020-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.base.Validated;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.pdp.concepts.PdpEngineWorkerStatistics;
import org.onap.policy.models.pdp.concepts.PdpStatistics;

public class PdpStatisticsProviderTest {
    private static final String DAO_IS_NULL = "dao is marked .*ull but is null";
    private static final String LIST_IS_NULL = "pdpStatisticsList is marked .*ull but is null";
    private static final String GROUP0 = "group0";
    private static final String NAME = "name";
    private static final String GROUP = "group";
    private static final String SUBGROUP = "subgroup";
    private static final Instant TIMESTAMP1 = Instant.ofEpochSecond(1078884319);
    private static final Instant TIMESTAMP2 = Instant.ofEpochSecond(1078884350);
    private static final Long GENERATEDID1 = 1L;
    private static final Long GENERATEDID2 = 2L;

    private PfDao pfDao;

    private ArrayList<PdpStatistics> pdpStatisticsTestList = new ArrayList<>();
    private List<PdpEngineWorkerStatistics> engineStats = new ArrayList<>();
    private String testListStr;
    private String name1ListStr;
    private String createdListStr;

    /**
     * Set up test Dao.
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
            jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:mem:PdpStatisticsProviderTest");
        }

        daoParameters.setJdbcProperties(jdbcProperties);

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);

        PdpStatistics pdpStatistics = new PdpStatistics();
        pdpStatistics.setPdpInstanceId(NAME);
        pdpStatistics.setTimeStamp(TIMESTAMP1);
        pdpStatistics.setGeneratedId(GENERATEDID1);
        pdpStatistics.setPdpGroupName(GROUP);
        pdpStatistics.setPdpSubGroupName(SUBGROUP);
        pdpStatistics.setPolicyDeployCount(2);
        pdpStatistics.setPolicyDeployFailCount(1);
        pdpStatistics.setPolicyDeploySuccessCount(1);
        pdpStatistics.setPolicyExecutedCount(2);
        pdpStatistics.setPolicyExecutedFailCount(1);
        pdpStatistics.setPolicyExecutedSuccessCount(1);
        pdpStatistics.setEngineStats(engineStats);
        pdpStatisticsTestList.add(pdpStatistics);
        name1ListStr = pdpStatisticsTestList.toString();

        PdpStatistics pdpStatistics2 = new PdpStatistics();
        pdpStatistics2.setPdpInstanceId("name2");
        pdpStatistics2.setTimeStamp(TIMESTAMP2);
        pdpStatistics2.setGeneratedId(GENERATEDID2);
        pdpStatistics2.setPdpGroupName(GROUP);
        pdpStatistics2.setPdpSubGroupName(SUBGROUP);
        pdpStatistics2.setPolicyDeployCount(2);
        pdpStatistics2.setPolicyDeployFailCount(1);
        pdpStatistics2.setPolicyDeploySuccessCount(1);
        pdpStatistics2.setPolicyExecutedCount(2);
        pdpStatistics2.setPolicyExecutedFailCount(1);
        pdpStatistics2.setPolicyExecutedSuccessCount(1);
        pdpStatistics2.setEngineStats(engineStats);
        pdpStatisticsTestList.add(pdpStatistics2);
        testListStr = pdpStatisticsTestList.toString();

        List<PdpStatistics> createdPdpStatisticsList;
        createdPdpStatisticsList = new PdpStatisticsProvider().createPdpStatistics(pfDao, pdpStatisticsTestList);
        createdListStr = createdPdpStatisticsList.toString();
        assertEquals(createdListStr.replaceAll("\\s+", ""), testListStr.replaceAll("\\s+", ""));
    }

    @After
    public void teardown() {
        pfDao.close();
    }

    @Test
    public void testNotOkPdpStatistics() {
        PdpStatistics pdpStatisticsErr = new PdpStatistics();
        pdpStatisticsErr.setPdpInstanceId("NULL");
        pdpStatisticsErr.setPdpGroupName(GROUP);
        ArrayList<PdpStatistics> pdpStatisticsNullList = new ArrayList<>();
        pdpStatisticsNullList.add(pdpStatisticsErr);

        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().createPdpStatistics(pfDao, null);
        }).hasMessageMatching(LIST_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().updatePdpStatistics(pfDao, null);
        }).hasMessageMatching(LIST_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().createPdpStatistics(pfDao, pdpStatisticsNullList);
        }).hasMessageContaining("pdp statistics").hasMessageContaining("key")
                        .hasMessageContaining(Validated.IS_A_NULL_KEY);

        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().updatePdpStatistics(pfDao, pdpStatisticsNullList);
        }).hasMessageContaining("pdp statistics").hasMessageContaining("key")
                        .hasMessageContaining(Validated.IS_A_NULL_KEY);
    }

    @Test
    public void testGetPdpStatistics() throws Exception {
        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().createPdpStatistics(null, null);
        }).hasMessageMatching(DAO_IS_NULL);
        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().getPdpStatistics(null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        List<PdpStatistics> getPdpStatisticsList;
        getPdpStatisticsList = new PdpStatisticsProvider().getPdpStatistics(pfDao, NAME, TIMESTAMP1);
        assertThat(getPdpStatisticsList).hasSize(1);
        String gotListStr = getPdpStatisticsList.toString();
        assertEquals(name1ListStr.replaceAll("\\s+", ""), gotListStr.replaceAll("\\s+", ""));

        // name is null
        getPdpStatisticsList = new PdpStatisticsProvider().getPdpStatistics(pfDao, null, TIMESTAMP1);
        gotListStr = getPdpStatisticsList.toString();
        assertEquals(testListStr.replaceAll("\\s+", ""), gotListStr.replaceAll("\\s+", ""));
    }

    @Test
    public void testGetFilteredPdpStatistics() throws Exception {

        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().getFilteredPdpStatistics(null, PdpFilterParameters.builder().build());
        }).hasMessageMatching(DAO_IS_NULL);


        List<PdpStatistics> createdPdpStatisticsList;
        createdPdpStatisticsList = new PdpStatisticsProvider().createPdpStatistics(pfDao, pdpStatisticsTestList);
        createdListStr = createdPdpStatisticsList.toString();
        assertEquals(createdListStr.replaceAll("\\s+", ""), testListStr.replaceAll("\\s+", ""));

        List<PdpStatistics> getPdpStatisticsList;
        getPdpStatisticsList = new PdpStatisticsProvider().getFilteredPdpStatistics(pfDao, PdpFilterParameters
                        .builder().name(NAME).group(GROUP).startTime(TIMESTAMP1).endTime(TIMESTAMP2).build());
        assertThat(getPdpStatisticsList).hasSize(1);
        getPdpStatisticsList = new PdpStatisticsProvider().getFilteredPdpStatistics(pfDao, PdpFilterParameters
                        .builder().name("name2").group(GROUP).startTime(TIMESTAMP1).endTime(TIMESTAMP2).build());
        assertThat(getPdpStatisticsList).hasSize(1);
        getPdpStatisticsList = new PdpStatisticsProvider().getFilteredPdpStatistics(pfDao,
                        PdpFilterParameters.builder().name("name2").group(GROUP).subGroup(SUBGROUP)
                                        .startTime(TIMESTAMP1).endTime(TIMESTAMP2).build());
        assertThat(getPdpStatisticsList).hasSize(1);
    }

    @Test
    public void testUpdatePdpStatistics() throws Exception {
        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().updatePdpStatistics(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        pdpStatisticsTestList.get(0).setPdpGroupName(GROUP0);
        testListStr = pdpStatisticsTestList.toString();
        List<PdpStatistics> updatePdpStatisticsList =
                new PdpStatisticsProvider().updatePdpStatistics(pfDao, pdpStatisticsTestList);
        String gotListStr = updatePdpStatisticsList.toString();
        assertEquals(testListStr.replaceAll("\\s+", ""), gotListStr.replaceAll("\\s+", ""));
    }

    @Test
    public void testDeletePdpStatistics() throws Exception {
        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().deletePdpStatistics(null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().deletePdpStatistics(pfDao, null, null);
        }).hasMessageMatching("name is marked .*ull but is null");

        List<PdpStatistics> deletedPdpStatisticsList =
                new PdpStatisticsProvider().deletePdpStatistics(pfDao, NAME, null);
        String gotListStr = deletedPdpStatisticsList.toString();
        assertEquals(name1ListStr.replaceAll("\\s+", ""), gotListStr.replaceAll("\\s+", ""));
    }

}
