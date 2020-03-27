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

package org.onap.policy.controlloop.actor.sdnc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.sdnc.SdncRequest;
import org.onap.policy.sdnc.SdncResponse;

/**
 * Superclass for SDNC Operators.
 */
public abstract class SdncOperation extends HttpOperation<SdncResponse> {

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public SdncOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, SdncResponse.class);
    }

    /**
     * Starts the GUARD.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        return startGuardAsync();
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        SdncRequest request = makeRequest(attempt);
        outcome.setSubRequestId(request.getHealRequest().getRequestHeaderInfo().getSvcRequestId());

        Entity<SdncRequest> entity = Entity.entity(request, MediaType.APPLICATION_JSON);

        Map<String, Object> headers = makeHeaders();

        headers.put("Accept", MediaType.APPLICATION_JSON);
        String path = getPath();
        String url = getClient().getBaseUrl() + path;

        logMessage(EventType.OUT, CommInfrastructure.REST, url, request);

        // @formatter:off
        return handleResponse(outcome, url,
            callback -> getClient().post(callback, path, entity, headers));
        // @formatter:on
    }

    /**
     * Makes the request.
     *
     * @param attempt current attempt, starting with "1"
     * @return a new request to be posted
     */
    protected abstract SdncRequest makeRequest(int attempt);

    /**
     * Checks that the response has an "output" and that the output indicates success.
     */
    @Override
    protected boolean isSuccess(Response rawResponse, SdncResponse response) {
        return response.getResponseOutput() != null && "200".equals(response.getResponseOutput().getResponseCode());
    }
}
