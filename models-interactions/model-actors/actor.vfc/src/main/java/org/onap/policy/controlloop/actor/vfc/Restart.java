/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.vfc.VfcRequest;

public class Restart extends VfcOperation {
    public static final String NAME = "Restart";

    public Restart(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config);
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        // starting a whole new attempt - reset the count
        resetPollCount();

        Pair<String, VfcRequest> pair = makeRequest();
        Entity<VfcRequest> entity = Entity.entity(pair.getRight(), MediaType.APPLICATION_JSON);
        String path = getPath() + pair.getLeft();
        String url = getClient().getBaseUrl() + path;

        Map<String, Object> headers = makeHeaders();
        headers.put("Accept", MediaType.APPLICATION_JSON);

        return handleResponse(outcome, url, callback -> getClient().post(callback, path, entity, headers));
    }

    /**
     * Makes a request.
     *
     * @return a pair containing the request URL and the new request
     */
    protected Pair<String, VfcRequest> makeRequest() {

        var request = super.constructVfcRequest();
        String requestUrl = "/" + request.getNsInstanceId() + "/heal";
        return Pair.of(requestUrl, request);
    }
}
