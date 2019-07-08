/*
 * ============LICENSE_START=======================================================
 * rest
 * ================================================================================
 * Copyright (C) 2018 Amdocs. All rights reserved.
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

public class HttpDeleteWithBodyTest {

    private static final String BASE_URI = "http://localhost:32802/base/";

    @Test
    public void getMethod() {
        HttpDeleteWithBody deleteWithBody = new HttpDeleteWithBody(BASE_URI);
        assertEquals("DELETE", deleteWithBody.getMethod());
        assertEquals(BASE_URI, deleteWithBody.getURI().toString());
    }
}