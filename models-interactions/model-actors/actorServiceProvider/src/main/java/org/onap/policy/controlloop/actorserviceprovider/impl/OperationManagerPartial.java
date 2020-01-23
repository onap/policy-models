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

package org.onap.policy.controlloop.actorserviceprovider.impl;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import lombok.Getter;
import org.onap.policy.controlloop.actorserviceprovider.ClosedLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.OperationManager;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationTracker;

/**
 * Partial implementation of an operation manager.
 */
public abstract class OperationManagerPartial extends ConfigImpl<Map<String, Object>> implements OperationManager {

    @Getter
    private final String actorName;

    @Getter
    private final String name;

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this manager is associated
     * @param name operation name
     */
    public OperationManagerPartial(String actorName, String name) {
        super(actorName + "." + name);
        this.actorName = actorName;
        this.name = name;
    }

    /**
     * This method does nothing.
     */
    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        // do nothing
    }

    @Override
    protected void doStart() {
        // do nothing
    }

    @Override
    protected void doStop() {
        // do nothing
    }

    @Override
    protected void doShutdown() {
        // do nothing
    }

    @Override
    public CompletableFuture<OperationOutcome> startOperation(ClosedLoopOperationParams params) {

        if (!isAlive()) {
            throw new IllegalStateException("operation is not running: " + getFullName());
        }

        final OperationTracker tracker = new OperationTracker();

        /*
         * Don't mark it complete until we've built the whole pipeline. This will prevent
         * the operation from starting until after it has been successfully built (i.e.,
         * without generating any exceptions).
         */
        final CompletableFuture<Void> firstFuture = new CompletableFuture<>();

        // TODO incorporate guard check into pipeline
        // TODO incorporate timeout check into pipeline

        CompletableFuture<OperationOutcome> finalFuture = tracker.setFinalFuture(
                        appendOperation(params, firstFuture.thenCompose(tracker::verifyRunning), tracker));

        // start the pipeline
        firstFuture.complete(null);

        return finalFuture;
    }

    @Override
    public <T> CompletableFuture<OperationOutcome> buildSubOperation(ClosedLoopOperationParams params,
                    CompletableFuture<T> future, OperationTracker tracker) {

        if (!isAlive()) {
            throw new IllegalStateException("operation is not running: " + getFullName());
        }

        // TODO incorporate timeout check into pipeline

        return tracker.setFinalFuture(appendOperation(params, future.thenCompose(tracker::verifyRunning), tracker));
    }

    /**
     * Builds an operation, appending it to a pipeline. The method can assume that the
     * tracker has already been verified before the operation starts.
     *
     * @param <T> result type of the current pipeline
     * @param params parameters needed to start the sub-operation
     * @param future pipeline onto which the operation should be added
     * @param tracker tracker used to determine if the pipeline is still running
     * @return a future that will execute the sub-operation after "future" completes
     */
    protected abstract <T> CompletableFuture<OperationOutcome> appendOperation(ClosedLoopOperationParams params,
                    CompletableFuture<T> future, OperationTracker tracker);

    /**
     * Invokes the callback to indicate that the operation has started.
     *
     * @param params parameters containing the callback
     * @return an identity mapping function that invokes the "started" callback
     */
    public <T> Function<T, T> invokeStartCallback(ClosedLoopOperationParams params) {
        Runnable callback = params.getStartCallback();
        if (callback == null) {
            return value -> value;
        }

        return value -> {
            callback.run();
            return value;
        };
    }
}
