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

package org.onap.policy.models.sim.pdp;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.services.Registry;
import org.onap.policy.models.pdp.concepts.PdpStatus;
import org.onap.policy.models.sim.pdp.exception.PdpSimulatorException;
import org.onap.policy.models.sim.pdp.parameters.CommonTestData;
import org.onap.policy.models.sim.pdp.parameters.PdpSimulatorParameterGroup;
import org.onap.policy.models.sim.pdp.parameters.PdpSimulatorParameterHandler;

/**
 * Class to perform unit test of {@link PdpSimulatorActivator}}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
class TestPdpSimulatorActivator {

    private PdpSimulatorActivator activator;

    /**
     * Initializes an activator.
     *
     * @throws Exception if an error occurs
     */
    @BeforeEach
    void setUp() throws Exception {
        Registry.newRegistry();
        final String[] pdpSimulatorConfigParameters = { "-c", "src/test/resources/PdpSimulatorConfigParameters.json" };
        final PdpSimulatorCommandLineArguments arguments =
                new PdpSimulatorCommandLineArguments(pdpSimulatorConfigParameters);
        final PdpSimulatorParameterGroup parGroup = new PdpSimulatorParameterHandler().getParameters(arguments);

        activator = new PdpSimulatorActivator(parGroup);
    }

    /**
     * Method for cleanup after each test.
     *
     * @throws Exception if an error occurs
     */
    @AfterEach
    void teardown() throws Exception {
        if (activator != null && activator.isAlive()) {
            activator.terminate();
        }
    }

    @Test
    void testPdpSimulatorActivator() throws PdpSimulatorException {
        assertFalse(activator.isAlive());
        activator.initialize();
        assertTrue(activator.isAlive());
        assertTrue(activator.getParameterGroup().isValid());
        assertEquals(CommonTestData.PDP_SIMULATOR_GROUP_NAME, activator.getParameterGroup().getName());

        // ensure items were added to the registry
        assertNotNull(Registry.get(PdpSimulatorConstants.REG_PDP_STATUS_OBJECT, PdpStatus.class));

        // repeat - should throw an exception
        assertThatIllegalStateException().isThrownBy(() -> activator.initialize());
        assertTrue(activator.isAlive());
        assertTrue(activator.getParameterGroup().isValid());
    }

    @Test
    void testTerminate() throws Exception {
        activator.initialize();
        activator.terminate();
        assertFalse(activator.isAlive());

        // ensure items have been removed from the registry
        assertNull(Registry.getOrDefault(PdpSimulatorConstants.REG_PDP_STATUS_OBJECT, PdpStatus.class, null));

        // repeat - should throw an exception
        assertThatIllegalStateException().isThrownBy(() -> activator.terminate());
        assertFalse(activator.isAlive());
    }
}
