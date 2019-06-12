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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.protobuf.Struct;
import java.util.HashMap;
import java.util.Map;
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
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.policy.cds.client.CdsProcessorGrpcClient;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.controlloop.policy.Policy;

@RunWith(MockitoJUnitRunner.class)
public class CdsActorServiceProviderTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();
    @Mock
    private CdsProcessorGrpcClient cdsClient;
    private CdsActorServiceProvider cdsActor;
    private Policy policy;

    /**
     * Test setup.
     */
    @Before
    public void setup() throws InterruptedException {
        // Setup policy
        policy = new Policy();
        Map<String, String> payloadMap = new HashMap<String, String>() {
            {
                put("artifact_name", "vfw-cds");
                put("artifact_version", "1.0.0");
                put("data", "{\"mapInfo\":{\"key\":\"val\"},\"arrayInfo\":[\"one\",\"two\"],\"paramInfo\":\"val\"}");
            }
        };
        policy.setPayload(payloadMap);
        policy.setRecipe("CDS");

        // Setup the CDS properties
        CdsServerProperties cdsProps = new CdsServerProperties();
        cdsProps.setHost("10.10.10.10");
        cdsProps.setPort(2000);
        cdsProps.setUsername("testUser");
        cdsProps.setPassword("testPassword");
        cdsProps.setTimeout(1);

        // Setup cdsClient
        when(cdsClient.sendRequest(any(ExecutionServiceInput.class))).thenReturn(mock(CountDownLatch.class));

        // Setup aaiParams
        Map<String, String> aaiParams = ImmutableMap.of("service-instance-id", "1234", "generic-vnf-id", "5678");

        // Setup the cdsActor
        cdsActor = new CdsActorServiceProvider(UUID.randomUUID().toString(), "123456", policy, aaiParams, cdsProps,
            cdsClient);
    }

    @Test
    public void testSendRequestToCds1() {
        cdsActor.sendRequestToCds();
        verify(cdsClient).sendRequest(any(ExecutionServiceInput.class));
    }

    @Test
    public void testSendRequestToCds2() {
        ExecutionServiceInput input = ExecutionServiceInput.newBuilder()
            .setCommonHeader(CommonHeader.newBuilder().setOriginatorId("POLICY").build())
            .setActionIdentifiers(ActionIdentifiers.newBuilder().setBlueprintName("abc")
                .setBlueprintVersion("1.1").setActionName("test"))
            .setPayload(Struct.newBuilder())
            .build();
        cdsActor.sendRequestToCds(input);
        verify(cdsClient).sendRequest(input);
    }

    @Test(expected = IllegalStateException.class)
    public void testSendRequestToCds1Failed() {
        policy.setPayload(new HashMap<>());
        cdsActor = new CdsActorServiceProvider(UUID.randomUUID().toString(), "123456", policy, new HashMap<>(),
            new CdsServerProperties(), cdsClient);
        cdsActor.sendRequestToCds();
        exceptionRule.expectMessage("Missing mapping for CDS");
    }
}
