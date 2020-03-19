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

package org.onap.policy.simulators;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.utils.coder.StandardCoder;

public class TopicServerTest {
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
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        server = new MyServer();
    }

    @Test
    public void testConstructor() {
        verify(source).register(server);
    }

    @Test
    public void testShutdown() {
        server.shutdown();
        verify(source).unregister(server);
    }

    @Test
    public void testOnTopicEvent() {
        server.onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, "{\"text\": \"hello\"}");
        verify(sink).send(RESPONSE);
    }

    /**
     * Tests onTopicEvent() when the coder throws an exception.
     */
    @Test
    public void testOnTopicEventException() {
        assertThatIllegalArgumentException()
                        .isThrownBy(() -> server.onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, "{invalid json"));

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
