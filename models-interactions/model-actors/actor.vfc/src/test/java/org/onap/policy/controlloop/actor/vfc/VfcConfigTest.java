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

package org.onap.policy.controlloop.actor.vfc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;

public class VfcConfigTest {
    private static final String MY_CLIENT = "my-client";
    private static final String MY_PATH = "my-path";
    private static final String GET_PATH = "get-path";
    private static final int TIMEOUT_SEC = 10;
    private static final int MAX_GETS = 20;
    private static final int WAIT_SEC = 30;

    @Mock
    private HttpClient client;
    @Mock
    private HttpClientFactory factory;
    @Mock
    private Executor executor;

    private VfcParams params;
    private VfcConfig config;

    /**
     * Setup.
     *
     * @throws Exception Exception
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(factory.get(MY_CLIENT)).thenReturn(client);

        params = VfcParams.builder().maxGets(MAX_GETS).pathGet(GET_PATH).waitSecGet(WAIT_SEC).clientName(MY_CLIENT)
                .path(MY_PATH).timeoutSec(TIMEOUT_SEC).build();
        config = new VfcConfig(executor, params, factory);
    }

    @Test
    public void test() {
        assertEquals(GET_PATH + "/", config.getPathGet());
        assertEquals(MAX_GETS, config.getMaxGets());
        assertEquals(WAIT_SEC, config.getWaitSecGet());

        // check value from superclass
        assertSame(executor, config.getBlockingExecutor());
        assertSame(client, config.getClient());

        // path with trailing "/"
        params = params.toBuilder().pathGet(GET_PATH + "/").build();
        config = new VfcConfig(executor, params, factory);
        assertEquals(GET_PATH + "/", config.getPathGet());
    }

}
