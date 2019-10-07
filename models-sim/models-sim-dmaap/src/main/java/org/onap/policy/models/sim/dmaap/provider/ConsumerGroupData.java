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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data associated with a consumer group. All consumer instances within a group share the
 * same data object.
 */
public class ConsumerGroupData {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerGroupData.class);

    /**
     * Returned when messages can no longer be read from this consumer group object,
     * because it is being removed from the topic. {@link #UNREADABLE_LIST} must not be
     * the same list as Collections.emptyList(), thus we wrap it.
     */
    public static final List<String> UNREADABLE_LIST = Collections.unmodifiableList(Collections.emptyList());

    /**
     * Returned when there are no messages read. Collections.emptyList() is already
     * unmodifiable, thus no need to wrap it.
     */
    private static final List<String> EMPTY_LIST = Collections.emptyList();

    /**
     * This is locked while fields other than {@link #messageQueue} are updated.
     */
    private final Object lockit = new Object();

    /**
     * Number of sweep cycles that have occurred since a consumer has attempted to read
     * from the queue. This consumer group should be removed once this count exceeds
     * {@code 1}, provided {@link #nreaders} is zero.
     */
    private int nsweeps = 0;

    /**
     * Number of consumers that are currently attempting to read from the queue. This
     * consumer group should not be removed as long as this is non-zero.
     */
    private int nreaders = 0;

    /**
     * Message queue.
     */
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();


    /**
     * Constructs the object.
     *
     * @param topicName name of the topic with which this object is associated
     * @param groupName name of the consumer group with which this object is associated
     */
    public ConsumerGroupData(String topicName, String groupName) {
        logger.info("Topic {}: add consumer group: {}", topicName, groupName);
    }

    /**
     * Determines if this consumer group should be removed. This should be invoked once
     * during each sweep cycle. When this returns {@code true}, this consumer group should
     * be immediately discarded, as any readers will sit in a spin loop waiting for it to
     * be discarded.
     *
     * @return {@code true} if this consumer group should be removed, {@code false}
     *         otherwise
     */
    public boolean shouldRemove() {
        synchronized (lockit) {
            return (nreaders == 0 && ++nsweeps > 1);
        }
    }

    /**
     * Reads messages from the queue, blocking if necessary.
     *
     * @param maxRead maximum number of messages to read
     * @param waitMs time to wait, in milliseconds, if the queue is currently empty
     * @return a list of messages read from the queue, empty if no messages became
     *         available before the wait time elapsed, or {@link #UNREADABLE_LIST} if this
     *         consumer group object is no longer active
     * @throws InterruptedException if this thread was interrupted while waiting for the
     *         first message
     */
    public List<String> read(int maxRead, long waitMs) throws InterruptedException {

        synchronized (lockit) {
            if (nsweeps > 1 && nreaders == 0) {
                // cannot use this consumer group object anymore
                return UNREADABLE_LIST;
            }

            ++nreaders;
        }

        /*
         * Note: do EVERYTHING inside of the "try" block, so that the "finally" block can
         * update the reader count.
         *
         * Do NOT hold the lockit while we're polling, as poll() may block for a while.
         */
        try {
            // always read at least one message
            int maxRead2 = Math.max(1, maxRead);
            long waitMs2 = Math.max(0, waitMs);

            // perform a blocking read of the queue
            String obj = getNextMessage(waitMs2);
            if (obj == null) {
                return EMPTY_LIST;
            }

            /*
             * List should hold all messages from the queue PLUS the one we already have.
             * Note: it's possible for additional messages to be added to the queue while
             * we're reading from it. In that case, the list will grow as needed.
             */
            List<String> lst = new ArrayList<>(Math.min(maxRead2, messageQueue.size() + 1));
            lst.add(obj);

            // perform NON-blocking read of subsequent messages
            for (int x = 1; x < maxRead2; ++x) {
                if ((obj = messageQueue.poll()) == null) {
                    break;
                }

                lst.add(obj);
            }

            return lst;

        } finally {
            synchronized (lockit) {
                --nreaders;
                nsweeps = 0;
            }
        }
    }

    /**
     * Writes messages to the queue.
     *
     * @param messages messages to be written to the queue
     */
    public void write(List<String> messages) {
        messageQueue.addAll(messages);
    }

    // the following methods may be overridden by junit tests

    /**
     * Gets the next message from the queue.
     *
     * @param waitMs time to wait, in milliseconds, if the queue is currently empty
     * @return the next message, or {@code null} if no messages became available before
     *         the wait time elapsed
     * @throws InterruptedException if this thread was interrupted while waiting for the
     *         message
     */
    protected String getNextMessage(long waitMs) throws InterruptedException {
        return messageQueue.poll(waitMs, TimeUnit.MILLISECONDS);
    }
}
