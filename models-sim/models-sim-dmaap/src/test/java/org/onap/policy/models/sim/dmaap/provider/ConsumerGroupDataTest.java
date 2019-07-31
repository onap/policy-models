/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy Models
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConsumerGroupDataTest {
    private static final int WAIT_MS = 5000;
    private static final int MIN_WAIT_MS = WAIT_MS / 2;
    private static final String MY_TOPIC = "my-topic";
    private static final String MY_CONSUMER = "my-consumer";
    private static final String MSG1 = "hello";
    private static final String MSG2 = "there";
    private static final String MSG3 = "world";
    private static final int MAX_THREADS = 30;

    private MyData data;
    private MyReader thread;
    private List<MyReader> threads;

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        data = new MyData();
        thread = null;
        threads = new ArrayList<>(MAX_THREADS);
    }

    /**
     * Stops any running thread.
     */
    @After
    public void tearDown() {
        for (MyReader thr : threads) {
            thr.interrupt();
        }

        for (MyReader thr : threads) {
            thr.await();
        }
    }

    @Test
    public void testShouldRemove() throws InterruptedException {
        assertFalse(data.shouldRemove());
        assertTrue(data.shouldRemove());

        data = new MyData();

        // start a reader thread and wait for it to poll its queue
        startReader(0, 10);
        assertTrue(data.await());

        assertFalse(data.shouldRemove());
    }

    @Test
    public void testRead() {
        data.enqueue(MSG1, MSG2, MSG3, MSG1, MSG2, MSG3);

        // this reader only wants one
        startReader(1, 1);
        assertTrue(thread.await());
        assertEquals("[hello]", thread.result.toString());

        // this reader wants three
        startReader(3, 1);
        assertTrue(thread.await());
        assertEquals("[there, world, hello]", thread.result.toString());

        // this reader wants three, but will only get two
        startReader(3, 1);
        assertTrue(thread.await());
        assertEquals("[there, world]", thread.result.toString());
    }

    @Test
    public void testRead_Idle() throws InterruptedException {
        // force it to idle
        data.shouldRemove();
        data.shouldRemove();

        long tbeg = System.currentTimeMillis();
        assertSame(ConsumerGroupData.UNREADABLE_LIST, data.read(1, WAIT_MS));

        // should not have waited
        assertTrue(System.currentTimeMillis() < tbeg + MIN_WAIT_MS);
    }

    @Test
    public void testRead_NegativeCount() throws InterruptedException {
        data.enqueue(MSG1, MSG2);
        startReader(-1, 3);
        assertTrue(data.await());

        // wait time should be unaffected
        assertEquals(3L, data.waitMs2);

        assertTrue(thread.await());

        // should only return one message
        assertEquals("[hello]", thread.result.toString());
    }

    @Test
    public void testRead_NegativeWait() throws InterruptedException {
        data.enqueue(MSG1, MSG2, MSG3);
        startReader(2, -3);
        assertTrue(data.await());

        assertEquals(0L, data.waitMs2);

        assertTrue(thread.await());

        // should return two messages, as requested
        assertEquals("[hello, there]", thread.result.toString());
    }

    @Test
    public void testRead_NoMessages() throws InterruptedException {
        startReader(0, 0);
        assertTrue(data.await());

        assertTrue(thread.await());
        assertTrue(thread.result.isEmpty());
    }

    @Test
    public void testRead_MultiThreaded() {
        // queue up a bunch of messages
        final int expected = MAX_THREADS * 3;
        for (int x = 0; x < expected; ++x) {
            data.enqueue(MSG1);
        }

        for (int x = 0; x < MAX_THREADS; ++x) {
            startReader(4, 1);
        }

        int actual = 0;
        for (MyReader thr : threads) {
            thr.await();
            actual += thr.result.size();
        }

        assertEquals(expected, actual);
    }


    /**
     * Starts a reader thread.
     *
     * @param limit number of messages to read at one time
     * @param waitMs wait time, in milliseconds
     */
    private void startReader(int limit, long waitMs) {
        thread = new MyReader(limit, waitMs);

        thread.setDaemon(true);
        thread.start();

        threads.add(thread);
    }


    private class MyData extends ConsumerGroupData {

        /**
         * Decremented when {@link #getNextMessage(long)} is invoked.
         */
        private final CountDownLatch latch = new CountDownLatch(1);

        /**
         * Messages to be added to the queue when {@link #getNextMessage(long)} is
         * invoked.
         */
        private final List<String> messages = new ArrayList<>();

        /**
         * Value passed to {@link #getNextMessage(long)}.
         */
        private volatile long waitMs2 = -1;

        /**
         * Constructs the object.
         */
        public MyData() {
            super(MY_TOPIC, MY_CONSUMER);
        }

        /**
         * Arranges for messages to be injected into the queue the next time
         * {@link #getNextMessage(long)} is invoked.
         *
         * @param messages the messages to be injected
         */
        public void enqueue(String... messages) {
            this.messages.addAll(Arrays.asList(messages));
        }

        @Override
        protected String getNextMessage(long waitMs) throws InterruptedException {
            waitMs2 = waitMs;

            latch.countDown();

            synchronized (messages) {
                write(messages);
                messages.clear();
            }

            return super.getNextMessage(waitMs);
        }

        /**
         * Waits for {@link #getNextMessage(long)} to be invoked.
         *
         * @return {@code true} if {@link #getNextMessage(long)} was invoked,
         *         {@code false} if the timer expired first
         * @throws InterruptedException if the current thread is interrupted while waiting
         */
        public boolean await() throws InterruptedException {
            return latch.await(WAIT_MS, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Thread that will invoke the consumer group's read() method one time.
     */
    private class MyReader extends Thread {
        private final ConsumerGroupData group = data;
        private final int limit;
        private final long waitMs;

        /**
         * Result returned by the read() method.
         */
        private List<String> result = Collections.emptyList();

        public MyReader(int limit, long waitMs) {
            this.limit = limit;
            this.waitMs = waitMs;
        }

        @Override
        public void run() {
            try {
                result = group.read(limit, waitMs);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        /**
         * Waits for the thread to complete.
         *
         * @return {@code true} if the thread completed, {@code false} if the thread is
         *         still running
         */
        public boolean await() {
            try {
                this.join(WAIT_MS);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return !this.isAlive();
        }
    }
}
