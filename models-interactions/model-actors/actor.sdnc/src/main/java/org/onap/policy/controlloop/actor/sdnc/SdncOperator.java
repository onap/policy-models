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
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.Getter;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.impl.OperatorPartial;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.sdnc.SdncRequest;
import org.onap.policy.sdnc.SdncResponse;
import org.onap.policy.sdnc.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for SDNC Operators.
 */
public abstract class SdncOperator extends OperatorPartial {
    private static final Logger logger = LoggerFactory.getLogger(SdncOperator.class);

    @Getter(AccessLevel.PROTECTED)
    private HttpClient client;

    /**
     * URI path for this particular operation.
     */
    private String path;

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     */
    public SdncOperator(String actorName, String name) {
        super(actorName, name);
    }

    // TODO add a junit for this and for plug-in via ActorService
    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        HttpParams params = Util.translate(getFullName(), parameters, HttpParams.class);
        ValidationResult result = params.validate(getFullName());
        if (!result.isValid()) {
            throw new ParameterValidationRuntimeException("invalid parameters", result);
        }

        client = HttpClientFactoryInstance.getClientFactory().get(params.getClientName());
        path = params.getPath();
    }

    @Override
    protected ControlLoopOperation doOperation(ControlLoopOperationParams params, int attempt,
                    ControlLoopOperation operation) {

        SdncRequest request = constructRequest(params.getContext().getEvent());
        PolicyResult result = doRequest(request);

        return setOutcome(params, operation, result);
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
     * @param sdncRequest request to be posted
     * @return the result of the request
     */
    private PolicyResult doRequest(SdncRequest sdncRequest) {
        Map<String, Object> headers = new HashMap<>();

        headers.put("Accept", "application/json");
        String sdncUrl = client.getBaseUrl();

        String sdncRequestJson = Serialization.gsonPretty.toJson(sdncRequest);

        // TODO move this into a utility
        NetLoggerUtil.log(EventType.OUT, CommInfrastructure.REST, sdncUrl, sdncRequestJson);
        logger.info("[OUT|{}|{}|]{}{}", CommInfrastructure.REST, sdncUrl, NetLoggerUtil.SYSTEM_LS, sdncRequestJson);

        Entity<SdncRequest> entity = Entity.entity(sdncRequest, MediaType.APPLICATION_JSON);

        // TODO modify this to use asynchronous client operations
        Response rawResponse = client.post(path, entity, headers);
        String strResponse = HttpClient.getBody(rawResponse, String.class);

        // TODO move this into a utility
        NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, sdncUrl, strResponse);
        logger.info("[IN|{}|{}|]{}{}", "Sdnc", sdncUrl, NetLoggerUtil.SYSTEM_LS, strResponse);
        logger.info("Response to Sdnc Heal post:");
        logger.info(strResponse);

        SdncResponse response = Serialization.gsonPretty.fromJson(strResponse, SdncResponse.class);

        if (response.getResponseOutput() == null || !"200".equals(response.getResponseOutput().getResponseCode())) {
            logger.info("Sdnc Heal Restcall failed with http error code {}", rawResponse.getStatus());
            return PolicyResult.FAILURE;
        }

        return PolicyResult.SUCCESS;
    }
}
