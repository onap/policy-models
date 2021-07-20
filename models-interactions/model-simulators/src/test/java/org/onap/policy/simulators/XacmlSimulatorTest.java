/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;
import org.onap.policy.rest.RestManager;

public class XacmlSimulatorTest {
    private static final StandardCoder coder = new StandardCoder();

    /**
     * Set up test class.
     */
    @BeforeClass
    public static void setupSimulator() {
        try {
            org.onap.policy.simulators.Util.buildXacmlSim();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownSimulator() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    public void testGuard() throws CoderException {
        String request = makeRequest("test_actor_id", "test_op_id", "test_target", "test_clName");
        String url = "http://localhost:" + Util.XACMLSIM_SERVER_PORT + "/policy/pdpx/v1/decision";
        Pair<Integer, String> response =
                new RestManager().post(url, "testUname", "testPass", null, "application/json", request);
        assertNotNull(response);
        assertNotNull(response.getLeft());
        assertNotNull(response.getRight());

        DecisionResponse decision = coder.decode(response.getRight(), DecisionResponse.class);
        assertEquals("Permit", decision.getStatus());

        request = makeRequest("test_actor_id", "test_op_id", "test_target", "denyGuard");
        response = new RestManager().post(url, "testUname", "testPass", null, "application/json", request);
        assertNotNull(response);
        assertNotNull(response.getLeft());
        assertNotNull(response.getRight());
        decision = coder.decode(response.getRight(), DecisionResponse.class);
        assertEquals("Deny", decision.getStatus());
    }

    private static String makeRequest(String actor, String recipe, String target, String clName) throws CoderException {
        Map<String, String> guard = new HashMap<String, String>();
        guard.put("actor", actor);
        guard.put("recipe", recipe);
        guard.put("target", target);
        guard.put("clname", clName);
        Map<String, Object> resource = new HashMap<String, Object>();
        resource.put("guard", guard);
        DecisionRequest request = new DecisionRequest();
        request.setResource(resource);

        return coder.encode(request);
    }
}
