/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2023 Nordix Foundation.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/policy/pdpx/v1")
public class XacmlSimulatorJaxRs {
    private static final Logger logger = LoggerFactory.getLogger(XacmlSimulatorJaxRs.class);

    public static final String POLICY_CONFIG_OPER_PREFIX = "org/onap/policy/simulators/xacml/xacml.configure.";
    public static final String DENY_CLNAME = "denyGuard";
    public static final Coder coder = new StandardCoder();

    // @formatter:off
    private Map<String, Function<DecisionRequest, DecisionResponse>> action2method = Map.of(
                "guard", this::guardDecision,
                "configure", this::configureDecision
                );
    // @formatter:on

    /**
     * Get a XACML decision.
     *
     * @param req the request
     * @return the response
     */
    @POST
    @Path("/decision")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/json")
    public DecisionResponse getDecision(DecisionRequest req) {
        Function<DecisionRequest, DecisionResponse> func = action2method.get(req.getAction());
        if (func != null) {
            return func.apply(req);
        }

        var response = new DecisionResponse();
        response.setMessage("unsupported action: " + req.getAction());
        return response;
    }

    private DecisionResponse guardDecision(DecisionRequest req) {
        @SuppressWarnings("unchecked")
        Map<String, String> guard = (Map<String, String>) req.getResource().get("guard");
        String clName = guard.get("clname");

        var response = new DecisionResponse();
        response.setStatus(DENY_CLNAME.equals(clName) ? "Deny" : "Permit");
        response.setAdvice(Collections.emptyMap());
        response.setObligations(Collections.emptyMap());
        response.setPolicies(Collections.emptyMap());
        return response;
    }

    private DecisionResponse configureDecision(DecisionRequest req) {
        var response = new DecisionResponse();
        response.setPolicies(new HashMap<>());

        Map<String, Object> resources = req.getResource();
        var policyId = resources.get("policy-id");
        if (policyId != null) {
            String fileName = POLICY_CONFIG_OPER_PREFIX + policyId + ".json";
            try {
                var policyJson = ResourceUtils.getResourceAsString(fileName);
                var toscaServiceTemplate = coder.decode(policyJson, ToscaServiceTemplate.class);
                toscaServiceTemplate.getToscaTopologyTemplate().getPolicies()
                                .forEach(policyMap -> response.getPolicies().putAll(policyMap));
            } catch (CoderException e) {
                logger.warn("cannot decode policy file: {}", fileName, e);
                response.setMessage("cannot decode policy");
            } catch (NullPointerException e) {
                logger.warn("cannot read policy simulator file", e);
                response.setMessage("cannot read policy simulator file");
            }
        } else {
            // the current simulator only supports searching by policy-id
            // future changes may support getting policies by policy-type
            response.setMessage("resource must contain policy-id key");
        }
        return response;
    }
}
