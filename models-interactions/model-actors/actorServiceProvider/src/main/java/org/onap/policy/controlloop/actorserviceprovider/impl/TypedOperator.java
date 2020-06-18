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
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

/**
 * Operator with typed parameter information.
 *
 * @param <C> type of configuration data
 * @param <T> type of operation that the operator creates
 */
public abstract class TypedOperator<C, T extends Operation> extends OperatorPartial {

    /**
     * Function to make an operation.
     */
    private final OperationMaker<C, T> operationMaker;

    /**
     * Current configuration. This is set by {@link #doConfigure(Map)}.
     */
    @Getter
    private C currentConfig;


    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     */
    protected TypedOperator(String actorName, String name) {
        this(actorName, name, null);
    }

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     * @param operationMaker function to make an operation
     */
    public TypedOperator(String actorName, String name, OperationMaker<C, T> operationMaker) {
        super(actorName, name);
        this.operationMaker = operationMaker;
    }

    /**
     * Translates the parameters, saving the relevant configuration data.
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
    protected abstract C makeConfiguration(Map<String, Object> parameters);

    @Override
    public T buildOperation(ControlLoopOperationParams params) {
        if (operationMaker == null) {
            throw new UnsupportedOperationException("cannot make operation for " + getFullName());
        }

        verifyRunning();

        return operationMaker.apply(params, currentConfig);
    }
}
