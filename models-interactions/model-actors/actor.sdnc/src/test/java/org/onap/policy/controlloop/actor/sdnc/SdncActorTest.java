/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018-2019 Huawei. All rights reserved.
 * Modifications Copyright (C) 2018-2020 AT&T Corp. All rights reserved.
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

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actor.test.BasicActor;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.sdnc.SdncRequest;

public class SdncActorTest extends BasicActor {

    private static final String REROUTE = RerouteOperation.NAME;

    /**
     * Set up before test class.
     *
     * @throws Exception if the A&AI simulator cannot be started
     */
    @BeforeClass
    public static void setUpSimulator() throws Exception {
        org.onap.policy.simulators.Util.buildAaiSim();
    }

    @AfterClass
    public static void tearDownSimulator() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    public void testSdncActor() {
        final SdncActor prov = new SdncActor();

        // verify that it has the operators we expect
        var expected = Arrays.asList(BandwidthOnDemandOperation.NAME, RerouteOperation.NAME).stream().sorted()
                        .collect(Collectors.toList());
        var actual = prov.getOperationNames().stream().sorted().collect(Collectors.toList());

        assertEquals(expected.toString(), actual.toString());

        // verify that it all plugs into the ActorService
        verifyActorService(SdncActor.NAME, "service.yaml");
    }

    @Test
    public void testConstructRequest() {
        VirtualControlLoopEvent onset = new VirtualControlLoopEvent();
        ControlLoopOperation operation = new ControlLoopOperation();

        Policy policy = new Policy();
        policy.setRecipe(REROUTE);

        SdncActor provider = new SdncActor();
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

        SdncRequest request = provider.constructRequest(onset, operation, policy);

        assertEquals(requestId, Objects.requireNonNull(request).getRequestId());
        assertEquals("reoptimize", request.getHealRequest().getRequestHeaderInfo().getSvcAction());
        assertEquals("ReoptimizeSOTNInstance", request.getHealRequest().getRequestInfo().getRequestAction());
        assertEquals("network-5555", request.getHealRequest().getNetworkInfo().getNetworkId());
        assertEquals("service-instance-01", request.getHealRequest().getServiceInfo().getServiceInstanceId());
    }

    @Test
    public void testMethods() {
        SdncActor sp = new SdncActor();

        assertEquals("SDNC", sp.actor());
        assertEquals(1, sp.recipes().size());
        assertEquals(REROUTE, sp.recipes().get(0));
        assertEquals("VM", sp.recipeTargets(REROUTE).get(0));
        assertEquals(0, sp.recipePayloads(REROUTE).size());
    }
}
