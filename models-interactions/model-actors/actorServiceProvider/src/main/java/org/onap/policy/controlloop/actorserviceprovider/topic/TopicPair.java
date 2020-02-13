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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicEndpoint;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A pair of topics, one of which is used to publish requests and the other to receive
 * responses.
 */
public class TopicPair extends TopicListenerImpl {
    private static final Logger logger = LoggerFactory.getLogger(TopicPair.class);

    @Getter
    private final String source;

    @Getter
    private final String target;

    private final List<TopicSink> publishers;
    private final List<TopicSource> subscribers;

    /**
     * Constructs the object.
     *
     * @param source source topic name
     * @param target target topic name
     */
    public TopicPair(String source, String target) {
        this.source = source;
        this.target = target;

        publishers = getTopicEndpointManager().getTopicSinks(target);
        if (publishers.isEmpty()) {
            throw new IllegalArgumentException("no sinks for topic: " + target);
        }

        subscribers = getTopicEndpointManager().getTopicSources(Arrays.asList(source));
        if (subscribers.isEmpty()) {
            throw new IllegalArgumentException("no sources for topic: " + source);
        }
    }

    /**
     * Starts listening on the source topic(s).
     */
    public void start() {
        subscribers.forEach(topic -> topic.register(this));
    }

    /**
     * Stops listening on the source topic(s).
     */
    public void stop() {
        subscribers.forEach(topic -> topic.unregister(this));
    }

    /**
     * Stops listening on the source topic(s) and clears all of the forwarders.
     */
    @Override
    public void shutdown() {
        stop();
        super.shutdown();
    }

    /**
     * Publishes a message to the target topic.
     *
     * @param message message to be published
     * @return a list of the infrastructures on which it was published
     */
    public List<CommInfrastructure> publish(String message) {
        List<CommInfrastructure> infrastructures = new ArrayList<>(publishers.size());

        for (TopicSink topic : publishers) {
            try {
                topic.send(message);
                infrastructures.add(topic.getTopicCommInfrastructure());

            } catch (RuntimeException e) {
                logger.warn("cannot publish to {}:{}", topic.getTopicCommInfrastructure(), target, e);
            }
        }

        return infrastructures;
    }

    // these may be overridden by junit tests

    protected TopicEndpoint getTopicEndpointManager() {
        return TopicEndpointManager.getManager();
    }
}
