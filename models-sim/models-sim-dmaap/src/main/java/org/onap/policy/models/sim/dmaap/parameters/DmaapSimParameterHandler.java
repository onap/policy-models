/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
 *  Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.sim.dmaap.parameters;

import java.io.File;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.sim.dmaap.DmaapSimException;
import org.onap.policy.models.sim.dmaap.startstop.DmaapSimCommandLineArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles reading, parsing and validating of DMaaP simulator parameters from JSON files.
 */
public class DmaapSimParameterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapSimParameterHandler.class);

    private final Coder coder = new StandardCoder();

    /**
     * Read the parameters from the parameter file.
     *
     * @param arguments the arguments passed to DMaaP simulator
     * @return the parameters read from the configuration file
     * @throws DmaapSimException on parameter exceptions
     */
    public DmaapSimParameterGroup getParameters(final DmaapSimCommandLineArguments arguments) throws DmaapSimException {
        DmaapSimParameterGroup dmaapSimParameterGroup = null;

        // Read the parameters
        try {
            // Read the parameters from JSON
            var file = new File(arguments.getFullConfigurationFilePath());
            dmaapSimParameterGroup = coder.decode(file, DmaapSimParameterGroup.class);
        } catch (final CoderException e) {
            final String errorMessage = "error reading parameters from \"" + arguments.getConfigurationFilePath()
                    + "\"\n" + "(" + e.getClass().getSimpleName() + "):" + e.getMessage();
            LOGGER.error(errorMessage);
            throw new DmaapSimException(errorMessage, e);
        }

        // The JSON processing returns null if there is an empty file
        if (dmaapSimParameterGroup == null) {
            final String errorMessage = "no parameters found in \"" + arguments.getConfigurationFilePath() + "\"";
            LOGGER.error(errorMessage);
            throw new DmaapSimException(errorMessage);
        }

        // validate the parameters
        final ValidationResult validationResult = dmaapSimParameterGroup.validate();
        if (!validationResult.isValid()) {
            String returnMessage =
                    "validation error(s) on parameters from \"" + arguments.getConfigurationFilePath() + "\"\n";
            returnMessage += validationResult.getResult();

            LOGGER.error(returnMessage);
            throw new DmaapSimException(returnMessage);
        }

        return dmaapSimParameterGroup;
    }
}
