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

package org.onap.policy.models.provider.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.serialization.ToscaServiceTemplateMessageBodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test persistence of monitoring policies to and from the database.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class MonitoringPolicyPersistenceTest {
    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringPolicyPersistenceTest.class);

    private Gson gson;

    private Connection connection;
    private PfDao pfDao;

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
        gson = new ToscaServiceTemplateMessageBodyHandler().getGson();
    }

    @After
    public void teardown() throws Exception {
        pfDao.close();
        connection.close();
    }

    @Test
    public void testJsonDeserialization() throws JsonSyntaxException, IOException {
        ToscaServiceTemplate serviceTemplate =
                gson.fromJson(ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json"),
                        ToscaServiceTemplate.class);

        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        ToscaPolicy policyBeforeDb = serviceTemplate.getTopologyTemplate().getPolicies().get("onap.restart.tca");

        pfDao.create(policyBeforeDb);

        ToscaPolicy policyAfterDb = pfDao.get(ToscaPolicy.class, new PfConceptKey("onap.restart.tca:1.0.0"));

        assertEquals(policyBeforeDb, policyAfterDb);
    }
}
