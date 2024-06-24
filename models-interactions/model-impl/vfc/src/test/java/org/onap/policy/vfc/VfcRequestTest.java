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

import java.util.UUID;
import org.junit.jupiter.api.Test;

class VfcRequestTest {

    @Test
    void testVfcRequest() {
        VfcRequest request = new VfcRequest();
        assertNotNull(request);
        assertNotEquals(0, request.hashCode());

        String nsInstanceId = "Dorothy";
        request.setNsInstanceId(nsInstanceId);
        assertEquals(nsInstanceId, request.getNsInstanceId());

        UUID requestId = UUID.randomUUID();
        request.setRequestId(requestId);
        assertEquals(requestId, request.getRequestId());

        VfcHealRequest healRequest = new VfcHealRequest();
        request.setHealRequest(healRequest);
        assertEquals(healRequest, request.getHealRequest());

        assertNotEquals(0, request.hashCode());
    }
}
