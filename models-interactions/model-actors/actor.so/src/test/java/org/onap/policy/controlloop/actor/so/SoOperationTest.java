/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Wipro Limited.
 * Modifications Copyright (C) 2023, 2024 Nordix Foundation.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.aai.domain.yang.CloudRegion;
import org.onap.aai.domain.yang.Tenant;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.so.SoModelInfo;
import org.onap.policy.so.SoRequest;
import org.onap.policy.so.SoRequestInfo;
import org.onap.policy.so.SoRequestStatus;
import org.onap.policy.so.SoResponse;

@ExtendWith(MockitoExtension.class)
 class SoOperationTest extends BasicSoOperation {

    private static final List<String> PROP_NAMES = Collections.emptyList();

    private SoOperation oper;

    /**
     * Sets up.
     */
    @Override
    @BeforeEach
     void setUp() throws Exception {
        super.setUp();

        initConfig();

        oper = new SoOperation(params, config, PROP_NAMES, params.getTargetEntityIds()) {};
    }

    @Test
     void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
        assertSame(config, oper.getConfig());

        // check when Target is null
        params = params.toBuilder().targetType(null).build();
        assertThatIllegalArgumentException().isThrownBy(() -> new SoOperation(params, config, PROP_NAMES) {})
                        .withMessageContaining("Target information");
    }

    @Test
     void testValidateTarget() {
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
     void testGetRequestState() {
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
     void testIsSuccess() {
        // always true

        assertTrue(oper.isSuccess(rawResponse, response));

        lenient().when(rawResponse.getStatus()).thenReturn(500);
        assertTrue(oper.isSuccess(rawResponse, response));
    }

    @Test
     void testSetOutcome() {
        // success case
        lenient().when(rawResponse.getStatus()).thenReturn(200);
        assertSame(outcome, oper.setOutcome(outcome, OperationResult.SUCCESS, rawResponse, response));

        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertEquals("200 " + ControlLoopOperation.SUCCESS_MSG, outcome.getMessage());
        assertSame(response, outcome.getResponse());

        // failure case
        lenient().when(rawResponse.getStatus()).thenReturn(500);
        assertSame(outcome, oper.setOutcome(outcome, OperationResult.FAILURE, rawResponse, response));

        assertEquals(OperationResult.FAILURE, outcome.getResult());
        assertEquals("500 " + ControlLoopOperation.FAILED_MSG, outcome.getMessage());
        assertSame(response, outcome.getResponse());
    }

    @Test
     void testPrepareSoModelInfo() throws CoderException {
        // valid data
        SoModelInfo info = oper.prepareSoModelInfo();
        verifyRequest("model.json", info);

        // try with null target
        params = params.toBuilder().targetType(null).build();
        assertThatIllegalArgumentException().isThrownBy(() -> new SoOperation(params, config, PROP_NAMES) {})
                        .withMessageContaining("missing Target");
    }

    @Test
     void testConstructRequestInfo() throws CoderException {
        SoRequestInfo info = oper.constructRequestInfo();
        verifyRequest("reqinfo.json", info);
    }

    @Test
     void testBuildRequestParameters() throws CoderException {
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
     void testBuildConfigurationParameters() {
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
     void testConstructCloudConfiguration() {
        Tenant tenantItem = new Tenant();
        tenantItem.setTenantId("my-tenant-id");

        CloudRegion cloudRegionItem = new CloudRegion();
        cloudRegionItem.setCloudRegionId("my-cloud-id");

        assertThatCode(() -> oper.constructCloudConfiguration(tenantItem, cloudRegionItem)).doesNotThrowAnyException();

        tenantItem.setTenantId(null);
        assertThatIllegalArgumentException()
                        .isThrownBy(() -> oper.constructCloudConfiguration(tenantItem, cloudRegionItem))
                        .withMessageContaining("missing tenant ID");
        tenantItem.setTenantId("my-tenant-id");

        cloudRegionItem.setCloudRegionId(null);
        assertThatIllegalArgumentException()
                        .isThrownBy(() -> oper.constructCloudConfiguration(tenantItem, cloudRegionItem))
                        .withMessageContaining("missing cloud region ID");
        cloudRegionItem.setCloudRegionId("my-cloud-id");
    }

    @Test
     void testGetRequiredText() {

        assertThatCode(() -> oper.getRequiredText("some value", "my value")).doesNotThrowAnyException();

        assertThatIllegalArgumentException().isThrownBy(() -> oper.getRequiredText("some value", null))
                        .withMessageContaining("missing some value");
    }

    @Test
     void testGetCoder() throws CoderException {
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
