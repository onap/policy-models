/*-
 * ============LICENSE_START=======================================================
 * controlloop
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.params;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ControlLoopParamsTest {

    @Test
    public void test() {
        ControlLoopParams params = new ControlLoopParams();
        assertNotNull(params);

        params.setClosedLoopControlName("name");
        params.setControlLoopYaml("yaml");
        params.setPolicyName("name");
        params.setPolicyScope("scope");
        params.setPolicyVersion("1");

        ControlLoopParams params2 = new ControlLoopParams(params);

        assertEquals("name", params2.getClosedLoopControlName());
        assertEquals("yaml", params2.getControlLoopYaml());
        assertEquals("name", params2.getPolicyName());
        assertEquals("scope", params2.getPolicyScope());
        assertEquals("1", params2.getPolicyVersion());

    }
}
