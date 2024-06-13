/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.controlloop.actor.xacml;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

@ExtendWith(MockitoExtension.class)
 class DecisionOperatorTest {
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

    @InjectMocks
    private DecisionOperator oper;

    /**
     * Initializes fields, including {@link #oper}, and resets the static fields used by
     * the REST server.
     */
    @BeforeEach
     void setUp() {
        when(factory.get(CLIENT)).thenReturn(client);

        oper = new MyOperator();

        DecisionParams params =
                        DecisionParams.builder().onapName(ONAP_NAME).onapComponent(ONAP_COMP).onapInstance(ONAP_INST)
                                        .action(MY_ACTION).clientName(CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        Map<String, Object> paramMap = Util.translateToMap(OPERATION, params);
        oper.configure(paramMap);

        assertInstanceOf(DecisionConfig.class, oper.makeConfiguration(paramMap));
    }

    @Test
     void testConstructor() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(ACTOR + "." + OPERATION, oper.getFullName());
    }

    @Test
     void testDoConfigure_testGetters() {
        assertInstanceOf(DecisionConfig.class, oper.getCurrentConfig());

        // test invalid parameters
        Map<String, Object> paramMap2 = Util.translateToMap(OPERATION, DecisionParams.builder().build());
        assertThatThrownBy(() -> oper.configure(paramMap2)).isInstanceOf(ParameterValidationRuntimeException.class);
    }


    private class MyOperator extends DecisionOperator {
        MyOperator() {
            super(ACTOR, OPERATION, null);
        }

        @Override
        protected HttpClientFactory getClientFactory() {
            return factory;
        }
    }
}
