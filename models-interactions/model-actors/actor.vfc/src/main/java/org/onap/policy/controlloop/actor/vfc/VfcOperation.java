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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.vfc.VfcHealActionVmInfo;
import org.onap.policy.vfc.VfcHealAdditionalParams;
import org.onap.policy.vfc.VfcHealRequest;
import org.onap.policy.vfc.VfcRequest;
import org.onap.policy.vfc.VfcResponse;

public abstract class VfcOperation extends HttpOperation<VfcResponse> {
    public static final String FAILED = "FAILED";
    public static final String COMPLETE = "COMPLETE";
    public static final int VFC_RESPONSE_CODE = 999;
    public static final String GENERIC_VNF_ID = "generic-vnf.vnf-id";

    public static final String REQ_PARAM_NM = "requestParameters";
    public static final String CONFIG_PARAM_NM = "configurationParameters";

    // @formatter:off
    private static final List<String> PROPERTY_NAMES = List.of(
                            OperationProperties.ENRICHMENT_SERVICE_ID,
                            OperationProperties.ENRICHMENT_VSERVER_ID,
                            OperationProperties.ENRICHMENT_VSERVER_NAME,
                            OperationProperties.ENRICHMENT_GENERIC_VNF_ID);
    // @formatter:on

    /**
     * Job ID extracted from the first response.
     */
    private String jobId;


    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public VfcOperation(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config, VfcResponse.class, PROPERTY_NAMES);

        setUsePolling();
    }

    @Override
    protected void resetPollCount() {
        super.resetPollCount();
        jobId = null;
    }

    @Override
    protected String getPollingPath() {
        return super.getPollingPath() + jobId;
    }

    /**
     * Starts the GUARD.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        return startGuardAsync();
    }

    @Override
    protected Status detmStatus(Response rawResponse, VfcResponse response) {
        if (rawResponse.getStatus() == 200) {
            String requestState = getRequestState(response);
            if ("finished".equalsIgnoreCase(requestState)) {
                extractJobId(response);
                return Status.SUCCESS;
            }

            if ("error".equalsIgnoreCase(requestState)) {
                extractJobId(response);
                return Status.FAILURE;
            }
        }

        // still incomplete

        // need a job ID with which to query
        if (jobId == null && !extractJobId(response)) {
            throw new IllegalArgumentException("missing job ID in response");
        }

        return Status.STILL_WAITING;
    }

    private boolean extractJobId(VfcResponse response) {
        if (response == null || response.getJobId() == null) {
            return false;
        }

        jobId = response.getJobId();
        return true;
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

        int code = (result == PolicyResult.FAILURE_TIMEOUT ? VFC_RESPONSE_CODE : rawResponse.getStatus());

        outcome.setResponse(response);
        outcome.setMessage(code + " " + outcome.getMessage());
        return outcome;
    }

    /**
     * Construct VfcRequestObject from the ControlLoopOperationParams.
     *
     * @return request
     */
    protected VfcRequest constructVfcRequest() {
        final String serviceInstance = getOptProperty(OperationProperties.ENRICHMENT_SERVICE_ID,
                        "service-instance.service-instance-id");
        final String vmId = getOptProperty(OperationProperties.ENRICHMENT_VSERVER_ID, "vserver.vserver-id");
        final String vmName = getOptProperty(OperationProperties.ENRICHMENT_VSERVER_NAME, "vserver.vserver-name");
        final String vnfId = getOptProperty(OperationProperties.ENRICHMENT_GENERIC_VNF_ID, GENERIC_VNF_ID);

        if (StringUtils.isBlank(serviceInstance) || StringUtils.isBlank(vmId) || StringUtils.isBlank(vmName)) {
            // original code did not check the VNF id, so we won't check it either
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
        healRequest.setVnfInstanceId(vnfId);
        healRequest.setCause(getName());
        healRequest.setAdditionalParams(additionalParams);

        VfcRequest request = new VfcRequest();
        request.setHealRequest(healRequest);
        request.setNsInstanceId(serviceInstance);
        request.setRequestId(params.getRequestId());

        return request;
    }

    /**
     * Gets an optional property, first checking the properties, then checking the
     * enrichment data.
     *
     * @param propName property name
     * @param enrichmentName property name within the enrichment data
     * @return the property's value, or {@code null} if it is not found
     */
    protected String getOptProperty(String propName, String enrichmentName) {
        if (containsProperty(propName)) {
            // return the value, even if it's null
            return getProperty(propName);
        }

        return params.getContext().getEnrichment().get(enrichmentName);
    }
}
