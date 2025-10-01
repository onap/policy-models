/*-
 * ============LICENSE_START=======================================================
 * simulators
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.aai.AaiManager;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.rest.RestManager;

class AaiSimulatorTest {

    /**
     * Set up test class.
     */
    @BeforeAll
    static void setUpSimulator() {
        try {
            var testServer = Util.buildAaiSim();
            assertNotNull(testServer);
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterAll
    static void tearDownSimulator() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    void testCqGet() {
        final AaiCqResponse response = new AaiManager(new RestManager()).getCustomQueryResponse("http://localhost:6666",
                "testUser", "testPass", UUID.randomUUID(), "Ete_vFWCLvFWSNK_7ba1fbde_0");
        assertNotNull(response);
        assertEquals("f953c499-4b1e-426b-8c6d-e9e9f1fc730f", response.getVserver().getVserverId());
    }
}
