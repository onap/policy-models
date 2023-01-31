/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.controlloop.actorserviceprovider.Util;

@RunWith(MockitoJUnitRunner.class)
public class BasicOperationTest {
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";

    private BasicOperation oper;


    @Before
    public void setUp() throws Exception {
        oper = new BasicHttpOperation(ACTOR, OPERATION);
        oper.setUpBasic();
    }

    @Test
    public void testBasicHttpOperation() {
        oper = new BasicHttpOperation();
        assertEquals(BasicHttpOperation.DEFAULT_ACTOR, oper.actorName);
        assertEquals(BasicHttpOperation.DEFAULT_OPERATION, oper.operationName);
    }

    @Test
    public void testBasicHttpOperationStringString() {
        assertEquals(ACTOR, oper.actorName);
        assertEquals(OPERATION, oper.operationName);
    }

    @Test
    public void testSetUp() throws Exception {
        assertNotNull(oper.future);
        assertNotNull(oper.outcome);
        assertNotNull(oper.executor);
    }

    @Test
    public void testMakeContext() {
        oper.makeContext();

        assertSame(oper.service, oper.params.getActorService());
        assertSame(oper.executor, oper.params.getExecutor());
        assertEquals(ACTOR, oper.params.getActor());
        assertEquals(OPERATION, oper.params.getOperation());
        assertSame(BasicHttpOperation.REQ_ID, oper.params.getRequestId());
    }

    @Test
    public void testMakePayload() {
        assertNull(oper.makePayload());
    }

    @Test
    public void testVerifyRequest() throws CoderException {
        Map<String, Object> map = Util.translateToMap("", ResourceUtils.getResourceAsString("actual.json"));
        oper.verifyRequest("expected.json", map, "svc-request-id", "vnf-id");
    }
}
