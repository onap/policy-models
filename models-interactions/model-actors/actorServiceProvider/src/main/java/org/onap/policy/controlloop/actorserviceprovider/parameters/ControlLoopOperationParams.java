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

package org.onap.policy.controlloop.actorserviceprovider.parameters;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.onap.policy.common.parameters.BeanValidationResult;
import org.onap.policy.common.parameters.BeanValidator;
import org.onap.policy.common.parameters.annotations.Min;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.PipelineController;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parameters for control loop operations. The executor defaults to
 * {@link ForkJoinPool#commonPool()}, but may be overridden.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
public class ControlLoopOperationParams {

    private static final Logger logger = LoggerFactory.getLogger(ControlLoopOperationParams.class);

    public static final String UNKNOWN = "-unknown-";
    private static final Policy EMPTY_POLICY = new Policy();
    private static final String OUTCOME_EXCEPTION = PolicyResult.FAILURE_EXCEPTION.toString();


    /**
     * The actor service in which to find the actor/operation.
     */
    @NotNull
    private ActorService actorService;

    /**
     * The attempt number, starting with 1.
     */
    @Min(1)
    private int attempt;

    /**
     * The event for which the operation applies.
     */
    @NotNull
    private ControlLoopEventContext context;

    /**
     * The executor to use to run the operation.
     */
    @NotNull
    @Builder.Default
    private Executor executor = ForkJoinPool.commonPool();

    /**
     * The pipeline controller. The operation will typically invoke
     * {@link PipelineController#stop()} if the operation fails.
     */
    @NotNull
    @Builder.Default
    private PipelineController pipelineController = new PipelineController();

    /**
     * The policy associated with the operation.
     */
    @NotNull
    private Policy policy;

    /**
     * The function to invoke when the operation starts. This is optional.
     * <p/>
     * Note: this may be invoked multiple times, but with different actor/operations. That
     * may happen if the current operation requires other operations to be performed first
     * (e.g., A&AI queries, guard checks).
     */
    private Consumer<ControlLoopOperation> startCallback;

    /**
     * The function to invoke when the operation completes. This is optional.
     * <p/>
     * Note: this may be invoked multiple times, but with different actor/operations. That
     * may happen if the current operation requires other operations to be performed first
     * (e.g., A&AI queries, guard checks).
     */
    private Consumer<ControlLoopOperation> completeCallback;

    /**
     * Target entity.
     */
    @NotNull
    private String target;


    /**
     * Starts the specified operation.
     *
     * @return a future that will return the result of the operation
     *         <p/>
     *         Note: canceling this future will not necessarily stop the operation from
     *         continuing to run in the background; use the {@link PipelineController} to
     *         stop it.
     */
    public CompletableFuture<ControlLoopOperation> start() {

        try {
            BeanValidationResult result = validate();
            if (!result.isValid()) {
                logger.warn("parameter error in operation {}.{} for {}:\n{}", getActor(), getOperation(),
                                getRequestId(), result);
                throw new IllegalArgumentException("invalid parameters");
            }

            // @formatter:off
            return actorService
                        .getActor(policy.getActor())
                        .getOperationManager(policy.getRecipe())
                        .startOperation(this);
            // @formatter:on

        } catch (RuntimeException e) {
            CompletableFuture<ControlLoopOperation> future = handleException(e);
            stopPipeline();
            return future;
        }
    }

    private CompletableFuture<ControlLoopOperation> handleException(Exception exception) {
        ControlLoopOperation operation = makeOutcome();
        logger.warn("exception starting operation {}.{} for {}", operation.getActor(), operation.getOperation(),
                        getRequestId(), exception);

        Runnable invokeCallbacks = () -> {
            callbackStarted(operation);

            operation.setOutcome(OUTCOME_EXCEPTION);
            operation.setMessage(ControlLoopOperation.FAILED_MSG);
            callbackCompleted(operation);
        };

        if (pipelineController == null) {
            invokeCallbacks.run();
        } else {
            pipelineController.doIfRunning(invokeCallbacks);
        }

        return CompletableFuture.completedFuture(operation);
    }

    /**
     * Gets the name of the actor from the policy.
     *
     * @return the actor name, or {@link #UNKNOWN} if no name is available
     */
    public String getActor() {
        return (policy == null || policy.getActor() == null ? UNKNOWN : policy.getActor());
    }

    /**
     * Gets the name of the operation from the policy.
     *
     * @return the actor name, or {@link #UNKNOWN} if no name is available
     */
    public String getOperation() {
        return (policy == null || policy.getRecipe() == null ? UNKNOWN : policy.getRecipe());
    }

    /**
     * Gets the requested ID of the associated event.
     *
     * @return the event's request ID, or {@code null} if it cannot be determined
     */
    public UUID getRequestId() {
        return (context == null || context.getEvent() == null ? null : context.getEvent().getRequestId());
    }

    /**
     * Makes an operation outcome, populating it from the parameters.
     *
     * @return a new operation outcome
     */
    public ControlLoopOperation makeOutcome() {
        Policy policy2 = policy;
        if (policy2 == null) {
            policy2 = EMPTY_POLICY;
        }

        ControlLoopOperation operation = new ControlLoopOperation();
        operation.setActor(policy2.getActor() == null ? UNKNOWN : policy2.getActor());
        operation.setOperation(policy2.getRecipe() == null ? UNKNOWN : policy2.getRecipe());
        operation.setTarget(target);
        operation.setSubRequestId(String.valueOf(attempt));

        return operation;
    }

    /**
     * Stops the pipeline via the controller, if one has been specified. This may be
     * invoked even if the parameter values are invalid.
     */
    public void stopPipeline() {
        if (pipelineController != null) {
            pipelineController.stop();
        }
    }

    /**
     * Invokes the callback to indicate that the operation has started. Any exceptions
     * generated by the callback are logged, but not re-thrown.
     *
     * @param operation the operation that is being started
     */
    public void callbackStarted(ControlLoopOperation operation) {
        logger.info("started operation {}.{} for {}", operation.getActor(), operation.getOperation(), getRequestId());

        if (startCallback != null) {
            Util.logException(() -> startCallback.accept(operation), operation.getActor() + "."
                            + operation.getOperation() + ": start-callback threw an exception");
        }
    }

    /**
     * Invokes the callback to indicate that the operation has completed. Any exceptions
     * generated by the callback are logged, but not re-thrown.
     *
     * @param operation the operation that is being started
     */
    public void callbackCompleted(ControlLoopOperation operation) {
        logger.info("completed operation {}.{} outcome={} for {}", operation.getActor(), operation.getOperation(),
                        operation.getOutcome(), getRequestId());

        if (completeCallback != null) {
            Util.logException(() -> completeCallback.accept(operation), operation.getActor() + "."
                            + operation.getOperation() + ": complete-callback threw an exception");
        }
    }

    /**
     * Validates the parameters.
     *
     * @return the validation result
     */
    public BeanValidationResult validate() {
        return new BeanValidator().validateTop(ControlLoopOperationParams.class.getSimpleName(), this);
    }
}
