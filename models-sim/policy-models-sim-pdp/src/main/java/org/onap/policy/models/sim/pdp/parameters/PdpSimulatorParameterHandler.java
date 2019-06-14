/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.io.File;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.sim.pdp.PdpSimulatorCommandLineArguments;
import org.onap.policy.models.sim.pdp.exception.PdpSimulatorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles reading, parsing and validating of pdp simulator parameters from JSON files.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class PdpSimulatorParameterHandler {

    private static final Logger logger = LoggerFactory.getLogger(PdpSimulatorParameterHandler.class);
    private static final Coder CODER = new StandardCoder();

    /**
     * Read the parameters from the parameter file.
     *
     * @param arguments the arguments passed to pdp simulator
     * @return the parameters read from the configuration file
     * @throws PdpSimulatorException on parameter exceptions
     */
    public PdpSimulatorParameterGroup getParameters(final PdpSimulatorCommandLineArguments arguments)
            throws PdpSimulatorException {
        PdpSimulatorParameterGroup pdpSimulatorParameterGroup = null;

        // Read the parameters
        try {
            // Read the parameters from JSON
            final File file = new File(arguments.getFullConfigurationFilePath());
            pdpSimulatorParameterGroup = CODER.decode(file, PdpSimulatorParameterGroup.class);
        } catch (final CoderException e) {
            final String errorMessage = "error reading parameters from \"" + arguments.getConfigurationFilePath()
                    + "\"\n" + "(" + e.getClass().getSimpleName() + "):" + e.getMessage();
            logger.error(errorMessage);
            throw new PdpSimulatorException(errorMessage, e);
        }

        // The JSON processing returns null if there is an empty file
        if (pdpSimulatorParameterGroup == null) {
            final String errorMessage = "no parameters found in \"" + arguments.getConfigurationFilePath() + "\"";
            logger.error(errorMessage);
            throw new PdpSimulatorException(errorMessage);
        }

        // validate the parameters
        final GroupValidationResult validationResult = pdpSimulatorParameterGroup.validate();
        if (!validationResult.isValid()) {
            String returnMessage =
                    "validation error(s) on parameters from \"" + arguments.getConfigurationFilePath() + "\"\n";
            returnMessage += validationResult.getResult();

            logger.error(returnMessage);
            throw new PdpSimulatorException(returnMessage);
        }

        return pdpSimulatorParameterGroup;
    }

}
