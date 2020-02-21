/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Bell Canada. All rights reserved.
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.cds.client.CdsProcessorGrpcClient;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actor.aai.AaiCustomQueryOperation;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.actor.cds.request.CdsActionRequest;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.impl.OperationPartial;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Operation that uses gRPC to send request to CDS.
 *
 */
@Getter
public class GrpcOperation extends OperationPartial {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrpcOperation.class);

    public static final String NAME = "gRPC";

    private CdsProcessorGrpcClient client;

    /**
     * Configuration for this operation.
     */
    private final GrpcConfig config;

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public GrpcOperation(ControlLoopOperationParams params, GrpcConfig config) {
        super(params, config);
        this.config = config;
    }

    /**
     * If no timeout is specified, then it returns the operator's configured timeout.
     */
    @Override
    protected long getTimeoutMs(Integer timeoutSec) {
        return (timeoutSec == null || timeoutSec == 0 ? config.getTimeoutMs() : super.getTimeoutMs(timeoutSec));
    }

    /**
     * Ensures that A&AI customer query has been performed.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        ControlLoopOperationParams cqParams = params.toBuilder().actor(AaiConstants.ACTOR_NAME)
                        .operation(AaiCustomQueryOperation.NAME).payload(null).retry(null).timeoutSec(null).build();

        return params.getContext().obtain(AaiCqResponse.CONTEXT_KEY, cqParams);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        CompletableFuture<OperationOutcome> future = new CompletableFuture<>();
        client = new CdsProcessorGrpcClient(new CdsActorServiceManager(outcome, future),
                        config.getCdsServerProperties());

        Optional<ExecutionServiceInput> optionalRequest = constructRequest(params);
        if (optionalRequest.isPresent()) {
            logMessage(EventType.OUT, CommInfrastructure.REST, "N/A", optionalRequest.get());
            client.sendRequest(optionalRequest.get());
        } else {
            LOGGER.error("Failed to build CDS input request for params - {}", params);
            outcome.setResult(PolicyResult.FAILURE);
            future.complete(outcome);
        }
        return future;
    }

    /**
     * Build the CDS ExecutionServiceInput request from the policy object and the AAI
     * enriched parameters. TO-DO: Avoid leaking Exceptions to the Kie Session thread. TBD
     * item for Frankfurt release.
     *
     * @param params the control loop parameters specifying the onset, payload, etc.
     * @return an Optional ExecutionServiceInput instance if valid else an Optional empty
     *         object is returned.
     */
    public Optional<ExecutionServiceInput> constructRequest(ControlLoopOperationParams params) {

        // For the current operational TOSCA policy model (yaml) CBA name and version are
        // embedded in the payload
        // section, with the new policy type model being proposed in Frankfurt we will be
        // able to move it out.
        if (!validateCdsMandatoryParams(params)) {
            throw new IllegalArgumentException("missing cds mandatory params -  " + params);
        }
        Map<String, String> payload = convertPayloadMap(params.getPayload());
        String cbaName = payload.get(CdsActorConstants.KEY_CBA_NAME);
        String cbaVersion = payload.get(CdsActorConstants.KEY_CBA_VERSION);

        // Retain only the payload by removing CBA name and version once they are
        // extracted
        // to be put in CDS request header.
        payload.remove(CdsActorConstants.KEY_CBA_NAME);
        payload.remove(CdsActorConstants.KEY_CBA_VERSION);

        // Embed payload from policy to ConfigDeployRequest object, serialize and inject
        // into grpc request.
        String cbaActionName = params.getOperation();
        CdsActionRequest request = new CdsActionRequest();
        request.setPolicyPayload(payload);
        request.setActionName(cbaActionName);
        request.setResolutionKey(UUID.randomUUID().toString());

        // Inject AAI properties into payload map. Offer flexibility to the usecase
        // implementation to inject whatever AAI parameters are of interest to them.
        // E.g. For vFW usecase El-Alto inject service-instance-id, generic-vnf-id as
        // needed by CDS.
        request.setAaiProperties(params.getContext().getEnrichment());

        // Inject any additional event parameters that may be present in the onset event
        if (params.getContext().getEvent().getAdditionalEventParams() != null) {
            request.setAdditionalEventParams(params.getContext().getEvent().getAdditionalEventParams());
        }

        Builder struct = Struct.newBuilder();
        try {
            String requestStr = request.generateCdsPayload();
            Preconditions.checkState(!Strings.isNullOrEmpty(requestStr),
                            "Unable to build " + "config-deploy-request from payload parameters: {}", payload);
            JsonFormat.parser().merge(requestStr, struct);
        } catch (InvalidProtocolBufferException | CoderException e) {
            LOGGER.error("Failed to embed CDS payload string into the input request. blueprint({}:{}) for action({})",
                            cbaName, cbaVersion, cbaActionName, e);
            return Optional.empty();
        }

        // Build CDS gRPC request common-header
        CommonHeader commonHeader = CommonHeader.newBuilder().setOriginatorId(CdsActorConstants.ORIGINATOR_ID)
                        .setRequestId(params.getContext().getEvent().getRequestId().toString())
                        .setSubRequestId(Integer.toString(0)).build();

        // Build CDS gRPC request action-identifier
        ActionIdentifiers actionIdentifiers =
                        ActionIdentifiers.newBuilder().setBlueprintName(cbaName).setBlueprintVersion(cbaVersion)
                                        .setActionName(cbaActionName).setMode(CdsActorConstants.CDS_MODE).build();

        // Finally build the ExecutionServiceInput gRPC request object.
        ExecutionServiceInput executionServiceInput = ExecutionServiceInput.newBuilder().setCommonHeader(commonHeader)
                        .setActionIdentifiers(actionIdentifiers).setPayload(struct.build()).build();
        return Optional.of(executionServiceInput);
    }

    private Map<String, String> convertPayloadMap(Map<String, Object> payload) {
        Map<String, String> convertedPayload = new HashMap<>();
        for (Entry<String, Object> entry : payload.entrySet()) {
            convertedPayload.put(entry.getKey(), entry.getValue().toString());
        }
        return convertedPayload;
    }

    private boolean validateCdsMandatoryParams(ControlLoopOperationParams params) {
        if (params == null || params.getPayload() == null) {
            return false;
        }
        Map<String, Object> payload = params.getPayload();
        String cbaName = payload.get(CdsActorConstants.KEY_CBA_NAME).toString();
        String cbaVersion = payload.get(CdsActorConstants.KEY_CBA_VERSION).toString();
        String cbaActionName = params.getOperation();
        return !Strings.isNullOrEmpty(cbaName) && !Strings.isNullOrEmpty(cbaVersion)
                        && !Strings.isNullOrEmpty(cbaActionName);
    }

    @Override
    public <Q> String logMessage(EventType direction, CommInfrastructure infra, String sink, Q request) {
        String json;
        try {
            if (request == null) {
                json = null;
            } else if (request instanceof String) {
                json = request.toString();
            } else {
                json = makeCoder().encode(request, true);
            }

        } catch (CoderException e) {
            String type = (direction == EventType.IN ? "response" : "request");
            LOGGER.warn("cannot pretty-print {}", type, e);
            json = request.toString();
        }

        LOGGER.info("[{}|{}|{}|]{}{}", direction, "gRPC", sink, NetLoggerUtil.SYSTEM_LS, json);

        return json;
    }
}
