/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.simple.serialization.ToscaServiceTemplateMessageBodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Test serialization of monitoring policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class MonitoringPolicySerializationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringPolicySerializationTest.class);

    private static final String VCPE_MONITORING_INPUT_JSON = "policies/vCPE.policy.monitoring.input.tosca.json";
    private static final String VCPE_MONITORING_INPUT_YAML = "policies/vCPE.policy.monitoring.input.tosca.yaml";
    private static final String VDNS_MONITORING_INPUT_JSON = "policies/vDNS.policy.monitoring.input.tosca.json";
    private static final String VDNS_MONITORING_INPUT_YAML = "policies/vDNS.policy.monitoring.input.tosca.yaml";
    private static final String VFW_MONITORING_INPUT_JSON = "policies/vFirewall.policy.monitoring.input.tosca.json";
    private static final String VFW_MONITORING_INPUT_YAML = "policies/vFirewall.policy.monitoring.input.tosca.yaml";

    private Gson gson;

    @Before
    public void setUp() {
        gson = new ToscaServiceTemplateMessageBodyHandler().getGson();
    }

    @Test
    public void testDeserialization() {
        try {
            // vCPE
            ToscaServiceTemplate serviceTemplateFromJson = deserializeMonitoringInputJson(VCPE_MONITORING_INPUT_JSON);
            verifyVcpeMonitoringInputDeserialization(serviceTemplateFromJson);
            ToscaServiceTemplate serviceTemplateFromYaml = deserializeMonitoringInputYaml(VCPE_MONITORING_INPUT_YAML);
            assertTrue(serviceTemplateFromJson.compareTo(serviceTemplateFromYaml) == 0);

            // vDNS
            serviceTemplateFromJson = deserializeMonitoringInputJson(VDNS_MONITORING_INPUT_JSON);
            verifyVdnsMonitoringInputDeserialization(serviceTemplateFromJson);
            serviceTemplateFromYaml = deserializeMonitoringInputYaml(VDNS_MONITORING_INPUT_YAML);
            assertTrue(serviceTemplateFromJson.compareTo(serviceTemplateFromYaml) == 0);

            // vFirewall
            serviceTemplateFromJson = deserializeMonitoringInputJson(VFW_MONITORING_INPUT_JSON);
            verifyVfwMonitoringInputDeserialization(serviceTemplateFromJson);
            serviceTemplateFromYaml = deserializeMonitoringInputYaml(VFW_MONITORING_INPUT_YAML);
            assertTrue(serviceTemplateFromJson.compareTo(serviceTemplateFromYaml) == 0);

        } catch (Exception e) {
            fail("No exception should be thrown");
        }
    }

    @Test
    public void testSerialization() {
        try {
            // vCPE
            ToscaServiceTemplate serviceTemplate = deserializeMonitoringInputJson(VCPE_MONITORING_INPUT_JSON);
            String serializedServiceTemplate = serializeMonitoringServiceTemplate(serviceTemplate);
            verifyVcpeMonitoringOutputserialization(serializedServiceTemplate);

            // vDNS
            serviceTemplate = deserializeMonitoringInputJson(VDNS_MONITORING_INPUT_JSON);
            serializedServiceTemplate = serializeMonitoringServiceTemplate(serviceTemplate);
            verifyVdnsMonitoringOutputserialization(serializedServiceTemplate);

            // vFirewall
            serviceTemplate = deserializeMonitoringInputJson(VFW_MONITORING_INPUT_JSON);
            serializedServiceTemplate = serializeMonitoringServiceTemplate(serviceTemplate);
            verifyVfwMonitoringOutputserialization(serializedServiceTemplate);

        } catch (Exception e) {
            LOGGER.warn("No exception should be thrown", e);
            fail("No exception should be thrown");
        }
    }

    private ToscaServiceTemplate deserializeMonitoringInputJson(String resourcePath)
            throws JsonSyntaxException, IOException {

        String policyJson = ResourceUtils.getResourceAsString(resourcePath);
        ToscaServiceTemplate serviceTemplate = gson.fromJson(policyJson, ToscaServiceTemplate.class);
        return serviceTemplate;
    }

    private ToscaServiceTemplate deserializeMonitoringInputYaml(String resourcePath)
            throws Exception {

        Yaml yaml = new Yaml();
        String policyYaml = ResourceUtils.getResourceAsString(resourcePath);
        Object yamlObject = yaml.load(policyYaml);
        String yamlAsJsonString = new StandardCoder().encode(yamlObject);
        ToscaServiceTemplate serviceTemplate = gson.fromJson(yamlAsJsonString, ToscaServiceTemplate.class);
        return serviceTemplate;
    }

    private String serializeMonitoringServiceTemplate(ToscaServiceTemplate serviceTemplate) {
        return gson.toJson(serviceTemplate);
    }

    private void verifyVcpeMonitoringInputDeserialization(ToscaServiceTemplate serviceTemplate) {

        // Sanity check the entire structure
        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        // Check tosca_definitions_version
        assertEquals("tosca_simple_yaml_1_0_0",
                serviceTemplate.getToscaDefinitionsVersion());

        Map<PfConceptKey, ToscaPolicy> policiesConceptMap = serviceTemplate.getTopologyTemplate()
                .getPolicies().getConceptMap();

        // Check policies
        assertTrue(policiesConceptMap.size() == 1);
        assertEquals("onap.restart.tca", policiesConceptMap.keySet().iterator().next().getName());
        assertEquals("onap.restart.tca:1.0.0",
                serviceTemplate.getTopologyTemplate().getPolicies().get("onap.restart.tca").getId());

        ToscaPolicy policyVal = policiesConceptMap.values().iterator().next();

        // Check metadata
        assertTrue(policyVal.getMetadata().size() == 1);
        assertEquals("policy-id", policyVal.getMetadata().entrySet().iterator().next().getKey());
        assertEquals("onap.restart.tca", policyVal.getMetadata().entrySet().iterator().next().getValue());

        // Check properties
        assertTrue(policiesConceptMap.values().iterator().next().getProperties().size() == 1);
        assertEquals("tca_policy", policyVal.getProperties().keySet().iterator().next());
        assertNotNull(policyVal.getProperties().values().iterator().next());
    }

    private void verifyVdnsMonitoringInputDeserialization(ToscaServiceTemplate serviceTemplate) {

        // Sanity check the entire structure
        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        // Check tosca_definitions_version
        assertEquals("tosca_simple_yaml_1_0_0",
                serviceTemplate.getToscaDefinitionsVersion());

        Map<PfConceptKey, ToscaPolicy> policiesConceptMap = serviceTemplate.getTopologyTemplate()
                .getPolicies().getConceptMap();

        // Check policies
        assertTrue(policiesConceptMap.size() == 1);
        assertEquals("onap.scaleout.tca", policiesConceptMap.keySet().iterator().next().getName());
        assertEquals("onap.scaleout.tca:1.0.0",
                serviceTemplate.getTopologyTemplate().getPolicies().get("onap.scaleout.tca").getId());

        ToscaPolicy policyVal = policiesConceptMap.values().iterator().next();

        // Check metadata
        assertTrue(policyVal.getMetadata().size() == 1);
        assertEquals("policy-id", policyVal.getMetadata().entrySet().iterator().next().getKey());
        assertEquals("onap.scaleout.tca", policyVal.getMetadata().entrySet().iterator().next().getValue());

        // Check properties
        assertTrue(policiesConceptMap.values().iterator().next().getProperties().size() == 1);
        assertEquals("tca_policy", policyVal.getProperties().keySet().iterator().next());
        assertNotNull(policyVal.getProperties().values().iterator().next());
    }

    private void verifyVfwMonitoringInputDeserialization(ToscaServiceTemplate serviceTemplate) {

        // Sanity check the entire structure
        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        // Check tosca_definitions_version
        assertEquals("tosca_simple_yaml_1_0_0",
                serviceTemplate.getToscaDefinitionsVersion());

        Map<PfConceptKey, ToscaPolicy> policiesConceptMap = serviceTemplate.getTopologyTemplate()
                .getPolicies().getConceptMap();

        // Check policies
        assertTrue(policiesConceptMap.size() == 1);
        assertEquals("onap.vfirewall.tca", policiesConceptMap.keySet().iterator().next().getName());
        assertEquals("onap.vfirewall.tca:1.0.0",
                serviceTemplate.getTopologyTemplate().getPolicies().get("onap.vfirewall.tca").getId());

        ToscaPolicy policyVal = policiesConceptMap.values().iterator().next();

        // Check metadata
        assertTrue(policyVal.getMetadata().size() == 1);
        assertEquals("policy-id", policyVal.getMetadata().entrySet().iterator().next().getKey());
        assertEquals("onap.vfirewall.tca", policyVal.getMetadata().entrySet().iterator().next().getValue());

        // Check properties
        assertTrue(policiesConceptMap.values().iterator().next().getProperties().size() == 1);
        assertEquals("tca_policy", policyVal.getProperties().keySet().iterator().next());
        assertNotNull(policyVal.getProperties().values().iterator().next());
    }

    private void verifyVcpeMonitoringOutputserialization(String serializedServiceTemplate) {

        JsonObject serviceTemplateJsonObject = new JsonParser().parse(serializedServiceTemplate).getAsJsonObject();
        assertEquals("tosca_simple_yaml_1_0_0", serviceTemplateJsonObject.get("tosca_definitions_version")
                .getAsString());
        JsonObject topologyTemplateJsonObject = serviceTemplateJsonObject.get("topology_template")
                .getAsJsonObject();
        JsonArray policiesJsonArray = topologyTemplateJsonObject.get("policies").getAsJsonArray();
        assertTrue(policiesJsonArray.size() == 1);
        JsonObject policy = policiesJsonArray.iterator().next().getAsJsonObject();
        assertNotNull(policy.get("onap.restart.tca"));
        JsonObject policyVal = policy.get("onap.restart.tca").getAsJsonObject();
        assertEquals("onap.policies.monitoring.cdap.tca.hi.lo.app", policyVal.get("type").getAsString());
        assertEquals("1.0.0", policyVal.get("version").getAsString());
        assertEquals("onap.restart.tca", policyVal.get("metadata").getAsJsonObject().get("policy-id")
                .getAsString());
        JsonObject properties = policyVal.get("properties").getAsJsonObject();
        assertNotNull(properties.get("tca_policy"));
    }

    private void verifyVdnsMonitoringOutputserialization(String serializedServiceTemplate) {

        JsonObject serviceTemplateJsonObject = new JsonParser().parse(serializedServiceTemplate).getAsJsonObject();
        assertEquals("tosca_simple_yaml_1_0_0", serviceTemplateJsonObject.get("tosca_definitions_version")
                .getAsString());
        JsonObject topologyTemplateJsonObject = serviceTemplateJsonObject.get("topology_template").getAsJsonObject();
        JsonArray policiesJsonArray = topologyTemplateJsonObject.get("policies").getAsJsonArray();
        assertTrue(policiesJsonArray.size() == 1);
        JsonObject policy = policiesJsonArray.iterator().next().getAsJsonObject();
        assertNotNull(policy.get("onap.scaleout.tca"));
        JsonObject policyVal = policy.get("onap.scaleout.tca").getAsJsonObject();
        assertEquals("onap.policies.monitoring.cdap.tca.hi.lo.app", policyVal.get("type").getAsString());
        assertEquals("1.0.0", policyVal.get("version").getAsString());
        assertEquals("onap.scaleout.tca", policyVal.get("metadata").getAsJsonObject().get("policy-id")
                .getAsString());
        JsonObject properties = policyVal.get("properties").getAsJsonObject();
        assertNotNull(properties.get("tca_policy"));
    }

    private void verifyVfwMonitoringOutputserialization(String serializedServiceTemplate) {

        JsonObject serviceTemplateJsonObject = new JsonParser().parse(serializedServiceTemplate).getAsJsonObject();
        assertEquals("tosca_simple_yaml_1_0_0", serviceTemplateJsonObject.get("tosca_definitions_version")
                .getAsString());
        JsonObject topologyTemplateJsonObject = serviceTemplateJsonObject.get("topology_template").getAsJsonObject();
        JsonArray policiesJsonArray = topologyTemplateJsonObject.get("policies").getAsJsonArray();
        assertTrue(policiesJsonArray.size() == 1);
        JsonObject policy = policiesJsonArray.iterator().next().getAsJsonObject();
        assertNotNull(policy.get("onap.vfirewall.tca"));
        JsonObject policyVal = policy.get("onap.vfirewall.tca").getAsJsonObject();
        assertEquals("onap.policy.monitoring.cdap.tca.hi.lo.app", policyVal.get("type").getAsString());
        assertEquals("1.0.0", policyVal.get("version").getAsString());
        assertEquals("onap.vfirewall.tca", policyVal.get("metadata").getAsJsonObject().get("policy-id")
                .getAsString());
        JsonObject properties = policyVal.get("properties").getAsJsonObject();
        assertNotNull(properties.get("tca_policy"));
    }
}
