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

package org.onap.policy.controlloop.actor.appc;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.appc.CommonHeader;
import org.onap.policy.appc.Request;
import org.onap.policy.appc.Response;
import org.onap.policy.appc.ResponseCode;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderInstantAsMillis;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Superclass for APPC Operations.
 */
public class AppcOperation extends BidirectionalTopicOperation<Request, Response> {
    private static final Logger logger = LoggerFactory.getLogger(AppcOperation.class);
    private static final StandardCoder coder = new StandardCoderInstantAsMillis();
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
    public AppcOperation(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
        super(params, config, Response.class);
    }

    /**
     * Ensures that A&AI customer query has been performed, and then runs the guard query.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        ControlLoopOperationParams cqParams = params.toBuilder().actor(AaiConstants.ACTOR_NAME)
                        .operation(AaiCqResponse.OPERATION).payload(null).retry(null).timeoutSec(null).build();

        // run Custom Query and Guard, in parallel
        return allOf(() -> params.getContext().obtain(AaiCqResponse.CONTEXT_KEY, cqParams), this::startGuardAsync);
    }

    @Override
    protected Request makeRequest(int attempt) {
        AaiCqResponse cq = params.getContext().getProperty(AaiCqResponse.CONTEXT_KEY);

        GenericVnf genvnf = cq.getGenericVnfByModelInvariantId(params.getTarget().getResourceID());
        if (genvnf == null) {
            logger.info("{}: target entity could not be found for {}", getFullName(), params.getRequestId());
            throw new IllegalArgumentException("target vnf-id could not be found");
        }

        Request request = new Request();
        request.setCommonHeader(new CommonHeader());
        request.getCommonHeader().setRequestId(params.getRequestId());

        // TODO ok to use UUID, or does it have to be the "attempt"?
        request.getCommonHeader().setSubRequestId(UUID.randomUUID().toString());

        request.setAction(getName());

        // convert payload strings to objects
        if (params.getPayload() == null) {
            logger.info("{}: no payload specified for {}", getFullName(), params.getRequestId());
        } else {
            convertPayload(params.getPayload(), request.getPayload());
        }

        // add/replace specific values
        request.getPayload().put(VNF_ID_KEY, genvnf.getVnfId());

        return request;
    }

    /**
     * Converts a payload. The original value is assumed to be a JSON string, which is
     * decoded into an object.
     *
     * @param source source from which to get the values
     * @param target where to place the decoded values
     */
    private static void convertPayload(Map<String, Object> source, Map<String, Object> target) {
        for (Entry<String, Object> ent : source.entrySet()) {
            Object value = ent.getValue();
            if (value == null) {
                target.put(ent.getKey(), null);
                continue;
            }

            try {
                target.put(ent.getKey(), coder.decode(value.toString(), Object.class));

            } catch (CoderException e) {
                logger.warn("cannot decode JSON value {}: {}", ent.getKey(), ent.getValue(), e);
            }
        }
    }

    /**
     * Note: these values must match {@link #SELECTOR_KEYS}.
     */
    @Override
    protected List<String> getExpectedKeyValues(int attempt, Request request) {
        return List.of(request.getCommonHeader().getSubRequestId());
    }

    @Override
    protected Status detmStatus(String rawResponse, Response response) {
        if (response.getStatus() == null) {
            throw new IllegalArgumentException("APP-C response is missing the response status");
        }

        ResponseCode code = ResponseCode.toResponseCode(response.getStatus().getCode());
        if (code == null) {
            throw new IllegalArgumentException(
                            "unknown APPC-C response status code: " + response.getStatus().getCode());
        }

        switch (code) {
            case SUCCESS:
                return Status.SUCCESS;
            case FAILURE:
                return Status.FAILURE;
            case ERROR:
            case REJECT:
                throw new IllegalArgumentException("APP-C request was not accepted, code=" + code);
            case ACCEPT:
            default:
                // awaiting a "final" response
                return Status.STILL_WAITING;
        }
    }

    /**
     * Sets the message to the status description, if available.
     */
    @Override
    public OperationOutcome setOutcome(OperationOutcome outcome, PolicyResult result, Response response) {
        if (response.getStatus() == null || response.getStatus().getDescription() == null) {
            return setOutcome(outcome, result);
        }

        outcome.setResult(result);
        outcome.setMessage(response.getStatus().getDescription());
        return outcome;
    }

    @Override
    protected Coder makeCoder() {
        return coder;
    }
}
