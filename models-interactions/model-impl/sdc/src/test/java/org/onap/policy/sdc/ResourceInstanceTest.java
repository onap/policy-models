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

public class ResourceInstanceTest {

    @Test
    public void testConstructors() {
        ResourceInstance ri = new ResourceInstance();
        assertEquals(null, ri.getResourceInstanceName());
        assertEquals(null, ri.getResourceUuid());
        assertEquals(null, ri.getResourceInvariantUUID());
        assertEquals(null, ri.getResourceName());
        assertEquals(null, ri.getResourceType());
        assertEquals(null, ri.getResourceVersion());

        ResourceInstance ri2 = new ResourceInstance((ResourceInstance) null);
        assertEquals(null, ri2.getResourceInstanceName());
        assertEquals(null, ri2.getResourceUuid());
        assertEquals(null, ri2.getResourceInvariantUUID());
        assertEquals(null, ri2.getResourceName());
        assertEquals(null, ri2.getResourceType());
        assertEquals(null, ri2.getResourceVersion());

        ri2 = new ResourceInstance(ri);
        assertEquals(ri2.getResourceInstanceName(), ri.getResourceInstanceName());
        assertEquals(ri2.getResourceUuid(), ri.getResourceUuid());
        assertEquals(ri2.getResourceInvariantUUID(), ri.getResourceInvariantUUID());
        assertEquals(ri2.getResourceName(), ri.getResourceName());
        assertEquals(ri2.getResourceType(), ri.getResourceType());
        assertEquals(ri2.getResourceVersion(), ri.getResourceVersion());
    }

    @Test
    public void testInstanceName() {
        ResourceInstance ri = new ResourceInstance();
        String name = "nameTestInstance";
        ri.setResourceInstanceName(name);;
        assertEquals(name, ri.getResourceInstanceName());
    }

    @Test
    public void testUuid() {
        ResourceInstance ri = new ResourceInstance();
        UUID uuid = UUID.randomUUID();
        ri.setResourceUuid(uuid);
        assertEquals(uuid, ri.getResourceUuid());
    }

    @Test
    public void testInvariantUuid() {
        ResourceInstance ri = new ResourceInstance();
        UUID uuid = UUID.randomUUID();
        ri.setResourceInvariantUUID(uuid);
        assertEquals(uuid, ri.getResourceInvariantUUID());
    }

    @Test
    public void testName() {
        ResourceInstance ri = new ResourceInstance();
        String name = "nameTest";
        ri.setResourceName(name);
        assertEquals(name, ri.getResourceName());
    }

    @Test
    public void testVersion() {
        ResourceInstance ri = new ResourceInstance();
        String version = "versionTest";
        ri.setResourceVersion(version);
        assertEquals(version, ri.getResourceVersion());
    }

    @Test
    public void testType() {
        ResourceInstance ri = new ResourceInstance();
        ri.setResourceType(ResourceType.CP);
        assertEquals(ResourceType.CP, ri.getResourceType());
    }

    @Test
    public void testEquals() {
        ResourceInstance ri1 = new ResourceInstance();
        ResourceInstance ri2 = new ResourceInstance(ri1);
        assertTrue(ri1.equals(ri2));
        assertTrue(ri2.equals(ri1));

        ri1.setResourceInstanceName("instance");
        ri1.setResourceName("resource");
        ri1.setResourceInvariantUUID(UUID.randomUUID());
        ri1.setResourceInvariantUUID(UUID.randomUUID());
        ri1.setResourceVersion("0.0.0");
        ri1.setResourceType(ResourceType.VL);
        ri2 = new ResourceInstance(ri1);
        assertTrue(ri1.equals(ri2));
        assertTrue(ri2.equals(ri1));
    }

    @Test
    public void testToString() {
        ResourceInstance ri1 = new ResourceInstance();
        ResourceInstance ri2 = new ResourceInstance(ri1);
        assertEquals(ri1.toString(), ri2.toString());

        ri1.setResourceInstanceName("instance");
        ri1.setResourceName("resource");
        ri1.setResourceInvariantUUID(UUID.randomUUID());
        ri1.setResourceInvariantUUID(UUID.randomUUID());
        ri1.setResourceVersion("0.0.0");
        ri1.setResourceType(ResourceType.VL);
        ri2 = new ResourceInstance(ri1);
        assertEquals(ri1.toString(), ri2.toString());
    }

    @Test
    public void testHashCode() {
        ResourceInstance ri1 = new ResourceInstance();
        ResourceInstance ri2 = new ResourceInstance(ri1);
        assertEquals(ri1.hashCode(), ri2.hashCode());

        ri1.setResourceInstanceName("instance");
        ri1.setResourceName("resource");
        ri1.setResourceInvariantUUID(UUID.randomUUID());
        ri1.setResourceInvariantUUID(UUID.randomUUID());
        ri1.setResourceVersion("0.0.0");
        ri1.setResourceType(ResourceType.VL);
        ri2 = new ResourceInstance(ri1);
        assertEquals(ri1.hashCode(), ri2.hashCode());
    }
}
