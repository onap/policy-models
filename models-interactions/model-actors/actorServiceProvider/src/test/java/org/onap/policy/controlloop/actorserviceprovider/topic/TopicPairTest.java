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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicEndpoint;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;

public class TopicPairTest {
    private static final String UNKNOWN = "unknown";
    private static final String MY_SOURCE = "pair-source";
    private static final String MY_TARGET = "pair-target";
    private static final String TEXT = "some text";

    @Mock
    private TopicSink publisher1;

    @Mock
    private TopicSink publisher2;

    @Mock
    private TopicSource subscriber1;

    @Mock
    private TopicSource subscriber2;

    @Mock
    private TopicEndpoint mgr;

    private TopicPair pair;


    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mgr.getTopicSinks(MY_TARGET)).thenReturn(Arrays.asList(publisher1, publisher2));
        when(mgr.getTopicSources(eq(Arrays.asList(MY_SOURCE)))).thenReturn(Arrays.asList(subscriber1, subscriber2));

        when(publisher1.getTopicCommInfrastructure()).thenReturn(CommInfrastructure.NOOP);
        when(publisher2.getTopicCommInfrastructure()).thenReturn(CommInfrastructure.UEB);

        pair = new MyTopicPair(MY_SOURCE, MY_TARGET);

        pair.start();
    }

    @Test
    public void testTopicPair_testGetSource_testGetTarget() {
        assertEquals(MY_SOURCE, pair.getSource());
        assertEquals(MY_TARGET, pair.getTarget());

        verify(mgr).getTopicSinks(anyString());
        verify(mgr).getTopicSources(any());

        // source not found
        assertThatIllegalArgumentException().isThrownBy(() -> new MyTopicPair(UNKNOWN, MY_TARGET))
                        .withMessageContaining("sources").withMessageContaining(UNKNOWN);

        // target not found
        assertThatIllegalArgumentException().isThrownBy(() -> new MyTopicPair(MY_SOURCE, UNKNOWN))
                        .withMessageContaining("sinks").withMessageContaining(UNKNOWN);
    }

    @Test
    public void testShutdown() {
        pair.shutdown();
        verify(subscriber1).unregister(pair);
        verify(subscriber2).unregister(pair);
    }

    @Test
    public void testStart() {
        verify(subscriber1).register(pair);
        verify(subscriber2).register(pair);
    }

    @Test
    public void testStop() {
        pair.stop();
        verify(subscriber1).unregister(pair);
        verify(subscriber2).unregister(pair);
    }

    @Test
    public void testPublish() {
        List<CommInfrastructure> infrastructures = pair.publish(TEXT);
        assertEquals(Arrays.asList(CommInfrastructure.NOOP, CommInfrastructure.UEB), infrastructures);

        verify(publisher1).send(TEXT);
        verify(publisher2).send(TEXT);

        // first one throws an exception - should have only published to the second
        when(publisher1.send(any())).thenThrow(new IllegalStateException("expected exception"));

        infrastructures = pair.publish(TEXT);
        assertEquals(Arrays.asList(CommInfrastructure.UEB), infrastructures);

        verify(publisher2, times(2)).send(TEXT);
    }

    @Test
    public void testGetTopicEndpointManager() {
        // setting "mgr" to null should cause it to use the superclass' method
        mgr = null;
        assertNotNull(pair.getTopicEndpointManager());
    }


    private class MyTopicPair extends TopicPair {
        public MyTopicPair(String source, String target) {
            super(source, target);
        }

        @Override
        protected TopicEndpoint getTopicEndpointManager() {
            return (mgr != null ? mgr : super.getTopicEndpointManager());
        }
    }
}
