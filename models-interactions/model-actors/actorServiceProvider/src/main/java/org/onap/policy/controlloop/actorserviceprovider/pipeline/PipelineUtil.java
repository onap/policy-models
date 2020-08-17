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

package org.onap.policy.controlloop.actorserviceprovider.pipeline;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.OperationPartial;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.OperatorConfig;

/**
 * Utility class providing anyOf(), allOf(), sequence() to actor clients.
 */
public class PipelineUtil extends OperationPartial {

    /**
     * Constructs the utility.
     *
     * @param params utility parameters
     */
    public PipelineUtil(ControlLoopOperationParams params) {
        super(params, new OperatorConfig(ForkJoinPool.commonPool()));
    }

    @Override
    public CompletableFuture<OperationOutcome> start() {
        throw new UnsupportedOperationException("cannot start() pipeline utility");
    }
}
