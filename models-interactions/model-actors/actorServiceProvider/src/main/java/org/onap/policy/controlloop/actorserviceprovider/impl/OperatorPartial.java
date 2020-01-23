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
import org.onap.policy.controlloop.actorserviceprovider.PipelineController;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial implementation of an operator. Subclasses can choose to simply implement
 * {@link #doOperation(ControlLoopOperationParams)}, or they may choose to override
 * {@link #doOperationAsFuture(ControlLoopOperationParams)}.
 */
public abstract class OperatorPartial extends ConfigurablePartial<Map<String, Object>> implements Operator {

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
    public final CompletableFuture<ControlLoopOperation> startOperation(ControlLoopOperationParams params) {
        if (!isAlive()) {
            throw new IllegalStateException("operation is not running: " + getFullName());
        }

        final Executor executor = params.getExecutor();

        // preprocessor gets its own sub parameters
        CompletableFuture<ControlLoopOperation> preproc = startPreprocessor(makeSubParameters(params));
        if (preproc == null) {
            // no preprocessor required - just start the operation
            return startOperationOnly(params).thenApplyAsync(stopPipeline(params), executor);
        }

        /*
         * Do preprocessor first and then, if successful, start the operation. Note:
         * operations create their own outcome, ignoring the outcome from any previous
         * steps.
         */
        return preproc.thenComposeAsync(handleFailure(params), executor)
                        .thenComposeAsync(onSuccess(unused -> startOperationOnly(params)), executor)
                        .thenApplyAsync(stopPipeline(params), executor);
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
        final Executor executor = params.getExecutor();
        final ControlLoopOperation operation = params.makeOutcome();

        Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> preproc =
                        doPreprocessorAsFuture(params);
        if (preproc == null) {
            // no preprocessor required
            return null;
        }

        /*
         * Don't mark it complete until we've built the whole pipeline. This will prevent
         * the operation from starting until after it has been successfully built (i.e.,
         * without generating any exceptions).
         */
        final CompletableFuture<ControlLoopOperation> firstFuture = new CompletableFuture<>();

        // @formatter:off
        CompletableFuture<ControlLoopOperation> future2 = firstFuture.thenComposeAsync(verifyRunning(params), executor)
                        .thenComposeAsync(preproc, executor)
                        .exceptionally(fromException(params, operation))
                        .thenApplyAsync(stopPipeline(params), executor);
        // @formatter:on

        // start the pipeline
        firstFuture.complete(operation);

        return future2;
    }

    /**
     * Handles a failure in the preprocessor pipeline. If a failure occurred, then it
     * invokes the call-backs and returns a failed outcome. Otherwise, it returns the
     * outcome that it received.
     *
     * @param params operation parameters
     * @return a function that checks the outcome status and continues, if successful, or
     *         indicates a failure otherwise
     */
    private Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> handleFailure(
                    ControlLoopOperationParams params) {

        return outcome -> {

            if (isSuccess(outcome)) {
                return CompletableFuture.completedFuture(outcome);
            }

            logger.warn("preprocessor failed, discontinuing operation {} for {}", getFullName(), params.getRequestId());

            final Executor executor = params.getExecutor();
            final AtomicReference<Instant> startTime = new AtomicReference<>();
            final ControlLoopOperation outcome2 = params.makeOutcome();

            // TODO need a FAILURE_MISSING_DATA (e.g., A&AI)

            outcome2.setOutcome(PolicyResult.FAILURE_GUARD.toString());
            outcome2.setMessage(outcome.getMessage());

            return CompletableFuture.completedFuture(outcome2)
                            .thenApplyAsync(callbackStarted(params, startTime), executor)
                            .thenApplyAsync(callbackCompleted(params, startTime), executor);
        };
    }

    /**
     * Invokes the operation's preprocessor step(s) as a "future". This method simply
     * returns {@code null}.
     * <p/>
     * This method assumes following:
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
     * Starts the operation, but with no preprocessor. Performs retries, if appropriate.
     *
     * @param params operation parameters
     * @return a future that will return the result of the operation
     */
    private CompletableFuture<ControlLoopOperation> startOperationOnly(ControlLoopOperationParams params) {
        // parameters to be used for one single execution
        final ControlLoopOperationParams params1 = makeSubParameters(params);

        /*
         * Note: MUST use "params", not "params1", in the call to retryOnFailure(),
         * because the method may need to stop the whole pipeline, not just the currently
         * running instance.
         */
        return startOperationOnce(params1).thenComposeAsync(retryOnFailure(params, 1), params.getExecutor());
    }

    /**
     * Makes parameters to be used by a subtask. Builds a copy of the given parameters,
     * but with the attempt count set to one and the pipeline controller attached to the
     * parent's pipeline controller.
     *
     * @param params operation parameters
     * @return new subtask parameters
     */
    protected ControlLoopOperationParams makeSubParameters(ControlLoopOperationParams params) {
        // @formatter:off
        return params.toBuilder()
                        .attempt(1)
                        .pipelineController(new PipelineController(params.getPipelineController()))
                        .build();
        // @formatter:on
    }

    /**
     * Starts the operation, with no preprocessor, running it once, without retries.
     *
     * @param params operation parameters
     * @return a future that will return the result of the operation
     */
    private CompletableFuture<ControlLoopOperation> startOperationOnce(ControlLoopOperationParams params) {

        final Executor executor = params.getExecutor();
        final ControlLoopOperation outcome = params.makeOutcome();

        /*
         * Don't mark it complete until we've built the whole pipeline. This will prevent
         * the operation from starting until after it has been successfully built (i.e.,
         * without generating any exceptions).
         */
        final CompletableFuture<ControlLoopOperation> firstFuture = new CompletableFuture<>();
        final AtomicReference<Instant> startTime = new AtomicReference<>();

        // @formatter:off
        CompletableFuture<ControlLoopOperation> future2 = firstFuture.thenComposeAsync(verifyRunning(params), executor)
                        .thenApplyAsync(callbackStarted(params, startTime), executor)
                        .thenComposeAsync(doOperationAsFuture(params), executor);
        // @formatter:on

        long timeoutMillis = getTimeOutMillis(params.getPolicy());
        if (timeoutMillis > 0) {
            future2 = future2.orTimeout(timeoutMillis, TimeUnit.MILLISECONDS);
        }

        /*
         * Note: we re-invoke callbackStarted() just to be sure the callback is invoked
         * before callbackCompleted() is invoked.
         */

        // @formatter:off
        future2 = future2.exceptionally(fromException(params, outcome))
                        .thenApplyAsync(setRetryFlag(params), executor)
                        .thenApplyAsync(callbackStarted(params, startTime), executor)
                        .thenApplyAsync(callbackCompleted(params, startTime), executor)
                        .thenApplyAsync(stopPipeline(params), executor);
        // @formatter:on

        // start the pipeline
        firstFuture.complete(outcome);

        return future2;
    }

    /**
     * Determines if the outcome was successful.
     *
     * @param outcome outcome to examine
     * @return {@code true} if the outcome was successful, {@code false} otherwise
     */
    protected boolean isSuccess(ControlLoopOperation outcome) {
        return OUTCOME_SUCCESS.equals(outcome.getOutcome());
    }

    /**
     * Determines if the outcome was a failure for this operator.
     *
     * @param outcome outcome to examine
     * @return {@code true} if the outcome was a failure <i>and</i> it was associated with
     *         this operator, {@code false} otherwise
     */
    protected boolean isActorFailed(ControlLoopOperation outcome) {
        return OUTCOME_FAILURE.equals(getActorOutcome(outcome));
    }

    /**
     * Invokes the operation as a "future". This method simply invokes
     * {@link #doOperation(ControlLoopOperationParams)} turning it into a "future".
     * <p/>
     * This method assumes following:
     * <ul>
     * <li>the operator is alive</li>
     * <li>verifyRunning() has been invoked</li>
     * <li>callbackStarted() has been invoked</li>
     * <li>the invoker will perform appropriate timeout checks</li>
     * <li>exceptions generated within the pipeline will be handled by the invoker</li>
     * </ul>
     *
     * @param params operation parameters
     * @return a function that will start the operation and return its result when
     *         complete
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
     * Determines if retries are necessary. If the current operation outcome is FAILURE,
     * then it changes it to FAILURE_RETRIES, assuming the policy specifies retries and
     * the retry count has been exhausted.
     *
     * @param params operation parameters
     * @return a function to get the next future to execute
     */
    private Function<ControlLoopOperation, ControlLoopOperation> setRetryFlag(ControlLoopOperationParams params) {

        return operation -> {
            if (!isActorFailed(operation)) {
                // wrong type or wrong operation - just leave it as is
                return operation;
            }

            if (params.getPolicy().getRetry() != null && params.getPolicy().getRetry() > 0
                            && params.getAttempt() > params.getPolicy().getRetry()) {
                /*
                 * retries were specified and we've already tried them all - change to
                 * FAILURE_RETRIES
                 */
                logger.info("operation {} retries exhausted for {}", getFullName(), params.getRequestId());
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
    private Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> retryOnFailure(
                    ControlLoopOperationParams parentParams, int attempt) {

        return operation -> {
            if (!isActorFailed(operation)) {
                // wrong type or wrong operation - just leave it as is
                return CompletableFuture.completedFuture(operation);
            }

            if (parentParams.getPolicy().getRetry() == null || parentParams.getPolicy().getRetry() <= 0) {
                // no retries - already marked as FAILURE, so just return it
                logger.info("operation {} no retries for {}", getFullName(), parentParams.getRequestId());
                return CompletableFuture.completedFuture(operation);
            }


            /*
             * Retry the operation.
             */
            logger.info("retry operation {} for {}", getFullName(), parentParams.getRequestId());

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
     * @return the outcome of the given operation, if it's for this operator, {@code null}
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
     * Gets a function that will start the next step, if the current operation was
     * successful, or just return the current operation, otherwise.
     *
     * @param params operation parameters
     * @param nextStep function that will invoke the next step, passing it the operation
     * @return a function that will start the next step
     */
    protected Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> onSuccess(
                    Function<ControlLoopOperation, CompletableFuture<ControlLoopOperation>> nextStep) {

        return operation -> {
            if (isSuccess(operation)) {
                return nextStep.apply(operation);

            } else {
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
            return setOutcome(new ControlLoopOperation(operation), thrown);
        };
    }

    /**
     * Gets a function to verify that the operation is still running. If the pipeline is
     * not running, then it returns an incomplete future, which will effectively halt
     * subsequent operations in the pipeline. This method is intended to be used with one
     * of the {@link CompletableFuture}'s <i>thenCompose()</i> methods.
     *
     * @param params operation parameters
     * @return a function to verify that the operation is still running
     */
    protected <T> Function<T, CompletableFuture<T>> verifyRunning(ControlLoopOperationParams params) {
        PipelineController controller = params.getPipelineController();
        return value -> (controller.isRunning() ? CompletableFuture.completedFuture(value) : new CompletableFuture<>());
    }

    /**
     * Sets the start time of the operation and invokes the callback to indicate that the
     * operation has started. Does nothing if the pipeline has been stopped.
     *
     * @param params operation parameters
     * @param startTime populated with the start time. If already set, then the callback
     *        is not invoked
     * @return a function that sets the start time and invokes the callback
     */
    private Function<ControlLoopOperation, ControlLoopOperation> callbackStarted(ControlLoopOperationParams params,
                    AtomicReference<Instant> startTime) {

        return operation -> {

            params.getPipelineController().doIfRunning(() -> {

                if (startTime.compareAndSet(null, Instant.now())) {
                    // haven't invoked "start" callback yet
                    operation.setStart(startTime.get());
                    operation.setEnd(null);
                    params.callbackStarted(operation);
                }
            });

            return operation;
        };
    }

    /**
     * Sets the end time of the operation and invokes the callback to indicate that the
     * operation has completed. Does nothing if the pipeline has been stopped.
     * <p/>
     * Note: the start time must be a reference rather than a plain value, because it's
     * value must be gotten on-demand, when the returned function is executed at a later
     * time.
     *
     * @param params operation parameters
     * @param startTime value to be put in the "start" time field
     * @return a function that sets the end time and invokes the callback
     */
    private Function<ControlLoopOperation, ControlLoopOperation> callbackCompleted(ControlLoopOperationParams params,
                    AtomicReference<Instant> startTime) {

        return operation -> {

            params.getPipelineController().doIfRunning(() -> {
                operation.setStart(startTime.get());
                operation.setEnd(Instant.now());
                params.callbackCompleted(operation);
            });

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
    protected boolean isTimeout(Throwable thrown) {
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
