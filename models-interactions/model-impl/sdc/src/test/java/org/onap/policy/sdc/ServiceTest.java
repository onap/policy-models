/*-
 * ============LICENSE_START=======================================================
 * sdc
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

package org.onap.policy.sdc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.Test;

public class ServiceTest {

    @Test
    public void testConstructors() {
        Service svc = new Service();
        assertEquals(null, svc.getServiceUUID());
        assertEquals(null, svc.getServiceInvariantUUID());
        assertEquals(null, svc.getServiceName());
        assertEquals(null, svc.getServiceVersion());

        UUID uuid = UUID.randomUUID();
        svc = new Service(uuid);
        assertEquals(uuid, svc.getServiceUUID());
        assertEquals(null, svc.getServiceInvariantUUID());
        assertEquals(null, svc.getServiceName());
        assertEquals(null, svc.getServiceVersion());

        String name = "constTest";
        svc = new Service(name);
        assertEquals(null, svc.getServiceUUID());
        assertEquals(name, svc.getServiceName());
        assertEquals(null, svc.getServiceInvariantUUID());
        assertEquals(null, svc.getServiceVersion());

        uuid = UUID.randomUUID();
        UUID uuidInvariant = UUID.randomUUID();
        name = "constTestUUID";
        String version = "0.0.1";
        svc = new Service(uuid, uuidInvariant, name, version);
        assertEquals(uuid, svc.getServiceUUID());
        assertEquals(uuidInvariant, svc.getServiceInvariantUUID());
        assertEquals(name, svc.getServiceName());
        assertEquals(version, svc.getServiceVersion());

        Service s2 = new Service(svc);
        assertEquals(uuid, s2.getServiceUUID());
        assertEquals(uuidInvariant, s2.getServiceInvariantUUID());
        assertEquals(name, s2.getServiceName());
        assertEquals(version, s2.getServiceVersion());
    }

    @Test
    public void testUuid() {
        Service svc = new Service();
        UUID uuid = UUID.randomUUID();
        svc.setServiceUUID(uuid);
        assertEquals(uuid, svc.getServiceUUID());
    }

    @Test
    public void testInvariantUuid() {
        Service svc = new Service();
        UUID uuid = UUID.randomUUID();
        svc.setServiceInvariantUUID(uuid);
        assertEquals(uuid, svc.getServiceInvariantUUID());
    }

    @Test
    public void testName() {
        Service svc = new Service();
        String name = "nameTest";
        svc.setServiceName(name);
        assertEquals(name, svc.getServiceName());
    }

    @Test
    public void testVersion() {
        Service svc = new Service();
        String version = "versionTest";
        svc.setServiceVersion(version);
        assertEquals(version, svc.getServiceVersion());
    }

    @Test
    public void testEquals() {
        Service s1 = new Service();
        Service s2 = new Service(s1);
        assertTrue(s1.equals(s2));
        assertTrue(s2.equals(s1));

        s1 = new Service(UUID.randomUUID(), UUID.randomUUID(), "equalsTest", "1.1.1");
        s2 = new Service(s1);
        assertTrue(s1.equals(s2));
        assertTrue(s2.equals(s1));
    }

    @Test
    public void testToString() {
        Service s1 = new Service();
        Service s2 = new Service(s1);
        assertEquals(s1.toString(), s2.toString());

        s1 = new Service(UUID.randomUUID(), UUID.randomUUID(), "equalsTest", "1.1.1");
        s2 = new Service(s1);
        assertEquals(s1.toString(), s2.toString());
    }

    @Test
    public void testHashCode() {
        Service s1 = new Service();
        Service s2 = new Service(s1);
        assertEquals(s1.hashCode(), s2.hashCode());

        s1 = new Service(UUID.randomUUID(), UUID.randomUUID(), "equalsTest", "1.1.1");
        s2 = new Service(s1);
        assertEquals(s1.hashCode(), s2.hashCode());
    }
}
