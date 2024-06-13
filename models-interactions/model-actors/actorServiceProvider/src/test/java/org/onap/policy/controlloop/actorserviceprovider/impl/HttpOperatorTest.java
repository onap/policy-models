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

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class HttpOperatorTest {

    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-name";
    private static final String HTTP_CLIENT = "my-client";
    private static final String PATH = "/my-path";
    private static final int TIMEOUT = 100;

    @Mock
    private HttpClient client;

    @Mock
    private HttpClientFactory factory;

    private MyOperator oper;

    /**
     * Initializes fields, including {@link #oper}, and resets the static fields used by
     * the REST server.
     */
    @BeforeEach
    public void setUp() {
        when(factory.get(HTTP_CLIENT)).thenReturn(client);

        oper = new MyOperator();

        HttpParams params = HttpParams.builder().clientName(HTTP_CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        Map<String, Object> paramMap = Util.translateToMap(OPERATION, params);
        oper.configure(paramMap);
    }

    @Test
    public void testHttpOperator() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(ACTOR + "." + OPERATION, oper.getFullName());
    }

    @Test
    public void testDoConfigureMapOfStringObject_testGetConfig() {
        // start with an UNCONFIGURED operator
        oper.shutdown();
        oper = new MyOperator();

        assertNull(oper.getCurrentConfig());

        HttpParams params = HttpParams.builder().clientName(HTTP_CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        Map<String, Object> paramMap = Util.translateToMap(OPERATION, params);
        oper.configure(paramMap);

        assertNotNull(oper.getCurrentConfig());

        // test invalid parameters
        paramMap.remove("path");
        assertThatThrownBy(() -> oper.configure(paramMap)).isInstanceOf(ParameterValidationRuntimeException.class);
    }

    @Test
    public void testBuildOperation() {
        HttpOperator oper2 = new MyOperator();
        assertNotNull(oper2);
        assertNotNull(oper2.getClientFactory());

        ControlLoopOperationParams params = ControlLoopOperationParams.builder().actor(ACTOR).operation(OPERATION)
                        .requestId(UUID.randomUUID()).build();

        // configure and start it
        HttpParams params2 = HttpParams.builder().clientName(HTTP_CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        Map<String, Object> paramMap = Util.translateToMap(OPERATION, params2);
        oper2.configure(paramMap);

        // not running yet
        assertThatIllegalStateException().isThrownBy(() -> oper2.buildOperation(params));

        oper2.start();

        Operation operation1 = oper2.buildOperation(params);
        assertNotNull(operation1);

        Operation operation2 = oper2.buildOperation(params);
        assertNotNull(operation2);
        assertNotSame(operation1, operation2);

        // with no operation-maker
        HttpOperator oper3 = new HttpOperator(ACTOR, OPERATION);
        assertThatThrownBy(() -> oper3.buildOperation(params)).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void testGetClientFactory() {
        HttpOperator oper2 = new HttpOperator(ACTOR, OPERATION);
        assertNotNull(oper2.getClientFactory());
    }

    private class MyOperator extends HttpOperator {
        public MyOperator() {
            super(ACTOR, OPERATION, MyOperation::new);
        }

        @Override
        protected HttpClientFactory getClientFactory() {
            return factory;
        }
    }

    private class MyOperation extends HttpOperation<String> {
        public MyOperation(ControlLoopOperationParams params, HttpConfig config) {
            super(params, config, String.class, Collections.emptyList());
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {
            return null;
        }
    }
}
