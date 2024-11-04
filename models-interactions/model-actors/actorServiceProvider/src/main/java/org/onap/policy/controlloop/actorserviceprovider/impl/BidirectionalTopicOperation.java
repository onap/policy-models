/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation.
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
import java.util.function.BiConsumer;
import lombok.Getter;
import org.onap.policy.common.message.bus.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation that uses a bidirectional topic.
 *
 * @param <S> response type
 */
@Getter
public abstract class BidirectionalTopicOperation<Q, S> extends OperationPartial {
    private static final Logger logger = LoggerFactory.getLogger(BidirectionalTopicOperation.class);

    /**
     * Response status.
     */
    public enum Status {
        SUCCESS, FAILURE, STILL_WAITING
    }

    /**
     * Configuration for this operation.
     */
    private final BidirectionalTopicConfig config;

    /**
     * Response class.
     */
    private final Class<S> responseClass;

    // fields extracted from "config"

    private final BidirectionalTopicHandler topicHandler;
    private final Forwarder forwarder;


    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     * @param clazz response class
     * @param propertyNames names of properties required by this operation
     */
    protected BidirectionalTopicOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config,
                    Class<S> clazz, List<String> propertyNames) {
        super(params, config, propertyNames);
        this.config = config;
        this.responseClass = clazz;
        this.forwarder = config.getForwarder();
        this.topicHandler = config.getTopicHandler();
    }

    public long getTimeoutMs() {
        return config.getTimeoutMs();
    }

    /**
     * If no timeout is specified, then it returns the default timeout.
     */
    @Override
    protected long getTimeoutMs(Integer timeoutSec) {
        return (timeoutSec == null || timeoutSec == 0 ? getTimeoutMs() : super.getTimeoutMs(timeoutSec));
    }

    /**
     * Publishes the request and arranges to receive the response.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        final var request = makeRequest(attempt);
        final List<String> expectedKeyValues = getExpectedKeyValues(attempt, request);

        final PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();
        final var executor = params.getExecutor();

        // register a listener BEFORE publishing

        BiConsumer<String, StandardCoderObject> listener = (rawResponse, scoResponse) -> {
            try {
                OperationOutcome latestOutcome = processResponse(outcome, rawResponse, scoResponse);
                if (latestOutcome != null) {
                    // final response - complete the controller
                    controller.completeAsync(() -> latestOutcome, executor);
                }
            } catch (RuntimeException e) {
                logger.warn("{}: failed to process response for {}", getFullName(), params.getRequestId());
                controller.completeExceptionally(e);
            }
        };

        forwarder.register(expectedKeyValues, listener);

        // ensure listener is unregistered if the controller is canceled
        controller.add(() -> forwarder.unregister(expectedKeyValues, listener));

        // publish the request
        try {
            publishRequest(request);
        } catch (RuntimeException e) {
            logger.warn("{}: failed to publish request for {}", getFullName(), params.getRequestId());
            forwarder.unregister(expectedKeyValues, listener);
            throw e;
        }

        return controller;
    }

    /**
     * Makes the request.
     *
     * @param attempt operation attempt
     * @return a new request
     */
    protected abstract Q makeRequest(int attempt);

    /**
     * Gets values, expected in the response, that should match the selector keys.
     *
     * @param attempt operation attempt
     * @param request request to be published
     * @return a list of the values to be matched by the selector keys
     */
    protected abstract List<String> getExpectedKeyValues(int attempt, Q request);

    /**
     * Publishes the request. Encodes the request, if it is not already a String.
     *
     * @param request request to be published
     */
    protected void publishRequest(Q request) {
        String json = prettyPrint(request);
        logMessage(EventType.OUT, topicHandler.getSinkTopicCommInfrastructure(), topicHandler.getSinkTopic(), json);

        if (!topicHandler.send(json)) {
            throw new IllegalStateException("nothing published");
        }
    }

    /**
     * Processes a response.
     *
     * @param outcome outcome to be populated
     * @param rawResponse raw response to process
     * @param scoResponse response, as a {@link StandardCoderObject}
     * @return the outcome, or {@code null} if still waiting for completion
     */
    protected OperationOutcome processResponse(OperationOutcome outcome, String rawResponse,
                    StandardCoderObject scoResponse) {

        logger.info("{}.{}: response received for {}", params.getActor(), params.getOperation(), params.getRequestId());

        logMessage(EventType.IN, topicHandler.getSourceTopicCommInfrastructure(), topicHandler.getSourceTopic(),
                        rawResponse);

        // decode the response
        S response;
        if (responseClass == String.class) {
            response = responseClass.cast(rawResponse);

        } else if (responseClass == StandardCoderObject.class) {
            response = responseClass.cast(scoResponse);

        } else {
            try {
                response = getCoder().decode(rawResponse, responseClass);
            } catch (CoderException e) {
                logger.warn("{}.{} cannot decode response for {}", params.getActor(), params.getOperation(),
                                params.getRequestId());
                throw new IllegalArgumentException("cannot decode response", e);
            }
        }

        // check its status
        switch (detmStatus(rawResponse, response)) {
            case SUCCESS:
                logger.info("{}.{} request succeeded for {}", params.getActor(), params.getOperation(),
                                params.getRequestId());
                setOutcome(outcome, OperationResult.SUCCESS, response);
                postProcessResponse(outcome, rawResponse, response);
                return outcome;

            case FAILURE:
                logger.info("{}.{} request failed for {}", params.getActor(), params.getOperation(),
                                params.getRequestId());
                return setOutcome(outcome, OperationResult.FAILURE, response);

            case STILL_WAITING:
            default:
                logger.info("{}.{} request incomplete for {}", params.getActor(), params.getOperation(),
                                params.getRequestId());
                return null;
        }
    }

    /**
     * Sets an operation's outcome and default message based on the result.
     *
     * @param outcome operation to be updated
     * @param result result of the operation
     * @param response response used to populate the outcome
     * @return the updated operation
     */
    public OperationOutcome setOutcome(OperationOutcome outcome, OperationResult result, S response) {
        outcome.setResponse(response);
        return setOutcome(outcome, result);
    }

    /**
     * Processes a successful response.
     *
     * @param outcome outcome to be populated
     * @param rawResponse raw response
     * @param response decoded response
     */
    protected void postProcessResponse(OperationOutcome outcome, String rawResponse, S response) {
        // do nothing
    }

    /**
     * Determines the status of the response.
     *
     * @param rawResponse raw response
     * @param response decoded response
     * @return the status of the response
     */
    protected abstract Status detmStatus(String rawResponse, S response);
}
