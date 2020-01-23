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

package org.onap.policy.controlloop.actorserviceprovider.controlloop;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.PipelineController;
import org.onap.policy.controlloop.policy.Policy;

/**
 * Parameters for control loop operations. The executor defaults to
 * {@link ForkJoinPool#commonPool()}, but may be overridden.
 */
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class ControlLoopOperationParams {

    private static final Policy EMPTY_POLICY = new Policy();
    private static final String UNKNOWN = "unknown";


    /**
     * The attempt number, starting with 1.
     */
    private int attempt;

    /**
     * The event for which the operation applies.
     */
    private ControlLoopEventContext context;

    /**
     * The executor to use to run the operation.
     */
    @Builder.Default
    private Executor executor = ForkJoinPool.commonPool();

    /**
     * The pipeline controller. The operation will typically invoke
     * {@link PipelineController#stop()} if the operation fails.
     */
    @Builder.Default
    private PipelineController pipelineController = new PipelineController();

    /**
     * The policy associated with the operation.
     */
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
    private String target;

    /**
     * Constructs a builder for an operation that is to be run as a sub task of another
     * operation. Uses a new pipeline controller that is automatically stopped if the
     * parent's pipeline controller is stopped.
     *
     * @param parent parameters for the parent operation
     */
    public ControlLoopOperationParams(ControlLoopOperationParams parent) {
        this.attempt = 1;
        this.executor = parent.getExecutor();
        this.pipelineController = new PipelineController(parent.getPipelineController());
        this.policy = parent.getPolicy();
        this.target = parent.getTarget();
    }

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
            validateFields();

            // @formatter:off
            return ActorService.getInstance()
                        .getActor(policy.getActor())
                        .getOperationManager(policy.getRecipe())
                        .startOperation(this);
            // @formatter:on

        } catch (RuntimeException e) {
            stopPipeline();
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Makes an operation outcome, populating it from the parameters.
     *
     * @return a new operation outcome
     */
    public ControlLoopOperation makeOutcome() {
        Policy policy2 = getPolicy();
        if (policy2 == null) {
            policy2 = EMPTY_POLICY;
        }

        ControlLoopOperation operation = new ControlLoopOperation();
        operation.setActor(policy2.getActor() == null ? UNKNOWN : policy2.getActor());
        operation.setOperation(policy2.getRecipe() == null ? UNKNOWN : policy2.getRecipe());
        operation.setTarget(getTarget());
        operation.setSubRequestId(String.valueOf(getAttempt()));

        return operation;
    }

    /**
     * Stops the pipeline via the controller, if one has been specified. This may be
     * invoked even if the parameter values are invalid.
     */
    public void stopPipeline() {
        PipelineController controller = getPipelineController();
        if (controller != null) {
            controller.stop();
        }
    }

    private void validateFields() {
        validateNotNull("context", getContext());
        validateNotNull("executor", getExecutor());
        validateNotNull("policy", getPolicy());
        validateNotNull("policy.actor", policy.getActor());
        validateNotNull("policy.recipe", policy.getRecipe());
        // startedCallback is NOT required
        validateNotNull("pipelineController", getPipelineController());
        validatePositive("attempt", getAttempt());
    }

    private void validateNotNull(String fieldName, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is not set");
        }
    }

    private void validatePositive(String fieldName, int value) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " is negative or zero");
        }
    }
}
