/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.sim.pdp.comm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.message.bus.event.Topic.CommInfrastructure;
import org.onap.policy.common.utils.services.Registry;
import org.onap.policy.models.pdp.concepts.PdpStateChange;
import org.onap.policy.models.pdp.concepts.PdpStatus;
import org.onap.policy.models.pdp.concepts.PdpUpdate;
import org.onap.policy.models.pdp.enums.PdpState;
import org.onap.policy.models.sim.pdp.PdpSimulatorActivator;
import org.onap.policy.models.sim.pdp.PdpSimulatorCommandLineArguments;
import org.onap.policy.models.sim.pdp.PdpSimulatorConstants;
import org.onap.policy.models.sim.pdp.parameters.PdpSimulatorParameterGroup;
import org.onap.policy.models.sim.pdp.parameters.PdpSimulatorParameterHandler;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * Class to perform unit test of {@link PdpStateChangeListener}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
class TestPdpStateChangeListener {
    private static final String PDP_SUBGROUP = "pdpSubgroup";
    private static final String PDP_GROUP = "pdpGroup";
    private PdpUpdateListener pdpUpdateMessageListener;
    private PdpStateChangeListener pdpStateChangeListener;
    private static final CommInfrastructure INFRA = CommInfrastructure.NOOP;
    private static final String TOPIC = "my-topic";
    private PdpSimulatorActivator activator;

    /**
     * Method for setup before each test.
     *
     * @throws Exception if an error occurs
     */
    @BeforeEach
    void setUp() throws Exception {
        pdpUpdateMessageListener = new PdpUpdateListener();
        pdpStateChangeListener = new PdpStateChangeListener();
        Registry.newRegistry();
        final String[] pdpSimulatorConfigParameters = { "-c", "src/test/resources/PdpSimulatorConfigParameters.json" };
        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();
        PdpSimulatorParameterGroup pdpSimulatorParameterGroup;
        // The arguments return a string if there is a message to print and we should
        // exit
        final String argumentMessage = arguments.parse(pdpSimulatorConfigParameters);
        if (argumentMessage != null) {
            return;
        }
        // Validate that the arguments are sane
        arguments.validate();

        // Read the parameters
        pdpSimulatorParameterGroup = new PdpSimulatorParameterHandler().getParameters(arguments);

        activator = new PdpSimulatorActivator(pdpSimulatorParameterGroup);
        Registry.register(PdpSimulatorConstants.REG_PDP_SIMULATOR_ACTIVATOR, activator);
        activator.initialize();
    }

    /**
     * Method for cleanup after each test.
     *
     * @throws Exception if an error occurs
     */
    @AfterEach
    void teardown() throws Exception {

        // clear the pdp simulator activator
        if (activator != null && activator.isAlive()) {
            activator.terminate();
        }
    }

    /**
     * Method to initiate a PdpUpdate.
     *
     * @param instance the instance id
     * @return PdpUpdate the pdp update message
     */
    private PdpUpdate performPdpUpdate(final String instance) {
        final PdpUpdate pdpUpdateMsg = new PdpUpdate();
        pdpUpdateMsg.setDescription("dummy pdp status for test");
        pdpUpdateMsg.setPdpGroup(PDP_GROUP);
        pdpUpdateMsg.setPdpSubgroup(PDP_SUBGROUP);
        pdpUpdateMsg.setName(instance);
        final ToscaPolicy toscaPolicy = new ToscaPolicy();
        toscaPolicy.setType("apexpolicytype");
        toscaPolicy.setVersion("1.0");
        final Map<String, Object> propertiesMap = new LinkedHashMap<>();

        String properties;
        try {
            properties = Files.readString(Paths.get("src\\test\\resources\\dummyProperties.json"));
            propertiesMap.put("content", properties);
        } catch (final IOException e) {
            propertiesMap.put("content", "");
        }
        toscaPolicy.setProperties(propertiesMap);
        final List<ToscaPolicy> toscaPolicies = new ArrayList<>();
        toscaPolicies.add(toscaPolicy);
        pdpUpdateMsg.setPoliciesToBeDeployed(toscaPolicies);
        pdpUpdateMessageListener.onTopicEvent(INFRA, TOPIC, null, pdpUpdateMsg);
        return pdpUpdateMsg;
    }

    @Test
    void testPdpStateChangeMessageListener_passivetopassive() {
        final PdpStatus pdpStatus = Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_OBJECT);
        performPdpUpdate(pdpStatus.getName());
        final PdpStateChange pdpStateChangeMsg = new PdpStateChange();
        pdpStateChangeMsg.setState(PdpState.PASSIVE);
        pdpStateChangeMsg.setPdpGroup(PDP_GROUP);
        pdpStateChangeMsg.setPdpSubgroup(PDP_SUBGROUP);
        pdpStateChangeMsg.setName(pdpStatus.getName());
        pdpStateChangeListener.onTopicEvent(INFRA, TOPIC, null, pdpStateChangeMsg);

        assertEquals(pdpStateChangeMsg.getState(), pdpStatus.getState());
    }

    @Test
    void testPdpStateChangeMessageListener_activetoactive() {
        final PdpStatus pdpStatus = Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_OBJECT);
        performPdpUpdate(pdpStatus.getName());
        pdpStatus.setState(PdpState.ACTIVE);
        final PdpStateChange pdpStateChangeMsg = new PdpStateChange();
        pdpStateChangeMsg.setState(PdpState.ACTIVE);
        pdpStateChangeMsg.setPdpGroup(PDP_GROUP);
        pdpStateChangeMsg.setPdpSubgroup(PDP_SUBGROUP);
        pdpStateChangeMsg.setName(pdpStatus.getName());
        pdpStateChangeListener.onTopicEvent(INFRA, TOPIC, null, pdpStateChangeMsg);

        assertEquals(pdpStatus.getState(), pdpStateChangeMsg.getState());
    }
}
