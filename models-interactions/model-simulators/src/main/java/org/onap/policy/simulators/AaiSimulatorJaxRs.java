/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2018, 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2023 Nordix Foundation.
 * Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.common.utils.services.Registry;

@Path("/aai")
public class AaiSimulatorJaxRs {

    private static final String DOT_JSON = ".json";
    private static final String DEFAULT_RESOURCE_LOCATION = "org/onap/policy/simulators/aai/";
    private static final String INVALID_VNF_FILE_NAME = "invalid-vnf";
    private static final String INVALID_PNF_FILE_NAME = "invalid-pnf";

    /**
     * A&AI get query.
     *
     * @return the result
     */
    @GET
    @Path("/{version:v16|v21}/search/nodes-query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String aaiGetVserverQuery(@QueryParam("filter") final String filter) {
        if (filter.equals("vserver-name:EQUALS:f953c499-4b1e-426b-8c6d-e9e9f1fc730f")
            || filter.equals("vserver-name:EQUALS:Ete_vFWCLvFWSNK_7ba1fbde_0")
            || filter.equals("vserver-name:EQUALS:OzVServer")
            || filter.equals("vserver-name:EQUALS:testVserverName")) {
            return "{\"result-data\":[{\"resource-type\": \"vserver\",\"resource-link\":\"/aai/v15/"
                + "cloud-infrastructure/cloud-regions/cloud-region/CloudOwner/RegionOne/tenants"
                + "/tenant/3f2aaef74ecb4b19b35e26d0849fe9a2/vservers/vserver/"
                + "6c3b3714-e36c-45af-9f16-7d3a73d99497\"}]}";
        } else {
            return null;
        }
    }

    /**
     * A&AI put query.
     *
     * @param req the request
     * @return the response
     */
    @PUT
    @Path("/{version:v16|v21}/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public Response aaiPutQuery(final String req) {
        return getResponse("AaiCqResponse", "invalid-cq");
    }

    /**
     * A&AI get PNF query using pnfName.
     *
     * @return the result
     */
    @GET
    @Path("/{version:v16|v21}/network/pnfs/pnf/{pnfName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public Response aaiGetPnfUsingPnfName(@PathParam("pnfName") final String pnfName) {
        return getResponse(pnfName, INVALID_PNF_FILE_NAME);
    }

    /**
     * A&AI get PNF query using pnf-id.
     *
     * @return the result
     */
    @GET
    @Path("/{version:v16|v21}/network/pnfs/pnf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public Response aaiGetPnfUsingPnfId(@QueryParam("pnf-id") final String pnfId) {
        return getResponse(pnfId, INVALID_PNF_FILE_NAME);
    }

    /**
     * A&AI get VNF query using vnf-id.
     *
     * @return the result
     */
    @GET
    @Path("/{version:v16|v21}/network/generic-vnfs/generic-vnf/{vnfId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public Response aaiGetVnfUsingVnfId(@PathParam("vnfId") final String vnfId) {
        return getResponse(vnfId, INVALID_VNF_FILE_NAME);
    }

    /**
     * A&AI get VNF query using vnf-name.
     *
     * @return the result
     */
    @GET
    @Path("/{version:v16|v21}/network/generic-vnfs/generic-vnf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public Response aaiGetVnfUsingVnfName(@QueryParam("vnf-name") final String vnfName) {
        return getResponse(vnfName, INVALID_VNF_FILE_NAME);
    }

    private Response getResponse(final String expectedFileName, final String defaultFileName) {
        String resourceLocation = getResourceLocation();
        var responseString = ResourceUtils.getResourceAsString(resourceLocation + expectedFileName + DOT_JSON);
        if (null == responseString) {
            // if a response file is not found in expected location, look for it in default location
            responseString = ResourceUtils.getResourceAsString(DEFAULT_RESOURCE_LOCATION + expectedFileName + DOT_JSON);
        }
        if (null != responseString) {
            return Response.ok(responseString).build();
        } else {
            // if a response file is not available in expected or default location, return an appropriate 404 response
            responseString = ResourceUtils.getResourceAsString(DEFAULT_RESOURCE_LOCATION + defaultFileName + DOT_JSON);
            return Response.status(Response.Status.NOT_FOUND).entity(responseString).build();
        }
    }

    private String getResourceLocation() {
        return Registry.getOrDefault(this.getClass().getName() + "_RESOURCE_LOCATION", String.class,
            DEFAULT_RESOURCE_LOCATION);
    }
}
