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

package org.onap.policy.controlloop.actor.sdnc;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.sdnc.SdncRequest;
import org.onap.policy.sdnc.SdncResponse;

/**
 * Superclass for SDNC Operators.
 */
public abstract class SdncOperator extends HttpOperator<SdncRequest, SdncResponse> {

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     */
    public SdncOperator(String actorName, String name) {
        super(actorName, name, SdncResponse.class);
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(ControlLoopOperationParams params, int attempt,
                    OperationOutcome outcome) {

        return startRequestAsync(params, attempt, outcome);
    }

    /**
     * Performs a POST.
     */
    @Override
    protected Future<Response> startRequestAsync(InvocationCallback<Response> callback, String path,
                    Entity<SdncRequest> entity, Map<String, Object> headers) {

        return getClient().post(callback, path, entity, headers);
    }

    /**
     * Checks that the response has an "output" and that the output indicates success.
     */
    @Override
    protected boolean isSuccess(Response rawResponse, SdncResponse response) {
        return response.getResponseOutput() != null && "200".equals(response.getResponseOutput().getResponseCode());
    }

    @Override
    protected Future<Response> startQueryAsync(InvocationCallback<Response> callback, String path,
                    Map<String, Object> headers) {
        throw new UnsupportedOperationException("queries");
    }
}
