/*
 * ============LICENSE_START=======================================================
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property.
 * Modifications Copyright (C) 2021 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.models.sim.dmaap.parameters.CommonTestData;
import org.onap.policy.models.sim.dmaap.rest.CommonRestServer;

/**
 * Class to perform unit test of {@link Main}}.
 *
 * @author Ram Krishna Verma (ram.krishna.verma@est.tech)
 */
public class MainTest extends CommonRestServer {
    private Main main;

    /**
     * Sets up.
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
    public void testMain() throws Exception {
        CommonRestServer.reconfigure();
        final String[] NormalParameters = {"-c", CONFIG_FILE};
        main = new Main(NormalParameters);
        assertTrue(main.getParameters().isValid());
        assertEquals(CommonTestData.SIM_GROUP_NAME, main.getParameters().getName());

        main.shutdown();
    }

    @Test
    public void testMain_NoArguments() {
        final String[] NormalParameters = {};
        main = new Main(NormalParameters);
        assertNull(main.getParameters());
    }

    @Test
    public void testMain_InvalidArguments() throws Exception {
        CommonRestServer.reconfigure();

        // note: this is missing the "-c" argument, thus the ARGUMENTS are invalid
        final String[] NormalParameters = {CONFIG_FILE};
        main = new Main(NormalParameters);
        assertNull(main.getParameters());
    }

    @Test
    public void testMain_Help() {
        final String[] NormalParameters = {"-h"};
        assertThatCode(() -> Main.main(NormalParameters)).doesNotThrowAnyException();
    }

    @Test
    public void testMain_InvalidParameters() {
        final String[] NormalParameters = {"-c", "parameters/InvalidParameters.json"};
        main = new Main(NormalParameters);
        assertNull(main.getParameters());
    }

    @Test
    public void testDmaapSimVersion() {
        String[] testArgs = {"-v"};
        DmaapSimCommandLineArguments sutArgs = new DmaapSimCommandLineArguments(testArgs);
        assertThat(sutArgs.version()).startsWith("ONAP DMaaP simulator Service");
    }
}
