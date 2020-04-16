/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2020 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.cds.client.CdsProcessorGrpcClient;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.common.utils.time.PseudoExecutor;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.policy.PolicyResult;

public class GrpcOperationTest {

    private static final String CDS_BLUEPRINT_NAME = "vfw-cds";
    private static final String CDS_BLUEPRINT_VERSION = "1.0.0";
    private static final UUID REQUEST_ID = UUID.randomUUID();

    @Mock
    private CdsProcessorGrpcClient cdsClient;
    private CdsServerProperties cdsProps;
    private VirtualControlLoopEvent onset;
    private PseudoExecutor executor;
    private GrpcOperation operation;

    /**
     * Sets up the fields.
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // Setup the CDS properties
        cdsProps = new CdsServerProperties();
        cdsProps.setHost("10.10.10.10");
        cdsProps.setPort(2000);
        cdsProps.setUsername("testUser");
        cdsProps.setPassword("testPassword");
        cdsProps.setTimeout(1);

        // Setup cdsClient
        when(cdsClient.sendRequest(any(ExecutionServiceInput.class))).thenReturn(mock(CountDownLatch.class));

        // Setup onset event
        onset = new VirtualControlLoopEvent();
        onset.setRequestId(REQUEST_ID);

        // Setup executor
        executor = new PseudoExecutor();
    }

    @Test
    public void testStartPreprocessorAsync() throws InterruptedException, ExecutionException, TimeoutException {

        CompletableFuture<OperationOutcome> future2 = new CompletableFuture<>();
        ControlLoopEventContext context = mock(ControlLoopEventContext.class);
        when(context.obtain(eq(AaiCqResponse.CONTEXT_KEY), any())).thenReturn(future2);
        when(context.getEvent()).thenReturn(onset);

        AtomicBoolean guardStarted = new AtomicBoolean();

        ControlLoopOperationParams params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR)
                        .operation(GrpcOperation.NAME).context(context).actorService(new ActorService())
                        .targetEntity("entity").build();
        GrpcConfig config = new GrpcConfig(executor, cdsProps);

        operation = new GrpcOperation(params, config) {
            @Override
            protected CompletableFuture<OperationOutcome> startGuardAsync() {
                guardStarted.set(true);
                return future2;
            }
        };

        CompletableFuture<OperationOutcome> future3 = operation.startPreprocessorAsync();
        assertNotNull(future3);
        assertTrue(guardStarted.get());
        verify(context).obtain(eq(AaiCqResponse.CONTEXT_KEY), any());

        future2.complete(params.makeOutcome());
        assertTrue(executor.runAll(100));
        assertEquals(PolicyResult.SUCCESS, future3.get(2, TimeUnit.SECONDS).getResult());
        assertTrue(future3.isDone());
    }

    @Test
    public void testStartOperationAsync() throws Exception {

        ControlLoopEventContext context = new ControlLoopEventContext(onset);
        verifyOperation(context);
    }

    @Test
    public void testStartOperationAsyncWithAdditionalParams() throws Exception {

        Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("test", "additionalParams");
        onset.setAdditionalEventParams(additionalParams);
        ControlLoopEventContext context = new ControlLoopEventContext(onset);
        verifyOperation(context);
    }

    @Test
    public void testStartOperationAsyncError() throws Exception {

        ControlLoopEventContext context = new ControlLoopEventContext(onset);
        ControlLoopOperationParams params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR)
                        .operation(GrpcOperation.NAME).context(context).actorService(new ActorService())
                        .targetEntity("entity").build();

        GrpcConfig config = new GrpcConfig(executor, cdsProps);
        operation = new GrpcOperation(params, config);
        assertThatIllegalArgumentException().isThrownBy(() -> operation.startOperationAsync(1, params.makeOutcome()));
    }

    private void verifyOperation(ControlLoopEventContext context) {

        Map<String, Object> payloadMap = Map.of(CdsActorConstants.KEY_CBA_NAME, CDS_BLUEPRINT_NAME,
                        CdsActorConstants.KEY_CBA_VERSION, CDS_BLUEPRINT_VERSION, "data",
                        "{\"mapInfo\":{\"key\":\"val\"},\"arrayInfo\":[\"one\",\"two\"],\"paramInfo\":\"val\"}");

        ControlLoopOperationParams params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR)
                        .operation(GrpcOperation.NAME).context(context).actorService(new ActorService())
                        .targetEntity("entity").payload(payloadMap).build();

        GrpcConfig config = new GrpcConfig(executor, cdsProps);
        operation = new GrpcOperation(params, config);
        assertEquals(1000, operation.getTimeoutMs(null));
        assertEquals(1000, operation.getTimeoutMs(0));
        assertEquals(2000, operation.getTimeoutMs(2));
        operation.generateSubRequestId(1);
        CompletableFuture<OperationOutcome> future3 = operation.startOperationAsync(1, params.makeOutcome());
        assertNotNull(future3);
    }
}
