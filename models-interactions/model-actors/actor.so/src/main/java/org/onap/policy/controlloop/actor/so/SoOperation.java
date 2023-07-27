/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Wipro Limited.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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

package org.onap.policy.controlloop.actor.so;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ModelVer;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.impl.HttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.so.SoCloudConfiguration;
import org.onap.policy.so.SoModelInfo;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestInfo;
import org.onap.policy.so.SoRequestParameters;
import org.onap.policy.so.SoRequestStatus;
import org.onap.policy.so.SoResponse;
import org.onap.policy.so.util.SoLocalDateTimeTypeAdapter;

/**
 * Superclass for SDNC Operators. Note: subclasses should invoke {@link #resetPollCount()}
 * each time they issue an HTTP request.
 */
public abstract class SoOperation extends HttpOperation<SoResponse> {
    private static final Coder coder = new SoCoder();

    public static final String FAILED = "FAILED";
    public static final String COMPLETE = "COMPLETE";
    public static final int SO_RESPONSE_CODE = 999;

    // fields within the policy payload
    public static final String REQ_PARAM_NM = "requestParameters";
    public static final String CONFIG_PARAM_NM = "configurationParameters";

    /* Values extracted from the parameter Target. These fields are required by any
       subclasses that make use of prepareSoModelInfo().
    */
    private final String modelCustomizationId;
    private final String modelInvariantId;
    private final String modelVersionId;
    private final String modelName;
    private final String modelVersion;



    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     * @param propertyNames names of properties required by this operation
     */
    protected SoOperation(ControlLoopOperationParams params, HttpPollingConfig config, List<String> propertyNames) {
        super(params, config, SoResponse.class, propertyNames);

        this.modelCustomizationId = null;
        this.modelInvariantId = null;
        this.modelVersionId = null;
        this.modelVersion = null;
        this.modelName = null;

        verifyNotNull("Target information", params.getTargetType());
    }

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     * @param propertyNames names of properties required by this operation
     * @param targetEntityIds Target Entity information
     */
    protected SoOperation(ControlLoopOperationParams params, HttpPollingConfig config, List<String> propertyNames,
                       Map<String, String> targetEntityIds) {
        super(params, config, SoResponse.class, propertyNames);

        verifyNotNull("Target entity Ids information", targetEntityIds);

        this.modelCustomizationId = targetEntityIds
                .get(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_CUSTOMIZATION_ID);
        this.modelInvariantId = targetEntityIds
                .get(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_INVARIANT_ID);
        this.modelVersionId = targetEntityIds
                .get(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_VERSION_ID);
        this.modelVersion = targetEntityIds
                .get(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_VERSION);
        this.modelName = targetEntityIds
                .get(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_NAME);

        verifyNotNull("Target information", params.getTargetType());
    }

    @Override
    protected void resetPollCount() {
        super.resetPollCount();
        setSubRequestId(null);
    }

    /**
     * Validates that the parameters contain the required target information to construct
     * the request.
     */
    protected void validateTarget() {
        verifyNotNull(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_CUSTOMIZATION_ID, modelCustomizationId);
        verifyNotNull(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_INVARIANT_ID, modelInvariantId);
        verifyNotNull(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_VERSION_ID, modelVersionId);
    }

    private void verifyNotNull(String type, Object value) {
        if (value == null) {
            throw new IllegalArgumentException("missing Target." + type);
        }
    }

    protected int getVfCount() {
        return getRequiredProperty(OperationProperties.DATA_VF_COUNT, "VF Count");
    }

    protected void setVfCount(int vfCount) {
        setProperty(OperationProperties.DATA_VF_COUNT, vfCount);
    }

    @Override
    protected Status detmStatus(Response rawResponse, SoResponse response) {
        if (rawResponse.getStatus() == 200) {
            String requestState = getRequestState(response);
            if (COMPLETE.equalsIgnoreCase(requestState)) {
                extractSubRequestId(response);
                return Status.SUCCESS;
            }

            if (FAILED.equalsIgnoreCase(requestState)) {
                extractSubRequestId(response);
                return Status.FAILURE;
            }
        }

        // still incomplete

        // need a request ID with which to query
        if (getSubRequestId() == null && !extractSubRequestId(response)) {
            throw new IllegalArgumentException("missing request ID in response");
        }

        return Status.STILL_WAITING;
    }

    @Override
    protected String getPollingPath() {
        return super.getPollingPath() + getSubRequestId();
    }

    @Override
    public void generateSubRequestId(int attempt) {
        setSubRequestId(null);
    }

    private boolean extractSubRequestId(SoResponse response) {
        if (response == null || response.getRequestReferences() == null
                        || response.getRequestReferences().getRequestId() == null) {
            return false;
        }

        setSubRequestId(response.getRequestReferences().getRequestId());
        return true;
    }

    /**
     * Gets the request state of a response.
     *
     * @param response response from which to get the state
     * @return the request state of the response, or {@code null} if it does not exist
     */
    protected String getRequestState(SoResponse response) {
        SoRequest request = response.getRequest();
        if (request == null) {
            return null;
        }

        SoRequestStatus status = request.getRequestStatus();
        if (status == null) {
            return null;
        }

        return status.getRequestState();
    }

    /**
     * Treats everything as a success, so we always go into
     * {@link #postProcessResponse(OperationOutcome, String, Response, SoResponse)}.
     */
    @Override
    protected boolean isSuccess(Response rawResponse, SoResponse response) {
        return true;
    }

    /**
     * Prepends the message with the http status code.
     */
    @Override
    public OperationOutcome setOutcome(OperationOutcome outcome, OperationResult result, Response rawResponse,
                    SoResponse response) {

        // set default result and message
        setOutcome(outcome, result);

        int code = (result == OperationResult.FAILURE_TIMEOUT ? SO_RESPONSE_CODE : rawResponse.getStatus());

        outcome.setResponse(response);
        outcome.setMessage(code + " " + outcome.getMessage());
        return outcome;
    }

    protected SoModelInfo prepareSoModelInfo() {
        var soModelInfo = new SoModelInfo();
        soModelInfo.setModelCustomizationId(modelCustomizationId);
        soModelInfo.setModelInvariantId(modelInvariantId);
        soModelInfo.setModelName(modelName);
        soModelInfo.setModelVersion(modelVersion);
        soModelInfo.setModelVersionId(modelVersionId);
        soModelInfo.setModelType("vfModule");
        return soModelInfo;
    }

    /**
     * Construct requestInfo for the SO requestDetails.
     *
     * @return SO request information
     */
    protected SoRequestInfo constructRequestInfo() {
        var soRequestInfo = new SoRequestInfo();
        soRequestInfo.setSource("POLICY");
        soRequestInfo.setSuppressRollback(false);
        soRequestInfo.setRequestorId("policy");
        return soRequestInfo;
    }

    /**
     * Builds the request parameters from the policy payload.
     */
    protected Optional<SoRequestParameters> buildRequestParameters() {
        if (params.getPayload() == null) {
            return Optional.empty();
        }

        Object data = params.getPayload().get(REQ_PARAM_NM);
        if (data == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(coder.decode(data.toString(), SoRequestParameters.class));
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

    /**
     * Construct cloudConfiguration for the SO requestDetails.
     *
     * @param tenantItem tenant item from A&AI named-query response
     * @return SO cloud configuration
     */
    protected SoCloudConfiguration constructCloudConfiguration(Tenant tenantItem, CloudRegion cloudRegionItem) {
        var cloudConfiguration = new SoCloudConfiguration();
        cloudConfiguration.setTenantId(getRequiredText("tenant ID", tenantItem.getTenantId()));
        cloudConfiguration.setLcpCloudRegionId(getRequiredText("cloud region ID", cloudRegionItem.getCloudRegionId()));
        return cloudConfiguration;
    }

    /**
     * Verifies that a value is not {@code null}.
     *
     * @param name value name
     * @param value value to check
     * @return the value
     */
    protected String getRequiredText(String name, String value) {
        if (value == null) {
            throw new IllegalArgumentException("missing " + name);
        }

        return value;
    }

    /**
     * Create simple HTTP headers for unauthenticated requests to SO.
     *
     * @return the HTTP headers
     */
    protected Map<String, Object> createSimpleHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Accept", MediaType.APPLICATION_JSON);
        return headers;
    }

    /*
     * These methods extract data from the Custom Query and throw an
     * IllegalArgumentException if the desired data item is not found.
     */

    protected GenericVnf getVnfItem() {
        return getRequiredProperty(OperationProperties.AAI_VNF, "generic VNF");
    }

    protected ServiceInstance getServiceInstance() {
        return getRequiredProperty(OperationProperties.AAI_SERVICE, "VNF Service Item");
    }

    protected Tenant getDefaultTenant() {
        return getRequiredProperty(OperationProperties.AAI_DEFAULT_TENANT, "Default Tenant Item");
    }

    protected CloudRegion getDefaultCloudRegion() {
        return getRequiredProperty(OperationProperties.AAI_DEFAULT_CLOUD_REGION, "Default Cloud Region");
    }

    protected ModelVer getVnfModel() {
        return getRequiredProperty(OperationProperties.AAI_VNF_MODEL, "generic VNF Model");
    }

    protected ModelVer getServiceModel() {
        return getRequiredProperty(OperationProperties.AAI_SERVICE_MODEL, "Service Model");
    }

    // these may be overridden by junit tests

    @Override
    protected Coder getCoder() {
        return coder;
    }

    private static class SoCoder extends StandardCoder {

        /**
         * Gson object used to encode and decode messages.
         */
        private static final Gson SO_GSON;

        /**
         * Gson object used to encode messages in "pretty" format.
         */
        private static final Gson SO_GSON_PRETTY;

        static {
            GsonBuilder builder = GsonMessageBodyHandler
                            .configBuilder(new GsonBuilder().registerTypeAdapter(StandardCoderObject.class,
                                            new StandardTypeAdapter()))
                            .registerTypeAdapter(LocalDateTime.class, new SoLocalDateTimeTypeAdapter());

            SO_GSON = builder.create();
            SO_GSON_PRETTY = builder.setPrettyPrinting().create();
        }

        public SoCoder() {
            super(SO_GSON, SO_GSON_PRETTY);
        }
    }
}
