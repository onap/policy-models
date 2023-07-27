/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2018 Huawei. All rights reserved.
 * Modifications Copyright (C) 2019, 2023 Nordix Foundation.
 * Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.UUID;
import org.onap.policy.sdnc.SdncResponse;
import org.onap.policy.sdnc.SdncResponseOutput;
import org.onap.policy.sdnc.util.Serialization;


@Path("/restconf/operations/")
public class SdncSimulatorJaxRs {

    /**
     * SDNC post query.
     *
     * @return the response
     */
    @POST
    @Path("/GENERIC-RESOURCE-API:network-topology-operation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String sdncPostQuery() {
        return makeSuccessResponse();
    }


    /**
     * SDNC vf module topology operation.
     *
     * @return the response
     */
    @POST
    @Path("/GENERIC-RESOURCE-API:vf-module-topology-operation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public String sdncVnfTopologyOperation() {
        return makeSuccessResponse();
    }


    private String makeSuccessResponse() {
        final var response = new SdncResponse();
        response.setRequestId(UUID.randomUUID().toString());
        var responseOutput = new SdncResponseOutput();
        responseOutput.setResponseCode("200");
        responseOutput.setAckFinalIndicator("Y");
        responseOutput.setSvcRequestId(UUID.randomUUID().toString());
        response.setResponseOutput(responseOutput);
        return Serialization.gsonPretty.toJson(response);
    }
}
