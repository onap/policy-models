/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.sim.pdp.parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.parameters.topic.TopicParameterGroup;

/**
 * Class to perform unit test of {@link PdpSimulatorParameterGroup}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
class TestPdpSimulatorParameterGroup {
    CommonTestData commonTestData = new CommonTestData();

    @Test
    void testPdpSimulatorParameterGroup_Named() {
        final PdpSimulatorParameterGroup pdpSimulatorParameters = new PdpSimulatorParameterGroup("my-name");
        assertEquals("my-name", pdpSimulatorParameters.getName());
    }

    @Test
    void testPdpSimulatorParameterGroup() {
        final PdpSimulatorParameterGroup pdpSimulatorParameters = commonTestData.toObject(
                commonTestData.getPdpSimulatorParameterGroupMap(CommonTestData.PDP_SIMULATOR_GROUP_NAME),
                PdpSimulatorParameterGroup.class);
        final PdpStatusParameters pdpStatusParameters = pdpSimulatorParameters.getPdpStatusParameters();
        final TopicParameterGroup topicParameterGroup  = pdpSimulatorParameters.getTopicParameterGroup();
        final ValidationResult validationResult = pdpSimulatorParameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(CommonTestData.PDP_SIMULATOR_GROUP_NAME, pdpSimulatorParameters.getName());
        assertEquals(CommonTestData.TIME_INTERVAL, pdpStatusParameters.getTimeIntervalMs());
        assertEquals(CommonTestData.PDP_TYPE, pdpStatusParameters.getPdpType());
        assertEquals(CommonTestData.DESCRIPTION, pdpStatusParameters.getDescription());
        assertEquals(CommonTestData.SUPPORTED_POLICY_TYPES, pdpStatusParameters.getSupportedPolicyTypes());
        assertEquals(CommonTestData.TOPIC_PARAMS, topicParameterGroup.getTopicSinks());
        assertEquals(CommonTestData.TOPIC_PARAMS, topicParameterGroup.getTopicSources());
    }

    @Test
    void testPdpSimulatorParameterGroup_NullName() {
        final PdpSimulatorParameterGroup pdpSimulatorParameters = commonTestData
                .toObject(commonTestData.getPdpSimulatorParameterGroupMap(null), PdpSimulatorParameterGroup.class);
        final ValidationResult validationResult = pdpSimulatorParameters.validate();
        assertFalse(validationResult.isValid());
        assertNull(pdpSimulatorParameters.getName());
        assertTrue(validationResult.getResult().contains("is null"));
    }

    @Test
    void testPdpSimulatorParameterGroup_EmptyName() {
        final PdpSimulatorParameterGroup pdpSimulatorParameters = commonTestData
                .toObject(commonTestData.getPdpSimulatorParameterGroupMap(""), PdpSimulatorParameterGroup.class);
        final ValidationResult validationResult = pdpSimulatorParameters.validate();
        assertFalse(validationResult.isValid());
        assertEquals("", pdpSimulatorParameters.getName());
        assertThat(validationResult.getResult()).contains(
                "\"name\" value \"\" INVALID, " + "is blank");
    }

    @Test
    void testPdpSimulatorParameterGroup_SetName() {
        final PdpSimulatorParameterGroup pdpSimulatorParameters = commonTestData.toObject(
                commonTestData.getPdpSimulatorParameterGroupMap(CommonTestData.PDP_SIMULATOR_GROUP_NAME),
                PdpSimulatorParameterGroup.class);
        pdpSimulatorParameters.setName("PdpSimulatorNewGroup");
        final ValidationResult validationResult = pdpSimulatorParameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals("PdpSimulatorNewGroup", pdpSimulatorParameters.getName());
    }

    @Test
    void testPdpSimulatorParameterGroup_EmptyPdpStatusParameters() {
        final Map<String, Object> map =
                commonTestData.getPdpSimulatorParameterGroupMap(CommonTestData.PDP_SIMULATOR_GROUP_NAME);
        map.put("pdpStatusParameters", commonTestData.getPdpStatusParametersMap(true));
        final PdpSimulatorParameterGroup pdpSimulatorParameters =
                commonTestData.toObject(map, PdpSimulatorParameterGroup.class);
        final ValidationResult validationResult = pdpSimulatorParameters.validate();
        assertFalse(validationResult.isValid());
        assertThat(validationResult.getResult())
                .contains("\"PdpSimulatorParameterGroup\" INVALID, item has status INVALID");
    }

    @Test
    void testApexStarterParameterGroupp_EmptyTopicParameters() {
        final Map<String, Object> map =
                commonTestData.getPdpSimulatorParameterGroupMap(CommonTestData.PDP_SIMULATOR_GROUP_NAME);
        map.put("topicParameterGroup", commonTestData.getTopicParametersMap(true));

        final PdpSimulatorParameterGroup parGroup =
                commonTestData.toObject(map, PdpSimulatorParameterGroup.class);
        final ValidationResult validationResult = parGroup.validate();
        assertFalse(validationResult.isValid());
        assertThat(validationResult.getResult())
                .contains("\"TopicParameterGroup\" INVALID, item has status INVALID");
    }

}
