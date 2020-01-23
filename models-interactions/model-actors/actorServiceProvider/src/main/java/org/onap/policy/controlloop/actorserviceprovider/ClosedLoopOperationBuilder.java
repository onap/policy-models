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

package org.onap.policy.controlloop.actorserviceprovider;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import lombok.Getter;
import lombok.NonNull;
import org.onap.policy.controlloop.policy.Policy;

/**
 * Builder for closed loop operations. The executor defaults to
 * {@link ForkJoinPool#commonPool()}, but may be overridden.
 */
@Getter
public class ClosedLoopOperationBuilder implements ClosedLoopOperationParams {
    private String actor;
    private String operation;

    private ClosedLoopEventContext context;
    private Executor executor;
    private Policy policy;
    private Runnable startCallback;
    private PipelineController pipelineController = new PipelineController();
    private String subRequestId;

    /**
     * Constructs a builder for a stand-alone operation.
     */
    public ClosedLoopOperationBuilder() {
        this.executor = ForkJoinPool.commonPool();
    }

    /**
     * Constructs a builder for an operation that is to be run as a sub task of another
     * operation.
     *
     * @param parent builder for the parent operation
     */
    public ClosedLoopOperationBuilder(ClosedLoopOperationBuilder parent) {
        this.executor = parent.getExecutor();
        this.pipelineController = parent.getPipelineController();
        this.subRequestId = parent.getSubRequestId();
    }

    /**
     * Builds and starts the specified operation.
     *
     * @return a future that will return the result of the operation
     */
    public CompletableFuture<OperationOutcome> build() {

        try {
            validateFields();

            // @formatter:off
            return ActorService.getInstance()
                        .getActor(getActor())
                        .getOperationManager(getOperation())
                        .startOperation(this);
            // @formatter:on

        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Builds the specified operation, appending it as the next stage of the given future.
     *
     * @param future future onto which to append this operation
     * @return a future that will return the result of the operation
     */
    public <T> CompletableFuture<OperationOutcome> build(CompletableFuture<T> future) {
        try {
            validateFields();

            // @formatter:off
            return ActorService.getInstance()
                        .getActor(getActor())
                        .getOperationManager(getOperation())
                        .appendOperation(this, future);
            // @formatter:on

        } catch (RuntimeException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private void validateFields() {
        validateNotNull("actor", actor);
        validateNotNull("operation", operation);
        validateNotNull("context", context);
        validateNotNull("executor", executor);
        validateNotNull("policy", policy);
        // startedCallback is NOT required
        validateNotNull("stopper", pipelineController);
        validateNotNull("subRequestId", subRequestId);
    }

    private void validateNotNull(String fieldName, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is not set");
        }
    }

    public ClosedLoopOperationBuilder setActor(@NonNull String actor) {
        this.actor = actor;
        return this;
    }

    public ClosedLoopOperationBuilder setOperation(@NonNull String operation) {
        this.operation = operation;
        return this;
    }

    public ClosedLoopOperationBuilder setContext(@NonNull ClosedLoopEventContext context) {
        this.context = context;
        return this;
    }

    public ClosedLoopOperationBuilder setExecutor(@NonNull Executor executor) {
        this.executor = executor;
        return this;
    }

    public ClosedLoopOperationBuilder setPipeLineController(@NonNull PipelineController pipelineController) {
        this.pipelineController = pipelineController;
        return this;
    }

    public ClosedLoopOperationBuilder setPolicy(@NonNull Policy policy) {
        this.policy = policy;
        return this;
    }

    public ClosedLoopOperationBuilder setStartCallback(@NonNull Runnable startCallback) {
        this.startCallback = startCallback;
        return this;
    }

    public ClosedLoopOperationBuilder setSubRequestId(@NonNull String subRequestId) {
        this.subRequestId = subRequestId;
        return this;
    }
}
