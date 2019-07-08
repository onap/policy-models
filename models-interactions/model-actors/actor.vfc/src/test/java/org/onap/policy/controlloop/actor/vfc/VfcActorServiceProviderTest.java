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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.aai.AaiGetVnfResponse;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.simulators.Util;
import org.onap.policy.vfc.VfcRequest;

public class VfcActorServiceProviderTest {

    private static final String LOCAL_URL = "http://localhost:6666";
    private static final String DOROTHY_GALE_1939 = "dorothy.gale.1939";
    private static final String CQ_RESPONSE_JSON = "aai/AaiCqResponse.json";
    private static final String RESTART = "Restart";

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
        policy.setRecipe("GoToOz");

        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, null, null, null));

        onset.getAai().put("generic-vnf.vnf-id", DOROTHY_GALE_1939);
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, null, null, null));

        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, LOCAL_URL,
                "AAI", "AAI"));

        UUID requestId = UUID.randomUUID();
        onset.setRequestId(requestId);
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, LOCAL_URL,
                "AAI", "AAI"));

        onset.getAai().put("generic-vnf.vnf-name", "Dorothy");
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, LOCAL_URL,
                "AAI", null));

        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, LOCAL_URL,
                "AAI", "AAI"));

        onset.getAai().put("service-instance.service-instance-id", "");
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, LOCAL_URL,
                "AAI", "AAI"));

        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, new AaiGetVnfResponse(),
                LOCAL_URL, "AAI", "AAI"));

        policy.setRecipe(RESTART);
        assertNotNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, new AaiGetVnfResponse(),
                LOCAL_URL, "AAI", "AAI"));

        VfcRequest request = VfcActorServiceProvider.constructRequest(onset, operation, policy, new AaiGetVnfResponse(),
                LOCAL_URL, "AAI", "AAI");

        assertEquals(requestId, Objects.requireNonNull(request).getRequestId());
        assertEquals(DOROTHY_GALE_1939, request.getHealRequest().getVnfInstanceId());
        assertEquals("restartvm", request.getHealRequest().getAdditionalParams().getAction());
    }

    @Test
    public void testMethods() {
        VfcActorServiceProvider sp = new VfcActorServiceProvider();

        assertEquals("VFC", sp.actor());
        assertEquals(1, sp.recipes().size());
        assertEquals(RESTART, sp.recipes().get(0));
        assertEquals("VM", sp.recipeTargets(RESTART).get(0));
        assertEquals(0, sp.recipePayloads(RESTART).size());
    }

    @Test
    public void testConstructRequestCq() throws IOException {
        VirtualControlLoopEvent onset = new VirtualControlLoopEvent();
        ControlLoopOperation operation = new ControlLoopOperation();

        Policy policy = new Policy();
        policy.setRecipe("GoToOz");

        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy, null));

        onset.getAai().put("generic-vnf.vnf-id", DOROTHY_GALE_1939);
        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy, null));


        UUID requestId = UUID.randomUUID();
        onset.setRequestId(requestId);
        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy, null));

        onset.getAai().put("generic-vnf.vnf-name", "Dorothy");
        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy, null));


        onset.getAai().put("service-instance.service-instance-id", "");
        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy, null));

        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy,
                loadAaiResponse(CQ_RESPONSE_JSON)));

        policy.setRecipe(RESTART);
        assertNotNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy,
                loadAaiResponse(CQ_RESPONSE_JSON)));

        VfcRequest request = VfcActorServiceProvider.constructRequestCq(onset, operation, policy,
                loadAaiResponse(CQ_RESPONSE_JSON));

        assertEquals(requestId, Objects.requireNonNull(request).getRequestId());
        assertEquals(DOROTHY_GALE_1939, request.getHealRequest().getVnfInstanceId());
        assertEquals("restartvm", request.getHealRequest().getAdditionalParams().getAction());
    }

    /**
     * Reads an AAI vserver named-query response from a file.
     *
     * @param fileName name of the file containing the JSON response
     * @return output from the AAI vserver named-query
     * @throws IOException if the file cannot be read
     */
    private AaiCqResponse loadAaiResponse(String fileName) throws IOException {
        String resp = IOUtils.toString(getClass().getResource(fileName), StandardCharsets.UTF_8);
        return new AaiCqResponse(resp);
    }

}
