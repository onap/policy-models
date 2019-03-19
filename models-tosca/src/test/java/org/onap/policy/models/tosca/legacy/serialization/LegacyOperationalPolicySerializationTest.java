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

package org.onap.policy.models.tosca.legacy.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
import org.onap.policy.models.tosca.legacy.mapping.LegacyOperationalPolicyMapper;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test serialization of monitoring policies.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class LegacyOperationalPolicySerializationTest {
    // Logger for this class
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyOperationalPolicySerializationTest.class);

    private Gson gson;

    @Before
    public void setUp() {
        gson = new Gson();
    }

    @Test
    public void testJsonDeserialization() throws JsonSyntaxException, IOException {
        String vcpePolicyJson = ResourceUtils.getResourceAsString("policies/vCPE.policy.operational.input.json");

        LegacyOperationalPolicy legacyOperationalPolicy = gson.fromJson(vcpePolicyJson, LegacyOperationalPolicy.class);

        ToscaServiceTemplate serviceTemplate =
                new LegacyOperationalPolicyMapper().toToscaServiceTemplate(legacyOperationalPolicy);

        assertNotNull(serviceTemplate);
        LOGGER.info(serviceTemplate.validate(new PfValidationResult()).toString());
        assertTrue(serviceTemplate.validate(new PfValidationResult()).isValid());

        assertEquals("operational.restart:1.0.1",
                serviceTemplate.getTopologyTemplate().getPolicies().get("operational.restart").getId());
    }
}
