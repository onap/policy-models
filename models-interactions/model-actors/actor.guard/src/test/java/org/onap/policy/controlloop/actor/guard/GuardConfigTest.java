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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.models.decisions.concepts.DecisionRequest;

public class GuardConfigTest {
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

    private GuardParams params;
    private GuardConfig config;

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(factory.get(MY_CLIENT)).thenReturn(client);

        params = GuardParams.builder().onapName(ONAP_NAME).onapComponent(ONAP_COMP).onapInstance(ONAP_INST)
                        .action(MY_ACTION).clientName(MY_CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        config = new GuardConfig(executor, params, factory);
    }

    @Test
    public void test() {
        DecisionRequest expected = new DecisionRequest();
        expected.setOnapComponent(ONAP_COMP);
        expected.setOnapInstance(ONAP_INST);
        expected.setOnapName(ONAP_NAME);
        expected.setAction(MY_ACTION);

        DecisionRequest actual = Util.translate("", config.makeRequest(), DecisionRequest.class);
        assertEquals(expected, actual);

        // check value from superclass
        assertSame(executor, config.getBlockingExecutor());
        assertSame(client, config.getClient());

        // repeat, with minimal parameters
        params = GuardParams.builder().clientName(MY_CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        config = new GuardConfig(executor, params, factory);
        assertFalse(config.isDisabled());

        actual = Util.translate("", config.makeRequest(), DecisionRequest.class);
        assertEquals(new DecisionRequest(), actual);

        // try with disabled=true
        params = params.toBuilder().disabled(true).build();
        config = new GuardConfig(executor, params, factory);
        assertTrue(config.isDisabled());
    }
}
