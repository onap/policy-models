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

import java.util.HashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.rest.RestManager;
import org.onap.policy.vfc.VfcResponse;
import org.onap.policy.vfc.util.Serialization;

class VfcSimulatorTest {

    /**
     * Set up test class.
     */
    @BeforeAll
    public static void setUpSimulator() {
        try {
            var testServer = Util.buildVfcSim();
            assertNotNull(testServer);
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterAll
    public static void tearDownSimulator() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    void testPost() {
        final Pair<Integer, String> httpDetails =
                new RestManager().post("http://localhost:6668/api/nslcm/v1/ns/1234567890/heal", "username", "password",
                    new HashMap<>(), "application/json", "Some Request Here");
        assertNotNull(httpDetails);
        assertEquals(202, httpDetails.getLeft().intValue());
        final VfcResponse response = Serialization.gsonPretty.fromJson(httpDetails.getRight(), VfcResponse.class);
        assertNotNull(response);
    }

    @Test
    void testGet() {
        final Pair<Integer, String> httpDetails = new RestManager().get("http://localhost:6668/api/nslcm/v1/jobs/1234",
                "username", "password", new HashMap<>());
        assertNotNull(httpDetails);
        final VfcResponse response = Serialization.gsonPretty.fromJson(httpDetails.getRight(), VfcResponse.class);
        assertNotNull(response);
    }
}
