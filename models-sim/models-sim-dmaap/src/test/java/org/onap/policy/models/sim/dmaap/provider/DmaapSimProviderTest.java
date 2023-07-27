/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.models.sim.dmaap.provider;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.models.sim.dmaap.parameters.DmaapSimParameterGroup;

@RunWith(MockitoJUnitRunner.class)
public class DmaapSimProviderTest {
    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final long SWEEP_SEC = 10L;
    private static final String TOPIC1 = "topic-A";
    private static final String TOPIC2 = "topic-B";
    private static final String CONSUMER1 = "consumer-X";
    private static final String CONSUMER_ID1 = "id1";

    private MyProvider prov;

    @Mock
    private DmaapSimParameterGroup params;

    @Mock
    private ScheduledExecutorService timer;

    @Mock
    private TopicData data1;

    @Mock
    private TopicData data2;

    @Captor
    private ArgumentCaptor<List<Object>> listCaptor;

    @Captor
    private ArgumentCaptor<List<Object>> listCaptor2;

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        when(params.getTopicSweepSec()).thenReturn(SWEEP_SEC);

        prov = new MyProvider(params);
    }

    /**
     * Shuts down the provider, if it's running.
     */
    @After
    public void tearDown() {
        if (prov.isAlive()) {
            prov.shutdown();
        }
    }

    /**
     * Verifies that the constructor adds all the expected actions to the service
     * manager container.
     */
    @Test
    public void testDmaapSimProvider() {
        prov.start();
        verify(timer).scheduleWithFixedDelay(any(), eq(SWEEP_SEC), eq(SWEEP_SEC), eq(TimeUnit.SECONDS));

        prov.stop();
        verify(timer).shutdown();
    }

    @Test
    public void testProcessDmaapMessagePut_List() throws CoderException {
        prov = spy(new MyProvider(params));

        when(data1.write(any())).thenReturn(2);

        // force topics to exist
        prov.processDmaapMessageGet(TOPIC1, CONSUMER1, CONSUMER_ID1, 1, 0);
        prov.processDmaapMessageGet(TOPIC2, CONSUMER1, CONSUMER_ID1, 1, 0);

        List<Object> lst = Arrays.asList("hello", "world");
        Response resp = prov.processDmaapMessagePut(TOPIC1, lst);
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
        StandardCoderObject sco = new StandardCoder().decode(resp.getEntity().toString(), StandardCoderObject.class);
        assertEquals("2", sco.getString("count"));

        List<Object> lst2 = Arrays.asList("helloB", "worldB");
        prov.processDmaapMessagePut(TOPIC1, lst2);
        prov.processDmaapMessagePut(TOPIC2, lst2);

        // should only invoke this once for each topic
        verify(prov).makeTopicData(TOPIC1);
        verify(prov).makeTopicData(TOPIC2);

        // should process all writes
        verify(data1).write(lst);
        verify(data1).write(lst2);

        verify(data2).write(lst2);
    }

    @Test
    public void testProcessDmaapMessagePut_Single() throws CoderException {
        prov = spy(new MyProvider(params));

        // force topics to exist
        prov.processDmaapMessageGet(TOPIC1, CONSUMER1, CONSUMER_ID1, 1, 0);
        prov.processDmaapMessageGet(TOPIC2, CONSUMER1, CONSUMER_ID1, 1, 0);

        final String value1 = "abc";
        Response resp = prov.processDmaapMessagePut(TOPIC1, value1);
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());

        // ensure that the response can be decoded
        new StandardCoder().decode(resp.getEntity().toString(), StandardCoderObject.class);

        final String value2 = "def";
        prov.processDmaapMessagePut(TOPIC1, value2);
        prov.processDmaapMessagePut(TOPIC2, value2);

        // should only invoke this once for each topic
        verify(prov).makeTopicData(TOPIC1);
        verify(prov).makeTopicData(TOPIC2);

        // should process all writes as singleton lists
        verify(data1, times(2)).write(listCaptor.capture());
        assertEquals(Collections.singletonList(value1), listCaptor.getAllValues().get(0));
        assertEquals(Collections.singletonList(value2), listCaptor.getAllValues().get(1));

        verify(data2).write(listCaptor2.capture());
        assertEquals(Collections.singletonList(value2), listCaptor2.getAllValues().get(0));
    }

    @Test
    public void testProcessDmaapMessageGet() throws InterruptedException {
        List<String> msgs = Arrays.asList("400", "500");
        when(data1.read(any(), anyInt(), anyLong())).thenReturn(msgs);

        Response resp = prov.processDmaapMessageGet(TOPIC1, CONSUMER1, CONSUMER_ID1, 4, 400L);
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
        assertEquals(msgs.toString(), resp.getEntity().toString());
    }

    @Test
    public void testProcessDmaapMessageGet_Timeout() throws InterruptedException {
        when(data1.read(any(), anyInt(), anyLong())).thenReturn(Collections.emptyList());

        Response resp = prov.processDmaapMessageGet(TOPIC1, CONSUMER1, CONSUMER_ID1, 3, 300L);
        assertEquals(Status.OK.getStatusCode(), resp.getStatus());
        assertEquals("[]", resp.getEntity().toString());
    }

    @Test
    public void testProcessDmaapMessageGet_Ex() throws InterruptedException {
        BlockingQueue<Response> respQueue = new LinkedBlockingQueue<>();

        // put in a background thread, so it doesn't interrupt the tester thread
        new Thread(() -> {
            try {
                when(data1.read(any(), anyInt(), anyLong())).thenThrow(new InterruptedException(EXPECTED_EXCEPTION));
                respQueue.offer(prov.processDmaapMessageGet(TOPIC1, CONSUMER1, CONSUMER_ID1, 3, 300L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        Response resp = respQueue.poll(3, TimeUnit.SECONDS);
        assertNotNull(resp);

        assertEquals(Status.GONE.getStatusCode(), resp.getStatus());
        assertEquals("[]", resp.getEntity().toString());
    }

    @Test
    public void testSweepTopicTaskRun() {
        prov.start();
        prov.processDmaapMessageGet(TOPIC1, CONSUMER1, CONSUMER_ID1, 0, 0);
        prov.processDmaapMessageGet(TOPIC2, CONSUMER1, CONSUMER_ID1, 0, 0);

        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(timer).scheduleWithFixedDelay(captor.capture(), anyLong(), anyLong(), any(TimeUnit.class));

        captor.getValue().run();
        verify(data1).removeIdleConsumers();
        verify(data2).removeIdleConsumers();

        // run it again
        captor.getValue().run();
        verify(data1, times(2)).removeIdleConsumers();
        verify(data2, times(2)).removeIdleConsumers();
    }

    @Test
    public void testMakeTimerPool() {
        // use a real provider, so we can test the real makeTimer() method
        DmaapSimProvider prov2 = new DmaapSimProvider(params);
        prov2.start();
        assertThatCode(prov2::stop).doesNotThrowAnyException();
    }

    @Test
    public void testMakeTopicData() {
        // use a real provider, so we can test the real makeTopicData() method
        DmaapSimProvider prov2 = new DmaapSimProvider(params);
        assertThatCode(() -> prov2.processDmaapMessageGet(TOPIC1, CONSUMER1, CONSUMER_ID1, 0, 0))
                        .doesNotThrowAnyException();
    }

    @Test
    public void testGetInstance_testSetInstance() {
        DmaapSimProvider.setInstance(prov);
        assertSame(prov, DmaapSimProvider.getInstance());

        DmaapSimProvider.setInstance(null);
        assertNull(DmaapSimProvider.getInstance());
    }


    public class MyProvider extends DmaapSimProvider {

        public MyProvider(DmaapSimParameterGroup params) {
            super(params);
        }

        @Override
        protected ScheduledExecutorService makeTimerPool() {
            return timer;
        }

        @Override
        protected TopicData makeTopicData(String topicName) {
            return switch (topicName) {
                case TOPIC1 -> data1;
                case TOPIC2 -> data2;
                default -> throw new IllegalArgumentException("unknown topic name: " + topicName);
            };
        }
    }
}
