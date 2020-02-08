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

package org.onap.policy.controlloop.actorserviceprovider.controlloop;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

/**
 * Context associated with a control loop event.
 */
@Getter
@Setter
public class ControlLoopEventContext implements Serializable {
    private static final long serialVersionUID = 1L;


    private final VirtualControlLoopEvent event;

    /**
     * Enrichment data extracted from the event. Never {@code null}, though it may be
     * immutable.
     */
    private final Map<String, String> enrichment;

    /**
     * Set of properties that have been stored in the context.
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private Map<String, Serializable> properties = new ConcurrentHashMap<>();

    /**
     * When {@link #obtain(String, ControlLoopOperationParams)} is invoked and the
     * specified property is not found in {@link #properties}, it is retrieved. This holds
     * the futures for the operations retrieving the properties.
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private transient Map<String, CompletableFuture<OperationOutcome>> retrievers = new ConcurrentHashMap<>();

    /**
     * Request ID extracted from the event, or a generated value if the event has no
     * request id; never {@code null}.
     */
    private final UUID requestId;


    /**
     * Constructs the object.
     *
     * @param event event with which this is associated
     */
    public ControlLoopEventContext(@NonNull VirtualControlLoopEvent event) {
        this.event = event;
        this.requestId = (event.getRequestId() != null ? event.getRequestId() : UUID.randomUUID());
        this.enrichment = (event.getAai() != null ? event.getAai() : Map.of());
    }

    /**
     * Determines if the context contains a property.
     *
     * @param name name of the property of interest
     * @return {@code true} if the context contains the property, {@code false} otherwise
     */
    public boolean contains(String name) {
        return properties.containsKey(name);
    }

    /**
     * Gets a property, casting it to the desired type.
     *
     * @param <T> desired type
     * @param name name of the property whose value is to be retrieved
     * @return the property's value, or {@code null} if it does not yet have a value
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String name) {
        return (T) properties.get(name);
    }

    /**
     * Sets a property's value.
     *
     * @param name property name
     * @param value new property value
     */
    public void setProperty(String name, Serializable value) {
        properties.put(name, value);
    }

    /**
     * Obtains the given property.
     *
     * @param name name of the desired property
     * @param params parameters needed to perform the operation to retrieve the desired
     *        property
     * @return a future for retrieving the property, {@code null} if the property has
     *         already been retrieved
     */
    public CompletableFuture<OperationOutcome> obtain(String name, ControlLoopOperationParams params) {
        if (properties.containsKey(name)) {
            return null;
        }

        CompletableFuture<OperationOutcome> future = retrievers.get(name);
        if (future != null) {
            return future;
        }

        future = params.start();

        CompletableFuture<OperationOutcome> oldFuture = retrievers.putIfAbsent(name, future);
        if (oldFuture != null) {
            future.cancel(false);
            return oldFuture;
        }

        return future;
    }
}
