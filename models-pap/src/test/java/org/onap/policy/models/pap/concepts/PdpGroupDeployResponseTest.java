/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Bell Canada. All rights reserved.
 *  Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.models.pap.concepts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Test class for PdpGroupDeployResponse.
 */
class PdpGroupDeployResponseTest {

    private static final String URI = "/policy/pap/v1/policies/status";
    private static final String MESSAGE = "the message";

    @Test
    void testPdpGroupDeployResponse() {
        assertNotNull(new PdpGroupDeployResponse("message", "uri"));
        assertNotNull(new PdpGroupDeployResponse("message", null));
        assertNotNull(new PdpGroupDeployResponse(null, null));
        assertNotNull(new PdpGroupDeployResponse());

        PdpGroupDeployResponse resp = new PdpGroupDeployResponse(MESSAGE, URI);
        assertEquals(MESSAGE, resp.getMessage());
        assertEquals(URI, resp.getUri());

        resp = new PdpGroupDeployResponse(null, null);
        assertNull(resp.getMessage());
        assertNull(resp.getUri());

        resp = new PdpGroupDeployResponse();
        assertNull(resp.getMessage());
        assertNull(resp.getUri());
    }
}
