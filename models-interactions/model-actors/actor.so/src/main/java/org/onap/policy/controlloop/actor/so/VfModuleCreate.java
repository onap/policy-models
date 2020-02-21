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

package org.onap.policy.controlloop.actor.so;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.controlloop.actor.aai.AaiCustomQueryOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.so.SoCloudConfiguration;
import org.onap.policy.so.SoModelInfo;
import org.onap.policy.so.SoOperationType;
import org.onap.policy.so.SoRelatedInstance;
import org.onap.policy.so.SoRelatedInstanceListElement;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestDetails;
import org.onap.policy.so.SoRequestParameters;

/**
 * Operation to create a VF Module. This gets the VF count from the A&AI Custom Query
 * response and stores it in the context. It also passes the count+1 to the guard. Once
 * the "create" completes successfully, it bumps the VF count that's stored in the
 * context.
 * <p/>
 * Note: currently, this only supports storing the count for a single target VF.
 */
public class VfModuleCreate extends SoOperation {
    public static final String NAME = "VF Module Create";

    public static final String PAYLOAD_KEY_VF_COUNT = "vfCount";

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public VfModuleCreate(ControlLoopOperationParams params, HttpConfig config) {
        super(params, config);

        // ensure we have the necessary parameters
        validateTarget();
    }

    /**
     * Ensures that A&AI customer query has been performed, and then runs the guard.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected CompletableFuture<OperationOutcome> startPreprocessorAsync() {
        if (params.getContext().contains(SoConstants.CONTEXT_KEY_VF_COUNT)) {
            return startGuardAsync();
        }

        // need the VF count
        ControlLoopOperationParams cqParams = params.toBuilder().actor(AaiConstants.ACTOR_NAME)
                        .operation(AaiCustomQueryOperation.NAME).payload(null).retry(null).timeoutSec(null).build();

        // run Custom Query, extract the VF count, and then run the Guard
        return sequence(() -> params.getContext().obtain(AaiCqResponse.CONTEXT_KEY, cqParams),
                        this::storeVfCountRunGuard);
    }

    @Override
    protected Map<String, Object> makeGuardPayload() {
        Map<String, Object> payload = super.makeGuardPayload();

        int vfcount = params.getContext().getProperty(SoConstants.CONTEXT_KEY_VF_COUNT);

        // run guard with the proposed vf count
        payload.put(PAYLOAD_KEY_VF_COUNT, vfcount + 1);

        return payload;
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        // starting a whole new attempt - reset the count
        resetGetCount();

        Pair<String, SoRequest> pair = makeRequest();
        String path = pair.getLeft();
        SoRequest request = pair.getRight();

        Entity<SoRequest> entity = Entity.entity(request, MediaType.APPLICATION_JSON);
        String url = getClient().getBaseUrl() + path;

        logMessage(EventType.OUT, CommInfrastructure.REST, url, request);

        // TODO should this use "path" or the full "url"?

        return handleResponse(outcome, url, callback -> getClient().post(callback, path, entity, null));
    }

    /**
     * Increments the VF count that's stored in the context.
     */
    @Override
    protected void successfulCompletion() {
        int vfcount = params.getContext().getProperty(SoConstants.CONTEXT_KEY_VF_COUNT);
        params.getContext().setProperty(SoConstants.CONTEXT_KEY_VF_COUNT, vfcount + 1);
    }

    /**
     * Makes a request.
     *
     * @return a pair containing the request URL and the new request
     */
    protected Pair<String, SoRequest> makeRequest() {
        final AaiCqResponse aaiCqResponse = params.getContext().getProperty(AaiCqResponse.CONTEXT_KEY);
        final SoModelInfo soModelInfo = prepareSoModelInfo();
        final GenericVnf vnfItem = getVnfItem(aaiCqResponse, soModelInfo);
        final ServiceInstance vnfServiceItem = getServiceInstance(aaiCqResponse);
        final Tenant tenantItem = getDefaultTenant(aaiCqResponse);
        final CloudRegion cloudRegionItem = getDefaultCloudRegion(aaiCqResponse);

        SoRequest request = new SoRequest();
        request.setOperationType(SoOperationType.SCALE_OUT);

        //
        //
        // Do NOT send SO the requestId, they do not support this field
        //
        request.setRequestDetails(new SoRequestDetails());
        request.getRequestDetails().setRequestParameters(new SoRequestParameters());
        request.getRequestDetails().getRequestParameters().setUserParams(null);

        // cloudConfiguration
        request.getRequestDetails().setCloudConfiguration(constructCloudConfigurationCq(tenantItem, cloudRegionItem));

        // modelInfo
        request.getRequestDetails().setModelInfo(soModelInfo);

        // requestInfo
        request.getRequestDetails().setRequestInfo(constructRequestInfo());
        request.getRequestDetails().getRequestInfo().setInstanceName("vfModuleName");

        // relatedInstanceList
        SoRelatedInstanceListElement relatedInstanceListElement1 = new SoRelatedInstanceListElement();
        SoRelatedInstanceListElement relatedInstanceListElement2 = new SoRelatedInstanceListElement();
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
        relatedInstanceListElement1.getRelatedInstance().getModelInfo().setModelName(
                        aaiCqResponse.getModelVerByVersionId(vnfServiceItem.getModelVersionId()).getModelName());
        relatedInstanceListElement1.getRelatedInstance().getModelInfo().setModelVersion(
                        aaiCqResponse.getModelVerByVersionId(vnfServiceItem.getModelVersionId()).getModelVersion());

        // VNF Item
        relatedInstanceListElement2.getRelatedInstance().setInstanceId(vnfItem.getVnfId());
        relatedInstanceListElement2.getRelatedInstance().setModelInfo(new SoModelInfo());
        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelType("vnf");
        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                        .setModelInvariantId(vnfItem.getModelInvariantId());
        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelVersionId(vnfItem.getModelVersionId());

        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                        .setModelName(aaiCqResponse.getModelVerByVersionId(vnfItem.getModelVersionId()).getModelName());
        relatedInstanceListElement2.getRelatedInstance().getModelInfo().setModelVersion(
                        aaiCqResponse.getModelVerByVersionId(vnfItem.getModelVersionId()).getModelVersion());

        relatedInstanceListElement2.getRelatedInstance().getModelInfo()
                        .setModelCustomizationId(vnfItem.getModelCustomizationId());

        // Insert the Service Item and VNF Item
        request.getRequestDetails().getRelatedInstanceList().add(relatedInstanceListElement1);
        request.getRequestDetails().getRelatedInstanceList().add(relatedInstanceListElement2);

        // Request Parameters
        request.getRequestDetails().setRequestParameters(buildRequestParameters());

        // Configuration Parameters
        request.getRequestDetails().setConfigurationParameters(buildConfigurationParameters());

        // compute the path
        String path = "/serviceInstantiation/v7/serviceInstances/" + vnfServiceItem.getServiceInstanceId() + "/vnfs/"
                        + vnfItem.getVnfId() + "/vfModules/scaleOut";

        return Pair.of(path, request);
    }

    /**
     * Construct cloudConfiguration for the SO requestDetails. Overridden for custom
     * query.
     *
     * @param tenantItem tenant item from A&AI named-query response
     * @return SO cloud configuration
     */
    private SoCloudConfiguration constructCloudConfigurationCq(Tenant tenantItem, CloudRegion cloudRegionItem) {
        SoCloudConfiguration cloudConfiguration = new SoCloudConfiguration();
        cloudConfiguration.setTenantId(tenantItem.getTenantId());
        cloudConfiguration.setLcpCloudRegionId(cloudRegionItem.getCloudRegionId());
        return cloudConfiguration;
    }
}
