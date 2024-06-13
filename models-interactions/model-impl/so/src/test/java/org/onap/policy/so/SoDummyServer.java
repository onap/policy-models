/*-
 * ============LICENSE_START=======================================================
 * so
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018-2019 AT&T. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.so;

import com.google.gson.Gson;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Path("/SO")
class SoDummyServer {

    private static final String ONGOING = "ONGOING";
    private static int postMessagesReceived = 0;
    private static int putMessagesReceived = 0;
    private static int statMessagesReceived = 0;
    private static int getMessagesReceived = 0;
    private static int deleteMessagesReceived = 0;

    private static Map<String, SoResponse> ongoingRequestMap = new ConcurrentHashMap<>();

    /**
     * Stats method.
     *
     * @return response
     */
    @GET
    @Path("/Stats")
    public Response serviceGetStats() {
        statMessagesReceived++;
        return Response.status(200).entity("{\"GET\": " + getMessagesReceived + ",\"STAT\": " + statMessagesReceived
                + ",\"POST\": " + postMessagesReceived + ",\"PUT\": " + putMessagesReceived
                + ",\"DELETE\": " + deleteMessagesReceived + "}").build();

    }

    /**
     * Get stat type.
     *
     * @param statType the stat type
     * @return http response
     */
    @GET
    @Path("/OneStat/{statType}")
    public Response serviceGetStat(@PathParam("statType") final String statType) {
        statMessagesReceived++;
        return Response.status(200).entity("{\"TYPE\": " + statType + "}").build();
    }

    /**
     * Post to service instantiation.
     *
     * @param jsonString string to send
     * @return http response
     */
    @POST
    @Path("/serviceInstantiation/v7")
    public Response servicePostRequest(final String jsonString) {
        postMessagesReceived++;
        return buildResponse(jsonString);
    }

    /**
     * Post.
     *
     * @param serviceInstanceId service instance id
     * @param vnfInstanceId vnf instance id
     * @param jsonString json body
     * @return http response
     */
    @POST
    @Path("/serviceInstantiation/v7/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/scaleOut")
    public Response servicePostRequestVfModules(@PathParam("serviceInstanceId") final String serviceInstanceId,
                    @PathParam("vnfInstanceId") final String vnfInstanceId, final String jsonString) {
        postMessagesReceived++;
        return buildResponse(jsonString);
    }

    /**
     * Get instance ID.
     *
     * @param nsInstanceId node instance id
     * @return http response
     */
    @GET
    @Path("/orchestrationRequests/v5/{nsInstanceId}")
    public Response soRequestStatus(@PathParam("nsInstanceId") final String nsInstanceId) {

        SoResponse response = ongoingRequestMap.get(nsInstanceId);

        int iterationsLeft = Integer.parseInt(response.getRequest().getRequestScope());
        if (--iterationsLeft > 0) {
            response.getRequest().setRequestScope(Integer.toString(iterationsLeft));
            String responseString = new Gson().toJson(response, SoResponse.class);
            return Response.status(response.getHttpResponseCode()).entity(responseString).build();
        }

        ongoingRequestMap.remove(nsInstanceId);

        if ("ReturnBadAfterWait".equals(response.getRequest().getRequestType())) {
            return Response.status(400).build();
        }

        response.getRequest().getRequestStatus().setRequestState("COMPLETE");
        response.getRequest().setRequestScope("0");
        response.setHttpResponseCode(200);
        String responseString = new Gson().toJson(response, SoResponse.class);
        return Response.status(response.getHttpResponseCode()).entity(responseString).build();
    }

    /**
     * Delete.
     *
     * @param serviceInstanceId service instance id
     * @param vnfInstanceId vnf instance id
     * @param vfModuleInstanceId vf module instance id
     * @param jsonString json body
     * @return http response
     */
    @DELETE
    @Path("/serviceInstances/v7/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfModuleInstanceId}")
    public Response serviceDeleteRequestVfModules(
            @PathParam("serviceInstanceId") final String serviceInstanceId,
            @PathParam("vnfInstanceId") final String vnfInstanceId,
            @PathParam("vfModuleInstanceId") final String vfModuleInstanceId,
            final String jsonString) {
        deleteMessagesReceived++;
        return buildResponse(jsonString);
    }

    private Response buildResponse(String jsonString) {
        if (jsonString == null) {
            return Response.status(400).build();
        }

        SoRequest request = new Gson().fromJson(jsonString, SoRequest.class);

        if (request == null) {
            return Response.status(400).build();
        }

        if (request.getRequestType() == null) {
            return Response.status(400).build();
        }

        if ("ReturnBadJson".equals(request.getRequestType())) {
            return Response.status(200)
                    .entity("{\"GET\": , " + getMessagesReceived + ",\"STAT\": " + statMessagesReceived
                            + ",\"POST\":" + " , " + postMessagesReceived + ",\"PUT\": " + putMessagesReceived
                            + ",\"DELETE\": " + deleteMessagesReceived + "}").build();
        }

        SoResponse response = new SoResponse();
        response.setRequest(request);
        response.setRequestReferences(new SoRequestReferences());
        response.getRequestReferences().setRequestId(request.getRequestId().toString());

        if ("ReturnCompleted".equals(request.getRequestType())) {
            response.getRequest().getRequestStatus().setRequestState("COMPLETE");
            response.setHttpResponseCode(200);
            String responseString = new Gson().toJson(response, SoResponse.class);
            return Response.status(response.getHttpResponseCode())
                    .entity(responseString)
                    .build();
        }

        if ("ReturnFailed".equals(request.getRequestType())) {
            response.getRequest().getRequestStatus().setRequestState("FAILED");
            response.setHttpResponseCode(200);
            String responseString = new Gson().toJson(response, SoResponse.class);
            return Response.status(response.getHttpResponseCode())
                    .entity(responseString)
                    .build();
        }

        if ("ReturnOnging202".equals(request.getRequestType())) {
            ongoingRequestMap.put(request.getRequestId().toString(), response);

            response.getRequest().getRequestStatus().setRequestState(ONGOING);
            response.setHttpResponseCode(202);
            String responseString = new Gson().toJson(response, SoResponse.class);
            return Response.status(response.getHttpResponseCode())
                    .entity(responseString)
                    .build();
        }

        if ("ReturnOnging200".equals(request.getRequestType())) {
            ongoingRequestMap.put(request.getRequestId().toString(), response);

            response.getRequest().getRequestStatus().setRequestState(ONGOING);
            response.setHttpResponseCode(200);
            String responseString = new Gson().toJson(response, SoResponse.class);
            return Response.status(response.getHttpResponseCode())
                    .entity(responseString)
                    .build();
        }

        if ("ReturnBadAfterWait".equals(request.getRequestType())) {
            ongoingRequestMap.put(request.getRequestId().toString(), response);

            response.getRequest().getRequestStatus().setRequestState(ONGOING);
            response.setHttpResponseCode(200);
            String responseString = new Gson().toJson(response, SoResponse.class);
            return Response.status(response.getHttpResponseCode())
                    .entity(responseString)
                    .build();
        }
        return null;
    }
}
