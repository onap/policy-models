/*
 * ============LICENSE_START=======================================================
 * rest
 * ================================================================================
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved
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

package org.onap.policy.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.policy.rest.RestManager.Pair;

public class PairTest {

    @Test
    public void testPair() {
        RestManager mgr = new RestManager();

        Pair<Integer, Integer> pii = mgr.new Pair<>(1, 2);
        assertEquals((Integer) 1, pii.first);
        assertEquals((Integer) 2, pii.second);

        Pair<Integer, String> pis = mgr.new Pair<>(1, "test");
        assertEquals((Integer) 1, pis.first);
        assertEquals("test", pis.second);
    }
}
