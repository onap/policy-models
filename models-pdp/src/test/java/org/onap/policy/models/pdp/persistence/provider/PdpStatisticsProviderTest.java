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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
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

    private PfDao pfDao;

    private List<PdpEngineWorkerStatistics> engineStats = new ArrayList<>();
    private PdpStatistics pdpStatistics11;
    private PdpStatistics pdpStatistics12;
    private PdpStatistics pdpStatistics22;
    private PdpStatistics pdpStatistics31;

    // checkstyle complained about this as a local variable; had to make it a field
    private long genId;

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

        genId = 1;

        pdpStatistics11 = new PdpStatistics();
        pdpStatistics11.setPdpInstanceId(NAME);
        pdpStatistics11.setTimeStamp(TIMESTAMP1);
        pdpStatistics11.setGeneratedId(genId++);
        pdpStatistics11.setPdpGroupName(GROUP);
        pdpStatistics11.setPdpSubGroupName(SUBGROUP);
        pdpStatistics11.setPolicyDeployCount(2);
        pdpStatistics11.setPolicyDeployFailCount(1);
        pdpStatistics11.setPolicyDeploySuccessCount(1);
        pdpStatistics11.setPolicyExecutedCount(2);
        pdpStatistics11.setPolicyExecutedFailCount(1);
        pdpStatistics11.setPolicyExecutedSuccessCount(1);
        pdpStatistics11.setEngineStats(engineStats);

        pdpStatistics12 = new PdpStatistics();
        pdpStatistics12.setPdpInstanceId(NAME);
        pdpStatistics12.setTimeStamp(TIMESTAMP2);
        pdpStatistics12.setGeneratedId(genId++);
        pdpStatistics12.setPdpGroupName(GROUP);
        pdpStatistics12.setPdpSubGroupName(SUBGROUP);
        pdpStatistics12.setPolicyDeployCount(2);
        pdpStatistics12.setPolicyDeployFailCount(1);
        pdpStatistics12.setPolicyDeploySuccessCount(1);
        pdpStatistics12.setPolicyExecutedCount(2);
        pdpStatistics12.setPolicyExecutedFailCount(1);
        pdpStatistics12.setPolicyExecutedSuccessCount(1);
        pdpStatistics12.setEngineStats(engineStats);

        pdpStatistics22 = new PdpStatistics();
        pdpStatistics22.setPdpInstanceId("name2");
        pdpStatistics22.setTimeStamp(TIMESTAMP2);
        pdpStatistics22.setGeneratedId(genId++);
        pdpStatistics22.setPdpGroupName(GROUP);
        pdpStatistics22.setPdpSubGroupName(SUBGROUP);
        pdpStatistics22.setPolicyDeployCount(2);
        pdpStatistics22.setPolicyDeployFailCount(1);
        pdpStatistics22.setPolicyDeploySuccessCount(1);
        pdpStatistics22.setPolicyExecutedCount(2);
        pdpStatistics22.setPolicyExecutedFailCount(1);
        pdpStatistics22.setPolicyExecutedSuccessCount(1);
        pdpStatistics22.setEngineStats(engineStats);

        pdpStatistics31 = new PdpStatistics();
        pdpStatistics31.setPdpInstanceId("name3");
        pdpStatistics31.setTimeStamp(TIMESTAMP1);
        pdpStatistics31.setGeneratedId(genId++);
        pdpStatistics31.setPdpGroupName(GROUP);
        pdpStatistics31.setPdpSubGroupName(SUBGROUP);
        pdpStatistics31.setPolicyDeployCount(2);
        pdpStatistics31.setPolicyDeployFailCount(1);
        pdpStatistics31.setPolicyDeploySuccessCount(1);
        pdpStatistics31.setPolicyExecutedCount(2);
        pdpStatistics31.setPolicyExecutedFailCount(1);
        pdpStatistics31.setPolicyExecutedSuccessCount(1);
        pdpStatistics31.setEngineStats(engineStats);

        List<PdpStatistics> create = List.of(pdpStatistics11, pdpStatistics22, pdpStatistics31, pdpStatistics12);
        List<PdpStatistics> createdPdpStatisticsList = new PdpStatisticsProvider().createPdpStatistics(pfDao, create);

        // these should match AND be in the same order
        assertThat(createdPdpStatisticsList).isEqualTo(create);
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
    public void testGetFilteredPdpStatistics() throws Exception {

        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().getFilteredPdpStatistics(null, PdpFilterParameters.builder().build());
        }).hasMessageMatching(DAO_IS_NULL);

        List<PdpStatistics> getPdpStatisticsList;

        // match on name - returns multiple records
        getPdpStatisticsList = new PdpStatisticsProvider().getFilteredPdpStatistics(pfDao, PdpFilterParameters
                        .builder().name(NAME).group(GROUP).startTime(TIMESTAMP1).endTime(TIMESTAMP2).build());
        verifyEquals(getPdpStatisticsList, List.of(pdpStatistics11, pdpStatistics12));

        // this name only has one record
        getPdpStatisticsList = new PdpStatisticsProvider().getFilteredPdpStatistics(pfDao, PdpFilterParameters
                        .builder().name("name2").group(GROUP).startTime(TIMESTAMP1).endTime(TIMESTAMP2).build());
        verifyEquals(getPdpStatisticsList, List.of(pdpStatistics22));

        // match on subgroup
        getPdpStatisticsList = new PdpStatisticsProvider().getFilteredPdpStatistics(pfDao,
                        PdpFilterParameters.builder().name("name2").group(GROUP).subGroup(SUBGROUP)
                                        .startTime(TIMESTAMP1).endTime(TIMESTAMP2).build());
        verifyEquals(getPdpStatisticsList, List.of(pdpStatistics22));

        // only request one record
        getPdpStatisticsList = new PdpStatisticsProvider().getFilteredPdpStatistics(pfDao, PdpFilterParameters
                        .builder().name(NAME).recordNum(1).build());
        verifyEquals(getPdpStatisticsList, List.of(pdpStatistics12));

        // request too many records
        getPdpStatisticsList = new PdpStatisticsProvider().getFilteredPdpStatistics(pfDao, PdpFilterParameters
                        .builder().name(NAME).recordNum(10000).build());
        verifyEquals(getPdpStatisticsList, List.of(pdpStatistics11, pdpStatistics12));

        // group mismatch
        getPdpStatisticsList = new PdpStatisticsProvider().getFilteredPdpStatistics(pfDao, PdpFilterParameters
                        .builder().name(NAME).group(GROUP0).startTime(TIMESTAMP1).endTime(TIMESTAMP2).build());
        assertThat(getPdpStatisticsList).isEmpty();

        // subgroup mismatch
        getPdpStatisticsList = new PdpStatisticsProvider().getFilteredPdpStatistics(pfDao,
                        PdpFilterParameters.builder().name("name2").group(GROUP).subGroup("subgroup2")
                                        .startTime(TIMESTAMP1).endTime(TIMESTAMP2).build());
        assertThat(getPdpStatisticsList).isEmpty();
    }

    @Test
    public void testUpdatePdpStatistics() throws Exception {
        assertThatThrownBy(() -> {
            new PdpStatisticsProvider().updatePdpStatistics(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        pdpStatistics11.setPdpGroupName(GROUP0);
        List<PdpStatistics> update = List.of(pdpStatistics11);
        List<PdpStatistics> updatePdpStatisticsList = new PdpStatisticsProvider().updatePdpStatistics(pfDao, update);

        // these should match AND be in the same order
        assertThat(updatePdpStatisticsList).isEqualTo(update);
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
        verifyEquals(deletedPdpStatisticsList, List.of(pdpStatistics12, pdpStatistics11));
    }

    private void verifyEquals(List<PdpStatistics> list1, List<PdpStatistics> list2) {
        assertThat(sort(list1)).isEqualTo(sort(list2));
    }

    private List<PdpStatistics> sort(List<PdpStatistics> list1) {
        List<PdpStatistics> list2 = new ArrayList<>(list1);
        Collections.sort(list2, (stat1, stat2) -> stat1.getGeneratedId().compareTo(stat2.getGeneratedId()));

        return list2;
    }
}
