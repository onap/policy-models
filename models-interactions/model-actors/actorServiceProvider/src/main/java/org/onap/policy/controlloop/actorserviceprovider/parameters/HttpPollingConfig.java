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
import lombok.Getter;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;

/**
 * Configuration for HTTP Operators that, after issuing a request, must poll the target
 * server to determine the request completion status.
 */
@Getter
public class HttpPollingConfig extends HttpConfig {

    /**
     * Path to use when polling for request completion. A trailing "/" is added, if it is
     * missing.
     */
    private String pollPath;

    /**
     * Maximum number of times to poll to retrieve the response.
     */
    private int maxPolls;

    /**
     * Time, in seconds, to wait between polling.
     */
    private int pollWaitSec;


    /**
     * Constructs the object.
     *
     * @param blockingExecutor executor to be used for tasks that may perform blocking I/O
     * @param params operator parameters
     * @param clientFactory factory from which to obtain the {@link HttpClient}
     */
    public HttpPollingConfig(Executor blockingExecutor, HttpPollingParams params, HttpClientFactory clientFactory) {
        super(blockingExecutor, params, clientFactory);

        this.pollPath = params.getPollPath() + (params.getPollPath().endsWith("/") ? "" : "/");
        this.maxPolls = params.getMaxPolls();
        this.pollWaitSec = params.getPollWaitSec();
    }
}
