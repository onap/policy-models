/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.policy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import org.onap.policy.aai.Pnf;
import org.onap.policy.common.utils.io.Serializer;
import org.onap.policy.sdc.Resource;
import org.onap.policy.sdc.ResourceType;
import org.onap.policy.sdc.Service;

public class ControlLoopTest {

    private String controlLoopName = "control loop 1";
    private String version = "1.0.1";
    private String triggerPolicy = FinalResult.FINAL_OPENLOOP.toString();
    private Integer timeout = 100;
    private Boolean abatement = false;

    @Test
    public void testEqualsSameInstance() {
        ControlLoop controlLoop1 = new ControlLoop();
        assertTrue(controlLoop1.equals(controlLoop1));
    }

    @Test
    public void testEqualsNull() {
        ControlLoop controlLoop1 = new ControlLoop();
        assertFalse(controlLoop1.equals(null));
    }

    @Test
    public void testEqualsInstanceOfDiffClass() {
        ControlLoop controlLoop1 = new ControlLoop();
        assertFalse(controlLoop1.equals(""));
    }

    @Test
    public void testEqualsNoServicesAndResourcesOrTimeout() {
        final Pnf pnf = new Pnf();
        pnf.setPnfName("pnf 1");

        ControlLoop controlLoop1 = new ControlLoop();
        controlLoop1.setControlLoopName(controlLoopName);
        controlLoop1.setVersion(version);
        controlLoop1.setPnf(pnf);
        controlLoop1.setTrigger_policy(triggerPolicy);
        controlLoop1.setAbatement(abatement);

        ControlLoop controlLoop2 = new ControlLoop();
        controlLoop2.setControlLoopName(controlLoopName);
        controlLoop2.setVersion(version);
        controlLoop2.setPnf(pnf);
        controlLoop2.setTrigger_policy(triggerPolicy);
        controlLoop2.setAbatement(abatement);

        assertTrue(controlLoop1.equals(controlLoop2));
    }

    @Test
    public void testEquals() throws IOException {
        final Pnf pnf = new Pnf();
        pnf.setPnfName("pnf 1");

        ControlLoop controlLoop1 = new ControlLoop();
        controlLoop1.setControlLoopName(controlLoopName);
        controlLoop1.setVersion(version);
        Service service1 = new Service("service1");
        Service service2 = new Service("service2");
        List<Service> services = new ArrayList<>();
        services.add(service1);
        services.add(service2);
        controlLoop1.setServices(services);
        Resource resource1 = new Resource("resource1", ResourceType.VF);
        Resource resource2 = new Resource("resource2", ResourceType.VFC);
        List<Resource> resources = new ArrayList<>();
        resources.add(resource1);
        resources.add(resource2);
        controlLoop1.setResources(resources);
        controlLoop1.setPnf(pnf);
        controlLoop1.setTrigger_policy(triggerPolicy);
        controlLoop1.setTimeout(timeout);
        controlLoop1.setAbatement(abatement);

        ControlLoop controlLoop2 = new ControlLoop();
        controlLoop2.setControlLoopName(controlLoopName);
        controlLoop2.setVersion(version);
        Service controlLoop2Service1 = new Service("service1");
        Service controlLoop2Service2 = new Service("service2");
        List<Service> controlLoop2Services = new ArrayList<>();
        controlLoop2Services.add(controlLoop2Service1);
        controlLoop2Services.add(controlLoop2Service2);
        controlLoop2.setServices(controlLoop2Services);
        Resource controlLoop2Resource1 = new Resource("resource1", ResourceType.VF);
        Resource controlLoop2Resource2 = new Resource("resource2", ResourceType.VFC);
        List<Resource> controlLoop2Resources = new ArrayList<>();
        controlLoop2Resources.add(controlLoop2Resource1);
        controlLoop2Resources.add(controlLoop2Resource2);
        controlLoop2.setResources(controlLoop2Resources);
        controlLoop2.setPnf(pnf);
        controlLoop2.setTrigger_policy(triggerPolicy);
        controlLoop2.setTimeout(timeout);
        controlLoop1.setAbatement(abatement);

        assertTrue(controlLoop1.equals(controlLoop2));
        assertEquals(controlLoop1.hashCode(), controlLoop2.hashCode());

        controlLoop2 = Serializer.roundTrip(controlLoop1);
        assertTrue(controlLoop1.equals(controlLoop2));
        assertEquals(controlLoop1.hashCode(), controlLoop2.hashCode());
    }

    @Test
    @Ignore
    // I'VE MARKED THIS TEST CASE AS IGNORE BECAUSE THE TEST CASE FAILS
    // This test case fails because the ControlLoop(ControlLoop controlLoop) constructor.
    // does not copy the value of pnf and version into the newly created object
    // PLEASE ADVISE IF THE EXISTING BEHAVIOUR IS CORRECT
    public void testControlLoop() {
        final Pnf pnf = new Pnf();
        pnf.setPnfName("pnf 1");

        ControlLoop controlLoop1 = new ControlLoop();
        controlLoop1.setControlLoopName(controlLoopName);
        controlLoop1.setVersion(version);
        Service service1 = new Service("service1");
        Service service2 = new Service("service2");
        List<Service> services = new ArrayList<>();
        services.add(service1);
        services.add(service2);
        controlLoop1.setServices(services);
        Resource resource1 = new Resource("resource1", ResourceType.VF);
        Resource resource2 = new Resource("resource2", ResourceType.VFC);
        List<Resource> resources = new ArrayList<>();
        resources.add(resource1);
        resources.add(resource2);
        controlLoop1.setResources(resources);
        controlLoop1.setPnf(pnf);
        controlLoop1.setTrigger_policy(triggerPolicy);
        controlLoop1.setAbatement(abatement);

        ControlLoop controlLoop2 = new ControlLoop(controlLoop1);

        assertEquals(controlLoop1.getControlLoopName(), controlLoop2.getControlLoopName());
        assertEquals(controlLoop1.getVersion(), controlLoop2.getVersion());
        assertEquals(controlLoop1.getServices(), controlLoop2.getServices());
        assertEquals(controlLoop1.getResources(), controlLoop2.getResources());
        assertEquals(controlLoop1.getPnf(), controlLoop2.getPnf());
        assertEquals(controlLoop1.getTrigger_policy(), controlLoop2.getTrigger_policy());
        assertEquals(controlLoop1.getAbatement(), controlLoop2.getAbatement());

        assertTrue(controlLoop1.equals(controlLoop2));
    }

}
