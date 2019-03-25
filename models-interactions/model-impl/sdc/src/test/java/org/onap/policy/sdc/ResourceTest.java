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

public class ResourceTest {

    @Test
    public void testConstructors() {
        Resource res = new Resource();
        assertEquals(null, res.getResourceUuid());
        assertEquals(null, res.getResourceInvariantUuid());
        assertEquals(null, res.getResourceName());
        assertEquals(null, res.getResourceType());
        assertEquals(null, res.getResourceVersion());

        UUID uuid = UUID.randomUUID();
        res = new Resource(uuid);
        assertEquals(uuid, res.getResourceUuid());
        assertEquals(null, res.getResourceInvariantUuid());
        assertEquals(null, res.getResourceName());
        assertEquals(null, res.getResourceType());
        assertEquals(null, res.getResourceVersion());

        String name = "constTest";
        res = new Resource(name, ResourceType.CP);
        assertEquals(null, res.getResourceUuid());
        assertEquals(name, res.getResourceName());
        assertEquals(ResourceType.CP, res.getResourceType());
        assertEquals(null, res.getResourceInvariantUuid());
        assertEquals(null, res.getResourceVersion());

        uuid = UUID.randomUUID();
        UUID uuidInvariant = UUID.randomUUID();
        name = "constTestUUID";
        String version = "0.0.1";
        res = new Resource(uuid, uuidInvariant, name, version, ResourceType.VF);
        assertEquals(uuid, res.getResourceUuid());
        assertEquals(uuidInvariant, res.getResourceInvariantUuid());
        assertEquals(name, res.getResourceName());
        assertEquals(ResourceType.VF, res.getResourceType());
        assertEquals(version, res.getResourceVersion());

        Resource r2 = new Resource(res);
        assertEquals(uuid, r2.getResourceUuid());
        assertEquals(uuidInvariant, r2.getResourceInvariantUuid());
        assertEquals(name, r2.getResourceName());
        assertEquals(ResourceType.VF, r2.getResourceType());
        assertEquals(version, r2.getResourceVersion());
    }

    @Test
    public void testUuid() {
        Resource res = new Resource();
        UUID uuid = UUID.randomUUID();
        res.setResourceUuid(uuid);
        assertEquals(uuid, res.getResourceUuid());
    }

    @Test
    public void testInvariantUuid() {
        Resource res = new Resource();
        UUID uuid = UUID.randomUUID();
        res.setResourceInvariantUuid(uuid);
        assertEquals(uuid, res.getResourceInvariantUuid());
    }

    @Test
    public void testName() {
        Resource res = new Resource();
        String name = "nameTest";
        res.setResourceName(name);
        assertEquals(name, res.getResourceName());
    }

    @Test
    public void testVersion() {
        Resource res = new Resource();
        String version = "versionTest";
        res.setResourceVersion(version);
        assertEquals(version, res.getResourceVersion());
    }

    @Test
    public void testType() {
        Resource res = new Resource();
        res.setResourceType(ResourceType.CP);
        assertEquals(ResourceType.CP, res.getResourceType());
    }

    @Test
    public void testEquals() {
        Resource r1 = new Resource();
        Resource r2 = new Resource(r1);
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));

        r1 = new Resource(UUID.randomUUID(), UUID.randomUUID(), "equalsTest", "1.1.1",
                ResourceType.VFC);
        r2 = new Resource(r1);
        assertTrue(r1.equals(r2));
        assertTrue(r2.equals(r1));
    }

    @Test
    public void testToString() {
        Resource r1 = new Resource();
        Resource r2 = new Resource(r1);
        assertEquals(r1.toString(), r2.toString());

        r1 = new Resource(UUID.randomUUID(), UUID.randomUUID(), "equalsTest", "1.1.1",
                ResourceType.VFC);
        r2 = new Resource(r1);
        assertEquals(r1.toString(), r2.toString());
    }

    @Test
    public void testHashCode() {
        Resource r1 = new Resource();
        Resource r2 = new Resource(r1);
        assertEquals(r1.hashCode(), r2.hashCode());

        r1 = new Resource(UUID.randomUUID(), UUID.randomUUID(), "equalsTest", "1.1.1",
                ResourceType.VFC);
        r2 = new Resource(r1);
        assertEquals(r1.hashCode(), r2.hashCode());
    }
}
