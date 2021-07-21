/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.controlloop.actorserviceprovider.CallbackManager;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic Decision Operation.
 */
public abstract class DecisionOperation extends HttpOperation<DecisionResponse> {
    private static final Logger logger = LoggerFactory.getLogger(DecisionOperation.class);

    protected final DecisionConfig config;


    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     * @param propertyNames names of properties required by this operation
     */
    public DecisionOperation(ControlLoopOperationParams params, HttpConfig config,
                    List<String> propertyNames) {
        super(params, config, DecisionResponse.class, propertyNames);
        this.config = (DecisionConfig) config;
    }

    @Override
    public CompletableFuture<OperationOutcome> start() {
        if (!config.isDisabled()) {
            // enabled - do full guard operation
            return super.start();
        }

        // guard is disabled, thus it is always treated as a success
        logger.info("{}: guard disabled, always succeeds for {}", getFullName(), params.getRequestId());

        final var executor = params.getExecutor();
        final var callbacks = new CallbackManager();

        return CompletableFuture.completedFuture(makeOutcome())
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
    protected abstract DecisionRequest makeRequest();
}
