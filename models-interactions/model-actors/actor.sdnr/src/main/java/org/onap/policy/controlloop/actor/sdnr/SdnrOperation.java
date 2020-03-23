/*-
 * ============LICENSE_START=======================================================
 * SdnrOperation
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

package org.onap.policy.controlloop.actor.sdnr;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.sdnr.PciBody;
import org.onap.policy.sdnr.PciCommonHeader;
import org.onap.policy.sdnr.PciMessage;
import org.onap.policy.sdnr.PciRequest;
import org.onap.policy.sdnr.PciResponse;
import org.onap.policy.sdnr.util.StatusCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SdnrOperation extends BidirectionalTopicOperation<PciMessage, PciMessage> {
    private static final Logger logger = LoggerFactory.getLogger(SdnrOperation.class);

    /**
     * Keys used to match the response with the request listener. The sub request ID is a
     * UUID, so it can be used to uniquely identify the response.
     * <p/>
     * Note: if these change, then {@link #getExpectedKeyValues(int, PciMessage)} must be
     * updated accordingly.
     */
    public static final List<SelectorKey> SELECTOR_KEYS = List.of(new SelectorKey("CommonHeader", "SubRequestID"));

    public SdnrOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
        super(params, config, PciMessage.class);
    }

    /**
     * Note: these values must match {@link #SELECTOR_KEYS}.
     */
    @Override
    protected List<String> getExpectedKeyValues(int attempt, PciMessage request) {
        return List.of(request.getBody().getInput().getCommonHeader().getSubRequestId());
    }

    @Override
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        return startGuardAsync();
    }

    @Override
    protected Status detmStatus(String rawResponse, PciMessage responseWrapper) {
        if (responseWrapper.getBody() == null || responseWrapper.getBody().getOutput() == null) {
            // no output - this must be a request, not a response - just ignore it
            logger.info("{}: ignoring request message for {}", getFullName(), params.getRequestId());
            return Status.STILL_WAITING;
        }

        PciResponse response = responseWrapper.getBody().getOutput();

        if (response.getStatus() == null) {
            throw new IllegalArgumentException("SDNR response is missing the response status");
        }

        StatusCodeEnum code = StatusCodeEnum.fromStatusCode(response.getStatus().getCode());

        if (code == null) {
            throw new IllegalArgumentException("unknown SDNR response status code: " + response.getStatus().getCode());
        }

        /*
         * Response and Payload are just printed and no further action needed since
         * casablanca release
         */
        logger.info("SDNR Response Code {} Message is {}", code, response.getStatus().getValue());
        logger.info("SDNR Response Payload is {}", response.getPayload());

        switch (code) {
            case SUCCESS:
            case PARTIAL_SUCCESS:
                return Status.SUCCESS;
            case FAILURE:
            case PARTIAL_FAILURE:
                return Status.FAILURE;
            case ERROR:
            case REJECT:
                throw new IllegalArgumentException("SDNR request was not accepted, code=" + code);
            case ACCEPTED:
            default:
                // awaiting a "final" response
                return Status.STILL_WAITING;
        }
    }

    /**
     * Sets the message to the status description, if available.
     */
    @Override
    public OperationOutcome setOutcome(OperationOutcome outcome, PolicyResult result, PciMessage responseWrapper) {
        if (responseWrapper.getBody() == null || responseWrapper.getBody().getOutput() == null) {
            return setOutcome(outcome, result);
        }

        PciResponse response = responseWrapper.getBody().getOutput();
        if (response.getStatus() == null || response.getStatus().getValue() == null) {
            return setOutcome(outcome, result);
        }

        outcome.setResult(result);
        outcome.setMessage(response.getStatus().getValue());
        return outcome;
    }

    @Override
    protected Pair<String, PciMessage> makeRequest(int attempt) {
        VirtualControlLoopEvent onset = params.getContext().getEvent();
        String subRequestId = UUID.randomUUID().toString();

        /* Construct an SDNR request using pci Model */

        PciMessage dmaapRequest = new PciMessage();
        dmaapRequest.setVersion("1.0");
        dmaapRequest.setCorrelationId(onset.getRequestId() + "-" + subRequestId);
        dmaapRequest.setType("request");

        /* This is the actual request that is placed in the dmaap wrapper. */
        final PciRequest sdnrRequest = new PciRequest();

        /* The common header is a required field for all SDNR requests. */
        PciCommonHeader requestCommonHeader = new PciCommonHeader();
        requestCommonHeader.setRequestId(onset.getRequestId());
        requestCommonHeader.setSubRequestId(subRequestId);

        sdnrRequest.setCommonHeader(requestCommonHeader);
        sdnrRequest.setPayload(onset.getPayload());

        /*
         * Once the pci request is constructed, add it into the body of the dmaap wrapper.
         */
        PciBody body = new PciBody();
        body.setInput(sdnrRequest);
        dmaapRequest.setBody(body);
        logger.info("SDNR Request to be sent is {}", dmaapRequest);

        /* Return the request to be sent through dmaap. */
        return Pair.of(subRequestId, dmaapRequest);
    }
}
