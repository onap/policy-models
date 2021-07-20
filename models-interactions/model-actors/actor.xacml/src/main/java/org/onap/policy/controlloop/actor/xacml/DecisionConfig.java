/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.xacml;

import java.util.concurrent.Executor;
import lombok.Getter;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.models.decisions.concepts.DecisionRequest;

/**
 * Configuration for Decision Operators.
 */
public class DecisionConfig extends HttpConfig {
    private final DecisionRequest defaultRequest = new DecisionRequest();

    /**
     * {@code True} if the associated decision operation is disabled.
     */
    @Getter
    private boolean disabled;

    /**
     * Constructs the object.
     *
     * @param blockingExecutor executor to be used for tasks that may perform blocking I/O
     * @param params operator parameters
     * @param clientFactory factory from which to obtain the {@link HttpClient}
     */
    public DecisionConfig(Executor blockingExecutor, DecisionParams params, HttpClientFactory clientFactory) {
        super(blockingExecutor, params, clientFactory);

        defaultRequest.setOnapComponent(params.getOnapComponent());
        defaultRequest.setOnapInstance(params.getOnapInstance());
        defaultRequest.setOnapName(params.getOnapName());
        defaultRequest.setAction(params.getAction());

        this.disabled = params.isDisabled();
    }

    /**
     * Creates a new request, with the default values.
     *
     * @return a new request
     */
    public DecisionRequest makeRequest() {
        return new DecisionRequest(defaultRequest);
    }
}
