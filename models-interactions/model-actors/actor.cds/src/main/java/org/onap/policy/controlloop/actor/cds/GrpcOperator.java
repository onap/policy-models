/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Bell Canada. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.impl.OperationMaker;
import org.onap.policy.controlloop.actorserviceprovider.impl.OperatorPartial;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.policy.TargetType;

/**
 * Operator that uses gRPC. The operator's parameters must be a
 * {@link CdsServerProperties}.
 */
@Getter
public class GrpcOperator extends OperatorPartial {


    // @formatter:off
    private static final List<String> PNF_PROPERTY_NAMES = List.of(
                            OperationProperties.AAI_PNF,
                            OperationProperties.EVENT_ADDITIONAL_PARAMS,
                            OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES);


    private static final List<String> VNF_PROPERTY_NAMES = List.of(
                            OperationProperties.AAI_RESOURCE_VNF,
                            OperationProperties.AAI_SERVICE,
                            OperationProperties.EVENT_ADDITIONAL_PARAMS,
                            OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES);
    // @formatter:on

    /**
     * Function to make an operation.
     */
    private final OperationMaker<GrpcConfig, GrpcOperation> operationMaker;

    /**
     * Current configuration. This is set by {@link #doConfigure(Map)}.
     */
    private GrpcConfig currentConfig;

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     */
    public GrpcOperator(String actorName, String name) {
        this(actorName, name, null);
    }

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     * @param operationMaker function to make an operation
     */
    public GrpcOperator(String actorName, String name, OperationMaker<GrpcConfig, GrpcOperation> operationMaker) {
        super(actorName, name, Collections.emptyList());
        this.operationMaker = operationMaker;
    }

    /**
     * Translates the parameters to an {@link CdsServerProperties} and then extracts the
     * relevant values.
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
    protected GrpcConfig makeConfiguration(Map<String, Object> parameters) {
        CdsServerProperties params = Util.translate(getFullName(), parameters, CdsServerProperties.class);
        ValidationResult result = params.validate();
        if (!result.isValid()) {
            throw new ParameterValidationRuntimeException("invalid parameters", result);
        }
        return new GrpcConfig(getBlockingExecutor(), params);
    }

    @Override
    public List<String> getPropertyNames(ControlLoopOperationParams params) {
        return (TargetType.PNF.equals(params.getTarget().getType()) ? PNF_PROPERTY_NAMES : VNF_PROPERTY_NAMES);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public Operation buildOperation(ControlLoopOperationParams params) {
        if (operationMaker == null) {
            throw new UnsupportedOperationException("cannot make operation for " + getFullName());
        }
        verifyRunning();
        return operationMaker.apply(params, currentConfig);
    }
}
