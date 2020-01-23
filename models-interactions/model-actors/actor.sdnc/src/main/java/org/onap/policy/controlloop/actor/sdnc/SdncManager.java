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
import java.util.concurrent.ForkJoinPool;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.Getter;
import org.onap.policy.aai.ControlLoopAaiData;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.spi.OperationManagerPartial;
import org.onap.policy.controlloop.actorserviceprovider.spi.ParameterTranslator;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.sdnc.SdncRequest;
import org.onap.policy.sdnc.SdncResponse;
import org.onap.policy.sdnc.SdncResponseOutput;
import org.onap.policy.sdnc.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for SDNC Operation Managers.
 */
public abstract class SdncManager extends OperationManagerPartial {
    private static final Logger logger = LoggerFactory.getLogger(SdncManager.class);

    @Getter(AccessLevel.PROTECTED)
    private HttpClient client;

    /**
     * URI path for this particular operation.
     */
    private String path;

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this manager is associated
     * @param name operation name
     */
    public SdncManager(String actorName, String name) {
        super(actorName, name);
    }

    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        HttpParams params = ParameterTranslator.translate(getFullName(), parameters, HttpParams.class);
        ValidationResult result = params.validate(getFullName());
        if (!result.isValid()) {
            throw new IllegalArgumentException(result.getResult());
        }

        client = HttpClientFactoryInstance.getClientFactory().get(params.getClientName());
        path = params.getPath();
    }

    @Override
    public CompletableFuture<PolicyResult> start(ControlLoopAaiData aaiData, VirtualControlLoopEvent onset,
                    Policy policy, String subRequestId) {

        final CompletableFuture<PolicyResult> future = new CompletableFuture<>();

        SdncRequest request = constructRequest(onset);
        if (request == null) {
            future.complete(PolicyResult.FAILURE);
            return future;
        }

        // do the request in the background
        ForkJoinPool.commonPool().execute(() -> doRequest(future, request));

        return future;
    }

    /**
     * Constructs the request.
     *
     * @param onset event for which the request should be constructed
     * @return a new request
     */
    protected abstract SdncRequest constructRequest(VirtualControlLoopEvent onset);

    /**
     * Posts the request and retrieves the response.
     *
     * @param future future to be completed when the request completes
     * @param sdncRequest request to be posted
     */
    private void doRequest(CompletableFuture<PolicyResult> future, SdncRequest sdncRequest) {
        Map<String, Object> headers = new HashMap<>();

        SdncResponse responseError = new SdncResponse();
        SdncResponseOutput responseOutput = new SdncResponseOutput();
        responseOutput.setResponseCode("404");
        responseError.setResponseOutput(responseOutput);

        headers.put("Accept", "application/json");
        String sdncUrl = client.getBaseUrl();

        Response rawResponse;

        try {
            String sdncRequestJson = Serialization.gsonPretty.toJson(sdncRequest);
            NetLoggerUtil.log(EventType.OUT, CommInfrastructure.REST, sdncUrl, sdncRequestJson);
            logger.info("[OUT|{}|{}|]{}{}", CommInfrastructure.REST, sdncUrl, NetLoggerUtil.SYSTEM_LS, sdncRequestJson);

            Entity<SdncRequest> entity = Entity.entity(sdncRequest, MediaType.APPLICATION_JSON);

            rawResponse = client.post(path, entity, headers);

        } catch (RuntimeException e) {
            future.completeExceptionally(e);
            return;
        }

        try {
            String strResponse = HttpClient.getBody(rawResponse, String.class);
            SdncResponse response = Serialization.gsonPretty.fromJson(strResponse, SdncResponse.class);
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, sdncUrl, strResponse);
            logger.info("[IN|{}|{}|]{}{}", "Sdnc", sdncUrl, NetLoggerUtil.SYSTEM_LS, strResponse);
            String body = Serialization.gsonPretty.toJson(response);
            logger.info("Response to Sdnc Heal post:");
            logger.info(body);
            response.setRequestId(sdncRequest.getRequestId().toString());

            if (!"200".equals(response.getResponseOutput().getResponseCode())) {
                logger.info("Sdnc Heal Restcall failed with http error code {} {}", rawResponse.getStatus(),
                                strResponse);
                future.complete(PolicyResult.FAILURE);
                return;
            }

        } catch (RuntimeException e) {
            logger.info("Unknown error deserializing into SdncResponse");
            future.completeExceptionally(e);
            return;
        }

        future.complete(PolicyResult.SUCCESS);
    }
}
