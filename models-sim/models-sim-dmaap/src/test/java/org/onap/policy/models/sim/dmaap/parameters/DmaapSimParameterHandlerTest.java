/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.sim.dmaap.parameters;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.cmd.CommandLineException;
import org.onap.policy.models.sim.dmaap.DmaapSimException;
import org.onap.policy.models.sim.dmaap.startstop.DmaapSimCommandLineArguments;

public class DmaapSimParameterHandlerTest {

    private static final String RESOURCE_DIR = "src/test/resources/parameters/";

    private DmaapSimParameterHandler handler;

    @Before
    public void setUp() {
        handler = new DmaapSimParameterHandler();
    }

    @Test
    public void testGetParameters() throws DmaapSimException, CommandLineException {
        final DmaapSimCommandLineArguments args = new DmaapSimCommandLineArguments();

        args.parse(new String[] {"-c", RESOURCE_DIR + "NormalParameters.json"});
        DmaapSimParameterGroup params = handler.getParameters(args);
        assertNotNull(params);
        assertEquals("DMaapSim", params.getName());
        assertEquals(300L, params.getTopicSweepSec());
        assertEquals(6845, params.getRestServerParameters().getPort());


        args.parse(new String[] {"-c", "FileNotFound.json"});
        assertThatThrownBy(() -> handler.getParameters(args)).isInstanceOf(DmaapSimException.class)
                        .hasMessageStartingWith("error reading parameters");


        args.parse(new String[] {"-c", RESOURCE_DIR + "EmptyParameterFile.json"});
        assertThatThrownBy(() -> handler.getParameters(args)).isInstanceOf(DmaapSimException.class)
                        .hasMessageStartingWith("no parameters found");


        args.parse(new String[] {"-c", RESOURCE_DIR + "Parameters_InvalidName.json"});
        assertThatThrownBy(() -> handler.getParameters(args)).isInstanceOf(DmaapSimException.class)
                        .hasMessageContaining("validation error");
    }
}
