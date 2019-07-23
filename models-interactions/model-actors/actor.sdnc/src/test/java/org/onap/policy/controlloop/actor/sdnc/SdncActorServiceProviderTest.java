/*-
 * ============LICENSE_START=======================================================
 * TestSdncActorServiceProvider
 * ================================================================================
 * Copyright (C) 2018-2019 Huawei. All rights reserved.
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

package org.onap.policy.controlloop.actor.sdnc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Objects;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.sdnc.SdncRequest;
import org.onap.policy.simulators.Util;

public class SdncActorServiceProviderTest {

    private static final String REROUTE = "Reroute";

    /**
     * Set up before test class.
     * @throws Exception if the A&AI simulator cannot be started
     */
    @BeforeClass
    public static void setUpSimulator() throws Exception {
        Util.buildAaiSim();
    }

    @AfterClass
    public static void tearDownSimulator() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    public void testConstructRequest() {
        VirtualControlLoopEvent onset = new VirtualControlLoopEvent();
        ControlLoopOperation operation = new ControlLoopOperation();

        Policy policy = new Policy();
        policy.setRecipe(REROUTE);

        SdncActorServiceProvider provider = new SdncActorServiceProvider();
        assertNull(provider.constructRequest(onset, operation, policy));

        onset.getAai().put("network-information.network-id", "network-5555");
        assertNull(provider.constructRequest(onset, operation, policy));

        assertNull(provider.constructRequest(onset, operation, policy));

        UUID requestId = UUID.randomUUID();
        onset.setRequestId(requestId);
        assertNull(provider.constructRequest(onset, operation, policy));

        assertNull(provider.constructRequest(onset, operation, policy));

        onset.getAai().put("service-instance.service-instance-id", "service-instance-01");
        assertNotNull(provider.constructRequest(onset, operation, policy));

        policy.setRecipe(REROUTE);
        assertNotNull(provider.constructRequest(onset, operation, policy));

        SdncRequest request =
                provider.constructRequest(onset, operation, policy);

        assertEquals(requestId, Objects.requireNonNull(request).getRequestId());
        assertEquals("reoptimize", request.getHealRequest().getRequestHeaderInfo().getSvcAction());
        assertEquals("ReoptimizeSOTNInstance", request.getHealRequest().getRequestInfo().getRequestAction());
        assertEquals("network-5555", request.getHealRequest().getNetworkInfo().getNetworkId());
        assertEquals("service-instance-01", request.getHealRequest().getServiceInfo().getServiceInstanceId());
    }

    @Test
    public void testMethods() {
        SdncActorServiceProvider sp = new SdncActorServiceProvider();

        assertEquals("SDNC", sp.actor());
        assertEquals(1, sp.operations().size());
        assertEquals(REROUTE, sp.operations().get(0));
    }
}
