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

package org.onap.policy.controlloop.actorserviceprovider.parameters;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;

/**
 * Configuration for HTTP Operators.
 */
@Getter
public class HttpConfig extends OperatorConfig {

    private final HttpClient client;

    /**
     * Default timeout, in milliseconds, if none specified in the request.
     */
    private final long timeoutMs;

    /**
     * URI path for this particular operation. Includes a leading "/".
     */
    private final String path;


    /**
     * Constructs the object.
     *
     * @param blockingExecutor executor to be used for tasks that may perform blocking I/O
     * @param params operator parameters
     * @param clientFactory factory from which to obtain the {@link HttpClient}
     */
    public HttpConfig(Executor blockingExecutor, HttpParams params, HttpClientFactory clientFactory) {
        super(blockingExecutor);
        client = clientFactory.get(params.getClientName());
        path = params.getPath();
        timeoutMs = TimeUnit.MILLISECONDS.convert(params.getTimeoutSec(), TimeUnit.SECONDS);
    }
}
