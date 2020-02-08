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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.Getter;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operator that uses HTTP. The operator's parameters must be an {@link HttpParams}.
 * Currently, this assumes the media type for any request and response is
 * "application/json". Subclasses will typically override many of the methods.
 *
 * @param <Q> request type; Void for operators that have no request (e.g., "get",
 *        "delete")
 * @param <S> response type
 */
@Getter
public abstract class HttpOperator<Q, S> extends OperatorPartial {
    private static final Logger logger = LoggerFactory.getLogger(HttpOperator.class);

    /*
     * NOTE: This class should actually be split into one class that takes no request type
     * and another that takes a request. Unfortunately, that results in a sonar issue,
     * because class hierarchy is too deep.
     */

    private HttpClient client;

    /**
     * Default timeout, in milliseconds, if none specified in the request.
     */
    @Getter(AccessLevel.NONE)
    private long timeoutMs;

    /**
     * URI path for this particular operation.
     */
    private String path;

    /**
     * Response class.
     */
    private final Class<S> responseClass;


    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     * @param responseClass response class
     */
    public HttpOperator(String actorName, String name, Class<S> responseClass) {
        super(actorName, name);
        this.responseClass = responseClass;
    }

    /**
     * Translates the parameters to an {@link HttpParams} and then extracts the relevant
     * values.
     */
    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        HttpParams params = Util.translate(getFullName(), parameters, HttpParams.class);
        ValidationResult result = params.validate(getFullName());
        if (!result.isValid()) {
            throw new ParameterValidationRuntimeException("invalid parameters", result);
        }

        client = getClientFactory().get(params.getClientName());
        path = params.getPath();
        timeoutMs = TimeUnit.MILLISECONDS.convert(params.getTimeoutSec(), TimeUnit.SECONDS);
    }

    @Override
    protected long getTimeOutMs(Integer timeoutSec) {
        return (timeoutSec == null || timeoutSec == 0 ? this.timeoutMs : super.getTimeOutMs(timeoutSec));
    }

    /**
     * Subclasses should override this, typically just having it invoke
     * {@link #startQueryAsync(ControlLoopOperationParams, OperationOutcome)} or
     * {@link #startRequestAsync(ControlLoopOperationParams, int, OperationOutcome)}.
     */
    @Override
    protected abstract CompletableFuture<OperationOutcome> startOperationAsync(ControlLoopOperationParams params,
                    int attempt, OperationOutcome outcome);

    /**
     * Starts the query and creates a handler to handle the response. Subclasses that use
     * this should override {@link #startQueryAsync(InvocationCallback, String, Map)}.
     *
     *
     * @param params operation parameters
     * @param outcome where to be the result
     * @return a future to cancel the query or await the outcome
     */
    protected CompletableFuture<OperationOutcome> startQueryAsync(ControlLoopOperationParams params,
                    OperationOutcome outcome) {

        Map<String, Object> headers = makeHeaders(params);

        headers.put("Accept", MediaType.APPLICATION_JSON);
        String url = makeUrl(params);

        logRestRequest(url, null);

        // @formatter:off
        return handleResponse(params, outcome, url, responseClass,
            callback -> startQueryAsync(callback, makePath(params), headers));
        // @formatter:on
    }

    /**
     * Starts the query (e.g. "get" or "delete"). If query is not supported, then this
     * will typically throw UnsupportedOperationException.
     *
     * @param callback callback to be invoked when the response is received
     * @param path target URL
     * @param headers request headers
     * @return a future to cancel the query or await the response
     */
    protected abstract Future<Response> startQueryAsync(InvocationCallback<Response> callback, String path,
                    Map<String, Object> headers);

    /**
     * Starts the request and creates a handler to handle the response. Subclasses that
     * use this should override
     * {@link #startRequestAsync(InvocationCallback, String, Entity, Map)} and
     * {@link #makeRequest(ControlLoopOperationParams, int)}.
     *
     * @param params operation parameters
     * @param attempt attempt number
     * @param outcome where to be the result
     * @return a future to cancel the query or await the outcome
     */
    protected CompletableFuture<OperationOutcome> startRequestAsync(ControlLoopOperationParams params, int attempt,
                    OperationOutcome outcome) {

        Q request = makeRequest(params, attempt);

        Entity<Q> entity = Entity.entity(request, MediaType.APPLICATION_JSON);

        Map<String, Object> headers = makeHeaders(params);

        headers.put("Accept", MediaType.APPLICATION_JSON);
        String url = makeUrl(params);

        logRestRequest(url, request);

        // @formatter:off
        return handleResponse(params, outcome, url, responseClass,
            callback -> startRequestAsync(callback, makePath(params), entity, headers));
        // @formatter:on
    }

    /**
     * Starts the request (e.g., "post" or "put"). If requests are not supported, then
     * this will typically throw UnsupportedOperationException.
     *
     * @param callback callback to be invoked when the response is received
     * @param path target URL
     * @param headers request headers
     * @return a future to cancel the query or await the response
     */
    protected abstract Future<Response> startRequestAsync(InvocationCallback<Response> callback, String path,
                    Entity<Q> entity, Map<String, Object> headers);

    /**
     * Makes the request (e.g., "post" or "put"). If requests are not supported, then this
     * will typically throw UnsupportedOperationException.
     *
     * @param params operation parameters
     * @param attempt current attempt, starting with "1"
     * @return a new request to be posted
     */
    protected abstract Q makeRequest(ControlLoopOperationParams params, int attempt);

    /**
     * Makes the request headers. This simply returns an empty map.
     *
     * @param params operation parameters
     * @return request headers, a non-null, modifiable map
     */
    protected Map<String, Object> makeHeaders(ControlLoopOperationParams params) {
        return new HashMap<>();
    }

    /**
     * Gets the path to be used when perform the request; this is typically appended to
     * the base URL. This method simply invokes {@link #getPath()}.
     *
     * @param params operation parameters
     * @return the path URI suffix
     */
    public String makePath(ControlLoopOperationParams params) {
        return getPath();
    }

    /**
     * Makes the URL to which the "get" request should be posted. This method returns the
     * base URL appended with the return value from
     * {@link #makePath(ControlLoopOperationParams)}.
     *
     * @param params operation parameters
     * @return the URL to which from which to get
     */
    public String makeUrl(ControlLoopOperationParams params) {
        return (getClient().getBaseUrl() + "/" + makePath(params));
    }

    /**
     * Arranges to handle a response.
     *
     * @param params operation parameters
     * @param outcome outcome to be populate
     * @param url URL to which to request was sent
     * @param clazz desired response class
     * @param requester function to initiate the request and invoke the given callback
     *        when it completes
     * @return a future for the response
     */
    protected CompletableFuture<OperationOutcome> handleResponse(ControlLoopOperationParams params,
                    OperationOutcome outcome, String url, Class<S> clazz,
                    Function<InvocationCallback<Response>, Future<Response>> requester) {

        final PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();
        final CompletableFuture<Response> future = new CompletableFuture<>();
        final Executor executor = params.getExecutor();

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
        future.thenApplyAsync(response -> processResponse(params, outcome, url, clazz, response), executor)
                        .whenCompleteAsync(controller.delayedComplete(), executor);

        return controller;
    }

    /**
     * Processes a response. This method simply sets the outcome to SUCCESS.
     *
     * @param <T> response type
     * @param params operation parameters
     * @param outcome outcome to be populate
     * @param url URL to which to request was sent
     * @param clazz desired response class
     * @param response raw response to process
     * @return the outcome
     */
    protected OperationOutcome processResponse(ControlLoopOperationParams params, OperationOutcome outcome, String url,
                    Class<S> clazz, Response rawResponse) {

        logger.info("{}.{}: response received for {}", params.getActor(), params.getOperation(), params.getRequestId());

        String strResponse = HttpClient.getBody(rawResponse, String.class);

        logRestResponse(url, strResponse);

        S response;
        if (clazz == String.class) {
            response = clazz.cast(strResponse);

        } else {
            try {
                response = makeCoder().decode(strResponse, clazz);
            } catch (CoderException e) {
                logger.warn("{}.{} cannot decode response with http error code {} for {}", params.getActor(),
                                params.getOperation(), rawResponse.getStatus(), params.getRequestId(), e);
                return setOutcome(params, outcome, PolicyResult.FAILURE_EXCEPTION);
            }
        }

        if (!isSuccess(rawResponse, response)) {
            logger.info("{}.{} request failed with http error code {} for {}", params.getActor(), params.getOperation(),
                            rawResponse.getStatus(), params.getRequestId());
            return setOutcome(params, outcome, PolicyResult.FAILURE);
        }

        logger.info("{}.{} request succeeded for {}", params.getActor(), params.getOperation(), params.getRequestId());
        setOutcome(params, outcome, PolicyResult.SUCCESS);
        postProcessResponse(params, outcome, url, rawResponse, response);

        return outcome;
    }

    /**
     * Processes a successful response.
     *
     * @param params operation parameters
     * @param outcome outcome to be populate
     * @param url URL to which to request was sent
     * @param rawResponse raw response
     * @param response decoded response
     */
    protected void postProcessResponse(ControlLoopOperationParams params, OperationOutcome outcome, String url,
                    Response rawResponse, S response) {
        // do nothing
    }

    /**
     * Determines if the response indicates success. This method simply checks the HTTP
     * status code.
     *
     * @param rawResponse raw response
     * @param response decoded response
     * @return {@code true} if the response indicates success, {@code false} otherwise
     */
    protected boolean isSuccess(Response rawResponse, S response) {
        return (rawResponse.getStatus() == 200);
    }

    /**
     * Logs a REST request. If the request is not of type, String, then it attempts to
     * pretty-print it into JSON before logging.
     *
     * @param url request URL
     * @param request request to be logged
     */
    public <T> void logRestRequest(String url, T request) {
        String json;
        try {
            if (request instanceof String) {
                json = request.toString();
            } else {
                json = makeCoder().encode(request, true);
            }

        } catch (CoderException e) {
            logger.warn("cannot pretty-print request", e);
            json = request.toString();
        }

        NetLoggerUtil.log(EventType.OUT, CommInfrastructure.REST, url, json);
        logger.info("[OUT|{}|{}|]{}{}", CommInfrastructure.REST, url, NetLoggerUtil.SYSTEM_LS, json);
    }

    /**
     * Logs a REST response. If the response is not of type, String, then it attempts to
     * pretty-print it into JSON before logging.
     *
     * @param url request URL
     * @param response response to be logged
     */
    public <T> void logRestResponse(String url, T response) {
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

        NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, url, json);
        logger.info("[IN|{}|{}|]{}{}", CommInfrastructure.REST, url, NetLoggerUtil.SYSTEM_LS, json);
    }

    /**
     * Function to perform a query.
     */
    public static interface QueryFunction {
        public CompletableFuture<Response> query(HttpClient client, InvocationCallback<Response> callback, String path,
                        Map<String, Object> headers);
    }

    /**
     * Function to perform a request.
     *
     * @param <Q> Request type
     */
    public static interface RequestFunction<Q> {
        public CompletableFuture<Response> request(HttpClient client, InvocationCallback<Response> callback,
                        String path, Entity<Q> entity, Map<String, Object> headers);

    }

    // these may be overridden by junit tests

    protected Coder makeCoder() {
        return new StandardCoder();
    }

    public HttpClientFactory getClientFactory() {
        return HttpClientFactoryInstance.getClientFactory();
    }
}
