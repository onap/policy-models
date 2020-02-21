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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.function.BiConsumer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.utils.coder.StandardCoderObject;

public class BasicBidirectionalTopicOperationTest {
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";

    @Mock
    private BiConsumer<String, StandardCoderObject> listener;

    private BasicBidirectionalTopicOperation oper;


    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        oper = new BasicBidirectionalTopicOperation(ACTOR, OPERATION);
        oper.setUpBasic();
    }

    @Test
    public void testBasicBidirectionalTopicOperation() {
        oper = new BasicBidirectionalTopicOperation();
        assertEquals(BasicOperation.DEFAULT_ACTOR, oper.actorName);
        assertEquals(BasicOperation.DEFAULT_OPERATION, oper.operationName);
    }

    @Test
    public void testBasicBidirectionalTopicOperationStringString() {
        assertEquals(ACTOR, oper.actorName);
        assertEquals(OPERATION, oper.operationName);
    }

    @Test
    public void testSetUp() {
        assertNotNull(oper.config);
        assertNotNull(oper.context);
        assertNotNull(oper.outcome);
        assertNotNull(oper.executor);
    }

    @Test
    public void testInitOperator() {
        oper.initConfig();

        assertSame(oper.topicHandler, oper.config.getTopicHandler());
        assertSame(oper.forwarder, oper.config.getForwarder());
        assertEquals(BasicBidirectionalTopicOperation.TIMEOUT_MS, oper.config.getTimeoutMs());
    }

    @Test
    public void testProvideResponse() {
        String response = "{\"input\": 10}";

        oper.provideResponse(listener, response);

        ArgumentCaptor<StandardCoderObject> scoCaptor = ArgumentCaptor.forClass(StandardCoderObject.class);
        verify(listener).accept(eq(response), scoCaptor.capture());

        assertEquals("10", scoCaptor.getValue().getString("input"));

        // try with an invalid response
        assertThatIllegalArgumentException().isThrownBy(() -> oper.provideResponse(listener, "{invalid json"))
                        .withMessage("response is not a Map");
    }
}
