/*-
 * ============LICENSE_START=======================================================
 * sdnc
 * ================================================================================
 * Copyright (C) 2018 Huawei. All rights reserved.
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

package org.onap.policy.sdnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SdncResponseDescriptorTest {

    @Test
    public void testSdncResponseDescriptor() {
        SdncResponseOutput output = new SdncResponseOutput();
        assertNotNull(output);
        assertNotEquals(0, output.hashCode());
        
        String responseCode = "200";
        output.setResponseCode(responseCode);
        assertEquals(responseCode, output.getResponseCode());
        
        String svcRequest = "svc-request-01";
        output.setSvcRequestId(svcRequest);
        assertEquals(svcRequest, output.getSvcRequestId());
        
        String indicator = "final-indicator-00";
        output.setAckFinalIndicator(indicator);
        assertEquals(indicator, output.getAckFinalIndicator());
        
        assertNotEquals(0, output.hashCode());
    }
}
