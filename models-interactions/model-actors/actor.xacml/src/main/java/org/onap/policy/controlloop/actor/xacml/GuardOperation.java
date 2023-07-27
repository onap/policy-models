/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

/**
 * Guard Operation. The outcome message is set to the guard response. If the guard is
 * permitted or indeterminate, then the outcome is set to SUCCESS.
 * <p/>
 * The input to the request is taken from the payload, where properties are mapped to the
 * field names in the {@link DecisionRequest} object. Properties whose names begin with
 * "resource." are placed into the "resource" field of the {@link DecisionRequest}. The
 * following will be provided, if not specified in the payload:
 * <dl>
 * <dt>action</dt>
 * <dd>"guard"</dd>
 * <dt>request ID</dt>
 * <dd>generated</dd>
 * </dl>
 */
public class GuardOperation extends DecisionOperation {
    // operation name
    public static final String NAME = "Guard";

    public static final String PERMIT = "Permit";
    public static final String DENY = "Deny";
    public static final String INDETERMINATE = "Indeterminate";


    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public GuardOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, Collections.emptyList());
    }

    /**
     * Makes a request from the payload.
     *
     * @return a new request
     */
    protected DecisionRequest makeRequest() {
        if (params.getPayload() == null) {
            throw new IllegalArgumentException("missing payload");
        }

        DecisionRequest req = config.makeRequest();
        req.setRequestId(getSubRequestId());
        req.setResource(Map.of("guard", params.getPayload()));

        return req;
    }

    @Override
    protected CompletableFuture<OperationOutcome> postProcessResponse(OperationOutcome outcome, String url,
                    Response rawResponse, DecisionResponse response) {

        outcome.setResponse(response);

        // determine the result
        String status = response.getStatus();
        if (status == null) {
            outcome.setResult(OperationResult.FAILURE);
            outcome.setMessage("response contains no status");
            return CompletableFuture.completedFuture(outcome);
        }

        if (PERMIT.equalsIgnoreCase(status) || INDETERMINATE.equalsIgnoreCase(status)) {
            outcome.setResult(OperationResult.SUCCESS);
        } else {
            outcome.setResult(OperationResult.FAILURE);
        }

        // set the message
        outcome.setMessage(response.getStatus());

        return CompletableFuture.completedFuture(outcome);
    }
}
