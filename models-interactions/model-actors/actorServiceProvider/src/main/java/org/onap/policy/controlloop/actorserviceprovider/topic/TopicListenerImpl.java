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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.onap.policy.common.message.bus.event.Topic.CommInfrastructure;
import org.onap.policy.common.message.bus.event.TopicListener;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A topic listener. When a message arrives at a topic, it is forwarded to listeners based
 * on the content of fields found within the message. However, depending on the message
 * type, the relevant fields might be found in different places within the message's
 * object hierarchy. For each different list of keys, this class maintains a
 * {@link Forwarder}, which is used to forward the message to all relevant listeners.
 * <p/>
 * Once a selector has been added, it is not removed until {@link #shutdown()} is invoked.
 * As selectors are typically only added by Operators, and not by individual Operations,
 * this should not pose a problem.
 */
public class TopicListenerImpl implements TopicListener {
    private static final Logger logger = LoggerFactory.getLogger(TopicListenerImpl.class);
    private static final StandardCoder coder = new StandardCoder();

    /**
     * Maps selector to a forwarder.
     */
    private final Map<List<SelectorKey>, Forwarder> selector2forwarder = new ConcurrentHashMap<>();


    /**
     * Removes all forwarders.
     */
    public void shutdown() {
        selector2forwarder.clear();
    }

    /**
     * Adds a forwarder, if it doesn't already exist.
     *
     * @param keys the selector keys
     * @return the forwarder associated with the given selector keys
     */
    public Forwarder addForwarder(SelectorKey... keys) {
        return addForwarder(Arrays.asList(keys));
    }

    /**
     * Adds a forwarder, if it doesn't already exist.
     *
     * @param keys the selector keys
     * @return the forwarder associated with the given selector keys
     */
    public Forwarder addForwarder(List<SelectorKey> keys) {
        return selector2forwarder.computeIfAbsent(keys, key -> new Forwarder(keys));
    }

    /**
     * Decodes the message and then forwards it to each forwarder for processing.
     */
    @Override
    public void onTopicEvent(CommInfrastructure infra, String topic, String message) {
        StandardCoderObject object;
        try {
            object = coder.decode(message, StandardCoderObject.class);
        } catch (CoderException e) {
            logger.warn("cannot decode message", e);
            return;
        }

        /*
         * We don't know which selector is appropriate for the message, so we just let
         * them all take a crack at it.
         */
        for (Forwarder forwarder : selector2forwarder.values()) {
            forwarder.onMessage(message, object);
        }
    }
}
