/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
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

package org.onap.policy.models.tosca.authorative.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyTypeImpl;

/**
 * Test of the {@link AuthorativeToscaProvider} class.
 */
public class AuthorativeToscaProviderPolicyTypeImplTest {

    private static final String VCPE_JSON = "policies/vCPE.policy.policytypeimpl.input.tosca.json";
    private static final String DAO_IS_NULL = "^dao is marked .*on.*ull but is null$";
    private static final String POLICY_TYPE_IMPL_NAME = "apexPolicyModel";
    private static final String POLICY_TYPE_IMPL_VERSION = "0.0.1";
    private static ToscaPolicyTypeImpl toscaPolicyTypeImpl;
    private PfDao pfDao;
    private StandardCoder standardCoder;
    private AuthorativeToscaProvider authorativeToscaProvider = new AuthorativeToscaProvider();

    /**
     * Read policy type impl input json.
     * @throws Exception Coder exception
     */
    @Before
    public void fetchPolicyImplJson() throws Exception {
        toscaPolicyTypeImpl =
            standardCoder.decode(ResourceUtils.getResourceAsString(VCPE_JSON), ToscaPolicyTypeImpl.class);
    }

    /**
     * Set up DAO towards the database.
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
            jdbcProperties.setProperty(PersistenceUnitProperties.JDBC_URL,
                "jdbc:h2:mem:AuthorativeToscaProviderPolicyImplTest");
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

    @After
    public void teardown() {
        pfDao.close();
    }

    @Test
    public void testPolicyTypeImplGet() throws Exception {

        assertThatThrownBy(() -> {
            authorativeToscaProvider.getPolicyTypeImpl(null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.getAllPolicyTypeImpl(null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertNotNull(toscaPolicyTypeImpl);
        ToscaPolicyTypeImpl createdPolicyTypeImpl =
            authorativeToscaProvider.createPolicyTypeImpl(pfDao, toscaPolicyTypeImpl);

        List<ToscaPolicyTypeImpl> gotPolicyImplList = authorativeToscaProvider.getAllPolicyTypeImpl(pfDao);
        assertEquals(1, gotPolicyImplList.size());
        assertEquals(0, toscaPolicyTypeImpl.getPolicyTypeImplRef().getName().compareTo(gotPolicyImplList.get(0)
            .getPolicyTypeImplRef().getName()));

        ToscaPolicyTypeImpl gotPolicyTypeImpl = authorativeToscaProvider.getPolicyTypeImpl(pfDao, POLICY_TYPE_IMPL_NAME,
            POLICY_TYPE_IMPL_VERSION);
        assertThat(createdPolicyTypeImpl.getPolicyModel()).hasSize(toscaPolicyTypeImpl.getPolicyModel().size());

        assertTrue(new AuthorativeToscaProvider().getPolicyList(pfDao, "Nonexistant", "0.0.1")
            .isEmpty());

        // Get filtered policy type impl based on pdp type
        Map<String, Object> filterMap = new HashMap<>();
        filterMap.put("pdpType", "apex-pdp");
        filterMap.put("policyTypeRef", new PfConceptKey("onap.policies.controlloop.operational.common.Apex",
            "1.0.0"));

        List<ToscaPolicyTypeImpl> filteredPolicyTypeImplList = authorativeToscaProvider.getFilteredPolicyTypeImpl(pfDao,
            filterMap);
        assertEquals(1, filteredPolicyTypeImplList.size());
        assertEquals("apex-pdp", filteredPolicyTypeImplList.get(0).getPdpType());

        filterMap.clear();
        filterMap.put("pdpType", "invalid-pdp");
        List<ToscaPolicyTypeImpl> filteredPolicyTypeImplInvalid = authorativeToscaProvider
            .getFilteredPolicyTypeImpl(pfDao, filterMap);
        assertEquals(0, filteredPolicyTypeImplInvalid.size());

    }

    @Test
    public void testPolicyTypeImplCreate() throws Exception {
        assertThatThrownBy(() -> {
            authorativeToscaProvider.createPolicyTypeImpl(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.createPolicyTypeImpl(null, new ToscaPolicyTypeImpl());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.createPolicyTypeImpl(pfDao, null);
        }).hasMessageMatching("^policyImpl is marked .*on.*ull but is null$");

        ToscaPolicyTypeImpl createdPolicyTypeImpl =
            authorativeToscaProvider.createPolicyTypeImpl(pfDao, toscaPolicyTypeImpl);

        String inputPolicy = toscaPolicyTypeImpl.getPolicyTypeImplRef().getName();
        String createdPolicy = createdPolicyTypeImpl.getPolicyTypeImplRef().getName();
        assertEquals(0, inputPolicy.compareTo(createdPolicy));
        assertEquals(createdPolicyTypeImpl.getPolicyTypeImplRef().getVersion(),
            toscaPolicyTypeImpl.getPolicyTypeImplRef().getVersion());
    }

    @Test
    public void testPolicyTypeImplUpdate() throws Exception {
        assertThatThrownBy(() -> {
            authorativeToscaProvider.updatePolicyTypeImpl(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.updatePolicyTypeImpl(null, new ToscaPolicyTypeImpl());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.updatePolicyTypeImpl(pfDao, null);
        }).hasMessageMatching("^policyImpl is marked .*on.*ull but is null$");

        ToscaPolicyTypeImpl createdPolicyTypeImpl =
            authorativeToscaProvider.createPolicyTypeImpl(pfDao, toscaPolicyTypeImpl);

        toscaPolicyTypeImpl.setPdpType("updated-pdp");
        ToscaPolicyTypeImpl updatedPolicyTypeImpl =
            authorativeToscaProvider.updatePolicyTypeImpl(pfDao, toscaPolicyTypeImpl);

        assertEquals(updatedPolicyTypeImpl.getPdpType(), "updated-pdp");
        assertEquals(createdPolicyTypeImpl.getIdentifier(), updatedPolicyTypeImpl.getIdentifier());
    }

    @Test
    public void testPolicyTypeImplDelete() throws Exception {
        assertThatThrownBy(() -> {
            authorativeToscaProvider.deletePolicyTypeImpl(null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.deletePolicyTypeImpl(null, null, "0.0.1");
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.deletePolicy(pfDao, null, null);
        }).hasMessageMatching("^name is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            authorativeToscaProvider.deletePolicy(pfDao, "name", null);
        }).hasMessageMatching("^version is marked .*on.*ull but is null$");

        authorativeToscaProvider.createPolicyTypeImpl(pfDao, toscaPolicyTypeImpl);

        ToscaPolicyTypeImpl deletedPolicyTypeImpl =
            authorativeToscaProvider.deletePolicyTypeImpl(pfDao, toscaPolicyTypeImpl.getPolicyTypeImplRef().getName(),
                toscaPolicyTypeImpl.getPolicyTypeImplRef().getVersion());

        String deletedPolicyImplName =
            deletedPolicyTypeImpl.getPolicyTypeImplRef().getName();
        assertEquals(0, toscaPolicyTypeImpl.getPolicyTypeImplRef().getName().compareTo(deletedPolicyImplName));

    }

}
