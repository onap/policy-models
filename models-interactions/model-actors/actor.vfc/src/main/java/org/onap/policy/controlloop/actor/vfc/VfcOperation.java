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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.ws.rs.core.Response;
import lombok.Getter;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
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
    private static final Coder coder = new StandardCoder();

    public static final String FAILED = "FAILED";
    public static final String COMPLETE = "COMPLETE";
    public static final int VFC_RESPONSE_CODE = 999;

    // TODO Verify following strings.
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
        VirtualControlLoopEvent onset = params.getContext().getEvent();
        if (onset.getAai() == null) {
            onset.setAai(new HashMap<String, String>());
        }
        onset.getAai().put("vserver.vserver-name", "vserver-name-16102016-aai3255-data-11-1");
        onset.getAai().put("vserver.vserver-id", "vserver-id-16102016-aai3255-data-11-1");
        onset.getAai().put("generic-vnf.vnf-id", "vnf-id-16102016-aai3255-data-11-1");
        onset.getAai().put("service-instance.service-instance-id", "service-instance-id-16102016-aai3255-data-11-1");
        onset.getAai().put("vserver.is-closed-loop-disabled", "false");
        onset.getAai().put("vserver.prov-status", "ACTIVE");
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
            if (COMPLETE.equalsIgnoreCase(requestState)) {
                return CompletableFuture
                        .completedFuture(setOutcome(outcome, PolicyResult.SUCCESS, rawResponse, response));
            }

            if (FAILED.equalsIgnoreCase(requestState)) {
                return CompletableFuture
                        .completedFuture(setOutcome(outcome, PolicyResult.FAILURE, rawResponse, response));
            }
        }

        // still incomplete

        // need a request ID with which to query
        if (response == null || response.getRequestId() == null) {
            throw new IllegalArgumentException("missing request ID in response");
        }

        // see if the limit for the number of "gets" has been reached
        if (getCount++ >= getMaxGets()) {
            logger.warn("{}: execeeded 'get' limit {} for {}", getFullName(), getMaxGets(), params.getRequestId());
            setOutcome(outcome, PolicyResult.FAILURE_TIMEOUT);
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
        String path = getPathGet() + response.getRequestId();
        String url = getClient().getBaseUrl() + path;

        logger.debug("{}: 'get' count {} for {}", getFullName(), getCount, params.getRequestId());

        logMessage(EventType.OUT, CommInfrastructure.REST, url, null);

        // TODO should this use "path" or the full "url"?
        return handleResponse(outcome, url, callback -> getClient().get(callback, path, null));
    }

    /**
     * Gets the request state of a response.
     *
     * @param response response from which to get the state
     * @return the request state of the response, or {@code null} if it does not exist
     */
    protected String getRequestState(VfcResponse response) {

        // TODO verify this is the correct information needed
        if (response == null || response.getResponseDescriptor() == null
                || response.getResponseDescriptor().getStatus() == null
                || response.getResponseDescriptor().getStatus().isEmpty()) {
            return null;
        }
        return response.getResponseDescriptor().getStatus();

        /*
         * VfcRequest request = response.getRequest();
         * if (request == null) {
         * return null;
         * }
         *
         * SoRequestStatus status = request.getRequestStatus();
         * if (status == null) {
         * return null;
         * }
         *
         * return status.getRequestState();
         */
        // return null;
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

        outcome.setMessage(rawResponse.getStatus() + " " + outcome.getMessage());
        return outcome;
    }

    /**
     * TODO: not sure if these are the correct values to use for getters/setters
     * Construct VfcRequestObject from the params.
     *
     * @return request
     */
    protected VfcRequest constructVfcRequest() {
        VirtualControlLoopEvent onset = params.getContext().getEvent();
        String serviceInstance = onset.getAai().get("service-instance.service-instance-id");
        String vmId = onset.getAai().get("vserver.vserver-id");
        String vmName = onset.getAai().get("vserver.vserver-name");
        AaiCqResponse aaiCqResponse = params.getContext().getProperty(AaiCqResponse.CONTEXT_KEY);

        if (serviceInstance == null || serviceInstance.isEmpty() || vmId == null || vmId.isEmpty() || vmName == null
                || vmName.isEmpty()) {
            if (aaiCqResponse == null) {
                return null;
            }

            serviceInstance = aaiCqResponse.getServiceInstance().getServiceInstanceId();

            if (serviceInstance == null) {
                return null;
            }
        }

        VfcHealActionVmInfo vmActionInfo = new VfcHealActionVmInfo();
        vmActionInfo.setVmid(vmId);
        vmActionInfo.setVmname(vmName);

        VfcHealAdditionalParams additionalParams = new VfcHealAdditionalParams();
        // TODO additionalParams.setAction("");
        additionalParams.setActionInfo(vmActionInfo);

        VfcHealRequest healRequest = new VfcHealRequest();
        // TODO healRequest.setVnfInstanceId("");
        // TODO healRequest.setCause("");
        healRequest.setAdditionalParams(additionalParams);

        VfcRequest request = new VfcRequest();
        request.setHealRequest(healRequest);
        request.setNsInstanceId(serviceInstance);
        request.setRequestId(onset.getRequestId());

        return request;
    }

    /**
     * Builds the request parameters from the policy payload.
     *
     * @return optional
     */
    protected Optional<VfcHealAdditionalParams> buildAdditionalParams() {
        if (params.getPayload() == null) {
            return Optional.empty();
        }

        Object data = params.getPayload().get(REQ_PARAM_NM);
        if (data == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(coder.decode(data.toString(), VfcHealAdditionalParams.class));
        } catch (CoderException e) {
            throw new IllegalArgumentException("invalid payload value: " + REQ_PARAM_NM);
        }
    }

    /**
     * Builds the configuration parameters from the policy payload.
     */
    protected Optional<List<Map<String, String>>> buildConfigurationParameters() {
        if (params.getPayload() == null) {
            return Optional.empty();
        }

        Object data = params.getPayload().get(CONFIG_PARAM_NM);
        if (data == null) {
            return Optional.empty();
        }

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, String>> result = coder.decode(data.toString(), ArrayList.class);
            return Optional.of(result);
        } catch (CoderException | RuntimeException e) {
            throw new IllegalArgumentException("invalid payload value: " + CONFIG_PARAM_NM);
        }
    }

    /*
     * These methods extract data from the Custom Query and throw an
     * IllegalArgumentException if the desired data item is not found.
     */

    protected GenericVnf getVnfItem(AaiCqResponse aaiCqResponse, VfcHealActionVmInfo vmInfo) {
        GenericVnf vnf = aaiCqResponse.getGenericVnfByVnfName(vmInfo.getVmname());
        if (vnf == null) {
            throw new IllegalArgumentException("missing generic VNF");
        }

        return vnf;
    }

    protected ServiceInstance getServiceInstance(AaiCqResponse aaiCqResponse) {
        ServiceInstance vnfService = aaiCqResponse.getServiceInstance();
        if (vnfService == null) {
            throw new IllegalArgumentException("missing VNF Service Item");
        }

        return vnfService;
    }

    protected Tenant getDefaultTenant(AaiCqResponse aaiCqResponse) {
        Tenant tenant = aaiCqResponse.getDefaultTenant();
        if (tenant == null) {
            throw new IllegalArgumentException("missing Tenant Item");
        }

        return tenant;
    }

    protected CloudRegion getDefaultCloudRegion(AaiCqResponse aaiCqResponse) {
        CloudRegion cloudRegion = aaiCqResponse.getDefaultCloudRegion();
        if (cloudRegion == null) {
            throw new IllegalArgumentException("missing Cloud Region");
        }

        return cloudRegion;
    }

    // these may be overridden by jUnit tests

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
