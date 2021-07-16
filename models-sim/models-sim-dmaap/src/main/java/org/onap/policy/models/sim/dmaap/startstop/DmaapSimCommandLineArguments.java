/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019-2021 Nordix Foundation.
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

package org.onap.policy.models.sim.dmaap.startstop;

import org.onap.policy.common.utils.cmd.CommandLineArgumentsHandler;
import org.onap.policy.common.utils.cmd.CommandLineException;
import org.onap.policy.models.sim.dmaap.DmaapSimRuntimeException;

/**
 * This class reads and handles command line parameters for the DMaaP simulator service.
 */
public class DmaapSimCommandLineArguments extends CommandLineArgumentsHandler {
    private static final String DMAAP_SIM = "DMaaP simulator";

    /**
     * Construct the options for the CLI editor.
     */
    public DmaapSimCommandLineArguments() {
        super(Main.class.getName(), DMAAP_SIM);
    }

    /**
     * Construct the options for the CLI editor and parse in the given arguments.
     *
     * @param args The command line arguments
     */
    public DmaapSimCommandLineArguments(final String[] args) {
        this();

        try {
            parse(args);
        } catch (final CommandLineException e) {
            throw new DmaapSimRuntimeException("parse error on DMaaP simulator parameters", e);
        }
    }
}
