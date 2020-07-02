/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications copyright (c) 2018 Nokia
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onap.policy.appclcm.AppcLcmBody;
import org.onap.policy.appclcm.AppcLcmCommonHeader;
import org.onap.policy.appclcm.AppcLcmDmaapWrapper;
import org.onap.policy.appclcm.AppcLcmInput;
import org.onap.policy.appclcm.AppcLcmOutput;
import org.onap.policy.appclcm.AppcLcmResponseCode;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actor.appc.AppcOperation;
import org.onap.policy.controlloop.actor.appc.ModifyConfigOperation;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicActor;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicActorParams;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppcLcmActor extends BidirectionalTopicActor<BidirectionalTopicActorParams> {

    /*
     * Confirmed by Daniel, should be 'APPC'.
     * The actor name defined in the yaml for both legacy operations and lcm operations is still “APPC”. Perhaps in a
     * future review it would be better to distinguish them as two separate actors in the yaml but it should be okay for
     * now.
     */
    public static final String NAME = "APPC";

    private static final Logger logger = LoggerFactory.getLogger(AppcLcmActor.class);

    /* To be used in future releases to restart a single vm */
    private static final String APPC_VM_ID = "vm-id";

    // Strings for targets
    private static final String TARGET_VM = "VM";
    private static final String TARGET_VNF = "VNF";

    // Strings for recipes
    private static final String RECIPE_RESTART = AppcLcmConstants.OPERATION_RESTART;
    private static final String RECIPE_REBUILD = AppcLcmConstants.OPERATION_REBUILD;
    private static final String RECIPE_MIGRATE = AppcLcmConstants.OPERATION_MIGRATE;
    private static final String RECIPE_MODIFY = AppcLcmConstants.OPERATION_CONFIG_MODIFY;

    /* To be used in future releases when LCM ConfigModify is used */
    private static final String APPC_REQUEST_PARAMS = "request-parameters";
    private static final String APPC_CONFIG_PARAMS = "configuration-parameters";

    private static final Set<String> recipes = AppcLcmConstants.OPERATION_NAMES;
    private static final ImmutableMap<String, List<String>> targets = new ImmutableMap.Builder<String, List<String>>()
            .put(RECIPE_RESTART, ImmutableList.of(TARGET_VM)).put(RECIPE_REBUILD, ImmutableList.of(TARGET_VM))
            .put(RECIPE_MIGRATE, ImmutableList.of(TARGET_VM)).put(RECIPE_MODIFY, ImmutableList.of(TARGET_VNF)).build();
    private static final ImmutableMap<String, List<String>> payloads =
            new ImmutableMap.Builder<String, List<String>>().put(RECIPE_RESTART, ImmutableList.of(APPC_VM_ID))
                    .put(RECIPE_MODIFY, ImmutableList.of(APPC_REQUEST_PARAMS, APPC_CONFIG_PARAMS)).build();

    /**
     * Constructs the object.
     */
    public AppcLcmActor() {
        super(NAME, BidirectionalTopicActorParams.class);

        // add LCM operations first as they take precedence
        for (String opname : AppcLcmConstants.OPERATION_NAMES) {
            addOperator(new BidirectionalTopicOperator(NAME, opname, this, AppcLcmOperation.SELECTOR_KEYS,
                            AppcLcmOperation::new));
        }

        // add legacy operations
        addOperator(new BidirectionalTopicOperator(NAME, ModifyConfigOperation.NAME, this, AppcOperation.SELECTOR_KEYS,
                        ModifyConfigOperation::new));
    }

    /**
     * This actor should take precedence.
     */
    @Override
    public int getSequenceNumber() {
        return -1;
    }

    @Override
    public String actor() {
        return NAME;
    }

    @Override
    public List<String> recipes() {
        return ImmutableList.copyOf(recipes);
    }

    @Override
    public List<String> recipeTargets(String recipe) {
        return ImmutableList.copyOf(targets.getOrDefault(recipe, Collections.emptyList()));
    }

    @Override
    public List<String> recipePayloads(String recipe) {
        return ImmutableList.copyOf(payloads.getOrDefault(recipe, Collections.emptyList()));
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
     * Parses the operation attempt using the subRequestId of APPC response.
     *
     * @param subRequestId the sub id used to send to APPC, Policy sets this using the operation
     *        attempt
     *
     * @return the current operation attempt
     */
    public static Integer parseOperationAttempt(String subRequestId) {
        Integer operationAttempt;
        try {
            operationAttempt = Integer.parseInt(subRequestId);
        } catch (NumberFormatException e) {
            logger.debug("A NumberFormatException was thrown due to error in parsing the operation attempt");
            return null;
        }
        return operationAttempt;
    }

    /**
     * Processes the APPC LCM response sent from APPC. Determines if the APPC operation was
     * successful/unsuccessful and maps this to the corresponding Policy result.
     *
     * @param dmaapResponse the dmaap wrapper message that contains the actual APPC reponse inside
     *        the body field
     *
     * @return an key-value pair that contains the Policy result and APPC response message
     */
    public static SimpleEntry<PolicyResult, String> processResponse(AppcLcmDmaapWrapper dmaapResponse) {
        AppcLcmBody appcBody = dmaapResponse.getBody();
        if (appcBody == null) {
            throw new NullPointerException("APPC Body is null");
        }

        /* The actual APPC response is inside the dmaap wrapper's body.input field. */
        AppcLcmOutput appcResponse = appcBody.getOutput();
        if (appcResponse == null) {
            throw new NullPointerException("APPC Response is null");
        }

        /* The message returned in the APPC response. */
        String message;

        /* The Policy result determined from the APPC Response. */
        PolicyResult result;

        /* If there is no status, Policy cannot determine if the request was successful. */
        if (appcResponse.getStatus() == null) {
            message = "Policy was unable to parse APP-C response status field (it was null).";
            return new AbstractMap.SimpleEntry<>(PolicyResult.FAILURE_EXCEPTION, message);
        }

        /* If there is no code, Policy cannot determine if the request was successful. */
        String responseValue = AppcLcmResponseCode.toResponseValue(appcResponse.getStatus().getCode());
        if (responseValue == null) {
            message = "Policy was unable to parse APP-C response status code field.";
            return new AbstractMap.SimpleEntry<>(PolicyResult.FAILURE_EXCEPTION, message);
        }

        /* Save the APPC response's message for Policy notification message. */
        message = appcResponse.getStatus().getMessage();

        /* Maps the APPC response result to a Policy result. */
        switch (responseValue) {
            case AppcLcmResponseCode.ACCEPTED:
                /* Nothing to do if code is accept, continue processing */
                result = null;
                break;
            case AppcLcmResponseCode.SUCCESS:
                result = PolicyResult.SUCCESS;
                break;
            case AppcLcmResponseCode.FAILURE:
                result = PolicyResult.FAILURE;
                break;
            case AppcLcmResponseCode.REJECT:
            case AppcLcmResponseCode.ERROR:
            default:
                result = PolicyResult.FAILURE_EXCEPTION;
        }
        return new AbstractMap.SimpleEntry<>(result, message);
    }
}
