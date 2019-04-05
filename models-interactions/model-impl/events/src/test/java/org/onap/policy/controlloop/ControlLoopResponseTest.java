/*-
 * ============LICENSE_START=======================================================
 * controlloop
 * ================================================================================
 * Copyright (C) 2019 Wipro Limited Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import org.junit.Test;

public class ControlLoopResponseTest {
    
    @Test
    public void test() {
        ControlLoopResponse rsp = new ControlLoopResponse();

        assertEquals("1.0.0", rsp.getVersion());
        
        rsp = new ControlLoopResponse(null);
        assertEquals("1.0.0", rsp.getVersion());
        
        rsp.setClosedLoopControlName("name");
        assertEquals("name", rsp.getClosedLoopControlName());
        
        rsp.setFrom("from");
        assertEquals("from", rsp.getFrom());
        
        rsp.setPayload("payload");
        assertEquals("payload", rsp.getPayload());
        
        rsp.setPolicyName("policyname");
        assertEquals("policyname", rsp.getPolicyName());
        
        rsp.setPolicyVersion("1");
        assertEquals("1", rsp.getPolicyVersion());
        
        UUID id = UUID.randomUUID();
        rsp.setRequestId(id);
        assertEquals(id, rsp.getRequestId());
        
        rsp.setTarget("target");
        assertEquals("target", rsp.getTarget());
        
        rsp.setVersion("foo");
        assertEquals("foo", rsp.getVersion());
        
    }
}
