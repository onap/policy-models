/*-
 * ============LICENSE_START=======================================================
 * AppcServiceProviderTest
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

package org.onap.policy.controlloop.actor.appc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.appc.Request;
import org.onap.policy.appc.Response;
import org.onap.policy.appc.ResponseCode;
import org.onap.policy.appc.util.Serialization;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.controlloop.ControlLoopEventStatus;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.ControlLoopTargetType;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.Target;
import org.onap.policy.controlloop.policy.TargetType;
import org.onap.policy.simulators.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppcServiceProviderTest {

    private static final String GENERIC_VNF_ID = "generic-vnf.vnf-id";

    private static final String MODIFY_CONFIG = "ModifyConfig";

    private static final String JSON_OUTPUT = "JSON Output: \n";

    private static final Logger logger = LoggerFactory.getLogger(AppcServiceProviderTest.class);

    private static final VirtualControlLoopEvent onsetEvent;
    private static final ControlLoopOperation operation;
    private static final Policy policy;

    private static final String KEY1 = "my-keyA";
    private static final String KEY2 = "my-keyB";
    private static final String SUBKEY = "sub-key";

    private static final String VALUE1 = "'my-value'".replace('\'', '"');
    private static final String VALUE2 = "{'sub-key':20}".replace('\'', '"');
    private static final String SUBVALUE = "20";

    static {
        /*
         * Construct an onset with an AAI subtag containing generic-vnf.vnf-id and a target type of
         * VM.
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
        onsetEvent.getAai().put("generic-vnf.vnf-name", "fw0001vm001fw001");
        onsetEvent.setClosedLoopEventStatus(ControlLoopEventStatus.ONSET);

        /* Construct an operation with an APPC actor and ModifyConfig operation. */
        operation = new ControlLoopOperation();
        operation.setActor("APPC");
        operation.setOperation(MODIFY_CONFIG);
        operation.setTarget("VNF");
        operation.setEnd(Instant.now());
        operation.setSubRequestId("1");

        /* Construct a policy specifying to modify configuration. */
        policy = new Policy();
        policy.setName("Modify Packet Generation Config");
        policy.setDescription("Upon getting the trigger event, modify packet gen config");
        policy.setActor("APPC");
        policy.setTarget(new Target(TargetType.VNF));
        policy.getTarget().setResourceID("Eace933104d443b496b8.nodes.heat.vpg");
        policy.setRecipe(MODIFY_CONFIG);
        policy.setPayload(null);
        policy.setRetry(2);
        policy.setTimeout(300);

    }

    /**
     * Set up before test class.
     * @throws Exception if the A&AI simulator cannot be started
     */
    @BeforeClass
    public static void setUpSimulator() throws Exception {
        Util.buildAaiSim();
    }

    /**
     * Tear down after test class.
     */
    @AfterClass
    public static void tearDownSimulator() {
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    public void constructModifyConfigRequestTest() {
        policy.setPayload(new HashMap<>());
        policy.getPayload().put(KEY1, VALUE1);
        policy.getPayload().put(KEY2, VALUE2);

        Request appcRequest;
        appcRequest = AppcActorServiceProvider.constructRequest(onsetEvent, operation, policy, "vnf01");

        /* The service provider must return a non null APPC request */
        assertNotNull(appcRequest);

        /* A common header is required and cannot be null */
        assertNotNull(appcRequest.getCommonHeader());
        assertEquals(appcRequest.getCommonHeader().getRequestId(), onsetEvent.getRequestId());

        /* An action is required and cannot be null */
        assertNotNull(appcRequest.getAction());
        assertEquals(MODIFY_CONFIG, appcRequest.getAction());

        /* A payload is required and cannot be null */
        assertNotNull(appcRequest.getPayload());
        assertTrue(appcRequest.getPayload().containsKey(GENERIC_VNF_ID));
        assertNotNull(appcRequest.getPayload().get(GENERIC_VNF_ID));
        assertTrue(appcRequest.getPayload().containsKey(KEY1));
        assertTrue(appcRequest.getPayload().containsKey(KEY2));

        logger.debug("APPC Request: \n" + appcRequest.toString());

        /* Print out request as json to make sure serialization works */
        String jsonRequest = Serialization.gsonPretty.toJson(appcRequest);
        logger.debug(JSON_OUTPUT + jsonRequest);

        /* The JSON string must contain the following fields */
        assertTrue(jsonRequest.contains("CommonHeader"));
        assertTrue(jsonRequest.contains("Action"));
        assertTrue(jsonRequest.contains(MODIFY_CONFIG));
        assertTrue(jsonRequest.contains("Payload"));
        assertTrue(jsonRequest.contains(GENERIC_VNF_ID));
        assertTrue(jsonRequest.contains(KEY1));
        assertTrue(jsonRequest.contains(KEY2));
        assertTrue(jsonRequest.contains(SUBKEY));
        assertTrue(jsonRequest.contains(SUBVALUE));

        Response appcResponse = new Response(appcRequest);
        appcResponse.getStatus().setCode(ResponseCode.SUCCESS.getValue());
        appcResponse.getStatus().setDescription("AppC success");
        /* Print out request as json to make sure serialization works */
        String jsonResponse = Serialization.gsonPretty.toJson(appcResponse);
        logger.debug(JSON_OUTPUT + jsonResponse);
    }

    @Test
    public void constructModifyConfigRequestTest_NullPayload() {

        Request appcRequest;
        appcRequest = AppcActorServiceProvider.constructRequest(onsetEvent, operation, policy, "vnf01");

        /* The service provider must return a non null APPC request */
        assertNotNull(appcRequest);

        /* A common header is required and cannot be null */
        assertNotNull(appcRequest.getCommonHeader());
        assertEquals(appcRequest.getCommonHeader().getRequestId(), onsetEvent.getRequestId());

        /* An action is required and cannot be null */
        assertNotNull(appcRequest.getAction());
        assertEquals(MODIFY_CONFIG, appcRequest.getAction());

        /* A payload is required and cannot be null */
        assertNotNull(appcRequest.getPayload());
        assertTrue(appcRequest.getPayload().containsKey(GENERIC_VNF_ID));
        assertNotNull(appcRequest.getPayload().get(GENERIC_VNF_ID));

        logger.debug("APPC Request: \n" + appcRequest.toString());

        /* Print out request as json to make sure serialization works */
        String jsonRequest = Serialization.gsonPretty.toJson(appcRequest);
        logger.debug(JSON_OUTPUT + jsonRequest);

        /* The JSON string must contain the following fields */
        assertTrue(jsonRequest.contains("CommonHeader"));
        assertTrue(jsonRequest.contains("Action"));
        assertTrue(jsonRequest.contains(MODIFY_CONFIG));
        assertTrue(jsonRequest.contains("Payload"));
        assertTrue(jsonRequest.contains(GENERIC_VNF_ID));

        Response appcResponse = new Response(appcRequest);
        appcResponse.getStatus().setCode(ResponseCode.SUCCESS.getValue());
        appcResponse.getStatus().setDescription("AppC success");
        /* Print out request as json to make sure serialization works */
        String jsonResponse = Serialization.gsonPretty.toJson(appcResponse);
        logger.debug(JSON_OUTPUT + jsonResponse);
    }

    @Test
    public void testMethods() {
        AppcActorServiceProvider sp = new AppcActorServiceProvider();

        assertEquals("APPC", sp.actor());
        assertEquals(4, sp.operations().size());
    }
}
