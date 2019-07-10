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

public class ServiceInstanceTest {

    private static final String SERVICE = "service";
    private static final String INSTANCE = "instance";
    private static final String VERSION_333 = "3.3.3";

    @Test
    public void testConstructors() {
        ServiceInstance si = new ServiceInstance();
        assertEquals(null, si.getServiceInstanceName());
        assertEquals(null, si.getServiceUuid());
        assertEquals(null, si.getServiceInstanceUuid());
        assertEquals(null, si.getServiceName());
        assertEquals(null, si.getPersonaModelUuid());
        assertEquals(null, si.getWidgetModelUuid());
        assertEquals(null, si.getWidgetModelVersion());

        ServiceInstance si2 = new ServiceInstance((ServiceInstance) null);
        assertEquals(null, si2.getServiceInstanceName());
        assertEquals(null, si2.getServiceUuid());
        assertEquals(null, si2.getServiceInstanceUuid());
        assertEquals(null, si2.getServiceName());
        assertEquals(null, si2.getPersonaModelUuid());
        assertEquals(null, si2.getWidgetModelUuid());
        assertEquals(null, si2.getWidgetModelVersion());

        si2 = new ServiceInstance(si);
        assertEquals(si2.getServiceInstanceName(), si.getServiceInstanceName());
        assertEquals(si2.getServiceUuid(), si.getServiceUuid());
        assertEquals(si2.getServiceInstanceUuid(), si.getServiceInstanceUuid());
        assertEquals(si2.getServiceName(), si.getServiceName());
        assertEquals(si2.getPersonaModelUuid(), si.getPersonaModelUuid());
        assertEquals(si2.getWidgetModelUuid(), si.getWidgetModelUuid());
        assertEquals(si2.getWidgetModelVersion(), si.getWidgetModelVersion());
    }

    @Test
    public void testInstanceName() {
        ServiceInstance si = new ServiceInstance();
        String name = "nameTestInstance";
        si.setServiceInstanceName(name);;
        assertEquals(name, si.getServiceInstanceName());
    }

    @Test
    public void testUuid() {
        ServiceInstance si = new ServiceInstance();
        UUID uuid = UUID.randomUUID();
        si.setServiceUuid(uuid);
        assertEquals(uuid, si.getServiceUuid());
    }

    @Test
    public void testInstanceUuid() {
        ServiceInstance si = new ServiceInstance();
        UUID uuid = UUID.randomUUID();
        si.setServiceInstanceUuid(uuid);
        assertEquals(uuid, si.getServiceInstanceUuid());
    }

    @Test
    public void testName() {
        ServiceInstance si = new ServiceInstance();
        String name = "nameTest";
        si.setServiceName(name);
        assertEquals(name, si.getServiceName());
    }

    @Test
    public void testPersonaModelUuid() {
        ServiceInstance si = new ServiceInstance();
        UUID uuid = UUID.randomUUID();
        si.setPersonaModelUuid(uuid);
        assertEquals(uuid, si.getPersonaModelUuid());
    }

    @Test
    public void testWidgetModelUuid() {
        ServiceInstance si = new ServiceInstance();
        UUID uuid = UUID.randomUUID();
        si.setWidgetModelUuid(uuid);
        assertEquals(uuid, si.getWidgetModelUuid());
    }

    @Test
    public void testWidgetModelVersion() {
        ServiceInstance si = new ServiceInstance();
        String version = "2.2.2";
        si.setWidgetModelVersion(version);;
        assertEquals(version, si.getWidgetModelVersion());
    }

    @Test
    public void testEquals() {
        ServiceInstance si1 = new ServiceInstance();
        ServiceInstance si2 = new ServiceInstance(si1);
        assertTrue(si1.equals(si2));
        assertTrue(si2.equals(si1));

        si1.setServiceInstanceName(INSTANCE);
        si1.setServiceName(SERVICE);
        si1.setServiceInstanceUuid(UUID.randomUUID());
        si1.setServiceUuid(UUID.randomUUID());
        si1.setPersonaModelUuid(UUID.randomUUID());
        si1.setWidgetModelUuid(UUID.randomUUID());
        si1.setWidgetModelVersion(VERSION_333);
        si2 = new ServiceInstance(si1);
        assertTrue(si1.equals(si2));
        assertTrue(si2.equals(si1));
    }

    @Test
    public void testToString() {
        ServiceInstance si1 = new ServiceInstance();
        ServiceInstance si2 = new ServiceInstance(si1);
        assertEquals(si1.toString(), si2.toString());

        si1.setServiceInstanceName(INSTANCE);
        si1.setServiceName(SERVICE);
        si1.setServiceInstanceUuid(UUID.randomUUID());
        si1.setServiceUuid(UUID.randomUUID());
        si1.setPersonaModelUuid(UUID.randomUUID());
        si1.setWidgetModelUuid(UUID.randomUUID());
        si1.setWidgetModelVersion(VERSION_333);
        si2 = new ServiceInstance(si1);
        assertEquals(si1.toString(), si2.toString());
    }

    @Test
    public void testHashCode() {
        ServiceInstance si1 = new ServiceInstance();
        ServiceInstance si2 = new ServiceInstance(si1);
        assertEquals(si1.hashCode(), si2.hashCode());

        si1.setServiceInstanceName(INSTANCE);
        si1.setServiceName(SERVICE);
        si1.setServiceInstanceUuid(UUID.randomUUID());
        si1.setServiceUuid(UUID.randomUUID());
        si1.setPersonaModelUuid(UUID.randomUUID());
        si1.setWidgetModelUuid(UUID.randomUUID());
        si1.setWidgetModelVersion(VERSION_333);
        si2 = new ServiceInstance(si1);
        assertEquals(si1.hashCode(), si2.hashCode());
    }
}
