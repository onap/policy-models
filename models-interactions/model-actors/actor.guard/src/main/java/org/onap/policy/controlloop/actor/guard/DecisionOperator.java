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

package org.onap.policy.controlloop.actor.guard;

import java.util.Map;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperator;
import org.onap.policy.controlloop.actorserviceprovider.impl.OperationMaker;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

/**
 * Guard Operator.
 */
public class DecisionOperator extends HttpOperator {

    public DecisionOperator(String actorName, String name,
                    @SuppressWarnings("rawtypes") OperationMaker<HttpConfig, HttpOperation> operationMaker) {
        super(actorName, name, operationMaker);
    }

    @Override
    protected HttpConfig makeConfiguration(Map<String, Object> parameters) {
        GuardParams params = Util.translate(getFullName(), parameters, GuardParams.class);
        ValidationResult result = params.validate(getFullName());
        if (!result.isValid()) {
            throw new ParameterValidationRuntimeException("invalid parameters", result);
        }

        return new GuardConfig(getBlockingExecutor(), params, getClientFactory());
    }
}
