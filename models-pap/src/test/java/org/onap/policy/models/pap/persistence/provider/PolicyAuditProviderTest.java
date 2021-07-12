/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Nordix Foundation.
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

package org.onap.policy.models.pap.persistence.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.pap.concepts.PolicyAudit;
import org.onap.policy.models.pap.concepts.PolicyAudit.AuditAction;
import org.onap.policy.models.pap.persistence.provider.PolicyAuditProvider.AuditFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * Class for unit testing {@link PolicyAuditProvider}.
 *
 * @author Adheli Tavares (adheli.tavares@est.tech)
 *
 */
public class PolicyAuditProviderTest {

    private static final String FIELD_IS_NULL = "%s is marked .*ull but is null";
    private static final String GROUP_A = "groupA";
    private static final String GROUP_B = "groupB";
    private static final ToscaConceptIdentifier MY_POLICY = new ToscaConceptIdentifier("MyPolicy", "1.2.3");
    private static final ToscaConceptIdentifier MY_POLICY2 = new ToscaConceptIdentifier("MyPolicyB", "2.3.4");
    private static final Integer NUMBER_RECORDS = 10;

    private PfDao pfDao;

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
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_USER, "policy");
        jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_PASSWORD, "P01icY");

        if (System.getProperty("USE-MARIADB") != null) {
            jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_DRIVER, "org.mariadb.jdbc.Driver");
            jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL, "jdbc:mariadb://localhost:3306/policy");
        } else {
            jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_DRIVER, "org.h2.Driver");
            jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:mem:PolicyAuditProviderTest");
        }

        daoParameters.setJdbcProperties(jdbcProperties);

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);
    }

    @After
    public void teardown() {
        pfDao.close();
    }

    @Test
    public void testCreatePolicyAudit() {
        PolicyAuditProvider provider = new PolicyAuditProvider();

        Instant date = Instant.now();
        provider.createAuditRecords(pfDao, generatePolicyAudits(date, GROUP_A, MY_POLICY));

        List<PolicyAudit> records =
                provider.getAuditRecords(pfDao, AuditFilter.builder().recordNum(NUMBER_RECORDS).build());
        assertThat(records).hasSize(2);

        // as the start date is 10 min ahead of first record, shouldn't return any records
        List<PolicyAudit> emptyList = provider.getAuditRecords(pfDao,
                AuditFilter.builder().fromDate(Instant.now().plusSeconds(600)).recordNum(600).build());
        assertThat(emptyList).isEmpty();
    }

    @Test
    public void testCreatePolicyAuditInvalid() {
        PolicyAuditProvider provider = new PolicyAuditProvider();

        List<PolicyAudit> audits = List.of(PolicyAudit.builder().pdpType("pdpType").action(AuditAction.DEPLOYMENT)
                .timestamp(Instant.now()).build());

        assertThrows(PfModelRuntimeException.class, () -> provider.createAuditRecords(pfDao, audits));

        List<PolicyAudit> records =
                provider.getAuditRecords(pfDao, AuditFilter.builder().recordNum(NUMBER_RECORDS).build());
        assertThat(records).isEmpty();
    }

    @Test
    public void testFilters() {
        PolicyAuditProvider provider = new PolicyAuditProvider();

        Instant date = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        provider.createAuditRecords(pfDao, generatePolicyAudits(date, GROUP_A, MY_POLICY));
        provider.createAuditRecords(pfDao, generatePolicyAudits(date, GROUP_B, MY_POLICY));
        provider.createAuditRecords(pfDao, generatePolicyAudits(date, GROUP_B, MY_POLICY2));
        Awaitility.await().pollDelay(3, TimeUnit.SECONDS).until(() -> {
            return true;
        });

        List<PolicyAudit> records = provider.getAuditRecords(pfDao,
                AuditFilter.builder().fromDate(date).toDate(Instant.now()).recordNum(NUMBER_RECORDS).build());
        assertThat(records).hasSize(6);

        List<PolicyAudit> recordsWithGroupB = provider.getAuditRecords(pfDao,
                AuditFilter.builder().pdpGroup(GROUP_B).recordNum(NUMBER_RECORDS).build());
        assertThat(recordsWithGroupB).hasSize(4);

        List<PolicyAudit> recordsWithActionDeploy = provider.getAuditRecords(pfDao,
                AuditFilter.builder().action(AuditAction.DEPLOYMENT).recordNum(NUMBER_RECORDS).build());
        assertThat(recordsWithActionDeploy).hasSize(3);

        List<PolicyAudit> recordsWithMyPolicy = provider.getAuditRecords(pfDao, AuditFilter.builder()
                .name(MY_POLICY.getName()).version(MY_POLICY.getVersion()).recordNum(NUMBER_RECORDS).build());
        assertThat(recordsWithMyPolicy).hasSize(4);
    }

    @Test
    public void testLoadRecordsForLimit() {
        PolicyAuditProvider provider = new PolicyAuditProvider();

        List<PolicyAudit> loadAudits = new ArrayList<>();

        // going to create 102 records.
        for (int i = 0; i <= 50; i++) {
            loadAudits.addAll(generatePolicyAudits(Instant.now().plusSeconds(i), GROUP_A, MY_POLICY));
        }

        provider.createAuditRecords(pfDao, loadAudits);

        List<PolicyAudit> records =
                provider.getAuditRecords(pfDao, AuditFilter.builder().recordNum(NUMBER_RECORDS).build());
        assertThat(records).hasSize(10);

        // check that is being ordered
        assertTrue(records.get(0).getTimestamp().isAfter(records.get(9).getTimestamp()));
        assertEquals(loadAudits.get(loadAudits.size() - 1).getTimestamp(), records.get(0).getTimestamp());

        // try to get 102 records should return 100
        records = provider.getAuditRecords(pfDao, AuditFilter.builder().recordNum(102).build());
        assertThat(records).hasSize(100);

        // try to get -1 records should return 10
        records = provider.getAuditRecords(pfDao, AuditFilter.builder().recordNum(-1).build());
        assertThat(records).hasSize(10);
    }

    @Test
    public void policyProviderExceptions() {
        PolicyAuditProvider provider = new PolicyAuditProvider();

        assertThatThrownBy(() -> {
            provider.createAuditRecords(null, null);
        }).hasMessageMatching(String.format(FIELD_IS_NULL, "dao"));

        assertThatThrownBy(() -> {
            provider.createAuditRecords(pfDao, null);
        }).hasMessageMatching(String.format(FIELD_IS_NULL, "audits"));

        assertThatThrownBy(() -> {
            provider.getAuditRecords(null, AuditFilter.builder().build());
        }).hasMessageMatching(String.format(FIELD_IS_NULL, "dao"));

        assertThatThrownBy(() -> {
            provider.getAuditRecords(pfDao, null);
        }).hasMessageMatching(String.format(FIELD_IS_NULL, "auditFilter"));
    }

    private List<PolicyAudit> generatePolicyAudits(Instant date, String group, ToscaConceptIdentifier policy) {
        // @formatter:off
        PolicyAudit deploy = PolicyAudit.builder()
                                        .pdpGroup(group)
                                        .pdpType("pdpType")
                                        .policy(policy)
                                        .action(AuditAction.DEPLOYMENT)
                                        .timestamp(date.truncatedTo(ChronoUnit.SECONDS))
                                        .build();

        PolicyAudit undeploy = PolicyAudit.builder()
                                        .pdpGroup(group)
                                        .pdpType("pdpType")
                                        .policy(policy)
                                        .action(AuditAction.UNDEPLOYMENT)
                                        .timestamp(date.plusSeconds(1).truncatedTo(ChronoUnit.SECONDS))
                                        .build();
        // @formatter:on

        return List.of(deploy, undeploy);
    }
}
