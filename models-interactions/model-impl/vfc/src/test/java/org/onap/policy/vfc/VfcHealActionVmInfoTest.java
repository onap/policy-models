/*-
 * ============LICENSE_START=======================================================
 * vfc
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class VfcHealActionVmInfoTest {

    @Test
    public void testVfcHealActionVmInfo() {
        VfcHealActionVmInfo actionInfo = new VfcHealActionVmInfo();
        assertNotNull(actionInfo);
        assertNotEquals(0, actionInfo.hashCode());

        String vmid = "ECity";
        actionInfo.setVmid(vmid);
        assertEquals(vmid, actionInfo.getVmid());

        String vmName = "Emerald City";
        actionInfo.setVmname(vmName);
        assertEquals(vmName, actionInfo.getVmname());

        assertNotEquals(0, actionInfo.hashCode());
    }
}
