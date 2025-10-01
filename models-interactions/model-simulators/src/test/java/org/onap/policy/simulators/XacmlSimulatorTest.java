/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2024 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;
import org.onap.policy.rest.RestManager;

class XacmlSimulatorTest {
    private static final StandardCoder coder = new StandardCoder();

    /**
     * Set up test class.
     */
    @BeforeAll
    static void setupSimulator() {
        try {
            var testServer = Util.buildXacmlSim();
            assertNotNull(testServer);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterAll
    static void tearDownSimulator() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    void testGuard() throws CoderException {
        String request = makeGuardRequest("test_actor_id", "test_op_id", "test_target", "test_clName");
        DecisionResponse decision = sendRequest(request);
        assertEquals("Permit", decision.getStatus());

        request = makeGuardRequest("test_actor_id", "test_op_id", "test_target", "denyGuard");
        decision = sendRequest(request);
        assertEquals("Deny", decision.getStatus());
    }

    @Test
    void testConfigure() throws CoderException {
        // test retrieving a policy
        String request = makeConfigureRequest("policy-id", "test-policy");
        DecisionResponse decision = sendRequest(request);
        assertNotNull(decision.getPolicies());
        assertThat(decision.getPolicies()).containsKey("test-policy");

        // test no policy found
        request = makeConfigureRequest("policy-id", "nonexistent");
        decision = sendRequest(request);
        assertNotNull(decision.getPolicies());
        assertThat(decision.getPolicies()).doesNotContainKey("nonexistent");

        // test unsupported operation
        request = makeConfigureRequest("policy-type", "test");
        decision = sendRequest(request);
        assertEquals("resource must contain policy-id key", decision.getMessage());
    }

    @Test
    void testConfigureMissingFile() throws CoderException {
        // test retrieving a policy
        String request = makeConfigureRequest("policy-id", "bogus-policy");
        DecisionResponse decision = sendRequest(request);
        assertNotNull(decision.getPolicies());
        assertEquals("cannot read policy simulator file", decision.getMessage());
    }

    @Test
    void testConfigureInvalidJson() throws CoderException {
        // test retrieving a policy
        String request = makeConfigureRequest("policy-id", "invalid-policy");
        DecisionResponse decision = sendRequest(request);
        assertNotNull(decision.getPolicies());
        assertEquals("cannot decode policy", decision.getMessage());
    }

    @Test
    void testUnknownAction() throws CoderException {
        String request = makeGuardRequest("test_actor_id", "test_op_id", "test_target", "test_clName");
        request = request.replace("guard", "bogus-action");
        DecisionResponse decision = sendRequest(request);
        assertThat(decision.getStatus()).isNull();
        assertThat(decision.getMessage()).isEqualTo("unsupported action: bogus-action");
    }

    private DecisionResponse sendRequest(String request) throws CoderException {
        String url = "http://localhost:" + Util.XACMLSIM_SERVER_PORT + "/policy/pdpx/v1/decision";
        Pair<Integer, String> response =
                new RestManager().post(url, "testUname", "testPass", null, "application/json", request);

        // verify the response isn't null
        assertNotNull(response);
        assertNotNull(response.getLeft());
        assertNotNull(response.getRight());

        return coder.decode(response.getRight(), DecisionResponse.class);
    }

    private String makeGuardRequest(String actor, String recipe, String target, String clName) throws CoderException {
        Map<String, String> guard = new HashMap<>();
        guard.put("actor", actor);
        guard.put("recipe", recipe);
        guard.put("target", target);
        guard.put("clname", clName);

        Map<String, Object> resource = new HashMap<>();
        resource.put("guard", guard);

        DecisionRequest request = new DecisionRequest();
        request.setAction("guard");
        request.setResource(resource);

        return coder.encode(request);
    }

    private String makeConfigureRequest(String key, String val) throws CoderException {
        Map<String, Object> resource = new HashMap<>();
        resource.put(key, val);

        DecisionRequest request = new DecisionRequest();
        request.setAction("configure");
        request.setResource(resource);

        return coder.encode(request);
    }
}
