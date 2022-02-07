/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation.
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

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.dao.DaoParameters;
import org.onap.policy.models.dao.PfDao;
import org.onap.policy.models.dao.PfDaoFactory;
import org.onap.policy.models.dao.impl.DefaultPfDao;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.yaml.snakeyaml.Yaml;

/**
 * Test of the {@link AuthorativeToscaProvider} class.
 */
public class AuthorativeToscaProviderMetadataSetTest {


    private static final String METADATA_SETS_JSON = "metadataSets/sampleMetadataSets.json";
    private static final String UPDATED_METADATA_SET_JSON = "metadataSets/TestUpdateMetadataSet.json";
    private static final String CREATE_METADATA_SET_JSON = "metadataSets/TestCreateMetadataSet.json";
    private static final String POLICY_WITH_METADATA_SET_REF = "metadataSets/apexDecisionMakerPolicy.yaml";
    private static final String APEX_POLICY_TYPE_YAML = "policytypes/onap.policies.native.Apex.yaml";
    private static final String DAO_IS_NULL = "^dao is marked .*on.*ull but is null$";
    private static ToscaServiceTemplate toscaServiceTemplate;
    private static ToscaServiceTemplate updatedToscaServiceTemplate;
    private PfDao pfDao;
    private StandardCoder standardCoder;
    private AuthorativeToscaProvider authorativeToscaProvider = new AuthorativeToscaProvider();
    private YamlJsonTranslator yamlJsonTranslator = new YamlJsonTranslator();

    /**
     * Read policy metadataSet input json.
     * @throws Exception Coder exception
     */
    @Before
    public void fetchPolicyMetadataSetsJson() throws Exception {
        standardCoder = new StandardCoder();
        toscaServiceTemplate =
            standardCoder.decode(ResourceUtils.getResourceAsString(METADATA_SETS_JSON), ToscaServiceTemplate.class);
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
                "jdbc:h2:mem:AuthorativeToscaProviderMetadataSetTest");
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
    public void testPolicyMetadataSetsGet() throws Exception {

        assertThatThrownBy(() -> {
            authorativeToscaProvider.getPolicyMetadataSets(null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertNotNull(toscaServiceTemplate);
        authorativeToscaProvider.createPolicyMetadata(pfDao, toscaServiceTemplate);

        //Fetch all metadataSet if id is null
        List<Map<ToscaEntityKey, Map<String, Object>>> gotPolicyMetadataSets = authorativeToscaProvider
            .getPolicyMetadataSets(pfDao, null, null);
        assertEquals(3, gotPolicyMetadataSets.size());

        // Get filtered metadataSet
        List<Map<ToscaEntityKey, Map<String, Object>>> filteredPolicyMetadataSet = authorativeToscaProvider
            .getPolicyMetadataSets(pfDao, "apexMetadata_adaptive", "2.3.1");
        assertEquals(1, filteredPolicyMetadataSet.size());

        //Get invalid metadataSet
        List<Map<ToscaEntityKey, Map<String, Object>>> filteredMetadataSetInvalid = authorativeToscaProvider
            .getPolicyMetadataSets(pfDao, "invalidname", "1.0.0");
        assertThat(filteredMetadataSetInvalid).isEmpty();

    }

    @Test
    public void testPolicyMetadataSetCreate() throws Exception {
        assertThatThrownBy(() -> {
            authorativeToscaProvider.createPolicyMetadata(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.createPolicyMetadata(null, new ToscaServiceTemplate());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.createPolicyMetadata(pfDao, null);
        }).hasMessageMatching("^toscaServiceTemplate is marked .*on.*ull but is null$");

        ToscaServiceTemplate createdPolicyMetadataSets =
            authorativeToscaProvider.createPolicyMetadata(pfDao, toscaServiceTemplate);
        assertThat(createdPolicyMetadataSets.getToscaTopologyTemplate().getNodeTemplates()).hasSize(3);
        assertThat(createdPolicyMetadataSets.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_grpc")
            .getMetadata()).containsKey("threshold");

        ToscaServiceTemplate serviceTemplate2 = standardCoder.decode(ResourceUtils.getResourceAsString(
            CREATE_METADATA_SET_JSON),
            ToscaServiceTemplate.class);
        authorativeToscaProvider.createPolicyMetadata(pfDao, serviceTemplate2);
        assertThat(authorativeToscaProvider.getPolicyMetadataSets(pfDao, null, null)).hasSize(4);

        //Create metadataSet with invalid node type
        serviceTemplate2.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_adaptive")
            .setType("invalid.type");
        assertThatThrownBy(() -> {
            authorativeToscaProvider.createPolicyMetadata(pfDao, serviceTemplate2);
        }).hasMessageMatching("^NODE_TYPE .* for toscaNodeTemplate .* does not exist$");

    }

    @Test
    public void testPolicyMetadataSetUpdate() throws Exception {
        updatedToscaServiceTemplate =
            standardCoder.decode(ResourceUtils.getResourceAsString(UPDATED_METADATA_SET_JSON),
                ToscaServiceTemplate.class);
        assertThatThrownBy(() -> {
            authorativeToscaProvider.updatePolicyMetadataSet(null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.updatePolicyMetadataSet(null, new ToscaServiceTemplate());
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.updatePolicyMetadataSet(pfDao, null);
        }).hasMessageMatching("^serviceTemplate is marked non-null but is null$");

        authorativeToscaProvider.createPolicyMetadata(pfDao, toscaServiceTemplate);
        ToscaServiceTemplate updatedTemplate =
            authorativeToscaProvider.updatePolicyMetadataSet(pfDao, updatedToscaServiceTemplate);
        assertEquals("Updated Metadata set for GRPC",
            updatedTemplate.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_grpc")
                .getDescription());

        //Update metadataSet with invalid node type
        updatedToscaServiceTemplate.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_grpc")
            .setTypeVersion("0.0.0");
        assertThatThrownBy(() -> {
            authorativeToscaProvider.updatePolicyMetadataSet(pfDao, updatedToscaServiceTemplate);
        }).hasMessageMatching("^NODE_TYPE .* for toscaNodeTemplate .* does not exist$");
    }

    @Test
    public void testPolicyMetadataSetDelete() throws Exception {
        assertThatThrownBy(() -> {
            authorativeToscaProvider.deletePolicyMetadataSet(null, null, null);
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.deletePolicyMetadataSet(null, null, "0.0.1");
        }).hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> {
            authorativeToscaProvider.deletePolicyMetadataSet(pfDao, null, null);
        }).hasMessageMatching("^name is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> {
            authorativeToscaProvider.deletePolicyMetadataSet(pfDao, "name", null);
        }).hasMessageMatching("^version is marked .*on.*ull but is null$");

        authorativeToscaProvider.createPolicyMetadata(pfDao, toscaServiceTemplate);
        assertThatThrownBy(() -> {
            authorativeToscaProvider.deletePolicyMetadataSet(pfDao, "dummyname", "1.0.1");
        }).hasMessage("metadataSet dummyname:1.0.1 not found");


        ToscaServiceTemplate responseTemplate =
            authorativeToscaProvider.deletePolicyMetadataSet(pfDao, "apexMetadata_decisionMaker",
                "1.0.0");

        assertTrue(responseTemplate.getToscaTopologyTemplate().getNodeTemplates()
            .containsKey("apexMetadata_decisionMaker"));
        assertThat(responseTemplate.getToscaTopologyTemplate().getNodeTemplates()).hasSize(1);

        assertThat(authorativeToscaProvider.getPolicyMetadataSets(pfDao, null, null)).hasSize(2);

    }

    @Test
    public void testMetadataSetWithExistingPolicy() throws Exception {
        String policyString = ResourceUtils.getResourceAsString(POLICY_WITH_METADATA_SET_REF);
        ToscaServiceTemplate policyServiceTemplate =
            yamlJsonTranslator.fromYaml(policyString, ToscaServiceTemplate.class);

        createPolicyTypes();
        //Create policy with metadataSet reference in it
        authorativeToscaProvider.createPolicies(pfDao, policyServiceTemplate);
        assertThat(authorativeToscaProvider.getPolicyList(pfDao, null, null)).hasSize(1);
        assertEquals("apexMetadata_decisionMaker", authorativeToscaProvider
            .getPolicyList(pfDao, null, null).get(0).getMetadata().get("metadataSetName"));

        //Create metadataSets
        authorativeToscaProvider.createPolicyMetadata(pfDao, toscaServiceTemplate);

        //delete metadataSet referenced in existing policy
        assertThatThrownBy(() -> {
            authorativeToscaProvider
                .deletePolicyMetadataSet(pfDao, "apexMetadata_decisionMaker", "1.0.0");
        }).hasMessageEndingWith("MetadataSet is in use, it is referenced in Tosca Policy "
            + "operational.apex.decisionMaker version 1.0.0");

        //delete unreferenced metadataSet
        authorativeToscaProvider.deletePolicyMetadataSet(pfDao, "apexMetadata_adaptive", "2.3.1");
        assertThat(authorativeToscaProvider.getPolicyMetadataSets(pfDao, null, null)).hasSize(2);
    }

    private void createPolicyTypes() throws CoderException, PfModelException {
        Object yamlObject =
            new Yaml().load(ResourceUtils.getResourceAsString(APEX_POLICY_TYPE_YAML));
        String yamlAsJsonString = new StandardCoder().encode(yamlObject);

        ToscaServiceTemplate toscaServiceTemplatePolicyType =
            standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(toscaServiceTemplatePolicyType);
        new AuthorativeToscaProvider().createPolicyTypes(pfDao, toscaServiceTemplatePolicyType);
    }

}
