/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited.
 * Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

package org.onap.policy.controlloop.actor.so;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.onap.policy.common.message.bus.event.Topic;
import org.onap.policy.common.message.bus.utils.NetLoggerUtil;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.so.SoRequest3gpp;

public class ModifyNssi extends SoOperation {
    public static final String NAME = "Modify NSSI";

    private static final List<String> PROPERTY_NAMES = List.of(
            OperationProperties.EVENT_PAYLOAD);

    /**
     * Constructs the object.
     *
     * @param params        operation parameters
     * @param config        configuration for this operation
     */
    public ModifyNssi(ControlLoopOperationParams params, HttpPollingConfig config) {
        super(params, config, PROPERTY_NAMES);
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        SoRequest3gpp soRequest = makeRequest();

        String path = getPath();
        String url = getClient().getBaseUrl() + path;

        String strRequest = prettyPrint(soRequest);
        logMessage(NetLoggerUtil.EventType.OUT, Topic.CommInfrastructure.REST, url, strRequest);

        Entity<String> entity = Entity.entity(strRequest, MediaType.APPLICATION_JSON);
        Map<String, Object> headers = createSimpleHeaders();

        return handleResponse(outcome, url, callback -> getClient().put(callback, path, entity, headers));
    }

    protected SoRequest3gpp makeRequest() {

        String payload = getRequiredProperty(OperationProperties.EVENT_PAYLOAD, "event payload");
        try {
            return getCoder().convert(payload, SoRequest3gpp.class);
        } catch (CoderException e) {
            throw new IllegalArgumentException("invalid payload value: " + payload, e);
        }
    }
}
