/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021, 2024 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import org.junit.Test;
import org.onap.policy.common.utils.cmd.CommandLineException;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.models.sim.pdp.PdpSimulatorCommandLineArguments;
import org.onap.policy.models.sim.pdp.exception.PdpSimulatorException;

/**
 * Class to perform unit test of {@link PdpSimulatorParameterHandler}.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class TestPdpSimulatorParameterHandler {

    @Test
    public void testParameterHandlerNoParameterFile() throws CommandLineException {
        final String[] emptyArgumentString = { "-c", "src/test/resources/NoParametersFile.json" };

        final PdpSimulatorCommandLineArguments emptyArguments = new PdpSimulatorCommandLineArguments();
        emptyArguments.parse(emptyArgumentString);

        try {
            new PdpSimulatorParameterHandler().getParameters(emptyArguments);
            fail("test should throw an exception here");
        } catch (final Exception e) {
            assertTrue(e.getCause() instanceof CoderException);
            assertTrue(e.getCause().getCause() instanceof FileNotFoundException);
        }
    }

    @Test
    public void testParameterHandlerEmptyParameters() throws CommandLineException {
        final String[] noArgumentString = { "-c", "src/test/resources/NoParameters.json" };

        final PdpSimulatorCommandLineArguments noArguments = new PdpSimulatorCommandLineArguments();
        noArguments.parse(noArgumentString);

        assertThatThrownBy(() -> new PdpSimulatorParameterHandler().getParameters(noArguments))
                        .hasMessageContaining("no parameters found");
    }

    @Test
    public void testParameterHandlerInvalidParameters() throws CommandLineException {
        final String[] invalidArgumentString = { "-c", "src/test/resources/InvalidParameters.json" };

        final PdpSimulatorCommandLineArguments invalidArguments = new PdpSimulatorCommandLineArguments();
        invalidArguments.parse(invalidArgumentString);

        assertThatThrownBy(() -> new PdpSimulatorParameterHandler().getParameters(invalidArguments))
                        .hasMessageStartingWith("error reading parameters from")
                        .hasCauseInstanceOf(CoderException.class);
    }

    @Test
    public void testParameterHandlerNoParameters() throws CommandLineException {
        final String[] noArgumentString = { "-c", "src/test/resources/EmptyConfigParameters.json" };

        final PdpSimulatorCommandLineArguments noArguments = new PdpSimulatorCommandLineArguments();
        noArguments.parse(noArgumentString);

        assertThatThrownBy(() -> new PdpSimulatorParameterHandler().getParameters(noArguments))
                        .hasMessageContaining("is null");
    }

    @Test
    public void testPdpSimulatorParameterGroup() throws PdpSimulatorException, CommandLineException {
        final String[] pdpSimulatorConfigParameters = { "-c", "src/test/resources/PdpSimulatorConfigParameters.json" };

        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();
        arguments.parse(pdpSimulatorConfigParameters);

        final PdpSimulatorParameterGroup parGroup = new PdpSimulatorParameterHandler().getParameters(arguments);
        assertTrue(arguments.checkSetConfigurationFilePath());
        assertEquals(CommonTestData.PDP_SIMULATOR_GROUP_NAME, parGroup.getName());
    }

    @Test
    public void testPdpSimulatorParameterGroup_InvalidName() throws CommandLineException {
        final String[] pdpSimulatorConfigParameters = {"-c",
            "src/test/resources/PdpSimulatorConfigParameters_InvalidName.json"};

        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();
        arguments.parse(pdpSimulatorConfigParameters);

        assertThatThrownBy(() -> new PdpSimulatorParameterHandler().getParameters(arguments)).hasMessageContaining(
                        "\"name\" value \" \" INVALID, is blank");
    }

    @Test
    public void testPdpSimulatorVersion() throws CommandLineException {
        final String[] pdpSimulatorConfigParameters = { "-v" };
        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();
        final String version = arguments.parse(pdpSimulatorConfigParameters);
        assertTrue(version.startsWith("ONAP Policy-PDP simulator Service"));
    }

    @Test
    public void testPdpSimulatorHelp() throws CommandLineException {
        final String[] pdpSimulatorConfigParameters = { "-h" };
        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();
        final String help = arguments.parse(pdpSimulatorConfigParameters);
        assertTrue(help.startsWith("usage:"));
    }

    @Test
    public void testPdpSimulatorInvalidOption() {
        final String[] pdpSimulatorConfigParameters = { "-d" };
        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();

        assertThatThrownBy(() -> arguments.parse(pdpSimulatorConfigParameters))
                        .hasMessageStartingWith("invalid command line arguments specified");
    }

    @Test
    public void testPdpSimulatorProperty() throws CommandLineException {
        final String[] pdpSimulatorConfigParameters = { "-p", "dummyProperties.json" };
        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();
        arguments.parse(pdpSimulatorConfigParameters);
        assertTrue(arguments.checkSetPropertyFilePath());
    }
}
