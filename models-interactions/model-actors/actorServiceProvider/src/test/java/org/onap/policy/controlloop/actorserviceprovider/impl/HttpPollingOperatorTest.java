/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actorserviceprovider.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class HttpPollingOperatorTest {
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-name";
    private static final String CLIENT = "my-client";
    private static final String PATH = "/my-path";
    private static final String POLL_PATH = "my-path-get/";
    private static final int MAX_POLLS = 3;
    private static final int POLL_WAIT_SEC = 20;
    private static final int TIMEOUT = 100;

    @Mock
    private HttpClient client;
    @Mock
    private HttpClientFactory factory;

    private HttpPollingOperator oper;

    /**
     * Initializes fields, including {@link #oper}, and resets the static fields used by
     * the REST server.
     */
    @BeforeEach
    void setUp() {
        when(factory.get(CLIENT)).thenReturn(client);

        oper = new MyOperator();

        HttpPollingParams params = HttpPollingParams.builder().pollPath(POLL_PATH).maxPolls(MAX_POLLS)
                        .pollWaitSec(POLL_WAIT_SEC).clientName(CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        Map<String, Object> paramMap = Util.translateToMap(OPERATION, params);
        oper.configure(paramMap);
    }

    @Test
    void testConstructor() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(ACTOR + "." + OPERATION, oper.getFullName());
    }

    @Test
    void testDoConfigure_testGetters() {
        assertTrue(oper.getCurrentConfig() instanceof HttpPollingConfig);

        // test invalid parameters
        Map<String, Object> paramMap2 = Util.translateToMap(OPERATION, HttpPollingParams.builder().build());
        assertThatThrownBy(() -> oper.configure(paramMap2)).isInstanceOf(ParameterValidationRuntimeException.class);
    }

    @Test
     void testGetClientFactory() {
        HttpPollingOperator oper2 = new HttpPollingOperator(ACTOR, OPERATION);
        assertNotNull(oper2.getClientFactory());
    }


    private class MyOperator extends HttpPollingOperator {
        public MyOperator() {
            super(ACTOR, OPERATION, MyOperation::new);
        }

        @Override
        protected HttpClientFactory getClientFactory() {
            return factory;
        }
    }

    private static class MyOperation extends HttpOperation<String> {
        public MyOperation(ControlLoopOperationParams params, HttpConfig config) {
            super(params, config, String.class, Collections.emptyList());
        }
    }
}
