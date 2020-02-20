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

package org.onap.policy.controlloop.actor.so;

import java.util.concurrent.Executor;
import lombok.Getter;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;

@Getter
public class SoConfig extends HttpConfig {

    /**
     * Path to use for the "get" request. A trailing "/" is added, if it is missing.
     */
    private String pathGet;

    /**
     * Maximum number of "get" requests permitted, after the initial request, to retrieve
     * the response.
     */
    private int maxGets;

    /**
     * Time, in seconds, to wait between issuing "get" requests.
     */
    private int waitSecGet;


    /**
     * Constructs the object.
     *
     * @param blockingExecutor executor to be used for tasks that may perform blocking I/O
     * @param params operator parameters
     * @param clientFactory factory from which to obtain the {@link HttpClient}
     */
    public SoConfig(Executor blockingExecutor, SoParams params, HttpClientFactory clientFactory) {
        super(blockingExecutor, params, clientFactory);

        this.pathGet = params.getPathGet() + (params.getPathGet().endsWith("/") ? "" : "/");
        this.maxGets = params.getMaxGets();
        this.waitSecGet = params.getWaitSecGet();
    }
}
