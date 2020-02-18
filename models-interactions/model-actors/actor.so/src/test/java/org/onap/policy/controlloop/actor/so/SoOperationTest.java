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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.so.SoModelInfo;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestInfo;
import org.onap.policy.so.SoRequestParameters;
import org.onap.policy.so.SoRequestReferences;
import org.onap.policy.so.SoRequestStatus;
import org.onap.policy.so.SoResponse;

public class SoOperationTest extends BasicSoOperation {

    private SoOperation oper;

    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        initOperator();

        oper = new SoOperation(params, soOperator) {};
    }

    @Test
    public void testConstructor_testGetWaitMsGet() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
        assertSame(soOperator, oper.getOperator());
        assertEquals(1000 * WAIT_SEC_GETS, oper.getWaitMsGet());
    }

    @Test
    public void testStartPreprocessorAsync() {
        AtomicBoolean guardStarted = new AtomicBoolean();

        oper = new SoOperation(params, soOperator) {
            @Override
            protected CompletableFuture<OperationOutcome> startGuardAsync() {
                guardStarted.set(true);
                return super.startGuardAsync();
            }
        };

        assertNull(oper.startPreprocessorAsync());
        assertTrue(guardStarted.get());
    }

    @Test
    public void testPostProcess() throws Exception {
        // completed
        CompletableFuture<OperationOutcome> future2 = oper.postProcessResponse(outcome, PATH, rawResponse, response);
        assertTrue(future2.isDone());
        assertSame(outcome, future2.get());
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());

        // failed
        response.getRequest().getRequestStatus().setRequestState(SoOperation.FAILED);
        future2 = oper.postProcessResponse(outcome, PATH, rawResponse, response);
        assertTrue(future2.isDone());
        assertSame(outcome, future2.get());
        assertEquals(PolicyResult.FAILURE, outcome.getResult());

        // no request id in the response
        response.getRequestReferences().setRequestId(null);
        response.getRequest().getRequestStatus().setRequestState("unknown");
        assertThatIllegalArgumentException()
                        .isThrownBy(() -> oper.postProcessResponse(outcome, PATH, rawResponse, response))
                        .withMessage("missing request ID in response");
        response.getRequestReferences().setRequestId(REQ_ID.toString());

        // status = 500
        when(rawResponse.getStatus()).thenReturn(500);

        // null request reference
        SoRequestReferences ref = response.getRequestReferences();
        response.setRequestReferences(null);
        assertThatIllegalArgumentException()
                        .isThrownBy(() -> oper.postProcessResponse(outcome, PATH, rawResponse, response))
                        .withMessage("missing request ID in response");
        response.setRequestReferences(ref);
    }

    /**
     * Tests postProcess() when the "get" is repeated a couple of times.
     */
    @Test
    public void testPostProcessRepeated_testResetGetCount() throws Exception {
        /*
         * Two failures and then a success - should result in two "get" calls.
         *
         * Note: getStatus() is invoked twice during each call, so have to double up the
         * return values.
         */
        when(rawResponse.getStatus()).thenReturn(500, 500, 500, 500, 200, 200);

        when(client.get(any(), any(), any())).thenAnswer(provideResponse(rawResponse));

        // use a real executor
        params = params.toBuilder().executor(ForkJoinPool.commonPool()).build();

        oper = new SoOperation(params, soOperator) {
            @Override
            public long getWaitMsGet() {
                return 1;
            }
        };

        CompletableFuture<OperationOutcome> future2 = oper.postProcessResponse(outcome, PATH, rawResponse, response);

        assertSame(outcome, future2.get(5, TimeUnit.SECONDS));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
        assertEquals(2, oper.getGetCount());

        /*
         * repeat - this time, the "get" operations will be exhausted, so it should fail
         */
        when(rawResponse.getStatus()).thenReturn(500);

        future2 = oper.postProcessResponse(outcome, PATH, rawResponse, response);

        assertSame(outcome, future2.get(5, TimeUnit.SECONDS));
        assertEquals(PolicyResult.FAILURE_TIMEOUT, outcome.getResult());
        assertEquals(MAX_GETS + 1, oper.getGetCount());

        oper.resetGetCount();
        assertEquals(0, oper.getGetCount());
    }

    @Test
    public void testGetRequestState() {
        SoResponse resp = new SoResponse();
        assertNull(oper.getRequestState(resp));

        SoRequest req = new SoRequest();
        resp.setRequest(req);
        assertNull(oper.getRequestState(resp));

        SoRequestStatus status = new SoRequestStatus();
        req.setRequestStatus(status);
        assertNull(oper.getRequestState(resp));

        status.setRequestState("my-state");
        assertEquals("my-state", oper.getRequestState(resp));
    }

    @Test
    public void testIsSuccess() {
        // always true

        assertTrue(oper.isSuccess(rawResponse, response));

        when(rawResponse.getStatus()).thenReturn(500);
        assertTrue(oper.isSuccess(rawResponse, response));
    }

    @Test
    public void testSetOutcome() {
        // success case
        when(rawResponse.getStatus()).thenReturn(200);
        assertSame(outcome, oper.setOutcome(outcome, PolicyResult.SUCCESS, rawResponse, response));

        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
        assertEquals("200 " + ControlLoopOperation.SUCCESS_MSG, outcome.getMessage());

        // failure case
        when(rawResponse.getStatus()).thenReturn(500);
        assertSame(outcome, oper.setOutcome(outcome, PolicyResult.FAILURE, rawResponse, response));

        assertEquals(PolicyResult.FAILURE, outcome.getResult());
        assertEquals("500 " + ControlLoopOperation.FAILED_MSG, outcome.getMessage());
    }

    @Test
    public void testPrepareSoModelInfo() throws CoderException {
        verifyMissingModelInfo(target::getModelCustomizationId, target::setModelCustomizationId);
        verifyMissingModelInfo(target::getModelInvariantId, target::setModelInvariantId);
        verifyMissingModelInfo(target::getModelName, target::setModelName);
        verifyMissingModelInfo(target::getModelVersion, target::setModelVersion);
        verifyMissingModelInfo(target::getModelVersionId, target::setModelVersionId);

        // valid data
        SoModelInfo info = oper.prepareSoModelInfo();
        verifyRequest("model.json", info);

        // try with null target
        params = params.toBuilder().target(null).build();
        oper = new SoOperation(params, soOperator) {};

        assertThatIllegalArgumentException().isThrownBy(() -> oper.prepareSoModelInfo()).withMessage("missing Target");
    }

    private void verifyMissingModelInfo(Supplier<String> getter, Consumer<String> setter) {
        String original = getter.get();

        setter.accept(null);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.prepareSoModelInfo())
                        .withMessage("missing VF Module model");

        setter.accept(original);
    }

    @Test
    public void testConstructRequestInfo() throws CoderException {
        SoRequestInfo info = oper.constructRequestInfo();
        verifyRequest("reqinfo.json", info);
    }

    @Test
    public void testBuildRequestParameters() throws CoderException {
        // valid data
        SoRequestParameters reqParams = oper.buildRequestParameters();
        verifyRequest("reqparams.json", reqParams);

        // invalid json
        params.getPayload().put(SoOperation.REQ_PARAM_NM, "{invalid json");
        assertThatIllegalArgumentException().isThrownBy(() -> oper.buildRequestParameters())
                        .withMessage("invalid payload value: " + SoOperation.REQ_PARAM_NM);

        // missing data
        params.getPayload().remove(SoOperation.REQ_PARAM_NM);
        assertNull(oper.buildRequestParameters());

        // null payload
        params = params.toBuilder().payload(null).build();
        oper = new SoOperation(params, soOperator) {};
        assertNull(oper.buildRequestParameters());
    }

    @Test
    public void testBuildConfigurationParameters() {
        // valid data
        List<Map<String, String>> result = oper.buildConfigurationParameters();
        assertEquals(List.of(Collections.emptyMap()), result);

        // invalid json
        params.getPayload().put(SoOperation.CONFIG_PARAM_NM, "{invalid json");
        assertThatIllegalArgumentException().isThrownBy(() -> oper.buildConfigurationParameters())
                        .withMessage("invalid payload value: " + SoOperation.CONFIG_PARAM_NM);

        // missing data
        params.getPayload().remove(SoOperation.CONFIG_PARAM_NM);
        assertNull(oper.buildConfigurationParameters());

        // null payload
        params = params.toBuilder().payload(null).build();
        oper = new SoOperation(params, soOperator) {};
        assertNull(oper.buildConfigurationParameters());
    }

    @Test
    public void testGetVnfItem() {
        // missing data
        AaiCqResponse cq = mock(AaiCqResponse.class);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.getVnfItem(cq, oper.prepareSoModelInfo()))
                        .withMessage("missing generic VNF");

        // valid data
        GenericVnf vnf = new GenericVnf();
        when(cq.getGenericVnfByVfModuleModelInvariantId(MODEL_INVAR_ID)).thenReturn(vnf);
        assertSame(vnf, oper.getVnfItem(cq, oper.prepareSoModelInfo()));
    }

    @Test
    public void testGetServiceInstance() {
        // missing data
        AaiCqResponse cq = mock(AaiCqResponse.class);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.getServiceInstance(cq))
                        .withMessage("missing VNF Service Item");

        // valid data
        ServiceInstance instance = new ServiceInstance();
        when(cq.getServiceInstance()).thenReturn(instance);
        assertSame(instance, oper.getServiceInstance(cq));
    }

    @Test
    public void testGetDefaultTenant() {
        // missing data
        AaiCqResponse cq = mock(AaiCqResponse.class);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.getDefaultTenant(cq))
                        .withMessage("missing Tenant Item");

        // valid data
        Tenant tenant = new Tenant();
        when(cq.getDefaultTenant()).thenReturn(tenant);
        assertSame(tenant, oper.getDefaultTenant(cq));
    }

    @Test
    public void testGetDefaultCloudRegion() {
        // missing data
        AaiCqResponse cq = mock(AaiCqResponse.class);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.getDefaultCloudRegion(cq))
                        .withMessage("missing Cloud Region");

        // valid data
        CloudRegion region = new CloudRegion();
        when(cq.getDefaultCloudRegion()).thenReturn(region);
        assertSame(region, oper.getDefaultCloudRegion(cq));
    }
}
