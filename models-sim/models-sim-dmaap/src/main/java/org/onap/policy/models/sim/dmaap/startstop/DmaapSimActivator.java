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

package org.onap.policy.models.sim.dmaap.startstop;

import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.common.utils.services.ServiceManagerContainer;
import org.onap.policy.models.sim.dmaap.parameters.DmaapSimParameterGroup;
import org.onap.policy.models.sim.dmaap.rest.DmaapSimRestServer;

/**
 * This class activates the DMaaP simulator as a complete service.
 */
public class DmaapSimActivator extends ServiceManagerContainer {
    /**
     * The DMaaP simulator REST API server.
     */
    private DmaapSimRestServer restServer;

    /**
     * Instantiate the activator for the DMaaP simulator as a complete service.
     *
     * @param dmaapSimParameterGroup the parameters for the DMaaP simulator service
     */
    public DmaapSimActivator(final DmaapSimParameterGroup dmaapSimParameterGroup) {
        super("DMaaP Simulator");

        // @formatter:off
        addAction("DMaaP Simulator parameters",
            () -> ParameterService.register(dmaapSimParameterGroup),
            () -> ParameterService.deregister(dmaapSimParameterGroup.getName()));

        addAction("Create REST server",
            () -> restServer = new DmaapSimRestServer(dmaapSimParameterGroup.getRestServerParameters()),
            () -> restServer = null
        );

        addAction("REST server",
            () -> restServer.start(),
            () -> restServer.stop());
        // @formatter:on
    }
}
