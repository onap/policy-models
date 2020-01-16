/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
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

package org.onap.policy.models.provider.impl;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.provider.AuthorativeToscaProvider;
import org.yaml.snakeyaml.Yaml;

/**
 * Test of the {@link AuthorativeToscaProvider} class.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class AuthorativeToscaProviderReferenceTest {
    private static PfDao pfDao;
    private static final StandardCoder coder = new StandardCoder();

    // @formatter:off
    private static final String[] EXAMPLE_POLICY_TYPES = {
        "onap.policies.controlloop.guard.Blacklist#1.0.0"
                + "#policytypes/onap.policies.controlloop.guard.Blacklist.yaml",
        "onap.policies.controlloop.guard.coordination.FirstBlocksSecond#1.0.0"
                + "#policytypes/onap.policies.controlloop.guard.coordination.FirstBlocksSecond.yaml",
        "onap.policies.controlloop.guard.FrequencyLimiter#1.0.0"
                + "#policytypes/onap.policies.controlloop.guard.FrequencyLimiter.yaml",
        "onap.policies.controlloop.guard.MinMax#1.0.0"
                + "#policytypes/onap.policies.controlloop.guard.MinMax.yaml",
        "onap.policies.controlloop.operational.Common#1.0.0"
                + "#policytypes/onap.policies.controlloop.operational.Common.yaml",
        "onap.policies.controlloop.Operational#1.0.0"
                + "#policytypes/onap.policies.controlloop.Operational.yaml",
        "onap.policies.drools.Controller#1.0.0"
                + "#policytypes/onap.policies.drools.Controller.yaml",
        "onap.policies.monitoring.cdap.tca.hi.lo.app#1.0.0"
                + "#policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.yaml",
        "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server#1.0.0"
                + "#policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server.yaml",
        "onap.policies.Naming#1.0.0"
                + "#policytypes/onap.policies.Naming.yaml",
        "onap.policies.native.Apex#1.0.0"
                + "#policytypes/onap.policies.native.Apex.yaml",
        "onap.policies.native.Drools#1.0.0"
                + "#policytypes/onap.policies.native.Drools.yaml",
        "onap.policies.native.Xacml#1.0.0"
                + "#policytypes/onap.policies.native.Xacml.yaml",
        "onap.policies.optimization.resource.AffinityPolicy#1.0.0"
                + "#policytypes/onap.policies.optimization.resource.AffinityPolicy.yaml",
        "onap.policies.optimization.resource.DistancePolicy#1.0.0"
                + "#policytypes/onap.policies.optimization.resource.DistancePolicy.yaml",
        "onap.policies.optimization.resource.HpaPolicy#1.0.0"
                + "#policytypes/onap.policies.optimization.resource.HpaPolicy.yaml",
        "onap.policies.optimization.resource.OptimizationPolicy#1.0.0"
                + "#policytypes/onap.policies.optimization.resource.OptimizationPolicy.yaml",
        "onap.policies.optimization.resource.PciPolicy#1.0.0"
                + "#policytypes/onap.policies.optimization.resource.PciPolicy.yaml",
        "onap.policies.optimization.resource.Vim_fit#1.0.0"
                + "#policytypes/onap.policies.optimization.resource.Vim_fit.yaml",
        "onap.policies.optimization.resource.VnfPolicy#1.0.0"
                + "#policytypes/onap.policies.optimization.resource.VnfPolicy.yaml",
        "onap.policies.optimization.Resource#1.0.0"
                + "#policytypes/onap.policies.optimization.Resource.yaml",
        "onap.policies.optimization.service.QueryPolicy#1.0.0"
                + "#policytypes/onap.policies.optimization.service.QueryPolicy.yaml",
        "onap.policies.optimization.service.SubscriberPolicy#1.0.0"
                + "#policytypes/onap.policies.optimization.service.SubscriberPolicy.yaml",
        "onap.policies.optimization.Service#1.0.0"
                + "#policytypes/onap.policies.optimization.Service.yaml",
        "onap.policies.Optimization#1.0.0"
                + "#policytypes/onap.policies.Optimization.yaml"};
    // @formatter:on

    private static final Map<ToscaEntityKey, ToscaServiceTemplate> policyTypeMap = new LinkedHashMap<>();

    /**
     * Set up the DAO towards the database.
     *
     * @throws Exception on database errors
     */
    @BeforeClass
    public static void beforeSetupDao() throws Exception {
        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getName());

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
     * Populate the database.
     *
     * @throws PfModelException on database exceptions
     * @throws CoderException on JSON encoding/decoding errors
     */
    @BeforeClass
    public static void beforeFillDatabase() throws PfModelException, CoderException {
        for (String policyTypeDataString : EXAMPLE_POLICY_TYPES) {
            String[] policyTypeDataArray = policyTypeDataString.split("#");
            String policyTypeYamlDefinition = ResourceUtils.getResourceAsString(policyTypeDataArray[2]);

            Object yamlObject = new Yaml().load(policyTypeYamlDefinition);
            String policyTypeJsonDefinition = coder.encode(yamlObject);

            ToscaServiceTemplate serviceTemplate = coder.decode(policyTypeJsonDefinition, ToscaServiceTemplate.class);
            policyTypeMap.put(new ToscaEntityKey(policyTypeDataArray[0], policyTypeDataArray[1]), serviceTemplate);
            new AuthorativeToscaProvider().createPolicyTypes(pfDao, serviceTemplate);
        }
    }

    @AfterClass
    public static void afterTeardown() {
        pfDao.close();
    }

    @Test
    public void testPolicyTypeRead() throws PfModelException, CoderException {
        for (Entry<ToscaEntityKey, ToscaServiceTemplate> policyTypeMapEntry : policyTypeMap.entrySet()) {
            ToscaServiceTemplate serviceTemplate = new AuthorativeToscaProvider().getPolicyTypes(pfDao,
                    policyTypeMapEntry.getKey().getName(), policyTypeMapEntry.getKey().getVersion());

            assertEquals(1, serviceTemplate.getPolicyTypes().size());

            String originalJson = coder.encode(policyTypeMapEntry.getValue());
            String databaseJson = coder.encode(serviceTemplate);

            // TODO: This test has no chance of passing yet but must eventually pass to prove that the policy types
            // TODO: that were retrieved are the same as the policy types that were stored
            // assertEquals(originalJson, databaseJson);
        }
    }
}
