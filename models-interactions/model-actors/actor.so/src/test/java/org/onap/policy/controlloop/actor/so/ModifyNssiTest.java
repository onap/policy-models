/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited.
 * Modifications Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020,2023,2024 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingParams;
import org.onap.policy.so.SoResponse;

@ExtendWith(MockitoExtension.class)
public class ModifyNssiTest extends BasicSoOperation {

    private ModifyNssi oper;


    public ModifyNssiTest() {
        super(DEFAULT_ACTOR, ModifyNssi.NAME);
    }

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
        initBeforeClass();
    }

    @AfterAll
    public static void tearDownAfterClass() {
        destroyAfterClass();
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        oper = new ModifyNssi(params, config);
    }

    @Test
    public void testSuccess() throws Exception {
        HttpPollingParams opParams = HttpPollingParams.builder().clientName(MY_CLIENT).path("3gppservices/v7/modify")
                .pollPath("orchestrationRequests/v5/").maxPolls(2).build();
        config = new HttpPollingConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());
        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).build();

        oper = new ModifyNssi(params, config);
        oper.setProperty(OperationProperties.EVENT_PAYLOAD, getPayload());
        outcome = oper.start().get();

        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertInstanceOf(SoResponse.class, outcome.getResponse());
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(ModifyNssi.NAME, oper.getName());
        assertFalse(oper.isUsePolling());

        params = params.toBuilder().targetType(null).build();
        assertThatIllegalArgumentException().isThrownBy(() -> new ModifyNssi(params, config))
                .withMessageContaining("Target information");
    }

    @Test
    public void testGetPropertyNames() {
        assertThat(oper.getPropertyNames()).isEqualTo(List.of(OperationProperties.EVENT_PAYLOAD));
    }

    private String getPayload() {
        return ResourceUtils.getResourceAsString("src/test/resources/ModifyNSSI.json");
    }

    /**
     * Tests makeRequest() when a property is missing.
     */
    @Test
    public void testMakeRequestMissingProperty() throws Exception {
        oper = new ModifyNssi(params, config);

        assertThatIllegalStateException().isThrownBy(() -> oper.makeRequest())
                        .withMessageContaining("missing event payload");
    }

}
