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

package org.onap.policy.models.pdp.persistence.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import java.util.Properties;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.pdp.concepts.PdpPolicyAudit;
import org.onap.policy.models.pdp.concepts.PdpPolicyAudit.AuditAction;
import org.onap.policy.models.tosca.authorative.concepts.ToscaConceptIdentifier;

/**
 * Class for unit testing {@link PdpPolicyAuditProvider}.
 * @author Adheli Tavares (adheli.tavares@est.tech)
 *
 */
public class PdpPolicyAuditProviderTest {

    private static final String DAO_IS_NULL = "dao is marked .*ull but is null";
    private static final String GROUP_A = "groupA";
    private static final String GROUP_B = "groupB";
    private static final ToscaConceptIdentifier MY_POLICY = new ToscaConceptIdentifier("MyPolicy", "1.2.3");
    private static final ToscaConceptIdentifier MY_POLICY2 = new ToscaConceptIdentifier("MyPolicyB", "2.3.4");

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
            jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL, "jdbc:h2:mem:PdpProviderTest");
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
    public void createPdpPolicyAudit() {
        PdpPolicyAuditProvider provider = new PdpPolicyAuditProvider();

        PdpPolicyAudit audit1 = PdpPolicyAudit.builder().pdpGroup(GROUP_A).pdpType("pdpType")
                .policy(MY_POLICY).action(AuditAction.DEPLOYMENT).timestamp(Instant.now()).build();

        PdpPolicyAudit audit2 = PdpPolicyAudit.builder().pdpGroup(GROUP_A).pdpType("pdpType")
                .policy(MY_POLICY2).action(AuditAction.DEPLOYMENT).timestamp(Instant.now()).build();

        PdpPolicyAudit audit3 = PdpPolicyAudit.builder().pdpGroup(GROUP_A).pdpType("pdpType")
                .policy(MY_POLICY2).action(AuditAction.UNDEPLOYMENT).timestamp(Instant.now()).build();

        PdpPolicyAudit audit4 = PdpPolicyAudit.builder().pdpGroup(GROUP_B).pdpType("pdpType")
                .policy(MY_POLICY).action(AuditAction.DEPLOYMENT).timestamp(Instant.now()).build();

        provider.createPdpPolicyDeploymentAudit(pfDao, List.of(audit1, audit2, audit3, audit4));

        List<PdpPolicyAudit> records = provider.auditPdpPolicyDeploymentByPolicy(pfDao, MY_POLICY.getName(),
                MY_POLICY.getVersion(), null, null);
        assertThat(records).hasSize(2);

        List<PdpPolicyAudit> groupARecords =
                provider.auditPdpPolicyDeploymentByGroup(pfDao, GROUP_A, null, null);
        assertThat(groupARecords).hasSize(3);

        List<PdpPolicyAudit> groupBRecords =
                provider.auditPdpPolicyDeploymentByGroup(pfDao, GROUP_B, null, 1);
        assertThat(groupBRecords).hasSize(1);

        // as the start date is now, shouldn't return any records
        List<PdpPolicyAudit> emptyList = provider.auditPdpPolicyDeploymentByPolicy(pfDao, MY_POLICY.getName(),
                MY_POLICY.getVersion(), Instant.now(), 1);
        assertThat(emptyList).isEmpty();
    }

    @Test
    public void createPdpPolicyTrackerExceptions() {
        PdpPolicyAuditProvider provider = new PdpPolicyAuditProvider();

        assertThatThrownBy(() -> {
            provider.createPdpPolicyDeploymentAudit(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            provider.auditPdpPolicyDeploymentByGroup(null, null, Instant.now(), null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            provider.auditPdpPolicyDeploymentByPolicy(null, null, null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            provider.createPdpPolicyDeploymentAudit(pfDao, null);
        }).hasMessageMatching("trackers is marked non-null but is null");

        assertThatThrownBy(() -> {
            provider.auditPdpPolicyDeploymentByGroup(pfDao, null, null, null);
        }).hasMessageMatching("groupName is marked non-null but is null");

        assertThatThrownBy(() -> {
            provider.auditPdpPolicyDeploymentByPolicy(pfDao, null, null, Instant.now(), null);
        }).hasMessageMatching("name is marked non-null but is null");

        assertThatThrownBy(() -> {
            provider.auditPdpPolicyDeploymentByPolicy(pfDao, "name", null, Instant.now(), null);
        }).hasMessageMatching("version is marked non-null but is null");
    }
}
