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

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
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
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.controlloop.actorserviceprovider.pipeline.PipelineControllerFuture;
import org.onap.policy.so.SoModelInfo;
import org.onap.policy.so.SoOperationType;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestDetails;
import org.onap.policy.so.SoResponse;

/**
 * Operation to delete a VF Module. This gets the VF count from the A&AI Custom Query
 * response and stores it in the context. It also passes the count-1 to the guard. Once
 * the "delete" completes successfully, it decrements the VF count that's stored in the
 * context.
 */
public class VfModuleDelete extends SoOperation {
    public static final String NAME = "VF Module Delete";

    private static final String PATH_PREFIX = "/";

    // @formatter:off
    private static final List<String> PROPERTY_NAMES = List.of(
                            OperationProperties.AAI_MODEL_SERVICE,
                            OperationProperties.AAI_MODEL_VNF,
                            OperationProperties.AAI_MODEL_CLOUD_REGION,
                            OperationProperties.AAI_MODEL_TENANT,
                            OperationProperties.DATA_VF_COUNT);
    // @formatter:on

    /**
     * Constructs the object.
     *
     * @param params operation parameters
     * @param config configuration for this operation
     */
    public VfModuleDelete(ControlLoopOperationParams params, HttpPollingConfig config) {
        super(params, config, PROPERTY_NAMES);

        // ensure we have the necessary parameters
        validateTarget();
    }

    /**
     * Ensures that A&AI custom query has been performed, and then runs the guard.
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
        resetPollCount();

        Pair<String, SoRequest> pair = makeRequest();
        SoRequest request = pair.getRight();
        String url = getPath() + pair.getLeft();

        String strRequest = prettyPrint(request);
        logMessage(EventType.OUT, CommInfrastructure.REST, url, strRequest);

        Map<String, Object> headers = createSimpleHeaders();

        // @formatter:off
        return handleResponse(outcome, url,
            callback -> delete(url, headers, MediaType.APPLICATION_JSON, strRequest, callback));
        // @formatter:on
    }

    /**
     * Issues an HTTP "DELETE" request, containing a request body, using the java built-in
     * HttpClient, as the JerseyClient does not support it. This will add the content-type
     * and authorization headers, so they should not be included within "headers".
     *
     * @param uri URI suffix, to be appended to the URI prefix
     * @param headers headers to be included
     * @param contentType content type of the request
     * @param request request to be posted
     * @param callback response callbacks
     * @return a future to await the response. Note: it's untested whether canceling this
     *         future will actually cancel the underlying HTTP request
     */
    protected CompletableFuture<Response> delete(String uri, Map<String, Object> headers, String contentType,
                    String request, InvocationCallback<Response> callback) {
        // TODO move to HttpOperation

        final String url = getClient().getBaseUrl() + uri;

        Builder builder = HttpRequest.newBuilder(URI.create(url));
        builder = builder.header("Content-type", contentType);
        builder = addAuthHeader(builder);

        for (Entry<String, Object> header : headers.entrySet()) {
            builder = builder.header(header.getKey(), header.getValue().toString());
        }

        PipelineControllerFuture<Response> controller = new PipelineControllerFuture<>();

        HttpRequest req = builder.method("DELETE", BodyPublishers.ofString(request)).build();

        CompletableFuture<HttpResponse<String>> future = makeHttpClient().sendAsync(req, BodyHandlers.ofString());

        // propagate "cancel" to the future
        controller.add(future);

        future.thenApply(response -> new RestManagerResponse(response.statusCode(), response.body(), getCoder()))
                        .whenComplete((resp, thrown) -> {
                            if (thrown != null) {
                                callback.failed(thrown);
                                controller.completeExceptionally(thrown);
                            } else {
                                callback.completed(resp);
                                controller.complete(resp);
                            }
                        });

        return controller;
    }

    /**
     * Adds the authorization header to the HTTP request, if configured.
     *
     * @param builder request builder to which the header should be added
     * @return the builder
     */
    protected Builder addAuthHeader(Builder builder) {
        // TODO move to HttpOperation
        final HttpClient client = getClient();
        String username = client.getUserName();
        if (StringUtils.isBlank(username)) {
            return builder;
        }

        String password = client.getPassword();
        if (password == null) {
            password = "";
        }

        String encoded = username + ":" + password;
        encoded = Base64.getEncoder().encodeToString(encoded.getBytes(StandardCharsets.UTF_8));
        return builder.header("Authorization", "Basic " + encoded);
    }


    /**
     * Decrements the VF count that's stored in the context, if the request was
     * successful.
     */
    @Override
    protected Status detmStatus(Response rawResponse, SoResponse response) {
        Status status = super.detmStatus(rawResponse, response);

        if (status == Status.SUCCESS) {
            setVfCount(getVfCount() - 1);
        }

        return status;
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
                        + "/vfModules/null";

        return Pair.of(path, request);
    }

    // these may be overridden by junit tests

    protected java.net.http.HttpClient makeHttpClient() {
        return java.net.http.HttpClient.newHttpClient();
    }
}
