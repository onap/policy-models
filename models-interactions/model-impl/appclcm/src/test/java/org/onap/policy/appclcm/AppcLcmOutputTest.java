/*-
 * ============LICENSE_START=======================================================
 * appclcm
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;

public class AppcLcmOutputTest {

    @Test
    public void testAppcLcmOutput() {
        AppcLcmCommonHeader commonHeader = new AppcLcmCommonHeader("Policy", UUID.randomUUID(), "1");
        Map<String, String> actionIdentifiers = new HashMap<>();
        AppcLcmInput input = new AppcLcmInput(commonHeader, "testAction", actionIdentifiers, "testPayload");

        AppcLcmOutput output = new AppcLcmOutput(input);
        assertEquals(output.getPayload(), input.getPayload());

        AppcLcmCommonHeader commonHeaderCopy = output.getCommonHeader();
        assertNotNull(commonHeaderCopy);
        assertEquals(commonHeaderCopy.getOriginatorId(), commonHeader.getOriginatorId());
        assertEquals(commonHeaderCopy.getRequestId(), commonHeader.getRequestId());
        assertEquals(commonHeaderCopy.getSubRequestId(), commonHeader.getSubRequestId());
        assertEquals(commonHeaderCopy.getFlags(), commonHeader.getFlags());
    }

}
