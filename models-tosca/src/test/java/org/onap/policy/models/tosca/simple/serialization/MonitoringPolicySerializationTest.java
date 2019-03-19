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
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
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
 * @author Chenfei Gao (cgao@research.att.com
 */
public class MonitoringPolicySerializationTest {
    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringPolicySerializationTest.class);

    private Gson gson;

    @Before
    public void setUp() {
        gson = new ToscaServiceTemplateMessageBodyHandler().getGson();
    }

    @Test
    public void testDeserialization() {
        try {
            // vCPE monitoring
            ToscaServiceTemplate serviceTemplateFromJson = deserializeVcpeMonitoringInputJson();
            verifyVcpeMonitoringInputDeserialization(serviceTemplateFromJson);
            ToscaServiceTemplate serviceTemplateFromYaml = deserializeVcpeMonitoringInputYaml();
            assertTrue(serviceTemplateFromJson.compareTo(serviceTemplateFromYaml) == 0);
        } catch (Exception e) {
            fail("No exception should be thrown");
        }
    }

    @Test
    public void testSerialization() {
        //TODO
    }

    private ToscaServiceTemplate deserializeVcpeMonitoringInputJson()
            throws JsonSyntaxException, IOException {

        String vcpePolicyJson = ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.json");
        ToscaServiceTemplate serviceTemplate = gson.fromJson(vcpePolicyJson, ToscaServiceTemplate.class);
        return serviceTemplate;
    }

    private ToscaServiceTemplate deserializeVcpeMonitoringInputYaml() throws JsonSyntaxException, IOException {
        Yaml yaml = new Yaml();
        String vcpePolicyYaml = ResourceUtils.getResourceAsString("policies/vCPE.policy.monitoring.input.tosca.yaml");
        Object yamlObject = yaml.load(vcpePolicyYaml);
        String yamlAsJsonString = new Gson().toJson(yamlObject);
        ToscaServiceTemplate serviceTemplate = gson.fromJson(yamlAsJsonString, ToscaServiceTemplate.class);
        return serviceTemplate;
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
        assertTrue(policyVal.getProperties().values().iterator().next() instanceof JsonElement);
    }
}
