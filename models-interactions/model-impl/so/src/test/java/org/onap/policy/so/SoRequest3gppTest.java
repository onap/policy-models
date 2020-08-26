/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited.
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

package org.onap.policy.so;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class SoRequest3gppTest {

    @Test
    public void testConstructor() {
        SoRequest3gpp obj = new SoRequest3gpp();

        assertNull(obj.getName());
        assertNull(obj.getServiceInstanceID());
        assertNull(obj.getSubscriptionServiceType());
        assertNull(obj.getGlobalSubscriberId());
        assertNull(obj.getNetworkType());
        assertNull(obj.getAdditionalProperties());
    }

    @Test
    public void testSetGet() {
        SoRequest3gpp obj = new SoRequest3gpp();

        obj.setServiceInstanceID("12345");
        assertEquals("12345", obj.getServiceInstanceID());

        obj.setName("name");
        assertEquals("name", obj.getName());

        obj.setNetworkType("an");
        assertEquals("an", obj.getNetworkType());

        obj.setGlobalSubscriberId("5G customer");
        assertEquals("5G customer", obj.getGlobalSubscriberId());

        obj.setSubscriptionServiceType("5G");
        assertEquals("5G", obj.getSubscriptionServiceType());

        Map<String, Object> props = new HashMap<>();
        obj.setAdditionalProperties(props);
        assertEquals(props, obj.getAdditionalProperties());
    }
}
