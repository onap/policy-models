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
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A&AI Custom Query. Stores the {@link AaiCqResponse} in the context.
 */
public class AaiCustomQueryOperation extends HttpOperation<String> {
    private static final Logger logger = LoggerFactory.getLogger(AaiCustomQueryOperation.class);

    public static final String NAME = "CustomQuery";

    public static final String RESOURCE_LINK = "resource-link";
    public static final String RESULT_DATA = "result-data";

    private static final String PREFIX = "/aai/v16";

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param operator operator that created this operation
     */
    public AaiCustomQueryOperation(ControlLoopOperationParams params, HttpOperator operator) {
        super(params, operator, String.class);
    }

    /**
     * Queries the vserver, if necessary.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        String vserver = params.getTargetEntity();

        ControlLoopOperationParams tenantParams = params.toBuilder().actor(AaiConstants.ACTOR_NAME)
                        .operation(AaiGetOperation.TENANT).payload(null).retry(null).timeoutSec(null).build();

        return params.getContext().obtain(AaiGetOperation.getTenantKey(vserver), tenantParams);
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        Map<String, String> request = makeRequest();

        Entity<Map<String, String>> entity = Entity.entity(request, MediaType.APPLICATION_JSON);

        Map<String, Object> headers = makeHeaders();

        headers.put("Accept", MediaType.APPLICATION_JSON);
        String url = makeUrl();

        logRestRequest(url, request);

        // @formatter:off
        return handleResponse(outcome, url,
            callback -> operator.getClient().put(callback, makePath(), entity, headers));
        // @formatter:on
    }

    /**
     * Constructs the custom query using the previously retrieved tenant data.
     */
    private Map<String, String> makeRequest() {
        String vserver = params.getTargetEntity();
        StandardCoderObject tenant = params.getContext().getProperty(AaiGetOperation.getTenantKey(vserver));

        String resourceLink = tenant.getString(RESULT_DATA, 0, RESOURCE_LINK);
        if (resourceLink == null) {
            throw new IllegalArgumentException("cannot perform custom query - no resource-link");
        }

        return Map.of("start", resourceLink.replace(PREFIX, ""), "query", "query/closed-loop");
    }

    @Override
    protected Map<String, Object> makeHeaders() {
        return AaiUtil.makeHeaders(params);
    }

    /**
     * Injects the response into the context.
     */
    @Override
    protected void postProcessResponse(OperationOutcome outcome, String url, Response rawResponse, String response) {

        logger.info("{}: caching response for {}", getFullName(), params.getRequestId());
        params.getContext().setProperty(AaiCqResponse.CONTEXT_KEY, new AaiCqResponse(response));
    }
}
