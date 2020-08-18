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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.controlloop.actorserviceprovider.CallbackManager;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.impl.OperationPartial;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class DecisionOperation extends HttpOperation<DecisionResponse> {
    private static final Logger logger = LoggerFactory.getLogger(DecisionOperation.class);

    // operation name
    public static final String NAME = OperationPartial.GUARD_OPERATION_NAME;

    public static final String PERMIT = "Permit";
    public static final String DENY = "Deny";
    public static final String INDETERMINATE = "Indeterminate";

    /**
     * Prefix for properties in the payload that should be copied to the "resource" field
     * of the request.
     */
    public static final String RESOURCE_PREFIX = "resource.";

    private final GuardConfig config;


    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public DecisionOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, DecisionResponse.class, Collections.emptyList());
        this.config = (GuardConfig) config;
    }

    @Override
    public CompletableFuture<OperationOutcome> start() {
        if (!config.isDisabled()) {
            // enabled - do full guard operation
            return super.start();
        }

        // guard is disabled, thus it is always treated as a success
        logger.info("{}: guard disabled, always succeeds for {}", getFullName(), params.getRequestId());

        final Executor executor = params.getExecutor();
        final CallbackManager callbacks = new CallbackManager();

        return CompletableFuture.completedFuture(params.makeOutcome(getTargetEntity()))
                        .whenCompleteAsync(callbackStarted(callbacks), executor)
                        .whenCompleteAsync(callbackCompleted(callbacks), executor);
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {
        DecisionRequest request = makeRequest();

        Map<String, Object> headers = makeHeaders();

        headers.put("Accept", MediaType.APPLICATION_JSON);
        String url = getUrl();

        String strRequest = prettyPrint(request);
        logMessage(EventType.OUT, CommInfrastructure.REST, url, strRequest);

        Entity<String> entity = Entity.entity(strRequest, MediaType.APPLICATION_JSON);

        // @formatter:off
        return handleResponse(outcome, url,
            callback -> getClient().post(callback, getPath(), entity, headers));
        // @formatter:on
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
            outcome.setResult(PolicyResult.FAILURE);
            outcome.setMessage("response contains no status");
            return CompletableFuture.completedFuture(outcome);
        }

        if (PERMIT.equalsIgnoreCase(status) || INDETERMINATE.equalsIgnoreCase(status)) {
            outcome.setResult(PolicyResult.SUCCESS);
        } else {
            outcome.setResult(PolicyResult.FAILURE);
        }

        // set the message
        outcome.setMessage(response.getStatus());

        return CompletableFuture.completedFuture(outcome);
    }
}
