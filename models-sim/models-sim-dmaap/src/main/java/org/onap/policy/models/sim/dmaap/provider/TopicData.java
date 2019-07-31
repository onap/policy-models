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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data associated with a topic.
 *
 * <p/>
 * Note: for ease of implementation, this adds a topic when a consumer polls it rather
 * than when a publisher writes to it. This is the opposite of how the real DMaaP works.
 * As a result, this will never return a topic-not-found message to the consumer.
 */
public class TopicData {
    private static final Logger logger = LoggerFactory.getLogger(TopicData.class);

    /**
     * Name of the topic with which this data is associated.
     */
    private final String topicName;

    /**
     * Maps a consumer group name to its associated data.
     */
    private final Map<String, ConsumerGroupData> group2data = new ConcurrentHashMap<>();


    /**
     * Constructs the object.
     *
     * @param topicName name of the topic with which this object is associated
     */
    public TopicData(String topicName) {
        logger.info("Topic {}: added", topicName);
        this.topicName = topicName;
    }

    /**
     * Removes idle consumers from the topic. This is typically called once during each
     * sweep cycle.
     */
    public void removeIdleConsumers() {
        Iterator<Entry<String, ConsumerGroupData>> iter = group2data.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, ConsumerGroupData> ent = iter.next();
            if (ent.getValue().shouldRemove()) {
                /*
                 * We want the minimum amount of time to elapse between invoking
                 * shouldRemove() and iter.remove(), thus all other statements (e.g.,
                 * logging) should be done AFTER iter.remove().
                 */
                iter.remove();

                logger.info("Topic {}: removed consumer group: {}", topicName, ent.getKey());
            }
        }
    }

    /**
     * Reads from a particular consumer group's queue.
     *
     * @param consumerGroup name of the consumer group from which to read
     * @param maxRead maximum number of messages to read
     * @param waitMs time to wait, in milliseconds, if the queue is currently empty
     * @return a list of messages read from the queue, empty if no messages became
     *         available before the wait time elapsed
     * @throws InterruptedException if this thread was interrupted while waiting for the
     *         first message
     */
    public List<String> read(String consumerGroup, int maxRead, long waitMs) throws InterruptedException {
        /*
         * It's possible that this thread may spin several times while waiting for
         * removeIdleConsumers() to complete its call to iter.remove(), thus we create
         * this closure once, rather than each time through the loop.
         */
        Function<String, ConsumerGroupData> maker = this::makeData;

        // loop until we get a readable list
        List<String> result;

        // @formatter:off

        do {
            result = group2data.computeIfAbsent(consumerGroup, maker).read(maxRead, waitMs);
        }
        while (result == ConsumerGroupData.UNREADABLE_LIST);

        // @formatter:on

        return result;
    }

    /**
     * Writes messages to the queues of every consumer group.
     *
     * @param messages messages to be written to the queues
     * @return the number of messages enqueued
     */
    public int write(List<Object> messages) {
        List<String> list = convertMessagesToStrings(messages);

        /*
         * We don't care if a consumer group is deleted from the map while we're adding
         * messages to it, as those messages will simply be ignored (and discarded by the
         * garbage collector).
         */
        for (ConsumerGroupData data : group2data.values()) {
            data.write(list);
        }

        return list.size();
    }

    /**
     * Converts a list of message objects to a list of message strings. If a message
     * cannot be converted for some reason, then it is not added to the result list, thus
     * the result list may be shorted than the original input list.
     *
     * @param messages objects to be converted
     * @return a list of message strings
     */
    protected List<String> convertMessagesToStrings(List<Object> messages) {
        Coder coder = new StandardCoder();
        List<String> list = new ArrayList<>(messages.size());

        for (Object msg : messages) {
            String str = convertMessageToString(msg, coder);
            if (str != null) {
                list.add(str);
            }
        }

        return list;
    }

    /**
     * Converts a message object to a message string.
     *
     * @param message message to be converted
     * @param coder used to encode the message as a string
     * @return the message string, or {@code null} if it cannot be converted
     */
    protected String convertMessageToString(Object message, Coder coder) {
        if (message == null) {
            return null;
        }

        if (message instanceof String) {
            return message.toString();
        }

        try {
            return coder.encode(message);
        } catch (CoderException e) {
            logger.warn("cannot encode {}", message, e);
            return null;
        }
    }

    // this may be overridden by junit tests

    /**
     * Makes data for a consumer group.
     *
     * @param consumerGroup name of the consumer group to make
     * @return new consumer group data
     */
    protected ConsumerGroupData makeData(String consumerGroup) {
        return new ConsumerGroupData(topicName, consumerGroup);
    }
}
