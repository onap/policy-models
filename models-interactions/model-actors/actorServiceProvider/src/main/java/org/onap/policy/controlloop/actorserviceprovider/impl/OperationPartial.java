/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.OperatorConfig;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial implementation of an operator. In general, it's preferable that subclasses
 * would override {@link #startOperationAsync(int, OperationOutcome)
 * startOperationAsync()}. However, if that proves to be too difficult, then they can
 * simply override {@link #doOperation(int, OperationOutcome) doOperation()}.
 * <p/>
 * The futures returned by the methods within this class can be canceled, and will
 * propagate the cancellation to any subtasks. Thus it is also expected that any futures
 * returned by overridden methods will do the same. Of course, if a class overrides
 * {@link #doOperation(int, OperationOutcome) doOperation()}, then there's little that can
 * be done to cancel that particular operation.
 * <p/>
 * In general tasks in a pipeline are executed by the same thread. However, the following
 * should always be executed via the executor specified in "params":
 * <ul>
 * <li>start callback</li>
 * <li>completion callback</li>
 * <li>controller completion (i.e., delayedComplete())</li>
 * </ul>
 */
public abstract class OperationPartial implements Operation {
    private static final Logger logger = LoggerFactory.getLogger(OperationPartial.class);
    private static final Coder coder = new StandardCoder();

    public static final String GUARD_ACTOR_NAME = "GUARD";
    public static final String GUARD_OPERATION_NAME = "Decision";
    public static final long DEFAULT_RETRY_WAIT_MS = 1000L;

    private final OperatorConfig config;

    /**
     * Operation parameters.
     */
    protected final ControlLoopOperationParams params;

    @Getter
    private final String fullName;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String subRequestId;

    @Getter
    private final List<String> propertyNames;

    /**
     * Values for the properties identified by {@link #getPropertyNames()}.
     */
    private final Map<String, Object> properties = new HashMap<>();


    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     * @param propertyNames names of properties required by this operation
     */
    protected OperationPartial(ControlLoopOperationParams params, OperatorConfig config, List<String> propertyNames) {
        this.params = params;
        this.config = config;
        this.fullName = params.getActor() + "." + params.getOperation();
        this.propertyNames = propertyNames;
    }

    public Executor getBlockingExecutor() {
        return config.getBlockingExecutor();
    }

    @Override
    public String getActorName() {
        return params.getActor();
    }

    @Override
    public String getName() {
        return params.getOperation();
    }

    @Override
    public boolean containsProperty(String name) {
        return properties.containsKey(name);
    }

    @Override
    public void setProperty(String name, Object value) {
        logger.info("{}: set property {}={}", getFullName(), name, value);
        properties.put(name, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String name) {
        return (T) properties.get(name);
    }

    /**
     * Gets a property value, throwing an exception if it's missing.
     *
     * @param name property name
     * @param propertyType property type, used in an error message if the property value
     *        is {@code null}
     * @return the property value
     */
    @SuppressWarnings("unchecked")
    protected <T> T getRequiredProperty(String name, String propertyType) {
        T value = (T) properties.get(name);
        if (value == null) {
            throw new IllegalStateException("missing " + propertyType);
        }

        return value;
    }

    @Override
    public CompletableFuture<OperationOutcome> start() {
        // allocate a controller for the entire operation
        final PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        // start attempt #1
        return startOperationAttempt(controller, 1);
    }

    /**
     * Starts the operation attempt. When all retries complete, it will complete the
     * controller.
     *
     * @param controller controller for all operation attempts
     * @param attempt attempt number, typically starting with 1
     * @return a future that will return the final result of all attempts
     */
    private CompletableFuture<OperationOutcome> startOperationAttempt(
                    PipelineControllerFuture<OperationOutcome> controller, int attempt) {

        generateSubRequestId(attempt);

        // propagate "stop" to the operation attempt
        controller.wrap(startAttemptWithoutRetries(attempt)).thenCompose(retryOnFailure(controller, attempt))
                        .whenCompleteAsync(controller.delayedComplete(), params.getExecutor());

        return controller;
    }

    /**
     * Generates and sets {@link #subRequestId} to a new subrequest ID.
     *
     * @param attempt attempt number, typically starting with 1
     */
    public void generateSubRequestId(int attempt) {
        // Note: this should be "protected", but that makes junits much messier

        setSubRequestId(UUID.randomUUID().toString());
    }

    /**
     * Starts the operation attempt, without doing any retries.
     *
     * @param attempt attempt number, typically starting with 1
     * @return a future that will return the result of a single operation attempt
     */
    private CompletableFuture<OperationOutcome> startAttemptWithoutRetries(int attempt) {

        logger.info("{}: start operation attempt {} for {}", getFullName(), attempt, params.getRequestId());

        final Executor executor = params.getExecutor();
        final OperationOutcome outcome = makeOutcome();
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
        return (outcome != null && outcome.getResult() == OperationResult.SUCCESS);
    }

    /**
     * Determines if the outcome was a failure for this operator.
     *
     * @param outcome outcome to examine, or {@code null}
     * @return {@code true} if the outcome is not {@code null} and was a failure
     *         <i>and</i> was associated with this operator, {@code false} otherwise
     */
    protected boolean isActorFailed(OperationOutcome outcome) {
        return (isSameOperation(outcome) && outcome.getResult() == OperationResult.FAILURE);
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

        return origOutcome -> {
            // ensure we have a non-null outcome
            OperationOutcome outcome;
            if (origOutcome != null) {
                outcome = origOutcome;
            } else {
                logger.warn("{}: null outcome; treating as a failure for {}", getFullName(), params.getRequestId());
                outcome = this.setOutcome(makeOutcome(), OperationResult.FAILURE);
            }

            // ensure correct actor/operation
            outcome.setActor(getActorName());
            outcome.setOperation(getName());

            // determine if we should retry, based on the result
            if (outcome.getResult() != OperationResult.FAILURE) {
                // do not retry success or other failure types (e.g., exception)
                outcome.setFinalOutcome(true);
                return outcome;
            }

            int retry = getRetry(params.getRetry());
            if (retry <= 0) {
                // no retries were specified
                outcome.setFinalOutcome(true);

            } else if (attempt <= retry) {
                // have more retries - not the final outcome
                outcome.setFinalOutcome(false);

            } else {
                /*
                 * retries were specified and we've already tried them all - change to
                 * FAILURE_RETRIES
                 */
                logger.info("operation {} retries exhausted for {}", getFullName(), params.getRequestId());
                outcome.setResult(OperationResult.FAILURE_RETRIES);
                outcome.setFinalOutcome(true);
            }

            return outcome;
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
            OperationOutcome outcome = makeOutcome();

            if (thrown instanceof CancellationException || thrown.getCause() instanceof CancellationException) {
                // do not include exception in the message, as it just clutters the log
                logger.warn("{} canceled {}.{} for {}", type, outcome.getActor(), outcome.getOperation(),
                                params.getRequestId());
            } else {
                logger.warn("exception thrown by {} {}.{} for {}", type, outcome.getActor(), outcome.getOperation(),
                                params.getRequestId(), thrown);
            }

            return setOutcome(outcome, thrown);
        };
    }

    /**
     * Similar to {@link CompletableFuture#anyOf(CompletableFuture...)}, but it cancels
     * any outstanding futures when one completes.
     *
     * @param futureMakers function to make a future. If the function returns
     *        {@code null}, then no future is created for that function. On the other
     *        hand, if the function throws an exception, then the previously created
     *        functions are canceled and the exception is re-thrown
     * @return a future to cancel or await an outcome, or {@code null} if no futures were
     *         created. If this future is canceled, then all of the futures will be
     *         canceled
     */
    public CompletableFuture<OperationOutcome> anyOf(
                    @SuppressWarnings("unchecked") Supplier<CompletableFuture<OperationOutcome>>... futureMakers) {

        return anyOf(Arrays.asList(futureMakers));
    }

    /**
     * Similar to {@link CompletableFuture#anyOf(CompletableFuture...)}, but it cancels
     * any outstanding futures when one completes.
     *
     * @param futureMakers function to make a future. If the function returns
     *        {@code null}, then no future is created for that function. On the other
     *        hand, if the function throws an exception, then the previously created
     *        functions are canceled and the exception is re-thrown
     * @return a future to cancel or await an outcome, or {@code null} if no futures were
     *         created. If this future is canceled, then all of the futures will be
     *         canceled. Similarly, when this future completes, any incomplete futures
     *         will be canceled
     */
    public CompletableFuture<OperationOutcome> anyOf(List<Supplier<CompletableFuture<OperationOutcome>>> futureMakers) {

        PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        CompletableFuture<OperationOutcome>[] futures =
                        attachFutures(controller, futureMakers, UnaryOperator.identity());

        if (futures.length == 0) {
            // no futures were started
            return null;
        }

        if (futures.length == 1) {
            return futures[0];
        }

        CompletableFuture.anyOf(futures).thenApply(OperationOutcome.class::cast)
                        .whenCompleteAsync(controller.delayedComplete(), params.getExecutor());

        return controller;
    }

    /**
     * Similar to {@link CompletableFuture#allOf(CompletableFuture...)}.
     *
     * @param futureMakers function to make a future. If the function returns
     *        {@code null}, then no future is created for that function. On the other
     *        hand, if the function throws an exception, then the previously created
     *        functions are canceled and the exception is re-thrown
     * @return a future to cancel or await an outcome, or {@code null} if no futures were
     *         created. If this future is canceled, then all of the futures will be
     *         canceled
     */
    public CompletableFuture<OperationOutcome> allOf(
                    @SuppressWarnings("unchecked") Supplier<CompletableFuture<OperationOutcome>>... futureMakers) {

        return allOf(Arrays.asList(futureMakers));
    }

    /**
     * Similar to {@link CompletableFuture#allOf(CompletableFuture...)}.
     *
     * @param futureMakers function to make a future. If the function returns
     *        {@code null}, then no future is created for that function. On the other
     *        hand, if the function throws an exception, then the previously created
     *        functions are canceled and the exception is re-thrown
     * @return a future to cancel or await an outcome, or {@code null} if no futures were
     *         created. If this future is canceled, then all of the futures will be
     *         canceled. Similarly, when this future completes, any incomplete futures
     *         will be canceled
     */
    public CompletableFuture<OperationOutcome> allOf(List<Supplier<CompletableFuture<OperationOutcome>>> futureMakers) {
        PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        Queue<OperationOutcome> outcomes = new LinkedList<>();

        CompletableFuture<OperationOutcome>[] futures =
                        attachFutures(controller, futureMakers, future -> future.thenApply(outcome -> {
                            synchronized (outcomes) {
                                outcomes.add(outcome);
                            }
                            return outcome;
                        }));

        if (futures.length == 0) {
            // no futures were started
            return null;
        }

        if (futures.length == 1) {
            return futures[0];
        }

        // @formatter:off
        CompletableFuture.allOf(futures)
                        .thenApply(unused -> combineOutcomes(outcomes))
                        .whenCompleteAsync(controller.delayedComplete(), params.getExecutor());
        // @formatter:on

        return controller;
    }

    /**
     * Invokes the functions to create the futures and attaches them to the controller.
     *
     * @param controller master controller for all of the futures
     * @param futureMakers futures to be attached to the controller
     * @param adorn function that "adorns" the future, possible adding onto its pipeline.
     *        Returns the adorned future
     * @return an array of futures, possibly zero-length. If the array is of size one,
     *         then that one item should be returned instead of the controller
     */
    private CompletableFuture<OperationOutcome>[] attachFutures(PipelineControllerFuture<OperationOutcome> controller,
                    List<Supplier<CompletableFuture<OperationOutcome>>> futureMakers,
                    UnaryOperator<CompletableFuture<OperationOutcome>> adorn) {

        if (futureMakers.isEmpty()) {
            @SuppressWarnings("unchecked")
            CompletableFuture<OperationOutcome>[] result = new CompletableFuture[0];
            return result;
        }

        // the last, unadorned future that is created
        CompletableFuture<OperationOutcome> lastFuture = null;

        List<CompletableFuture<OperationOutcome>> futures = new ArrayList<>(futureMakers.size());

        // make each future
        for (var maker : futureMakers) {
            try {
                CompletableFuture<OperationOutcome> future = maker.get();
                if (future == null) {
                    continue;
                }

                // propagate "stop" to the future
                controller.add(future);

                futures.add(adorn.apply(future));

                lastFuture = future;

            } catch (RuntimeException e) {
                logger.warn("{}: exception creating 'future' for {}", getFullName(), params.getRequestId());
                controller.cancel(false);
                throw e;
            }
        }

        @SuppressWarnings("unchecked")
        CompletableFuture<OperationOutcome>[] result = new CompletableFuture[futures.size()];

        if (result.length == 1) {
            // special case - return the unadorned future
            result[0] = lastFuture;
            return result;
        }

        return futures.toArray(result);
    }

    /**
     * Combines the outcomes from a set of tasks.
     *
     * @param outcomes outcomes to be examined
     * @return the combined outcome
     */
    private OperationOutcome combineOutcomes(Queue<OperationOutcome> outcomes) {

        // identify the outcome with the highest priority
        OperationOutcome outcome = outcomes.remove();
        int priority = detmPriority(outcome);

        for (OperationOutcome outcome2 : outcomes) {
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
     * Performs a sequence of tasks, stopping if a task fails. A given task's future is
     * not created until the previous task completes. The pipeline returns the outcome of
     * the last task executed.
     *
     * @param futureMakers functions to make the futures
     * @return a future to cancel the sequence or await the outcome
     */
    public CompletableFuture<OperationOutcome> sequence(
                    @SuppressWarnings("unchecked") Supplier<CompletableFuture<OperationOutcome>>... futureMakers) {

        return sequence(Arrays.asList(futureMakers));
    }

    /**
     * Performs a sequence of tasks, stopping if a task fails. A given task's future is
     * not created until the previous task completes. The pipeline returns the outcome of
     * the last task executed.
     *
     * @param futureMakers functions to make the futures
     * @return a future to cancel the sequence or await the outcome, or {@code null} if
     *         there were no tasks to perform
     */
    public CompletableFuture<OperationOutcome> sequence(
                    List<Supplier<CompletableFuture<OperationOutcome>>> futureMakers) {

        Queue<Supplier<CompletableFuture<OperationOutcome>>> queue = new ArrayDeque<>(futureMakers);

        CompletableFuture<OperationOutcome> nextTask = getNextTask(queue);
        if (nextTask == null) {
            // no tasks
            return null;
        }

        if (queue.isEmpty()) {
            // only one task - just return it rather than wrapping it in a controller
            return nextTask;
        }

        /*
         * multiple tasks - need a controller to stop whichever task is currently
         * executing
         */
        final PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();
        final Executor executor = params.getExecutor();

        // @formatter:off
        controller.wrap(nextTask)
                    .thenCompose(nextTaskOnSuccess(controller, queue))
                    .whenCompleteAsync(controller.delayedComplete(), executor);
        // @formatter:on

        return controller;
    }

    /**
     * Executes the next task in the queue, if the previous outcome was successful.
     *
     * @param controller pipeline controller
     * @param taskQueue queue of tasks to be performed
     * @return a future to execute the remaining tasks, or the current outcome, if it's a
     *         failure, or if there are no more tasks
     */
    private Function<OperationOutcome, CompletableFuture<OperationOutcome>> nextTaskOnSuccess(
                    PipelineControllerFuture<OperationOutcome> controller,
                    Queue<Supplier<CompletableFuture<OperationOutcome>>> taskQueue) {

        return outcome -> {
            if (!isSuccess(outcome)) {
                // return the failure
                return CompletableFuture.completedFuture(outcome);
            }

            CompletableFuture<OperationOutcome> nextTask = getNextTask(taskQueue);
            if (nextTask == null) {
                // no tasks - just return the success
                return CompletableFuture.completedFuture(outcome);
            }

            // @formatter:off
            return controller
                        .wrap(nextTask)
                        .thenCompose(nextTaskOnSuccess(controller, taskQueue));
            // @formatter:on
        };
    }

    /**
     * Gets the next task from the queue, skipping those that are {@code null}.
     *
     * @param taskQueue task queue
     * @return the next task, or {@code null} if the queue is now empty
     */
    private CompletableFuture<OperationOutcome> getNextTask(
                    Queue<Supplier<CompletableFuture<OperationOutcome>>> taskQueue) {

        Supplier<CompletableFuture<OperationOutcome>> maker;

        while ((maker = taskQueue.poll()) != null) {
            CompletableFuture<OperationOutcome> future = maker.get();
            if (future != null) {
                return future;
            }
        }

        return null;
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
    protected BiConsumer<OperationOutcome, Throwable> callbackStarted(CallbackManager callbacks) {

        return (outcome, thrown) -> {

            if (callbacks.canStart()) {
                outcome.setSubRequestId(getSubRequestId());
                outcome.setStart(callbacks.getStartTime());
                outcome.setEnd(null);

                // pass a copy to the callback
                OperationOutcome outcome2 = new OperationOutcome(outcome);
                outcome2.setFinalOutcome(false);
                params.callbackStarted(outcome2);
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
    protected BiConsumer<OperationOutcome, Throwable> callbackCompleted(CallbackManager callbacks) {

        return (outcome, thrown) -> {
            if (callbacks.canEnd()) {
                outcome.setSubRequestId(getSubRequestId());
                outcome.setStart(callbacks.getStartTime());
                outcome.setEnd(callbacks.getEndTime());

                // pass a copy to the callback
                params.callbackCompleted(new OperationOutcome(outcome));
            }
        };
    }

    /**
     * Sets an operation's outcome and message, based on a throwable.
     *
     * @param operation operation to be updated
     * @return the updated operation
     */
    public OperationOutcome setOutcome(OperationOutcome operation, Throwable thrown) {
        OperationResult result = (isTimeout(thrown) ? OperationResult.FAILURE_TIMEOUT
                : OperationResult.FAILURE_EXCEPTION);
        return setOutcome(operation, result);
    }

    /**
     * Sets an operation's outcome and default message based on the result.
     *
     * @param operation operation to be updated
     * @param result result of the operation
     * @return the updated operation
     */
    public OperationOutcome setOutcome(OperationOutcome operation, OperationResult result) {
        logger.trace("{}: set outcome {} for {}", getFullName(), result, params.getRequestId());
        operation.setResult(result);
        operation.setMessage(result == OperationResult.SUCCESS ? ControlLoopOperation.SUCCESS_MSG
                        : ControlLoopOperation.FAILED_MSG);

        return operation;
    }

    /**
     * Makes an outcome, populating the "target" field with the contents of the target
     * entity property.
     *
     * @return a new operation outcome
     */
    protected OperationOutcome makeOutcome() {
        OperationOutcome outcome = params.makeOutcome();
        outcome.setTarget(getProperty(OperationProperties.AAI_TARGET_ENTITY));
        return outcome;
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
     * Logs a message. If the message is not of type, String, then it attempts to
     * pretty-print it into JSON before logging.
     *
     * @param direction IN or OUT
     * @param infra communication infrastructure on which it was published
     * @param source source name (e.g., the URL or Topic name)
     * @param message message to be logged
     * @return the JSON text that was logged
     */
    public <T> String logMessage(EventType direction, CommInfrastructure infra, String source, T message) {
        String json;
        try {
            json = prettyPrint(message);

        } catch (IllegalArgumentException e) {
            String type = (direction == EventType.IN ? "response" : "request");
            logger.warn("cannot pretty-print {}", type, e);
            json = message.toString();
        }

        logger.info("[{}|{}|{}|]{}{}", direction, infra, source, NetLoggerUtil.SYSTEM_LS, json);

        return json;
    }

    /**
     * Converts a message to a "pretty-printed" String using the operation's normal
     * serialization provider (i.e., it's <i>coder</i>).
     *
     * @param message response to be logged
     * @return the JSON text that was logged
     * @throws IllegalArgumentException if the message cannot be converted
     */
    public <T> String prettyPrint(T message) {
        if (message == null) {
            return null;
        } else if (message instanceof String) {
            return message.toString();
        } else {
            try {
                return getCoder().encode(message, true);
            } catch (CoderException e) {
                throw new IllegalArgumentException("cannot encode message", e);
            }
        }
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

    protected Coder getCoder() {
        return coder;
    }
}
