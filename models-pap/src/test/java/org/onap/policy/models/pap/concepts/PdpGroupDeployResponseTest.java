/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2021 Bell Canada. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

/**
 * Test class for PdpGroupDeployResponse.
 */
public class PdpGroupDeployResponseTest {

    private static final String URL = "/policy/pap/v1/policies/status";
    private static final String MESSAGE = "the message";

    @Test
    public void testPdpGroupDeployResponse() {
        assertNotNull(new PdpGroupDeployResponse("message", "url"));
        assertNotNull(new PdpGroupDeployResponse("message", null));
        assertNotNull(new PdpGroupDeployResponse(null, null));

        PdpGroupDeployResponse resp = new PdpGroupDeployResponse(MESSAGE, URL);
        assertEquals(MESSAGE, resp.getMessage());
        assertEquals(URL, resp.getUrl());

        resp = new PdpGroupDeployResponse(null, null);
        assertNull(resp.getMessage());
        assertNull(resp.getUrl());
    }
}
