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
import java.util.Optional;
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
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actor.cds.beans.ConfigDeployRequest;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.policy.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CDS Actor service-provider implementation. This is a deploy dark feature for El-Alto release.
 */
public class CdsActorServiceProvider implements Actor {

    private static final Logger LOGGER = LoggerFactory.getLogger(CdsActorServiceProvider.class);

    /**
     * {@inheritDoc}.
     */
    @Override
    public String actor() {
        return CdsActorConstants.CDS_ACTOR;
    }

    /**
     * {@inheritDoc}. Note: This is a placeholder for now.
     */
    @Override
    public List<String> recipes() {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}. Note: This is a placeholder for now.
     */
    @Override
    public List<String> recipeTargets(final String recipe) {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}. Note: This is a placeholder for now.
     */
    @Override
    public List<String> recipePayloads(final String recipe) {
        return new ArrayList<>();
    }

    /**
     * Build the CDS ExecutionServiceInput request from the policy object and the AAI enriched parameters. TO-DO: Avoid
     * leaking Exceptions to the Kie Session thread. TBD item for Frankfurt release.
     *
     * @param onset the event that is reporting the alert for policy to perform an action.
     * @param operation the control loop operation specifying the actor, operation, target, etc.
     * @param policy the policy specified from the yaml generated by CLAMP or through Policy API.
     * @param aaiParams Map of enriched AAI attributes in node.attribute notation.
     * @return an Optional ExecutionServiceInput instance if valid else an Optional empty object is returned.
     */
    public Optional<ExecutionServiceInput> constructRequest(VirtualControlLoopEvent onset,
        ControlLoopOperation operation, Policy policy, Map<String, String> aaiParams) {

        // For the current operational TOSCA policy model (yaml) CBA name and version are embedded in the payload
        // section, with the new policy type model being proposed in Frankfurt we will be able to move it out.
        Map<String, String> payload = policy.getPayload();
        if (!validateCdsMandatoryParams(policy)) {
            return Optional.empty();
        }
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
            String requestStr = request.toString();
            Preconditions.checkState(!Strings.isNullOrEmpty(requestStr), "Unable to build "
                + "config-deploy-request from payload parameters: {}", payload);
            JsonFormat.parser().merge(requestStr, struct);
        } catch (InvalidProtocolBufferException e) {
            LOGGER.error("Failed to parse received message. blueprint({}:{}) for action({})", cbaName, cbaVersion,
                cbaActionName, e);
            return Optional.empty();
        }

        // Build CDS gRPC request common-header
        CommonHeader commonHeader = CommonHeader.newBuilder()
            .setOriginatorId(CdsActorConstants.ORIGINATOR_ID)
            .setRequestId(onset.getRequestId().toString())
            .setSubRequestId(operation.getSubRequestId())
            .build();

        // Build CDS gRPC request action-identifier
        ActionIdentifiers actionIdentifiers = ActionIdentifiers.newBuilder()
            .setBlueprintName(cbaName)
            .setBlueprintVersion(cbaVersion)
            .setActionName(cbaActionName)
            .setMode(CdsActorConstants.CDS_MODE)
            .build();

        // Finally build the ExecutionServiceInput gRPC request object.
        ExecutionServiceInput executionServiceInput = ExecutionServiceInput.newBuilder()
            .setCommonHeader(commonHeader)
            .setActionIdentifiers(actionIdentifiers)
            .setPayload(struct.build())
            .build();
        return Optional.of(executionServiceInput);
    }

    private boolean validateCdsMandatoryParams(Policy policy) {
        if (policy == null || policy.getPayload() == null) {
            return false;
        }
        Map<String, String> payload = policy.getPayload();
        String cbaName = payload.get(CdsActorConstants.KEY_CBA_NAME);
        String cbaVersion = payload.get(CdsActorConstants.KEY_CBA_VERSION);
        String cbaActionName = policy.getRecipe();
        return !Strings.isNullOrEmpty(cbaName) && !Strings.isNullOrEmpty(cbaVersion) && !Strings
            .isNullOrEmpty(cbaActionName);
    }

    class CdsActorServiceManager implements CdsProcessorListener {

        private final AtomicReference<String> cdsResponse = new AtomicReference<>();

        /**
         * {@inheritDoc}.
         */
        @Override
        public void onMessage(final ExecutionServiceOutput message) {
            LOGGER.info("Received notification from CDS: {}", message);
            EventType eventType = message.getStatus().getEventType();
            switch (eventType) {
                case EVENT_COMPONENT_FAILURE:
                    cdsResponse.compareAndSet(null, CdsActorConstants.FAILED);
                    break;
                case EVENT_COMPONENT_PROCESSING:
                    cdsResponse.compareAndSet(null, CdsActorConstants.PROCESSING);
                    break;
                case EVENT_COMPONENT_EXECUTED:
                    cdsResponse.compareAndSet(null, CdsActorConstants.SUCCESS);
                    break;
                default:
                    cdsResponse.compareAndSet(null, CdsActorConstants.FAILED);
                    break;
            }
        }

        /**
         * {@inheritDoc}.
         */
        @Override
        public void onError(final Throwable throwable) {
            Status status = Status.fromThrowable(throwable);
            cdsResponse.compareAndSet(null, CdsActorConstants.ERROR);
            LOGGER.error("Failed processing blueprint {} {}", status, throwable);
        }

        /**
         * Send gRPC request to CDS to execute the blueprint.
         *
         * @param cdsClient CDS grpc client object.
         * @param cdsProps CDS properties.
         * @param executionServiceInput a valid CDS grpc request object.
         * @return Status of the CDS request, null if timeout happens or onError is invoked for any reason.
         */
        public String sendRequestToCds(CdsProcessorGrpcClient cdsClient, CdsServerProperties cdsProps,
            ExecutionServiceInput executionServiceInput) {
            try {
                LOGGER.trace("Start CdsActorServiceProvider.executeCdsBlueprintProcessor {}.", executionServiceInput);
                // TO-DO: Handle requests asynchronously once the callback support is added to actors.
                CountDownLatch countDownLatch = cdsClient.sendRequest(executionServiceInput);
                boolean status = countDownLatch.await(cdsProps.getTimeout(), TimeUnit.SECONDS);
                if (!status) {
                    cdsResponse.compareAndSet(null, CdsActorConstants.TIMED_OUT);
                }
                LOGGER.info("CDS response {}", getCdsResponse());
            } catch (InterruptedException ex) {
                LOGGER.error("Caught exception in executeCdsBlueprintProcessor in CdsActorServiceProvider: ", ex);
                cdsResponse.compareAndSet(null, CdsActorConstants.INTERRUPTED);
                Thread.currentThread().interrupt();
            }
            LOGGER.info("Status of the CDS gRPC request is: {}", getCdsResponse());
            return getCdsResponse();
        }

        String getCdsResponse() {
            return cdsResponse.get();
        }
    }
}
