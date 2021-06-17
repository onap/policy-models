/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
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

package org.onap.policy.models.sim.pdp;

import java.util.Arrays;
import lombok.Getter;
import org.onap.policy.common.utils.cmd.CommandLineException;
import org.onap.policy.common.utils.services.Registry;
import org.onap.policy.models.sim.pdp.exception.PdpSimulatorException;
import org.onap.policy.models.sim.pdp.exception.PdpSimulatorRunTimeException;
import org.onap.policy.models.sim.pdp.parameters.PdpSimulatorParameterGroup;
import org.onap.policy.models.sim.pdp.parameters.PdpSimulatorParameterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class initiates PdpSimulator.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class PdpSimulatorMain {

    private static final String PDP_SIMULATOR_FAIL_MSG = "start of pdp simulator failed";

    private static final Logger LOGGER = LoggerFactory.getLogger(PdpSimulatorMain.class);

    private PdpSimulatorActivator activator;
    @Getter
    private PdpSimulatorParameterGroup parameters;

    /**
     * Instantiates the PdpSimulator.
     *
     * @param args the command line arguments
     */
    public PdpSimulatorMain(final String[] args) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("In PdpSimulator with parameters {}", Arrays.toString(args));
        }

        // Check the arguments
        final var arguments = new PdpSimulatorCommandLineArguments();
        try {
            // The arguments return a string if there is a message to print and we should exit
            final String argumentMessage = arguments.parse(args);
            if (argumentMessage != null) {
                LOGGER.debug(argumentMessage);
                return;
            }
            // Validate that the arguments are sane
            arguments.validate();
        } catch (final PdpSimulatorRunTimeException | CommandLineException e) {
            LOGGER.error(PDP_SIMULATOR_FAIL_MSG, e);
            return;
        }

        // Read the parameters
        try {
            parameters = new PdpSimulatorParameterHandler().getParameters(arguments);
        } catch (final Exception e) {
            LOGGER.error(PDP_SIMULATOR_FAIL_MSG, e);
            return;
        }

        // create the activator
        activator = new PdpSimulatorActivator(parameters);
        Registry.register(PdpSimulatorConstants.REG_PDP_SIMULATOR_ACTIVATOR, activator);
        // Start the activator
        try {
            activator.initialize();
        } catch (final PdpSimulatorException e) {
            LOGGER.error("start of PdpSimulator failed, used parameters are {}", Arrays.toString(args), e);
            Registry.unregister(PdpSimulatorConstants.REG_PDP_SIMULATOR_ACTIVATOR);
            return;
        }

        // Add a shutdown hook to shut everything down in an orderly manner
        Runtime.getRuntime().addShutdownHook(new PdpSimulatorShutdownHookClass());

        LOGGER.info("Started PdpSimulator service");
    }


    /**
     * Shut down Execution.
     *
     * @throws PdpSimulatorException on shutdown errors
     */
    public void shutdown() throws PdpSimulatorException {
        // clear the parameterGroup variable
        parameters = null;

        // clear the pdp simulator activator
        if (activator != null && activator.isAlive()) {
            activator.terminate();
        }
    }

    /**
     * The Class PdpSimulatorShutdownHookClass terminates the pdp simulator when its run method is called.
     */
    private class PdpSimulatorShutdownHookClass extends Thread {
        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                // Shutdown the pdp simulator service and wait for everything to stop
                if (activator != null && activator.isAlive()) {
                    activator.terminate();
                }
            } catch (final PdpSimulatorException e) {
                LOGGER.warn("error occured during shut down of the pdp simulator service", e);
            }
        }
    }

    /**
     * The main method. Arguments are validated in the constructor thus adding the NOSONAR.
     *
     * @param args the arguments
     *
     */
    public static void main(final String[] args) { // NOSONAR
        /*
         * The arguments are validated by the constructor, thus sonar is disabled.
         */

        new PdpSimulatorMain(args);
    }
}
