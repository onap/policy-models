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

package org.onap.policy.models.sim.pdp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.services.Registry;
import org.onap.policy.models.sim.pdp.exception.PdpSimulatorException;
import org.onap.policy.models.sim.pdp.parameters.CommonTestData;

/**
 * Class to perform unit test of {@link PdpSimulatorMain}}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class TestPdpSimulatorMain {
    private PdpSimulatorMain pdpSimulator;

    /**
     * Set up.
     */
    @Before
    public void setUp() {
        Registry.newRegistry();
    }

    /**
     * Shuts "main" down.
     *
     * @throws Exception if an error occurs
     */
    @After
    public void tearDown() throws Exception {
        // shut down activator
        final PdpSimulatorActivator activator = Registry.getOrDefault(PdpSimulatorConstants.REG_PDP_SIMULATOR_ACTIVATOR,
                PdpSimulatorActivator.class, null);
        if (activator != null && activator.isAlive()) {
            activator.terminate();
        }
    }

    @Test
    public void testPdpSimulator() throws PdpSimulatorException {
        final String[] pdpSimulatorConfigParameters = { "-c", "src/test/resources/PdpSimulatorConfigParameters.json" };
        pdpSimulator = new PdpSimulatorMain(pdpSimulatorConfigParameters);
        assertTrue(pdpSimulator.getParameters().isValid());
        assertEquals(CommonTestData.PDP_SIMULATOR_GROUP_NAME, pdpSimulator.getParameters().getName());

        // ensure items were added to the registry
        assertNotNull(Registry.get(PdpSimulatorConstants.REG_PDP_SIMULATOR_ACTIVATOR, PdpSimulatorActivator.class));

        pdpSimulator.shutdown();
    }

    @Test
    public void testPdpSimulator_NoArguments() {
        final String[] pdpSimulatorConfigParameters = {};
        pdpSimulator = new PdpSimulatorMain(pdpSimulatorConfigParameters);
        assertTrue(pdpSimulator.getParameters() == null);
    }

    @Test
    public void testPdpSimulator_InvalidArguments() {
        final String[] pdpSimulatorConfigParameters = { "src/test/resourcesPdpSimulatorConfigParameters.json" };
        pdpSimulator = new PdpSimulatorMain(pdpSimulatorConfigParameters);
        assertTrue(pdpSimulator.getParameters() == null);
    }

    @Test
    public void testPdpSimulator_Help() {
        final String[] pdpSimulatorConfigParameters = { "-h" };
        PdpSimulatorMain.main(pdpSimulatorConfigParameters);
    }

    @Test
    public void testPdpSimulator_InvalidParameters() {
        final String[] pdpSimulatorConfigParameters =
            { "-c", "src/test/resources/PdpSimulatorConfigParameters_InvalidName.json" };
        pdpSimulator = new PdpSimulatorMain(pdpSimulatorConfigParameters);
        assertTrue(pdpSimulator.getParameters() == null);
    }
}
