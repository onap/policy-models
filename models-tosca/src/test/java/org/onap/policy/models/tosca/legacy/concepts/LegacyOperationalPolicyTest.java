/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.tosca.legacy.concepts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

public class LegacyOperationalPolicyTest {

    @Test
    public void test() {
        LegacyOperationalPolicy policy = new LegacyOperationalPolicy();
        assertNotNull(policy);
        policy.setPolicyId("onap.scaleout");
        assertEquals("onap.scaleout", policy.getPolicyId());
        policy.setPolicyVersion("1");
        assertEquals("1", policy.getPolicyVersion());
        policy.setContent("controlLoop%3A%0A%20%20");
        assertTrue(policy.getContent().length() > 0);
    }

}
