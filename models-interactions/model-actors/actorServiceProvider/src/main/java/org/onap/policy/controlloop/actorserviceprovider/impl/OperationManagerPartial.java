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
import org.onap.policy.controlloop.actorserviceprovider.PipelineController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial implementation of an operation manager.
 */
public abstract class OperationManagerPartial extends ConfigImpl<Map<String, Object>> implements OperationManager {
    private static final Logger logger = LoggerFactory.getLogger(OperationManagerPartial.class);

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

        /*
         * Don't mark it complete until we've built the whole pipeline. This will prevent
         * the operation from starting until after it has been successfully built (i.e.,
         * without generating any exceptions).
         */
        final CompletableFuture<Void> firstFuture = new CompletableFuture<>();

        CompletableFuture<OperationOutcome> lastFuture = appendOperation(params, firstFuture);

        // start the pipeline
        firstFuture.complete(null);

        return lastFuture;
    }

    @Override
    public <T> CompletableFuture<OperationOutcome> appendOperation(ClosedLoopOperationParams params,
                    CompletableFuture<T> future) {

        if (!isAlive()) {
            throw new IllegalStateException("operation manager is not running: " + getFullName());
        }

        // TODO incorporate guard check into pipeline
        // TODO incorporate timeout check into pipeline

        return doAppendOperation(params, future.thenCompose(verifyRunning(params)));
    }

    /**
     * Low-level method that appends an operation to a pipeline. The method can assume
     * that the operation manager is alive and that verifyRunning() has already been
     * invoked.
     *
     * @param params parameters needed to build the operation
     * @param future pipeline onto which the operation should be appended
     * @return a future that will return the result of the operation
     */
    protected abstract <T> CompletableFuture<OperationOutcome> doAppendOperation(ClosedLoopOperationParams params,
                    CompletableFuture<T> future);

    /**
     * Gets a function to verify that the operation is still running. If the pipeline is
     * not running, then it returns an incomplete future, which will effectively halt
     * subsequent operations in the pipeline. This method is intended to be used with one
     * of the {@link CompletableFuture}'s <i>thenCompose()</i> methods. It is light weight
     * and thus need not be executed asynchronously.
     *
     * @param params parameters containing the pipeline controller
     * @return a function to verify that the operation is still running
     */
    public <T> Function<T, CompletableFuture<T>> verifyRunning(ClosedLoopOperationParams params) {
        PipelineController controller = params.getPipelineController();
        return value -> (controller.isRunning() ? CompletableFuture.completedFuture(value) : new CompletableFuture<>());
    }

    /**
     * Invokes the callback to indicate that the operation has started.
     *
     * @param params parameters containing the callback
     * @return an identity mapping function that invokes the "started" callback
     */
    public <T> Function<T, T> invokeStartCallback(ClosedLoopOperationParams params) {
        Runnable callback = params.getStartCallback();
        if (callback != null) {
            return value -> {
                try {
                    callback.run();
                } catch (RuntimeException e) {
                    logger.warn("{}: start-callback threw an exception", getFullName(), e);
                }

                return value;
            };
        }

        return value -> value;
    }
}
