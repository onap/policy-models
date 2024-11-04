/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.aai;

import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.common.message.bus.event.Topic.CommInfrastructure;
import org.onap.policy.common.message.bus.utils.NetLoggerUtil.EventType;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;

/**
 * A&AI get-pnf operator.
 */
public class AaiGetPnfOperation extends AaiGetOperation {
    private static final String URI_SEP = "/";

    public static final String NAME = "Pnf";

    // property prefixes
    private static final String KEY_PREFIX = AaiConstants.CONTEXT_PREFIX + NAME + ".";

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public AaiGetPnfOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config);
    }

    /**
     * Gets the "context key" for the PNF query response associated with the given
     * target entity.
     *
     * @param targetEntity target entity
     * @return the "context key" for the response associated with the given target
     */
    public static String getKey(String targetEntity) {
        return (KEY_PREFIX + targetEntity);
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {
        Map<String, Object> headers = makeHeaders();

        var str = new StringBuilder(getClient().getBaseUrl());

        String target = getRequiredProperty(OperationProperties.AAI_TARGET_ENTITY, "target entity");
        String path = getPath() + URI_SEP + URLEncoder.encode(target, StandardCharsets.UTF_8);
        WebTarget web = getClient().getWebTarget().path(path);
        str.append(path);

        web = addQuery(web, str, "?", "depth", "0");

        Builder webldr = web.request();
        addHeaders(webldr, headers);

        var url = str.toString();

        logMessage(EventType.OUT, CommInfrastructure.REST, url, null);

        return handleResponse(outcome, url, callback -> webldr.async().get(callback));
    }
}
