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

package org.onap.policy.controlloop.actorserviceprovider.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;

@ExtendWith(MockitoExtension.class)
class HttpPollingConfigTest {
    private static final String MY_CLIENT = "my-client";
    private static final String MY_PATH = "my-path";
    private static final String POLL_PATH = "poll-path";
    private static final int TIMEOUT_SEC = 10;
    private static final int MAX_POLLS = 20;
    private static final int WAIT_SEC = 30;

    @Mock
    private HttpClient client;
    @Mock
    private HttpClientFactory factory;
    @Mock
    private Executor executor;

    private HttpPollingParams params;
    private HttpPollingConfig config;

    /**
     * Sets up.
     */
    @BeforeEach
     void setUp() {
        when(factory.get(MY_CLIENT)).thenReturn(client);

        params = HttpPollingParams.builder().maxPolls(MAX_POLLS).pollPath(POLL_PATH).pollWaitSec(WAIT_SEC)
                        .clientName(MY_CLIENT).path(MY_PATH).timeoutSec(TIMEOUT_SEC).build();
        config = new HttpPollingConfig(executor, params, factory);
    }

    @Test
     void test() {
        assertEquals(POLL_PATH + "/", config.getPollPath());
        assertEquals(MAX_POLLS, config.getMaxPolls());
        assertEquals(WAIT_SEC, config.getPollWaitSec());

        // check value from superclass
        assertSame(executor, config.getBlockingExecutor());
        assertSame(client, config.getClient());

        // path with trailing "/"
        params = params.toBuilder().pollPath(POLL_PATH + "/").build();
        config = new HttpPollingConfig(executor, params, factory);
        assertEquals(POLL_PATH + "/", config.getPollPath());
    }
}
