/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.controlloop.actor.xacml;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;

public class ConfigureOperation extends DecisionOperation {

    // operation name
    public static final String NAME = "Configure";

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public ConfigureOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, Collections.emptyList());
    }

    @Override
    protected DecisionRequest makeRequest() {
        if (params.getPayload() == null) {
            throw new IllegalArgumentException("missing payload");
        }

        DecisionRequest req = config.makeRequest();
        req.setRequestId(getSubRequestId());
        req.setResource(params.getPayload());

        return req;
    }

    @Override
    protected CompletableFuture<OperationOutcome> postProcessResponse(OperationOutcome outcome, String url,
                    Response rawResponse, DecisionResponse response) {

        outcome.setResponse(response);

        // check for policies
        Map<String, Object> policies = response.getPolicies();
        if (policies == null || policies.isEmpty()) {
            outcome.setResult(OperationResult.FAILURE);
            outcome.setMessage("response contains no policies");
            return CompletableFuture.completedFuture(outcome);
        }

        outcome.setResult(OperationResult.SUCCESS);

        // set the message
        outcome.setMessage(response.getMessage());

        return CompletableFuture.completedFuture(outcome);
    }
}
