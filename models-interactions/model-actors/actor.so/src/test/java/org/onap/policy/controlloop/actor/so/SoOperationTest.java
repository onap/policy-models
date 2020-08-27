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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.aai.domain.yang.ModelVer;
import org.onap.aai.domain.yang.ServiceInstance;
import org.onap.aai.domain.yang.Tenant;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.so.SoModelInfo;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestInfo;
import org.onap.policy.so.SoRequestStatus;
import org.onap.policy.so.SoResponse;

public class SoOperationTest extends BasicSoOperation {

    private static final String VF_COUNT_KEY = SoConstants.VF_COUNT_PREFIX
                    + "[my-model-customization-id][my-model-invariant-id][my-model-version-id]";

    private static final List<String> PROP_NAMES = Collections.emptyList();

    private static final String VERSION_ID = "1.2.3";

    private SoOperation oper;

    /**
     * Sets up.
     */
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        initConfig();

        oper = new SoOperation(params, config, PROP_NAMES) {};
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
        assertSame(config, oper.getConfig());
        assertTrue(oper.isUsePolling());

        // check when Target is null
        params = params.toBuilder().targetType(null).build();
        assertThatIllegalArgumentException().isThrownBy(() -> new SoOperation(params, config, PROP_NAMES) {})
                        .withMessageContaining("Target information");
    }

    @Test
    public void testValidateTarget() {
        // check when various fields are null
        verifyNotNull(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_CUSTOMIZATION_ID, targetEntities);
        verifyNotNull(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_INVARIANT_ID, targetEntities);
        verifyNotNull(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_VERSION_ID, targetEntities);

        // verify it's still valid
        assertThatCode(() -> new VfModuleCreate(params, config)).doesNotThrowAnyException();
    }

    private void verifyNotNull(String expectedText, Map<String, String> targetEntities) {
        String originalValue = targetEntities.get(expectedText);

        // try with null
        targetEntities.put(expectedText, null);
        assertThatIllegalArgumentException().isThrownBy(() -> new VfModuleCreate(params, config))
                        .withMessageContaining(expectedText);

        targetEntities.put(expectedText, originalValue);
    }

    @Test
    public void testStartPreprocessorAsync() {
        assertNotNull(oper.startPreprocessorAsync());
    }

    @Test
    public void testObtainVfCount_testGetVfCount_testSetVfCount() throws Exception {
        // insert CQ data so it's there for the check
        context.setProperty(AaiCqResponse.CONTEXT_KEY, makeCqResponse());

        // shouldn't actually need to do anything
        assertNull(oper.obtainVfCount());

        // verify that the count was stored
        Integer vfcount = context.getProperty(VF_COUNT_KEY);
        assertEquals(VF_COUNT, vfcount);
        assertEquals(VF_COUNT.intValue(), oper.getVfCount());

        // change the count and then verify that it isn't overwritten by another call
        oper.setVfCount(VF_COUNT + 1);

        assertNull(oper.obtainVfCount());
        vfcount = context.getProperty(VF_COUNT_KEY);
        assertEquals(VF_COUNT + 1, vfcount.intValue());
        assertEquals(VF_COUNT + 1, oper.getVfCount());
    }

    /**
     * Tests the VF Count methods when properties are being used.
     * @throws Exception if an error occurs
     */
    @Test
    public void testGetVfCount_testSetVfCount_ViaProperties() throws Exception {
        oper.setProperty(OperationProperties.DATA_VF_COUNT, VF_COUNT);

        // verify that the count was stored
        assertEquals(VF_COUNT.intValue(), oper.getVfCount());

        oper.setVfCount(VF_COUNT + 1);

        int count = oper.getProperty(OperationProperties.DATA_VF_COUNT);
        assertEquals(VF_COUNT + 1, count);
        assertEquals(VF_COUNT + 1, oper.getVfCount());
    }

    /**
     * Tests obtainVfCount() when it actually has to query.
     */
    @Test
    public void testObtainVfCountQuery() throws Exception {
        CompletableFuture<OperationOutcome> future2 = oper.obtainVfCount();
        assertNotNull(future2);
        assertTrue(executor.runAll(100));

        // not done yet
        assertFalse(future2.isDone());

        provideCqResponse(makeCqResponse());

        assertTrue(executor.runAll(100));
        assertTrue(future2.isDone());
        assertEquals(OperationResult.SUCCESS, future2.get().getResult());

        // verify that the count was stored
        Integer vfcount = context.getProperty(VF_COUNT_KEY);
        assertEquals(VF_COUNT, vfcount);

        // repeat - shouldn't need to do anything now
        assertNull(oper.obtainVfCount());
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
        assertSame(outcome, oper.setOutcome(outcome, OperationResult.SUCCESS, rawResponse, response));

        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals("200 " + ControlLoopOperation.SUCCESS_MSG, outcome.getMessage());
        assertSame(response, outcome.getResponse());

        // failure case
        when(rawResponse.getStatus()).thenReturn(500);
        assertSame(outcome, oper.setOutcome(outcome, OperationResult.FAILURE, rawResponse, response));

        assertEquals(OperationResult.FAILURE, outcome.getResult());
        assertEquals("500 " + ControlLoopOperation.FAILED_MSG, outcome.getMessage());
        assertSame(response, outcome.getResponse());
    }

    @Test
    public void testPrepareSoModelInfo() throws CoderException {
        verifyMissingModelInfo(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_CUSTOMIZATION_ID, targetEntities);
        verifyMissingModelInfo(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_INVARIANT_ID, targetEntities);
        verifyMissingModelInfo(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_NAME, targetEntities);
        verifyMissingModelInfo(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_VERSION, targetEntities);
        verifyMissingModelInfo(ControlLoopOperationParams.PARAMS_ENTITY_MODEL_VERSION_ID, targetEntities);

        // valid data
        SoModelInfo info = oper.prepareSoModelInfo();
        verifyRequest("model.json", info);

        // try with null target
        params = params.toBuilder().targetType(null).build();
        assertThatIllegalArgumentException().isThrownBy(() -> new SoOperation(params, config, PROP_NAMES) {})
                        .withMessageContaining("missing Target");
    }

    private void verifyMissingModelInfo(String key, Map<String, String> targetEntities) {
        String original = targetEntities.get(key);

        targetEntities.put(key, null);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.prepareSoModelInfo())
                        .withMessage("missing VF Module model");

        targetEntities.put(key, original);
    }

    @Test
    public void testConstructRequestInfo() throws CoderException {
        SoRequestInfo info = oper.constructRequestInfo();
        verifyRequest("reqinfo.json", info);
    }

    @Test
    public void testBuildRequestParameters() throws CoderException {
        // valid data
        verifyRequest("reqparams.json", oper.buildRequestParameters().get());

        // invalid json
        params.getPayload().put(SoOperation.REQ_PARAM_NM, "{invalid json");
        assertThatIllegalArgumentException().isThrownBy(() -> oper.buildRequestParameters())
                        .withMessage("invalid payload value: " + SoOperation.REQ_PARAM_NM);

        // missing data
        params.getPayload().remove(SoOperation.REQ_PARAM_NM);
        assertTrue(oper.buildRequestParameters().isEmpty());

        // null payload
        params = params.toBuilder().payload(null).build();
        oper = new SoOperation(params, config, PROP_NAMES) {};
        assertTrue(oper.buildRequestParameters().isEmpty());
    }

    @Test
    public void testBuildConfigurationParameters() {
        // valid data
        assertEquals(List.of(Collections.emptyMap()), oper.buildConfigurationParameters().get());

        // invalid json
        params.getPayload().put(SoOperation.CONFIG_PARAM_NM, "{invalid json");
        assertThatIllegalArgumentException().isThrownBy(() -> oper.buildConfigurationParameters())
                        .withMessage("invalid payload value: " + SoOperation.CONFIG_PARAM_NM);

        // missing data
        params.getPayload().remove(SoOperation.CONFIG_PARAM_NM);
        assertTrue(oper.buildConfigurationParameters().isEmpty());

        // null payload
        params = params.toBuilder().payload(null).build();
        oper = new SoOperation(params, config, PROP_NAMES) {};
        assertTrue(oper.buildConfigurationParameters().isEmpty());
    }

    @Test
    public void testGetItem() {
        AaiCqResponse cq = mock(AaiCqResponse.class);
        params.getContext().setProperty(AaiCqResponse.CONTEXT_KEY, cq);

        // in neither property nor custom query
        assertThatIllegalArgumentException().isThrownBy(() -> oper.getItem("propA", cq2 -> null, "not found"))
                        .withMessage("not found");

        // only in custom query
        assertEquals("valueB", oper.getItem("propB", cq2 -> "valueB", "failureB"));

        // both - should choose the property
        oper.setProperty("propC", "valueC");
        assertEquals("valueC", oper.getItem("propC", cq2 -> "valueC2", "failureC"));

        // both - should choose the property, even if it's null
        oper.setProperty("propD", null);
        assertNull(oper.getItem("propD", cq2 -> "valueD2", "failureD"));
    }

    @Test
    public void testGetVnfItem() {
        // @formatter:off
        verifyItems(OperationProperties.AAI_VNF, GenericVnf::new,
            (cq, instance) -> when(cq.getGenericVnfByVfModuleModelInvariantId(MODEL_INVAR_ID)).thenReturn(instance),
            () -> oper.getVnfItem(oper.prepareSoModelInfo()),
            "missing generic VNF");
        // @formatter:on
    }

    @Test
    public void testGetServiceInstance() {
        // @formatter:off
        verifyItems(OperationProperties.AAI_SERVICE, ServiceInstance::new,
            (cq, instance) -> when(cq.getServiceInstance()).thenReturn(instance),
            () -> oper.getServiceInstance(),
            "missing VNF Service Item");
        // @formatter:on
    }

    @Test
    public void testGetDefaultTenant() {
        // @formatter:off
        verifyItems(OperationProperties.AAI_DEFAULT_TENANT, Tenant::new,
            (cq, tenant) -> when(cq.getDefaultTenant()).thenReturn(tenant),
            () -> oper.getDefaultTenant(),
            "missing Default Tenant Item");
        // @formatter:on
    }

    @Test
    public void testGetVnfModel() {
        GenericVnf vnf = new GenericVnf();
        vnf.setModelVersionId(VERSION_ID);

        // @formatter:off
        verifyItems(OperationProperties.AAI_VNF_MODEL, ModelVer::new,
            (cq, model) -> when(cq.getModelVerByVersionId(VERSION_ID)).thenReturn(model),
            () -> oper.getVnfModel(vnf),
            "missing generic VNF Model");
        // @formatter:on
    }

    @Test
    public void testGetServiceModel() {
        ServiceInstance service = new ServiceInstance();
        service.setModelVersionId(VERSION_ID);

        // @formatter:off
        verifyItems(OperationProperties.AAI_SERVICE_MODEL, ModelVer::new,
            (cq, model) -> when(cq.getModelVerByVersionId(VERSION_ID)).thenReturn(model),
            () -> oper.getServiceModel(service),
            "missing Service Model");
        // @formatter:on
    }

    @Test
    public void testGetDefaultCloudRegion() {
        // @formatter:off
        verifyItems(OperationProperties.AAI_DEFAULT_CLOUD_REGION, CloudRegion::new,
            (cq, region) -> when(cq.getDefaultCloudRegion()).thenReturn(region),
            () -> oper.getDefaultCloudRegion(),
            "missing Default Cloud Region");
        // @formatter:on
    }

    private <T> void verifyItems(String propName, Supplier<T> maker, BiConsumer<AaiCqResponse, T> setter,
                    Supplier<T> getter, String errmsg) {

        AaiCqResponse cq = mock(AaiCqResponse.class);
        params.getContext().setProperty(AaiCqResponse.CONTEXT_KEY, cq);

        // in neither property nor custom query
        assertThatIllegalArgumentException().isThrownBy(getter::get).withMessage(errmsg);

        // only in custom query
        final T item1 = maker.get();
        setter.accept(cq, item1);
        assertSame(item1, getter.get());

        // both - should choose the property
        final T item2 = maker.get();
        oper.setProperty(propName, item2);
        assertSame(item2, getter.get());

        // both - should choose the property, even if it's null
        oper.setProperty(propName, null);
        assertNull(getter.get());
    }

    @Test
    public void testGetCoder() throws CoderException {
        Coder opcoder = oper.getCoder();

        // ensure we can decode an SO timestamp
        String json = "{'request':{'finishTime':'Fri, 15 May 2020 12:14:21 GMT'}}";
        SoResponse resp = opcoder.decode(json.replace('\'', '"'), SoResponse.class);

        LocalDateTime tfinish = resp.getRequest().getFinishTime();
        assertNotNull(tfinish);
        assertEquals(2020, tfinish.getYear());
        assertEquals(Month.MAY, tfinish.getMonth());
    }
}
