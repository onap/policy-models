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
import lombok.Getter;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

/**
 * Operator that uses HTTP. The operator's parameters must be an {@link HttpParams}.
 */
public class HttpOperator extends OperatorPartial {

    /**
     * Function to make an operation.
     */
    @SuppressWarnings("rawtypes")
    private final OperationMaker<HttpConfig, HttpOperation> operationMaker;

    /**
     * Current configuration. This is set by {@link #doConfigure(Map)}.
     */
    @Getter
    private HttpConfig currentConfig;


    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     */
    protected HttpOperator(String actorName, String name) {
        this(actorName, name, null);
    }

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     * @param operationMaker function to make an operation
     */
    public HttpOperator(String actorName, String name,
                    @SuppressWarnings("rawtypes") OperationMaker<HttpConfig, HttpOperation> operationMaker) {
        super(actorName, name);
        this.operationMaker = operationMaker;
    }

    /**
     * Translates the parameters to an {@link HttpParams} and then extracts the relevant
     * values.
     */
    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        currentConfig = makeConfiguration(parameters);
    }

    /**
     * Makes a new configuration using the specified parameters.
     *
     * @param parameters operator parameters
     * @return a new configuration
     */
    protected HttpConfig makeConfiguration(Map<String, Object> parameters) {
        HttpParams params = Util.translate(getFullName(), parameters, HttpParams.class);
        ValidationResult result = params.validate(getFullName());
        if (!result.isValid()) {
            throw new ParameterValidationRuntimeException("invalid parameters", result);
        }

        return new HttpConfig(getBlockingExecutor(), params, getClientFactory());
    }

    @Override
    public Operation buildOperation(ControlLoopOperationParams params) {
        if (operationMaker == null) {
            throw new UnsupportedOperationException("cannot make operation for " + getFullName());
        }

        verifyRunning();

        return operationMaker.apply(params, currentConfig);
    }

    // these may be overridden by junit tests

    protected HttpClientFactory getClientFactory() {
        return HttpClientFactoryInstance.getClientFactory();
    }
}
