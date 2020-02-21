/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.so;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ModelVer;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.so.SoRequest;

public class VfModuleCreateTest extends BasicSoOperation {
    private static final String MODEL_NAME2 = "my-model-name-B";
    private static final String MODEL_VERS2 = "my-model-version-B";
    private static final String SVC_INSTANCE_ID = "my-service-instance-id";
    private static final String VNF_ID = "my-vnf-id";

    private VfModuleCreate oper;

    public VfModuleCreateTest() {
        super(DEFAULT_ACTOR, VfModuleCreate.NAME);
    }


    @Before
    public void setUp() throws Exception {
        super.setUp();
        oper = new VfModuleCreate(params, config);
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(VfModuleCreate.NAME, oper.getName());
    }

    @Test
    public void testStartPreprocessorAsync() {
        CompletableFuture<OperationOutcome> future = new CompletableFuture<>();
        context = mock(ControlLoopEventContext.class);
        when(context.obtain(eq(AaiCqResponse.CONTEXT_KEY), any())).thenReturn(future);
        params = params.toBuilder().context(context).build();

        AtomicBoolean guardStarted = new AtomicBoolean();

        oper = new VfModuleCreate(params, config) {
            @Override
            protected CompletableFuture<OperationOutcome> startGuardAsync() {
                guardStarted.set(true);
                return super.startGuardAsync();
            }
        };

        assertSame(future, oper.startPreprocessorAsync());
        assertFalse(future.isDone());
        assertTrue(guardStarted.get());
    }

    @Test
    public void testStartOperationAsync() throws Exception {
        when(client.post(any(), any(), any(), any())).thenAnswer(provideResponse(rawResponse));

        // use a real executor
        params = params.toBuilder().executor(ForkJoinPool.commonPool()).build();

        oper = new VfModuleCreate(params, config) {
            @Override
            public long getWaitMsGet() {
                return 1;
            }
        };

        CompletableFuture<OperationOutcome> future2 = oper.start();

        outcome = future2.get(500, TimeUnit.SECONDS);
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    /**
     * Tests startOperationAsync() when "get" operations are required.
     */
    @Test
    public void testStartOperationAsyncWithGets() throws Exception {
        when(rawResponse.getStatus()).thenReturn(500, 500, 500, 500, 200, 200);

        when(client.post(any(), any(), any(), any())).thenAnswer(provideResponse(rawResponse));
        when(client.get(any(), any(), any())).thenAnswer(provideResponse(rawResponse));

        // use a real executor
        params = params.toBuilder().executor(ForkJoinPool.commonPool()).build();

        oper = new VfModuleCreate(params, config) {
            @Override
            public long getWaitMsGet() {
                return 1;
            }
        };

        CompletableFuture<OperationOutcome> future2 = oper.start();

        outcome = future2.get(500, TimeUnit.SECONDS);
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
    }

    @Test
    public void testMakeRequest() throws CoderException {
        Pair<String, SoRequest> pair = oper.makeRequest();

        // @formatter:off
        assertEquals(
            "/serviceInstantiation/v7/serviceInstances/my-service-instance-id/vnfs/my-vnf-id/vfModules/scaleOut",
            pair.getLeft());
        // @formatter:on

        verifyRequest("vfModuleCreate.json", pair.getRight());
    }


    @Override
    protected void makeContext() {
        super.makeContext();

        AaiCqResponse cq = mock(AaiCqResponse.class);

        GenericVnf vnf = new GenericVnf();
        when(cq.getGenericVnfByVfModuleModelInvariantId(MODEL_INVAR_ID)).thenReturn(vnf);
        vnf.setVnfId(VNF_ID);

        ServiceInstance instance = new ServiceInstance();
        when(cq.getServiceInstance()).thenReturn(instance);
        instance.setServiceInstanceId(SVC_INSTANCE_ID);

        when(cq.getDefaultTenant()).thenReturn(new Tenant());
        when(cq.getDefaultCloudRegion()).thenReturn(new CloudRegion());

        ModelVer modelVers = new ModelVer();
        when(cq.getModelVerByVersionId(any())).thenReturn(modelVers);
        modelVers.setModelName(MODEL_NAME2);
        modelVers.setModelVersion(MODEL_VERS2);

        params.getContext().setProperty(AaiCqResponse.CONTEXT_KEY, cq);
    }
}
