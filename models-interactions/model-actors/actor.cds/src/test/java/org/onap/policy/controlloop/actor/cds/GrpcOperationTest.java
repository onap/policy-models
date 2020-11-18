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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.cds.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.policy.cds.client.CdsProcessorGrpcClient;
import org.onap.policy.cds.properties.CdsServerProperties;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.common.utils.time.PseudoExecutor;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.TargetType;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.simulators.CdsSimulator;
import org.onap.policy.simulators.Util;

public class GrpcOperationTest {
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
    private CdsServerProperties cdsProps;
    private PseudoExecutor executor;
    private Map<String, String> targetEntityIds;
    private ControlLoopOperationParams params;
    private GrpcConfig config;
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

        // Setup executor
        executor = new PseudoExecutor();

        targetEntityIds = new HashMap<>();
        targetEntityIds.put(ControlLoopOperationParams.PARAMS_ENTITY_RESOURCEID, RESOURCE_ID);

        params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR).operation(GrpcOperation.NAME)
                        .requestId(REQUEST_ID).actorService(new ActorService())
                        .build();
    }

    /**
     * Tests "success" case with simulator.
     */
    @Test
    public void testSuccess() throws Exception {
        Map<String, Object> payload = Map.of("artifact_name", "my_artifact", "artifact_version", "1.0");

        params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR).operation("subscribe")
                        .requestId(REQUEST_ID).actorService(new ActorService())
                        .retry(0).timeoutSec(5).executor(blockingExecutor).payload(payload)
                        .build();

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
        params = params.toBuilder().targetType(TargetType.PNF).build();
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
    public void testGetServiceInstanceId() {
        operation = new GrpcOperation(params, config);
        loadVnfData();
        assertEquals(MY_SVC_ID, operation.getServiceInstanceId());
    }

    @Test
    public void testGetVnfId() {
        operation = new GrpcOperation(params, config);
        loadVnfData();
        assertEquals(MY_VNF, operation.getVnfId());
    }

    @Test
    public void testStartOperationAsync() throws Exception {
        verifyOperation(TargetType.VNF, this::loadVnfData);
    }

    /**
     * Tests startOperationAsync() when the target type is PNF.
     */
    @Test
    public void testStartOperationAsyncPnf() throws Exception {
        verifyOperation(TargetType.PNF, this::loadPnfData);
    }

    @Test
    public void testStartOperationAsyncError() throws Exception {
        operation = new GrpcOperation(params, config);
        assertThatIllegalArgumentException()
                        .isThrownBy(() -> operation.startOperationAsync(1, params.makeOutcome(null)));
    }

    private void verifyOperation(TargetType targetType, Runnable loader) {

        Map<String, Object> payloadMap = Map.of(CdsActorConstants.KEY_CBA_NAME, CDS_BLUEPRINT_NAME,
                        CdsActorConstants.KEY_CBA_VERSION, CDS_BLUEPRINT_VERSION, "data",
                        "{\"mapInfo\":{\"key\":\"val\"},\"arrayInfo\":[\"one\",\"two\"],\"paramInfo\":\"val\"}");

        ControlLoopOperationParams params = ControlLoopOperationParams.builder().actor(CdsActorConstants.CDS_ACTOR)
                        .operation(GrpcOperation.NAME).requestId(REQUEST_ID).actorService(new ActorService())
                        .targetType(targetType).payload(payloadMap).build();

        GrpcConfig config = new GrpcConfig(executor, cdsProps);
        operation = new GrpcOperation(params, config);
        assertEquals(1000, operation.getTimeoutMs(null));
        assertEquals(1000, operation.getTimeoutMs(0));
        assertEquals(2000, operation.getTimeoutMs(2));
        operation.generateSubRequestId(1);

        loader.run();
        CompletableFuture<OperationOutcome> future3 = operation.startOperationAsync(1, params.makeOutcome(null));
        assertNotNull(future3);
    }

    private void loadPnfData() {
        try {
            String json = "{'dataA': 'valueA', 'dataB': 'valueB'}".replace('\'', '"');
            StandardCoderObject sco = coder.decode(json, StandardCoderObject.class);

            operation.setProperty(OperationProperties.AAI_PNF, sco);

        } catch (CoderException e) {
            throw new IllegalArgumentException("cannot decode PNF json", e);
        }
    }

    private void loadVnfData() {
        GenericVnf genvnf = new GenericVnf();
        genvnf.setVnfId(MY_VNF);
        operation.setProperty(OperationProperties.AAI_RESOURCE_VNF, genvnf);

        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(MY_SVC_ID);
        operation.setProperty(OperationProperties.AAI_SERVICE, serviceInstance);
    }
}
