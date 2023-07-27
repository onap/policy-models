/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019, 2023 Nordix Foundation.
 *  Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
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
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.models.sim.dmaap.provider;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.onap.policy.common.utils.services.ServiceManagerContainer;
import org.onap.policy.models.sim.dmaap.parameters.DmaapSimParameterGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider to simulate DMaaP.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DmaapSimProvider extends ServiceManagerContainer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapSimProvider.class);

    @Getter
    @Setter
    private static DmaapSimProvider instance;

    /**
     * Maps a topic name to its data.
     */
    private final Map<String, TopicData> topic2data = new ConcurrentHashMap<>();

    /**
     * Thread used to remove idle consumers from the topics.
     */
    private ScheduledExecutorService timerPool;


    /**
     * Constructs the object.
     *
     * @param params parameters
     */
    public DmaapSimProvider(DmaapSimParameterGroup params) {
        addAction("Topic Sweeper", () -> {
            timerPool = makeTimerPool();
            timerPool.scheduleWithFixedDelay(new SweeperTask(), params.getTopicSweepSec(), params.getTopicSweepSec(),
                            TimeUnit.SECONDS);
        }, () -> timerPool.shutdown());
    }

    /**
     * Process a DMaaP message.
     *
     * @param topicName the topic name
     * @param dmaapMessage the message to process
     * @return a response to the message
     */
    @SuppressWarnings("unchecked")
    public Response processDmaapMessagePut(final String topicName, final Object dmaapMessage) {
        LOGGER.debug("Topic: {}, Received DMaaP message(s): {}", topicName, dmaapMessage);

        List<Object> lst;

        if (dmaapMessage instanceof List) {
            lst = (List<Object>) dmaapMessage;
        } else {
            lst = Collections.singletonList(dmaapMessage);
        }

        TopicData topic = topic2data.get(topicName);

        /*
         * Write all messages and return the count. If the topic doesn't exist yet, then
         * there are no subscribers to receive the messages, thus treat it as if all
         * messages were published.
         */
        int nmessages = (topic != null ? topic.write(lst) : lst.size());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("serverTimeMs", 0);
        map.put("count", nmessages);

        return Response.status(Response.Status.OK).entity(map).build();
    }

    /**
     * Wait for and return a DMaaP message.
     *
     * @param topicName The topic to wait on
     * @param consumerGroup the consumer group that is waiting
     * @param consumerId the consumer ID that is waiting
     * @param limit the maximum number of messages to get
     * @param timeoutMs the length of time to wait for
     * @return the DMaaP message or
     */
    public Response processDmaapMessageGet(final String topicName, final String consumerGroup, final String consumerId,
                    final int limit, final long timeoutMs) {

        LOGGER.debug("Topic: {}, Request for DMaaP message: {}: {} with limit={} timeout={}", topicName, consumerGroup,
                        consumerId, limit, timeoutMs);

        try {
            List<String> lst = topic2data.computeIfAbsent(topicName, this::makeTopicData).read(consumerGroup, limit,
                            timeoutMs);

            LOGGER.debug("Topic: {}, Retrieved {} messages for: {}: {}", topicName, lst.size(), consumerGroup,
                            consumerId);
            return Response.status(Status.OK).entity(lst).build();

        } catch (InterruptedException e) {
            LOGGER.warn("Topic: {}, Request for DMaaP message interrupted: {}: {}", topicName, consumerGroup,
                            consumerId, e);
            Thread.currentThread().interrupt();
            return Response.status(Status.GONE).entity(Collections.emptyList()).build();
        }
    }

    /**
     * Returns the list of default topics.
     *
     * @return the topic list
     */
    public Response processDmaapTopicsGet() {

        LOGGER.debug("Request for listing DMaaP topics");
        var response = new DmaapGetTopicResponse();
        response.setTopics(List.of("POLICY-PDP-PAP", "POLICY-NOTIFICATION", "unauthenticated.DCAE_CL_OUTPUT",
                        "POLICY-CL-MGT"));
        return Response.status(Status.OK).entity(response).build();
    }

    /**
     * Task to remove idle consumers from each topic.
     */
    private class SweeperTask implements Runnable {
        @Override
        public void run() {
            topic2data.values().forEach(TopicData::removeIdleConsumers);
        }
    }

    // the following methods may be overridden by junit tests

    /**
     * Makes a new timer pool.
     *
     * @return a new timer pool
     */
    protected ScheduledExecutorService makeTimerPool() {
        return Executors.newScheduledThreadPool(1);
    }

    /**
     * Makes a new topic.
     *
     * @param topicName topic name
     * @return a new topic
     */
    protected TopicData makeTopicData(String topicName) {
        return new TopicData(topicName);
    }
}
