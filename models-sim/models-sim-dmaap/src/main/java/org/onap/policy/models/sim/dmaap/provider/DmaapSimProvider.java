/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2019 Nordix Foundation.
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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.MutablePair;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.sim.dmaap.DmaapSimRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider to simulate DMaaP.
 *
 * @author Liam Fallon (liam.fallon@est.tech)
 */
public class DmaapSimProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(DmaapSimProvider.class);

    // Time for a get to wait before checking of a message has come
    private static final long DMAAP_SIM_WAIT_TIME = 50;

    // recurring constants
    private static final String WITH_TIMEOUT = " with timeout ";

    // The map of topic messages
    private static final Map<String, SortedMap<Integer, Object>> topicMessageMap = new LinkedHashMap<>();

    // The map of topic messages
    private static final Map<String, Map<String, MutablePair<Integer, String>>> consumerGroupsMap =
            new LinkedHashMap<>();

    /**
     * Process a DMaaP message.
     *
     * @param topicName The topic name
     * @param dmaapMessage the message to process
     * @return a response to the message
     */
    public Response processDmaapMessagePut(final String topicName, final Object dmaapMessage) {
        LOGGER.debug("Topic:" + topicName + ", Received DMaaP message: " + dmaapMessage);

        synchronized (topicMessageMap) {
            SortedMap<Integer, Object> messageMap = topicMessageMap.get(topicName);
            if (messageMap == null) {
                messageMap = new TreeMap<>();
                topicMessageMap.put(topicName, messageMap);
                LOGGER.debug("Topic:" + topicName + ", created topic message map");
            }

            int nextKey = (messageMap.isEmpty() ? 0 : messageMap.lastKey() + 1);

            messageMap.put(nextKey, dmaapMessage);
            LOGGER.debug("Topic:" + topicName + ", cached DMaaP message " + nextKey + ": " + dmaapMessage);
        }

        return Response.status(Response.Status.OK).entity("{\n    \"serverTimeMs\": 0,\n    \"count\": 1\n}").build();
    }

    /**
     * Wait for and return a DMaaP message.
     *
     * @param topicName The topic to wait on
     * @param consumerGroup the consumer group that is waiting
     * @param consumerId the consumer ID that is waiting
     * @param timeout the length of time to wait for
     * @return the DMaaP message or
     */
    public Response processDmaapMessageGet(final String topicName, final String consumerGroup, final String consumerId,
            final int timeout) {

        LOGGER.debug("Topic:" + topicName + ", Request for DMaaP message: " + consumerGroup + ":" + consumerId
                + WITH_TIMEOUT + timeout);

        MutablePair<Integer, String> consumerGroupPair = null;

        synchronized (consumerGroupsMap) {
            Map<String, MutablePair<Integer, String>> consumerGroupMap = consumerGroupsMap.get(topicName);
            if (consumerGroupMap == null) {
                consumerGroupMap = new LinkedHashMap<>();
                consumerGroupsMap.put(topicName, consumerGroupMap);
                LOGGER.trace("Topic:" + topicName + ", Created consumer map entry for consumer group " + consumerGroup);
            }

            consumerGroupPair = consumerGroupMap.get(consumerGroup);
            if (consumerGroupPair == null) {
                consumerGroupPair = new MutablePair<>(-1, consumerId);
                consumerGroupMap.put(consumerGroup, consumerGroupPair);
                LOGGER.trace("Topic:" + topicName + ", Created consumer group entry for consumer group " + consumerGroup
                        + ":" + consumerId);
            }
        }

        long timeOfTimeout = System.currentTimeMillis() + timeout;

        do {

            Object waitingMessages = getWaitingMessages(topicName, consumerGroupPair);
            if (waitingMessages != null) {
                LOGGER.debug("Topic:" + topicName + ", Request for DMaaP message: " + consumerGroup + ":" + consumerId
                        + WITH_TIMEOUT + timeout + ", returning messages " + waitingMessages);
                return Response.status(Response.Status.OK).entity(waitingMessages).build();
            }

            try {
                Thread.sleep(DMAAP_SIM_WAIT_TIME);
            } catch (InterruptedException ie) {
                String errorMessage = "Interrupt on wait on simulation of DMaaP topic " + topicName + " for request ID "
                        + consumerGroup + ":" + consumerId + WITH_TIMEOUT + timeout;
                LOGGER.warn(errorMessage, ie);
                throw new DmaapSimRuntimeException(errorMessage, ie);
            }
        }
        while (timeOfTimeout > System.currentTimeMillis());

        LOGGER.trace("Topic:" + topicName + ", timed out waiting for messages : " + consumerGroup + ":" + consumerId
                + WITH_TIMEOUT + timeout);
        return Response.status(Response.Status.REQUEST_TIMEOUT).build();
    }

    /**
     * Return any messages on this topic with a message number greater than the supplied message number.
     *
     * @param topicName the topic name to check
     * @param consumerGroupPair the pair with the information on the last message retrieved
     * @return the messages or null if there are none
     */
    private Object getWaitingMessages(final String topicName, final MutablePair<Integer, String> consumerGroupPair) {
        String foundMessageList = "[";

        synchronized (topicMessageMap) {
            SortedMap<Integer, Object> messageMap = topicMessageMap.get(topicName);
            if (messageMap == null || messageMap.lastKey() <= consumerGroupPair.getLeft()) {
                return null;
            }

            boolean first = true;
            for (Object dmaapMessage : messageMap.tailMap(consumerGroupPair.getLeft() + 1).values()) {
                if (first) {
                    first = false;
                } else {
                    foundMessageList += ",";
                }
                try {
                    foundMessageList += new StandardCoder().encode(dmaapMessage);
                } catch (CoderException e) {
                    e.printStackTrace();
                }
            }
            foundMessageList += ']';

            LOGGER.debug("Topic:" + topicName + ", returning DMaaP messages from  " + consumerGroupPair.getLeft()
                    + " to " + messageMap.lastKey());
            synchronized (consumerGroupsMap) {
                consumerGroupPair.setLeft(messageMap.lastKey());
            }
        }

        return (foundMessageList.length() < 3 ? null : foundMessageList);
    }
}
