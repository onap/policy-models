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

package org.onap.policy.controlloop.actorserviceprovider.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

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
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

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
    public void testGetClient() {
        assertNotNull(oper.getClient());
    }

    @Test
    public void testMakeOperator() {
        HttpOperator oper2 = HttpOperator.makeOperator(ACTOR, OPERATION, MyOperation::new);
        assertNotNull(oper2);

        VirtualControlLoopEvent event = new VirtualControlLoopEvent();
        ControlLoopEventContext context = new ControlLoopEventContext(event);
        ControlLoopOperationParams params =
                        ControlLoopOperationParams.builder().actor(ACTOR).operation(OPERATION).context(context).build();

        Operation operation1 = oper2.buildOperation(params);
        assertNotNull(operation1);

        Operation operation2 = oper2.buildOperation(params);
        assertNotNull(operation2);
        assertNotSame(operation1, operation2);
    }

    @Test
    public void testDoConfigureMapOfStringObject_testGetClient_testGetPath_testGetTimeoutMs() {
        // start with an UNCONFIGURED operator
        oper.shutdown();
        oper = new MyOperator();

        assertNull(oper.getClient());
        assertNull(oper.getPath());

        // no timeout yet
        assertEquals(0L, oper.getTimeoutMs());

        HttpParams params = HttpParams.builder().clientName(HTTP_CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        Map<String, Object> paramMap = Util.translateToMap(OPERATION, params);
        oper.configure(paramMap);

        assertSame(client, oper.getClient());
        assertEquals(PATH, oper.getPath());

        // should use given value
        assertEquals(TIMEOUT * 1000, oper.getTimeoutMs());

        // test invalid parameters
        paramMap.remove("path");
        assertThatThrownBy(() -> oper.configure(paramMap)).isInstanceOf(ParameterValidationRuntimeException.class);
    }

    private class MyOperator extends HttpOperator {
        public MyOperator() {
            super(ACTOR, OPERATION);
        }

        @Override
        public Operation buildOperation(ControlLoopOperationParams params) {
            return null;
        }

        @Override
        protected HttpClientFactory getClientFactory() {
            return factory;
        }
    }

    private class MyOperation extends HttpOperation<String> {
        public MyOperation(ControlLoopOperationParams params, HttpOperator operator) {
            super(params, operator, String.class);
        }

        @Override
        protected CompletableFuture<OperationOutcome> startOperationAsync(int attempt, OperationOutcome outcome) {
            return null;
        }
    }
}
