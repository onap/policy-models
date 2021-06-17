/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import lombok.AllArgsConstructor;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forwarder that selectively forwards message to listeners based on the content of the
 * message. Each forwarder is associated with a single set of selector keys. Listeners are
 * then registered with that forwarder for a particular set of values for the given keys.
 */
@AllArgsConstructor
public class Forwarder {
    private static final Logger logger = LoggerFactory.getLogger(Forwarder.class);

    /**
     * Maps a set of field values to one or more listeners.
     */
    // @formatter:off
    private final Map<List<String>, Map<BiConsumer<String, StandardCoderObject>, String>>
                values2listeners = new ConcurrentHashMap<>();
    // @formatter:on

    /**
     * Keys used to extract the field values from the {@link StandardCoderObject}.
     */
    private final List<SelectorKey> keys;

    /**
     * Registers a listener for messages containing the given field values.
     *
     * @param values field values of interest, in one-to-one correspondence with the keys
     * @param listener listener to register
     */
    public void register(List<String> values, BiConsumer<String, StandardCoderObject> listener) {
        if (keys.size() != values.size()) {
            throw new IllegalArgumentException("key/value mismatch");
        }

        logger.info("register topic listener for key={} value={}", keys, values);

        values2listeners.compute(values, (key, listeners) -> {
            Map<BiConsumer<String, StandardCoderObject>, String> map = listeners;
            if (map == null) {
                map = new ConcurrentHashMap<>();
            }

            map.put(listener, "");
            return map;
        });
    }

    /**
     * Unregisters a listener for messages containing the given field values.
     *
     * @param values field values of interest, in one-to-one correspondence with the keys
     * @param listener listener to unregister
     */
    public void unregister(List<String> values, BiConsumer<String, StandardCoderObject> listener) {
        logger.info("unregister topic listener for key={} value={}", keys, values);

        values2listeners.computeIfPresent(values, (key, listeners) -> {
            listeners.remove(listener);
            return (listeners.isEmpty() ? null : listeners);
        });
    }

    /**
     * Processes a message, forwarding it to the appropriate listeners, if any.
     *
     * @param textMessage original text message that was received
     * @param scoMessage decoded text message
     */
    public void onMessage(String textMessage, StandardCoderObject scoMessage) {
        // extract the key values from the message
        List<String> values = new ArrayList<>(keys.size());
        for (SelectorKey key : keys) {
            String value = key.extractField(scoMessage);
            if (value == null) {
                /*
                 * No value for this field, so this message is not relevant to this
                 * forwarder.
                 */
                logger.info("message has no key={}", keys);
                return;
            }

            values.add(value);
        }

        // get the listeners for this set of values
        Map<BiConsumer<String, StandardCoderObject>, String> listeners = values2listeners.get(values);
        if (listeners == null) {
            // no listeners for this particular list of values
            logger.info("no listener registered for key={} value={}", keys, values);
            return;
        }


        // forward the message to each listener
        logger.info("forwarding message to listeners for key={} value={}", keys, values);
        for (BiConsumer<String, StandardCoderObject> listener : listeners.keySet()) {
            try {
                listener.accept(textMessage, scoMessage);
            } catch (RuntimeException e) {
                logger.warn("exception thrown by listener {}", Util.ident(listener), e);
            }
        }
    }
}
