/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.function.BiConsumer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.message.bus.event.TopicSink;
import org.onap.policy.common.message.bus.event.TopicSource;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.simulators.TopicServer;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class BasicBidirectionalTopicOperationTest {
    private static final String ACTOR = "my-actor";
    private static final String OPERATION = "my-operation";

    @Mock
    private BiConsumer<String, StandardCoderObject> listener;

    private BasicBidirectionalTopicOperation<String> oper = new MyOperation(ACTOR, OPERATION);

    @BeforeAll
    void setUpBeforeClass() throws Exception {
        BasicBidirectionalTopicOperation.initBeforeClass(BasicBidirectionalTopicOperation.MY_SINK,
                        BasicBidirectionalTopicOperation.MY_SOURCE);
    }

    @AfterAll
    static void tearDownAfterClass() {
        BasicBidirectionalTopicOperation.destroyAfterClass();
    }

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        oper.setUpBasic();
    }

    @AfterEach
    void tearDown() {
        oper.tearDownBasic();
    }

    @Test
    void testTopicMgr() {
        assertNotNull(BasicBidirectionalTopicOperation.topicMgr.getTopicHandler(
                        BasicBidirectionalTopicOperation.MY_SINK, BasicBidirectionalTopicOperation.MY_SOURCE));
    }

    @Test
    void testBasicBidirectionalTopicOperation() {
        oper.tearDownBasic();

        oper = new MyOperation();
        oper.setUpBasic();

        assertEquals(BasicOperation.DEFAULT_ACTOR, oper.actorName);
        assertEquals(BasicOperation.DEFAULT_OPERATION, oper.operationName);
    }

    @Test
    void testSetUp() {
        assertNotNull(oper.config);
        assertNotNull(oper.outcome);
        assertNotNull(oper.executor);
    }

    @Test
    void testInitOperator() {
        oper.initConfig();

        assertSame(oper.topicHandler, oper.config.getTopicHandler());
        assertSame(oper.forwarder, oper.config.getForwarder());
        assertEquals(BasicBidirectionalTopicOperation.TIMEOUT_MS, oper.config.getTimeoutMs());
    }

    @Test
    void testProvideResponse() {
        String response = "{\"input\": 10}";

        oper.provideResponse(listener, response);

        ArgumentCaptor<StandardCoderObject> scoCaptor = ArgumentCaptor.forClass(StandardCoderObject.class);
        verify(listener).accept(eq(response), scoCaptor.capture());

        assertEquals("10", scoCaptor.getValue().getString("input"));

        // try with an invalid response
        assertThatIllegalArgumentException().isThrownBy(() -> oper.provideResponse(listener, "{invalid json"))
                        .withMessage("response is not a Map");
    }

    private static class MyOperation extends BasicBidirectionalTopicOperation<String> {
        public MyOperation() {
            super();
        }

        /**
         * Constructs the object.
         *
         * @param actor actor name
         * @param operation operation name
         */
        public MyOperation(String actor, String operation) {
            super(actor, operation);
        }

        @Override
        protected TopicServer<String> makeServer(TopicSink sink, TopicSource source) {
            return new TopicServer<>(sink, source, null, String.class) {
                @Override
                protected String process(String request) {
                    return null;
                }
            };
        }
    }
}
