/*-
 * ============LICENSE_START=======================================================
 * AppcLcmActorServiceProvider
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
import java.util.UUID;

import org.onap.policy.aai.AaiManager;
import org.onap.policy.aai.AaiNqInstanceFilters;
import org.onap.policy.aai.AaiNqInventoryResponseItem;
import org.onap.policy.aai.AaiNqNamedQuery;
import org.onap.policy.aai.AaiNqQueryParameters;
import org.onap.policy.aai.AaiNqRequest;
import org.onap.policy.aai.AaiNqResponse;
import org.onap.policy.aai.util.AaiException;
import org.onap.policy.appclcm.LcmCommonHeader;
import org.onap.policy.appclcm.LcmRequest;
import org.onap.policy.appclcm.LcmRequestWrapper;
import org.onap.policy.appclcm.LcmResponse;
import org.onap.policy.appclcm.LcmResponseCode;
import org.onap.policy.appclcm.LcmResponseWrapper;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.rest.RestManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppcLcmActorServiceProvider implements Actor {

    private static final Logger logger = LoggerFactory.getLogger(AppcLcmActorServiceProvider.class);

    /* To be used in future releases to restart a single vm */
    private static final String APPC_VM_ID = "vm-id";

    // Strings for targets
    private static final String TARGET_VM = "VM";
    private static final String TARGET_VNF = "VNF";

    // Strings for recipes
    private static final String RECIPE_RESTART = "Restart";
    private static final String RECIPE_REBUILD = "Rebuild";
    private static final String RECIPE_MIGRATE = "Migrate";
    private static final String RECIPE_MODIFY = "ConfigModify";

    /* To be used in future releases when LCM ConfigModify is used */
    private static final String APPC_REQUEST_PARAMS = "request-parameters";
    private static final String APPC_CONFIG_PARAMS = "configuration-parameters";

    private static final ImmutableList<String> recipes =
            ImmutableList.of(RECIPE_RESTART, RECIPE_REBUILD, RECIPE_MIGRATE, RECIPE_MODIFY);
    private static final ImmutableMap<String, List<String>> targets = new ImmutableMap.Builder<String, List<String>>()
            .put(RECIPE_RESTART, ImmutableList.of(TARGET_VM)).put(RECIPE_REBUILD, ImmutableList.of(TARGET_VM))
            .put(RECIPE_MIGRATE, ImmutableList.of(TARGET_VM)).put(RECIPE_MODIFY, ImmutableList.of(TARGET_VNF)).build();
    private static final ImmutableMap<String, List<String>> payloads =
            new ImmutableMap.Builder<String, List<String>>().put(RECIPE_RESTART, ImmutableList.of(APPC_VM_ID))
                    .put(RECIPE_MODIFY, ImmutableList.of(APPC_REQUEST_PARAMS, APPC_CONFIG_PARAMS)).build();

    @Override
    public String actor() {
        return "APPC";
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
     * This method recursively traverses the A&AI named query response to find the generic-vnf
     * object that contains a model-invariant-id that matches the resourceId of the policy. Once
     * this match is found the generic-vnf object's vnf-id is returned.
     *
     * @param items the list of items related to the vnf returned by A&AI
     * @param resourceId the id of the target from the sdc catalog
     *
     * @return the vnf-id of the target vnf to act upon or null if not found
     */
    private static String parseAaiResponse(List<AaiNqInventoryResponseItem> items, String resourceId) {
        String vnfId = null;
        for (AaiNqInventoryResponseItem item : items) {
            if ((item.getGenericVnf() != null) && (item.getGenericVnf().getModelInvariantId() != null)
                    && (resourceId.equals(item.getGenericVnf().getModelInvariantId()))) {
                vnfId = item.getGenericVnf().getVnfId();
                break;
            } else {
                if ((item.getItems() != null) && (item.getItems().getInventoryResponseItems() != null)) {
                    vnfId = parseAaiResponse(item.getItems().getInventoryResponseItems(), resourceId);
                }
            }
        }
        return vnfId;
    }

    /**
     * Constructs an A&AI Named Query using a source vnf-id to determine the vnf-id of the target
     * entity specified in the policy to act upon.
     *
     * @param resourceId the id of the target from the sdc catalog
     *
     * @param sourceVnfId the vnf id of the source entity reporting the alert
     *
     * @return the target entities vnf id to act upon
     * @throws AaiException it an error occurs
     */
    public static String vnfNamedQuery(String resourceId, String sourceVnfId, String aaiUrl,
    		String aaiUser, String aaiPassword) throws AaiException {

        // TODO: This request id should not be hard coded in future releases
        UUID requestId = UUID.fromString("a93ac487-409c-4e8c-9e5f-334ae8f99087");

        AaiNqRequest aaiRequest = new AaiNqRequest();
        aaiRequest.setQueryParameters(new AaiNqQueryParameters());
        aaiRequest.getQueryParameters().setNamedQuery(new AaiNqNamedQuery());
        aaiRequest.getQueryParameters().getNamedQuery().setNamedQueryUuid(requestId);

        Map<String, Map<String, String>> filter = new HashMap<>();
        Map<String, String> filterItem = new HashMap<>();

        filterItem.put("vnf-id", sourceVnfId);
        filter.put("generic-vnf", filterItem);

        aaiRequest.setInstanceFilters(new AaiNqInstanceFilters());
        aaiRequest.getInstanceFilters().getInstanceFilter().add(filter);

        AaiNqResponse aaiResponse = new AaiManager(new RestManager()).postQuery(aaiUrl,
                aaiUser, aaiPassword, aaiRequest, requestId);

        if (aaiResponse == null) {
            throw new AaiException("The named query response was null");
        }

        String targetVnfId = parseAaiResponse(aaiResponse.getInventoryResponseItems(), resourceId);
        if (targetVnfId == null) {
            throw new AaiException("Target vnf-id could not be found");
        }

        return targetVnfId;
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
    public static LcmRequestWrapper constructRequest(VirtualControlLoopEvent onset, ControlLoopOperation operation,
            Policy policy, String targetVnf) {

        /* Construct an APPC request using LCM Model */

        /*
         * The actual LCM request is placed in a wrapper used to send through dmaap. The current
         * version is 2.0 as of R1.
         */
        AppcLcmRecipeFormatter lcmRecipeFormatter = new AppcLcmRecipeFormatter(policy.getRecipe());

        LcmRequestWrapper dmaapRequest = new LcmRequestWrapper();
        dmaapRequest.setVersion("2.0");
        dmaapRequest.setCorrelationId(onset.getRequestId() + "-" + operation.getSubRequestId());
        dmaapRequest.setRpcName(lcmRecipeFormatter.getUrlRecipe());
        dmaapRequest.setType("request");

        /* This is the actual request that is placed in the dmaap wrapper. */
        final LcmRequest appcRequest = new LcmRequest();

        /* The common header is a required field for all APPC requests. */
        LcmCommonHeader requestCommonHeader = new LcmCommonHeader();
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
         * Once the LCM request is constructed, add it into the body of the dmaap wrapper.
         */
        dmaapRequest.setBody(appcRequest);

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
        payload
            .forEach((key, value) -> payloadString.append("\"").append(key).append("\": ").append(value).append(","));
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
    public static SimpleEntry<PolicyResult, String> processResponse(LcmResponseWrapper dmaapResponse) {
        /* The actual APPC response is inside the wrapper's body field. */
        LcmResponse appcResponse = dmaapResponse.getBody();

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
        String responseValue = LcmResponseCode.toResponseValue(appcResponse.getStatus().getCode());
        if (responseValue == null) {
            message = "Policy was unable to parse APP-C response status code field.";
            return new AbstractMap.SimpleEntry<>(PolicyResult.FAILURE_EXCEPTION, message);
        }

        /* Save the APPC response's message for Policy notification message. */
        message = appcResponse.getStatus().getMessage();

        /* Maps the APPC response result to a Policy result. */
        switch (responseValue) {
            case LcmResponseCode.ACCEPTED:
                /* Nothing to do if code is accept, continue processing */
                result = null;
                break;
            case LcmResponseCode.SUCCESS:
                result = PolicyResult.SUCCESS;
                break;
            case LcmResponseCode.FAILURE:
                result = PolicyResult.FAILURE;
                break;
            case LcmResponseCode.REJECT:
            case LcmResponseCode.ERROR:
            default:
                result = PolicyResult.FAILURE_EXCEPTION;
        }
        return new AbstractMap.SimpleEntry<>(result, message);
    }
}
