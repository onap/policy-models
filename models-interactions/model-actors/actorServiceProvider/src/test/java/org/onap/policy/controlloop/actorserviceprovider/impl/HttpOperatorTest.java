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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.common.endpoints.http.client.HttpClientFactory;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

public class HttpOperatorTest {

    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-name";
    private static final String CLIENT = "my-client";
    private static final String PATH = "my-path";
    private static final long TIMEOUT = 100;

    @Mock
    private HttpClient client;

    private HttpOperator oper;

    /**
     * Initializes fields, including {@link #oper}.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        oper = new HttpOperator(ACTOR, OPERATION);
    }

    @Test
    public void testDoConfigureMapOfStringObject_testGetClient_testGetPath_testGetTimeoutSec() {
        assertNull(oper.getClient());
        assertNull(oper.getPath());
        assertEquals(0L, oper.getTimeoutSec());

        oper = new HttpOperator(ACTOR, OPERATION) {
            @Override
            protected HttpClientFactory getClientFactory() {
                HttpClientFactory factory = mock(HttpClientFactory.class);
                when(factory.get(CLIENT)).thenReturn(client);
                return factory;
            }
        };

        HttpParams params = HttpParams.builder().clientName(CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
        Map<String, Object> paramMap = Util.translateToMap(OPERATION, params);
        oper.configure(paramMap);

        assertSame(client, oper.getClient());
        assertEquals(PATH, oper.getPath());
        assertEquals(TIMEOUT, oper.getTimeoutSec());

        // test invalid parameters
        paramMap.remove("path");
        assertThatThrownBy(() -> oper.configure(paramMap)).isInstanceOf(ParameterValidationRuntimeException.class);
    }

    @Test
    public void testHttpOperator() {
        assertEquals(ACTOR, oper.getActorName());
        assertEquals(OPERATION, oper.getName());
        assertEquals(ACTOR + "." + OPERATION, oper.getFullName());
    }

    @Test
    public void testGetClient() {
        assertNotNull(oper.getClientFactory());
    }
}
