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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.actorserviceprovider.CallbackManager;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Operator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial implementation of an operator. In general, it's preferable that subclasses
 * would override
 * {@link #startOperationAsync(ControlLoopOperationParams, int, OperationOutcome)
 * startOperationAsync()}. However, if that proves to be too difficult, then they can
 * simply override {@link #doOperation(ControlLoopOperationParams, int, OperationOutcome)
 * doOperation()}. In addition, if the operation requires any preprocessor steps, the
 * subclass may choose to override
 * {@link #startPreprocessorAsync(ControlLoopOperationParams) startPreprocessorAsync()}.
 * <p/>
 * The futures returned by the methods within this class can be canceled, and will
 * propagate the cancellation to any subtasks. Thus it is also expected that any futures
 * returned by overridden methods will do the same. Of course, if a class overrides
 * {@link #doOperation(ControlLoopOperationParams, int, OperationOutcome) doOperation()},
 * then there's little that can be done to cancel that particular operation.
 */
public abstract class OperatorPartial extends StartConfigPartial<Map<String, Object>> implements Operator {

    private static final Logger logger = LoggerFactory.getLogger(OperatorPartial.class);

    /**
     * Executor to be used for tasks that may perform blocking I/O. The default executor
     * simply launches a new thread for each command that is submitted to it.
     * <p/>
     * May be overridden by junit tests.
     */
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private Executor blockingExecutor = command -> {
        Thread thread = new Thread(command);
        thread.setDaemon(true);
        thread.start();
    };

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
    public final CompletableFuture<OperationOutcome> startOperation(ControlLoopOperationParams params) {
        if (!isAlive()) {
            throw new IllegalStateException("operation is not running: " + getFullName());
        }

        // allocate a controller for the entire operation
        final PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        CompletableFuture<OperationOutcome> preproc = startPreprocessorAsync(params);
        if (preproc == null) {
            // no preprocessor required - just start the operation
            return startOperationAttempt(params, controller, 1);
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
                        .exceptionally(fromException(params, "preprocessor of operation"))
                        .thenCompose(handlePreprocessorFailure(params, controller))
                        .thenCompose(unusedOutcome -> startOperationAttempt(params, controller, 1));
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
     * @param params operation parameters
     * @param controller pipeline controller
     * @return a function that checks the outcome status and continues, if successful, or
     *         indicates a failure otherwise
     */
    private Function<OperationOutcome, CompletableFuture<OperationOutcome>> handlePreprocessorFailure(
                    ControlLoopOperationParams params, PipelineControllerFuture<OperationOutcome> controller) {

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

            final OperationOutcome outcome2 = params.makeOutcome();

            // TODO need a FAILURE_MISSING_DATA (e.g., A&AI)

            outcome2.setResult(PolicyResult.FAILURE_GUARD);
            outcome2.setMessage(outcome != null ? outcome.getMessage() : null);

            // @formatter:off
            CompletableFuture.completedFuture(outcome2)
                            .whenCompleteAsync(callbackStarted(params, callbacks), executor)
                            .whenCompleteAsync(callbackCompleted(params, callbacks), executor)
                            .whenCompleteAsync(controller.delayedComplete(), executor);
            // @formatter:on

            return new CompletableFuture<>();
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
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync(ControlLoopOperationParams params) {
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
    private CompletableFuture<OperationOutcome> startOperationAttempt(ControlLoopOperationParams params,
                    PipelineControllerFuture<OperationOutcome> controller, int attempt) {

        // propagate "stop" to the operation attempt
        controller.wrap(startAttemptWithoutRetries(params, attempt))
                        .thenCompose(retryOnFailure(params, controller, attempt));

        return controller;
    }

    /**
     * Starts the operation attempt, without doing any retries.
     *
     * @param params operation parameters
     * @param attempt attempt number, typically starting with 1
     * @return a future that will return the result of a single operation attempt
     */
    private CompletableFuture<OperationOutcome> startAttemptWithoutRetries(ControlLoopOperationParams params,
                    int attempt) {

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
                        .whenCompleteAsync(callbackStarted(params, callbacks), executor)
                        .thenCompose(controller.wrap(outcome2 -> startOperationAsync(params, attempt, outcome2)));
        // @formatter:on

        // handle timeouts, if specified
        long timeoutMillis = getTimeOutMillis(params.getTimeoutSec());
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
        future.exceptionally(fromException(params, "operation"))
                    .thenApply(setRetryFlag(params, attempt))
                    .whenCompleteAsync(callbackStarted(params, callbacks), executor)
                    .whenCompleteAsync(callbackCompleted(params, callbacks), executor)
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
     * {@link #doOperation(ControlLoopOperationParams)} using the {@link #blockingExecutor
     * "blocking executor"}, returning the result via a "future".
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
     * @param params operation parameters
     * @param attempt attempt number, typically starting with 1
     * @return a function that will start the operation and return its result when
     *         complete
     */
    protected CompletableFuture<OperationOutcome> startOperationAsync(ControlLoopOperationParams params, int attempt,
                    OperationOutcome outcome) {

        return CompletableFuture.supplyAsync(() -> doOperation(params, attempt, outcome), getBlockingExecutor());
    }

    /**
     * Low-level method that performs the operation. This can make the same assumptions
     * that are made by {@link #doOperationAsFuture(ControlLoopOperationParams)}. This
     * particular method simply throws an {@link UnsupportedOperationException}.
     *
     * @param params operation parameters
     * @param attempt attempt number, typically starting with 1
     * @param operation the operation being performed
     * @return the outcome of the operation
     */
    protected OperationOutcome doOperation(ControlLoopOperationParams params, int attempt, OperationOutcome operation) {

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
    private Function<OperationOutcome, OperationOutcome> setRetryFlag(ControlLoopOperationParams params, int attempt) {

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

            Integer retry = params.getRetry();
            if (retry != null && retry > 0 && attempt > retry) {
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
     * Restarts the operation if it was a FAILURE. Assumes that
     * {@link #setRetryFlag(ControlLoopOperationParams, int)} was previously invoked, and
     * thus that the "operation" is not {@code null}.
     *
     * @param params operation parameters
     * @param controller controller for all of the retries
     * @param attempt latest attempt number, starting with 1
     * @return a function to get the next future to execute
     */
    private Function<OperationOutcome, CompletableFuture<OperationOutcome>> retryOnFailure(
                    ControlLoopOperationParams params, PipelineControllerFuture<OperationOutcome> controller,
                    int attempt) {

        return operation -> {
            if (!isActorFailed(operation)) {
                // wrong type or wrong operation - just leave it as is
                logger.trace("not retrying operation {} for {}", getFullName(), params.getRequestId());
                controller.complete(operation);
                return new CompletableFuture<>();
            }

            Integer retry = params.getRetry();
            if (retry == null || retry <= 0) {
                // no retries - already marked as FAILURE, so just return it
                logger.info("operation {} no retries for {}", getFullName(), params.getRequestId());
                controller.complete(operation);
                return new CompletableFuture<>();
            }


            /*
             * Retry the operation.
             */
            logger.info("retry operation {} for {}", getFullName(), params.getRequestId());

            return startOperationAttempt(params, controller, attempt + 1);
        };
    }

    /**
     * Converts an exception into an operation outcome, returning a copy of the outcome to
     * prevent background jobs from changing it.
     *
     * @param params operation parameters
     * @param type type of item throwing the exception
     * @return a function that will convert an exception into an operation outcome
     */
    private Function<Throwable, OperationOutcome> fromException(ControlLoopOperationParams params, String type) {

        return thrown -> {
            OperationOutcome outcome = params.makeOutcome();

            logger.warn("exception throw by {} {}.{} for {}", type, outcome.getActor(), outcome.getOperation(),
                            params.getRequestId(), thrown);

            return setOutcome(params, outcome, thrown);
        };
    }

    /**
     * Similar to {@link CompletableFuture#anyOf(CompletableFuture...)}, but it cancels
     * any outstanding futures when one completes.
     *
     * @param params operation parameters
     * @param futures futures for which to wait
     * @return a future to cancel or await an outcome. If this future is canceled, then
     *         all of the futures will be canceled
     */
    protected CompletableFuture<OperationOutcome> anyOf(ControlLoopOperationParams params,
                    List<CompletableFuture<OperationOutcome>> futures) {

        // convert list to an array
        @SuppressWarnings("rawtypes")
        CompletableFuture[] arrFutures = futures.toArray(new CompletableFuture[futures.size()]);

        @SuppressWarnings("unchecked")
        CompletableFuture<OperationOutcome> result = anyOf(params, arrFutures);
        return result;
    }

    /**
     * Same as {@link CompletableFuture#anyOf(CompletableFuture...)}, but it cancels any
     * outstanding futures when one completes.
     *
     * @param params operation parameters
     * @param futures futures for which to wait
     * @return a future to cancel or await an outcome. If this future is canceled, then
     *         all of the futures will be canceled
     */
    protected CompletableFuture<OperationOutcome> anyOf(ControlLoopOperationParams params,
                    @SuppressWarnings("unchecked") CompletableFuture<OperationOutcome>... futures) {

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
     * @param params operation parameters
     * @param futures futures for which to wait
     * @return a future to cancel or await an outcome. If this future is canceled, then
     *         all of the futures will be canceled
     */
    protected CompletableFuture<OperationOutcome> allOf(ControlLoopOperationParams params,
                    List<CompletableFuture<OperationOutcome>> futures) {

        // convert list to an array
        @SuppressWarnings("rawtypes")
        CompletableFuture[] arrFutures = futures.toArray(new CompletableFuture[futures.size()]);

        @SuppressWarnings("unchecked")
        CompletableFuture<OperationOutcome> result = allOf(params, arrFutures);
        return result;
    }

    /**
     * Same as {@link CompletableFuture#allOf(CompletableFuture...)}, but it cancels the
     * futures if returned future is canceled. The future returns the "worst" outcome,
     * based on priority (see {@link #detmPriority(OperationOutcome)}).
     *
     * @param params operation parameters
     * @param futures futures for which to wait
     * @return a future to cancel or await an outcome. If this future is canceled, then
     *         all of the futures will be canceled
     */
    protected CompletableFuture<OperationOutcome> allOf(ControlLoopOperationParams params,
                    @SuppressWarnings("unchecked") CompletableFuture<OperationOutcome>... futures) {

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

        CompletableFuture.allOf(futures2).whenComplete(combineOutcomes(params, controller, outcomes));

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

        // attach each task
        for (CompletableFuture<OperationOutcome> future : futures) {
            controller.add(future);
        }
    }

    /**
     * Combines the outcomes from a set of tasks.
     *
     * @param params operation parameters
     * @param future future to be completed with the combined result
     * @param outcomes outcomes to be examined
     */
    private BiConsumer<Void, Throwable> combineOutcomes(ControlLoopOperationParams params,
                    CompletableFuture<OperationOutcome> future, OperationOutcome[] outcomes) {

        return (unused, thrown) -> {
            if (thrown != null) {
                future.completeExceptionally(thrown);
                return;
            }

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

            logger.trace("{}: combined outcome of tasks is {} for {}", getFullName(),
                            (outcome == null ? null : outcome.getResult()), params.getRequestId());

            future.complete(outcome);
        };
    }

    /**
     * Determines the priority of an outcome based on its result.
     *
     * @param outcome outcome to examine, or {@code null}
     * @return the outcome's priority
     */
    protected int detmPriority(OperationOutcome outcome) {
        if (outcome == null) {
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
     * @param params operation parameters
     * @param controller overall pipeline controller
     * @param checkSuccess {@code true} to check the previous outcome, {@code false}
     *        otherwise
     * @param outcome outcome of the previous task
     * @param tasks tasks to be performed
     * @return a function to perform the task. If everything checks out, then it returns
     *         the task's future. Otherwise, it returns an incomplete future and completes
     *         the controller instead.
     */
    // @formatter:off
    protected CompletableFuture<OperationOutcome> doTask(ControlLoopOperationParams params,
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
     * @param params operation parameters
     * @param controller overall pipeline controller
     * @param checkSuccess {@code true} to check the previous outcome, {@code false}
     *        otherwise
     * @param tasks tasks to be performed
     * @return a function to perform the task. If everything checks out, then it returns
     *         the task's future. Otherwise, it returns an incomplete future and completes
     *         the controller instead.
     */
    // @formatter:off
    protected Function<OperationOutcome, CompletableFuture<OperationOutcome>> doTask(ControlLoopOperationParams params,
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
     * @param params operation parameters
     * @param callbacks used to determine if the start callback can be invoked
     * @return a function that sets the start time and invokes the callback
     */
    private BiConsumer<OperationOutcome, Throwable> callbackStarted(ControlLoopOperationParams params,
                    CallbackManager callbacks) {

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
     * @param params operation parameters
     * @param callbacks used to determine if the end callback can be invoked
     * @return a function that sets the end time and invokes the callback
     */
    private BiConsumer<OperationOutcome, Throwable> callbackCompleted(ControlLoopOperationParams params,
                    CallbackManager callbacks) {

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
     * @param params operation parameters
     * @param operation operation to be updated
     * @return the updated operation
     */
    protected OperationOutcome setOutcome(ControlLoopOperationParams params, OperationOutcome operation,
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
    protected OperationOutcome setOutcome(ControlLoopOperationParams params, OperationOutcome operation,
                    PolicyResult result) {
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

    // these may be overridden by junit tests

    /**
     * Gets the operation timeout. Subclasses may override this method to obtain the
     * timeout in some other way (e.g., through configuration properties).
     *
     * @param timeoutSec timeout, in seconds, or {@code null}
     * @return the operation timeout, in milliseconds
     */
    protected long getTimeOutMillis(Integer timeoutSec) {
        return (timeoutSec == null ? 0 : TimeUnit.MILLISECONDS.convert(timeoutSec, TimeUnit.SECONDS));
    }
}
