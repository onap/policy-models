/*
 * ============LICENSE_START=======================================================
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property.
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

package org.onap.policy.sim.dmaap.startstop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.models.sim.dmaap.DmaapSimException;
import org.onap.policy.models.sim.dmaap.startstop.Main;
import org.onap.policy.sim.dmaap.parameters.CommonTestData;

/**
 * Class to perform unit test of {@link Main}}.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
public class MainTest {
    private Main main;

    /**
     * Set up.
     */
    @Before
    public void setUp() {
        main = null;
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    /**
     * Shuts "main" down.
     *
     * @throws Exception if an error occurs
     */
    @After
    public void tearDown() throws Exception {
        if (main != null) {
            main.shutdown();
        }
    }

    @Test
    public void testMain() throws DmaapSimException {
        final String[] NormalParameters = {"-c", "parameters/NormalParameters.json"};
        main = new Main(NormalParameters);
        assertTrue(main.getParameters().isValid());
        assertEquals(CommonTestData.SIM_GROUP_NAME, main.getParameters().getName());

        main.shutdown();
    }

    @Test
    public void testMain_NoArguments() {
        final String[] NormalParameters = {};
        main = new Main(NormalParameters);
        assertTrue(main.getParameters() == null);
    }

    @Test
    public void testMain_InvalidArguments() {
        // note: this is missing the "-c" argument, thus the ARGUMENTS are invalid
        final String[] NormalParameters = {"parameters/NormalParameters.json"};
        main = new Main(NormalParameters);
        assertTrue(main.getParameters() == null);
    }

    @Test
    public void testMain_Help() {
        final String[] NormalParameters = {"-h"};
        Main.main(NormalParameters);
    }

    @Test
    public void testMain_InvalidParameters() {
        final String[] NormalParameters = {"-c", "parameters/InvalidParameters.json"};
        main = new Main(NormalParameters);
        assertTrue(main.getParameters() == null);
    }
}
