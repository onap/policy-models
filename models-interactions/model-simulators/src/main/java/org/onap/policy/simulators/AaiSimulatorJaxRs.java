/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2018, 2020 AT&T Intellectual Property. All rights reserved.
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;

@Path("/aai")
public class AaiSimulatorJaxRs {

    private static final String GETFAIL = "getFail";

    /**
     * A&AI get query.
     *
     * @return the result
     */
    @GET
    @Path("/{version:v16|v20}/search/nodes-query")
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
     * @throws IOException if a response file cannot be read
     */
    @PUT
    @Path("/{version:v16|v20}/query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String aaiPutQuery(final String req) throws IOException {
        return IOUtils.toString(getClass().getResource("aai/AaiCqResponse.json"),
            StandardCharsets.UTF_8);
    }

    /**
     * A&AI get PNF query.
     *
     * @return the result
     * @throws IOException if a response file cannot be read
     */
    @GET
    @Path("/{version:v16|v20}/network/pnfs/pnf/{pnfName}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String aaiGetPnfQuery(@PathParam("pnfName") final String pnfName) throws IOException {
        if (GETFAIL.equals(pnfName)) {
            throw new IllegalArgumentException("query failed, as requested");
        }

        return IOUtils.toString(getClass().getResource("aai/AaiGetPnfResponse.json"),
                        StandardCharsets.UTF_8);
    }
}
