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

package org.onap.policy.controlloop.actor.appc;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.appc.Request;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModifyConfigOperation extends AppcOperation {
    private static final Logger logger = LoggerFactory.getLogger(ModifyConfigOperation.class);

    public static final String NAME = "ModifyConfig";

    public static final List<String> PROPERTY_NAMES = List.of(OperationProperties.AAI_RESOURCE_VNF);

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public ModifyConfigOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
        super(params, config);
    }

    /**
     * Ensures that A&AI customer query has been performed, and then runs the guard query.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        if (params.isPreprocessed()) {
            return null;
        }

        ControlLoopOperationParams cqParams = params.toBuilder().actor(AaiConstants.ACTOR_NAME)
                        .operation(AaiCqResponse.OPERATION).payload(null).retry(null).timeoutSec(null).build();

        // run Custom Query and Guard, in parallel
        return allOf(() -> params.getContext().obtain(AaiCqResponse.CONTEXT_KEY, cqParams), this::startGuardAsync);
    }

    @Override
    protected Request makeRequest(int attempt) {
        return makeRequest(attempt, getVnfId());
    }

    protected String getVnfId() {
        GenericVnf vnf = this.getProperty(OperationProperties.AAI_RESOURCE_VNF);
        if (vnf != null) {
            return vnf.getVnfId();
        }

        AaiCqResponse cq = params.getContext().getProperty(AaiCqResponse.CONTEXT_KEY);
        if (cq == null) {
            throw new IllegalStateException("target vnf-id could not be determined");
        }

        GenericVnf genvnf = cq.getGenericVnfByModelInvariantId(params.getTarget().getResourceID());
        if (genvnf == null) {
            logger.info("{}: target entity could not be found for {}", getFullName(), params.getRequestId());
            throw new IllegalArgumentException("target vnf-id could not be found");
        }

        return genvnf.getVnfId();
    }
}
