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

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import lombok.Getter;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

/**
 * Operator that uses HTTP. The operator's parameters must be an {@link HttpParams}.
 */
@Getter
public abstract class HttpOperator extends OperatorPartial {

    private HttpClient client;

    /**
     * Default timeout, in milliseconds, if none specified in the request.
     */
    private long timeoutMs;

    /**
     * URI path for this particular operation. Includes a leading "/".
     */
    private String path;


    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     */
    public HttpOperator(String actorName, String name) {
        super(actorName, name);
    }

    /**
     * Makes an operator that will construct operations.
     *
     * @param <T> response type
     * @param actorName actor name
     * @param operation operation name
     * @param operationMaker function to make an operation
     * @return a new operator
     */
    public static <T> HttpOperator makeOperator(String actorName, String operation,
                    BiFunction<ControlLoopOperationParams, HttpOperator, HttpOperation<T>> operationMaker) {

        return new HttpOperator(actorName, operation) {
            @Override
            public Operation buildOperation(ControlLoopOperationParams params) {
                return operationMaker.apply(params, this);
            }
        };
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

        doConfigure(params);
    }

    /**
     * Configures the operator using the specified parameters.
     *
     * @param params operator parameters
     */
    protected void doConfigure(HttpParams params) {
        client = getClientFactory().get(params.getClientName());
        path = params.getPath();
        timeoutMs = TimeUnit.MILLISECONDS.convert(params.getTimeoutSec(), TimeUnit.SECONDS);
    }

    // these may be overridden by junit tests

    protected HttpClientFactory getClientFactory() {
        return HttpClientFactoryInstance.getClientFactory();
    }
}
