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

package org.onap.policy.controlloop.actor.vfc;

import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.vfc.VfcRequest;

public class Restart extends VfcOperation {
    public static final String NAME = "VF Module Create";

    public Restart(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config);
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        // starting a whole new attempt - reset the count
        resetGetCount();

        Pair<String, VfcRequest> pair = makeRequest();
        String path = getPath() + pair.getLeft();
        String url = getClient().getBaseUrl() + path;

        return handleResponse(outcome, url, callback -> getClient().get(callback, path, null));
    }

    /**
     * Makes a request.
     *
     * @return a pair containing the request URL and the new request
     */
    protected Pair<String, VfcRequest> makeRequest() {

        VfcRequest request = super.constructVfcRequest();
        String requestUrl = makeUrl();
        return Pair.of(requestUrl, request);
    }
}
