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

package org.onap.policy.models.sim.pdp.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;
import org.onap.policy.common.endpoints.parameters.TopicParameterGroup;
import org.onap.policy.common.parameters.GroupValidationResult;

/**
 * Class to perform unit test of {@link PdpSimulatorParameterGroup}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class TestPdpSimulatorParameterGroup {
    CommonTestData commonTestData = new CommonTestData();

    @Test
    public void testPdpSimulatorParameterGroup_Named() {
        final PdpSimulatorParameterGroup pdpSimulatorParameters = new PdpSimulatorParameterGroup("my-name");
        assertEquals("my-name", pdpSimulatorParameters.getName());
    }

    @Test
    public void testPdpSimulatorParameterGroup() {
        final PdpSimulatorParameterGroup pdpSimulatorParameters = commonTestData.toObject(
                commonTestData.getPdpSimulatorParameterGroupMap(CommonTestData.PDP_SIMULATOR_GROUP_NAME),
                PdpSimulatorParameterGroup.class);
        final PdpStatusParameters pdpStatusParameters = pdpSimulatorParameters.getPdpStatusParameters();
        final TopicParameterGroup topicParameterGroup  = pdpSimulatorParameters.getTopicParameterGroup();
        final GroupValidationResult validationResult = pdpSimulatorParameters.validate();
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
    public void testPdpSimulatorParameterGroup_NullName() {
        final PdpSimulatorParameterGroup pdpSimulatorParameters = commonTestData
                .toObject(commonTestData.getPdpSimulatorParameterGroupMap(null), PdpSimulatorParameterGroup.class);
        final GroupValidationResult validationResult = pdpSimulatorParameters.validate();
        assertFalse(validationResult.isValid());
        assertEquals(null, pdpSimulatorParameters.getName());
        assertTrue(validationResult.getResult().contains("is null"));
    }

    @Test
    public void testPdpSimulatorParameterGroup_EmptyName() {
        final PdpSimulatorParameterGroup pdpSimulatorParameters = commonTestData
                .toObject(commonTestData.getPdpSimulatorParameterGroupMap(""), PdpSimulatorParameterGroup.class);
        final GroupValidationResult validationResult = pdpSimulatorParameters.validate();
        assertFalse(validationResult.isValid());
        assertEquals("", pdpSimulatorParameters.getName());
        assertTrue(validationResult.getResult().contains(
                "field \"name\" type \"java.lang.String\" value \"\" INVALID, " + "must be a non-blank string"));
    }

    @Test
    public void testPdpSimulatorParameterGroup_SetName() {
        final PdpSimulatorParameterGroup pdpSimulatorParameters = commonTestData.toObject(
                commonTestData.getPdpSimulatorParameterGroupMap(CommonTestData.PDP_SIMULATOR_GROUP_NAME),
                PdpSimulatorParameterGroup.class);
        pdpSimulatorParameters.setName("PdpSimulatorNewGroup");
        final GroupValidationResult validationResult = pdpSimulatorParameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals("PdpSimulatorNewGroup", pdpSimulatorParameters.getName());
    }

    @Test
    public void testPdpSimulatorParameterGroup_EmptyPdpStatusParameters() {
        final Map<String, Object> map =
                commonTestData.getPdpSimulatorParameterGroupMap(CommonTestData.PDP_SIMULATOR_GROUP_NAME);
        map.put("pdpStatusParameters", commonTestData.getPdpStatusParametersMap(true));
        final PdpSimulatorParameterGroup pdpSimulatorParameters =
                commonTestData.toObject(map, PdpSimulatorParameterGroup.class);
        final GroupValidationResult validationResult = pdpSimulatorParameters.validate();
        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getResult()
                .contains("\"org.onap.policy.models.sim.pdp.parameters.PdpSimulatorParameterGroup\" INVALID, "
                        + "parameter group has status INVALID"));
    }

    @Test
    public void testApexStarterParameterGroupp_EmptyTopicParameters() {
        final Map<String, Object> map =
                commonTestData.getPdpSimulatorParameterGroupMap(CommonTestData.PDP_SIMULATOR_GROUP_NAME);
        map.put("topicParameterGroup", commonTestData.getTopicParametersMap(true));

        final PdpSimulatorParameterGroup parGroup =
                commonTestData.toObject(map, PdpSimulatorParameterGroup.class);
        final GroupValidationResult validationResult = parGroup.validate();
        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getResult()
                .contains("\"org.onap.policy.common.endpoints.parameters.TopicParameterGroup\" INVALID, "
                        + "parameter group has status INVALID"));
    }

}
