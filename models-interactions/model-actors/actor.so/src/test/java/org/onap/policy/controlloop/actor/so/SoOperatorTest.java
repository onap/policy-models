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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

public class SoOperatorTest {
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-name";
    private static final String CLIENT = "my-client";
    private static final String PATH = "/my-path";
    private static final String PATH_GET = "my-path-get/";
    private static final int MAX_GETS = 3;
    private static final int WAIT_SEC_GETS = 20;
    private static final int TIMEOUT = 100;

    @Mock
    private HttpClient client;

    @Mock
    private HttpClientFactory factory;


    private SoOperator oper;

    /**
     * Initializes fields, including {@link #oper}, and resets the static fields used by
     * the REST server.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(factory.get(CLIENT)).thenReturn(client);

        oper = new MyOperator();

        SoParams params = SoParams.builder().pathGet(PATH_GET).maxGets(MAX_GETS).waitSecGet(WAIT_SEC_GETS)
                        .clientName(CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        Map<String, Object> paramMap = Util.translateToMap(OPERATION, params);
        oper.configure(paramMap);
    }

    @Test
    public void testConstructor() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(ACTOR + "." + OPERATION, oper.getFullName());
    }

    @Test
    public void testMakeSoOperator() {
        oper = SoOperator.makeSoOperator(ACTOR, OPERATION, MyOperation::new);

        VirtualControlLoopEvent event = new VirtualControlLoopEvent();
        ControlLoopEventContext context = new ControlLoopEventContext(event);
        ControlLoopOperationParams params =
                        ControlLoopOperationParams.builder().actor(ACTOR).operation(OPERATION).context(context).build();

        Operation operation1 = oper.buildOperation(params);
        assertNotNull(operation1);

        Operation operation2 = oper.buildOperation(params);
        assertNotNull(operation2);
        assertNotSame(operation1, operation2);
    }

    @Test
    public void testDoConfigure_testGetters() {
        // should use given values
        assertSame(client, oper.getClient());
        assertEquals(PATH_GET, oper.getPathGet());
        assertEquals(MAX_GETS, oper.getMaxGets());
        assertEquals(WAIT_SEC_GETS, oper.getWaitSecGet());

        SoParams params = SoParams.builder().pathGet("unslashed").maxGets(MAX_GETS).waitSecGet(WAIT_SEC_GETS)
                        .clientName(CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        Map<String, Object> paramMap = Util.translateToMap(OPERATION, params);
        oper.configure(paramMap);
        assertEquals("unslashed/", oper.getPathGet());

        // test invalid parameters
        Map<String, Object> paramMap2 = Util.translateToMap(OPERATION, SoParams.builder().build());
        assertThatThrownBy(() -> oper.configure(paramMap2)).isInstanceOf(ParameterValidationRuntimeException.class);
    }


    private class MyOperator extends SoOperator {
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

    private class MyOperation extends SoOperation {
        public MyOperation(ControlLoopOperationParams params, SoOperator operator) {
            super(params, operator);
        }
    }
}
