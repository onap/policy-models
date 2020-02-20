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

package org.onap.policy.controlloop.actor.so;

import java.util.Map;
import java.util.function.BiFunction;
import lombok.Getter;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

@Getter
public abstract class SoOperator extends HttpOperator {

    /**
     * Path to use for the "get" request. A trailing "/" is added, if it is missing.
     */
    private String pathGet;

    /**
     * Maximum number of "get" requests permitted, after the initial request, to retrieve
     * the response.
     */
    private int maxGets;

    /**
     * Time, in seconds, to wait between issuing "get" requests.
     */
    private int waitSecGet;


    public SoOperator(String actorName, String name) {
        super(actorName, name);
    }

    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        SoParams params = Util.translate(getFullName(), parameters, SoParams.class);
        ValidationResult result = params.validate(getFullName());
        if (!result.isValid()) {
            throw new ParameterValidationRuntimeException("invalid parameters", result);
        }

        this.pathGet = params.getPathGet() + (params.getPathGet().endsWith("/") ? "" : "/");
        this.maxGets = params.getMaxGets();
        this.waitSecGet = params.getWaitSecGet();

        super.doConfigure(params);
    }

    /**
     * Makes an operator that will construct operations.
     *
     * @param actorName actor name
     * @param operation operation name
     * @param operationMaker function to make an operation
     * @return a new operator
     */
    public static SoOperator makeSoOperator(String actorName, String operation,
                    BiFunction<ControlLoopOperationParams, SoOperator, SoOperation> operationMaker) {

        return new SoOperator(actorName, operation) {
            @Override
            public Operation buildOperation(ControlLoopOperationParams params) {
                return operationMaker.apply(params, this);
            }
        };
    }
}
