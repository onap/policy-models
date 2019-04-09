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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.io.IOUtils;
import org.eclipse.persistence.exceptions.JAXBException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.aai.AaiGetVnfResponse;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.policy.Policy;
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

        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, null, null, null));

        onset.getAai().put("generic-vnf.vnf-id", "dorothy.gale.1939");
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, null, null, null));

        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, "http://localhost:6666",
                "AAI", "AAI"));

        UUID requestId = UUID.randomUUID();
        onset.setRequestId(requestId);
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, "http://localhost:6666",
                "AAI", "AAI"));

        onset.getAai().put("generic-vnf.vnf-name", "Dorothy");
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, "http://localhost:6666",
                "AAI", null));

        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, "http://localhost:6666",
                "AAI", "AAI"));

        onset.getAai().put("service-instance.service-instance-id", "");
        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, null, "http://localhost:6666",
                "AAI", "AAI"));

        assertNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, new AaiGetVnfResponse(),
                "http://localhost:6666", "AAI", "AAI"));

        policy.setRecipe("Restart");
        assertNotNull(VfcActorServiceProvider.constructRequest(onset, operation, policy, new AaiGetVnfResponse(),
                "http://localhost:6666", "AAI", "AAI"));

        VfcRequest request = VfcActorServiceProvider.constructRequest(onset, operation, policy, new AaiGetVnfResponse(),
                "http://localhost:6666", "AAI", "AAI");

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

    @Test
    public void testConstructRequestCq() throws IOException, JAXBException {
        VirtualControlLoopEvent onset = new VirtualControlLoopEvent();
        ControlLoopOperation operation = new ControlLoopOperation();

        Policy policy = new Policy();
        policy.setRecipe("GoToOz");

        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy, null));

        onset.getAai().put("generic-vnf.vnf-id", "dorothy.gale.1939");
        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy, null));


        UUID requestId = UUID.randomUUID();
        onset.setRequestId(requestId);
        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy, null));

        onset.getAai().put("generic-vnf.vnf-name", "Dorothy");
        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy, null));


        onset.getAai().put("service-instance.service-instance-id", "");
        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy, null));

        assertNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy,
                loadAaiResponse("aai/AaiCqResponse.json")));

        policy.setRecipe("Restart");
        assertNotNull(VfcActorServiceProvider.constructRequestCq(onset, operation, policy,
                loadAaiResponse("aai/AaiCqResponse.json")));

        VfcRequest request = VfcActorServiceProvider.constructRequestCq(onset, operation, policy,
                loadAaiResponse("aai/AaiCqResponse.json"));

        assertEquals(requestId, Objects.requireNonNull(request).getRequestId());
        assertEquals("dorothy.gale.1939", request.getHealRequest().getVnfInstanceId());
        assertEquals("restartvm", request.getHealRequest().getAdditionalParams().getAction());
    }

    /**
     * Reads an AAI vserver named-query response from a file.
     *
     * @param fileName name of the file containing the JSON response
     * @return output from the AAI vserver named-query
     * @throws IOException if the file cannot be read
     * @throws JAXBException throws JAXBException
     */
    private AaiCqResponse loadAaiResponse(String fileName) throws IOException, JAXBException {
        String resp = IOUtils.toString(getClass().getResource(fileName), StandardCharsets.UTF_8);
        return new AaiCqResponse(resp);
    }

}
