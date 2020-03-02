/*-
 * ============LICENSE_START=======================================================
 * AppcLcmOperation
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

package org.onap.policy.controlloop.actor.appclcm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.onap.policy.appclcm.AppcLcmBody;
import org.onap.policy.appclcm.AppcLcmCommonHeader;
import org.onap.policy.appclcm.AppcLcmDmaapWrapper;
import org.onap.policy.appclcm.AppcLcmInput;
import org.onap.policy.appclcm.AppcLcmResponseCode;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AppcLcmOperation extends BidirectionalTopicOperation<AppcLcmDmaapWrapper, AppcLcmDmaapWrapper> {

    private static final Logger logger = LoggerFactory.getLogger(AppcLcmOperation.class);
    private static final StandardCoder coder = new StandardCoder();
    public static final String VNF_ID_KEY = "vnf-id";

    /**
     * Keys used to match the response with the request listener. The sub request ID is a
     * UUID, so it can be used to uniquely identify the response.
     * <p/>
     * Note: if these change, then {@link #getExpectedKeyValues(int, Request)} must be
     * updated accordingly.
     */
    public static final List<SelectorKey> SELECTOR_KEYS = List.of(new SelectorKey("common-header", "sub-request-id"));

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public AppcLcmOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
        super(params, config, AppcLcmDmaapWrapper.class);
    }

    /**
     * Starts the GUARD.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        return startGuardAsync();
    }

    /**
     * Makes a request, given the target VNF. This is a support function for
     * {@link #makeRequest(int)}.
     *
     * @param attempt attempt number
     * @param targetVnf target VNF
     * @return a new request
     */
    protected AppcLcmDmaapWrapper makeRequest(int attempt, String targetVnf) {
        VirtualControlLoopEvent onset = params.getContext().getEvent();
        String subRequestId = UUID.randomUUID().toString();

        AppcLcmCommonHeader header = new AppcLcmCommonHeader();
        header.setOriginatorId(onset.getRequestId().toString());
        header.setRequestId(onset.getRequestId());
        header.setSubRequestId(subRequestId);

        AppcLcmInput inputRequest = new AppcLcmInput();
        inputRequest.setCommonHeader(header);
        inputRequest.setAction(getName());

        /*
         * Action Identifiers are required for APPC LCM requests. For R1, the recipes supported by
         * Policy only require a vnf-id.
         */
        if (inputRequest.getActionIdentifiers() != null) {
            inputRequest.getActionIdentifiers().put(VNF_ID_KEY, targetVnf);
        } else {
            HashMap<String, String> requestActionIdentifiers = new HashMap<>();
            requestActionIdentifiers.put(VNF_ID_KEY, targetVnf);
            inputRequest.setActionIdentifiers(requestActionIdentifiers);
        }

        /*
         * For R1, the payloads will not be required for the Restart, Rebuild, or Migrate recipes.
         * APPC will populate the payload based on A&AI look up of the vnd-id provided in the action
         * identifiers. The payload is set when converPayload() is called.
         */
        if (operationSupportsPayload()) {
            convertPayload(params.getPayload(), inputRequest);
        } else {
            inputRequest.setPayload(null);
        }

        AppcLcmBody body = new AppcLcmBody();
        body.setInput(inputRequest);

        AppcLcmRecipeFormatter recipeFormatter = new AppcLcmRecipeFormatter(getName());
        inputRequest.setAction(recipeFormatter.getBodyRecipe());

        AppcLcmDmaapWrapper dmaapRequest = new AppcLcmDmaapWrapper();
        dmaapRequest.setBody(body);
        dmaapRequest.setVersion("2.0");
        dmaapRequest.setCorrelationId(onset.getRequestId() + "-" + subRequestId);
        dmaapRequest.setRpcName(recipeFormatter.getUrlRecipe());
        dmaapRequest.setType("request");

        body.setInput(inputRequest);
        dmaapRequest.setBody(body);
        return dmaapRequest;
    }

    /**
     * Converts a payload. The original value is assumed to be a JSON string, which is
     * decoded into an object.
     *
     * @param source source from which to get the values
     * @param map where to place the decoded values
     */
    private static String convertPayload(Map<String, Object> source, AppcLcmInput request) {
        String encodedPayloadString = null;
        try {
            encodedPayloadString = coder.encode(source);
            request.setPayload(encodedPayloadString);
        } catch (CoderException e) {
            logger.error("Cannot convert payload. Error encoding source as a string.", e);
            throw new IllegalArgumentException("Cannot convert payload. Error encoding source as a string.");
        }
        return encodedPayloadString;
    }

    /**
     * Note: these values must match {@link #SELECTOR_KEYS}.
     */
    @Override
    protected List<String> getExpectedKeyValues(int attempt, AppcLcmDmaapWrapper request) {
        return List.of(request.getBody().getInput().getCommonHeader().getSubRequestId());
    }

    @Override
    protected Status detmStatus(String rawResponse, AppcLcmDmaapWrapper response) {
        if (response == null || response.getBody() == null || response.getBody().getOutput() == null
                || response.getBody().getOutput().getStatus() == null) {
            throw new IllegalArgumentException("APPC-LCM response is missing the response status");
        }

        String code = AppcLcmResponseCode.toResponseValue(response.getBody().getOutput().getStatus().getCode());

        if (code == null) {
            throw new IllegalArgumentException(
                    "unknown APPC-LCM response status code: " + response.getBody().getOutput().getStatus().getCode());
        }

        switch (code) {
            case AppcLcmResponseCode.SUCCESS:
                return Status.SUCCESS;
            case AppcLcmResponseCode.FAILURE:
                return Status.FAILURE;
            case AppcLcmResponseCode.ERROR:
            case AppcLcmResponseCode.REJECT:
                throw new IllegalArgumentException("APPC-LCM request was not accepted, code=" + code);
            case AppcLcmResponseCode.ACCEPTED:
            default:
                return Status.STILL_WAITING;
        }
    }

    /**
     * Sets the message to the status description, if available.
     */
    @Override
    public OperationOutcome setOutcome(OperationOutcome outcome, PolicyResult result, AppcLcmDmaapWrapper response) {
        if (response == null || response.getBody() == null || response.getBody().getOutput() == null
                || response.getBody().getOutput().getStatus() == null
                || response.getBody().getOutput().getStatus().getMessage() == null) {
            return setOutcome(outcome, result);
        }

        outcome.setResult(result);
        outcome.setMessage(response.getBody().getOutput().getStatus().getMessage());
        return outcome;
    }

    protected abstract boolean recipeSupportsPayload(String recipe);

    protected abstract boolean operationSupportsPayload();
}
