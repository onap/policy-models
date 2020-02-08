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
import java.util.function.Function;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import lombok.Getter;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.onap.policy.controlloop.policy.PolicyResult;
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
     * Operator that created this operation.
     */
    protected final HttpOperator operator;

    /**
     * Response class.
     */
    private final Class<T> responseClass;


    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param operator operator that created this operation
     */
    public HttpOperation(ControlLoopOperationParams params, HttpOperator operator, Class<T> clazz) {
        super(params, operator);
        this.operator = operator;
        this.responseClass = clazz;
    }

    @Override
    protected long getTimeOutMs(Integer timeoutSec) {
        return (timeoutSec == null || timeoutSec == 0 ? operator.getTimeoutMs() : super.getTimeOutMs(timeoutSec));
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
     * Gets the path to be used when performing the request; this is typically appended to
     * the base URL. This method simply invokes {@link #getPath()}.
     *
     * @return the path URI suffix
     */
    public String makePath() {
        return operator.getPath();
    }

    /**
     * Makes the URL to which the "get" request should be posted. This ir primarily used
     * for logging purposes. This particular method returns the base URL appended with the
     * return value from {@link #makePath()}.
     *
     * @return the URL to which from which to get
     */
    public String makeUrl() {
        return (operator.getClient().getBaseUrl() + makePath());
    }

    /**
     * Arranges to handle a response.
     *
     * @param outcome outcome to be populate
     * @param url URL to which to request was sent
     * @param clazz desired response class
     * @param requester function to initiate the request and invoke the given callback
     *        when it completes
     * @return a future for the response
     */
    protected CompletableFuture<OperationOutcome> handleResponse(OperationOutcome outcome, String url,
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
        future.thenApplyAsync(response -> processResponse(outcome, url, response), executor)
                        .whenCompleteAsync(controller.delayedComplete(), executor);

        return controller;
    }

    /**
     * Processes a response. This method simply sets the outcome to SUCCESS.
     *
     * @param outcome outcome to be populate
     * @param url URL to which to request was sent
     * @param clazz desired response class
     * @param response raw response to process
     * @return the outcome
     */
    protected OperationOutcome processResponse(OperationOutcome outcome, String url,
                    Response rawResponse) {

        logger.info("{}.{}: response received for {}", params.getActor(), params.getOperation(), params.getRequestId());

        String strResponse = HttpClient.getBody(rawResponse, String.class);

        logRestResponse(url, strResponse);

        T response;
        if (responseClass == String.class) {
            response = responseClass.cast(strResponse);

        } else {
            try {
                response = makeCoder().decode(strResponse, responseClass);
            } catch (CoderException e) {
                logger.warn("{}.{} cannot decode response with http error code {} for {}", params.getActor(),
                                params.getOperation(), rawResponse.getStatus(), params.getRequestId(), e);
                return setOutcome(outcome, PolicyResult.FAILURE_EXCEPTION);
            }
        }

        if (!isSuccess(rawResponse, response)) {
            logger.info("{}.{} request failed with http error code {} for {}", params.getActor(), params.getOperation(),
                            rawResponse.getStatus(), params.getRequestId());
            return setOutcome(outcome, PolicyResult.FAILURE);
        }

        logger.info("{}.{} request succeeded for {}", params.getActor(), params.getOperation(), params.getRequestId());
        setOutcome(outcome, PolicyResult.SUCCESS);
        postProcessResponse(outcome, url, rawResponse, response);

        return outcome;
    }

    /**
     * Processes a successful response.
     *
     * @param outcome outcome to be populate
     * @param url URL to which to request was sent
     * @param rawResponse raw response
     * @param response decoded response
     */
    protected void postProcessResponse(OperationOutcome outcome, String url, Response rawResponse, T response) {
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
    protected boolean isSuccess(Response rawResponse, T response) {
        return (rawResponse.getStatus() == 200);
    }

    /**
     * Logs a REST request. If the request is not of type, String, then it attempts to
     * pretty-print it into JSON before logging.
     *
     * @param url request URL
     * @param request request to be logged
     */
    public <Q> void logRestRequest(String url, Q request) {
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
    public <S> void logRestResponse(String url, S response) {
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
}
