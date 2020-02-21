/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Bell Canada. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.controlloop.actorserviceprovider.parameters.OperatorConfig;

/**
 * Configuration for gRPC Operators.
 */
@Getter
public class GrpcConfig extends OperatorConfig {

    /**
     * Default timeout, in milliseconds, if none specified in the request.
     */
    private final long timeoutMs;

    private CdsServerProperties cdsServerProperties;

    /**
     * Constructs the object.
     *
     * @param blockingExecutor executor to be used for tasks that may perform blocking I/O
     * @param params operator parameters
     */
    public GrpcConfig(Executor blockingExecutor, CdsServerProperties params) {
        super(blockingExecutor);
        cdsServerProperties = params;
        timeoutMs = TimeUnit.MILLISECONDS.convert(params.getTimeout(), TimeUnit.SECONDS);
    }
}
