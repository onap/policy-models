/*-
 * ============LICENSE_START=======================================================
 * sdnc
 * ================================================================================
 * Copyright (C) 2018 Huawei. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
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

package org.onap.policy.sdnc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;
import org.junit.jupiter.api.Test;

public class SdncRequestTest {

    @Test
    public void testSdncRequest() {
        SdncRequest request = new SdncRequest();
        assertNotNull(request);
        assertNotEquals(0, request.hashCode());
        
        String nsInstanceId = "Dorothy";
        request.setNsInstanceId(nsInstanceId);
        assertEquals(nsInstanceId, request.getNsInstanceId());
        
        UUID requestId = UUID.randomUUID();
        request.setRequestId(requestId);
        assertEquals(requestId, request.getRequestId());
        
        SdncHealRequest healRequest = new SdncHealRequest();
        request.setHealRequest(healRequest);
        assertEquals(healRequest, request.getHealRequest());
        
        assertNotEquals(0, request.hashCode());
    }
}
