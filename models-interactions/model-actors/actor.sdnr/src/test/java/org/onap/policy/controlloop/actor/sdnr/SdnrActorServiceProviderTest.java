/*-
 * SdnrActorServiceProviderTest
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.sdnr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

import org.junit.Test;
import org.onap.policy.controlloop.ControlLoopEventStatus;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.ControlLoopResponse;
import org.onap.policy.controlloop.ControlLoopTargetType;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.Target;
import org.onap.policy.controlloop.policy.TargetType;
import org.onap.policy.sdnr.PciRequest;
import org.onap.policy.sdnr.PciResponse;
import org.onap.policy.sdnr.PciResponseWrapper;
import org.onap.policy.sdnr.util.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SdnrActorServiceProviderTest {

    private static final String MODIFY_CONFIG = "ModifyConfig";

    private static final Logger logger = LoggerFactory.getLogger(SdnrActorServiceProviderTest.class);

    private static final VirtualControlLoopEvent onsetEvent;
    private static final ControlLoopOperation operation;
    private static final Policy policy;

    static {
        /*
         * Construct an onset. Using dummy AAI details since the code mandates AAI
         * details.
         */
        onsetEvent = new VirtualControlLoopEvent();
        onsetEvent.setClosedLoopControlName("closedLoopControlName-Test");
        onsetEvent.setRequestId(UUID.randomUUID());
        onsetEvent.setClosedLoopEventClient("tca.instance00001");
        onsetEvent.setTargetType(ControlLoopTargetType.VNF);
        onsetEvent.setTarget("generic-vnf.vnf-name");
        onsetEvent.setFrom("DCAE");
        onsetEvent.setClosedLoopAlarmStart(Instant.now());
        onsetEvent.setAai(new HashMap<>());
        onsetEvent.getAai().put("generic-vnf.vnf-name", "notused");
        onsetEvent.setClosedLoopEventStatus(ControlLoopEventStatus.ONSET);
        onsetEvent.setPayload("some payload");

        /* Construct an operation with an SDNR actor and ModifyConfig operation. */
        operation = new ControlLoopOperation();
        operation.setActor("SDNR");
        operation.setOperation(MODIFY_CONFIG);
        operation.setTarget("VNF");
        operation.setEnd(Instant.now());
        operation.setSubRequestId("1");

        /* Construct a policy specifying to modify configuration. */
        policy = new Policy();
        policy.setName("Modify PCI Config");
        policy.setDescription("Upon getting the trigger event, modify pci config");
        policy.setActor("SDNR");
        policy.setTarget(new Target(TargetType.VNF));
        policy.getTarget().setResourceID("Eace933104d443b496b8.nodes.heat.vpg");
        policy.setRecipe(MODIFY_CONFIG);
        policy.setPayload(null);
        policy.setRetry(2);
        policy.setTimeout(300);

    }

    @Test
    public void getControlLoopResponseTest() {
        PciRequest sdnrRequest;
        sdnrRequest = SdnrActorServiceProvider.constructRequest(onsetEvent, operation, policy).getBody();
        PciResponse sdnrResponse = new PciResponse(sdnrRequest);
        sdnrResponse.getStatus().setCode(200);
        sdnrResponse.getStatus().setValue("SDNR success");
        sdnrResponse.setPayload("sdnr payload ");
        /* Print out request as json to make sure serialization works */
        String jsonResponse = Serialization.gsonPretty.toJson(sdnrResponse);
        logger.info(jsonResponse);
        PciResponseWrapper pciResponseWrapper = new PciResponseWrapper();
        pciResponseWrapper.setBody(sdnrResponse);

        ControlLoopResponse clRsp = SdnrActorServiceProvider.getControlLoopResponse(pciResponseWrapper, onsetEvent);
        assertEquals(clRsp.getClosedLoopControlName(), onsetEvent.getClosedLoopControlName());
        assertEquals(clRsp.getRequestId(), onsetEvent.getRequestId());
        assertEquals(clRsp.getPolicyName(), onsetEvent.getPolicyName());
        assertEquals(clRsp.getPolicyVersion(), onsetEvent.getPolicyVersion());
        assertEquals(clRsp.getVersion(), onsetEvent.getVersion());
        assertEquals(clRsp.getFrom(), "SDNR");
        assertEquals(clRsp.getTarget(), "DCAE");
        assertEquals(clRsp.getPayload(), sdnrResponse.getPayload());
    }

    @Test
    public void constructModifyConfigRequestTest() {

        PciRequest sdnrRequest;
        sdnrRequest = SdnrActorServiceProvider.constructRequest(onsetEvent, operation, policy).getBody();

        /* The service provider must return a non null SDNR request */
        assertNotNull(sdnrRequest);

        /* A common header is required and cannot be null */
        assertNotNull(sdnrRequest.getCommonHeader());
        assertEquals(sdnrRequest.getCommonHeader().getRequestId(), onsetEvent.getRequestId());

        /* An action is required and cannot be null */
        assertNotNull(sdnrRequest.getAction());
        assertEquals(MODIFY_CONFIG, sdnrRequest.getAction());

        /* A payload is required and cannot be null */
        assertNotNull(sdnrRequest.getPayload());
        assertEquals("some payload", sdnrRequest.getPayload());

        logger.debug("SDNR Request: \n" + sdnrRequest.toString());

        /* Print out request as json to make sure serialization works */
        String jsonRequest = Serialization.gsonPretty.toJson(sdnrRequest);
        logger.debug("JSON Output: \n" + jsonRequest);

        /* The JSON string must contain the following fields */
        assertTrue(jsonRequest.contains("CommonHeader"));
        assertTrue(jsonRequest.contains("Action"));
        assertTrue(jsonRequest.contains(MODIFY_CONFIG));
        assertTrue(jsonRequest.contains("payload"));

        PciResponse sdnrResponse = new PciResponse(sdnrRequest);
        sdnrResponse.getStatus().setCode(200);
        sdnrResponse.getStatus().setValue("SDNR success");
        /* Print out request as json to make sure serialization works */
        String jsonResponse = Serialization.gsonPretty.toJson(sdnrResponse);
        logger.debug("JSON Output: \n" + jsonResponse);
    }

    @Test
    public void testMethods() {
        SdnrActorServiceProvider sp = new SdnrActorServiceProvider();

        assertEquals("SDNR", sp.actor());
        assertEquals(1, sp.operations().size());
    }
}
