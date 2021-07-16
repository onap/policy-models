/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
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

package org.onap.policy.models.sim.dmaap.startstop;

import java.util.Arrays;
import lombok.Getter;
import org.onap.policy.common.utils.cmd.CommandLineException;
import org.onap.policy.models.sim.dmaap.DmaapSimException;
import org.onap.policy.models.sim.dmaap.DmaapSimRuntimeException;
import org.onap.policy.models.sim.dmaap.parameters.DmaapSimParameterGroup;
import org.onap.policy.models.sim.dmaap.parameters.DmaapSimParameterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class initiates the DMaaP simulator component.
 */
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private DmaapSimActivator activator;
    @Getter
    private DmaapSimParameterGroup parameters;

    /**
     * Instantiates the DMaap Simulator service.
     *
     * @param args the command line arguments
     */
    public Main(final String[] args) {
        final var argumentString = Arrays.toString(args);
        LOGGER.info("Starting DMaaP simulator service with arguments - {}", argumentString);

        // Check the arguments
        final var arguments = new DmaapSimCommandLineArguments();
        try {
            // The arguments return a string if there is a message to print and we should exit
            final String argumentMessage = arguments.parse(args);
            if (argumentMessage != null) {
                LOGGER.info(argumentMessage);
                return;
            }
            // Validate that the arguments are sane
            arguments.validate();
        } catch (final DmaapSimRuntimeException | CommandLineException e) {
            LOGGER.error("start of DMaaP simulator service failed", e);
            return;
        }

        // Read the parameters
        try {
            parameters = new DmaapSimParameterHandler().getParameters(arguments);
        } catch (final Exception e) {
            LOGGER.error("start of DMaaP simulator service failed", e);
            return;
        }

        // Now, create the activator for the DMaaP Simulator service
        activator = new DmaapSimActivator(parameters);

        // Start the activator
        try {
            activator.start();
        } catch (final RuntimeException e) {
            LOGGER.error("start of DMaaP simulator service failed, used parameters are {}", Arrays.toString(args), e);
            return;
        }

        // Add a shutdown hook to shut everything down in an orderly manner
        Runtime.getRuntime().addShutdownHook(new DmaapSimShutdownHookClass());
        LOGGER.info("Started DMaaP simulator service");
    }

    /**
     * Shut down Execution.
     *
     * @throws DmaapSimException on shutdown errors
     */
    public void shutdown() throws DmaapSimException {
        // clear the parameterGroup variable
        parameters = null;

        // clear the DMaaP simulator activator
        if (activator != null && activator.isAlive()) {
            activator.stop();
        }
    }

    /**
     * The Class DmaapSimShutdownHookClass terminates the DMaaP simulator service when its run method is called.
     */
    private class DmaapSimShutdownHookClass extends Thread {
        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                // Shutdown the DMaaP simulator service and wait for everything to stop
                shutdown();

            } catch (final RuntimeException | DmaapSimException e) {
                LOGGER.warn("error occured during shut down of the DMaaP simulator service", e);
            }
        }
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(final String[] args) {      // NOSONAR
        /*
         * The arguments are validated by the constructor, thus sonar is disabled.
         */

        new Main(args);
    }
}
