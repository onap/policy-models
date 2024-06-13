/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.function.Function;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.client.BidirectionalTopicClientException;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicActorParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;

@ExtendWith(MockitoExtension.class)
public class BidirectionalTopicActorTest {

    private static final String ACTOR = "my-actor";
    private static final String UNKNOWN = "unknown";
    private static final String MY_SINK = "my-sink";
    private static final String MY_SOURCE1 = "my-source-A";
    private static final String MY_SOURCE2 = "my-source-B";
    private static final int TIMEOUT = 10;

    @Mock
    private BidirectionalTopicHandler handler1;
    @Mock
    private BidirectionalTopicHandler handler2;

    private BidirectionalTopicActor<BidirectionalTopicActorParams> actor;


    /**
     * Configures the endpoints.
     */
    @BeforeAll
    public static void setUpBeforeClass() {
        Properties props = new Properties();
        props.setProperty("noop.sink.topics", MY_SINK);
        props.setProperty("noop.source.topics", MY_SOURCE1 + "," + MY_SOURCE2);

        // clear all topics and then configure one sink and two sources
        TopicEndpointManager.getManager().shutdown();
        TopicEndpointManager.getManager().addTopicSinks(props);
        TopicEndpointManager.getManager().addTopicSources(props);
    }

    @AfterAll
    public static void tearDownAfterClass() {
        // clear all topics after the tests
        TopicEndpointManager.getManager().shutdown();
    }

    /**
     * Sets up.
     */
    @BeforeEach
    public void setUp() {
        actor = new MyActor();
        actor.configure(Util.translateToMap(ACTOR, makeParams()));
    }

    @Test
    public void testDoStart() throws BidirectionalTopicClientException {
        // allocate some handlers
        actor.getTopicHandler(MY_SINK, MY_SOURCE1);
        actor.getTopicHandler(MY_SINK, MY_SOURCE2);

        // start it
        actor.start();

        verify(handler1).start();
        verify(handler2).start();

        verify(handler1, never()).stop();
        verify(handler2, never()).stop();

        verify(handler1, never()).shutdown();
        verify(handler2, never()).shutdown();
    }

    @Test
    public void testDoStop() throws BidirectionalTopicClientException {
        // allocate some handlers
        actor.getTopicHandler(MY_SINK, MY_SOURCE1);
        actor.getTopicHandler(MY_SINK, MY_SOURCE2);

        // start it
        actor.start();

        // stop it
        actor.stop();

        verify(handler1).stop();
        verify(handler2).stop();

        verify(handler1, never()).shutdown();
        verify(handler2, never()).shutdown();
    }

    @Test
    public void testDoShutdown() throws BidirectionalTopicClientException {

        // allocate some handlers
        actor.getTopicHandler(MY_SINK, MY_SOURCE1);
        actor.getTopicHandler(MY_SINK, MY_SOURCE2);

        // start it
        actor.start();

        // stop it
        actor.shutdown();

        verify(handler1).shutdown();
        verify(handler2).shutdown();

        verify(handler1, never()).stop();
        verify(handler2, never()).stop();
    }

    @Test
    public void testMakeOperatorParameters() {
        BidirectionalTopicActorParams params = makeParams();

        final BidirectionalTopicActor<BidirectionalTopicActorParams> prov =
            new BidirectionalTopicActor<>(ACTOR, BidirectionalTopicActorParams.class);
        Function<String, Map<String, Object>> maker =
            prov.makeOperatorParameters(Util.translateToMap(prov.getName(), params));

        assertNull(maker.apply(UNKNOWN));

        // use a TreeMap to ensure the properties are sorted
        assertEquals("{sinkTopic=my-sink, sourceTopic=my-source-A, timeoutSec=10}",
            new TreeMap<>(maker.apply("operA")).toString());

        assertEquals("{sinkTopic=my-sink, sourceTopic=topicB, timeoutSec=10}",
            new TreeMap<>(maker.apply("operB")).toString());

        // with invalid actor parameters
        params.setOperations(null);
        Map<String, Object> map = Util.translateToMap(prov.getName(), params);
        assertThatThrownBy(() -> prov.makeOperatorParameters(map))
            .isInstanceOf(ParameterValidationRuntimeException.class);
    }

    @Test
    public void testBidirectionalTopicActor() {
        assertEquals(ACTOR, actor.getName());
        assertEquals(ACTOR, actor.getFullName());
    }

    @Test
    public void testGetTopicHandler() throws BidirectionalTopicClientException {
        assertSame(handler1, actor.getTopicHandler(MY_SINK, MY_SOURCE1));
        assertSame(handler2, actor.getTopicHandler(MY_SINK, MY_SOURCE2));

        assertThatIllegalArgumentException().isThrownBy(() -> actor.getTopicHandler(UNKNOWN, MY_SOURCE1));
    }

    @Test
    public void testMakeTopicHandler() throws BidirectionalTopicClientException {
        // use a real actor
        actor = new BidirectionalTopicActor<>(ACTOR, BidirectionalTopicActorParams.class);

        handler1 = actor.getTopicHandler(MY_SINK, MY_SOURCE1);
        handler2 = actor.getTopicHandler(MY_SINK, MY_SOURCE2);

        assertNotNull(handler1);
        assertNotNull(handler2);
        assertNotSame(handler1, handler2);
    }


    private BidirectionalTopicActorParams makeParams() {
        BidirectionalTopicActorParams params = new BidirectionalTopicActorParams();
        params.setSinkTopic(MY_SINK);
        params.setSourceTopic(MY_SOURCE1);
        params.setTimeoutSec(TIMEOUT);

        // @formatter:off
        params.setOperations(Map.of(
            "operA", Map.of(),
            "operB", Map.of("sourceTopic", "topicB")));
        // @formatter:on
        return params;
    }

    private class MyActor extends BidirectionalTopicActor<BidirectionalTopicActorParams> {

        public MyActor() {
            super(ACTOR, BidirectionalTopicActorParams.class);
        }

        @Override
        protected BidirectionalTopicHandler makeTopicHandler(String sinkTopic, String sourceTopic)
            throws BidirectionalTopicClientException {

            if (MY_SINK.equals(sinkTopic)) {
                if (MY_SOURCE1.equals(sourceTopic)) {
                    return handler1;
                } else if (MY_SOURCE2.equals(sourceTopic)) {
                    return handler2;
                }
            }

            throw new BidirectionalTopicClientException("no topic " + sinkTopic + "/" + sourceTopic);
        }
    }
}
