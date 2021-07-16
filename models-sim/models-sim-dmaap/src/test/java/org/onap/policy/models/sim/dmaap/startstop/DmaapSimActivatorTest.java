/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2020 AT&T Intellectual Property.
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

package org.onap.policy.models.sim.dmaap.startstop;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.utils.services.Registry;
import org.onap.policy.models.sim.dmaap.parameters.DmaapSimParameterGroup;
import org.onap.policy.models.sim.dmaap.parameters.DmaapSimParameterHandler;
import org.onap.policy.models.sim.dmaap.rest.CommonRestServer;


/**
 * Class to perform unit test of {@link DmaapSimActivator}}.
 */
public class DmaapSimActivatorTest extends CommonRestServer {

    private DmaapSimActivator activator;

    /**
     * Initializes an activator.
     *
     * @throws Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception {
        Registry.newRegistry();
        HttpServletServerFactoryInstance.getServerFactory().destroy();

        CommonRestServer.reconfigure();

        final String[] papConfigParameters = {"-c", CONFIG_FILE};
        final DmaapSimCommandLineArguments arguments = new DmaapSimCommandLineArguments(papConfigParameters);
        final DmaapSimParameterGroup parGroup = new DmaapSimParameterHandler().getParameters(arguments);

        activator = new DmaapSimActivator(parGroup);
    }

    /**
     * Method for cleanup after each test.
     *
     * @throws Exception if an error occurs
     */
    @After
    public void teardown() throws Exception {
        if (activator != null && activator.isAlive()) {
            activator.stop();
        }
    }

    @Test
    public void testDmaapSimActivator() {
        assertFalse(activator.isAlive());
        activator.start();
        assertTrue(activator.isAlive());

        // repeat - should throw an exception
        assertThatIllegalStateException().isThrownBy(() -> activator.start());
        assertTrue(activator.isAlive());
    }

    @Test
    public void testTerminate() {
        activator.start();
        activator.stop();
        assertFalse(activator.isAlive());

        // repeat - should throw an exception
        assertThatIllegalStateException().isThrownBy(() -> activator.stop());
        assertFalse(activator.isAlive());
    }
}
