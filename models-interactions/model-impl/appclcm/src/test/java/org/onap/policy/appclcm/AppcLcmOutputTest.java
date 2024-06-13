/*-
 * ============LICENSE_START=======================================================
 * appclcm
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.appclcm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AppcLcmOutputTest {

    @Test
    void testAppcLcmOutput() {
        AppcLcmCommonHeader commonHeader = new AppcLcmCommonHeader("Policy", UUID.randomUUID(), "1");
        Map<String, String> actionIdentifiers = new HashMap<>();
        AppcLcmInput input = new AppcLcmInput(commonHeader, "testAction", actionIdentifiers, "testPayload");

        AppcLcmOutput output = new AppcLcmOutput(input);
        assertEquals(input.getPayload(), output.getPayload());

        AppcLcmCommonHeader commonHeaderCopy = output.getCommonHeader();
        assertNotNull(commonHeaderCopy);
        assertEquals(commonHeader.getOriginatorId(), commonHeaderCopy.getOriginatorId());
        assertEquals(commonHeader.getRequestId(), commonHeaderCopy.getRequestId());
        assertEquals(commonHeader.getSubRequestId(), commonHeaderCopy.getSubRequestId());
        assertEquals(commonHeader.getFlags(), commonHeaderCopy.getFlags());
    }

}
