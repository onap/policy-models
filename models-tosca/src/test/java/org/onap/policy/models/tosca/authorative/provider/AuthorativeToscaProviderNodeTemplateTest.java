/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022-2025 Nordix Foundation.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.yaml.snakeyaml.Yaml;

/**
 * Test of the {@link AuthorativeToscaProvider} class.
 */
class AuthorativeToscaProviderNodeTemplateTest {


    private static final String NODE_TEMPLATES_JSON = "nodetemplates/nodetemplates.metadatasets.input.tosca.json";
    private static final String UPDATED_METADATA_SET_JSON = "nodetemplates/TestUpdateMetadataSet.json";
    private static final String CREATE_METADATA_SET_JSON = "nodetemplates/TestCreateMetadataSet.json";
    private static final String POLICY_WITH_METADATA_SET_REF = "policies/apex.policy.decisionmaker.input.tosca.yaml";
    private static final String APEX_POLICY_TYPE_YAML = "policytypes/onap.policies.native.Apex.yaml";
    private static final String DAO_IS_NULL = "^dao is marked .*on.*ull but is null$";
    private static ToscaServiceTemplate toscaServiceTemplate;
    private static ToscaServiceTemplate updatedToscaServiceTemplate;
    private static ToscaServiceTemplate createToscaNodeTemplate;
    private PfDao pfDao;
    private StandardCoder standardCoder;
    private final AuthorativeToscaProvider authorativeToscaProvider = new AuthorativeToscaProvider();
    private final YamlJsonTranslator yamlJsonTranslator = new YamlJsonTranslator();

    /**
     * Read policy metadataSet input json.
     *
     * @throws Exception Coder exception
     */
    @BeforeEach
    void fetchToscaNodeTemplatesJson() throws Exception {
        standardCoder = new StandardCoder();
        toscaServiceTemplate =
            standardCoder.decode(ResourceUtils.getResourceAsString(NODE_TEMPLATES_JSON), ToscaServiceTemplate.class);
        updatedToscaServiceTemplate =
            standardCoder.decode(ResourceUtils.getResourceAsString(UPDATED_METADATA_SET_JSON),
                ToscaServiceTemplate.class);
        createToscaNodeTemplate = standardCoder.decode(ResourceUtils.getResourceAsString(
            CREATE_METADATA_SET_JSON), ToscaServiceTemplate.class);
    }

    /**
     * Set up DAO towards the database.
     *
     * @throws Exception on database errors
     */
    @BeforeEach
    void setupDao() throws Exception {
        final DaoParameters daoParameters = new DaoParameters();
        daoParameters.setPluginClass(DefaultPfDao.class.getName());

        daoParameters.setPersistenceUnit("ToscaConceptTest");

        Properties jdbcProperties = new Properties();
        jdbcProperties.setProperty("jakarta.persistence.jdbc.user", "policy");
        jdbcProperties.setProperty("jakarta.persistence.jdbc.password", "P01icY");
        jdbcProperties.setProperty("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        jdbcProperties.setProperty("jakarta.persistence.jdbc.url",
            "jdbc:h2:mem:AuthorativeToscaProviderNodeTemplatesTest");
        daoParameters.setJdbcProperties(jdbcProperties);

        pfDao = new PfDaoFactory().createPfDao(daoParameters);
        pfDao.init(daoParameters);
    }

    @AfterEach
    void teardown() {
        pfDao.close();
    }

    @Test
    void testPolicyMetadataSetsGet() throws Exception {

        assertThatThrownBy(() -> authorativeToscaProvider.getNodeTemplateMetadataSet(null, null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertNotNull(toscaServiceTemplate);
        authorativeToscaProvider.createToscaNodeTemplates(pfDao, toscaServiceTemplate);

        //Fetch all metadataSet if id is null
        List<Map<ToscaEntityKey, Map<String, Object>>> gotPolicyMetadataSets = authorativeToscaProvider
            .getNodeTemplateMetadataSet(pfDao, null, null);
        assertEquals(3, gotPolicyMetadataSets.size());

        // Get filtered metadataSet
        List<Map<ToscaEntityKey, Map<String, Object>>> filteredPolicyMetadataSet = authorativeToscaProvider
            .getNodeTemplateMetadataSet(pfDao, "apexMetadata_adaptive", "2.3.1");
        assertEquals(1, filteredPolicyMetadataSet.size());

        //Get invalid metadataSet
        List<Map<ToscaEntityKey, Map<String, Object>>> filteredMetadataSetInvalid = authorativeToscaProvider
            .getNodeTemplateMetadataSet(pfDao, "invalidname", "1.0.0");
        assertThat(filteredMetadataSetInvalid).isEmpty();
    }

    @Test
    void testToscaNodeTemplatesGet() throws Exception {

        assertThatThrownBy(() -> authorativeToscaProvider.getToscaNodeTemplate(null, null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertNotNull(toscaServiceTemplate);
        authorativeToscaProvider.createToscaNodeTemplates(pfDao, toscaServiceTemplate);

        //Fetch all node templates if id is null
        List<ToscaNodeTemplate> gotToscaNodeTemplates = authorativeToscaProvider
            .getToscaNodeTemplate(pfDao, null, null);
        assertEquals(3, gotToscaNodeTemplates.size());

        // Get filtered node templates
        List<ToscaNodeTemplate> filteredNodeTemplates = authorativeToscaProvider
            .getToscaNodeTemplate(pfDao, "apexMetadata_adaptive", "2.3.1");
        assertEquals(1, filteredNodeTemplates.size());

        //Get invalid node template
        List<ToscaNodeTemplate> filteredNodeTemplatesInvalid = authorativeToscaProvider
            .getToscaNodeTemplate(pfDao, "invalidname", "1.0.0");
        assertThat(filteredNodeTemplatesInvalid).isEmpty();
    }

    @Test
    void testToscaNodeTemplatesCreate() throws Exception {
        assertThatThrownBy(() -> authorativeToscaProvider.createToscaNodeTemplates(null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> authorativeToscaProvider.createToscaNodeTemplates(null, new ToscaServiceTemplate()))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> authorativeToscaProvider.createToscaNodeTemplates(pfDao, null))
            .hasMessageMatching("^toscaServiceTemplate is marked .*on.*ull but is null$");

        ToscaServiceTemplate createdNodeTemplates =
            authorativeToscaProvider.createToscaNodeTemplates(pfDao, toscaServiceTemplate);
        assertThat(createdNodeTemplates.getToscaTopologyTemplate().getNodeTemplates()).hasSize(3);
        assertThat(createdNodeTemplates.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_grpc")
            .getMetadata()).containsKey("threshold");

        authorativeToscaProvider.createToscaNodeTemplates(pfDao, createToscaNodeTemplate);
        assertThat(authorativeToscaProvider.getNodeTemplateMetadataSet(pfDao, null, null)).hasSize(4);

        //Create node template with invalid node type
        createToscaNodeTemplate.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_adaptive")
            .setType("invalid.type");
        assertThatThrownBy(() -> authorativeToscaProvider.createToscaNodeTemplates(pfDao, createToscaNodeTemplate))
            .hasMessageMatching("^NODE_TYPE .* for toscaNodeTemplate .* does not exist$");

    }

    @Test
    void testToscaNodeTemplateUpdate() throws Exception {
        assertThatThrownBy(() -> authorativeToscaProvider.updateToscaNodeTemplates(null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> authorativeToscaProvider.updateToscaNodeTemplates(null, new ToscaServiceTemplate()))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> authorativeToscaProvider.updateToscaNodeTemplates(pfDao, null))
            .hasMessageMatching("^serviceTemplate is marked non-null but is null$");

        authorativeToscaProvider.createToscaNodeTemplates(pfDao, toscaServiceTemplate);
        ToscaServiceTemplate updatedTemplate =
            authorativeToscaProvider.updateToscaNodeTemplates(pfDao, updatedToscaServiceTemplate);
        assertEquals("Updated Metadata set for GRPC",
            updatedTemplate.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_grpc")
                .getDescription());

        //Update nodeTemplate with invalid node type
        updatedToscaServiceTemplate.getToscaTopologyTemplate().getNodeTemplates().get("apexMetadata_grpc")
            .setTypeVersion("0.0.0");
        assertThatThrownBy(() -> authorativeToscaProvider.updateToscaNodeTemplates(pfDao, updatedToscaServiceTemplate))
            .hasMessageMatching("^NODE_TYPE .* for toscaNodeTemplate .* does not exist$");
    }

    @Test
    void testToscaNodeTemplateDelete() throws Exception {
        assertThatThrownBy(() -> authorativeToscaProvider.deleteToscaNodeTemplate(null, null, null))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> authorativeToscaProvider.deleteToscaNodeTemplate(null, null, "0.0.1"))
            .hasMessageMatching(DAO_IS_NULL);

        assertThatThrownBy(() -> authorativeToscaProvider.deleteToscaNodeTemplate(pfDao, null, null))
            .hasMessageMatching("^name is marked .*on.*ull but is null$");

        assertThatThrownBy(() -> authorativeToscaProvider.deleteToscaNodeTemplate(pfDao, "name", null))
            .hasMessageMatching("^version is marked .*on.*ull but is null$");

        authorativeToscaProvider.createToscaNodeTemplates(pfDao, toscaServiceTemplate);
        assertThatThrownBy(() -> authorativeToscaProvider.deleteToscaNodeTemplate(pfDao, "dummyname", "1.0.1"))
            .hasMessage("node template dummyname:1.0.1 not found");


        ToscaServiceTemplate responseTemplate =
            authorativeToscaProvider.deleteToscaNodeTemplate(pfDao, "apexMetadata_decisionMaker",
                "1.0.0");

        assertTrue(responseTemplate.getToscaTopologyTemplate().getNodeTemplates()
            .containsKey("apexMetadata_decisionMaker"));
        assertThat(responseTemplate.getToscaTopologyTemplate().getNodeTemplates()).hasSize(1);

        assertThat(authorativeToscaProvider.getNodeTemplateMetadataSet(pfDao, null, null)).hasSize(2);

    }

    @Test
    void testNodeTemplatesWithExistingPolicy() throws Exception {
        String policyString = ResourceUtils.getResourceAsString(POLICY_WITH_METADATA_SET_REF);
        ToscaServiceTemplate policyServiceTemplate =
            yamlJsonTranslator.fromYaml(policyString, ToscaServiceTemplate.class);

        createPolicyTypes();
        //Create policy with metadataSet reference in it
        authorativeToscaProvider.createPolicies(pfDao, policyServiceTemplate);
        assertThat(authorativeToscaProvider.getPolicyList(pfDao, null, null)).hasSize(1);
        assertEquals("apexMetadata_decisionMaker", authorativeToscaProvider
            .getPolicyList(pfDao, null, null).get(0).getMetadata().get("metadataSetName"));

        //Create node templates
        authorativeToscaProvider.createToscaNodeTemplates(pfDao, toscaServiceTemplate);

        //delete node templates referenced in existing policy
        assertThatThrownBy(() -> authorativeToscaProvider
            .deleteToscaNodeTemplate(pfDao, "apexMetadata_decisionMaker", "1.0.0"))
            .hasMessageEndingWith("Node template is in use, it is referenced in Tosca Policy "
                + "operational.apex.decisionMaker version 1.0.0");

        //delete unreferenced node template
        authorativeToscaProvider.deleteToscaNodeTemplate(pfDao, "apexMetadata_adaptive", "2.3.1");
        assertThat(authorativeToscaProvider.getNodeTemplateMetadataSet(pfDao, null, null)).hasSize(2);
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
