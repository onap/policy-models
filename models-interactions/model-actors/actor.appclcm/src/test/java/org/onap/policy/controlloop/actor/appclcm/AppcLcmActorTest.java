/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
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
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.appclcm.AppcLcmBody;
import org.onap.policy.appclcm.AppcLcmCommonHeader;
import org.onap.policy.appclcm.AppcLcmDmaapWrapper;
import org.onap.policy.appclcm.AppcLcmInput;
import org.onap.policy.appclcm.AppcLcmOutput;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
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

public class AppcLcmActorTest {

    private static final String VNF01 = "vnf01";

    private static final String VNF_ID_KEY = "vnf-id";

    private static final String REJECT = "REJECT";

    private static final String PARTIAL_FAILURE = "PARTIAL FAILURE";

    private static final String FAILURE = "FAILURE";

    private static final Logger logger = LoggerFactory.getLogger(AppcLcmActorTest.class);

    private static final VirtualControlLoopEvent onsetEvent;
    private static final ControlLoopOperation operation;
    private static final Policy policy;
    private static final AppcLcmDmaapWrapper dmaapResponse;

    private static final String RECIPE_RESTART = "Restart";
    private static final String RECIPE_REBUILD = "Rebuild";
    private static final String RECIPE_MIGRATE = "Migrate";

    static {
        /*
         * Construct an onset with an AAI subtag containing generic-vnf.vnf-id and a
         * target type of VM.
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
        AppcLcmDmaapWrapper dmaapRequest = new AppcLcmDmaapWrapper();
        dmaapRequest.setCorrelationId(onsetEvent.getRequestId().toString() + "-" + "1");
        dmaapRequest.setRpcName(policy.getRecipe().toLowerCase());
        dmaapRequest.setType("request");

        /* A sample DMAAP response wrapper */
        dmaapResponse = new AppcLcmDmaapWrapper();
        dmaapResponse.setCorrelationId(onsetEvent.getRequestId().toString() + "-" + "1");
        dmaapResponse.setRpcName(policy.getRecipe().toLowerCase());
        dmaapResponse.setType("response");

        /* A sample APPC LCM request. */
        AppcLcmInput appcRequest = new AppcLcmInput();

        /* The following code constructs a sample APPC LCM Request */
        appcRequest.setAction("restart");

        HashMap<String, String> actionIdentifiers = new HashMap<>();
        actionIdentifiers.put(VNF_ID_KEY, "trial-vnf-003");

        appcRequest.setActionIdentifiers(actionIdentifiers);

        AppcLcmCommonHeader commonHeader = new AppcLcmCommonHeader();
        commonHeader.setRequestId(onsetEvent.getRequestId());
        commonHeader.setSubRequestId("1");
        commonHeader.setOriginatorId(onsetEvent.getRequestId().toString());

        appcRequest.setCommonHeader(commonHeader);

        appcRequest.setPayload(null);

        AppcLcmBody appcBody = new AppcLcmBody();
        appcBody.setInput(appcRequest);

        dmaapRequest.setBody(appcBody);

        /* The following code constructs a sample APPC LCM Response */
        AppcLcmOutput appcResponse = new AppcLcmOutput(appcRequest);
        appcResponse.getStatus().setCode(400);
        appcResponse.getStatus().setMessage("Restart Successful");

        appcBody.setOutput(appcResponse);

        dmaapResponse.setBody(appcBody);
    }

    /**
     * Set up before test class.
     *
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
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    public void testConstructor() {
        AppcLcmActor prov = new AppcLcmActor();
        assertEquals(-1, prov.getSequenceNumber());

        // verify that it has the operators we expect
        var expected = AppcLcmConstants.COMBINED_OPERATION_NAMES.stream().sorted().collect(Collectors.toList());
        var actual = prov.getOperationNames().stream().sorted().collect(Collectors.toList());

        assertEquals(expected.toString(), actual.toString());
    }

    /**
     * A test to construct an APPC LCM restart request.
     */
    @Test
    public void constructRestartRequestTest() {

        AppcLcmDmaapWrapper dmaapRequest =
                        AppcLcmActor.constructRequest(onsetEvent, operation, policy, VNF01);

        /* The service provider must return a non null DMAAP request wrapper */
        assertNotNull(dmaapRequest);

        /* The DMAAP wrapper's type field must be request */
        assertEquals("request", dmaapRequest.getType());

        /* The DMAAP wrapper's body field cannot be null */
        assertNotNull(dmaapRequest.getBody());

        AppcLcmInput appcRequest = dmaapRequest.getBody().getInput();

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
                        AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.SUCCESS, result.getKey());
        assertEquals("Restart Successful", result.getValue());
    }

    /**
     * A test to assert that a null pointer exception is thrown if the APPC response body
     * is null.
     */
    @Test(expected = NullPointerException.class)
    public void processNullBodyResponseTest() {
        AppcLcmActor.processResponse(new AppcLcmDmaapWrapper());
    }

    /**
     * A test to assert that a null pointer exception is thrown if the APPC response
     * output is null.
     */
    @Test(expected = NullPointerException.class)
    public void processNullOutputResponseTest() {
        AppcLcmDmaapWrapper dmaapWrapper = new AppcLcmDmaapWrapper();
        dmaapWrapper.setBody(new AppcLcmBody());
        AppcLcmActor.processResponse(dmaapWrapper);
    }

    /**
     * A test to map APPC response results to corresponding Policy results.
     */
    @Test
    public void appcToPolicyResultTest() {

        AbstractMap.SimpleEntry<PolicyResult, String> result;

        /* If APPC accepts, PolicyResult is null */
        dmaapResponse.getBody().getOutput().getStatus().setCode(100);
        dmaapResponse.getBody().getOutput().getStatus().setMessage("ACCEPTED");
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertNull(result.getKey());

        /* If APPC is successful, PolicyResult is success */
        dmaapResponse.getBody().getOutput().getStatus().setCode(400);
        dmaapResponse.getBody().getOutput().getStatus().setMessage("SUCCESS");
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.SUCCESS, result.getKey());

        /* If APPC returns an error, PolicyResult is failure exception */
        dmaapResponse.getBody().getOutput().getStatus().setCode(200);
        dmaapResponse.getBody().getOutput().getStatus().setMessage("ERROR");
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* If APPC rejects, PolicyResult is failure exception */
        dmaapResponse.getBody().getOutput().getStatus().setCode(300);
        dmaapResponse.getBody().getOutput().getStatus().setMessage(REJECT);
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* Test multiple reject codes */
        dmaapResponse.getBody().getOutput().getStatus().setCode(306);
        dmaapResponse.getBody().getOutput().getStatus().setMessage(REJECT);
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        dmaapResponse.getBody().getOutput().getStatus().setCode(313);
        dmaapResponse.getBody().getOutput().getStatus().setMessage(REJECT);
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* If APPC returns failure, PolicyResult is failure */
        dmaapResponse.getBody().getOutput().getStatus().setCode(401);
        dmaapResponse.getBody().getOutput().getStatus().setMessage(FAILURE);
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE, result.getKey());

        /* Test multiple failure codes */
        dmaapResponse.getBody().getOutput().getStatus().setCode(406);
        dmaapResponse.getBody().getOutput().getStatus().setMessage(FAILURE);
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE, result.getKey());

        dmaapResponse.getBody().getOutput().getStatus().setCode(450);
        dmaapResponse.getBody().getOutput().getStatus().setMessage(FAILURE);
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE, result.getKey());

        /* If APPC returns partial success, PolicyResult is failure exception */
        dmaapResponse.getBody().getOutput().getStatus().setCode(500);
        dmaapResponse.getBody().getOutput().getStatus().setMessage("PARTIAL SUCCESS");
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* If APPC returns partial failure, PolicyResult is failure exception */
        dmaapResponse.getBody().getOutput().getStatus().setCode(501);
        dmaapResponse.getBody().getOutput().getStatus().setMessage(PARTIAL_FAILURE);
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* Test multiple partial failure codes */
        dmaapResponse.getBody().getOutput().getStatus().setCode(599);
        dmaapResponse.getBody().getOutput().getStatus().setMessage(PARTIAL_FAILURE);
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        dmaapResponse.getBody().getOutput().getStatus().setCode(550);
        dmaapResponse.getBody().getOutput().getStatus().setMessage(PARTIAL_FAILURE);
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());

        /* If APPC code is unknown to Policy, PolicyResult is failure exception */
        dmaapResponse.getBody().getOutput().getStatus().setCode(700);
        dmaapResponse.getBody().getOutput().getStatus().setMessage("UNKNOWN");
        result = AppcLcmActor.processResponse(dmaapResponse);
        assertEquals(PolicyResult.FAILURE_EXCEPTION, result.getKey());
    }

    /*
     * This test exercises getters not exercised in other tests.
     */
    @Test
    public void testMethods() {
        AppcLcmActor sp = new AppcLcmActor();

        assertEquals("APPC", sp.actor());
        assertEquals(4, sp.recipes().size());
        assertEquals("VM", sp.recipeTargets(RECIPE_RESTART).get(0));
        assertEquals("vm-id", sp.recipePayloads(RECIPE_RESTART).get(0));
    }

    @Test
    public void testPayloadNotPassedWhenNotSupportedByRecipe() {
        // given
        Policy migratePolicy = constructPolicyWithRecipe(RECIPE_MIGRATE);
        Policy rebuildPolicy = constructPolicyWithRecipe(RECIPE_REBUILD);
        Policy restartPolicy = constructPolicyWithRecipe(RECIPE_RESTART);

        // when
        AppcLcmDmaapWrapper migrateRequest =
                        AppcLcmActor.constructRequest(onsetEvent, operation, migratePolicy, VNF01);
        AppcLcmDmaapWrapper rebuildRequest =
                        AppcLcmActor.constructRequest(onsetEvent, operation, rebuildPolicy, VNF01);
        AppcLcmDmaapWrapper restartRequest =
                        AppcLcmActor.constructRequest(onsetEvent, operation, restartPolicy, VNF01);

        // then
        assertNull(migrateRequest.getBody().getInput().getPayload());
        assertNull(rebuildRequest.getBody().getInput().getPayload());
        assertNull(restartRequest.getBody().getInput().getPayload());
    }

    @Test
    public void testPayloadNotPassedWhenNotSuppliedOrEmpty() {
        // given
        Policy noPayloadPolicy = constructHealthCheckPolicyWithPayload(null);
        Policy emptyPayloadPolicy = constructHealthCheckPolicyWithPayload(new HashMap<>());

        // when
        AppcLcmDmaapWrapper noPayloadRequest =
                        AppcLcmActor.constructRequest(onsetEvent, operation, noPayloadPolicy, VNF01);
        AppcLcmDmaapWrapper emptyPayloadRequest =
                        AppcLcmActor.constructRequest(onsetEvent, operation, emptyPayloadPolicy, VNF01);

        // then
        assertNull(noPayloadRequest.getBody().getInput().getPayload());
        assertNull(emptyPayloadRequest.getBody().getInput().getPayload());
    }

    @Test
    public void testPayloadParsedProperlyForSinglePayloadParameter() {
        // given
        HashMap<String, String> payload = new HashMap<>();
        payload.put("requestParameters", "{\"host-ip-address\":\"10.183.37.25\"}");
        Policy otherPolicy = constructHealthCheckPolicyWithPayload(payload);

        // when
        AppcLcmDmaapWrapper dmaapRequest =
                        AppcLcmActor.constructRequest(onsetEvent, operation, otherPolicy, VNF01);

        // then
        assertEquals("{\"requestParameters\": {\"host-ip-address\":\"10.183.37.25\"}}",
                        dmaapRequest.getBody().getInput().getPayload());
    }

    @Test
    public void testPayloadParsedProperlyForMultiplePayloadParameters() {
        // given
        HashMap<String, String> payload = new HashMap<>();
        payload.put("requestParameters", "{\"host-ip-address\":\"10.183.37.25\"}");
        payload.put("configurationParameters",
                        "[{\"ip-addr\":\"$.vf-module-topology.vf-module-parameters.param[9]\","
                                        + "\"oam-ip-addr\":\"$.vf-module-topology.vf-module-parameters.param[16]\","
                                        + "\"enabled\":\"$.vf-module-topology.vf-module-parameters.param[23]\"}]");
        Policy otherPolicy = constructHealthCheckPolicyWithPayload(payload);

        // when
        AppcLcmDmaapWrapper dmaapRequest =
                        AppcLcmActor.constructRequest(onsetEvent, operation, otherPolicy, VNF01);

        // then
        assertEquals(dmaapRequest.getBody().getInput().getPayload(), "{\"requestParameters\": "
                        + "{\"host-ip-address\":\"10.183.37.25\"}," + "\"configurationParameters\": "
                        + "[{\"ip-addr\":\"$.vf-module-topology.vf-module-parameters.param[9]\","
                        + "\"oam-ip-addr\":\"$.vf-module-topology.vf-module-parameters.param[16]\","
                        + "\"enabled\":\"$.vf-module-topology.vf-module-parameters.param[23]\"}]" + "}");
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
