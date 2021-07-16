/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import org.onap.policy.common.utils.services.ServiceManagerContainer;
import org.onap.policy.models.sim.dmaap.parameters.DmaapSimParameterGroup;
import org.onap.policy.models.sim.dmaap.provider.DmaapSimProvider;
import org.onap.policy.models.sim.dmaap.rest.DmaapSimRestServer;

/**
 * This class activates the DMaaP simulator as a complete service.
 */
public class DmaapSimActivator extends ServiceManagerContainer {

    /**
     * Instantiate the activator for the DMaaP simulator as a complete service.
     *
     * @param dmaapSimParameterGroup the parameters for the DMaaP simulator service
     */
    public DmaapSimActivator(final DmaapSimParameterGroup dmaapSimParameterGroup) {
        super("DMaaP Simulator");

        var provider = new DmaapSimProvider(dmaapSimParameterGroup);
        DmaapSimProvider.setInstance(provider);
        addAction("Sim Provider", provider::start, provider::stop);

        var restServer = new DmaapSimRestServer(dmaapSimParameterGroup.getRestServerParameters());
        addAction("REST server", restServer::start, restServer::stop);
    }
}
