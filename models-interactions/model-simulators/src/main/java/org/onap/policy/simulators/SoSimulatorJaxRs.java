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
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestReferences;
import org.onap.policy.so.SoRequestStatus;
import org.onap.policy.so.SoResponse;


@Path("/")
public class SoSimulatorJaxRs {
    private final Coder coder = new StandardCoder();

    /**
     * Set of incomplete request IDs. When a POST or DELETE is performed, the new request
     * ID is added to the set. When the request is polled, the ID is removed and an empty
     * response is returned. When the request is polled again, it sees that there is no
     * entry and returns a completion indication.
     *
     * <p/>
     * This is static so request IDs are retained across servlets.
     */
    private static final Set<String> incomplete = ConcurrentHashMap.newKeySet();

    /**
     * {@code True} if the initial request should yield an incomplete, {@code false}
     * otherwise.  This is used when junit testing the SO actor.
     */
    @Setter
    private static boolean yieldIncomplete = false;

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
                    @PathParam("vnfInstanceId") final String vnfInstanceId) throws CoderException {

        return coder.encode(yieldIncomplete ? makeIncomplete() : makeComplete(UUID.randomUUID().toString()));
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
                    @PathParam("vfModuleInstanceId") final String vfModuleInstanceId) throws CoderException {

        return coder.encode(yieldIncomplete ? makeIncomplete() : makeComplete(UUID.randomUUID().toString()));
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
    public String soGetQuery(@PathParam("requestId") final String requestId) throws CoderException {
        if (incomplete.remove(requestId)) {
            // first poll - return empty response
            return coder.encode(new SoResponse());

        } else {
            return coder.encode(makeComplete(requestId));
        }
    }

    private SoResponse makeIncomplete() {
        final SoResponse response = makeResponse();
        response.getRequest().getRequestStatus().setRequestState("INCOMPLETE");

        incomplete.add(response.getRequestReferences().getRequestId());

        return response;
    }

    private SoResponse makeComplete(String requestId) {
        final SoResponse response = makeResponse();

        response.getRequest().getRequestStatus().setRequestState("COMPLETE");
        response.getRequest().setRequestId(UUID.fromString(requestId));

        return response;
    }

    private SoResponse makeResponse() {
        final SoRequest request = new SoRequest();
        final SoRequestStatus requestStatus = new SoRequestStatus();
        request.setRequestStatus(requestStatus);
        request.setRequestId(UUID.randomUUID());

        final SoResponse response = new SoResponse();

        final SoRequestReferences requestReferences = new SoRequestReferences();
        final String requestId = UUID.randomUUID().toString();
        requestReferences.setRequestId(requestId);
        response.setRequestReferences(requestReferences);

        response.setRequest(request);

        return response;
    }
}
