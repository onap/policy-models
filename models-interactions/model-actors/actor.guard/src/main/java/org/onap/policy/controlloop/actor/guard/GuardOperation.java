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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
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
public class GuardOperation extends HttpOperation<DecisionResponse> {
    private static final Logger logger = LoggerFactory.getLogger(GuardOperation.class);

    // operation name
    public static final String NAME = "Decision";

    public static final String PERMIT = "Permit";
    public static final String DENY = "Deny";
    public static final String INDETERMINATE = "Indeterminate";

    private static final String RESOURCE = "resource";

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
    public GuardOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, DecisionResponse.class);
        this.config = (GuardConfig) config;
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        DecisionRequest request = Util.translate(getName(), makeRequest(), DecisionRequest.class);

        Entity<DecisionRequest> entity = Entity.entity(request, MediaType.APPLICATION_JSON);

        Map<String, Object> headers = makeHeaders();

        headers.put("Accept", MediaType.APPLICATION_JSON);
        String url = makeUrl();

        logMessage(EventType.OUT, CommInfrastructure.REST, url, request);

        // @formatter:off
        return handleResponse(outcome, url,
            callback -> getClient().post(callback, makePath(), entity, headers));
        // @formatter:on
    }

    /**
     * Makes a request from the payload.
     *
     * @return a new request map
     */
    protected Map<String, Object> makeRequest() {
        if (params.getPayload() == null) {
            throw new IllegalArgumentException("missing payload");
        }

        /*
         * This code could be easily modified to allow the context and/or resource to be
         * an encoded JSON string, that is decoded into a Map and stuffed into the
         * appropriate field.
         */

        Map<String, Object> req = config.makeRequest();
        Map<String, Object> resource = new LinkedHashMap<>();

        for (Entry<String, String> ent : params.getPayload().entrySet()) {
            String key = ent.getKey();

            if (key.startsWith(RESOURCE_PREFIX)) {
                // it's a resource property - put into the resource map
                key = key.substring(RESOURCE_PREFIX.length());
                resource.put(key, ent.getValue());

            } else if (key.indexOf('.') < 0) {
                // it's a normal property - put into the request map
                req.put(key, ent.getValue());

            } else {
                logger.warn("{}: unused key {} in payload for {}", getFullName(), key, params.getRequestId());
            }
        }

        req.computeIfAbsent("requestId", key -> UUID.randomUUID().toString());
        req.put(RESOURCE, resource);

        return req;
    }

    @Override
    protected CompletableFuture<OperationOutcome> postProcessResponse(OperationOutcome outcome, String url,
                    Response rawResponse, DecisionResponse response) {

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
