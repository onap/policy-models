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

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationManager;
import org.onap.policy.controlloop.actorserviceprovider.PipelineController;
import org.onap.policy.controlloop.actorserviceprovider.closedloop.ClosedLoopOperationParams;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial implementation of an operation manager. Subclasses can choose to simply
 * implement {@link #doOperation(ClosedLoopOperationParams)}, or they may choose to
 * override {@link #doOperationAsFuture(ClosedLoopOperationParams)}.
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

    /**
     * This method runs the guard (and its preprocessing steps).
     */
    @Override
    public CompletableFuture<ControlLoopOperation> startPreProcessor(ClosedLoopOperationParams params) {

        // TODO run guard preprocessor and, if successful, then run guard

        return null;
    }

    @Override
    public CompletableFuture<ControlLoopOperation> startOperation(ClosedLoopOperationParams params) {

        final Executor executor = params.getExecutor();
        final ControlLoopOperation outcome = makeOutcome(params).build();

        /*
         * Don't mark it complete until we've built the whole pipeline. This will prevent
         * the operation from starting until after it has been successfully built (i.e.,
         * without generating any exceptions).
         */
        final CompletableFuture<ControlLoopOperation> firstFuture = new CompletableFuture<>();

        CompletableFuture<ControlLoopOperation> future2 = firstFuture.thenCompose(verifyRunning(params))
                        .thenApply(setStartTime()).thenApplyAsync(callbackStarted(params), executor)
                        .thenComposeAsync(doOperationAsFuture(params), executor);

        long timeoutMillis = getTimeOutMillis(params.getPolicy().getTimeout());
        if (timeoutMillis > 0) {
            future2 = future2.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
        }

        future2 = future2.exceptionally(fromException(params, outcome));

        // start the pipeline
        firstFuture.complete(outcome);

        return future2;
    }


    /**
     * Discards the incoming value and invokes the operation as a "future". This method
     * simply invokes {@link #doOperation(ClosedLoopOperationParams)} via a "future".
     * <p/>
     * This method assumes following:
     * <ul>
     * <li>the operation manager is alive</li>
     * <li>verifyRunning() has been invoked</li>
     * <li>invokeStartCallback() has been invoked</li>
     * <li>the invoker will perform appropriate timeout checks</li>
     * <li>exceptions generated within the pipeline will be handled by the invoker</li>
     * </ul>
     *
     * @param params parameters needed for the operation
     * @return a future that will return the result of the operation
     */
    protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> doOperationAsFuture(
                    ClosedLoopOperationParams params) {

        return operation -> CompletableFuture.supplyAsync(() -> doOperation(params, operation), params.getExecutor());
    }

    /**
     * Low-level method that performs the operation. This can make the same assumptions
     * that are made by {@link #doOperationAsFuture(ClosedLoopOperationParams)}.
     *
     * @param params parameters needed for the operation
     * @param operation the operation being performed
     * @return the outcome of the operation
     */
    protected abstract ControlLoopOperation doOperation(ClosedLoopOperationParams params,
                    ControlLoopOperation operation);

    /**
     * Gets a function that will start the next step, if the current operation was
     * successful, or just return the current operation, otherwise.
     *
     * @param nextStep function that will invoke the next step, passing it the operation
     * @return a function that will start the next step
     */
    public Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> onSuccess(
                    Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> nextStep) {

        return operation -> {
            if (PolicyResult.SUCCESS.toString().equals(operation.getOutcome())) {
                return nextStep.apply(operation);

            } else {
                return CompletableFuture.completedFuture(operation);
            }
        };
    }

    /**
     * Gets a function that will convert an exception into an operation outcome.
     *
     * @param params parameters needed for the operation
     * @param operation current operation
     * @return a function that will convert an exception into an operation outcome
     */
    public Function<Throwable, ControlLoopOperation> fromException(ClosedLoopOperationParams params,
                    ControlLoopOperation operation) {

        return thrown -> {
            logger.warn("operation {}.{} threw an exception", operation.getActor(), operation.getOperation(), thrown);

            params.stopPipeline();

            /*
             * Must make a copy of the operation, as the original could be changed by
             * background jobs that might still be running.
             */
            return setOutcome(new ControlLoopOperation(operation), thrown);
        };
    }

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
     * Gets a function that will invoke the callback to indicate that the operation has
     * started.
     *
     * @param params parameters containing the callback
     * @return an identity mapping function that invokes the "started" callback
     */
    public Function<ControlLoopOperation, ControlLoopOperation> callbackStarted(ClosedLoopOperationParams params) {
        Consumer<ControlLoopOperation> callback = params.getStartCallback();
        if (callback != null) {
            return operation -> {
                try {
                    callback.accept(operation);
                } catch (RuntimeException e) {
                    logger.warn("{}: start-callback threw an exception", getFullName(), e);
                }

                return operation;
            };
        }

        return operation -> operation;
    }

    /**
     * Gets a function that will set the start time of an operation and return the
     * operation.
     *
     * @return a function to set the start time of an operation
     */
    protected Function<ControlLoopOperation, ControlLoopOperation> setStartTime() {
        return operation -> {
            operation.setStart(Instant.now());
            return operation;
        };
    }

    /**
     * Makes an operation outcome.
     *
     * @param params parameters describing the operation
     * @return a new operation outcome
     */
    protected ControlLoopOperation.ControlLoopOperationBuilder makeOutcome(ClosedLoopOperationParams params) {
        return ControlLoopOperation.builder().actor(getActorName()).operation(getName())
                        .subRequestId(String.valueOf(params.getAttempt()));
    }

    /**
     * Sets an operation's outcome and message, based on a throwable.
     *
     * @param operation operation to be updated
     * @return the updated operation
     */
    protected ControlLoopOperation setOutcome(ControlLoopOperation operation, Throwable thrown) {
        PolicyResult result = (isTimeout(thrown) ? PolicyResult.FAILURE_TIMEOUT : PolicyResult.FAILURE_EXCEPTION);
        return setOutcome(operation, result);
    }

    /**
     * Sets an operation's outcome and default message based on the result.
     *
     * @param operation operation to be updated
     * @param result result of the operation
     * @return the updated operation
     */
    protected ControlLoopOperation setOutcome(ControlLoopOperation operation, PolicyResult result) {
        operation.setOutcome(result.toString());
        operation.setMessage(result == PolicyResult.SUCCESS ? ControlLoopOperation.SUCCESS_MSG
                        : ControlLoopOperation.FAILED_MSG);

        return operation;
    }

    /**
     * Determines if a throwable is due to a timeout.
     *
     * @param thrown throwable of interest
     * @return {@code true} if the throwable is due to a timeout, {@code false} otherwise
     */
    public boolean isTimeout(Throwable thrown) {
        Throwable thrown2 = thrown;
        while (thrown2 instanceof CompletionException && thrown2.getCause() != null) {
            thrown2 = thrown2.getCause();
        }

        return (thrown2 instanceof TimeoutException);
    }

    // these may be overridden by junit tests

    /**
     * Gets the operation timeout.
     *
     * @param timeoutSec timeout, in seconds
     * @return the operation timeout, in milliseconds
     */
    protected long getTimeOutMillis(Integer timeoutSec) {
        return (timeoutSec == null ? 0 : TimeUnit.MILLISECONDS.convert(timeoutSec, TimeUnit.SECONDS));
    }
}
