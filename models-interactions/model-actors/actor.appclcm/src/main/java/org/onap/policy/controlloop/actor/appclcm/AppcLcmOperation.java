/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

package org.onap.policy.controlloop.actor.appclcm;

import java.util.List;
import java.util.Map;
import org.onap.policy.appclcm.AppcLcmBody;
import org.onap.policy.appclcm.AppcLcmCommonHeader;
import org.onap.policy.appclcm.AppcLcmInput;
import org.onap.policy.appclcm.AppcLcmMessageWrapper;
import org.onap.policy.appclcm.AppcLcmOutput;
import org.onap.policy.appclcm.AppcLcmResponseCode;
import org.onap.policy.appclcm.AppcLcmResponseStatus;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;

public class AppcLcmOperation extends BidirectionalTopicOperation<AppcLcmMessageWrapper, AppcLcmMessageWrapper> {

    private static final String MISSING_STATUS = "APPC-LCM response is missing the response status";
    public static final String VNF_ID_KEY = "vnf-id";

    private static final List<String> PROPERTY_NAMES = List.of(OperationProperties.AAI_TARGET_ENTITY);

    /**
     * Keys used to match the response with the request listener. The sub request ID is a
     * UUID, so it can be used to uniquely identify the response.
     * <p/>
     * Note: if these change, then {@link #getExpectedKeyValues(int, AppcLcmMessageWrapper)}
     * must be updated accordingly.
     */
    public static final List<SelectorKey> SELECTOR_KEYS =
                    List.of(new SelectorKey("body", "output", "common-header", "sub-request-id"));

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public AppcLcmOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
        super(params, config, AppcLcmMessageWrapper.class, PROPERTY_NAMES);
    }

    @Override
    protected AppcLcmMessageWrapper makeRequest(int attempt) {
        String subRequestId = getSubRequestId();

        var header = new AppcLcmCommonHeader();
        header.setOriginatorId(params.getRequestId().toString());
        header.setRequestId(params.getRequestId());
        header.setSubRequestId(subRequestId);

        var inputRequest = new AppcLcmInput();
        inputRequest.setCommonHeader(header);

        var recipeFormatter = new AppcLcmRecipeFormatter(getName());
        inputRequest.setAction(recipeFormatter.getBodyRecipe());

        /*
         * Action Identifiers are required for APPC LCM requests. For R1, the recipes
         * supported by Policy only require a vnf-id.
         */
        String target = getRequiredProperty(OperationProperties.AAI_TARGET_ENTITY, "target entity");
        inputRequest.setActionIdentifiers(Map.of(VNF_ID_KEY, target));

        /*
         * For R1, the payloads will not be required for the Restart, Rebuild, or Migrate
         * recipes. APPC will populate the payload based on A&AI look up of the vnd-id
         * provided in the action identifiers. The payload is set when convertPayload() is
         * called.
         */
        if (operationSupportsPayload()) {
            convertPayload(params.getPayload(), inputRequest);
        } else {
            inputRequest.setPayload(null);
        }

        var body = new AppcLcmBody();
        body.setInput(inputRequest);

        var messageRequest = new AppcLcmMessageWrapper();
        messageRequest.setBody(body);
        messageRequest.setVersion("2.0");
        messageRequest.setCorrelationId(params.getRequestId() + "-" + subRequestId);
        messageRequest.setRpcName(recipeFormatter.getUrlRecipe());
        messageRequest.setType("request");

        body.setInput(inputRequest);
        messageRequest.setBody(body);
        return messageRequest;
    }

    /**
     * Converts a payload. The original value is assumed to be a JSON string, which is
     * decoded into an object.
     *
     * @param source source from which to get the values
     * @param request where to place the decoded values
     */
    private void convertPayload(Map<String, Object> source, AppcLcmInput request) {
        try {
            var encodedPayloadString = getCoder().encode(source);
            request.setPayload(encodedPayloadString);
        } catch (CoderException e) {
            throw new IllegalArgumentException("Cannot convert payload", e);
        }
    }

    /**
     * Note: these values must match {@link #SELECTOR_KEYS}.
     */
    @Override
    protected List<String> getExpectedKeyValues(int attempt, AppcLcmMessageWrapper request) {
        return List.of(getSubRequestId());
    }

    @Override
    protected Status detmStatus(String rawResponse, AppcLcmMessageWrapper response) {
        AppcLcmResponseStatus status = getStatus(response);
        if (status == null) {
            throw new IllegalArgumentException(MISSING_STATUS);
        }

        String code = AppcLcmResponseCode.toResponseValue(status.getCode());
        if (code == null) {
            throw new IllegalArgumentException("unknown APPC-LCM response status code: " + status.getCode());
        }

        return switch (code) {
            case AppcLcmResponseCode.SUCCESS -> Status.SUCCESS;
            case AppcLcmResponseCode.FAILURE -> Status.FAILURE;
            case AppcLcmResponseCode.ERROR, AppcLcmResponseCode.REJECT ->
                throw new IllegalArgumentException("APPC-LCM request was not accepted, code=" + code);
            default -> Status.STILL_WAITING;
        };
    }

    /**
     * Sets the message to the status description, if available.
     */
    @Override
    public OperationOutcome setOutcome(OperationOutcome outcome, OperationResult result,
                                       AppcLcmMessageWrapper response) {
        outcome.setResponse(response);

        AppcLcmResponseStatus status = getStatus(response);
        if (status == null) {
            return setOutcome(outcome, result);
        }

        String message = status.getMessage();
        if (message == null) {
            return setOutcome(outcome, result);
        }

        outcome.setResult(result);
        outcome.setMessage(message);
        return outcome;
    }

    /**
     * Gets the status from the response.
     *
     * @param response the response from which to extract the status, or {@code null}
     * @return the status, or {@code null} if it does not exist
     */
    protected AppcLcmResponseStatus getStatus(AppcLcmMessageWrapper response) {
        if (response == null) {
            return null;
        }

        AppcLcmBody body = response.getBody();
        if (body == null) {
            return null;
        }

        AppcLcmOutput output = body.getOutput();
        if (output == null) {
            return null;
        }

        return output.getStatus();
    }

    /**
     * Determines if the operation supports a payload.
     *
     * @return {@code true} if the operation supports a payload, {@code false} otherwise
     */
    protected boolean operationSupportsPayload() {
        return params.getPayload() != null && !params.getPayload().isEmpty()
                        && AppcLcmConstants.SUPPORTS_PAYLOAD.contains(params.getOperation().toLowerCase());
    }
}
