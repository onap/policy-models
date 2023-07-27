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

package org.onap.policy.controlloop.actor.sdnc;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
     * @param propertyNames names of properties required by this operation
     */
    protected SdncOperation(ControlLoopOperationParams params, HttpConfig config, List<String> propertyNames) {
        super(params, config, SdncResponse.class, propertyNames);
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        SdncRequest request = makeRequest(attempt);

        Map<String, Object> headers = makeHeaders();

        headers.put("Accept", MediaType.APPLICATION_JSON);
        String path = getPath();
        String url = getClient().getBaseUrl() + path;

        String strRequest = prettyPrint(request);
        logMessage(EventType.OUT, CommInfrastructure.REST, url, strRequest);

        Entity<String> entity = Entity.entity(strRequest, MediaType.APPLICATION_JSON);

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
