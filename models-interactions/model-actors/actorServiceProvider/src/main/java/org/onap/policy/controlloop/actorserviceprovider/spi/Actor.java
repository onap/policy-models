/*-
 * ============LICENSE_START=======================================================
 * Actor
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.controlloop.actorserviceprovider.spi;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import org.onap.policy.common.capabilities.Startable;
import org.onap.policy.controlloop.actorserviceprovider.Configurable;
import org.onap.policy.controlloop.actorserviceprovider.OperationManager;

/**
 * This is the service interface for defining an Actor used in Control Loop Operational
 * Policies for performing actions on runtime entities.
 *
 * @author pameladragosh
 *
 */
public interface Actor extends Startable, Configurable<Map<String,Object>> {

    /**
     * Gets the name of the actor.
     *
     * @return the actor name
     */
    String getName();

    /**
     * Gets a particular operation.
     *
     * @param name name of the operation of interest
     * @return the desired operation
     * @throws IllegalArgumentException if no operation by the given name exists
     */
    OperationManager getOperationManager(String name);

    /**
     * Gets the supported operations.
     *
     * @return the supported operations
     */
    public Collection<OperationManager> getOperationManagers();

    /**
     * Gets the names of the supported operations.
     *
     * @return the names of the supported operations
     */
    public Set<String> getOperationNames();
}
