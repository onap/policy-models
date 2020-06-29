/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import lombok.Getter;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.cds.client.CdsProcessorGrpcClient;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actor.aai.AaiCustomQueryOperation;
import org.onap.policy.controlloop.actor.aai.AaiGetPnfOperation;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.actor.cds.request.CdsActionRequest;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.impl.OperationPartial;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.onap.policy.controlloop.policy.TargetType;

/**
 * Operation that uses gRPC to send request to CDS.
 *
 */
@Getter
public class GrpcOperation extends OperationPartial {

    public static final String NAME = "any";

    private static final String AAI_PNF_PREFIX = "pnf.";
    private static final String AAI_VNF_ID_KEY = "generic-vnf.vnf-id";
    private static final String AAI_SERVICE_INSTANCE_ID_KEY = "service-instance.service-instance-id";

    private CdsProcessorGrpcClient client;

    /**
     * Configuration for this operation.
     */
    private final GrpcConfig config;

    /**
     * Function to request the A&AI data appropriate to the target type.
     */
    private final Supplier<CompletableFuture<OperationOutcome>> aaiRequestor;

    /**
     * Function to convert the A&AI data associated with the target type.
     */
    private final Supplier<Map<String, String>> aaiConverter;

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public GrpcOperation(ControlLoopOperationParams params, GrpcConfig config) {
        super(params, config);
        this.config = config;

        if (TargetType.PNF.equals(params.getTarget().getType())) {
            aaiRequestor = this::getPnf;
            aaiConverter = this::convertPnfToAaiProperties;
        } else {
            aaiRequestor = this::getCq;
            aaiConverter = this::convertCqToAaiProperties;
        }
    }

    /**
     * If no timeout is specified, then it returns the operator's configured timeout.
     */
    @Override
    protected long getTimeoutMs(Integer timeoutSec) {
        return (timeoutSec == null || timeoutSec == 0 ? config.getTimeoutMs() : super.getTimeoutMs(timeoutSec));
    }

    /**
     * Ensures that A&AI query has been performed, and runs the guard.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        // run A&AI Query and Guard, in parallel
        return allOf(aaiRequestor, this::startGuardAsync);
    }

    /**
     * Requests the A&AI PNF data.
     *
     * @return a future to get the PNF data
     */
    private CompletableFuture<OperationOutcome> getPnf() {
        ControlLoopOperationParams pnfParams = params.toBuilder().actor(AaiConstants.ACTOR_NAME)
                        .operation(AaiGetPnfOperation.NAME).payload(null).retry(null).timeoutSec(null).build();

        return params.getContext().obtain(AaiGetPnfOperation.getKey(params.getTargetEntity()), pnfParams);
    }

    /**
     * Requests the A&AI Custom Query data.
     *
     * @return a future to get the custom query data
     */
    private CompletableFuture<OperationOutcome> getCq() {
        ControlLoopOperationParams cqParams = params.toBuilder().actor(AaiConstants.ACTOR_NAME)
                        .operation(AaiCustomQueryOperation.NAME).payload(null).retry(null).timeoutSec(null).build();

        return params.getContext().obtain(AaiCqResponse.CONTEXT_KEY, cqParams);
    }

    /**
     * Converts the A&AI PNF data to a map suitable for passing via the "aaiProperties"
     * field in the CDS request.
     *
     * @return a map of the PNF data
     */
    private Map<String, String> convertPnfToAaiProperties() {
        // convert PNF data to a Map
        StandardCoderObject pnf = params.getContext().getProperty(AaiGetPnfOperation.getKey(params.getTargetEntity()));
        Map<String, Object> source = Util.translateToMap(getFullName(), pnf);

        Map<String, String> result = new LinkedHashMap<>();

        for (Entry<String, Object> ent : source.entrySet()) {
            result.put(AAI_PNF_PREFIX + ent.getKey(), ent.getValue().toString());
        }

        return result;
    }

    /**
     * Converts the A&AI Custom Query data to a map suitable for passing via the
     * "aaiProperties" field in the CDS request.
     *
     * @return a map of the custom query data
     */
    private Map<String, String> convertCqToAaiProperties() {
        AaiCqResponse aaicq = params.getContext().getProperty(AaiCqResponse.CONTEXT_KEY);

        Map<String, String> result = new LinkedHashMap<>();

        ServiceInstance serviceInstance = aaicq.getServiceInstance();
        if (serviceInstance == null) {
            throw new IllegalArgumentException("Target service instance could not be found");
        }

        GenericVnf genericVnf = aaicq.getGenericVnfByModelInvariantId(params.getTarget().getResourceID());
        if (genericVnf == null) {
            throw new IllegalArgumentException("Target generic vnf could not be found");
        }

        result.put(AAI_SERVICE_INSTANCE_ID_KEY, serviceInstance.getServiceInstanceId());
        result.put(AAI_VNF_ID_KEY, genericVnf.getVnfId());

        return result;
    }

    @Override
    public void generateSubRequestId(int attempt) {
        setSubRequestId("0");
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        /*
         * construct the request first so that we don't have to clean up the "client" if
         * an exception is thrown
         */
        ExecutionServiceInput request = constructRequest(params);

        CompletableFuture<OperationOutcome> future = new CompletableFuture<>();

        client = new CdsProcessorGrpcClient(new CdsActorServiceManager(outcome, future),
                        config.getCdsServerProperties());

        client.sendRequest(request);

        // arrange to shutdown the client when the request completes
        PipelineControllerFuture<OperationOutcome> controller = new PipelineControllerFuture<>();

        controller.wrap(future).whenCompleteAsync(controller.delayedComplete(), params.getExecutor())
                        .whenCompleteAsync((arg1, arg2) -> client.close(), getBlockingExecutor());

        return controller;
    }

    /**
     * Build the CDS ExecutionServiceInput request from the policy object and the AAI
     * enriched parameters. TO-DO: Avoid leaking Exceptions to the Kie Session thread. TBD
     * item for Frankfurt release.
     *
     * @param params the control loop parameters specifying the onset, payload, etc.
     * @return an ExecutionServiceInput instance.
     */
    public ExecutionServiceInput constructRequest(ControlLoopOperationParams params) {

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
        //
        // Note: that is a future enhancement. For now, the actor is hard-coded to
        // use the A&AI query result specific to the target type
        request.setAaiProperties(aaiConverter.get());

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
            throw new IllegalArgumentException("Failed to embed CDS payload string into the input request. blueprint({"
                            + cbaName + "}:{" + cbaVersion + "}) for action({" + cbaActionName + "})", e);
        }

        // Build CDS gRPC request common-header
        CommonHeader commonHeader = CommonHeader.newBuilder().setOriginatorId(CdsActorConstants.ORIGINATOR_ID)
                        .setRequestId(params.getContext().getEvent().getRequestId().toString())
                        .setSubRequestId(getSubRequestId()).build();

        // Build CDS gRPC request action-identifier
        ActionIdentifiers actionIdentifiers =
                        ActionIdentifiers.newBuilder().setBlueprintName(cbaName).setBlueprintVersion(cbaVersion)
                                        .setActionName(cbaActionName).setMode(CdsActorConstants.CDS_MODE).build();

        // Finally build & return the ExecutionServiceInput gRPC request object.
        return ExecutionServiceInput.newBuilder().setCommonHeader(commonHeader).setActionIdentifiers(actionIdentifiers)
                        .setPayload(struct.build()).build();
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
        if (payload.get(CdsActorConstants.KEY_CBA_NAME) == null
                        || payload.get(CdsActorConstants.KEY_CBA_VERSION) == null) {
            return false;
        }
        return !Strings.isNullOrEmpty(payload.get(CdsActorConstants.KEY_CBA_NAME).toString())
                        && !Strings.isNullOrEmpty(payload.get(CdsActorConstants.KEY_CBA_VERSION).toString())
                        && !Strings.isNullOrEmpty(params.getOperation());
    }
}
