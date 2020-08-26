/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 Wipro Limited.
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.controlloop.actorserviceprovider.OperationProperties;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingParams;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.so.SoResponse;

public class ModifyNssiTest extends BasicSoOperation {

    private ModifyNssi oper;

    public ModifyNssiTest() {
        super(DEFAULT_ACTOR, ModifyNssi.NAME);
    }

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        initBeforeClass();
    }

    @AfterClass
    public static void tearDownAfterClass() {
        destroyAfterClass();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        oper = new ModifyNssi(params, config);
    }

    @Test
    public void testSuccess() throws Exception {
        HttpPollingParams opParams = HttpPollingParams.builder().clientName(MY_CLIENT)
                .path("3gppservices/v7/modify").pollPath("orchestrationRequests/v5/")
                .maxPolls(2).build();
        config = new HttpPollingConfig(blockingExecutor, opParams, HttpClientFactoryInstance.getClientFactory());
        params = params.toBuilder().retry(0).timeoutSec(5).executor(blockingExecutor).payload(getPayload()).build();

        oper = new ModifyNssi(params, config);

        outcome = oper.start().get();

        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
        assertTrue(outcome.getResponse() instanceof SoResponse);
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(ModifyNssi.NAME, oper.getName());
        assertFalse(oper.isUsePolling());

        params = params.toBuilder().target(null).build();
        assertThatIllegalArgumentException().isThrownBy(() -> new ModifyNssi(params, config))
                .withMessageContaining("Target information");
    }

    @Test
    public void testGetPropertyNames() {
        assertThat(oper.getPropertyNames()).isEqualTo(
                List.of(
                        OperationProperties.AAI_SERVICE,
                        OperationProperties.EVENT_PAYLOAD));
    }

    private Map<String, Object> getPayload() {
        String payloadString = ResourceUtils
                .getResourceAsString("src/test/resources/ModifyNSSI.json");
        try {
            return oper.getCoder().convert(payloadString, Map.class);
        } catch (CoderException e) {
            return null;
        }
    }
}
