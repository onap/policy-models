/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;
import com.google.protobuf.util.JsonFormat;
import io.grpc.Status;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.policy.cds.api.CdsProcessorListener;
import org.onap.policy.cds.client.CdsProcessorGrpcClient;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.controlloop.actor.cds.beans.ConfigDeployRequest;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.policy.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDS Actor service-provider implementation.
 */
public class CdsActorServiceProvider implements Actor, CdsProcessorListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdsActorServiceProvider.class);

    private final String requestId;
    private final String subRequestId;
    private final Policy policy;
    private final Map<String, String> aaiParams;
    private final CdsServerProperties cdsProps;
    private final AtomicReference<String> cdsResponse = new AtomicReference<>();
    private final CdsProcessorGrpcClient cdsClient;

    /**
     * Constructor.
     * @param requestId The request-id typically coming from the onset event that triggers the closeloop.
     * @param subRequestId The sub-request-id that gets added by the controlloop manager.
     * @param policy Operational policy object that embeds the key actor attributes.
     * @param aaiParams Map of enriched AAI attributes in node.attribute notation.
     * @param cdsProps CDS server properties.
     */
    public CdsActorServiceProvider(String requestId, String subRequestId, Policy policy, Map<String, String> aaiParams,
        CdsServerProperties cdsProps) {
        this.requestId = requestId;
        this.subRequestId = subRequestId;
        this.policy = policy;
        this.aaiParams = aaiParams;
        this.cdsProps = cdsProps;
        this.cdsClient = new CdsProcessorGrpcClient(this, cdsProps);
    }

    /**
     * Constructor (primarily used for testing).
     * @param requestId The request-id typically coming from the onset event that triggers the closeloop.
     * @param subRequestId The sub-request-id that gets added by the controlloop operation.
     * @param policy Operational policy object that embeds the key actor attributes.
     * @param aaiParams Map of enriched AAI attributes in node.attribute notation.
     * @param cdsProps CDS server properties.
     * @param cdsClient CDS client object.
     */
    public CdsActorServiceProvider(String requestId, String subRequestId, Policy policy, Map<String, String> aaiParams,
        CdsServerProperties cdsProps, CdsProcessorGrpcClient cdsClient) {
        this.requestId = requestId;
        this.subRequestId = subRequestId;
        this.policy = policy;
        this.aaiParams = aaiParams;
        this.cdsProps = cdsProps;
        this.cdsClient = cdsClient;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public String actor() {
        return CdsActorConstants.CDS_ACTOR;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public List<String> recipes() {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public List<String> recipeTargets(final String recipe) {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public List<String> recipePayloads(final String recipe) {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void onMessage(final ExecutionServiceOutput message) {
        LOGGER.info("Received notification from CDS: {}", message);
        EventType eventType = message.getStatus().getEventType();
        switch (eventType) {
            case EVENT_COMPONENT_FAILURE:
                cdsResponse.set(CdsActorConstants.FAILED);
                break;
            case EVENT_COMPONENT_PROCESSING:
                cdsResponse.set(CdsActorConstants.PROCESSING);
                break;
            case EVENT_COMPONENT_EXECUTED:
                cdsResponse.set(CdsActorConstants.SUCCESS);
                break;
            default:
                cdsResponse.set(CdsActorConstants.FAILED);
                break;
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void onError(final Throwable throwable) {
        Status status = Status.fromThrowable(throwable);
        LOGGER.error("Failed processing blueprint {} {}", status, throwable);
    }

    /**
     * Construct the CDS input request and send the request to the CDS gRPC server.
     *
     * @return Status of the CDS request.
     */
    public String sendRequestToCds() {
        ExecutionServiceInput executionServiceInput = constructRequest();
        return sendRequestToCds(executionServiceInput);
    }

    /**
     * Send the request to the CDS gRPC server.
     *
     * @param executionServiceInput ExecutionServiceInput object to be sent to CDS gRPC server.
     * @return Status of the CDS request.
     */
    public String sendRequestToCds(ExecutionServiceInput executionServiceInput) {
        try {
            LOGGER.trace("Start CdsActorServiceProvider.sendRequestToCds {}.", executionServiceInput);
            CountDownLatch countDownLatch = cdsClient.sendRequest(executionServiceInput);
            boolean status = countDownLatch.await(cdsProps.getTimeout(), TimeUnit.SECONDS);
            LOGGER.trace("CDS response {}", status ? CdsActorConstants.SUCCESS : CdsActorConstants.TIMED_OUT);
        } catch (InterruptedException ex) {
            LOGGER.error("Caught exception in sendRequestToCds in CdsActorServiceProvider: ", ex);
            Thread.currentThread().interrupt();
        }
        LOGGER.info("Status of the CDS gRPC request is: {}", cdsResponse.get());
        return cdsResponse.get();
    }

    /**
     * Build the CDS ExecutionServiceInput request from the policy object and the AAI enriched parameters.
     *
     * @return ExecutionServiceInput instance.
     */
    private ExecutionServiceInput constructRequest() {
        Map<String, String> payload = policy.getPayload();
        validatePolicyPayloadForCdsMandatoryParams(payload);
        String cbaName = payload.get(CdsActorConstants.KEY_CBA_NAME);
        String cbaVersion = payload.get(CdsActorConstants.KEY_CBA_VERSION);
        String cbaActionName = policy.getRecipe();

        // Embed payload from policy to ConfigDeployRequest object, serialize and inject into grpc request.
        ConfigDeployRequest request = new ConfigDeployRequest();
        request.setConfigDeployProperties(payload);

        // Inject AAI properties into payload map. Offer flexibility to the usecase
        // implementation to inject whatever AAI parameters are of interest to them.
        // E.g. For vFW usecase El-Alto inject service-instance-id, generic-vnf-id as needed by CDS.
        request.setAaiProperties(aaiParams);

        Builder struct = Struct.newBuilder();
        try {
            Preconditions.checkState(!Strings.isNullOrEmpty(request.toString()), "Unable to build "
                + "config-deploy-request from payload parameters: {}", payload);
            JsonFormat.parser().merge(request.toString(), struct);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Failed to parse received message. blueprint({}:{}) for action({}) {}", cbaName, cbaVersion,
                cbaActionName, e);
        }

        // Build CDS gRPC request common-header
        CommonHeader commonHeader = CommonHeader.newBuilder()
            .setOriginatorId(CdsActorConstants.ORIGINATOR_ID)
            .setRequestId(requestId)
            .setSubRequestId(subRequestId)
            .build();

        // Build CDS gRPC request action-identifier
        ActionIdentifiers actionIdentifiers = ActionIdentifiers.newBuilder()
            .setBlueprintName(cbaName)
            .setBlueprintVersion(cbaVersion)
            .setActionName(cbaActionName)
            .setMode(CdsActorConstants.CDS_MODE)
            .build();

        // Finally build the ExecutionServiceInput gRPC request object.
        return ExecutionServiceInput.newBuilder()
            .setCommonHeader(commonHeader)
            .setActionIdentifiers(actionIdentifiers)
            .setPayload(struct.build())
            .build();
    }

    /**
     * Ensure that the policy object is supplying the CDS blueprint mandatory parameters.
     */
    private void validatePolicyPayloadForCdsMandatoryParams(Map<String, String> payload) {
        Preconditions.checkState(payload.containsKey(CdsActorConstants.KEY_CBA_NAME) && !Strings
                .isNullOrEmpty(payload.get(CdsActorConstants.KEY_CBA_NAME)),
            "Missing mapping for CDS blueprint name: {} in policy", CdsActorConstants.KEY_CBA_NAME);
        Preconditions.checkState(payload.containsKey(CdsActorConstants.KEY_CBA_VERSION) && !Strings
                .isNullOrEmpty(payload.get(CdsActorConstants.KEY_CBA_VERSION)),
            "Missing mapping for CDS blueprint version: {} in policy", CdsActorConstants.KEY_CBA_VERSION);
        Preconditions.checkState(!Strings.isNullOrEmpty(policy.getRecipe()), "Missing mapping for CDS action name in "
            + "policy.");
    }
}
