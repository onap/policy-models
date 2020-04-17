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

package org.onap.policy.controlloop.actor.aai;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A&AI Custom Query. Stores the {@link AaiCqResponse} in the context. In addition, if the
 * context does not contain the "tenant" data for the vserver, then it will request that,
 * as well. Note: this ignores the "target entity" in the parameters as this query always
 * applies to the vserver, thus the target entity may be set to an empty string.
 */
public class AaiCustomQueryOperation extends HttpOperation<String> {
    private static final Logger logger = LoggerFactory.getLogger(AaiCustomQueryOperation.class);

    public static final String NAME = AaiCqResponse.OPERATION;

    public static final String VSERVER_VSERVER_NAME = "vserver.vserver-name";
    public static final String RESOURCE_LINK = "resource-link";
    public static final String RESULT_DATA = "result-data";

    // TODO make this configurable
    private static final String PREFIX = "/aai/v16";

    @Getter
    private final String vserver;

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public AaiCustomQueryOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, String.class);

        this.vserver = params.getContext().getEnrichment().get(VSERVER_VSERVER_NAME);
        if (StringUtils.isBlank(this.vserver)) {
            throw new IllegalArgumentException("missing " + VSERVER_VSERVER_NAME + " in enrichment data");
        }
    }

    /**
     * Queries the vserver, if necessary.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        ControlLoopOperationParams tenantParams =
                        params.toBuilder().actor(AaiConstants.ACTOR_NAME).operation(AaiGetTenantOperation.NAME)
                                        .targetEntity(vserver).payload(null).retry(null).timeoutSec(null).build();

        return params.getContext().obtain(AaiGetTenantOperation.getKey(vserver), tenantParams);
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

        StringBuilder str = new StringBuilder(getClient().getBaseUrl());

        String path = getPath();
        WebTarget web = getClient().getWebTarget().path(path);
        str.append(path);

        web = addQuery(web, str, "?", "format", "resource");

        Builder webldr = web.request();
        for (Entry<String, Object> header : headers.entrySet()) {
            webldr.header(header.getKey(), header.getValue());
        }

        String url = str.toString();

        logMessage(EventType.OUT, CommInfrastructure.REST, url, request);

        Entity<Map<String, String>> entity = Entity.entity(request, MediaType.APPLICATION_JSON);

        return handleResponse(outcome, url, callback -> webldr.async().put(entity, callback));
    }

    private WebTarget addQuery(WebTarget web, StringBuilder str, String separator, String name, String value) {
        str.append(separator);
        str.append(name);
        str.append('=');
        str.append(value);

        return web.queryParam(name, value);
    }

    /**
     * Constructs the custom query using the previously retrieved tenant data.
     */
    private Map<String, String> makeRequest() {
        StandardCoderObject tenant = params.getContext().getProperty(AaiGetTenantOperation.getKey(vserver));

        String resourceLink = tenant.getString(RESULT_DATA, 0, RESOURCE_LINK);
        if (resourceLink == null) {
            throw new IllegalArgumentException("cannot perform custom query - no resource-link");
        }

        resourceLink = resourceLink.replace(PREFIX, "");

        return Map.of("start", resourceLink, "query", "query/closed-loop");
    }

    @Override
    protected Map<String, Object> makeHeaders() {
        return AaiUtil.makeHeaders(params);
    }

    /**
     * Injects the response into the context.
     */
    @Override
    protected CompletableFuture<OperationOutcome> postProcessResponse(OperationOutcome outcome, String url,
                    Response rawResponse, String response) {

        logger.info("{}: caching response for {}", getFullName(), params.getRequestId());
        params.getContext().setProperty(AaiCqResponse.CONTEXT_KEY, new AaiCqResponse(response));

        return super.postProcessResponse(outcome, url, rawResponse, response);
    }
}
