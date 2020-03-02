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

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.onap.policy.appclcm.AppcLcmCommonHeader;
import org.onap.policy.appclcm.AppcLcmInput;
import org.onap.policy.appclcm.AppcLcmOutput;
import org.onap.policy.appclcm.util.StatusCodeEnum;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AppcLcmOperation extends BidirectionalTopicOperation<AppcLcmInput, AppcLcmOutput> {

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
    public static final List<SelectorKey> SELECTOR_KEYS = List.of(new SelectorKey("CommonHeader", "SubRequestID"));

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public AppcLcmOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
        super(params, config, AppcLcmOutput.class);
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
    protected AppcLcmInput makeRequest(int attempt, String targetVnf) {
        AppcLcmInput request = new AppcLcmInput();
        request.setCommonHeader(new AppcLcmCommonHeader());
        request.getCommonHeader().setRequestId(params.getRequestId());

        // TODO ok to use UUID, or does it have to be the "attempt"?
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

        return request;
    }

    /**
     * Converts a payload. The original value is assumed to be a JSON string, which is
     * decoded into an object.
     *
     * @param source source from which to get the values
     * @param map where to place the decoded values
     */
    private static void convertPayload(Map<String, Object> source, Map<String, String> map) {
        for (Entry<String, Object> ent : source.entrySet()) {
            Object value = ent.getValue();
            if (value == null) {
                map.put(ent.getKey(), null);
                continue;
            }

            try {
                map.put(ent.getKey(), coder.decode(value.toString(), Object.class).toString());

            } catch (CoderException e) {
                logger.warn("cannot decode JSON value {}: {}", ent.getKey(), ent.getValue(), e);
            }
        }
    }

    /**
     * Note: these values must match {@link #SELECTOR_KEYS}.
     */
    @Override
    protected List<String> getExpectedKeyValues(int attempt, AppcLcmInput request) {
        return List.of(request.getCommonHeader().getSubRequestId());
    }

    @Override
    protected Status detmStatus(String rawResponse, AppcLcmOutput response) {
        if (response == null || response.getStatus() == null) {
            throw new IllegalArgumentException("APPC-LCM response is missing the response status");
        }

        StatusCodeEnum code = StatusCodeEnum.fromStatusCode(response.getStatus().getCode());

        if (code == null) {
            throw new IllegalArgumentException(
                    "unknown APPC-LCM response status code: " + response.getStatus().getCode());
        }

        logger.info("APPC-LCM Response Code {} Message is {}", code, response.getStatus().toString());
        logger.info("APPC-LCM Response Payload is {}", response.getPayload());

        switch (code) {
            case SUCCESS:
                return Status.SUCCESS;
            case FAILURE:
                return Status.FAILURE;
            case ERROR:
            case REJECT:
                throw new IllegalArgumentException("APP-C request was not accepted, code=" + code);
            case ACCEPTED:
            default:
                return Status.STILL_WAITING;
        }
    }

    /**
     * Sets the message to the status description, if available.
     */
    @Override
    public OperationOutcome setOutcome(OperationOutcome outcome, PolicyResult result, AppcLcmOutput response) {
        if (response.getStatus() == null || response.getStatus().getMessage() == null) {
            return setOutcome(outcome, result);
        }

        outcome.setResult(result);
        outcome.setMessage(response.getStatus().getMessage());
        return outcome;
    }
}
