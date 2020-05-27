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

package org.onap.policy.controlloop.actor.vfc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.ws.rs.core.Response;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.vfc.VfcHealActionVmInfo;
import org.onap.policy.vfc.VfcHealAdditionalParams;
import org.onap.policy.vfc.VfcHealRequest;
import org.onap.policy.vfc.VfcRequest;
import org.onap.policy.vfc.VfcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class VfcOperation extends HttpOperation<VfcResponse> {
    private static final Logger logger = LoggerFactory.getLogger(VfcOperation.class);

    public static final String FAILED = "FAILED";
    public static final String COMPLETE = "COMPLETE";
    public static final int VFC_RESPONSE_CODE = 999;
    public static final String GENERIC_VNF_ID = "generic-vnf.vnf-id";

    public static final String REQ_PARAM_NM = "requestParameters";
    public static final String CONFIG_PARAM_NM = "configurationParameters";

    private final VfcConfig config;

    /**
     * Number of "get" requests issued so far, on the current operation attempt.
     */
    @Getter
    private int getCount;

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public VfcOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, VfcResponse.class);
        this.config = (VfcConfig) config;
    }

    /**
     * Subclasses should invoke this before issuing their first HTTP request.
     */
    protected void resetGetCount() {
        getCount = 0;
    }

    /**
     * Starts the GUARD.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        return startGuardAsync();
    }

    /**
     * If the response does not indicate that the request has been completed, then sleep a
     * bit and issue a "get".
     */
    @Override
    protected CompletableFuture<OperationOutcome> postProcessResponse(OperationOutcome outcome, String url,
            Response rawResponse, VfcResponse response) {
        // Determine if the request has "completed" and determine if it was successful
        if (rawResponse.getStatus() == 200) {
            String requestState = getRequestState(response);
            if ("finished".equalsIgnoreCase(requestState)) {
                return CompletableFuture
                        .completedFuture(setOutcome(outcome, PolicyResult.SUCCESS, rawResponse, response));
            }

            if ("error".equalsIgnoreCase(requestState)) {
                return CompletableFuture
                        .completedFuture(setOutcome(outcome, PolicyResult.FAILURE, rawResponse, response));
            }
        }

        // still incomplete

        // need a request ID with which to query
        if (response == null || response.getJobId() == null) {
            throw new IllegalArgumentException("missing job ID in response");
        }

        // see if the limit for the number of "gets" has been reached
        if (getCount++ >= getMaxGets()) {
            logger.warn("{}: execeeded 'get' limit {} for {}", getFullName(), getMaxGets(), params.getRequestId());
            setOutcome(outcome, PolicyResult.FAILURE_TIMEOUT);
            outcome.setResponse(response);
            outcome.setMessage(VFC_RESPONSE_CODE + " " + outcome.getMessage());
            return CompletableFuture.completedFuture(outcome);
        }

        // sleep and then perform a "get" operation
        Function<Void, CompletableFuture<OperationOutcome>> doGet = unused -> issueGet(outcome, response);
        return sleep(getWaitMsGet(), TimeUnit.MILLISECONDS).thenComposeAsync(doGet);
    }

    /**
     * Issues a "get" request to see if the original request is complete yet.
     *
     * @param outcome outcome to be populated with the response
     * @param response previous response
     * @return a future that can be used to cancel the "get" request or await its response
     */
    private CompletableFuture<OperationOutcome> issueGet(OperationOutcome outcome, VfcResponse response) {
        String path = getPathGet() + response.getJobId();
        String url = getClient().getBaseUrl() + path;

        logger.debug("{}: 'get' count {} for {}", getFullName(), getCount, params.getRequestId());

        logMessage(EventType.OUT, CommInfrastructure.REST, url, null);

        return handleResponse(outcome, url, callback -> getClient().get(callback, path, null));
    }

    /**
     * Gets the request state of a response.
     *
     * @param response response from which to get the state
     * @return the request state of the response, or {@code null} if it does not exist
     */
    protected String getRequestState(VfcResponse response) {
        if (response == null || response.getResponseDescriptor() == null
                || StringUtils.isBlank(response.getResponseDescriptor().getStatus())) {
            return null;
        }
        return response.getResponseDescriptor().getStatus();
    }

    /**
     * Treats everything as a success, so we always go into
     * {@link #postProcessResponse(OperationOutcome, String, Response, SoResponse)}.
     */
    @Override
    protected boolean isSuccess(Response rawResponse, VfcResponse response) {
        return true;
    }

    /**
     * Prepends the message with the http status code.
     */
    @Override
    public OperationOutcome setOutcome(OperationOutcome outcome, PolicyResult result, Response rawResponse,
            VfcResponse response) {

        // set default result and message
        setOutcome(outcome, result);

        outcome.setResponse(response);
        outcome.setMessage(rawResponse.getStatus() + " " + outcome.getMessage());
        return outcome;
    }

    /**
     * Construct VfcRequestObject from the ControlLoopOperationParams.
     *
     * @return request
     */
    protected VfcRequest constructVfcRequest() {
        ControlLoopEventContext context = params.getContext();
        String serviceInstance = context.getEnrichment().get("service-instance.service-instance-id");
        String vmId = context.getEnrichment().get("vserver.vserver-id");
        String vmName = context.getEnrichment().get("vserver.vserver-name");

        if (StringUtils.isBlank(serviceInstance) || StringUtils.isBlank(vmId) || StringUtils.isBlank(vmName)) {
            throw new IllegalArgumentException(
                    "Cannot extract enrichment data for service instance, server id, or server name.");
        }

        VfcHealActionVmInfo vmActionInfo = new VfcHealActionVmInfo();
        vmActionInfo.setVmid(vmId);
        vmActionInfo.setVmname(vmName);

        VfcHealAdditionalParams additionalParams = new VfcHealAdditionalParams();
        additionalParams.setAction(getName());
        additionalParams.setActionInfo(vmActionInfo);

        VfcHealRequest healRequest = new VfcHealRequest();
        healRequest.setVnfInstanceId(params.getContext().getEvent().getAai().get(GENERIC_VNF_ID));
        healRequest.setCause(getName());
        healRequest.setAdditionalParams(additionalParams);

        VfcRequest request = new VfcRequest();
        request.setHealRequest(healRequest);
        request.setNsInstanceId(serviceInstance);
        request.setRequestId(context.getEvent().getRequestId());

        return request;
    }

    // These may be overridden by jUnit tests

    /**
     * Gets the wait time, in milliseconds, between "get" requests.
     *
     * @return the wait time, in milliseconds, between "get" requests
     */
    public long getWaitMsGet() {
        return TimeUnit.MILLISECONDS.convert(getWaitSecGet(), TimeUnit.SECONDS);
    }

    public int getMaxGets() {
        return config.getMaxGets();
    }

    public String getPathGet() {
        return config.getPathGet();
    }

    public int getWaitSecGet() {
        return config.getWaitSecGet();
    }
}
