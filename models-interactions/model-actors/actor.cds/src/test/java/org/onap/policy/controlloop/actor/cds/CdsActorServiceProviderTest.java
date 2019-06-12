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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.policy.cds.client.CdsProcessorGrpcClient;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.policy.Policy;

@RunWith(MockitoJUnitRunner.class)
public class CdsActorServiceProviderTest {

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
                put(CdsActorConstants.KEY_CBA_NAME, "vfw-cds");
                put(CdsActorConstants.KEY_CBA_VERSION, "1.0.0");
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
        onset.setRequestId(UUID.randomUUID());

        // Setup controlloop operation object
        operation = new ControlLoopOperation();
        operation.setSubRequestId("123456");
    }

    @Test
    public void testActor() {
        assertEquals(cdsActor.actor(), CdsActorConstants.CDS_ACTOR);
    }

    @Test
    public void testConstructRequest() {
        ExecutionServiceInput cdsRequest = cdsActor.constructRequest(onset, operation, policy, aaiParams);
        System.out.println(cdsRequest.getPayload());
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
    public void testSendRequestToCds() {
        cdsActor.sendRequestToCds(onset, operation, policy, aaiParams, cdsClient, cdsProps);
        verify(cdsClient).sendRequest(any(ExecutionServiceInput.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testSendRequestToCdsFailureWhenMissingCdsParamsInPolicyPayload() {
        aaiParams = new HashMap<>();

        VirtualControlLoopEvent onset = new VirtualControlLoopEvent();
        onset.setRequestId(UUID.randomUUID());

        ControlLoopOperation operation = new ControlLoopOperation();
        operation.setSubRequestId("123456");

        policy.setPayload(new HashMap<>());

        cdsActor.sendRequestToCds(onset, operation, policy, aaiParams, cdsClient, new CdsServerProperties());
        exceptionRule.expectMessage("Missing mapping for CDS");
    }
}
