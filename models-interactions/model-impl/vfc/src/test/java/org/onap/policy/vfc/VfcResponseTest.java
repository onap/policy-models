/*-
 * ============LICENSE_START=======================================================
 * vfc
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
 * Modifications Copyright (C) 2018-2019 AT&T Corporation. All rights reserved.
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

package org.onap.policy.vfc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class VfcResponseTest {

    @Test
    public void testVfcResponse() {
        VfcResponse response = new VfcResponse();
        assertNotNull(response);
        assertNotEquals(0, response.hashCode());

        String jobId = "GetToOz";
        response.setJobId(jobId);
        assertEquals(jobId, response.getJobId());

        String requestId = "Get Home";
        response.setRequestId(requestId);
        assertEquals(requestId, response.getRequestId());

        VfcResponseDescriptor responseDescriptor = new VfcResponseDescriptor();
        response.setResponseDescriptor(responseDescriptor);
        assertEquals(responseDescriptor, response.getResponseDescriptor());

        assertNotEquals(0, response.hashCode());
    }
}
