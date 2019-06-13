/*-
 * ============LICENSE_START=======================================================
 * AppcServiceProviderTest
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.appclcm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.UUID;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.appclcm.LcmCommonHeader;
import org.onap.policy.appclcm.LcmRequest;
import org.onap.policy.appclcm.LcmRequestWrapper;
import org.onap.policy.appclcm.LcmResponse;
import org.onap.policy.appclcm.LcmResponseWrapper;
import org.onap.policy.common.endpoints.http.server.HttpServletServer;
import org.onap.policy.controlloop.ControlLoopEventStatus;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.ControlLoopTargetType;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.policy.Policy;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.controlloop.policy.Target;
import org.onap.policy.controlloop.policy.TargetType;
import org.onap.policy.simulators.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AppcLcmActorServiceProviderTest {

    private static final String VNF01 = "vnf01";

    private static final String VNF_ID_KEY = "vnf-id";

    private static final String REJECT = "REJECT";

    private static final String PARTIAL_FAILURE = "PARTIAL FAILURE";

    private static final String FAILURE = "FAILURE";

    private static final Logger logger = LoggerFactory.getLogger(AppcLcmActorServiceProviderTest.class);

    private static final VirtualControlLoopEvent onsetEvent;
    private static final ControlLoopOperation operation;
    private static final Policy policy;
    private static final LcmResponseWrapper dmaapResponse;

    private static final String RECIPE_RESTART = "Restart";
    private static final String RECIPE_REBUILD = "Rebuild";
    private static final String RECIPE_MIGRATE = "Migrate";

    static {
        /*
         * Construct an onset with an AAI subtag containing generic-vnf.vnf-id and a target type of
         * VM.
         */
        onsetEvent = new VirtualControlLoopEvent();
        onsetEvent.setClosedLoopControlName("closedLoopControlName-Test");
        onsetEvent.setRequestId(UUID.randomUUID());
        onsetEvent.setClosedLoopEventClient("tca.instance00001");
        onsetEvent.setTargetType(ControlLoopTargetType.VM);
        onsetEvent.setTarget("generic-vnf.vnf-name");
        onsetEvent.setFrom("DCAE");
        onsetEvent.setClosedLoopAlarmStart(Instant.now());
        onsetEvent.setAai(new HashMap<>());
        onsetEvent.getAai().put("generic-vnf.vnf-name", "fw0001vm001fw001");
        onsetEvent.setClosedLoopEventStatus(ControlLoopEventStatus.ONSET);

        /* Construct an operation with an APPC actor and restart operation. */
        operation = new ControlLoopOperation();
        operation.setActor("APPC");
        operation.setOperation(RECIPE_RESTART);
        operation.setTarget("VM");
        operation.setEnd(Instant.now());
        operation.setSubRequestId("1");

        /* Construct a policy specifying to restart vm. */
        policy = new Policy();
        policy.setName("Restart the VM");
        policy.setDescription("Upon getting the trigger event, restart the VM");
        policy.setActor("APPC");
        policy.setTarget(new Target(TargetType.VNF));
        policy.setRecipe(RECIPE_RESTART);
        policy.setPayload(null);
        policy.setRetry(2);
        policy.setTimeout(300);

        /* A sample DMAAP request wrapper. */
        LcmRequestWrapper dmaapRequest = new LcmRequestWrapper();
        dmaapRequest.setCorrelationId(onsetEvent.getRequestId().toString() + "-" + "1");
        dmaapRequest.setRpcName(policy.getRecipe().toLowerCase());
        dmaapRequest.setType("request");

        /* A sample DMAAP response wrapper */
        dmaapResponse = new LcmResponseWrapper();
        dmaapResponse.setCorrelationId(onsetEvent.getRequestId().toString() + "-" + "1");
        dmaapResponse.setRpcName(policy.getRecipe().toLowerCase());
        dmaapResponse.setType("response");

        /* A sample APPC LCM request. */
        LcmRequest appcRequest = new LcmRequest();

        /* The following code constructs a sample APPC LCM Request */
        appcRequest.setAction("restart");

        HashMap<String, String> actionIdentifiers = new HashMap<>();
        actionIdentifiers.put(VNF_ID_KEY, "trial-vnf-003");

        appcRequest.setActionIdentifiers(actionIdentifiers);

        LcmCommonHeader commonHeader = new LcmCommonHeader();
        commonHeader.setRequestId(onsetEvent.getRequestId());
        commonHeader.setSubRequestId("1");
        commonHeader.setOriginatorId(onsetEvent.getRequestId().toString());

        appcRequest.setCommonHeader(commonHeader);

        appcRequest.setPayload(null);

        dmaapRequest.setBody(appcRequest);

        /* The following code constructs a sample APPC LCM Response */
        LcmResponse appcResponse = new LcmResponse(appcRequest);
        appcResponse.getStatus().setCode(400);
        appcResponse.getStatus().setMessage("Restart Successful");

        dmaapResponse.setBody(appcResponse);
    }

    /**
     * Set up before test class.
     * @throws Exception if an error occurs
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
        HttpServletServer.factory.destroy();
    }

    /**
     * A test to construct an APPC LCM restart request.
     */
    @Test
    public void constructRestartRequestTest() {

        LcmRequestWrapper dmaapRequest =
                AppcLcmActorServiceProvider.constructRequest(onsetEvent, operation, policy, VNF01);

        /* The service provider must return a non null DMAAP request wrapper */
        assertNotNull(dmaapRequest);

        /* The DMAAP wrapper's type field must be request */
        assertEquals("request", dmaapRequest.getType());

        /* The DMAAP wrapper's body field cannot be null */
        assertNotNull(dmaapRequest.getBody());

        LcmRequest appcRequest = dmaapRequest.getBody();

        /* A common header is required and cannot be null */
        assertNotNull(appcRequest.getCommonHeader());
        assertEquals(appcRequest.getCommonHeader().getRequestId(), onsetEvent.getRequestId());

        /* An action is required and cannot be null */
        assertNotNull(appcRequest.getAction());
        assertEquals(RECIPE_RESTART, appcRequest.getAction());

        /* Action Identifiers are required and cannot be null */
        assertNotNull(appcRequest.getActionIdentifiers());
        assertNotNull(appcRequest.getActionIdentifiers().get(VNF_ID_KEY));
        assertEquals(VNF01, appcRequest.getActionIdentifiers().get(VNF_ID_KEY));

        logger.debug("APPC Request: \n" + appcRequest.toString());
    }

    /**
     * A test to process a successful APPC restart response.
     */
    @Test
    public void processRestartResponseSuccessTest() {
        AbstractMap.SimpleEntry<PolicyResult, String> result =
                AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.SUCCESS, result.getKey());
        assertEquals("Restart Successful", result.getValue());
    }

    /**
     * A test to map APPC response results to corresponding Policy results.
     */
    @Test
    public void appcToPolicyResultTest() {

        AbstractMap.SimpleEntry<PolicyResult, String> result;

        /* If APPC accepts, PolicyResult is null */
        dmaapResponse.getBody().getStatus().setCode(100);
        dmaapResponse.getBody().getStatus().setMessage("ACCEPTED");
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertNull(result.getKey());

        /* If APPC is successful, PolicyResult is success */
        dmaapResponse.getBody().getStatus().setCode(400);
        dmaapResponse.getBody().getStatus().setMessage("SUCCESS");
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.SUCCESS, result.getKey());

        /* If APPC returns an error, PolicyResult is failure exception */
        dmaapResponse.getBody().getStatus().setCode(200);
        dmaapResponse.getBody().getStatus().setMessage("ERROR");
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* If APPC rejects, PolicyResult is failure exception */
        dmaapResponse.getBody().getStatus().setCode(300);
        dmaapResponse.getBody().getStatus().setMessage(REJECT);
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* Test multiple reject codes */
        dmaapResponse.getBody().getStatus().setCode(306);
        dmaapResponse.getBody().getStatus().setMessage(REJECT);
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        dmaapResponse.getBody().getStatus().setCode(313);
        dmaapResponse.getBody().getStatus().setMessage(REJECT);
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* If APPC returns failure, PolicyResult is failure */
        dmaapResponse.getBody().getStatus().setCode(401);
        dmaapResponse.getBody().getStatus().setMessage(FAILURE);
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE, result.getKey());

        /* Test multiple failure codes */
        dmaapResponse.getBody().getStatus().setCode(406);
        dmaapResponse.getBody().getStatus().setMessage(FAILURE);
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE, result.getKey());

        dmaapResponse.getBody().getStatus().setCode(450);
        dmaapResponse.getBody().getStatus().setMessage(FAILURE);
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE, result.getKey());

        /* If APPC returns partial success, PolicyResult is failure exception */
        dmaapResponse.getBody().getStatus().setCode(500);
        dmaapResponse.getBody().getStatus().setMessage("PARTIAL SUCCESS");
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* If APPC returns partial failure, PolicyResult is failure exception */
        dmaapResponse.getBody().getStatus().setCode(501);
        dmaapResponse.getBody().getStatus().setMessage(PARTIAL_FAILURE);
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* Test multiple partial failure codes */
        dmaapResponse.getBody().getStatus().setCode(599);
        dmaapResponse.getBody().getStatus().setMessage(PARTIAL_FAILURE);
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        dmaapResponse.getBody().getStatus().setCode(550);
        dmaapResponse.getBody().getStatus().setMessage(PARTIAL_FAILURE);
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* If APPC code is unknown to Policy, PolicyResult is failure exception */
        dmaapResponse.getBody().getStatus().setCode(700);
        dmaapResponse.getBody().getStatus().setMessage("UNKNOWN");
        result = AppcLcmActorServiceProvider.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());
    }

    /*
     * This test ensures that that if the the source entity is also the target entity, the source
     * will be used for the APPC request.
     */
    @Test
    public void sourceIsTargetTest() throws Exception {
        String resourceId = "82194af1-3c2c-485a-8f44-420e22a9eaa4";
        String targetVnfId = AppcLcmActorServiceProvider.vnfNamedQuery(resourceId, VNF01, "http://localhost:6666",
                        "AAI", "AAI");
        assertNotNull(targetVnfId);
        assertEquals(VNF01, targetVnfId);
    }

    /*
     * This test exercises getters not exercised in other tests.
     */
    @Test
    public void testMethods() {
        AppcLcmActorServiceProvider sp = new AppcLcmActorServiceProvider();

        assertEquals("APPC", sp.actor());
        assertEquals(4, sp.recipes().size());
        assertEquals("VM", sp.recipeTargets(RECIPE_RESTART).get(0));
        assertEquals("vm-id", sp.recipePayloads(RECIPE_RESTART).get(0));
    }

    @Test
    public void payloadNotPassedWhenNotSupportedByRecipe() {
        //given
        Policy migratePolicy = constructPolicyWithRecipe(RECIPE_MIGRATE);
        Policy rebuildPolicy = constructPolicyWithRecipe(RECIPE_REBUILD);
        Policy restartPolicy = constructPolicyWithRecipe(RECIPE_RESTART);

        // when
        LcmRequestWrapper migrateRequest =
            AppcLcmActorServiceProvider.constructRequest(onsetEvent, operation, migratePolicy, VNF01);
        LcmRequestWrapper rebuildRequest =
            AppcLcmActorServiceProvider.constructRequest(onsetEvent, operation, rebuildPolicy, VNF01);
        LcmRequestWrapper restartRequest =
            AppcLcmActorServiceProvider.constructRequest(onsetEvent, operation, restartPolicy, VNF01);

        // then
        assertNull(migrateRequest.getBody().getPayload());
        assertNull(rebuildRequest.getBody().getPayload());
        assertNull(restartRequest.getBody().getPayload());
    }

    @Test
    public void payloadNotPassedWhenNotSuppliedOrEmpty() {
        //given
        Policy noPayloadPolicy = constructHealthCheckPolicyWithPayload(null);
        Policy emptyPayloadPolicy = constructHealthCheckPolicyWithPayload(new HashMap<>());

        // when
        LcmRequestWrapper noPayloadRequest =
            AppcLcmActorServiceProvider.constructRequest(onsetEvent, operation, noPayloadPolicy, VNF01);
        LcmRequestWrapper emptyPayloadRequest =
            AppcLcmActorServiceProvider.constructRequest(onsetEvent, operation, emptyPayloadPolicy, VNF01);


        // then
        assertNull(noPayloadRequest.getBody().getPayload());
        assertNull(emptyPayloadRequest.getBody().getPayload());
    }

    @Test
    public void payloadParsedProperlyForSinglePayloadParameter() {
        // given
        HashMap<String, String> payload = new HashMap<>();
        payload.put("requestParameters", "{\"host-ip-address\":\"10.183.37.25\"}");
        Policy otherPolicy = constructHealthCheckPolicyWithPayload(payload);

        // when
        LcmRequestWrapper dmaapRequest =
            AppcLcmActorServiceProvider.constructRequest(onsetEvent, operation, otherPolicy, VNF01);

        // then
        assertEquals(dmaapRequest.getBody().getPayload(),
            "{\"requestParameters\": {\"host-ip-address\":\"10.183.37.25\"}}");
    }


    @Test
    public void payloadParsedProperlyForMultiplePayloadParameters() {
        // given
        HashMap<String, String> payload = new HashMap<>();
        payload.put("requestParameters", "{\"host-ip-address\":\"10.183.37.25\"}");
        payload.put("configurationParameters", "[{\"ip-addr\":\"$.vf-module-topology.vf-module-parameters.param[9]\","
            + "\"oam-ip-addr\":\"$.vf-module-topology.vf-module-parameters.param[16]\","
            + "\"enabled\":\"$.vf-module-topology.vf-module-parameters.param[23]\"}]");
        Policy otherPolicy = constructHealthCheckPolicyWithPayload(payload);

        // when
        LcmRequestWrapper dmaapRequest =
            AppcLcmActorServiceProvider.constructRequest(onsetEvent, operation, otherPolicy, VNF01);

        // then
        assertEquals(dmaapRequest.getBody().getPayload(),
            "{\"requestParameters\": "
                + "{\"host-ip-address\":\"10.183.37.25\"},"
                + "\"configurationParameters\": "
                + "[{\"ip-addr\":\"$.vf-module-topology.vf-module-parameters.param[9]\","
                + "\"oam-ip-addr\":\"$.vf-module-topology.vf-module-parameters.param[16]\","
                + "\"enabled\":\"$.vf-module-topology.vf-module-parameters.param[23]\"}]"
                + "}");
    }

    private Policy constructHealthCheckPolicyWithPayload(HashMap<String, String> payload) {
        return constructHealthCheckPolicyWithPayloadAndRecipe(payload, "Health-Check");
    }

    private Policy constructPolicyWithRecipe(String recipe) {
        return constructHealthCheckPolicyWithPayloadAndRecipe(null, recipe);
    }

    private Policy constructHealthCheckPolicyWithPayloadAndRecipe(HashMap<String, String> payload, String recipe) {
        Policy otherPolicy = new Policy();
        otherPolicy.setName("Perform health check");
        otherPolicy.setDescription("Upon getting the trigger event, perform health check");
        otherPolicy.setActor("APPC");
        otherPolicy.setTarget(new Target(TargetType.VNF));
        otherPolicy.setRecipe(recipe);
        otherPolicy.setPayload(payload);
        otherPolicy.setRetry(2);
        otherPolicy.setTimeout(300);
        return otherPolicy;
    }
}
