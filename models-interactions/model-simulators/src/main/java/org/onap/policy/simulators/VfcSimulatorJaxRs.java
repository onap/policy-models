/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/nslcm/v1")
public class VfcSimulatorJaxRs {

    /**
     * VFC post query.
     * 
     * @param nsInstanceId the NS instance
     * @param response the response
     * @return the response
     */
    @POST
    @Path("/ns/{nsInstanceId}/heal")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String vfcPostQuery(@PathParam("nsInstanceId") String nsInstanceId,
            @Context final HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_ACCEPTED);
        try {
            response.flushBuffer();
        } catch (Exception e) {
            final Logger logger = LoggerFactory.getLogger(VfcSimulatorJaxRs.class);
            logger.error("flushBuffer threw: ", e);
            return "";
        }

        return "{\"jobId\":\"1\"}";
    }

    /**
     * VFC get query.
     * 
     * @param jobId tthe job id
     * @return the response
     */
    @GET
    @Path("/jobs/{jobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String vfcGetQuery(@PathParam("jobId") String jobId) {
        return "{\"jobId\" : " + jobId
                + ",\"responseDescriptor\" : {\"progress\" : \"40\",\"status\" : \"finished\",\"statusDescription"
                + "\" : \"OMC VMs are decommissioned in VIM\",\"errorCode\" : null,\"responseId\": 101 ,\""
                + "responseHistoryList\": [{\"progress\" : \"40\",\"status\" : \"proccessing\",\"statusDescription"
                + "\" : \"OMC VMs are decommissioned in VIM\",\"errorCode\" : null,\"responseId\" : \"1\"}, {\""
                + "progress\" : \"41\",\"status\" : \"proccessing\",\"statusDescription\" : \"OMC VMs are "
                + "decommissioned in VIM\",\"errorCode\" : null,\"responseId\" : \"2\"}]}}";
    }

}

