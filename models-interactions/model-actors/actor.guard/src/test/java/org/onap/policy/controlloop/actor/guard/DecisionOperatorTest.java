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

package org.onap.policy.controlloop.actor.guard;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

public class DecisionOperatorTest {
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-name";
    private static final String CLIENT = "my-client";
    private static final String PATH = "my-path";
    private static final int TIMEOUT = 10;
    private static final String ONAP_NAME = "onap-nap";
    private static final String ONAP_COMP = "onap-component";
    private static final String ONAP_INST = "onap-instance";
    private static final String MY_ACTION = "my-action";

    @Mock
    private HttpClient client;

    @Mock
    private HttpClientFactory factory;


    private DecisionOperator oper;

    /**
     * Initializes fields, including {@link #oper}, and resets the static fields used by
     * the REST server.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(factory.get(CLIENT)).thenReturn(client);

        oper = new MyOperator();

        GuardParams params = GuardParams.builder().onapName(ONAP_NAME).onapComponent(ONAP_COMP).onapInstance(ONAP_INST)
                        .action(MY_ACTION).clientName(CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        Map<String, Object> paramMap = Util.translateToMap(OPERATION, params);
        oper.configure(paramMap);

        assertTrue(oper.makeConfiguration(paramMap) instanceof GuardConfig);
    }

    @Test
    public void testConstructor() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(ACTOR + "." + OPERATION, oper.getFullName());
    }

    @Test
    public void testDoConfigure_testGetters() {
        assertTrue(oper.getCurrentConfig() instanceof GuardConfig);

        // test invalid parameters
        Map<String, Object> paramMap2 = Util.translateToMap(OPERATION, GuardParams.builder().build());
        assertThatThrownBy(() -> oper.configure(paramMap2)).isInstanceOf(ParameterValidationRuntimeException.class);
    }


    private class MyOperator extends DecisionOperator {
        public MyOperator() {
            super(ACTOR, OPERATION, null);
        }

        @Override
        protected HttpClientFactory getClientFactory() {
            return factory;
        }
    }
}
