/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.cds.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.cds.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.cds.controllerblueprints.common.api.EventType;
import org.onap.ccsdk.cds.controllerblueprints.common.api.Status;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.policy.cds.client.CdsProcessorGrpcClient;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actor.cds.CdsActorServiceProvider.CdsActorServiceManager;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.policy.Policy;

@RunWith(MockitoJUnitRunner.class)
public class CdsActorServiceProviderTest {

    private static final String CDS_BLUEPRINT_NAME = "vfw-cds";
    private static final String CDS_BLUEPRINT_VERSION = "1.0.0";
    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final String SUBREQUEST_ID = "123456";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Mock
    private CdsProcessorGrpcClient cdsClient;
    private CdsActorServiceProvider cdsActor;
    private Policy policy;
    private CdsServerProperties cdsProps;
    private Map<String, String> aaiParams;
    private VirtualControlLoopEvent onset;
    private ControlLoopOperation operation;

    /**
     * Test setup.
     */
    @Before
    public void setup() {
        // Setup policy
        policy = new Policy();
        Map<String, String> payloadMap = new HashMap<String, String>() {
            {
                put(CdsActorConstants.KEY_CBA_NAME, CDS_BLUEPRINT_NAME);
                put(CdsActorConstants.KEY_CBA_VERSION, CDS_BLUEPRINT_VERSION);
                put("data", "{\"mapInfo\":{\"key\":\"val\"},\"arrayInfo\":[\"one\",\"two\"],\"paramInfo\":\"val\"}");
            }
        };
        policy.setPayload(payloadMap);
        policy.setRecipe("CDS");

        // Setup the CDS properties
        cdsProps = new CdsServerProperties();
        cdsProps.setHost("10.10.10.10");
        cdsProps.setPort(2000);
        cdsProps.setUsername("testUser");
        cdsProps.setPassword("testPassword");
        cdsProps.setTimeout(1);

        // Setup aaiParams
        aaiParams = ImmutableMap.of("service-instance-id", "1234", "generic-vnf-id", "5678");

        // Setup cdsClient
        when(cdsClient.sendRequest(any(ExecutionServiceInput.class))).thenReturn(mock(CountDownLatch.class));

        // Setup the cdsActor
        cdsActor = new CdsActorServiceProvider();

        // Setup onset event
        onset = new VirtualControlLoopEvent();
        onset.setRequestId(REQUEST_ID);

        // Setup controlloop operation object
        operation = new ControlLoopOperation();
        operation.setSubRequestId(SUBREQUEST_ID);
    }

    @Test
    public void testActor() {
        assertEquals(cdsActor.actor(), CdsActorConstants.CDS_ACTOR);
    }

    @Test
    public void testConstructRequest() {
        policy.setPayload(new HashMap<>());
        Optional<ExecutionServiceInput> cdsRequestOpt = cdsActor
            .constructRequest(onset, operation, policy, aaiParams);

        assertFalse(cdsRequestOpt.isPresent());
    }

    @Test
    public void testConstructRequestWhenMissingCdsParamsInPolicyPayload() {
        Optional<ExecutionServiceInput> cdsRequestOpt = cdsActor
            .constructRequest(onset, operation, policy, aaiParams);

        assertTrue(cdsRequestOpt.isPresent());
        final ExecutionServiceInput cdsRequest = cdsRequestOpt.get();

        assertTrue(cdsRequest.hasCommonHeader());
        CommonHeader commonHeader = cdsRequest.getCommonHeader();
        assertEquals(commonHeader.getRequestId(), REQUEST_ID.toString());
        assertEquals(commonHeader.getSubRequestId(), SUBREQUEST_ID);

        assertTrue(cdsRequest.hasPayload());

        assertTrue(cdsRequest.hasActionIdentifiers());
        ActionIdentifiers actionIdentifiers = cdsRequest.getActionIdentifiers();
        assertEquals(actionIdentifiers.getActionName(), CdsActorConstants.CDS_ACTOR);
        assertEquals(actionIdentifiers.getBlueprintName(), CDS_BLUEPRINT_NAME);
        assertEquals(actionIdentifiers.getBlueprintVersion(), CDS_BLUEPRINT_VERSION);
    }

    @Test
    public void testRecipePayloads() {
        assertEquals(cdsActor.recipePayloads("").size(), 0);
    }

    @Test
    public void testRecipes() {
        assertEquals(cdsActor.recipes().size(), 0);
    }

    @Test
    public void testRecipeTargets() {
        assertEquals(cdsActor.recipeTargets("").size(), 0);
    }

    @Test
    public void testSendRequestToCdsSuccess() {
        CdsActorServiceProvider.CdsActorServiceManager cdsActorSvcMgr = cdsActor.new CdsActorServiceManager();
        cdsActorSvcMgr.sendRequestToCds(cdsClient, cdsProps, ExecutionServiceInput.newBuilder().build());
        verify(cdsClient).sendRequest(any(ExecutionServiceInput.class));
    }

    @Test
    public void testSendRequestToCdsLatchInterrupted() throws InterruptedException {
        // Reset cdsClient
        CountDownLatch countDownLatch = mock(CountDownLatch.class);
        doThrow(new InterruptedException("Test latch interrupted failure")).when(countDownLatch)
            .await(anyLong(), any(TimeUnit.class));
        when(cdsClient.sendRequest(any(ExecutionServiceInput.class))).thenReturn(countDownLatch);

        CdsActorServiceProvider.CdsActorServiceManager cdsActorSvcMgr = cdsActor.new CdsActorServiceManager();
        String response = cdsActorSvcMgr
            .sendRequestToCds(cdsClient, cdsProps, ExecutionServiceInput.newBuilder().build());
        assertTrue(Thread.interrupted());
        assertEquals(response, CdsActorConstants.INTERRUPTED);
    }

    @Test
    public void testSendRequestToCdsLatchTimedOut() {
        CdsActorServiceProvider.CdsActorServiceManager cdsActorSvcMgr = cdsActor.new CdsActorServiceManager();
        String response = cdsActorSvcMgr
            .sendRequestToCds(cdsClient, cdsProps, ExecutionServiceInput.newBuilder().build());
        assertEquals(response, CdsActorConstants.TIMED_OUT);
    }

    @Test
    public void testOnMessage() throws InterruptedException {
        ExecutionServiceOutput message = ExecutionServiceOutput.newBuilder()
            .setStatus(Status.newBuilder().setEventType(EventType.EVENT_COMPONENT_FAILURE).build()).build();

        // Test "no timeout" scenarios
        CountDownLatch latch = mock(CountDownLatch.class);
        when(latch.await(anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(cdsClient.sendRequest(any(ExecutionServiceInput.class))).thenReturn(latch);

        CdsActorServiceManager cdsActorSvcMgr = getCdsActorServiceManager();

        // #1: Failure test
        cdsActorSvcMgr.onMessage(message);
        assertEquals(cdsActorSvcMgr.getCdsResponse(), CdsActorConstants.FAILED);

        // #2: Success test
        cdsActorSvcMgr = getCdsActorServiceManager();
        message = ExecutionServiceOutput.newBuilder()
            .setStatus(Status.newBuilder().setEventType(EventType.EVENT_COMPONENT_EXECUTED).build()).build();
        cdsActorSvcMgr.onMessage(message);
        assertEquals(cdsActorSvcMgr.getCdsResponse(), CdsActorConstants.SUCCESS);

        // #3: Processing test
        cdsActorSvcMgr = getCdsActorServiceManager();
        message = ExecutionServiceOutput.newBuilder()
            .setStatus(Status.newBuilder().setEventType(EventType.EVENT_COMPONENT_PROCESSING).build()).build();
        cdsActorSvcMgr.onMessage(message);
        assertEquals(cdsActorSvcMgr.getCdsResponse(), CdsActorConstants.PROCESSING);
    }

    private CdsActorServiceManager getCdsActorServiceManager() {
        CdsActorServiceManager cdsActorSvcMgr = cdsActor.new CdsActorServiceManager();
        cdsActorSvcMgr.sendRequestToCds(cdsClient, cdsProps, ExecutionServiceInput.newBuilder().build());
        return cdsActorSvcMgr;
    }
}
