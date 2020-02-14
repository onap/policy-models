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

package org.onap.policy.controlloop.actorserviceprovider.topic;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.utils.PropertyUtils.TriConsumer;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;

public class TopicListenerImplTest {
    private static final StandardCoder coder = new StandardCoder();
    private static final CommInfrastructure INFRA = CommInfrastructure.NOOP;
    private static final String MY_TOPIC = "my-topic";
    private static final String KEY1 = "requestId";
    private static final String KEY2 = "container";
    private static final String SUBKEY = "subRequestId";

    private static final String VALUEA_REQID = "hello";
    private static final String VALUEA_SUBREQID = "world";

    private static final String VALUEB_REQID = "bye";

    private Forwarder forwarder1;
    private Forwarder forwarder2;
    private TopicListenerImpl topic;

    @Mock
    private TriConsumer<CommInfrastructure, String, StandardCoderObject> listener1;

    @Mock
    private TriConsumer<CommInfrastructure, String, StandardCoderObject> listener1b;

    @Mock
    private TriConsumer<CommInfrastructure, String, StandardCoderObject> listener2;


    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        topic = new TopicListenerImpl();

        forwarder1 = topic.addForwarder(new SelectorKey(KEY1));
        forwarder2 = topic.addForwarder(new SelectorKey(KEY1), new SelectorKey(KEY2, SUBKEY));

        assertNotNull(forwarder1);
        assertNotNull(forwarder2);
        assertNotSame(forwarder1, forwarder2);

        forwarder1.register(Arrays.asList(VALUEA_REQID), listener1);
        forwarder1.register(Arrays.asList(VALUEB_REQID), listener1b);
        forwarder2.register(Arrays.asList(VALUEA_REQID, VALUEA_SUBREQID), listener2);
    }

    @Test
    public void testShutdown() {
        // shut it down, which should clear all forwarders
        topic.shutdown();

        // should get a new forwarder now
        Forwarder forwarder = topic.addForwarder(new SelectorKey(KEY1));
        assertNotSame(forwarder1, forwarder);
        assertNotSame(forwarder2, forwarder);

        // new forwarder should be unchanged
        assertSame(forwarder, topic.addForwarder(new SelectorKey(KEY1)));
    }

    @Test
    public void testAddForwarder() {
        assertSame(forwarder1, topic.addForwarder(new SelectorKey(KEY1)));
        assertSame(forwarder2, topic.addForwarder(new SelectorKey(KEY1), new SelectorKey(KEY2, SUBKEY)));
    }

    @Test
    public void testOnTopicEvent() {
        /*
         * send a message that should go to listener1 on forwarder1 and listener2 on
         * forwarder2
         */
        String msg = makeMessage(Map.of(KEY1, VALUEA_REQID, KEY2, Map.of(SUBKEY, VALUEA_SUBREQID)));
        topic.onTopicEvent(INFRA, MY_TOPIC, msg);

        verify(listener1).accept(eq(INFRA), eq(msg), any());
        verify(listener2).accept(eq(INFRA), eq(msg), any());

        // not to listener1b
        verify(listener1b, never()).accept(any(), any(), any());

        /*
         * now send a message that should only go to listener1b on forwarder1
         */
        msg = makeMessage(Map.of(KEY1, VALUEB_REQID, KEY2, Map.of(SUBKEY, VALUEA_SUBREQID)));
        topic.onTopicEvent(INFRA, MY_TOPIC, msg);

        // should route to listener1 on forwarder1 and listener2 on forwarder2
        verify(listener1b).accept(eq(INFRA), eq(msg), any());

        // try one where the coder throws an exception
        topic.onTopicEvent(INFRA, MY_TOPIC, "{invalid-json");

        // no extra invocations
        verify(listener1).accept(any(), any(), any());
        verify(listener1b).accept(any(), any(), any());
        verify(listener2).accept(any(), any(), any());
    }

    /**
     * Makes a message from a map.
     */
    private String makeMessage(Map<String, Object> map) {
        try {
            return coder.encode(map);
        } catch (CoderException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
