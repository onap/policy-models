/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
 *  Modifications Copyright (C) 2019, 2020 AT&T Intellectual Property. All rights reserved.
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

import org.apache.commons.cli.Option;
import org.onap.policy.common.utils.cmd.CommandLineArgumentsHandler;
import org.onap.policy.common.utils.cmd.CommandLineException;
import org.onap.policy.models.sim.pdp.exception.PdpSimulatorRunTimeException;

/**
 * This class reads and handles command line parameters for the pdp simulator.
 *
 * @author Ajith Sreekumar (ajith.sreekumar@est.tech)
 */
public class PdpSimulatorCommandLineArguments extends CommandLineArgumentsHandler {
    private static final String PDP_SIM = "pdp simulator";

    /**
     * Construct the options for the CLI editor.
     */
    public PdpSimulatorCommandLineArguments() {
        super(PdpSimulatorMain.class.getName(), PDP_SIM, pdpSimPropertyFileOption());
    }

    /**
     * Construct the options for the CLI editor and parse in the given arguments.
     *
     * @param args The command line arguments
     */
    public PdpSimulatorCommandLineArguments(final String[] args) {
        this();

        try {
            parse(args);
        } catch (final CommandLineException e) {
            throw new PdpSimulatorRunTimeException("parse error on pdp simulator parameters", e);
        }
    }

    /**
     * Build the property-file option for PDP Sim.
     * @return property-file option
     */
    private static Option pdpSimPropertyFileOption() {
        return Option.builder("p").longOpt("property-file")
                .desc("the full path to the topic property file to use, "
                        + "the property file contains the pdp simulator topic properties")
                .hasArg().argName("PROP_FILE").required(false).type(String.class).build();
    }
}
