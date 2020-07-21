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

package org.onap.policy.controlloop.actor.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;

public class BasicHttpOperationTest {
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";

    private BasicHttpOperation oper;


    @Before
    public void setUp() throws Exception {
        oper = new BasicHttpOperation(ACTOR, OPERATION);
        oper.setUpBasic();
    }

    @Test
    public void testBasicHttpOperation() {
        oper = new BasicHttpOperation();
        assertEquals(BasicOperation.DEFAULT_ACTOR, oper.actorName);
        assertEquals(BasicOperation.DEFAULT_OPERATION, oper.operationName);
    }

    @Test
    public void testBasicHttpOperationStringString() {
        assertEquals(ACTOR, oper.actorName);
        assertEquals(OPERATION, oper.operationName);
    }

    @Test
    public void testSetUp() throws Exception {
        assertNotNull(oper.client);
        assertSame(oper.client, oper.factory.get(BasicHttpOperation.MY_CLIENT));
        assertEquals(200, oper.rawResponse.getStatus());
        assertNotNull(oper.future);
        assertEquals(BasicHttpOperation.BASE_URI, oper.client.getBaseUrl());
        assertNotNull(oper.context);
        assertNotNull(oper.outcome);
        assertNotNull(oper.executor);
    }

    @Test
    public void testInitOperator() throws Exception {
        oper.initConfig();

        assertSame(oper.client, oper.config.getClient());
        assertEquals(BasicHttpOperation.PATH, oper.config.getPath());
    }

    @Test
    public void testProvideResponse() throws Exception {
        InvocationCallback<Response> cb = new InvocationCallback<>() {
            @Override
            public void completed(Response response) {
                // do nothing
            }

            @Override
            public void failed(Throwable throwable) {
                // do nothing
            }
        };


        when(oper.client.get(any(), any(), any())).thenAnswer(oper.provideResponse(oper.rawResponse));

        assertSame(oper.rawResponse, oper.client.get(cb, null, null).get());
    }
}
