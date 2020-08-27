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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.Pnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.cds.client.CdsProcessorGrpcClient;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.common.utils.time.PseudoExecutor;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actor.aai.AaiGetPnfOperation;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.simulators.CdsSimulator;
import org.onap.policy.simulators.Util;

public class GrpcOperationTest {
    private static final String TARGET_ENTITY = "entity";
    private static final String MY_VNF = "my-vnf";
    private static final String MY_SVC_ID = "my-service-instance-id";
    private static final String RESOURCE_ID = "my-resource-id";
    private static final String CDS_BLUEPRINT_NAME = "vfw-cds";
    private static final String CDS_BLUEPRINT_VERSION = "1.0.0";
    private static final UUID REQUEST_ID = UUID.randomUUID();
    private static final Coder coder = new StandardCoder();

    protected static final Executor blockingExecutor = command -> {
        Thread thread = new Thread(command);
        thread.setDaemon(true);
        thread.start();
    };

    private static CdsSimulator sim;

    @Mock
    private CdsProcessorGrpcClient cdsClient;
    @Mock
    private ControlLoopEventContext context;
    private CdsServerProperties cdsProps;
    private VirtualControlLoopEvent onset;
    private PseudoExecutor executor;
    private String targetType;
    private Map<String, String> targetEntityIds;
    private ControlLoopOperationParams params;
    private GrpcConfig config;
    private CompletableFuture<OperationOutcome> cqFuture;
    private GrpcOperation operation;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        sim = Util.buildCdsSim();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        sim.stop();
    }

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

        targetType = "VM";
        targetEntityIds = new HashMap<>();
        targetEntityIds.put(ControlLoopOperationParams.PARAMS_ENTITY_RESOURCEID, RESOURCE_ID);

        cqFuture = new CompletableFuture<>();
        when(context.obtain(eq(AaiCqResponse.CONTEXT_KEY), any())).thenReturn(cqFuture);
        when(context.getEvent()).thenReturn(onset);

        params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR)
                .operation(GrpcOperation.NAME).context(context).actorService(new ActorService())
                .targetEntity(TARGET_ENTITY).targetType(targetType).targetEntityIds(targetEntityIds)
                .build();
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    public void testSuccess() throws Exception {
        ControlLoopEventContext context = new ControlLoopEventContext(onset);
        loadCqData(context);

        Map<String, Object> payload = Map.of("artifact_name", "my_artifact", "artifact_version", "1.0");

        params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR).operation("subscribe")
                        .context(context).actorService(new ActorService()).targetEntity(TARGET_ENTITY)
                        .targetType(targetType).targetEntityIds(targetEntityIds)
                        .retry(0).timeoutSec(5).executor(blockingExecutor).payload(payload).build();

        cdsProps.setHost("localhost");
        cdsProps.setPort(sim.getPort());
        cdsProps.setTimeout(3);

        GrpcConfig config = new GrpcConfig(blockingExecutor, cdsProps);

        operation = new GrpcOperation(params, config) {
            @Override
            protected CompletableFuture<OperationOutcome> startGuardAsync() {
                // indicate that guard completed successfully
                return CompletableFuture.completedFuture(params.makeOutcome(null));
            }
        };

        OperationOutcome outcome = operation.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof ExecutionServiceOutput);
    }

    /**
     * Tests "success" case with simulator using properties.
     */
    @Test
    public void testSuccessViaProperties() throws Exception {
        ControlLoopEventContext context = new ControlLoopEventContext(onset);
        loadCqData(context);

        Map<String, Object> payload = Map.of("artifact_name", "my_artifact", "artifact_version", "1.0");

        params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR).operation("subscribe")
                        .context(context).actorService(new ActorService()).targetEntity(TARGET_ENTITY)
                        .targetType(targetType).targetEntityIds(targetEntityIds)
                        .retry(0).timeoutSec(5).executor(blockingExecutor).payload(payload).preprocessed(true).build();

        cdsProps.setHost("localhost");
        cdsProps.setPort(sim.getPort());
        cdsProps.setTimeout(3);

        GrpcConfig config = new GrpcConfig(blockingExecutor, cdsProps);

        operation = new GrpcOperation(params, config);

        // set the properties
        operation.setProperty(OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES, Collections.emptyMap());

        OperationOutcome outcome = operation.start().get();
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof ExecutionServiceOutput);
    }

    @Test
    public void testGetPropertyNames() {

        /*
         * check VNF case
         */
        operation = new GrpcOperation(params, config);

        // @formatter:off
        assertThat(operation.getPropertyNames()).isEqualTo(
                        List.of(
                            OperationProperties.AAI_RESOURCE_VNF,
                            OperationProperties.AAI_SERVICE,
                            OperationProperties.EVENT_ADDITIONAL_PARAMS,
                            OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES));
        // @formatter:on

        /*
         * check PNF case
         */
        params = params.toBuilder().targetType("PNF").build();
        operation = new GrpcOperation(params, config);

        // @formatter:off
        assertThat(operation.getPropertyNames()).isEqualTo(
                        List.of(
                            OperationProperties.AAI_PNF,
                            OperationProperties.EVENT_ADDITIONAL_PARAMS,
                            OperationProperties.OPT_CDS_GRPC_AAI_PROPERTIES));
        // @formatter:on
    }

    @Test
    public void testGetPnf() {
        ControlLoopEventContext context = new ControlLoopEventContext(onset);
        params = params.toBuilder().context(context).build();
        operation = new GrpcOperation(params, config);

        // in neither property nor context
        assertThatIllegalArgumentException().isThrownBy(() -> operation.getPnfData()).withMessage("missing PNF data");

        // only in context
        Pnf pnf = new Pnf();
        params.getContext().setProperty(AaiGetPnfOperation.getKey(params.getTargetEntity()), pnf);
        assertSame(pnf, operation.getPnfData());

        // both - should choose the property
        Pnf pnf2 = new Pnf();
        operation.setProperty(OperationProperties.AAI_PNF, pnf2);
        assertSame(pnf2, operation.getPnfData());
    }

    @Test
    public void testGetServiceInstanceId() {
        ControlLoopEventContext context = new ControlLoopEventContext(onset);
        params = params.toBuilder().context(context).build();
        operation = new GrpcOperation(params, config);

        // in neither property nor custom query
        context.setProperty(AaiCqResponse.CONTEXT_KEY, mock(AaiCqResponse.class));
        assertThatIllegalArgumentException().isThrownBy(() -> operation.getServiceInstanceId())
                        .withMessage("Target service instance could not be found");

        // only in custom query
        loadCqData(params.getContext());
        assertEquals(MY_SVC_ID, operation.getServiceInstanceId());

        // both - should choose the property
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId("another-service-id");
        operation.setProperty(OperationProperties.AAI_SERVICE, serviceInstance);
        assertEquals("another-service-id", operation.getServiceInstanceId());
    }

    @Test
    public void testGetVnfId() {
        ControlLoopEventContext context = new ControlLoopEventContext(onset);
        params = params.toBuilder().context(context).build();
        operation = new GrpcOperation(params, config);

        // in neither property nor custom query
        context.setProperty(AaiCqResponse.CONTEXT_KEY, mock(AaiCqResponse.class));
        assertThatIllegalArgumentException().isThrownBy(() -> operation.getVnfId())
                        .withMessage("Target generic vnf could not be found");

        // only in custom query
        loadCqData(params.getContext());
        assertEquals(MY_VNF, operation.getVnfId());

        // both - should choose the property
        GenericVnf vnf = new GenericVnf();
        vnf.setVnfId("another-vnf-id");
        operation.setProperty(OperationProperties.AAI_RESOURCE_VNF, vnf);
        assertEquals("another-vnf-id", operation.getVnfId());
    }

    @Test
    public void testStartPreprocessorAsync() throws InterruptedException, ExecutionException, TimeoutException {
        AtomicBoolean guardStarted = new AtomicBoolean();

        operation = new GrpcOperation(params, config) {
            @Override
            protected CompletableFuture<OperationOutcome> startGuardAsync() {
                guardStarted.set(true);
                return cqFuture;
            }
        };

        CompletableFuture<OperationOutcome> future3 = operation.startPreprocessorAsync();
        assertNotNull(future3);
        assertTrue(guardStarted.get());
        verify(context).obtain(eq(AaiCqResponse.CONTEXT_KEY), any());

        cqFuture.complete(params.makeOutcome(null));
        assertTrue(executor.runAll(100));
        assertEquals(OperationResult.SUCCESS, future3.get(2, TimeUnit.SECONDS).getResult());
        assertTrue(future3.isDone());
    }

    /**
     * Tests startPreprocessorAsync() when the target type is PNF.
     */
    @Test
    public void testStartPreprocessorAsyncPnf() throws InterruptedException, ExecutionException, TimeoutException {
        AtomicBoolean guardStarted = new AtomicBoolean();

        params = params.toBuilder().targetType("PNF").build();

        operation = new GrpcOperation(params, config) {
            @Override
            protected CompletableFuture<OperationOutcome> startGuardAsync() {
                guardStarted.set(true);
                return cqFuture;
            }
        };

        CompletableFuture<OperationOutcome> future3 = operation.startPreprocessorAsync();
        assertNotNull(future3);
        assertTrue(guardStarted.get());
        verify(context).obtain(eq(AaiGetPnfOperation.getKey(TARGET_ENTITY)), any());

        cqFuture.complete(params.makeOutcome(null));
        assertTrue(executor.runAll(100));
        assertEquals(OperationResult.SUCCESS, future3.get(2, TimeUnit.SECONDS).getResult());
        assertTrue(future3.isDone());
    }

    /**
     * Tests startPreprocessorAsync(), when preprocessing is disabled.
     */
    @Test
    public void testStartPreprocessorAsyncDisabled() {
        params = params.toBuilder().preprocessed(true).build();
        assertNull(new GrpcOperation(params, config).startPreprocessorAsync());
    }

    @Test
    public void testStartOperationAsync() throws Exception {

        ControlLoopEventContext context = new ControlLoopEventContext(onset);
        loadCqData(context);

        verifyOperation(context);
    }

    /**
     * Tests startOperationAsync() when the target type is PNF.
     */
    @Test
    public void testStartOperationAsyncPnf() throws Exception {

        targetType = "PNF";

        ControlLoopEventContext context = new ControlLoopEventContext(onset);
        loadPnfData(context);

        verifyOperation(context);
    }

    @Test
    public void testStartOperationAsyncWithAdditionalParams() throws Exception {

        Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("test", "additionalParams");
        onset.setAdditionalEventParams(additionalParams);
        ControlLoopEventContext context = new ControlLoopEventContext(onset);
        loadCqData(context);
        verifyOperation(context);
    }

    @Test
    public void testStartOperationAsyncError() throws Exception {
        operation = new GrpcOperation(params, config);
        assertThatIllegalArgumentException()
                        .isThrownBy(() -> operation.startOperationAsync(1, params.makeOutcome(null)));
    }

    @Test
    public void testGetAdditionalEventParams() {
        operation = new GrpcOperation(params, config);

        // in neither property nor context
        assertNull(operation.getAdditionalEventParams());

        final Map<String, String> eventParams = Collections.emptyMap();

        // only in context
        onset.setAdditionalEventParams(eventParams);
        assertSame(eventParams, operation.getAdditionalEventParams());

        // both - should choose the property, even if it's null
        operation.setProperty(OperationProperties.EVENT_ADDITIONAL_PARAMS, null);
        assertNull(operation.getAdditionalEventParams());

        // both - should choose the property
        final Map<String, String> propParams = Collections.emptyMap();
        operation.setProperty(OperationProperties.EVENT_ADDITIONAL_PARAMS, propParams);
        assertSame(propParams, operation.getAdditionalEventParams());
    }

    private void verifyOperation(ControlLoopEventContext context) {

        Map<String, Object> payloadMap = Map.of(CdsActorConstants.KEY_CBA_NAME, CDS_BLUEPRINT_NAME,
                        CdsActorConstants.KEY_CBA_VERSION, CDS_BLUEPRINT_VERSION, "data",
                        "{\"mapInfo\":{\"key\":\"val\"},\"arrayInfo\":[\"one\",\"two\"],\"paramInfo\":\"val\"}");

        ControlLoopOperationParams params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR)
                        .operation(GrpcOperation.NAME).context(context).actorService(new ActorService())
                        .targetEntity(TARGET_ENTITY).targetType(targetType).targetEntityIds(targetEntityIds)
                        .payload(payloadMap).build();

        GrpcConfig config = new GrpcConfig(executor, cdsProps);
        operation = new GrpcOperation(params, config);
        assertEquals(1000, operation.getTimeoutMs(null));
        assertEquals(1000, operation.getTimeoutMs(0));
        assertEquals(2000, operation.getTimeoutMs(2));
        operation.generateSubRequestId(1);
        CompletableFuture<OperationOutcome> future3 = operation.startOperationAsync(1, params.makeOutcome(null));
        assertNotNull(future3);
    }

    private void loadPnfData(ControlLoopEventContext context) throws CoderException {
        String json = "{'dataA': 'valueA', 'dataB': 'valueB'}".replace('\'', '"');
        StandardCoderObject sco = coder.decode(json, StandardCoderObject.class);

        context.setProperty(AaiGetPnfOperation.getKey(TARGET_ENTITY), sco);
    }

    private void loadCqData(ControlLoopEventContext context) {
        GenericVnf genvnf = new GenericVnf();
        genvnf.setVnfId(MY_VNF);

        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(MY_SVC_ID);

        AaiCqResponse cq = mock(AaiCqResponse.class);
        when(cq.getGenericVnfByModelInvariantId(any())).thenReturn(genvnf);
        when(cq.getServiceInstance()).thenReturn(serviceInstance);

        context.setProperty(AaiCqResponse.CONTEXT_KEY, cq);
    }
}
