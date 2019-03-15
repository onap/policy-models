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

package org.onap.policy.models.tosca.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.resources.TextFileUtils;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.serialization.simple.ToscaServiceTemplateMessageBodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Test serialization of monitoring policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
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
    public void testJsonDeserialization() throws JsonSyntaxException, IOException {
        ToscaServiceTemplate serviceTemplate = gson.fromJson(
                TextFileUtils
                        .getTextFileAsString("src/test/resources/policies/vCPE.policy.monitoring.input.tosca.json"),
                ToscaServiceTemplate.class);

        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        assertEquals("onap.vcpe.tca:1.0.0",
                serviceTemplate.getTopologyTemplate().getPolicies().get("onap.vcpe.tca").getId());
        assertEquals("onap.vcpe.tca:1.0.0",
                serviceTemplate.getTopologyTemplate().getPolicies().get("onap.vcpe.tca").getId());
    }

    @Test
    public void testYamlDeserialization() throws JsonSyntaxException, IOException {
        Yaml yaml = new Yaml();
        Object yamlObject = yaml.load(TextFileUtils
                .getTextFileAsString("src/test/resources/policies/vCPE.policy.monitoring.input.tosca.yaml"));

        String yamlAsJsonString = new Gson().toJson(yamlObject);

        ToscaServiceTemplate serviceTemplate = gson.fromJson(yamlAsJsonString, ToscaServiceTemplate.class);

        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        assertEquals("onap.vcpe.tca:1.0.0",
                serviceTemplate.getTopologyTemplate().getPolicies().get("onap.vcpe.tca").getId());
        assertEquals("onap.vcpe.tca:1.0.0",
                serviceTemplate.getTopologyTemplate().getPolicies().get("onap.vcpe.tca").getId());
    }
}
