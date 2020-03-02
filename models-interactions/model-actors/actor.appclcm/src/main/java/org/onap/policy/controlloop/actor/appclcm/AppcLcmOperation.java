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
import org.onap.policy.appclcm.util.StatusCodeEnum;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AppcLcmOperation extends BidirectionalTopicOperation<AppcLcmDmaapWrapper, AppcLcmDmaapWrapper> {

    // Strings for recipes
    private static final String RECIPE_RESTART = "Restart";
    private static final String RECIPE_REBUILD = "Rebuild";
    private static final String RECIPE_MIGRATE = "Migrate";
    private static final String RECIPE_MODIFY = "ConfigModify";

    private static final Logger logger = LoggerFactory.getLogger(AppcLcmOperation.class);
    private static final StandardCoder coder = new StandardCoder();
    public static final String VNF_ID_KEY = "generic-vnf.vnf-id";

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
        AppcLcmInput request = new AppcLcmInput();
        request.setCommonHeader(new AppcLcmCommonHeader());
        request.getCommonHeader().setRequestId(params.getRequestId());
        request.getCommonHeader().setSubRequestId(UUID.randomUUID().toString());
        request.setAction(getName());

        // convert payload strings to objects
        if (params.getPayload() == null) {
            logger.info("{}: no payload specified for {}", getFullName(), params.getRequestId());
        } else {
            convertPayload(params.getPayload(), request.getActionIdentifiers());
        }

        // add/replace specific values
        if (request.getActionIdentifiers() != null) {
            request.getActionIdentifiers().put(VNF_ID_KEY, targetVnf);
        }

        AppcLcmDmaapWrapper dmaapRequest = new AppcLcmDmaapWrapper();
        AppcLcmBody body = new AppcLcmBody();

        body.setInput(request);
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
    private static String convertPayload(Map<String, Object> source, Map<String, String> map) {
        String encodedPayloadString = null;
        try {
            encodedPayloadString = coder.encode(source);
        } catch (CoderException e) {
            logger.error("Cannot convert payload. Error encoding source as a string.", e);
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

        StatusCodeEnum code = StatusCodeEnum.fromStatusCode(response.getBody().getOutput().getStatus().getCode());

        if (code == null) {
            throw new IllegalArgumentException(
                    "unknown APPC-LCM response status code: " + response.getBody().getOutput().getStatus().getCode());
        }

        logger.info("APPC-LCM Response Code {} Message is {}", code, response.getBody().getOutput().getStatus());
        logger.info("APPC-LCM Response Payload is {}", response.getBody().getOutput().getPayload());

        switch (code) {
            case SUCCESS:
                return Status.SUCCESS;
            case FAILURE:
                return Status.FAILURE;
            case ERROR:
            case REJECT:
                throw new IllegalArgumentException("APPC-LCM request was not accepted, code=" + code);
            case ACCEPTED:
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

    private static boolean payloadSupplied(Map<String, String> payload) {
        return payload != null && !payload.isEmpty();
    }

    private static boolean recipeSupportsPayload(String recipe) {
        return !RECIPE_RESTART.equalsIgnoreCase(recipe) && !RECIPE_REBUILD.equalsIgnoreCase(recipe)
                && !RECIPE_MIGRATE.equalsIgnoreCase(recipe);
    }

    private static String parsePayload(Map<String, String> payload) {
        StringBuilder payloadString = new StringBuilder("{");
        payload.forEach(
            (key, value) -> payloadString.append("\"").append(key).append("\": ").append(value).append(","));
        return payloadString.substring(0, payloadString.length() - 1) + "}";
    }

    /**
     * Constructs an APPC request conforming to the lcm API. The actual request is constructed and
     * then placed in a wrapper object used to send through DMAAP.
     *
     * @param onset the event that is reporting the alert for policy to perform an action
     * @param operation the control loop operation specifying the actor, operation, target, etc.
     * @param policy the policy the was specified from the yaml generated by CLAMP or through the
     *        Policy GUI/API
     * @return an APPC request conforming to the lcm API using the DMAAP wrapper
     */
    public static AppcLcmDmaapWrapper constructRequest(VirtualControlLoopEvent onset, ControlLoopOperation operation,
            Policy policy, String targetVnf) {

        /* Construct an APPC request using LCM Model */

        /*
         * The actual LCM request is placed in a wrapper used to send through dmaap. The current
         * version is 2.0 as of R1.
         */
        AppcLcmRecipeFormatter lcmRecipeFormatter = new AppcLcmRecipeFormatter(policy.getRecipe());

        AppcLcmDmaapWrapper dmaapRequest = new AppcLcmDmaapWrapper();
        dmaapRequest.setVersion("2.0");
        dmaapRequest.setCorrelationId(onset.getRequestId() + "-" + operation.getSubRequestId());
        dmaapRequest.setRpcName(lcmRecipeFormatter.getUrlRecipe());
        dmaapRequest.setType("request");

        /* This is the actual request that is placed in the dmaap wrapper. */
        final AppcLcmInput appcRequest = new AppcLcmInput();

        /* The common header is a required field for all APPC requests. */
        AppcLcmCommonHeader requestCommonHeader = new AppcLcmCommonHeader();
        requestCommonHeader.setOriginatorId(onset.getRequestId().toString());
        requestCommonHeader.setRequestId(onset.getRequestId());
        requestCommonHeader.setSubRequestId(operation.getSubRequestId());

        appcRequest.setCommonHeader(requestCommonHeader);

        /*
         * Action Identifiers are required for APPC LCM requests. For R1, the recipes supported by
         * Policy only require a vnf-id.
         */
        HashMap<String, String> requestActionIdentifiers = new HashMap<>();
        requestActionIdentifiers.put("vnf-id", targetVnf);

        appcRequest.setActionIdentifiers(requestActionIdentifiers);

        /*
         * An action is required for all APPC requests, this will be the recipe specified in the
         * policy.
         */
        appcRequest.setAction(lcmRecipeFormatter.getBodyRecipe());

        /*
         * For R1, the payloads will not be required for the Restart, Rebuild, or Migrate recipes.
         * APPC will populate the payload based on A&AI look up of the vnd-id provided in the action
         * identifiers.
         */
        if (recipeSupportsPayload(policy.getRecipe()) && payloadSupplied(policy.getPayload())) {
            appcRequest.setPayload(parsePayload(policy.getPayload()));
        } else {
            appcRequest.setPayload(null);
        }

        /*
         * The APPC request must be wrapped in an input object.
         */
        AppcLcmBody body = new AppcLcmBody();
        body.setInput(appcRequest);

        /*
         * Once the LCM request is constructed, add it into the body of the dmaap wrapper.
         */
        dmaapRequest.setBody(body);

        /* Return the request to be sent through dmaap. */
        return dmaapRequest;
    }
}
