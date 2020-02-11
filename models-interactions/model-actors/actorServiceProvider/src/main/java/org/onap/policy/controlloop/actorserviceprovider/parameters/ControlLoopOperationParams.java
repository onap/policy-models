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

import java.util.Map;
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
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.policy.Target;
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

    /**
     * Actor name.
     */
    @NotNull
    private String actor;

    /**
     * Actor service in which to find the actor/operation.
     */
    @NotNull
    private ActorService actorService;

    /**
     * Event for which the operation applies.
     */
    @NotNull
    private ControlLoopEventContext context;

    /**
     * Executor to use to run the operation.
     */
    @NotNull
    @Builder.Default
    private Executor executor = ForkJoinPool.commonPool();

    /**
     * Operation name.
     */
    @NotNull
    private String operation;

    /**
     * Payload data for the request.
     */
    private Map<String, String> payload;

    /**
     * Number of retries allowed, or {@code null} if no retries.
     */
    private Integer retry;

    /**
     * The entity's target information. May be {@code null}, depending on the requirement
     * of the operation to be invoked.
     */
    private Target target;

    /**
     * Target entity.
     */
    @NotNull
    private String targetEntity;

    /**
     * Timeout, in seconds, or {@code null} if no timeout. Zero and negative values also
     * imply no timeout.
     */
    @Builder.Default
    private Integer timeoutSec = 300;

    /**
     * The function to invoke when the operation starts. This is optional.
     * <p/>
     * Note: this may be invoked multiple times, but with different actor/operations. That
     * may happen if the current operation requires other operations to be performed first
     * (e.g., A&AI queries, guard checks).
     */
    private Consumer<OperationOutcome> startCallback;

    /**
     * The function to invoke when the operation completes. This is optional.
     * <p/>
     * Note: this may be invoked multiple times, but with different actor/operations. That
     * may happen if the current operation requires other operations to be performed first
     * (e.g., A&AI queries, guard checks).
     */
    private Consumer<OperationOutcome> completeCallback;

    /**
     * Starts the specified operation.
     *
     * @return a future that will return the result of the operation
     * @throws IllegalArgumentException if the parameters are invalid
     */
    public CompletableFuture<OperationOutcome> start() {
        BeanValidationResult result = validate();
        if (!result.isValid()) {
            logger.warn("parameter error in operation {}.{} for {}:\n{}", getActor(), getOperation(), getRequestId(),
                            result.getResult());
            throw new IllegalArgumentException("invalid parameters");
        }

        // @formatter:off
        return actorService
                    .getActor(getActor())
                    .getOperator(getOperation())
                    .buildOperation(this)
                    .start();
        // @formatter:on
    }

    /**
     * Gets the requested ID of the associated event.
     *
     * @return the event's request ID, or {@code null} if no request ID is available
     */
    public UUID getRequestId() {
        return (context == null || context.getEvent() == null ? null : context.getEvent().getRequestId());
    }

    /**
     * Makes an operation outcome, populating it from the parameters.
     *
     * @return a new operation outcome
     */
    public OperationOutcome makeOutcome() {
        OperationOutcome outcome = new OperationOutcome();
        outcome.setActor(getActor());
        outcome.setOperation(getOperation());
        outcome.setTarget(targetEntity);

        return outcome;
    }

    /**
     * Invokes the callback to indicate that the operation has started. Any exceptions
     * generated by the callback are logged, but not re-thrown.
     *
     * @param operation the operation that is being started
     */
    public void callbackStarted(OperationOutcome operation) {
        logger.info("started operation {}.{} for {}", operation.getActor(), operation.getOperation(), getRequestId());

        if (startCallback != null) {
            Util.runFunction(() -> startCallback.accept(operation), "{}.{}: start-callback threw an exception for {}",
                            operation.getActor(), operation.getOperation(), getRequestId());
        }
    }

    /**
     * Invokes the callback to indicate that the operation has completed. Any exceptions
     * generated by the callback are logged, but not re-thrown.
     *
     * @param operation the operation that is being started
     */
    public void callbackCompleted(OperationOutcome operation) {
        logger.info("completed operation {}.{} outcome={} for {}", operation.getActor(), operation.getOperation(),
                        operation.getResult(), getRequestId());

        if (completeCallback != null) {
            Util.runFunction(() -> completeCallback.accept(operation),
                            "{}.{}: complete-callback threw an exception for {}", operation.getActor(),
                            operation.getOperation(), getRequestId());
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
