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

package org.onap.policy.simulators;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.message.bus.event.Topic.CommInfrastructure;
import org.onap.policy.common.message.bus.event.TopicSink;
import org.onap.policy.common.message.bus.event.TopicSource;
import org.onap.policy.common.utils.coder.StandardCoder;

@ExtendWith(MockitoExtension.class)
class TopicServerTest {
    private static final String MY_TOPIC = "my-topic";
    private static final String TEXT = "hello";
    private static final String RESPONSE = "world";

    @Mock
    private TopicSink sink;
    @Mock
    private TopicSource source;

    private MyServer server;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        server = new MyServer();
    }

    @Test
    void testConstructor() {
        verify(source).register(server);
    }

    @Test
    void testShutdown() {
        server.shutdown();
        verify(source).unregister(server);
    }

    @Test
    void testOnTopicEvent() {
        server.onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, "{\"text\": \"hello\"}");
        verify(sink).send(RESPONSE);
    }

    /**
     * Tests onTopicEvent() when the coder throws an exception.
     */
    @Test
    void testOnTopicEventException() {
        assertThatIllegalArgumentException()
                        .isThrownBy(() -> server.onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, "{invalid json"));

        verify(sink, never()).send(any());
    }

    /**
     * Tests onTopicEvent() when there is no response.
     */
    @Test
    void testOnTopicEventNoResponse() {
        server = new MyServer() {
            @Override
            protected String process(MyRequest request) {
                return null;
            }
        };

        server.onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, "{\"text\": \"bye-bye\"}");

        verify(sink, never()).send(any());
    }


    private class MyRequest {
        private String text;
    }

    private class MyServer extends TopicServer<MyRequest> {
        public MyServer() {
            super(sink, source, new StandardCoder(), MyRequest.class);
        }

        @Override
        protected String process(MyRequest request) {
            assertEquals(TEXT, request.text);
            return RESPONSE;
        }
    }
}
