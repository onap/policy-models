/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * This is the service interface for defining an Actor operation used in Control Loop
 * Operational Policies for performing actions on runtime entities.
 */
public interface Operation {

    /**
     * Gets the name of the associated actor.
     *
     * @return the name of the associated actor
     */
    String getActorName();

    /**
     * Gets the name of the operation.
     *
     * @return the operation name
     */
    String getName();

    /**
     * Gets the names of the properties required by the operation.
     *
     * @return the names of the properties required by the operation
     */
    List<String> getPropertyNames();

    /**
     * Determines if a property has been assigned for the operation.
     *
     * @param name property name
     * @return {@code true} if the given property has been assigned for the operation,
     *         {@code false} otherwise
     */
    public boolean containsProperty(String name);

    /**
     * Sets a property.
     *
     * @param name property name
     * @param value new value
     */
    public void setProperty(String name, Object value);

    /**
     * Gets a property's value.
     *
     * @param name name of the property of interest
     * @return the property's value, or {@code null} if it has no value
     */
    public <T> T getProperty(String name);

    /**
     * Called by enforcement PDP engine to start the operation. As part of the operation,
     * it invokes the "start" and "complete" call-backs found within the parameters.
     *
     * @return a future that can be used to cancel or await the result of the operation
     */
    CompletableFuture<OperationOutcome> start();
}
