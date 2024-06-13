/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 Nordix Foundation.
 *  Copyright (C) 2019-2020,2022 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.models.tosca.simple.serialization;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.YamlJsonTranslator;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.utils.ToscaServiceTemplateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Test serialization of monitoring policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 * @author Chenfei Gao (cgao@research.att.com)
 */
class MonitoringPolicySerializationTest {

    private static final String VERSION = "version";

    private static final String YAML_VERSION = "tosca_simple_yaml_1_1_0";

    private static final String DEFINITION_VERSION = "tosca_definitions_version";

    private static final String TOPOLOGY_TEMPLATE = "topology_template";

    private static final String TCA_POLICY = "tca.policy";

    private static final String PROPERTIES2 = "properties";

    private static final String POLICY_ID = "policy-id";

    private static final String POLICIES = "policies";

    private static final String POLICY3 = "onap.vfirewall.tca";

    private static final String POLICY2 = "onap.scaleout.tca";

    private static final String POLICY1 = "onap.restart.tca";

    private static final String TYPE1 = "onap.policies.monitoring.tcagen2";

    private static final String METADATA = "metadata";

    private static final String VERSION_100 = "1.0.0";
    private static final String VERSION_200 = "2.0.0";

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringPolicySerializationTest.class);

    private static final String VCPE_MON_INPUT_JSON = "policies/vCPE.policy.monitoring.input.tosca.json";
    private static final String VCPE_MON_INPUT_YAML = "policies/vCPE.policy.monitoring.input.tosca.yaml";
    private static final String VDNS_MON_INPUT_JSON = "policies/vDNS.policy.monitoring.input.tosca.json";
    private static final String VDNS_MON_INPUT_YAML = "policies/vDNS.policy.monitoring.input.tosca.yaml";
    private static final String VFW_MON_INPUT_JSON = "policies/vFirewall.policy.monitoring.input.tosca.json";
    private static final String VFW_MON_INPUT_YAML = "policies/vFirewall.policy.monitoring.input.tosca.yaml";
    private static final String VFW_MON_INPUT_V2_JSON = "policies/vFirewall.policy.monitoring.input.tosca.v2.json";
    private static final String VFW_MON_INPUT_V2_YAML = "policies/vFirewall.policy.monitoring.input.tosca.v2.yaml";

    private final StandardCoder standardCoder = new StandardCoder();
    private final YamlJsonTranslator yamlJsonTranslator = new YamlJsonTranslator();

    @Test
    void testDeserialization() throws Exception {
        String policyTypeInputJson =
                ResourceUtils.getResourceAsString("policytypes/onap.policies.monitoring.tcagen2.yaml");
        ToscaServiceTemplate plainPolicyTypes =
                yamlJsonTranslator.fromYaml(policyTypeInputJson, ToscaServiceTemplate.class);

        JpaToscaServiceTemplate policyTypeServiceTemplate = new JpaToscaServiceTemplate();
        policyTypeServiceTemplate.fromAuthorative(plainPolicyTypes);

        // vCPE
        JpaToscaServiceTemplate serviceTemplateFromJson = deserializeMonitoringInputJson(VCPE_MON_INPUT_JSON);
        JpaToscaServiceTemplate mergedServiceTemplate =
                ToscaServiceTemplateUtils.addFragment(policyTypeServiceTemplate, serviceTemplateFromJson);
        verifyVcpeMonitoringInputDeserialization(mergedServiceTemplate);
        JpaToscaServiceTemplate serviceTemplateFromYaml = deserializeMonitoringInputYaml(VCPE_MON_INPUT_YAML);
        assertEquals(0, serviceTemplateFromJson.compareTo(serviceTemplateFromYaml));

        // vDNS
        serviceTemplateFromJson = deserializeMonitoringInputJson(VDNS_MON_INPUT_JSON);
        mergedServiceTemplate =
                ToscaServiceTemplateUtils.addFragment(policyTypeServiceTemplate, serviceTemplateFromJson);
        verifyVdnsMonitoringInputDeserialization(mergedServiceTemplate);
        serviceTemplateFromYaml = deserializeMonitoringInputYaml(VDNS_MON_INPUT_YAML);
        assertEquals(0, serviceTemplateFromJson.compareTo(serviceTemplateFromYaml));

        // vFirewall
        serviceTemplateFromJson = deserializeMonitoringInputJson(VFW_MON_INPUT_JSON);
        mergedServiceTemplate =
                ToscaServiceTemplateUtils.addFragment(policyTypeServiceTemplate, serviceTemplateFromJson);
        verifyVfwMonitoringInputDeserialization(mergedServiceTemplate, VERSION_100);
        serviceTemplateFromYaml = deserializeMonitoringInputYaml(VFW_MON_INPUT_YAML);
        assertEquals(0, serviceTemplateFromJson.compareTo(serviceTemplateFromYaml));

        testDeserializationMonitoringV2();
    }

    @Test
    void testSerialization() {
        assertThatCode(() -> {
            // vCPE
            JpaToscaServiceTemplate serviceTemplate = deserializeMonitoringInputJson(VCPE_MON_INPUT_JSON);
            String serializedServiceTemplate = serializeMonitoringServiceTemplate(serviceTemplate);
            verifyVcpeMonitoringOutputserialization(serializedServiceTemplate);

            // vDNS
            serviceTemplate = deserializeMonitoringInputJson(VDNS_MON_INPUT_JSON);
            serializedServiceTemplate = serializeMonitoringServiceTemplate(serviceTemplate);
            verifyVdnsMonitoringOutputserialization(serializedServiceTemplate);

            // vFirewall
            serviceTemplate = deserializeMonitoringInputJson(VFW_MON_INPUT_JSON);
            serializedServiceTemplate = serializeMonitoringServiceTemplate(serviceTemplate);
            verifyVfwMonitoringOutputserialization(serializedServiceTemplate, VERSION_100);

            // vFirewall v2
            serviceTemplate = deserializeMonitoringInputJson(VFW_MON_INPUT_V2_JSON);
            serializedServiceTemplate = serializeMonitoringServiceTemplate(serviceTemplate);
            verifyVfwMonitoringOutputserialization(serializedServiceTemplate, VERSION_200);
        }).as("No exception should be thrown").doesNotThrowAnyException();
    }

    private void testDeserializationMonitoringV2() throws Exception {
        String policyTypeInputJson =
            ResourceUtils.getResourceAsString("policytypes/onap.policies.monitoring.tcagen2.v2.yaml");
        ToscaServiceTemplate plainPolicyTypes =
            yamlJsonTranslator.fromYaml(policyTypeInputJson, ToscaServiceTemplate.class);

        JpaToscaServiceTemplate policyTypeServiceTemplate = new JpaToscaServiceTemplate();
        policyTypeServiceTemplate.fromAuthorative(plainPolicyTypes);

        JpaToscaServiceTemplate serviceTemplateFromJson = deserializeMonitoringInputJson(VFW_MON_INPUT_V2_JSON);
        JpaToscaServiceTemplate mergedServiceTemplate =
            ToscaServiceTemplateUtils.addFragment(policyTypeServiceTemplate, serviceTemplateFromJson);
        verifyVfwMonitoringInputDeserialization(mergedServiceTemplate, VERSION_200);

        JpaToscaServiceTemplate serviceTemplateFromYaml = deserializeMonitoringInputYaml(VFW_MON_INPUT_V2_YAML);
        assertEquals(0, serviceTemplateFromJson.compareTo(serviceTemplateFromYaml));
    }

    private JpaToscaServiceTemplate deserializeMonitoringInputJson(String resourcePath) throws Exception {
        String policyJson = ResourceUtils.getResourceAsString(resourcePath);
        ToscaServiceTemplate serviceTemplate = standardCoder.decode(policyJson, ToscaServiceTemplate.class);
        JpaToscaServiceTemplate jpaToscaServiceTemplate = new JpaToscaServiceTemplate();
        jpaToscaServiceTemplate.fromAuthorative(serviceTemplate);
        return jpaToscaServiceTemplate;
    }

    private JpaToscaServiceTemplate deserializeMonitoringInputYaml(String resourcePath) throws Exception {

        Yaml yaml = new Yaml();
        String policyYaml = ResourceUtils.getResourceAsString(resourcePath);
        Object yamlObject = yaml.load(policyYaml);
        String yamlAsJsonString = new StandardCoder().encode(yamlObject);
        ToscaServiceTemplate serviceTemplate = standardCoder.decode(yamlAsJsonString, ToscaServiceTemplate.class);

        JpaToscaServiceTemplate jpaToscaServiceTemplate = new JpaToscaServiceTemplate();
        jpaToscaServiceTemplate.fromAuthorative(serviceTemplate);
        return jpaToscaServiceTemplate;
    }

    private String serializeMonitoringServiceTemplate(JpaToscaServiceTemplate serviceTemplate) throws CoderException {
        return standardCoder.encode(serviceTemplate.toAuthorative());
    }

    private void verifyVcpeMonitoringInputDeserialization(JpaToscaServiceTemplate serviceTemplate) {

        // Sanity check the entire structure
        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate("").toString());
        assertTrue(serviceTemplate.validate("").isValid());

        // Check tosca_definitions_version
        assertEquals(YAML_VERSION, serviceTemplate.getToscaDefinitionsVersion());

        Map<PfConceptKey, JpaToscaPolicy> policiesConceptMap =
                serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap();

        // Check policies
        assertEquals(1, policiesConceptMap.size());
        assertEquals(POLICY1, policiesConceptMap.keySet().iterator().next().getName());
        assertEquals("onap.restart.tca:1.0.0",
                serviceTemplate.getTopologyTemplate().getPolicies().get(POLICY1).getId());

        JpaToscaPolicy policyVal = policiesConceptMap.values().iterator().next();

        // Check metadata
        assertEquals(2, policyVal.getMetadata().size());
        assertEquals(POLICY_ID, policyVal.getMetadata().entrySet().iterator().next().getKey());
        assertEquals(POLICY1, policyVal.getMetadata().entrySet().iterator().next().getValue());

        // Check properties
        assertEquals(1, policiesConceptMap.values().iterator().next().getProperties().size());
        assertEquals(TCA_POLICY, policyVal.getProperties().keySet().iterator().next());
        assertNotNull(policyVal.getProperties().values().iterator().next());
    }

    private void verifyVdnsMonitoringInputDeserialization(JpaToscaServiceTemplate serviceTemplate) {

        // Sanity check the entire structure
        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate("").toString());
        assertTrue(serviceTemplate.validate("").isValid());

        // Check tosca_definitions_version
        assertEquals(YAML_VERSION, serviceTemplate.getToscaDefinitionsVersion());

        Map<PfConceptKey, JpaToscaPolicy> policiesConceptMap =
                serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap();

        // Check policies
        assertEquals(1, policiesConceptMap.size());
        assertEquals(POLICY2, policiesConceptMap.keySet().iterator().next().getName());
        assertEquals("onap.scaleout.tca:1.0.0",
                serviceTemplate.getTopologyTemplate().getPolicies().get(POLICY2).getId());

        JpaToscaPolicy policyVal = policiesConceptMap.values().iterator().next();

        // Check metadata
        assertEquals(2, policyVal.getMetadata().size());
        assertEquals(POLICY_ID, policyVal.getMetadata().entrySet().iterator().next().getKey());
        assertEquals(POLICY2, policyVal.getMetadata().entrySet().iterator().next().getValue());

        // Check properties
        assertEquals(1, policiesConceptMap.values().iterator().next().getProperties().size());
        assertEquals(TCA_POLICY, policyVal.getProperties().keySet().iterator().next());
        assertNotNull(policyVal.getProperties().values().iterator().next());
    }

    private void verifyVfwMonitoringInputDeserialization(JpaToscaServiceTemplate serviceTemplate, String version) {

        // Sanity check the entire structure
        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate("").toString());
        assertTrue(serviceTemplate.validate("").isValid());

        // Check tosca_definitions_version
        assertEquals(YAML_VERSION, serviceTemplate.getToscaDefinitionsVersion());

        Map<PfConceptKey, JpaToscaPolicy> policiesConceptMap =
                serviceTemplate.getTopologyTemplate().getPolicies().getConceptMap();

        // Check policies
        assertEquals(1, policiesConceptMap.size());
        assertEquals(POLICY3, policiesConceptMap.keySet().iterator().next().getName());
        assertEquals("onap.vfirewall.tca:" + version,
                serviceTemplate.getTopologyTemplate().getPolicies().get(POLICY3).getId());

        JpaToscaPolicy policyVal = policiesConceptMap.values().iterator().next();

        // Check metadata
        assertEquals(2, policyVal.getMetadata().size());
        assertEquals(POLICY_ID, policyVal.getMetadata().entrySet().iterator().next().getKey());
        assertEquals(POLICY3, policyVal.getMetadata().entrySet().iterator().next().getValue());

        // Check properties
        assertEquals(1, policiesConceptMap.values().iterator().next().getProperties().size());
        assertEquals(TCA_POLICY, policyVal.getProperties().keySet().iterator().next());
        assertNotNull(policyVal.getProperties().values().iterator().next());
    }

    private void verifyVcpeMonitoringOutputserialization(String serializedServiceTemplate) {

        JsonObject serviceTemplateJsonObject = JsonParser.parseString(serializedServiceTemplate).getAsJsonObject();
        assertEquals(YAML_VERSION, serviceTemplateJsonObject.get(DEFINITION_VERSION).getAsString());
        JsonObject topologyTemplateJsonObject = serviceTemplateJsonObject.get(TOPOLOGY_TEMPLATE).getAsJsonObject();
        JsonArray policiesJsonArray = topologyTemplateJsonObject.get(POLICIES).getAsJsonArray();
        assertEquals(1, policiesJsonArray.size());
        JsonObject policy = policiesJsonArray.iterator().next().getAsJsonObject();
        assertNotNull(policy.get(POLICY1));
        JsonObject policyVal = policy.get(POLICY1).getAsJsonObject();
        assertEquals(TYPE1, policyVal.get("type").getAsString());
        assertEquals(VERSION_100, policyVal.get(VERSION).getAsString());
        assertEquals(POLICY1, policyVal.get(METADATA).getAsJsonObject().get(POLICY_ID).getAsString());
        JsonObject properties = policyVal.get(PROPERTIES2).getAsJsonObject();
        assertNotNull(properties.get(TCA_POLICY));
    }

    private void verifyVdnsMonitoringOutputserialization(String serializedServiceTemplate) {

        JsonObject serviceTemplateJsonObject = JsonParser.parseString(serializedServiceTemplate).getAsJsonObject();
        assertEquals(YAML_VERSION, serviceTemplateJsonObject.get(DEFINITION_VERSION).getAsString());
        JsonObject topologyTemplateJsonObject = serviceTemplateJsonObject.get(TOPOLOGY_TEMPLATE).getAsJsonObject();
        JsonArray policiesJsonArray = topologyTemplateJsonObject.get(POLICIES).getAsJsonArray();
        assertEquals(1, policiesJsonArray.size());
        JsonObject policy = policiesJsonArray.iterator().next().getAsJsonObject();
        assertNotNull(policy.get(POLICY2));
        JsonObject policyVal = policy.get(POLICY2).getAsJsonObject();
        assertEquals(TYPE1, policyVal.get("type").getAsString());
        assertEquals(VERSION_100, policyVal.get(VERSION).getAsString());
        assertEquals(POLICY2, policyVal.get(METADATA).getAsJsonObject().get(POLICY_ID).getAsString());
        JsonObject properties = policyVal.get(PROPERTIES2).getAsJsonObject();
        assertNotNull(properties.get(TCA_POLICY));
    }

    private void verifyVfwMonitoringOutputserialization(String serializedServiceTemplate, String version) {

        JsonObject serviceTemplateJsonObject = JsonParser.parseString(serializedServiceTemplate).getAsJsonObject();
        assertEquals(YAML_VERSION, serviceTemplateJsonObject.get(DEFINITION_VERSION).getAsString());
        JsonObject topologyTemplateJsonObject = serviceTemplateJsonObject.get(TOPOLOGY_TEMPLATE).getAsJsonObject();
        JsonArray policiesJsonArray = topologyTemplateJsonObject.get(POLICIES).getAsJsonArray();
        assertEquals(1, policiesJsonArray.size());
        JsonObject policy = policiesJsonArray.iterator().next().getAsJsonObject();
        assertNotNull(policy.get(POLICY3));
        JsonObject policyVal = policy.get(POLICY3).getAsJsonObject();
        assertEquals(TYPE1, policyVal.get("type").getAsString());
        assertEquals(version, policyVal.get(VERSION).getAsString());
        assertEquals(POLICY3, policyVal.get(METADATA).getAsJsonObject().get(POLICY_ID).getAsString());
        JsonObject properties = policyVal.get(PROPERTIES2).getAsJsonObject();
        assertNotNull(properties.get(TCA_POLICY));
    }
}
