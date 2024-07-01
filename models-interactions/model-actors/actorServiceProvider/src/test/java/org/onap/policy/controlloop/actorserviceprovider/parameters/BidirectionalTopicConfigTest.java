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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicManager;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;

@ExtendWith(MockitoExtension.class)
class BidirectionalTopicConfigTest {
    private static final String MY_SINK = "my-sink";
    private static final String MY_SOURCE = "my-source";
    private static final int TIMEOUT_SEC = 10;

    @Mock
    private BidirectionalTopicManager topicManager;
    @Mock
    private BidirectionalTopicHandler topicHandler;
    @Mock
    private Forwarder forwarder;
    @Mock
    private Executor executor;

    private BidirectionalTopicConfig config;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        List<SelectorKey> keys = Arrays.asList(new SelectorKey(""));

        when(topicManager.getTopicHandler(MY_SINK, MY_SOURCE)).thenReturn(topicHandler);
        when(topicHandler.addForwarder(keys)).thenReturn(forwarder);

        BidirectionalTopicParams params = BidirectionalTopicParams.builder().sinkTopic(MY_SINK).sourceTopic(MY_SOURCE)
                        .timeoutSec(TIMEOUT_SEC).build();
        config = new BidirectionalTopicConfig(executor, params, topicManager, keys);
    }

    @Test
     void test() {
        assertSame(executor, config.getBlockingExecutor());
        assertSame(topicHandler, config.getTopicHandler());
        assertSame(forwarder, config.getForwarder());
        assertEquals(1000L * TIMEOUT_SEC, config.getTimeoutMs());
    }
}
