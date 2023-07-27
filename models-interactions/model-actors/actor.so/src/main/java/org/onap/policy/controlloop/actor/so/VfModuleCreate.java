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

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.aai.domain.yang.ModelVer;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.so.SoModelInfo;
import org.onap.policy.so.SoOperationType;
import org.onap.policy.so.SoRelatedInstance;
import org.onap.policy.so.SoRelatedInstanceListElement;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestDetails;
import org.onap.policy.so.SoRequestParameters;
import org.onap.policy.so.SoResponse;

/**
 * Operation to create a VF Module. When this completes successfully, it increments its VF
 * Count property.
 */
public class VfModuleCreate extends SoOperation {
    public static final String NAME = "VF Module Create";

    private static final String PATH_PREFIX = "/";

    // @formatter:off
    private static final List<String> PROPERTY_NAMES = List.of(
                            OperationProperties.AAI_SERVICE,
                            OperationProperties.AAI_SERVICE_MODEL,
                            OperationProperties.AAI_VNF,
                            OperationProperties.AAI_VNF_MODEL,
                            OperationProperties.AAI_DEFAULT_CLOUD_REGION,
                            OperationProperties.AAI_DEFAULT_TENANT,
                            OperationProperties.DATA_VF_COUNT);
    // @formatter:off

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public VfModuleCreate(ControlLoopOperationParams params, HttpPollingConfig config) {
        super(params, config, PROPERTY_NAMES, params.getTargetEntityIds());

        setUsePolling();
        // ensure we have the necessary parameters
        validateTarget();
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        // starting a whole new attempt - reset the count
        resetPollCount();

        Pair<String, SoRequest> pair = makeRequest();
        String path = getPath() + pair.getLeft();
        SoRequest request = pair.getRight();

        String url = getClient().getBaseUrl() + path;

        String strRequest = prettyPrint(request);
        logMessage(EventType.OUT, CommInfrastructure.REST, url, strRequest);

        Entity<String> entity = Entity.entity(strRequest, MediaType.APPLICATION_JSON);

        Map<String, Object> headers = createSimpleHeaders();

        return handleResponse(outcome, url, callback -> getClient().post(callback, path, entity, headers));
    }

    /**
     * Increments the VF count that's stored in the context, if the request was
     * successful.
     */
    @Override
    protected Status detmStatus(Response rawResponse, SoResponse response) {
        var status = super.detmStatus(rawResponse, response);

        if (status == Status.SUCCESS) {
            setVfCount(getVfCount() + 1);
        }

        return status;
    }

    /**
     * Makes a request.
     *
     * @return a pair containing the request URL and the new request
     */
    protected Pair<String, SoRequest> makeRequest() {
        final var soModelInfo = prepareSoModelInfo();
        final var vnfItem = getVnfItem();
        final var vnfServiceItem = getServiceInstance();
        final var tenantItem = getDefaultTenant();
        final var cloudRegionItem = getDefaultCloudRegion();
        final ModelVer vnfModel = getVnfModel();
        final ModelVer vnfServiceModel = getServiceModel();

        var request = new SoRequest();
        request.setOperationType(SoOperationType.SCALE_OUT);

        //
        //
        // Do NOT send SO the requestId, they do not support this field
        //
        request.setRequestDetails(new SoRequestDetails());
        request.getRequestDetails().setRequestParameters(new SoRequestParameters());
        request.getRequestDetails().getRequestParameters().setUserParams(null);

        // cloudConfiguration
        request.getRequestDetails().setCloudConfiguration(constructCloudConfiguration(tenantItem, cloudRegionItem));

        // modelInfo
        request.getRequestDetails().setModelInfo(soModelInfo);

        // requestInfo
        request.getRequestDetails().setRequestInfo(constructRequestInfo());
        request.getRequestDetails().getRequestInfo().setInstanceName("vfModuleName");

        // relatedInstanceList
        var relatedInstanceListElement1 = new SoRelatedInstanceListElement();
        var relatedInstanceListElement2 = new SoRelatedInstanceListElement();
        relatedInstanceListElement1.setRelatedInstance(new SoRelatedInstance());
        relatedInstanceListElement2.setRelatedInstance(new SoRelatedInstance());

        // Service Item
        relatedInstanceListElement1.getRelatedInstance().setInstanceId(vnfServiceItem.getServiceInstanceId());
        relatedInstanceListElement1.getRelatedInstance().setModelInfo(new SoModelInfo());
        relatedInstanceListElement1.getRelatedInstance().getModelInfo().setModelType("service");
        relatedInstanceListElement1.getRelatedInstance().getModelInfo()
                        .setModelInvariantId(vnfServiceItem.getModelInvariantId());
        relatedInstanceListElement1.getRelatedInstance().getModelInfo()
                        .setModelVersionId(vnfServiceItem.getModelVersionId());
        relatedInstanceListElement1.getRelatedInstance().getModelInfo().setModelName(vnfModel.getModelName());
        relatedInstanceListElement1.getRelatedInstance().getModelInfo().setModelVersion(vnfModel.getModelVersion());

        // VNF Item
        relatedInstanceListElement2.getRelatedInstance().setInstanceId(vnfItem.getVnfId());
        relatedInstanceListElement2.getRelatedInstance().setModelInfo(new SoModelInfo());
        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelType("vnf");
        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                        .setModelInvariantId(vnfItem.getModelInvariantId());
        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelVersionId(vnfItem.getModelVersionId());

        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelName(vnfServiceModel.getModelName());
        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                        .setModelVersion(vnfServiceModel.getModelVersion());

        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                        .setModelCustomizationId(vnfItem.getModelCustomizationId());

        // Insert the Service Item and VNF Item
        request.getRequestDetails().getRelatedInstanceList().add(relatedInstanceListElement1);
        request.getRequestDetails().getRelatedInstanceList().add(relatedInstanceListElement2);

        // Request Parameters
        buildRequestParameters().ifPresent(request.getRequestDetails()::setRequestParameters);

        // Configuration Parameters
        buildConfigurationParameters().ifPresent(request.getRequestDetails()::setConfigurationParameters);

        // compute the path
        String svcId = getRequiredText("service instance ID", vnfServiceItem.getServiceInstanceId());
        String path = PATH_PREFIX + svcId + "/vnfs/" + vnfItem.getVnfId() + "/vfModules/scaleOut";

        return Pair.of(path, request);
    }
}
