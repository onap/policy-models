/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation.
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

import java.util.List;
import org.onap.policy.common.message.bus.event.client.BidirectionalTopicClient;
import org.onap.policy.common.message.bus.event.client.BidirectionalTopicClientException;

/**
 * Handler for a bidirectional topic, supporting both publishing and forwarding of
 * incoming messages.
 */
public class BidirectionalTopicHandler extends BidirectionalTopicClient {

    /**
     * Listener that will be attached to the topic to receive responses.
     */
    private final TopicListenerImpl listener = new TopicListenerImpl();


    /**
     * Constructs the object.
     *
     * @param sinkTopic sink topic name
     * @param sourceTopic source topic name
     * @throws BidirectionalTopicClientException if an error occurs
     */
    public BidirectionalTopicHandler(String sinkTopic, String sourceTopic) throws BidirectionalTopicClientException {
        super(sinkTopic, sourceTopic);
    }

    /**
     * Starts listening on the source topic(s).
     */
    public void start() {
        getSource().register(listener);
    }

    /**
     * Stops listening on the source topic(s).
     */
    public void stop() {
        getSource().unregister(listener);
    }

    /**
     * Stops listening on the source topic(s) and clears all of the forwarders.
     */
    public void shutdown() {
        stop();
        listener.shutdown();
    }

    public Forwarder addForwarder(SelectorKey... keys) {
        return listener.addForwarder(keys);
    }

    public Forwarder addForwarder(List<SelectorKey> keys) {
        return listener.addForwarder(keys);
    }
}
