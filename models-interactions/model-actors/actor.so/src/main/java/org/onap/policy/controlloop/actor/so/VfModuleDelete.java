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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.rest.RestManager;
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
        SoRequest request = pair.getRight();
        String url = getPath() + pair.getLeft();

        logMessage(EventType.OUT, CommInfrastructure.REST, url, request);

        Map<String, Object> headers = createSimpleHeaders();

        // @formatter:off
        return handleResponse(outcome, url,
            callback -> delete(url, headers, MediaType.APPLICATION_JSON, request, callback));
        // @formatter:on
    }

    /**
     * Issues an HTTP "DELETE" request, containing a request body, using the blocking
     * executor.
     *
     * @param <Q> request type
     * @param uri URI suffix, to be appended to the URI prefix
     * @param headers headers to be included
     * @param contentType content type of the request
     * @param request request to be posted
     * @param callback response callbacks
     * @return a future to await the response. Note: canceling this future will not
     *         cancel the request
     */
    protected <Q> CompletableFuture<Response> delete(String uri, Map<String, Object> headers, String contentType,
                    Q request, InvocationCallback<Response> callback) {
        // TODO move to HttpOperation

        String body;
        try {
            if (request instanceof String) {
                body = request.toString();
            } else {
                body = makeCoder().encode(request);
            }
        } catch (CoderException e) {
            throw new IllegalArgumentException("cannot encode request", e);
        }


        final HttpClient client = getClient();

        String url = getUriPrefix() + uri;
        Map<String, String> headers2 = new LinkedHashMap<>();
        headers.forEach((key, value) -> headers2.put(key, value.toString()));

        CompletableFuture<Response> future = new CompletableFuture<>();

        // @formatter:off
        getBlockingExecutor().execute(() -> invokeRestManager(callback, future, () ->
            makeRestManager().delete(url, client.getUserName(), client.getPassword(), headers2, contentType, body)));
        // @formatter:on

        return future;
    }

    /**
     * Invokes a RestManager command.
     *
     * @param command command to be invoked
     * @param callback response callbacks
     * @param future future to be completed when the request completes
     */
    protected void invokeRestManager(InvocationCallback<Response> callback, CompletableFuture<Response> future,
                    Supplier<org.onap.policy.rest.RestManager.Pair<Integer, String>> command) {
        // TODO move to HttpOperation
        try {
            org.onap.policy.rest.RestManager.Pair<Integer, String> result = command.get();
            if (result == null) {
                throw new IOException("request failed");
            } else {
                future.complete(new RestManagerResponse(result, makeCoder()));
            }

        } catch (RuntimeException | IOException e) {
            future.completeExceptionally(e);
        }

        future.whenComplete((resp, thrown) -> {
            if (thrown != null) {
                callback.failed(thrown);
            } else {
                callback.completed(resp);
            }
        });
    }

    /**
     * Gets the URI prefix (e.g., "http://localhost:80/${baseUrl}").
     *
     * @return the URI prefix
     */
    protected String getUriPrefix() {
        // TODO move to HttpOperation
        final HttpClient client = getClient();

        StringBuilder sb = new StringBuilder();
        sb.append(client.isHttps() ? "https://" : "http://");
        sb.append(client.getHostname());
        sb.append(':');
        sb.append(client.getPort());
        sb.append(client.getBaseUrl());

        return sb.toString();
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

    // these may be overridden by junit tests

    protected RestManager makeRestManager() {
        return new RestManager();
    }
}
