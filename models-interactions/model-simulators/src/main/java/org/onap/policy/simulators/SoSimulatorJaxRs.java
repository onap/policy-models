/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

package org.onap.policy.simulators;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.Setter;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.so.SoRequest;

import io.swagger.annotations.ApiParam;

@Path("/")
public class SoSimulatorJaxRs {

    private static final String REPLACE_ME = "${replaceMe}";
    /**
     * Set of incomplete request IDs. When a POST or DELETE is performed, the new request
     * ID is added to the set. When the request is polled, the ID is removed and a "still
     * running" response is returned. When the request is polled again, it sees that there
     * is no entry and returns a completion indication.
     *
     * <p/>
     * This is static so request IDs are retained across servlets.
     */
    private static final Set<String> incomplete = ConcurrentHashMap.newKeySet();

    /**
     * {@code True} if requests should require polling, {@code false}
     * otherwise.  This is used when junit testing the SO actor.
     */
    @Setter
    private static boolean requirePolling = false;

    /**
     * SO post query.
     *
     * @param serviceInstanceId the service instance Id
     * @param vnfInstanceId the VNF Id
     * @return the response
     */
    @POST
    @Path("/serviceInstantiation/v7/serviceInstances/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/scaleOut")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String soPostQuery(@PathParam("serviceInstanceId") final String serviceInstanceId,
                    @PathParam("vnfInstanceId") final String vnfInstanceId, @ApiParam(required=true) SoRequest request) {

        List<Map<String, String>> useParam = null;
        useParam = request.getRequestDetails().getRequestParameters().getUserParams();
        if(!useParam.isEmpty() && useParam.toString().contains("FAIL")) {
            // this will be treated as a failure by the SO actor as it's missing the request ID
            return "{\"response\" : \"FAILED\" }";
        }
        return (requirePolling ? makeStarted() : makeImmediateComplete());
    }

    /**
     * SO Delete.
     *
     * @param serviceInstanceId the service instance Id
     * @param vnfInstanceId the VNF Id
     * @return the response
     */
    @DELETE
    @Path("/serviceInstances/v7/{serviceInstanceId}/vnfs/{vnfInstanceId}/vfModules/{vfModuleInstanceId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String soDelete(@PathParam("serviceInstanceId") final String serviceInstanceId,
                    @PathParam("vnfInstanceId") final String vnfInstanceId,
                    @PathParam("vfModuleInstanceId") final String vfModuleInstanceId) {

        return (requirePolling ? makeStarted() : makeImmediateComplete());
    }

    /**
     * Poll SO result.
     *
     * @param requestId the ID of the request whose status is to be queried
     * @return the response
     */
    @GET
    @Path("/orchestrationRequests/v5/{requestId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String soGetQuery(@PathParam("requestId") final String requestId) {
        if (incomplete.remove(requestId)) {
            // first poll - return "still running"
            return makeStillRunning(requestId);

        } else {
            return makeComplete(requestId);
        }
    }

    private String makeStarted() {
        String requestId = UUID.randomUUID().toString();

        String response = ResourceUtils.getResourceAsString("org/onap/policy/simulators/so/so.started.json");

        incomplete.add(requestId);

        return response.replace(REPLACE_ME, requestId);
    }

    private String makeImmediateComplete() {
        String response = ResourceUtils.getResourceAsString("org/onap/policy/simulators/so/so.immediate.success.json");
        return response.replace(REPLACE_ME, UUID.randomUUID().toString());
    }

    private String makeComplete(String requestId) {
        String response = ResourceUtils.getResourceAsString("org/onap/policy/simulators/so/so.complete.success.json");
        return response.replace(REPLACE_ME, requestId);
    }

    private String makeStillRunning(String requestId) {
        String response = ResourceUtils.getResourceAsString("org/onap/policy/simulators/so/so.still.running.json");
        return response.replace(REPLACE_ME, requestId);
    }
}
