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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.actorserviceprovider.CallbackManager;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial implementation of an operator. In general, it's preferable that subclasses
 * would override {@link #startOperationAsync(int, OperationOutcome)
 * startOperationAsync()}. However, if that proves to be too difficult, then they can
 * simply override {@link #doOperation(int, OperationOutcome) doOperation()}. In addition,
 * if the operation requires any preprocessor steps, the subclass may choose to override
 * {@link #startPreprocessorAsync()}.
 * <p/>
 * The futures returned by the methods within this class can be canceled, and will
 * propagate the cancellation to any subtasks. Thus it is also expected that any futures
 * returned by overridden methods will do the same. Of course, if a class overrides
 * {@link #doOperation(int, OperationOutcome) doOperation()}, then there's little that can
 * be done to cancel that particular operation.
 */
public abstract class OperationPartial implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(OperationPartial.class);
    private static final Coder coder = new StandardCoder();
    public static final long DEFAULT_RETRY_WAIT_MS = 1000L;

    // values extracted from the operator

    private final OperatorPartial operator;

    /**
     * Operation parameters.
     */
    protected final ControlLoopOperationParams params;


    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param operator operator that created this operation
     */
    public OperationPartial(ControlLoopOperationParams params, OperatorPartial operator) {
        this.params = params;
        this.operator = operator;
    }

    public Executor getBlockingExecutor() {
        return operator.getBlockingExecutor();
    }

    public String getFullName() {
        return operator.getFullName();
    }

    public String getActorName() {
        return operator.getActorName();
    }

    public String getName() {
        return operator.getName();
    }

    @Override
    public final CompletableFuture<OperationOutcome> start() {
        if (!operator.isAlive()) {
            throw new IllegalStateException("operation is not running: " + getFullName());
        }

        // allocate a controller for the entire operation
        final PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        CompletableFuture<OperationOutcome> preproc = startPreprocessorAsync();
        if (preproc == null) {
            // no preprocessor required - just start the operation
            return startOperationAttempt(controller, 1);
        }

        /*
         * Do preprocessor first and then, if successful, start the operation. Note:
         * operations create their own outcome, ignoring the outcome from any previous
         * steps.
         *
         * Wrap the preprocessor to ensure "stop" is propagated to it.
         */
        // @formatter:off
        controller.wrap(preproc)
                        .exceptionally(fromException("preprocessor of operation"))
                        .thenCompose(handlePreprocessorFailure(controller))
                        .thenCompose(unusedOutcome -> startOperationAttempt(controller, 1))
                        .whenCompleteAsync(controller.delayedComplete(), params.getExecutor());
        // @formatter:on

        return controller;
    }

    /**
     * Handles a failure in the preprocessor pipeline. If a failure occurred, then it
     * invokes the call-backs, marks the controller complete, and returns an incomplete
     * future, effectively halting the pipeline. Otherwise, it returns the outcome that it
     * received.
     * <p/>
     * Assumes that no callbacks have been invoked yet.
     *
     * @param controller pipeline controller
     * @return a function that checks the outcome status and continues, if successful, or
     *         indicates a failure otherwise
     */
    private Function<OperationOutcome, CompletableFuture<OperationOutcome>> handlePreprocessorFailure(
                    PipelineControllerFuture<OperationOutcome> controller) {

        return outcome -> {

            if (outcome != null && isSuccess(outcome)) {
                logger.info("{}: preprocessor succeeded for {}", getFullName(), params.getRequestId());
                return CompletableFuture.completedFuture(outcome);
            }

            logger.warn("preprocessor failed, discontinuing operation {} for {}", getFullName(), params.getRequestId());

            final Executor executor = params.getExecutor();
            final CallbackManager callbacks = new CallbackManager();

            // propagate "stop" to the callbacks
            controller.add(callbacks);

            final OperationOutcome outcome2 = params.makeOutcome();

            // TODO need a FAILURE_MISSING_DATA (e.g., A&AI)

            outcome2.setResult(PolicyResult.FAILURE_GUARD);
            outcome2.setMessage(outcome != null ? outcome.getMessage() : null);

            // @formatter:off
            CompletableFuture.completedFuture(outcome2)
                            .whenCompleteAsync(callbackStarted(callbacks), executor)
                            .whenCompleteAsync(callbackCompleted(callbacks), executor)
                            .whenCompleteAsync(controller.delayedComplete(), executor);
            // @formatter:on

            return new CompletableFuture<>();
        };
    }

    /**
     * Invokes the operation's preprocessor step(s) as a "future". This method simply
     * invokes {@link #startGuardAsync()}.
     * <p/>
     * This method assumes the following:
     * <ul>
     * <li>the operator is alive</li>
     * <li>exceptions generated within the pipeline will be handled by the invoker</li>
     * </ul>
     *
     * @return a function that will start the preprocessor and returns its outcome, or
     *         {@code null} if this operation needs no preprocessor
     */
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        return startGuardAsync();
    }

    /**
     * Invokes the operation's guard step(s) as a "future". This method simply returns
     * {@code null}.
     * <p/>
     * This method assumes the following:
     * <ul>
     * <li>the operator is alive</li>
     * <li>exceptions generated within the pipeline will be handled by the invoker</li>
     * </ul>
     *
     * @return a function that will start the guard checks and returns its outcome, or
     *         {@code null} if this operation has no guard
     */
    protected CompletableFuture<OperationOutcome> startGuardAsync() {
        return null;
    }

    /**
     * Starts the operation attempt, with no preprocessor. When all retries complete, it
     * will complete the controller.
     *
     * @param controller controller for all operation attempts
     * @param attempt attempt number, typically starting with 1
     * @return a future that will return the final result of all attempts
     */
    private CompletableFuture<OperationOutcome> startOperationAttempt(
                    PipelineControllerFuture<OperationOutcome> controller, int attempt) {

        // propagate "stop" to the operation attempt
        controller.wrap(startAttemptWithoutRetries(attempt)).thenCompose(retryOnFailure(controller, attempt))
                        .whenCompleteAsync(controller.delayedComplete(), params.getExecutor());

        return controller;
    }

    /**
     * Starts the operation attempt, without doing any retries.
     *
     * @param params operation parameters
     * @param attempt attempt number, typically starting with 1
     * @return a future that will return the result of a single operation attempt
     */
    private CompletableFuture<OperationOutcome> startAttemptWithoutRetries(int attempt) {

        logger.info("{}: start operation attempt {} for {}", getFullName(), attempt, params.getRequestId());

        final Executor executor = params.getExecutor();
        final OperationOutcome outcome = params.makeOutcome();
        final CallbackManager callbacks = new CallbackManager();

        // this operation attempt gets its own controller
        final PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        // propagate "stop" to the callbacks
        controller.add(callbacks);

        // @formatter:off
        CompletableFuture<OperationOutcome> future = CompletableFuture.completedFuture(outcome)
                        .whenCompleteAsync(callbackStarted(callbacks), executor)
                        .thenCompose(controller.wrap(outcome2 -> startOperationAsync(attempt, outcome2)));
        // @formatter:on

        // handle timeouts, if specified
        long timeoutMillis = getTimeoutMs(params.getTimeoutSec());
        if (timeoutMillis > 0) {
            logger.info("{}: set timeout to {}ms for {}", getFullName(), timeoutMillis, params.getRequestId());
            future = future.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
        }

        /*
         * Note: we re-invoke callbackStarted() just to be sure the callback is invoked
         * before callbackCompleted() is invoked.
         *
         * Note: no need to remove "callbacks" from the pipeline, as we're going to stop
         * the pipeline as the last step anyway.
         */

        // @formatter:off
        future.exceptionally(fromException("operation"))
                    .thenApply(setRetryFlag(attempt))
                    .whenCompleteAsync(callbackStarted(callbacks), executor)
                    .whenCompleteAsync(callbackCompleted(callbacks), executor)
                    .whenCompleteAsync(controller.delayedComplete(), executor);
        // @formatter:on

        return controller;
    }

    /**
     * Determines if the outcome was successful.
     *
     * @param outcome outcome to examine
     * @return {@code true} if the outcome was successful
     */
    protected boolean isSuccess(OperationOutcome outcome) {
        return (outcome.getResult() == PolicyResult.SUCCESS);
    }

    /**
     * Determines if the outcome was a failure for this operator.
     *
     * @param outcome outcome to examine, or {@code null}
     * @return {@code true} if the outcome is not {@code null} and was a failure
     *         <i>and</i> was associated with this operator, {@code false} otherwise
     */
    protected boolean isActorFailed(OperationOutcome outcome) {
        return (isSameOperation(outcome) && outcome.getResult() == PolicyResult.FAILURE);
    }

    /**
     * Determines if the given outcome is for this operation.
     *
     * @param outcome outcome to examine
     * @return {@code true} if the outcome is for this operation, {@code false} otherwise
     */
    protected boolean isSameOperation(OperationOutcome outcome) {
        return OperationOutcome.isFor(outcome, getActorName(), getName());
    }

    /**
     * Invokes the operation as a "future". This method simply invokes
     * {@link #doOperation()} using the {@link #blockingExecutor "blocking executor"},
     * returning the result via a "future".
     * <p/>
     * Note: if the operation uses blocking I/O, then it should <i>not</i> be run using
     * the executor in the "params", as that may bring the background thread pool to a
     * grinding halt. The {@link #blockingExecutor "blocking executor"} should be used
     * instead.
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
     * @param attempt attempt number, typically starting with 1
     * @return a function that will start the operation and return its result when
     *         complete
     */
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        return CompletableFuture.supplyAsync(() -> doOperation(attempt, outcome), getBlockingExecutor());
    }

    /**
     * Low-level method that performs the operation. This can make the same assumptions
     * that are made by {@link #doOperationAsFuture()}. This particular method simply
     * throws an {@link UnsupportedOperationException}.
     *
     * @param attempt attempt number, typically starting with 1
     * @param operation the operation being performed
     * @return the outcome of the operation
     */
    protected OperationOutcome doOperation(int attempt, OperationOutcome operation) {

        throw new UnsupportedOperationException("start operation " + getFullName());
    }

    /**
     * Sets the outcome status to FAILURE_RETRIES, if the current operation outcome is
     * FAILURE, assuming the policy specifies retries and the retry count has been
     * exhausted.
     *
     * @param attempt latest attempt number, starting with 1
     * @return a function to get the next future to execute
     */
    private Function<OperationOutcome, OperationOutcome> setRetryFlag(int attempt) {

        return operation -> {
            if (operation != null && !isActorFailed(operation)) {
                /*
                 * wrong type or wrong operation - just leave it as is. No need to log
                 * anything here, as retryOnFailure() will log a message
                 */
                return operation;
            }

            // get a non-null operation
            OperationOutcome oper2;
            if (operation != null) {
                oper2 = operation;
            } else {
                oper2 = params.makeOutcome();
                oper2.setResult(PolicyResult.FAILURE);
            }

            int retry = getRetry(params.getRetry());
            if (retry > 0 && attempt > retry) {
                /*
                 * retries were specified and we've already tried them all - change to
                 * FAILURE_RETRIES
                 */
                logger.info("operation {} retries exhausted for {}", getFullName(), params.getRequestId());
                oper2.setResult(PolicyResult.FAILURE_RETRIES);
            }

            return oper2;
        };
    }

    /**
     * Restarts the operation if it was a FAILURE. Assumes that {@link #setRetryFlag(int)}
     * was previously invoked, and thus that the "operation" is not {@code null}.
     *
     * @param controller controller for all of the retries
     * @param attempt latest attempt number, starting with 1
     * @return a function to get the next future to execute
     */
    private Function<OperationOutcome, CompletableFuture<OperationOutcome>> retryOnFailure(
                    PipelineControllerFuture<OperationOutcome> controller, int attempt) {

        return operation -> {
            if (!isActorFailed(operation)) {
                // wrong type or wrong operation - just leave it as is
                logger.info("not retrying operation {} for {}", getFullName(), params.getRequestId());
                controller.complete(operation);
                return new CompletableFuture<>();
            }

            if (getRetry(params.getRetry()) <= 0) {
                // no retries - already marked as FAILURE, so just return it
                logger.info("operation {} no retries for {}", getFullName(), params.getRequestId());
                controller.complete(operation);
                return new CompletableFuture<>();
            }

            /*
             * Retry the operation.
             */
            long waitMs = getRetryWaitMs();
            logger.info("retry operation {} in {}ms for {}", getFullName(), waitMs, params.getRequestId());

            return sleep(waitMs, TimeUnit.MILLISECONDS)
                            .thenCompose(unused -> startOperationAttempt(controller, attempt + 1));
        };
    }

    /**
     * Convenience method that starts a sleep(), running via a future.
     *
     * @param sleepTime time to sleep
     * @param unit time unit
     * @return a future that will complete when the sleep completes
     */
    protected CompletableFuture<Void> sleep(long sleepTime, TimeUnit unit) {
        if (sleepTime <= 0) {
            return CompletableFuture.completedFuture(null);
        }

        return new CompletableFuture<Void>().completeOnTimeout(null, sleepTime, unit);
    }

    /**
     * Converts an exception into an operation outcome, returning a copy of the outcome to
     * prevent background jobs from changing it.
     *
     * @param type type of item throwing the exception
     * @return a function that will convert an exception into an operation outcome
     */
    private Function<Throwable, OperationOutcome> fromException(String type) {

        return thrown -> {
            OperationOutcome outcome = params.makeOutcome();

            logger.warn("exception throw by {} {}.{} for {}", type, outcome.getActor(), outcome.getOperation(),
                            params.getRequestId(), thrown);

            return setOutcome(outcome, thrown);
        };
    }

    /**
     * Similar to {@link CompletableFuture#anyOf(CompletableFuture...)}, but it cancels
     * any outstanding futures when one completes.
     *
     * @param futures futures for which to wait
     * @return a future to cancel or await an outcome. If this future is canceled, then
     *         all of the futures will be canceled
     */
    protected CompletableFuture<OperationOutcome> anyOf(List<CompletableFuture<OperationOutcome>> futures) {

        // convert list to an array
        @SuppressWarnings("rawtypes")
        CompletableFuture[] arrFutures = futures.toArray(new CompletableFuture[futures.size()]);

        @SuppressWarnings("unchecked")
        CompletableFuture<OperationOutcome> result = anyOf(arrFutures);
        return result;
    }

    /**
     * Same as {@link CompletableFuture#anyOf(CompletableFuture...)}, but it cancels any
     * outstanding futures when one completes.
     *
     * @param futures futures for which to wait
     * @return a future to cancel or await an outcome. If this future is canceled, then
     *         all of the futures will be canceled
     */
    protected CompletableFuture<OperationOutcome> anyOf(
                    @SuppressWarnings("unchecked") CompletableFuture<OperationOutcome>... futures) {

        if (futures.length == 1) {
            return futures[0];
        }

        final Executor executor = params.getExecutor();
        final PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        attachFutures(controller, futures);

        // @formatter:off
        CompletableFuture.anyOf(futures)
                            .thenApply(object -> (OperationOutcome) object)
                            .whenCompleteAsync(controller.delayedComplete(), executor);
        // @formatter:on

        return controller;
    }

    /**
     * Similar to {@link CompletableFuture#allOf(CompletableFuture...)}, but it cancels
     * the futures if returned future is canceled. The future returns the "worst" outcome,
     * based on priority (see {@link #detmPriority(OperationOutcome)}).
     *
     * @param futures futures for which to wait
     * @return a future to cancel or await an outcome. If this future is canceled, then
     *         all of the futures will be canceled
     */
    protected CompletableFuture<OperationOutcome> allOf(List<CompletableFuture<OperationOutcome>> futures) {

        // convert list to an array
        @SuppressWarnings("rawtypes")
        CompletableFuture[] arrFutures = futures.toArray(new CompletableFuture[futures.size()]);

        @SuppressWarnings("unchecked")
        CompletableFuture<OperationOutcome> result = allOf(arrFutures);
        return result;
    }

    /**
     * Same as {@link CompletableFuture#allOf(CompletableFuture...)}, but it cancels the
     * futures if returned future is canceled. The future returns the "worst" outcome,
     * based on priority (see {@link #detmPriority(OperationOutcome)}).
     *
     * @param futures futures for which to wait
     * @return a future to cancel or await an outcome. If this future is canceled, then
     *         all of the futures will be canceled
     */
    protected CompletableFuture<OperationOutcome> allOf(
                    @SuppressWarnings("unchecked") CompletableFuture<OperationOutcome>... futures) {

        if (futures.length == 1) {
            return futures[0];
        }

        final PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        attachFutures(controller, futures);

        OperationOutcome[] outcomes = new OperationOutcome[futures.length];

        @SuppressWarnings("rawtypes")
        CompletableFuture[] futures2 = new CompletableFuture[futures.length];

        // record the outcomes of each future when it completes
        for (int count = 0; count < futures2.length; ++count) {
            final int count2 = count;
            futures2[count] = futures[count].whenComplete((outcome2, thrown) -> outcomes[count2] = outcome2);
        }

        // @formatter:off
        CompletableFuture.allOf(futures2)
                        .thenApply(unused -> combineOutcomes(outcomes))
                        .whenCompleteAsync(controller.delayedComplete(), params.getExecutor());
        // @formatter:on

        return controller;
    }

    /**
     * Attaches the given futures to the controller.
     *
     * @param controller master controller for all of the futures
     * @param futures futures to be attached to the controller
     */
    private void attachFutures(PipelineControllerFuture<OperationOutcome> controller,
                    @SuppressWarnings("unchecked") CompletableFuture<OperationOutcome>... futures) {

        if (futures.length == 0) {
            throw new IllegalArgumentException("empty list of futures");
        }

        // attach each task
        for (CompletableFuture<OperationOutcome> future : futures) {
            controller.add(future);
        }
    }

    /**
     * Combines the outcomes from a set of tasks.
     *
     * @param outcomes outcomes to be examined
     * @return the combined outcome
     */
    private OperationOutcome combineOutcomes(OperationOutcome[] outcomes) {

        // identify the outcome with the highest priority
        OperationOutcome outcome = outcomes[0];
        int priority = detmPriority(outcome);

        // start with "1", as we've already dealt with "0"
        for (int count = 1; count < outcomes.length; ++count) {
            OperationOutcome outcome2 = outcomes[count];
            int priority2 = detmPriority(outcome2);

            if (priority2 > priority) {
                outcome = outcome2;
                priority = priority2;
            }
        }

        logger.info("{}: combined outcome of tasks is {} for {}", getFullName(),
                        (outcome == null ? null : outcome.getResult()), params.getRequestId());

        return outcome;
    }

    /**
     * Determines the priority of an outcome based on its result.
     *
     * @param outcome outcome to examine, or {@code null}
     * @return the outcome's priority
     */
    protected int detmPriority(OperationOutcome outcome) {
        if (outcome == null || outcome.getResult() == null) {
            return 1;
        }

        switch (outcome.getResult()) {
            case SUCCESS:
                return 0;

            case FAILURE_GUARD:
                return 2;

            case FAILURE_RETRIES:
                return 3;

            case FAILURE:
                return 4;

            case FAILURE_TIMEOUT:
                return 5;

            case FAILURE_EXCEPTION:
            default:
                return 6;
        }
    }

    /**
     * Performs a task, after verifying that the controller is still running. Also checks
     * that the previous outcome was successful, if specified.
     *
     * @param controller overall pipeline controller
     * @param checkSuccess {@code true} to check the previous outcome, {@code false}
     *        otherwise
     * @param outcome outcome of the previous task
     * @param task task to be performed
     * @return the task, if everything checks out. Otherwise, it returns an incomplete
     *         future and completes the controller instead
     */
    // @formatter:off
    protected CompletableFuture<OperationOutcome> doTask(
                    PipelineControllerFuture<OperationOutcome> controller,
                    boolean checkSuccess, OperationOutcome outcome,
                    CompletableFuture<OperationOutcome> task) {
        // @formatter:on

        if (checkSuccess && !isSuccess(outcome)) {
            /*
             * must complete before canceling so that cancel() doesn't cause controller to
             * complete
             */
            controller.complete(outcome);
            task.cancel(false);
            return new CompletableFuture<>();
        }

        return controller.wrap(task);
    }

    /**
     * Performs a task, after verifying that the controller is still running. Also checks
     * that the previous outcome was successful, if specified.
     *
     * @param controller overall pipeline controller
     * @param checkSuccess {@code true} to check the previous outcome, {@code false}
     *        otherwise
     * @param task function to start the task to be performed
     * @return a function to perform the task. If everything checks out, then it returns
     *         the task. Otherwise, it returns an incomplete future and completes the
     *         controller instead
     */
    // @formatter:off
    protected Function<OperationOutcome, CompletableFuture<OperationOutcome>> doTask(
                    PipelineControllerFuture<OperationOutcome> controller,
                    boolean checkSuccess,
                    Function<OperationOutcome, CompletableFuture<OperationOutcome>> task) {
        // @formatter:on

        return outcome -> {

            if (!controller.isRunning()) {
                return new CompletableFuture<>();
            }

            if (checkSuccess && !isSuccess(outcome)) {
                controller.complete(outcome);
                return new CompletableFuture<>();
            }

            return controller.wrap(task.apply(outcome));
        };
    }

    /**
     * Sets the start time of the operation and invokes the callback to indicate that the
     * operation has started. Does nothing if the pipeline has been stopped.
     * <p/>
     * This assumes that the "outcome" is not {@code null}.
     *
     * @param callbacks used to determine if the start callback can be invoked
     * @return a function that sets the start time and invokes the callback
     */
    private BiConsumer<OperationOutcome, Throwable> callbackStarted(CallbackManager callbacks) {

        return (outcome, thrown) -> {

            if (callbacks.canStart()) {
                // haven't invoked "start" callback yet
                outcome.setStart(callbacks.getStartTime());
                outcome.setEnd(null);
                params.callbackStarted(outcome);
            }
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
     * @param callbacks used to determine if the end callback can be invoked
     * @return a function that sets the end time and invokes the callback
     */
    private BiConsumer<OperationOutcome, Throwable> callbackCompleted(CallbackManager callbacks) {

        return (outcome, thrown) -> {

            if (callbacks.canEnd()) {
                outcome.setStart(callbacks.getStartTime());
                outcome.setEnd(callbacks.getEndTime());
                params.callbackCompleted(outcome);
            }
        };
    }

    /**
     * Sets an operation's outcome and message, based on a throwable.
     *
     * @param operation operation to be updated
     * @return the updated operation
     */
    protected OperationOutcome setOutcome(OperationOutcome operation, Throwable thrown) {
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
    public OperationOutcome setOutcome(OperationOutcome operation, PolicyResult result) {
        logger.trace("{}: set outcome {} for {}", getFullName(), result, params.getRequestId());
        operation.setResult(result);
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

    /**
     * Logs a response. If the response is not of type, String, then it attempts to
     * pretty-print it into JSON before logging.
     *
     * @param direction IN or OUT
     * @param infra communication infrastructure on which it was published
     * @param source source name (e.g., the URL or Topic name)
     * @param response response to be logged
     * @return the JSON text that was logged
     */
    public <T> String logMessage(EventType direction, CommInfrastructure infra, String source, T response) {
        String json;
        try {
            if (response == null) {
                json = null;
            } else if (response instanceof String) {
                json = response.toString();
            } else {
                json = makeCoder().encode(response, true);
            }

        } catch (CoderException e) {
            String type = (direction == EventType.IN ? "response" : "request");
            logger.warn("cannot pretty-print {}", type, e);
            json = response.toString();
        }

        logger.info("[{}|{}|{}|]{}{}", direction, infra, source, NetLoggerUtil.SYSTEM_LS, json);

        return json;
    }

    // these may be overridden by subclasses or junit tests

    /**
     * Gets the retry count.
     *
     * @param retry retry, extracted from the parameters, or {@code null}
     * @return the number of retries, or {@code 0} if no retries were specified
     */
    protected int getRetry(Integer retry) {
        return (retry == null ? 0 : retry);
    }

    /**
     * Gets the retry wait, in milliseconds.
     *
     * @return the retry wait, in milliseconds
     */
    protected long getRetryWaitMs() {
        return DEFAULT_RETRY_WAIT_MS;
    }

    /**
     * Gets the operation timeout.
     *
     * @param timeoutSec timeout, in seconds, extracted from the parameters, or
     *        {@code null}
     * @return the operation timeout, in milliseconds, or {@code 0} if no timeout was
     *         specified
     */
    protected long getTimeoutMs(Integer timeoutSec) {
        return (timeoutSec == null ? 0 : TimeUnit.MILLISECONDS.convert(timeoutSec, TimeUnit.SECONDS));
    }

    // these may be overridden by junit tests

    protected Coder makeCoder() {
        return coder;
    }
}
