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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.models.decisions.concepts.DecisionRequest;

@ExtendWith(MockitoExtension.class)
 class DecisionConfigTest {
    private static final String MY_CLIENT = "my-client";
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
    @Mock
    private Executor executor;

    private DecisionParams params;
    private DecisionConfig config;

    /**
     * Sets up.
     */
    @BeforeEach
     void setUp() {
        when(factory.get(MY_CLIENT)).thenReturn(client);

        params = DecisionParams.builder().onapName(ONAP_NAME).onapComponent(ONAP_COMP).onapInstance(ONAP_INST)
                        .action(MY_ACTION).clientName(MY_CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        config = new DecisionConfig(executor, params, factory);
    }

    @Test
     void test() {
        DecisionRequest expected = new DecisionRequest();
        expected.setOnapComponent(ONAP_COMP);
        expected.setOnapInstance(ONAP_INST);
        expected.setOnapName(ONAP_NAME);
        expected.setAction(MY_ACTION);

        DecisionRequest actual = config.makeRequest();
        assertEquals(expected, actual);

        // check value from superclass
        assertSame(executor, config.getBlockingExecutor());
        assertSame(client, config.getClient());

        // repeat, with minimal parameters
        params = DecisionParams.builder().clientName(MY_CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        config = new DecisionConfig(executor, params, factory);
        assertFalse(config.isDisabled());

        actual = config.makeRequest();
        assertEquals(new DecisionRequest(), actual);

        // try with disabled=true
        params = params.toBuilder().disabled(true).build();
        config = new DecisionConfig(executor, params, factory);
        assertTrue(config.isDisabled());
    }
}
