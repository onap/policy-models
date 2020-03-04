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
import javax.ws.rs.client.AsyncInvoker;
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
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.so.SoModelInfo;
import org.onap.policy.so.SoOperationType;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestDetails;

/**
 * Operation to delete a VF Module. This gets the VF count from the A&AI Custom Query
 * response and stores it in the context. It also passes the count-1 to the guard. Once
 * the "delete" completes successfully, it decrements the VF count that's stored in the
 * context.
 */
public class VfModuleDelete extends SoOperation {
    public static final String NAME = "VF Module Delete";

    private static final String PATH_PREFIX = "/";

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public VfModuleDelete(ControlLoopOperationParams params, HttpConfig config) {
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

        // need the VF count
        ControlLoopOperationParams cqParams = params.toBuilder().actor(AaiConstants.ACTOR_NAME)
                        .operation(AaiCqResponse.OPERATION).payload(null).retry(null).timeoutSec(null).build();

        // run Custom Query, extract the VF count, and then run the Guard

        // @formatter:off
        return sequence(() -> params.getContext().obtain(AaiCqResponse.CONTEXT_KEY, cqParams),
                        this::obtainVfCount, this::startGuardAsync);
        // @formatter:on
    }

    @Override
    protected Map<String, Object> makeGuardPayload() {
        Map<String, Object> payload = super.makeGuardPayload();

        // run guard with the proposed vf count
        payload.put(PAYLOAD_KEY_VF_COUNT, getVfCount() - 1);

        return payload;
    }

    @Override
    protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {

        // starting a whole new attempt - reset the count
        resetGetCount();

        Pair<String, SoRequest> pair = makeRequest();
        String path = getPath() + pair.getLeft();
        SoRequest request = pair.getRight();

        Entity<SoRequest> entity = Entity.entity(request, MediaType.APPLICATION_JSON);
        String url = getClient().getBaseUrl() + path;

        logMessage(EventType.OUT, CommInfrastructure.REST, url, request);

        // TODO this may not work!

        AsyncInvoker sender =
                        getClient().getWebTarget().path(path).request().accept(MediaType.APPLICATION_JSON).async();

        // TODO pass "Accept" via headers
        return handleResponse(outcome, url, callback -> sender.method("DELETE", entity, callback));
    }

    /**
     * Increments the VF count that's stored in the context.
     */
    @Override
    protected void successfulCompletion() {
        setVfCount(getVfCount() - 1);
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
        request.setOperationType(SoOperationType.DELETE_VF_MODULE);

        //
        //
        // Do NOT send SO the requestId, they do not support this field
        //
        SoRequestDetails details = new SoRequestDetails();
        request.setRequestDetails(details);
        details.setRelatedInstanceList(null);
        details.setConfigurationParameters(null);

        // cloudConfiguration
        details.setCloudConfiguration(constructCloudConfigurationCq(tenantItem, cloudRegionItem));

        // modelInfo
        details.setModelInfo(soModelInfo);

        // requestInfo
        details.setRequestInfo(constructRequestInfo());

        /*
         * TODO the legacy SO code always passes null for the last argument, though it
         * should be passing the vfModuleInstanceId
         */

        // compute the path
        String path = PATH_PREFIX + vnfServiceItem.getServiceInstanceId() + "/vnfs/" + vnfItem.getVnfId()
                        + "/vfModules/";

        return Pair.of(path, request);
    }
}
