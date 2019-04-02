/*-
 * ============LICENSE_START=======================================================
 * ONAP - Policy Drools Applications
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2018-2019 AT&T Corp. All rights reserved.
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

package org.onap.policy.controlloop.actor.vfc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Objects;
import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.aai.AaiGetVnfResponse;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.drools.system.PolicyEngine;
import org.onap.policy.simulators.Util;
import org.onap.policy.vfc.VfcRequest;

public class VfcActorServiceProviderTest {

    /**
     * Set up for test class.
     */
    @BeforeClass
    public static void setUpSimulator() {
        try {
            Util.buildAaiSim();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void tearDownSimulator() {
        HttpServletServer.factory.destroy();
    }

    @Test
    public void testConstructRequest() {
        VirtualControlLoopEvent onset = new VirtualControlLoopEvent();
        ControlLoopOperation operation = new ControlLoopOperation();

        Policy policy = new Policy();
        policy.setRecipe("GoToOz");

        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null));

        onset.getAai().put("generic-vnf.vnf-id", "dorothy.gale.1939");
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null));

        PolicyEngine.manager.setEnvironmentProperty("aai.url", "http://localhost:6666");
        PolicyEngine.manager.setEnvironmentProperty("aai.username", "AAI");
        PolicyEngine.manager.setEnvironmentProperty("aai.password", "AAI");
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null));

        UUID requestId = UUID.randomUUID();
        onset.setRequestId(requestId);
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null));

        onset.getAai().put("generic-vnf.vnf-name", "Dorothy");
        PolicyEngine.manager.getEnvironment().remove("aai.password");
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null));

        PolicyEngine.manager.setEnvironmentProperty("aai.password", "AAI");
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null));

        onset.getAai().put("service-instance.service-instance-id", "");
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null));

        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, new AaiGetVnfResponse()));

        policy.setRecipe("Restart");
        assertNotNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, new AaiGetVnfResponse()));

        VfcRequest request =
                VfcActorServiceProvider.constructRequest(onset, operation, policy, new AaiGetVnfResponse());

        assertEquals(requestId, Objects.requireNonNull(request).getRequestId());
        assertEquals("dorothy.gale.1939", request.getHealRequest().getVnfInstanceId());
        assertEquals("restartvm", request.getHealRequest().getAdditionalParams().getAction());
    }

    @Test
    public void testMethods() {
        VfcActorServiceProvider sp = new VfcActorServiceProvider();

        assertEquals("VFC", sp.actor());
        assertEquals(1, sp.recipes().size());
        assertEquals("Restart", sp.recipes().get(0));
        assertEquals("VM", sp.recipeTargets("Restart").get(0));
        assertEquals(0, sp.recipePayloads("Restart").size());
    }
}
