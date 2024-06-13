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
public class HttpConfigTest {
    private static final String MY_CLIENT = "my-client";
    private static final String MY_PATH = "my-path";
    private static final int TIMEOUT_SEC = 10;

    @Mock
    private HttpClient client;
    @Mock
    private HttpClientFactory factory;
    @Mock
    private Executor executor;

    private HttpConfig config;

    /**
     * Sets up.
     */
    @BeforeEach
    public void setUp() {
        when(factory.get(MY_CLIENT)).thenReturn(client);

        HttpParams params = HttpParams.builder().clientName(MY_CLIENT).path(MY_PATH).timeoutSec(TIMEOUT_SEC).build();
        config = new HttpConfig(executor, params, factory);
    }

    @Test
    public void test() {
        assertSame(executor, config.getBlockingExecutor());
        assertSame(client, config.getClient());
        assertEquals(MY_PATH, config.getPath());
        assertEquals(1000L * TIMEOUT_SEC, config.getTimeoutMs());
    }
}
