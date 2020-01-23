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
import lombok.NoArgsConstructor;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;

/**
 * Builder for closed loop operations. The executor defaults to
 * {@link ForkJoinPool#commonPool()}, but may be overridden.
 */
@Getter
@NoArgsConstructor
public class ClosedLoopOperationBuilder implements ClosedLoopOperationParams {
    private String actor;
    private String operation;
    private ClosedLoopEventContext context;
    private Policy policy;
    private String subRequestId;

    private Executor executor = ForkJoinPool.commonPool();

    /**
     * Builds and starts the specified operation.
     *
     * @return a future that can be used to cancel, or await the result of, the operation
     */
    public CompletableFuture<PolicyResult> build() {
        return build(CompletableFuture.completedFuture(null));
    }

    /**
     * Builds the specified operation, appending it as the next stage of the given future.
     *
     * @param future future onto which to append this operation
     * @return a future that can be used to cancel, or await the result of, the operation
     */
    public <T> CompletableFuture<PolicyResult> build(CompletableFuture<T> future) {
        try {
            validateNotNull("actor", actor);
            validateNotNull("operation", operation);
            validateNotNull("context", context);
            validateNotNull("policy", policy);
            validateNotNull("subRequestId", subRequestId);
            validateNotNull("executor", executor);

            // @formatter:off
            return ActorService.getInstance()
                        .getActor(getActor())
                        .getOperationManager(getOperation())
                        .start(future, this);
            // @formatter:on

        } catch (RuntimeException e) {
            future.cancel(false);
            return CompletableFuture.failedFuture(e);
        }
    }

    private void validateNotNull(String fieldName, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is not set");
        }
    }

    public ClosedLoopOperationBuilder setActor(String actor) {
        this.actor = actor;
        return this;
    }

    public ClosedLoopOperationBuilder setOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public ClosedLoopOperationBuilder setContext(ClosedLoopEventContext context) {
        this.context = context;
        return this;
    }

    public ClosedLoopOperationBuilder setPolicy(Policy policy) {
        this.policy = policy;
        return this;
    }

    public ClosedLoopOperationBuilder setSubRequestId(String subRequestId) {
        this.subRequestId = subRequestId;
        return this;
    }

    public ClosedLoopOperationBuilder setExecutor(Executor executor) {
        this.executor = executor;
        return this;
    }
}
