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

package org.onap.policy.controlloop.actorserviceprovider.topic;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicEndpoint;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.endpoints.event.comm.client.BidirectionalTopicClientException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
class BidirectionalTopicHandlerTest {
    private static final String UNKNOWN = "unknown";
    private static final String MY_SOURCE = "my-source";
    private static final String MY_SINK = "my-sink";
    private static final String KEY1 = "requestId";
    private static final String KEY2 = "subRequestId";

    @Mock
    private TopicSink publisher;

    @Mock
    private TopicSource subscriber;

    @Mock
    private TopicEndpoint mgr;

    private MyTopicHandler handler;


    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() throws BidirectionalTopicClientException {
        when(mgr.getTopicSinks(MY_SINK)).thenReturn(Arrays.asList(publisher));
        when(mgr.getTopicSources(Arrays.asList(MY_SOURCE))).thenReturn(Arrays.asList(subscriber));

        when(publisher.getTopicCommInfrastructure()).thenReturn(CommInfrastructure.NOOP);

        handler = new MyTopicHandler(MY_SINK, MY_SOURCE);

        handler.start();
    }

    @Test
    void testBidirectionalTopicHandler_testGetSource_testGetTarget() {
        assertEquals(MY_SOURCE, handler.getSourceTopic());
        assertEquals(MY_SINK, handler.getSinkTopic());

        verify(mgr).getTopicSinks(anyString());
        verify(mgr).getTopicSources(any());

        // source not found
        assertThatThrownBy(() -> new MyTopicHandler(MY_SINK, UNKNOWN))
                        .isInstanceOf(BidirectionalTopicClientException.class).hasMessageContaining("sources")
                        .hasMessageContaining(UNKNOWN);

        // target not found
        assertThatThrownBy(() -> new MyTopicHandler(UNKNOWN, MY_SOURCE))
                        .isInstanceOf(BidirectionalTopicClientException.class).hasMessageContaining("sinks")
                        .hasMessageContaining(UNKNOWN);
    }

    @Test
    void testShutdown() {
        handler.shutdown();
        verify(subscriber).unregister(any());
    }

    @Test
     void testStart() {
        verify(subscriber).register(any());
    }

    @Test
     void testStop() {
        handler.stop();
        verify(subscriber).unregister(any());
    }

    @Test
     void testAddForwarder() {
        // array form
        Forwarder forwarder = handler.addForwarder(new SelectorKey(KEY1), new SelectorKey(KEY2));
        assertNotNull(forwarder);

        // repeat using list form
        assertSame(forwarder, handler.addForwarder(Arrays.asList(new SelectorKey(KEY1), new SelectorKey(KEY2))));
    }

    @Test
     void testGetTopicEndpointManager() {
        // setting "mgr" to null should cause it to use the superclass' method
        mgr = null;
        assertNotNull(handler.getTopicEndpointManager());
    }


    private class MyTopicHandler extends BidirectionalTopicHandler {
        MyTopicHandler(String sinkTopic, String sourceTopic) throws BidirectionalTopicClientException {
            super(sinkTopic, sourceTopic);
        }

        @Override
        protected TopicEndpoint getTopicEndpointManager() {
            return (mgr != null ? mgr : super.getTopicEndpointManager());
        }
    }
}
