/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.sdnr;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.ControlLoopResponse;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.Operator;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicActor;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicActorParams;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.sdnr.PciCommonHeader;
import org.onap.policy.sdnr.PciRequest;
import org.onap.policy.sdnr.PciRequestWrapper;
import org.onap.policy.sdnr.PciResponse;
import org.onap.policy.sdnr.PciResponseCode;
import org.onap.policy.sdnr.PciResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SDNR is an unusual actor in that it uses a single, generic operator to initiate all
 * operation types. The action taken is always the same, only the operation name changes.
 */
public class SdnrActor extends BidirectionalTopicActor<BidirectionalTopicActorParams>  {

    public static final String NAME = "SDNR";

    // TODO old code: remove lines down to **HERE**

    private static final Logger logger = LoggerFactory.getLogger(SdnrActor.class);

    // Strings for targets
    private static final String TARGET_VNF = "VNF";

    // Strings for recipes
    private static final String RECIPE_MODIFY = "ModifyConfig";

    /* To be used in future releases when pci ModifyConfig is used */
    private static final String SDNR_REQUEST_PARAMS = "request-parameters";
    private static final String SDNR_CONFIG_PARAMS = "configuration-parameters";

    private static final ImmutableList<String> recipes = ImmutableList.of(RECIPE_MODIFY);
    private static final ImmutableMap<String, List<String>> targets = new ImmutableMap.Builder<String, List<String>>()
            .put(RECIPE_MODIFY, ImmutableList.of(TARGET_VNF)).build();
    private static final ImmutableMap<String, List<String>> payloads = new ImmutableMap.Builder<String, List<String>>()
            .put(RECIPE_MODIFY, ImmutableList.of(SDNR_REQUEST_PARAMS, SDNR_CONFIG_PARAMS)).build();

    // **HERE**

    /**
     * Constructor.
     */
    public SdnrActor() {
        super(NAME, BidirectionalTopicActorParams.class);

        addOperator(new BidirectionalTopicOperator(NAME, SdnrOperation.NAME, this, SdnrOperation.SELECTOR_KEYS,
                        SdnrOperation::new));
    }

    @Override
    public Operator getOperator(String name) {
        /*
         * All operations are managed by the same operator, regardless of the name.
         */
        return super.getOperator(SdnrOperation.NAME);
    }

    // TODO old code: remove lines down to **HERE**

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
     * Constructs an SDNR request conforming to the pci API. The actual request is
     * constructed and then placed in a wrapper object used to send through DMAAP.
     *
     * @param onset
     *            the event that is reporting the alert for policy to perform an
     *            action
     * @param operation
     *            the control loop operation specifying the actor, operation,
     *            target, etc.
     * @param policy
     *            the policy the was specified from the yaml generated by CLAMP or
     *            through the Policy GUI/API
     * @return an SDNR request conforming to the pci API using the DMAAP wrapper
     */

    public static PciRequestWrapper constructRequest(VirtualControlLoopEvent onset, ControlLoopOperation operation,
            Policy policy) {

        /* Construct an SDNR request using pci Model */

        /*
         * The actual pci request is placed in a wrapper used to send through dmaap. The
         * current version is 2.0 as of R1.
         */
        PciRequestWrapper dmaapRequest = new PciRequestWrapper();
        dmaapRequest.setVersion("1.0");
        dmaapRequest.setCorrelationId(onset.getRequestId() + "-" + operation.getSubRequestId());
        dmaapRequest.setRpcName(policy.getRecipe().toLowerCase());
        dmaapRequest.setType("request");

        /* This is the actual request that is placed in the dmaap wrapper. */
        final PciRequest sdnrRequest = new PciRequest();

        /* The common header is a required field for all SDNR requests. */
        PciCommonHeader requestCommonHeader = new PciCommonHeader();
        requestCommonHeader.setRequestId(onset.getRequestId());
        requestCommonHeader.setSubRequestId(operation.getSubRequestId());

        sdnrRequest.setCommonHeader(requestCommonHeader);
        sdnrRequest.setPayload(onset.getPayload());

        /*
         * An action is required for all SDNR requests, this will be the recipe
         * specified in the policy.
         */
        sdnrRequest.setAction(policy.getRecipe());

        /*
         * Once the pci request is constructed, add it into the body of the dmaap
         * wrapper.
         */
        dmaapRequest.setBody(sdnrRequest);
        logger.info("SDNR Request to be sent is {}", dmaapRequest);

        /* Return the request to be sent through dmaap. */
        return dmaapRequest;
    }

    /**
     * Parses the operation attempt using the subRequestId of SDNR response.
     *
     * @param subRequestId
     *            the sub id used to send to SDNR, Policy sets this using the
     *            operation attempt
     *
     * @return the current operation attempt
     */
    public static Integer parseOperationAttempt(String subRequestId) {
        Integer operationAttempt;
        try {
            operationAttempt = Integer.parseInt(subRequestId);
        } catch (NumberFormatException e) {
            logger.debug("A NumberFormatException was thrown in parsing the operation attempt {}", subRequestId);
            return null;
        }
        return operationAttempt;
    }

    /**
     * Processes the SDNR pci response sent from SDNR. Determines if the SDNR
     * operation was successful/unsuccessful and maps this to the corresponding
     * Policy result.
     *
     * @param dmaapResponse
     *            the dmaap wrapper message that contains the actual SDNR reponse
     *            inside the body field
     *
     * @return an key-value pair that contains the Policy result and SDNR response
     *         message
     */
    public static Pair<PolicyResult, String> processResponse(
            PciResponseWrapper dmaapResponse) {

        logger.info("SDNR processResponse called : {}", dmaapResponse);

        /* The actual SDNR response is inside the wrapper's body field. */
        PciResponse sdnrResponse = dmaapResponse.getBody();

        /* The message returned in the SDNR response. */
        String message;

        /* The Policy result determined from the SDNR Response. */
        PolicyResult result;

        /*
         * If there is no status, Policy cannot determine if the request was successful.
         */
        if (sdnrResponse.getStatus() == null) {
            message = "Policy was unable to parse SDN-R response status field (it was null).";
            return Pair.of(PolicyResult.FAILURE_EXCEPTION, message);
        }

        /*
         * If there is no code, Policy cannot determine if the request was successful.
         */
        String responseValue = PciResponseCode.toResponseValue(sdnrResponse.getStatus().getCode());
        if (responseValue == null) {
            message = "Policy was unable to parse SDN-R response status code field.";
            return Pair.of(PolicyResult.FAILURE_EXCEPTION, message);
        }
        logger.info("SDNR Response Code is {}", responseValue);

        /* Save the SDNR response's message for Policy notification message. */
        message = sdnrResponse.getStatus().getValue();
        logger.info("SDNR Response Message is {}", message);

        /*
         * Response and Payload are just printed and no further action needed in
         * casablanca release
         */
        String rspPayload = sdnrResponse.getPayload();
        logger.info("SDNR Response Payload is {}", rspPayload);

        /* Maps the SDNR response result to a Policy result. */
        switch (responseValue) {
            case PciResponseCode.ACCEPTED:
                /* Nothing to do if code is accept, continue processing */
                result = null;
                break;
            case PciResponseCode.SUCCESS:
                result = PolicyResult.SUCCESS;
                break;
            case PciResponseCode.FAILURE:
                result = PolicyResult.FAILURE;
                break;
            case PciResponseCode.REJECT:
            case PciResponseCode.ERROR:
            default:
                result = PolicyResult.FAILURE_EXCEPTION;
        }
        return Pair.of(result, message);
    }

    /**
     * Converts the SDNR response to ControlLoopResponse object.
     *
     * @param dmaapResponse
     *            the dmaap wrapper message that contains the actual SDNR reponse
     *            inside the body field
     *
     * @return a ControlLoopResponse object to send to DCAE_CL_RSP topic
     */
    public static ControlLoopResponse getControlLoopResponse(PciResponseWrapper dmaapResponse,
            VirtualControlLoopEvent event) {

        logger.info("SDNR getClosedLoopResponse called : {} {}", dmaapResponse, event);

        /* The actual SDNR response is inside the wrapper's body field. */
        PciResponse sdnrResponse = dmaapResponse.getBody();

        /* The ControlLoop response determined from the SDNR Response and input event. */
        ControlLoopResponse clRsp = new ControlLoopResponse();
        clRsp.setPayload(sdnrResponse.getPayload());
        clRsp.setFrom(NAME);
        clRsp.setTarget("DCAE");
        clRsp.setClosedLoopControlName(event.getClosedLoopControlName());
        clRsp.setPolicyName(event.getPolicyName());
        clRsp.setPolicyVersion(event.getPolicyVersion());
        clRsp.setRequestId(event.getRequestId());
        clRsp.setVersion(event.getVersion());
        logger.info("SDNR getClosedLoopResponse clRsp : {}", clRsp);

        return clRsp;
    }

    // **HERE**
}
