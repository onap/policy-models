/*
 * ============LICENSE_START=======================================================
 * rest
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

package org.onap.policy.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.policy.rest.RestManager.Pair;

public class PostTest {

    @Test
    public void testUsernameNull() {
        RestManager mgr = new RestManager();
        Pair<Integer, String> result = mgr.post("http://www.example.org", null, null, null, null, null);
        assertEquals(null, result);
    }

    @Test
    public void testUsernameEmpty() {
        RestManager mgr = new RestManager();
        Pair<Integer, String> result = mgr.post("http://www.example.org", "", null, null, null, null);
        assertEquals(null, result);
    }

    @Test
    public void testBodyNull() {
        RestManager mgr = new RestManager();
        Pair<Integer, String> result = mgr.post("http://www.example.org", "user", null, null, null, null);
        assertEquals(null, result);
    }
}
