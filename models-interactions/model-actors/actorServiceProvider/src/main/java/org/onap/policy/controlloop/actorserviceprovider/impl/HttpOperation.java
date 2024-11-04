/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

import jakarta.ws.rs.client.InvocationCallback;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import lombok.Getter;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.message.bus.event.Topic.CommInfrastructure;
import org.onap.policy.common.message.bus.utils.NetLoggerUtil;
import org.onap.policy.common.message.bus.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operator that uses HTTP. The operator's parameters must be an {@link HttpParams}.
 *
 * @param <T> response type
 */
@Getter
public abstract class HttpOperation<T> extends OperationPartial {
    private static final Logger logger = LoggerFactory.getLogger(HttpOperation.class);

    /**
     * Response status.
     */
    public enum Status {
        SUCCESS, FAILURE, STILL_WAITING
    }

    /**
     * Configuration for this operation.
     */
    private final HttpConfig config;

    /**
     * Response class.
     */
    private final Class<T> responseClass;

    /**
     * {@code True} to use polling, {@code false} otherwise.
     */
    @Getter
    private boolean usePolling;

    /**
     * Number of polls issued so far, on the current operation attempt.
     */
    @Getter
    private int pollCount;


    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     * @param clazz response class
     * @param propertyNames names of properties required by this operation
     */
    protected HttpOperation(ControlLoopOperationParams params, HttpConfig config, Class<T> clazz,
                    List<String> propertyNames) {
        super(params, config, propertyNames);
        this.config = config;
        this.responseClass = clazz;
    }

    /**
     * Indicates that polling should be used.
     */
    protected void setUsePolling() {
        if (!(config instanceof HttpPollingConfig)) {
            throw new IllegalStateException("cannot poll without polling parameters");
        }

        usePolling = true;
    }

    public HttpClient getClient() {
        return config.getClient();
    }

    /**
     * Gets the path to be used when performing the request; this is typically appended to
     * the base URL. This method simply invokes {@link #getPath()}.
     *
     * @return the path URI suffix
     */
    public String getPath() {
        return config.getPath();
    }

    public long getTimeoutMs() {
        return config.getTimeoutMs();
    }

    /**
     * If no timeout is specified, then it returns the operator's configured timeout.
     */
    @Override
    protected long getTimeoutMs(Integer timeoutSec) {
        return (timeoutSec == null || timeoutSec == 0 ? getTimeoutMs() : super.getTimeoutMs(timeoutSec));
    }

    /**
     * Makes the request headers. This simply returns an empty map.
     *
     * @return request headers, a non-null, modifiable map
     */
    protected Map<String, Object> makeHeaders() {
        return new HashMap<>();
    }

    /**
     * Makes the URL to which the HTTP request should be posted. This is primarily used
     * for logging purposes. This particular method returns the base URL appended with the
     * return value from {@link #getPath()}.
     *
     * @return the URL to which from which to get
     */
    public String getUrl() {
        return (getClient().getBaseUrl() + getPath());
    }

    /**
     * Resets the polling count
     *
     * <p/>
     * Note: This should be invoked at the start of each operation (i.e., in
     * {@link #startOperationAsync(int, OperationOutcome)}.
     */
    protected void resetPollCount() {
        pollCount = 0;
    }

    /**
     * Arranges to handle a response.
     *
     * @param outcome outcome to be populate
     * @param url URL to which to request was sent
     * @param requester function to initiate the request and invoke the given callback
     *        when it completes
     * @return a future for the response
     */
    protected CompletableFuture<OperationOutcome> handleResponse(OperationOutcome outcome, String url,
                    Function<InvocationCallback<Response>, Future<Response>> requester) {

        final PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();
        final CompletableFuture<Response> future = new CompletableFuture<>();
        final var executor = params.getExecutor();

        // arrange for the callback to complete "future"
        InvocationCallback<Response> callback = new InvocationCallback<>() {
            @Override
            public void completed(Response response) {
                future.complete(response);
            }

            @Override
            public void failed(Throwable throwable) {
                logger.warn("{}.{}: response failure for {}", params.getActor(), params.getOperation(),
                                params.getRequestId());
                future.completeExceptionally(throwable);
            }
        };

        // start the request and arrange to cancel it if the controller is canceled
        controller.add(requester.apply(callback));

        // once "future" completes, process the response, and then complete the controller
        future.thenComposeAsync(response -> processResponse(outcome, url, response), executor)
                        .whenCompleteAsync(controller.delayedComplete(), executor);

        return controller;
    }

    /**
     * Processes a response. This method decodes the response, sets the outcome based on
     * the response, and then returns a completed future.
     *
     * @param outcome outcome to be populate
     * @param url URL to which to request was sent
     * @param rawResponse raw response to process
     * @return a future to cancel or await the outcome
     */
    protected CompletableFuture<OperationOutcome> processResponse(OperationOutcome outcome, String url,
                    Response rawResponse) {

        logger.info("{}.{}: response received for {}", params.getActor(), params.getOperation(), params.getRequestId());

        String strResponse = rawResponse.readEntity(String.class);

        logMessage(EventType.IN, CommInfrastructure.REST, url, strResponse);

        T response;
        if (responseClass == String.class) {
            response = responseClass.cast(strResponse);
        } else {
            try {
                response = getCoder().decode(strResponse, responseClass);
            } catch (CoderException e) {
                logger.warn("{}.{} cannot decode response for {}", params.getActor(), params.getOperation(),
                                params.getRequestId(), e);
                throw new IllegalArgumentException("cannot decode response");
            }
        }

        if (!isSuccess(rawResponse, response)) {
            logger.info("{}.{} request failed with http error code {} for {}", params.getActor(), params.getOperation(),
                            rawResponse.getStatus(), params.getRequestId());
            return CompletableFuture.completedFuture(
                    setOutcome(outcome, OperationResult.FAILURE, rawResponse, response));
        }

        logger.info("{}.{} request succeeded for {}", params.getActor(), params.getOperation(), params.getRequestId());
        setOutcome(outcome, OperationResult.SUCCESS, rawResponse, response);
        return postProcessResponse(outcome, url, rawResponse, response);
    }

    /**
     * Sets an operation's outcome and default message based on the result.
     *
     * @param outcome operation to be updated
     * @param result result of the operation
     * @param rawResponse raw response
     * @param response decoded response
     * @return the updated operation
     */
    public OperationOutcome setOutcome(OperationOutcome outcome, OperationResult result, Response rawResponse,
                    T response) {

        outcome.setResponse(response);
        return setOutcome(outcome, result);
    }

    /**
     * Processes a successful response. This method simply returns the outcome wrapped in
     * a completed future.
     *
     * @param outcome outcome to be populate
     * @param url URL to which to request was sent
     * @param rawResponse raw response
     * @param response decoded response
     * @return a future to cancel or await the outcome
     */
    protected CompletableFuture<OperationOutcome> postProcessResponse(OperationOutcome outcome, String url,
                    Response rawResponse, T response) {

        if (!usePolling) {
            // doesn't use polling - just return the completed future
            return CompletableFuture.completedFuture(outcome);
        }

        HttpPollingConfig cfg = (HttpPollingConfig) config;

        switch (detmStatus(rawResponse, response)) {
            case SUCCESS -> {
                logger.info("{}.{} request succeeded for {}", params.getActor(), params.getOperation(),
                    params.getRequestId());
                return CompletableFuture
                    .completedFuture(setOutcome(outcome, OperationResult.SUCCESS, rawResponse, response));
            }
            case FAILURE -> {
                logger.info("{}.{} request failed for {}", params.getActor(), params.getOperation(),
                    params.getRequestId());
                return CompletableFuture
                    .completedFuture(setOutcome(outcome, OperationResult.FAILURE, rawResponse, response));
            }
            default -> logger.info("{}.{} request incomplete for {}", params.getActor(), params.getOperation(),
                params.getRequestId());
        }

        // still incomplete

        // see if the limit for the number of polls has been reached
        if (pollCount++ >= cfg.getMaxPolls()) {
            logger.warn("{}: exceeded 'poll' limit {} for {}", getFullName(), cfg.getMaxPolls(),
                            params.getRequestId());
            setOutcome(outcome, OperationResult.FAILURE_TIMEOUT);
            return CompletableFuture.completedFuture(outcome);
        }

        // sleep and then poll
        Function<Void, CompletableFuture<OperationOutcome>> doPoll = unused -> issuePoll(outcome);
        return sleep(getPollWaitMs(), TimeUnit.MILLISECONDS).thenComposeAsync(doPoll);
    }

    /**
     * Polls to see if the original request is complete. This method polls using an HTTP
     * "get" request whose URL is constructed by appending the extracted "poll ID" to the
     * poll path from the configuration data.
     *
     * @param outcome outcome to be populated with the response
     * @return a future that can be used to cancel the poll or await its response
     */
    protected CompletableFuture<OperationOutcome> issuePoll(OperationOutcome outcome) {
        String path = getPollingPath();
        String url = getClient().getBaseUrl() + path;

        logger.debug("{}: 'poll' count {} for {}", getFullName(), pollCount, params.getRequestId());

        logMessage(EventType.OUT, CommInfrastructure.REST, url, null);

        return handleResponse(outcome, url, callback -> getClient().get(callback, path, null));
    }

    /**
     * Determines the status of the response. This particular method simply throws an
     * exception.
     *
     * @param rawResponse raw response
     * @param response decoded response
     * @return the status of the response
     */
    protected Status detmStatus(Response rawResponse, T response) {
        throw new UnsupportedOperationException("cannot determine response status");
    }

    /**
     * Gets the URL to use when polling. Typically, this is some unique ID appended to the
     * polling path found within the configuration data. This particular method simply
     * returns the polling path from the configuration data.
     *
     * @return the URL to use when polling
     */
    protected String getPollingPath() {
        return ((HttpPollingConfig) config).getPollPath();
    }

    /**
     * Determines if the response indicates success. This method simply checks the HTTP
     * status code.
     *
     * @param rawResponse raw response
     * @param response decoded response
     * @return {@code true} if the response indicates success, {@code false} otherwise
     */
    protected boolean isSuccess(Response rawResponse, T response) {
        return (rawResponse.getStatus() == 200);
    }

    @Override
    public <Q> String logMessage(EventType direction, CommInfrastructure infra, String sink, Q request) {
        String json = super.logMessage(direction, infra, sink, request);
        NetLoggerUtil.log(direction, infra, sink, json);
        return json;
    }

    // these may be overridden by junit tests

    protected long getPollWaitMs() {
        HttpPollingConfig cfg = (HttpPollingConfig) config;

        return TimeUnit.MILLISECONDS.convert(cfg.getPollWaitSec(), TimeUnit.SECONDS);
    }
}
