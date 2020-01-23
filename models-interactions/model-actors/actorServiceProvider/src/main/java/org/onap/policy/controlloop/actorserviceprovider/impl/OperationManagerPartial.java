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
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.util.Util;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial implementation of an operation manager. Subclasses can choose to simply
 * implement {@link #doOperation(ControlLoopOperationParams)}, or they may choose to
 * override {@link #doOperationAsFuture(ControlLoopOperationParams)}.
 */
public abstract class OperationManagerPartial extends ConfigImpl<Map<String, Object>> implements OperationManager {

    private static final Logger logger = LoggerFactory.getLogger(OperationManagerPartial.class);

    private static final String OUTCOME_SUCCESS = PolicyResult.SUCCESS.toString();
    private static final String OUTCOME_FAILURE = PolicyResult.FAILURE.toString();
    private static final String OUTCOME_RETRIES = PolicyResult.FAILURE_RETRIES.toString();

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
     * This method throws an {@link UnsupportedOperationException}.
     */
    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        throw new UnsupportedOperationException("configure " + getFullName());
    }

    /**
     * This method does nothing.
     */
    @Override
    protected void doStart() {
        // do nothing
    }

    /**
     * This method does nothing.
     */
    @Override
    protected void doStop() {
        // do nothing
    }

    /**
     * This method does nothing.
     */
    @Override
    protected void doShutdown() {
        // do nothing
    }

    @Override
    public CompletableFuture<ControlLoopOperation> startOperation(ControlLoopOperationParams params) {
        // parameters to be used for one single execution
        final ControlLoopOperationParams params1 = params.toBuilder().attempt(1)
                        .pipelineController(new PipelineController(params.getPipelineController())).build();

        /*
         * Note: MUST use "params" in the call to retryOnFailure()
         */
        return startOperationOnce(params1).thenComposeAsync(retryOnFailure(params, 1), params.getExecutor());
    }

    /**
     * Starts the operation, with no pre-processing, running it once, without retries.
     *
     * @param params operation parameters
     * @return a future that will return the result of the operation
     */
    public CompletableFuture<ControlLoopOperation> startOperationOnce(ControlLoopOperationParams params) {

        final Executor executor = params.getExecutor();
        final ControlLoopOperation outcome = params.makeOutcome();

        /*
         * Don't mark it complete until we've built the whole pipeline. This will prevent
         * the operation from starting until after it has been successfully built (i.e.,
         * without generating any exceptions).
         */
        final CompletableFuture<ControlLoopOperation> firstFuture = new CompletableFuture<>();

        // @formatter:off
        CompletableFuture<ControlLoopOperation> future2 = firstFuture.thenCompose(verifyRunning(params))
                        .thenApplyAsync(callbackStarted(params), executor)
                        .thenComposeAsync(doOperationAsFuture(params), executor);
        // @formatter:on

        long timeoutMillis = getTimeOutMillis(params.getPolicy());
        if (timeoutMillis > 0) {
            future2 = future2.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
        }

        // @formatter:off
        future2 = future2.exceptionally(fromException(params, outcome))
                        .thenApplyAsync(stopFailedPipeline(params), executor)
                        .thenApplyAsync(determineRetries(params), executor)
                        .thenApplyAsync(callbackCompleted(params), executor);
        // @formatter:on

        // start the pipeline
        firstFuture.complete(outcome);

        return future2;
    }

    /**
     * Determines if retries are necessary. If the current operation outcome is FAILURE,
     * then it changes it to FAILURE_RETRIES if no more retries should be attempted.
     *
     * @param params operation parameters
     * @return a function to get the next future to execute
     */
    protected Function<ControlLoopOperation, ControlLoopOperation> determineRetries(ControlLoopOperationParams params) {
        return operation -> {
            if (!OUTCOME_FAILURE.equals(getActorOutcome(operation))) {
                // wrong type or wrong operation - just leave it as is
                return operation;
            }

            if (params.getPolicy().getRetry() != null && params.getPolicy().getRetry() > 0
                            && params.getAttempt() > params.getPolicy().getRetry()) {
                /*
                 * retries were specified and we've already tried them all - change to
                 * FAILURE_RETRIES
                 */
                operation.setOutcome(OUTCOME_RETRIES);
            }

            return operation;
        };
    }

    /**
     * Restarts the operation if it was a FAILURE.
     *
     * @param parentParams parameters overseeing all operation attempts (i.e., not just
     *        the current attempt)
     * @param attempt latest attempt number, starting with 1
     * @return a function to get the next future to execute
     */
    protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> retryOnFailure(
                    ControlLoopOperationParams parentParams, int attempt) {

        return operation -> {
            if (!OUTCOME_FAILURE.equals(getActorOutcome(operation))) {
                // wrong type or wrong operation - just leave it as is
                return CompletableFuture.completedFuture(operation);
            }

            if (parentParams.getPolicy().getRetry() == null || parentParams.getPolicy().getRetry() <= 0) {
                // no retries - already marked as FAILURE, so just return it
                return CompletableFuture.completedFuture(operation);
            }

            // parameters to be used for one single execution
            int newAttempt = attempt + 1;
            ControlLoopOperationParams params1 = parentParams.toBuilder().attempt(newAttempt)
                            .pipelineController(new PipelineController(parentParams.getPipelineController())).build();

            /*
             * Note: MUST use "parentParams" in the call to retryOnFailure()
             */
            return startOperationOnce(params1).thenComposeAsync(retryOnFailure(parentParams, newAttempt),
                            parentParams.getExecutor());
        };
    }

    /**
     * Gets the outcome of an operation for this operation.
     *
     * @param operation operation whose outcome is to be extracted
     * @return the outcome of the operation, if it's for this operation, {@code null}
     *         otherwise
     */
    protected String getActorOutcome(ControlLoopOperation operation) {
        return (getActorName().equals(operation.getActor()) && getName().equals(operation.getOperation())
                        ? operation.getOutcome()
                        : null);
    }

    /**
     * Stops the pipeline.
     *
     * @param params operation parameters
     * @return a function that will stop the pipeline and return its input value,
     *         unchanged
     */
    protected Function<ControlLoopOperation, ControlLoopOperation> stopPipeline(ControlLoopOperationParams params) {
        return operation -> {
            params.stopPipeline();
            return operation;
        };
    }

    /**
     * Stops the pipeline if the operation has failed.
     *
     * @param params operation parameters
     * @return a function that will stop the pipeline, if it has failed, and return its
     *         input value, unchanged
     */
    protected Function<ControlLoopOperation, ControlLoopOperation> stopFailedPipeline(
                    ControlLoopOperationParams params) {

        return operation -> {
            if (!OUTCOME_SUCCESS.equals(operation.getOutcome())) {
                params.stopPipeline();
            }

            return operation;
        };
    }


    /**
     * Discards the incoming value and invokes the operation as a "future". This method
     * simply invokes {@link #doOperation(ControlLoopOperationParams)} via a "future".
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
     * @param params operation parameters
     * @return a future that will return the result of the operation
     */
    protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> doOperationAsFuture(
                    ControlLoopOperationParams params) {

        return operation -> CompletableFuture.supplyAsync(() -> doOperation(params, operation), params.getExecutor());
    }

    /**
     * Low-level method that performs the operation. This can make the same assumptions
     * that are made by {@link #doOperationAsFuture(ControlLoopOperationParams)}. This
     * method throws an {@link UnsupportedOperationException}.
     *
     * @param params operation parameters
     * @param operation the operation being performed
     * @return the outcome of the operation
     */
    protected ControlLoopOperation doOperation(ControlLoopOperationParams params, ControlLoopOperation operation) {

        throw new UnsupportedOperationException("start operation " + getFullName());
    }

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
            if (OUTCOME_SUCCESS.equals(operation.getOutcome())) {
                return nextStep.apply(operation);

            } else {
                return CompletableFuture.completedFuture(operation);
            }
        };
    }

    /**
     * Gets a function that will convert an exception into an operation outcome.
     *
     * @param params operation parameters
     * @param operation current operation
     * @return a function that will convert an exception into an operation outcome
     */
    public Function<Throwable, ControlLoopOperation> fromException(ControlLoopOperationParams params,
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
     * @param params operation parameters
     * @return a function to verify that the operation is still running
     */
    public <T> Function<T, CompletableFuture<T>> verifyRunning(ControlLoopOperationParams params) {
        PipelineController controller = params.getPipelineController();
        return value -> (controller.isRunning() ? CompletableFuture.completedFuture(value) : new CompletableFuture<>());
    }

    /**
     * Sets the start time of the operation and invokes the callback to indicate that the
     * operation has started.
     *
     * @param params operation parameters
     * @return a function that sets the start time and invokes the callback
     */
    public Function<ControlLoopOperation, ControlLoopOperation> callbackStarted(ControlLoopOperationParams params) {
        Consumer<ControlLoopOperation> callback = params.getStartCallback();

        return operation -> {
            operation.setStart(Instant.now());

            if (callback != null) {
                Util.logException(() -> callback.accept(operation),
                                getFullName() + ": start-callback threw an exception");
            }
            return operation;
        };
    }

    /**
     * Sets the end time of the operation and invokes the callback to indicate that the
     * operation has completed.
     *
     * @param params operation parameters
     * @return a function that sets the end time and invokes the callback
     */
    public Function<ControlLoopOperation, ControlLoopOperation> callbackCompleted(ControlLoopOperationParams params) {
        Consumer<ControlLoopOperation> callback = params.getCompleteCallback();

        return operation -> {
            operation.setEnd(Instant.now());

            if (callback != null) {
                Util.logException(() -> callback.accept(operation),
                                getFullName() + ": completed-callback threw an exception");
            }
            return operation;
        };
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
     * Gets the operation timeout. Subclasses may override this method to obtain the
     * timeout in some other way (e.g., through configuration properties).
     *
     * @param policy policy from which to extract the timeout
     * @return the operation timeout, in milliseconds
     */
    protected long getTimeOutMillis(Policy policy) {
        Integer timeoutSec = policy.getTimeout();
        return (timeoutSec == null ? 0 : TimeUnit.MILLISECONDS.convert(timeoutSec, TimeUnit.SECONDS));
    }
}
