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
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass of A&AI operators that use "get" to perform their request and store their
 * response within the context as a {@link StandardCoderObject}. The property name under
 * which they are stored is ${actor}.${operation}.${targetEntity}.
 */
public class AaiGetOperation extends HttpOperation<StandardCoderObject> {
    private static final Logger logger = LoggerFactory.getLogger(AaiGetOperation.class);

    public static final int DEFAULT_RETRY = 3;

    // operation names
    public static final String TENANT = "Tenant";

    // property prefixes
    private static final String TENANT_KEY_PREFIX = AaiConstants.CONTEXT_PREFIX + TENANT + ".";

    /**
     * Operation names supported by this operator.
     */
    public static final Set<String> OPERATIONS = Set.of(TENANT);


    /**
     * Responses that are retrieved from A&AI are placed in the operation context under
     * the name "${propertyPrefix}.${targetEntity}".
     */
    private final String propertyPrefix;

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param operator operator that created this operation
     */
    public AaiGetOperation(ControlLoopOperationParams params, HttpOperator operator) {
        super(params, operator, StandardCoderObject.class);
        this.propertyPrefix = operator.getFullName() + ".";
    }

    /**
     * Gets the "context key" for the tenant query response associated with the given
     * target entity.
     *
     * @param targetEntity target entity
     * @return the "context key" for the response associated with the given target
     */
    public static String getTenantKey(String targetEntity) {
        return (TENANT_KEY_PREFIX + targetEntity);
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        Map<String, Object> headers = makeHeaders();

        headers.put("Accept", MediaType.APPLICATION_JSON);
        String url = makeUrl();

        logMessage(EventType.OUT, CommInfrastructure.REST, url, null);

        // @formatter:off
        return handleResponse(outcome, url,
            callback -> getOperator().getClient().get(callback, makePath(), headers));
        // @formatter:on
    }

    @Override
    protected Map<String, Object> makeHeaders() {
        return AaiUtil.makeHeaders(params);
    }

    @Override
    public String makePath() {
        return (getOperator().getPath() + "/" + params.getTargetEntity());
    }

    /**
     * Injects the response into the context.
     */
    @Override
    protected CompletableFuture<OperationOutcome> postProcessResponse(OperationOutcome outcome, String url,
                    Response rawResponse, StandardCoderObject response) {
        String entity = params.getTargetEntity();

        logger.info("{}: caching response of {} for {}", getFullName(), entity, params.getRequestId());

        params.getContext().setProperty(propertyPrefix + entity, response);

        return super.postProcessResponse(outcome, url, rawResponse, response);
    }

    /**
     * Provides a default retry value, if none specified.
     */
    @Override
    protected int getRetry(Integer retry) {
        return (retry == null ? DEFAULT_RETRY : retry);
    }
}
