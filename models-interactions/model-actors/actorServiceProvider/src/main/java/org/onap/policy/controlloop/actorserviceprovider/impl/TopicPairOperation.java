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
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Triple;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.PropertyUtils.TriConsumer;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.TopicPairParams;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;
import org.onap.policy.controlloop.actorserviceprovider.topic.TopicPair;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation that uses a Topic pair.
 *
 * @param <S> response type
 */
@Getter
public abstract class TopicPairOperation<Q, S> extends OperationPartial {
    private static final Logger logger = LoggerFactory.getLogger(TopicPairOperation.class);
    private static final Coder coder = new StandardCoder();

    // fields extracted from the operator

    private final TopicPair topicPair;
    private final Forwarder forwarder;
    private final TopicPairParams pairParams;
    private final long timeoutMs;

    /**
     * Response class.
     */
    private final Class<S> responseClass;


    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param operator operator that created this operation
     * @param clazz response class
     */
    public TopicPairOperation(ControlLoopOperationParams params, TopicPairOperator operator, Class<S> clazz) {
        super(params, operator);
        this.topicPair = operator.getTopicPair();
        this.forwarder = operator.getForwarder();
        this.pairParams = operator.getParams();
        this.responseClass = clazz;
        this.timeoutMs = TimeUnit.MILLISECONDS.convert(pairParams.getTimeoutSec(), TimeUnit.SECONDS);
    }

    /**
     * If no timeout is specified, then it returns the default timeout.
     */
    @Override
    protected long getTimeoutMs(Integer timeoutSec) {
        return (timeoutSec == null || timeoutSec == 0 ? this.timeoutMs : super.getTimeoutMs(timeoutSec));
    }

    /**
     * Publishes the request and arranges to receive the response.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        final Q request = makeRequest(attempt);
        final List<String> expectedKeyValues = getExpectedKeyValues(attempt, request);

        final PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();
        final CompletableFuture<Triple<CommInfrastructure, String, StandardCoderObject>> future =
                        new CompletableFuture<>();
        final Executor executor = params.getExecutor();

        // register a listener BEFORE publishing

        // @formatter:off
        TriConsumer<CommInfrastructure, String, StandardCoderObject> listener =
            (infra, rawResponse, scoResponse) -> future.complete(Triple.of(infra, rawResponse, scoResponse));
        // @formatter:on

        // TODO this currently only allows a single matching response

        forwarder.register(expectedKeyValues, listener);

        // ensure listener is unregistered if the controller is canceled
        controller.add(() -> forwarder.unregister(expectedKeyValues, listener));

        // publish the request
        try {
            if (!publishRequest(request)) {
                controller.complete(setOutcome(outcome, PolicyResult.FAILURE));
                return controller;
            }
        } catch (RuntimeException e) {
            logger.warn("{}: failed to publish request for {}", getFullName(), params.getRequestId());
            forwarder.unregister(expectedKeyValues, listener);
            throw e;
        }


        // once "future" completes, process the response, and then complete the controller

        // @formatter:off
        future.thenApplyAsync(
            triple -> processResponse(triple.getLeft(), outcome, triple.getMiddle(), triple.getRight()),
                            executor)
                        .whenCompleteAsync(controller.delayedComplete(), executor);
        // @formatter:on

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
     * @return {@code true} if the request was published to at least one topic,
     *         {@code false} otherwise
     */
    protected boolean publishRequest(Q request) {
        String json;
        try {
            if (request instanceof String) {
                json = request.toString();
            } else {
                json = makeCoder().encode(request);
            }
        } catch (CoderException e) {
            throw new IllegalArgumentException("cannot encode request", e);
        }

        List<CommInfrastructure> list = topicPair.publish(json);
        if (list.isEmpty()) {
            return false;
        }

        logTopicRequest(list, request);
        return true;
    }

    /**
     * Processes a response. This method simply sets the outcome to SUCCESS.
     *
     * @param infra communication infrastructure on which the response was received
     * @param outcome outcome to be populate
     * @param response raw response to process
     * @param scoResponse response, as a {@link StandardCoderObject}
     * @return the outcome
     */
    protected OperationOutcome processResponse(CommInfrastructure infra, OperationOutcome outcome, String rawResponse,
                    StandardCoderObject scoResponse) {

        logger.info("{}.{}: response received for {}", params.getActor(), params.getOperation(), params.getRequestId());

        logTopicResponse(infra, rawResponse);

        S response;
        if (responseClass == String.class) {
            response = responseClass.cast(rawResponse);

        } else if (responseClass == StandardCoderObject.class) {
            response = responseClass.cast(scoResponse);

        } else {
            try {
                response = makeCoder().decode(rawResponse, responseClass);
            } catch (CoderException e) {
                logger.warn("{}.{} cannot decode response for {}", params.getActor(), params.getOperation(),
                                params.getRequestId(), e);
                return setOutcome(outcome, PolicyResult.FAILURE_EXCEPTION);
            }
        }

        if (!isSuccess(rawResponse, response)) {
            logger.info("{}.{} request failed  for {}", params.getActor(), params.getOperation(),
                            params.getRequestId());
            return setOutcome(outcome, PolicyResult.FAILURE);
        }

        logger.info("{}.{} request succeeded for {}", params.getActor(), params.getOperation(), params.getRequestId());
        setOutcome(outcome, PolicyResult.SUCCESS);
        postProcessResponse(outcome, rawResponse, response);

        return outcome;
    }

    /**
     * Processes a successful response.
     *
     * @param outcome outcome to be populate
     * @param rawResponse raw response
     * @param response decoded response
     */
    protected void postProcessResponse(OperationOutcome outcome, String rawResponse, S response) {
        // do nothing
    }

    /**
     * Determines if the response indicates success.
     *
     * @param rawResponse raw response
     * @param response decoded response
     * @return {@code true} if the response indicates success, {@code false} otherwise
     */
    protected abstract boolean isSuccess(String rawResponse, S response);

    /**
     * Logs a TOPIC request. If the request is not of type, String, then it attempts to
     * pretty-print it into JSON before logging.
     *
     * @param infrastructures list of communication infrastructures on which it was
     *        published
     * @param request request to be logged
     */
    protected void logTopicRequest(List<CommInfrastructure> infrastructures, Q request) {
        if (infrastructures.isEmpty()) {
            return;
        }

        String json;
        try {
            if (request == null) {
                json = null;
            } else if (request instanceof String) {
                json = request.toString();
            } else {
                json = makeCoder().encode(request, true);
            }

        } catch (CoderException e) {
            logger.warn("cannot pretty-print request", e);
            json = request.toString();
        }

        for (CommInfrastructure infra : infrastructures) {
            logger.info("[OUT|{}|{}|]{}{}", infra, pairParams.getTarget(), NetLoggerUtil.SYSTEM_LS, json);
        }
    }

    /**
     * Logs a TOPIC response. If the response is not of type, String, then it attempts to
     * pretty-print it into JSON before logging.
     *
     * @param infra communication infrastructure on which the response was received
     * @param response response to be logged
     */
    protected <T> void logTopicResponse(CommInfrastructure infra, T response) {
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
            logger.warn("cannot pretty-print response", e);
            json = response.toString();
        }

        logger.info("[IN|{}|{}|]{}{}", infra, pairParams.getSource(), NetLoggerUtil.SYSTEM_LS, json);
    }

    // these may be overridden by junit tests

    protected Coder makeCoder() {
        return coder;
    }
}
