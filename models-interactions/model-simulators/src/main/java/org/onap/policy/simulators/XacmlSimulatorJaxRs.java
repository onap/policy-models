/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.Collections;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;

@Path("/policy/pdpx/v1")
public class XacmlSimulatorJaxRs {
    public static final String DENY_CLNAME = "denyGuard";

    /**
     * Get a guard decision.
     *
     * @param req the request
     * @return the response
     */
    @POST
    @Path("/decision")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public DecisionResponse getGuardDecision(DecisionRequest req) {
        @SuppressWarnings("unchecked")
        Map<String, String> guard = (Map<String, String>) req.getResource().get("guard");
        String clName = guard.get("clname");
        var response = new DecisionResponse();
        if (DENY_CLNAME.equals(clName)) {
            response.setStatus("Deny");
            response.setAdvice(Collections.emptyMap());
            response.setObligations(Collections.emptyMap());
            response.setPolicies(Collections.emptyMap());
            return response;
        } else {
            response.setStatus("Permit");
            response.setAdvice(Collections.emptyMap());
            response.setObligations(Collections.emptyMap());
            response.setPolicies(Collections.emptyMap());
            return response;
        }
    }
}
