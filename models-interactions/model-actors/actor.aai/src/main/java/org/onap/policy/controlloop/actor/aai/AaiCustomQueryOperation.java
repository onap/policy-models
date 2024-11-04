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

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.message.bus.event.Topic.CommInfrastructure;
import org.onap.policy.common.message.bus.utils.NetLoggerUtil.EventType;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;

/**
 * A&AI Custom Query. Stores the {@link AaiCqResponse} in the context. In addition, if the
 * context does not contain the "tenant" data for the vserver, then it will request that,
 * as well. Note: this ignores the "target entity" in the parameters as this query always
 * applies to the vserver, thus the target entity may be set to an empty string.
 */
public class AaiCustomQueryOperation extends HttpOperation<String> {
    public static final String NAME = AaiCqResponse.OPERATION;

    public static final String VSERVER_VSERVER_NAME = "vserver.vserver-name";
    public static final String RESOURCE_LINK = "resource-link";
    public static final String RESULT_DATA = "result-data";

    private static final List<String> PROPERTY_NAMES = List.of(OperationProperties.AAI_VSERVER_LINK);

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public AaiCustomQueryOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, String.class, PROPERTY_NAMES);
    }

    @Override
    public void generateSubRequestId(int attempt) {
        setSubRequestId(String.valueOf(attempt));
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {
        outcome.setSubRequestId(String.valueOf(attempt));

        final Map<String, String> request = makeRequest();
        Map<String, Object> headers = makeHeaders();

        var str = new StringBuilder(getClient().getBaseUrl());

        String path = getPath();
        WebTarget web = getClient().getWebTarget().path(path);
        str.append(path);

        web = addQuery(web, str);

        Builder webldr = web.request();
        for (Entry<String, Object> header : headers.entrySet()) {
            webldr.header(header.getKey(), header.getValue());
        }

        var url = str.toString();

        String strRequest = prettyPrint(request);
        logMessage(EventType.OUT, CommInfrastructure.REST, url, strRequest);

        Entity<String> entity = Entity.entity(strRequest, MediaType.APPLICATION_JSON);

        return handleResponse(outcome, url, callback -> webldr.async().put(entity, callback));
    }

    private WebTarget addQuery(WebTarget web, StringBuilder str) {
        str.append("?");
        str.append("format");
        str.append('=');
        str.append("resource");

        return web.queryParam("format", "resource");
    }

    /**
     * Constructs the custom query using the previously retrieved tenant data.
     */
    private Map<String, String> makeRequest() {
        return Map.of("start", getVserverLink(), "query", "query/closed-loop");
    }

    /**
     * Gets the vserver link, first checking the properties, and then the tenant data.
     *
     * @return the vserver link
     */
    protected String getVserverLink() {
        return getRequiredProperty(OperationProperties.AAI_VSERVER_LINK, "vserver link");
    }

    @Override
    protected Map<String, Object> makeHeaders() {
        return AaiUtil.makeHeaders(params);
    }

    @Override
    public OperationOutcome setOutcome(OperationOutcome outcome, OperationResult result, Response rawResponse,
                    String response) {

        super.setOutcome(outcome, result, rawResponse, response);

        if (response != null) {
            outcome.setResponse(new AaiCqResponse(response));
        }

        return outcome;
    }
}
