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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.actorserviceprovider.AsyncResponseHandler;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.sdnc.SdncRequest;
import org.onap.policy.sdnc.SdncResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for SDNC Operators.
 */
public abstract class SdncOperator extends HttpOperator {
    private static final Logger logger = LoggerFactory.getLogger(SdncOperator.class);

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     */
    public SdncOperator(String actorName, String name) {
        super(actorName, name);
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(ControlLoopOperationParams params, int attempt,
                    OperationOutcome outcome) {

        SdncRequest request = constructRequest(params.getContext());
        return postRequest(params, outcome, request);
    }

    /**
     * Constructs the request.
     *
     * @param context associated event context
     * @return a new request
     */
    protected abstract SdncRequest constructRequest(ControlLoopEventContext context);

    /**
     * Posts the request and and arranges to retrieve the response.
     *
     * @param params operation parameters
     * @param outcome updated with the response
     * @param sdncRequest request to be posted
     * @return the result of the request
     */
    private CompletableFuture<OperationOutcome> postRequest(ControlLoopOperationParams params, OperationOutcome outcome,
                    SdncRequest sdncRequest) {
        Map<String, Object> headers = new HashMap<>();

        headers.put("Accept", "application/json");
        String sdncUrl = getClient().getBaseUrl();

        Util.logRestRequest(sdncUrl, sdncRequest);

        Entity<SdncRequest> entity = Entity.entity(sdncRequest, MediaType.APPLICATION_JSON);

        ResponseHandler handler = new ResponseHandler(params, outcome, sdncUrl);
        return handler.handle(getClient().post(handler, getPath(), entity, headers));
    }

    private class ResponseHandler extends AsyncResponseHandler<Response> {
        private final String sdncUrl;

        public ResponseHandler(ControlLoopOperationParams params, OperationOutcome outcome, String sdncUrl) {
            super(params, outcome);
            this.sdncUrl = sdncUrl;
        }

        /**
         * Handles the response.
         */
        @Override
        protected OperationOutcome doComplete(Response rawResponse) {
            String strResponse = HttpClient.getBody(rawResponse, String.class);

            Util.logRestResponse(sdncUrl, strResponse);

            SdncResponse response;
            try {
                response = makeDecoder().decode(strResponse, SdncResponse.class);
            } catch (CoderException e) {
                logger.warn("Sdnc Heal cannot decode response with http error code {}", rawResponse.getStatus(), e);
                return SdncOperator.this.setOutcome(getParams(), getOutcome(), PolicyResult.FAILURE_EXCEPTION);
            }

            if (response.getResponseOutput() != null && "200".equals(response.getResponseOutput().getResponseCode())) {
                return SdncOperator.this.setOutcome(getParams(), getOutcome(), PolicyResult.SUCCESS);

            } else {
                logger.info("Sdnc Heal Restcall failed with http error code {}", rawResponse.getStatus());
                return SdncOperator.this.setOutcome(getParams(), getOutcome(), PolicyResult.FAILURE);
            }
        }

        /**
         * Handles exceptions.
         */
        @Override
        protected OperationOutcome doFailed(Throwable thrown) {
            logger.info("Sdnc Heal Restcall threw an exception", thrown);
            return SdncOperator.this.setOutcome(getParams(), getOutcome(), PolicyResult.FAILURE_EXCEPTION);
        }
    }

    // these may be overridden by junit tests

    protected StandardCoder makeDecoder() {
        return new StandardCoder();
    }
}
