/*-
 * ============LICENSE_START=======================================================
 * AppcLcmOperation
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

package org.onap.policy.controlloop.actor.appclcm;

import java.util.concurrent.CompletableFuture;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.appclcm.AppcLcmDmaapWrapper;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigModifyOperation extends AppcLcmOperation {

    private static final Logger logger = LoggerFactory.getLogger(ConfigModifyOperation.class);

    public static final String NAME = "ConfigModify";

    // Strings for recipes
    private static final String RECIPE_RESTART = "Restart";
    private static final String RECIPE_REBUILD = "Rebuild";
    private static final String RECIPE_MIGRATE = "Migrate";
    private static final String RECIPE_MODIFY = "ConfigModify";

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public ConfigModifyOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
        super(params, config);
    }

    /**
     * Ensures that A&AI customer query has been performed, and then runs the guard query.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        ControlLoopOperationParams cqParams = params.toBuilder().actor(AaiConstants.ACTOR_NAME)
                .operation(AaiCqResponse.OPERATION).payload(null).retry(null).timeoutSec(null).build();

        // run Custom Query and Guard, in parallel
        return allOf(() -> params.getContext().obtain(AaiCqResponse.CONTEXT_KEY, cqParams), this::startGuardAsync);
    }

    @Override
    protected AppcLcmDmaapWrapper makeRequest(int attempt) {
        AaiCqResponse cq = params.getContext().getProperty(AaiCqResponse.CONTEXT_KEY);

        GenericVnf genvnf = cq.getGenericVnfByModelInvariantId(params.getTarget().getResourceID());
        if (genvnf == null) {
            logger.info("{}: target entity could not be found for {}", getFullName(), params.getRequestId());
            throw new IllegalArgumentException("target vnf-id could not be found");
        }

        return makeRequest(attempt, genvnf.getVnfId());
    }

    @Override
    protected boolean recipeSupportsPayload(String recipe) {
        return !RECIPE_RESTART.equalsIgnoreCase(recipe) && !RECIPE_REBUILD.equalsIgnoreCase(recipe)
                && !RECIPE_MIGRATE.equalsIgnoreCase(recipe);
    }

    @Override
    protected boolean operationSupportsPayload() {
        return params.getPayload() != null && !params.getPayload().isEmpty();
    }
}
