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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;

import org.junit.Test;
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
    public void testParameterHandlerNoParameterFile() throws PdpSimulatorException {
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
    public void testParameterHandlerEmptyParameters() throws PdpSimulatorException {
        final String[] noArgumentString = { "-c", "src/test/resources/NoParameters.json" };

        final PdpSimulatorCommandLineArguments noArguments = new PdpSimulatorCommandLineArguments();
        noArguments.parse(noArgumentString);

        try {
            new PdpSimulatorParameterHandler().getParameters(noArguments);
            fail("test should throw an exception here");
        } catch (final Exception e) {
            assertTrue(e.getMessage().contains("no parameters found"));
        }
    }

    @Test
    public void testParameterHandlerInvalidParameters() throws PdpSimulatorException {
        final String[] invalidArgumentString = { "-c", "src/test/resources/InvalidParameters.json" };

        final PdpSimulatorCommandLineArguments invalidArguments = new PdpSimulatorCommandLineArguments();
        invalidArguments.parse(invalidArgumentString);

        try {
            new PdpSimulatorParameterHandler().getParameters(invalidArguments);
            fail("test should throw an exception here");
        } catch (final Exception e) {
            assertTrue(e.getMessage().startsWith("error reading parameters from"));
            assertTrue(e.getCause() instanceof CoderException);
        }
    }

    @Test
    public void testParameterHandlerNoParameters() throws PdpSimulatorException {
        final String[] noArgumentString = { "-c", "src/test/resources/EmptyConfigParameters.json" };

        final PdpSimulatorCommandLineArguments noArguments = new PdpSimulatorCommandLineArguments();
        noArguments.parse(noArgumentString);

        try {
            new PdpSimulatorParameterHandler().getParameters(noArguments);
        } catch (final Exception e) {
            assertTrue(e.getMessage().contains("is null"));
        }
    }

    @Test
    public void testPdpSimulatorParameterGroup() throws PdpSimulatorException {
        final String[] pdpSimulatorConfigParameters = { "-c", "src/test/resources/PdpSimulatorConfigParameters.json" };

        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();
        arguments.parse(pdpSimulatorConfigParameters);

        final PdpSimulatorParameterGroup parGroup = new PdpSimulatorParameterHandler().getParameters(arguments);
        assertTrue(arguments.checkSetConfigurationFilePath());
        assertEquals(CommonTestData.PDP_SIMULATOR_GROUP_NAME, parGroup.getName());
    }

    @Test
    public void testPdpSimulatorParameterGroup_InvalidName() throws PdpSimulatorException {
        final String[] pdpSimulatorConfigParameters = {"-c", 
            "src/test/resources/PdpSimulatorConfigParameters_InvalidName.json"};

        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();
        arguments.parse(pdpSimulatorConfigParameters);

        try {
            new PdpSimulatorParameterHandler().getParameters(arguments);
            fail("test should throw an exception here");
        } catch (final Exception e) {
            assertTrue(e.getMessage().contains(
                "field \"name\" type \"java.lang.String\" value \" \" INVALID, must be a non-blank string"));
        }
    }

    @Test
    public void testPdpSimulatorVersion() throws PdpSimulatorException {
        final String[] pdpSimulatorConfigParameters = { "-v" };
        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();
        final String version = arguments.parse(pdpSimulatorConfigParameters);
        assertTrue(version.startsWith("ONAP Policy-PDP simulator Service"));
    }

    @Test
    public void testPdpSimulatorHelp() throws PdpSimulatorException {
        final String[] pdpSimulatorConfigParameters = { "-h" };
        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();
        final String help = arguments.parse(pdpSimulatorConfigParameters);
        assertTrue(help.startsWith("usage:"));
    }

    @Test
    public void testPdpSimulatorInvalidOption() throws PdpSimulatorException {
        final String[] pdpSimulatorConfigParameters = { "-d" };
        final PdpSimulatorCommandLineArguments arguments = new PdpSimulatorCommandLineArguments();
        try {
            arguments.parse(pdpSimulatorConfigParameters);
        } catch (final Exception exp) {
            assertTrue(exp.getMessage().startsWith("invalid command line arguments specified"));
        }
    }
}
