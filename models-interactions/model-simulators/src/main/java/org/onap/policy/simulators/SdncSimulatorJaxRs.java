/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2018 Huawei. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
        final SdncResponse response = new SdncResponse();
        response.setRequestId(UUID.randomUUID().toString());
        SdncResponseOutput responseOutput = new SdncResponseOutput();
        responseOutput.setResponseCode("200");
        responseOutput.setAckFinalIndicator("Y");
        responseOutput.setSvcRequestId(UUID.randomUUID().toString());
        response.setResponseOutput(responseOutput);
        return Serialization.gsonPretty.toJson(response);
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
        final SdncResponse response = new SdncResponse();
        response.setRequestId(UUID.randomUUID().toString());
        SdncResponseOutput responseOutput = new SdncResponseOutput();
        responseOutput.setResponseCode("200");
        responseOutput.setAckFinalIndicator("Y");
        responseOutput.setSvcRequestId(UUID.randomUUID().toString());
        response.setResponseOutput(responseOutput);
        return Serialization.gsonPretty.toJson(response);
    }
}
