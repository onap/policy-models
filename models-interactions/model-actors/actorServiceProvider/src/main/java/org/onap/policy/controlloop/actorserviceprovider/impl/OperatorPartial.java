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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import lombok.Getter;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.actorserviceprovider.Operator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial implementation of an operator. Subclasses can choose to simply implement
 * {@link #doOperation(ControlLoopOperationParams)}, or they may choose to override
 * {@link #doOperationAsFuture(ControlLoopOperationParams)}.
 */
public abstract class OperatorPartial extends StartConfigPartial<Map<String, Object>> implements Operator {

    private static final Logger logger = LoggerFactory.getLogger(OperatorPartial.class);

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
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     */
    public OperatorPartial(String actorName, String name) {
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
    public final CompletableFuture<ControlLoopOperation> startOperation(ControlLoopOperationParams params) {
        if (!isAlive()) {
            throw new IllegalStateException("operation is not running: " + getFullName());
        }

        final Executor executor = params.getExecutor();

        // allocate a controller for the entire operation
        final PipelineControllerFuture<ControlLoopOperation> controller = new PipelineControllerFuture<>();

        CompletableFuture<ControlLoopOperation> preproc = startPreprocessor(params);
        if (preproc == null) {
            // no preprocessor required - just start the operation
            return startOperationAttempt(params, controller, 1);
        }

        // propagate "stop" to the preprocessor
        controller.add(preproc);

        /*
         * Do preprocessor first and then, if successful, start the operation. Note:
         * operations create their own outcome, ignoring the outcome from any previous
         * steps.
         */
        preproc.whenCompleteAsync(controller.delayedRemove(preproc), executor)
                        .thenComposeAsync(handleFailure(params, controller), executor)
                        .thenComposeAsync(onSuccess(params, unused -> startOperationAttempt(params, controller, 1)),
                                        executor);

        return controller;
    }

    /**
     * Starts an operation's preprocessor step(s). If the preprocessor fails, then it
     * invokes the started and completed call-backs.
     *
     * @param params operation parameters
     * @return a future that will return the preprocessor outcome, or {@code null} if this
     *         operation needs no preprocessor
     */
    protected CompletableFuture<ControlLoopOperation> startPreprocessor(ControlLoopOperationParams params) {
        logger.info("{}: start low-level operation preprocessor for {}", getFullName(), params.getRequestId());

        final Executor executor = params.getExecutor();
        final ControlLoopOperation operation = params.makeOutcome();

        final Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> preproc =
                        doPreprocessorAsFuture(params);
        if (preproc == null) {
            // no preprocessor required
            return null;
        }

        // allocate a controller for the preprocessor steps
        final PipelineControllerFuture<ControlLoopOperation> controller = new PipelineControllerFuture<>();

        /*
         * Don't mark it complete until we've built the whole pipeline. This will prevent
         * the operation from starting until after it has been successfully built (i.e.,
         * without generating any exceptions).
         */
        final CompletableFuture<ControlLoopOperation> firstFuture = new CompletableFuture<>();

        // @formatter:off
        firstFuture
            .thenComposeAsync(controller.add(preproc), executor)
            .exceptionally(fromException(params, operation))
            .whenCompleteAsync(controller.delayedComplete(), executor);
        // @formatter:on

        // start the pipeline
        firstFuture.complete(operation);

        return controller;
    }

    /**
     * Handles a failure in the preprocessor pipeline. If a failure occurred, then it
     * invokes the call-backs and returns a failed outcome. Otherwise, it returns the
     * outcome that it received.
     *
     * @param params operation parameters
     * @param controller pipeline controller
     * @return a function that checks the outcome status and continues, if successful, or
     *         indicates a failure otherwise
     */
    private Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> handleFailure(
                    ControlLoopOperationParams params, PipelineControllerFuture<ControlLoopOperation> controller) {

        return outcome -> {

            if (outcome != null && isSuccess(outcome)) {
                logger.trace("{}: preprocessor succeeded for {}", getFullName(), params.getRequestId());
                return CompletableFuture.completedFuture(outcome);
            }

            logger.warn("preprocessor failed, discontinuing operation {} for {}", getFullName(), params.getRequestId());

            final Executor executor = params.getExecutor();
            final CallbackManager callbacks = new CallbackManager();

            // propagate "stop" to the callbacks
            controller.add(callbacks);

            final ControlLoopOperation outcome2 = params.makeOutcome();

            // TODO need a FAILURE_MISSING_DATA (e.g., A&AI)

            outcome2.setOutcome(PolicyResult.FAILURE_GUARD.toString());
            outcome2.setMessage(outcome != null ? outcome.getMessage() : null);

            CompletableFuture.completedFuture(outcome2).thenApplyAsync(callbackStarted(params, callbacks), executor)
                            .thenApplyAsync(callbackCompleted(params, callbacks), executor)
                            .whenCompleteAsync(controller.delayedRemove(callbacks), executor)
                            .whenCompleteAsync(controller.delayedComplete(), executor);

            return controller;
        };
    }

    /**
     * Invokes the operation's preprocessor step(s) as a "future". This method simply
     * returns {@code null}.
     * <p/>
     * This method assumes the following:
     * <ul>
     * <li>the operator is alive</li>
     * <li>exceptions generated within the pipeline will be handled by the invoker</li>
     * </ul>
     *
     * @param params operation parameters
     * @return a function that will start the preprocessor and returns its outcome, or
     *         {@code null} if this operation needs no preprocessor
     */
    protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> doPreprocessorAsFuture(
                    ControlLoopOperationParams params) {
        return null;
    }

    /**
     * Starts the operation attempt, with no preprocessor. When all retries complete, it
     * will complete the controller.
     *
     * @param params operation parameters
     * @param controller controller for all operation attempts
     * @param attempt attempt number, typically starting with 1
     * @return a future that will return the final result of all attempts
     */
    private CompletableFuture<ControlLoopOperation> startOperationAttempt(ControlLoopOperationParams params,
                    PipelineControllerFuture<ControlLoopOperation> controller, int attempt) {

        final Executor executor = params.getExecutor();

        CompletableFuture<ControlLoopOperation> future = startAttemptWithoutRetries(params, attempt);

        // propagate "stop" to the operation attempt
        controller.add(future);

        // detach when complete
        future.whenCompleteAsync(controller.delayedRemove(future), executor)
                        .thenComposeAsync(retryOnFailure(params, controller, attempt), params.getExecutor())
                        .whenCompleteAsync(controller.delayedComplete(), executor);

        return controller;
    }

    /**
     * Starts the operation attempt, without doing any retries.
     *
     * @param params operation parameters
     * @param attempt attempt number, typically starting with 1
     * @return a future that will return the result of a single operation attempt
     */
    private CompletableFuture<ControlLoopOperation> startAttemptWithoutRetries(ControlLoopOperationParams params,
                    int attempt) {

        logger.info("{}: start operation attempt {} for {}", getFullName(), attempt, params.getRequestId());

        final Executor executor = params.getExecutor();
        final ControlLoopOperation outcome = params.makeOutcome();
        final CallbackManager callbacks = new CallbackManager();

        // this operation attempt gets its own controller
        final PipelineControllerFuture<ControlLoopOperation> controller = new PipelineControllerFuture<>();

        // propagate "stop" to the callbacks
        controller.add(callbacks);

        /*
         * Don't mark it complete until we've built the whole pipeline. This will prevent
         * the operation from starting until after it has been successfully built (i.e.,
         * without generating any exceptions).
         */
        final CompletableFuture<ControlLoopOperation> firstFuture = new CompletableFuture<>();

        // @formatter:off
        CompletableFuture<ControlLoopOperation> future2 =
            firstFuture.thenComposeAsync(verifyRunning(controller, params), executor)
                        .thenApplyAsync(callbackStarted(params, callbacks), executor)
                        .thenComposeAsync(controller.add(doOperationAsFuture(params, attempt)), executor);
        // @formatter:on

        // handle timeouts, if specified
        long timeoutMillis = getTimeOutMillis(params.getPolicy());
        if (timeoutMillis > 0) {
            logger.info("{}: set timeout to {}ms for {}", getFullName(), timeoutMillis, params.getRequestId());
            future2 = future2.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
        }

        /*
         * Note: we re-invoke callbackStarted() just to be sure the callback is invoked
         * before callbackCompleted() is invoked.
         *
         * Note: no need to remove "callbacks" from the pipeline, as we're going to stop
         * the pipeline as the last step anyway.
         */

        // @formatter:off
        future2.exceptionally(fromException(params, outcome))
                    .thenApplyAsync(setRetryFlag(params, attempt), executor)
                    .thenApplyAsync(callbackStarted(params, callbacks), executor)
                    .thenApplyAsync(callbackCompleted(params, callbacks), executor)
                    .whenCompleteAsync(controller.delayedComplete(), executor);
        // @formatter:on

        // start the pipeline
        firstFuture.complete(outcome);

        return controller;
    }

    /**
     * Determines if the outcome was successful.
     *
     * @param outcome outcome to examine
     * @return {@code true} if the outcome was successful
     */
    protected boolean isSuccess(ControlLoopOperation outcome) {
        return OUTCOME_SUCCESS.equals(outcome.getOutcome());
    }

    /**
     * Determines if the outcome was a failure for this operator.
     *
     * @param outcome outcome to examine, or {@code null}
     * @return {@code true} if the outcome is not {@code null} and was a failure
     *         <i>and</i> was associated with this operator, {@code false} otherwise
     */
    protected boolean isActorFailed(ControlLoopOperation outcome) {
        return OUTCOME_FAILURE.equals(getActorOutcome(outcome));
    }

    /**
     * Invokes the operation as a "future". This method simply invokes
     * {@link #doOperation(ControlLoopOperationParams)} turning it into a "future".
     * <p/>
     * This method assumes the following:
     * <ul>
     * <li>the operator is alive</li>
     * <li>verifyRunning() has been invoked</li>
     * <li>callbackStarted() has been invoked</li>
     * <li>the invoker will perform appropriate timeout checks</li>
     * <li>exceptions generated within the pipeline will be handled by the invoker</li>
     * </ul>
     *
     * @param params operation parameters
     * @param attempt attempt number, typically starting with 1
     * @return a function that will start the operation and return its result when
     *         complete
     */
    protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> doOperationAsFuture(
                    ControlLoopOperationParams params, int attempt) {

        /*
         * TODO As doOperation() may perform blocking I/O, this should be launched in its
         * own thread to prevent the ForkJoinPool from being tied up. Should probably
         * provide a method to make that easy.
         */

        return operation -> CompletableFuture.supplyAsync(() -> doOperation(params, attempt, operation),
                        params.getExecutor());
    }

    /**
     * Low-level method that performs the operation. This can make the same assumptions
     * that are made by {@link #doOperationAsFuture(ControlLoopOperationParams)}. This
     * method throws an {@link UnsupportedOperationException}.
     *
     * @param params operation parameters
     * @param attempt attempt number, typically starting with 1
     * @param operation the operation being performed
     * @return the outcome of the operation
     */
    protected ControlLoopOperation doOperation(ControlLoopOperationParams params, int attempt,
                    ControlLoopOperation operation) {

        throw new UnsupportedOperationException("start operation " + getFullName());
    }

    /**
     * Sets the outcome status to FAILURE_RETRIES, if the current operation outcome is
     * FAILURE, assuming the policy specifies retries and the retry count has been
     * exhausted.
     *
     * @param params operation parameters
     * @param attempt latest attempt number, starting with 1
     * @return a function to get the next future to execute
     */
    private Function<ControlLoopOperation, ControlLoopOperation> setRetryFlag(ControlLoopOperationParams params,
                    int attempt) {

        return operation -> {
            if (operation != null && !isActorFailed(operation)) {
                /*
                 * wrong type or wrong operation - just leave it as is. No need to log
                 * anything here, as retryOnFailure() will log a message
                 */
                return operation;
            }

            // get a non-null operation
            ControlLoopOperation oper2;
            if (operation != null) {
                oper2 = operation;
            } else {
                oper2 = params.makeOutcome();
                oper2.setOutcome(OUTCOME_FAILURE);
            }

            if (params.getPolicy().getRetry() != null && params.getPolicy().getRetry() > 0
                            && attempt > params.getPolicy().getRetry()) {
                /*
                 * retries were specified and we've already tried them all - change to
                 * FAILURE_RETRIES
                 */
                logger.info("operation {} retries exhausted for {}", getFullName(), params.getRequestId());
                oper2.setOutcome(OUTCOME_RETRIES);
            }

            return oper2;
        };
    }

    /**
     * Restarts the operation if it was a FAILURE. Assumes that
     * {@link #setRetryFlag(ControlLoopOperationParams, int)} was previously invoked, and
     * thus that the "operation" is not {@code null}.
     *
     * @param params operation parameters
     * @param controller controller for all of the retries
     * @param attempt latest attempt number, starting with 1
     * @return a function to get the next future to execute
     */
    private Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> retryOnFailure(
                    ControlLoopOperationParams params, PipelineControllerFuture<ControlLoopOperation> controller,
                    int attempt) {

        return operation -> {
            if (!isActorFailed(operation)) {
                // wrong type or wrong operation - just leave it as is
                logger.trace("not retrying operation {} for {}", getFullName(), params.getRequestId());
                return CompletableFuture.completedFuture(operation);
            }

            if (params.getPolicy().getRetry() == null || params.getPolicy().getRetry() <= 0) {
                // no retries - already marked as FAILURE, so just return it
                logger.info("operation {} no retries for {}", getFullName(), params.getRequestId());
                return CompletableFuture.completedFuture(operation);
            }


            /*
             * Retry the operation.
             */
            logger.info("retry operation {} for {}", getFullName(), params.getRequestId());

            return startOperationAttempt(params, controller, attempt + 1);
        };
    }

    /**
     * Gets the outcome of an operation for this operation.
     *
     * @param operation operation whose outcome is to be extracted
     * @return the outcome of the given operation, if it's for this operator, {@code null}
     *         otherwise
     */
    protected String getActorOutcome(ControlLoopOperation operation) {
        if (operation == null) {
            return null;
        }

        if (!getActorName().equals(operation.getActor())) {
            return null;
        }

        if (!getName().equals(operation.getOperation())) {
            return null;
        }

        return operation.getOutcome();
    }

    /**
     * Gets a function that will start the next step, if the current operation was
     * successful, or just return the current operation, otherwise.
     *
     * @param params operation parameters
     * @param nextStep function that will invoke the next step, passing it the operation
     * @return a function that will start the next step
     */
    protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> onSuccess(
                    ControlLoopOperationParams params,
                    Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> nextStep) {

        return operation -> {

            if (operation == null) {
                logger.trace("{}: null outcome - discarding next task for {}", getFullName(), params.getRequestId());
                ControlLoopOperation outcome = params.makeOutcome();
                outcome.setOutcome(OUTCOME_FAILURE);
                return CompletableFuture.completedFuture(outcome);

            } else if (isSuccess(operation)) {
                logger.trace("{}: success - starting next task for {}", getFullName(), params.getRequestId());
                return nextStep.apply(operation);

            } else {
                logger.trace("{}: failure - discarding next task for {}", getFullName(), params.getRequestId());
                return CompletableFuture.completedFuture(operation);
            }
        };
    }

    /**
     * Converts an exception into an operation outcome, returning a copy of the outcome to
     * prevent background jobs from changing it.
     *
     * @param params operation parameters
     * @param operation current operation
     * @return a function that will convert an exception into an operation outcome
     */
    private Function<Throwable, ControlLoopOperation> fromException(ControlLoopOperationParams params,
                    ControlLoopOperation operation) {

        return thrown -> {
            logger.warn("exception throw by operation {}.{} for {}", operation.getActor(), operation.getOperation(),
                            params.getRequestId(), thrown);

            /*
             * Must make a copy of the operation, as the original could be changed by
             * background jobs that might still be running.
             */
            return setOutcome(params, new ControlLoopOperation(operation), thrown);
        };
    }

    /**
     * Gets a function to verify that the operation is still running. If the pipeline is
     * not running, then it returns an incomplete future, which will effectively halt
     * subsequent operations in the pipeline. This method is intended to be used with one
     * of the {@link CompletableFuture}'s <i>thenCompose()</i> methods.
     *
     * @param controller pipeline controller
     * @param params operation parameters
     * @return a function to verify that the operation is still running
     */
    protected <T> Function<T, CompletableFuture<T>> verifyRunning(
                    PipelineControllerFuture<ControlLoopOperation> controller, ControlLoopOperationParams params) {

        return value -> {
            boolean running = controller.isRunning();
            logger.trace("{}: verify running {} for {}", getFullName(), running, params.getRequestId());

            return (running ? CompletableFuture.completedFuture(value) : new CompletableFuture<>());
        };
    }

    /**
     * Sets the start time of the operation and invokes the callback to indicate that the
     * operation has started. Does nothing if the pipeline has been stopped.
     * <p/>
     * This assumes that the "outcome" is not {@code null}.
     *
     * @param params operation parameters
     * @param callbacks used to determine if the start callback can be invoked
     * @return a function that sets the start time and invokes the callback
     */
    private Function<ControlLoopOperation, ControlLoopOperation> callbackStarted(ControlLoopOperationParams params,
                    CallbackManager callbacks) {

        return outcome -> {

            if (callbacks.canStart()) {
                // haven't invoked "start" callback yet
                outcome.setStart(callbacks.getStartTime());
                outcome.setEnd(null);
                params.callbackStarted(outcome);
            }

            return outcome;
        };
    }

    /**
     * Sets the end time of the operation and invokes the callback to indicate that the
     * operation has completed. Does nothing if the pipeline has been stopped.
     * <p/>
     * This assumes that the "outcome" is not {@code null}.
     * <p/>
     * Note: the start time must be a reference rather than a plain value, because it's
     * value must be gotten on-demand, when the returned function is executed at a later
     * time.
     *
     * @param params operation parameters
     * @param callbacks used to determine if the end callback can be invoked
     * @return a function that sets the end time and invokes the callback
     */
    private Function<ControlLoopOperation, ControlLoopOperation> callbackCompleted(ControlLoopOperationParams params,
                    CallbackManager callbacks) {

        return operation -> {

            if (callbacks.canEnd()) {
                operation.setStart(callbacks.getStartTime());
                operation.setEnd(callbacks.getEndTime());
                params.callbackCompleted(operation);
            }

            return operation;
        };
    }

    /**
     * Sets an operation's outcome and message, based on a throwable.
     *
     * @param params operation parameters
     * @param operation operation to be updated
     * @return the updated operation
     */
    protected ControlLoopOperation setOutcome(ControlLoopOperationParams params, ControlLoopOperation operation,
                    Throwable thrown) {
        PolicyResult result = (isTimeout(thrown) ? PolicyResult.FAILURE_TIMEOUT : PolicyResult.FAILURE_EXCEPTION);
        return setOutcome(params, operation, result);
    }

    /**
     * Sets an operation's outcome and default message based on the result.
     *
     * @param params operation parameters
     * @param operation operation to be updated
     * @param result result of the operation
     * @return the updated operation
     */
    protected ControlLoopOperation setOutcome(ControlLoopOperationParams params, ControlLoopOperation operation,
                    PolicyResult result) {
        logger.trace("{}: set outcome {} for {}", getFullName(), result, params.getRequestId());
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
    protected boolean isTimeout(Throwable thrown) {
        if (thrown instanceof CompletionException) {
            thrown = thrown.getCause();
        }

        return (thrown instanceof TimeoutException);
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

    /**
     * Manager for "start" and "end" callbacks.
     */
    private static class CallbackManager implements Runnable {
        private final AtomicReference<Instant> startTime = new AtomicReference<>();
        private final AtomicReference<Instant> endTime = new AtomicReference<>();

        /**
         * Determines if the "start" callback can be invoked. If so, it sets the
         * {@link #startTime} to the current time.
         *
         * @return {@code true} if the "start" callback can be invoked, {@code false}
         *         otherwise
         */
        public boolean canStart() {
            return startTime.compareAndSet(null, Instant.now());
        }

        /**
         * Determines if the "end" callback can be invoked. If so, it sets the
         * {@link #endTime} to the current time.
         *
         * @return {@code true} if the "end" callback can be invoked, {@code false}
         *         otherwise
         */
        public boolean canEnd() {
            return endTime.compareAndSet(null, Instant.now());
        }

        /**
         * Gets the start time.
         *
         * @return the start time, or {@code null} if {@link #canStart()} has not been
         *         invoked yet.
         */
        public Instant getStartTime() {
            return startTime.get();
        }

        /**
         * Gets the end time.
         *
         * @return the end time, or {@code null} if {@link #canEnd()} has not been invoked
         *         yet.
         */
        public Instant getEndTime() {
            return endTime.get();
        }

        /**
         * Prevents further callbacks from being executed by setting {@link #startTime}
         * and {@link #endTime}.
         */
        @Override
        public void run() {
            canStart();
            canEnd();
        }
    }
}
