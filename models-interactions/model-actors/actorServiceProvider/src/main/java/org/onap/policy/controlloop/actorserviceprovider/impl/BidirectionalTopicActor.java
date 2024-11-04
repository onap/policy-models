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

package org.onap.policy.controlloop.actorserviceprovider.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.common.message.bus.event.client.BidirectionalTopicClientException;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicActorParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicManager;

/**
 * Actor that uses a bidirectional topic. The actor's operator parameters are expected to
 * be an {@link BidirectionalTopicParams}.
 *
 * @param <P> type of parameters
 */
public class BidirectionalTopicActor<P extends BidirectionalTopicActorParams> extends ActorImpl
                implements BidirectionalTopicManager {

    /**
     * Class of parameters.
     */
    private final Class<P> paramsClass;

    /**
     * Maps a pair of sink and source topic names to their bidirectional topic.
     */
    private final Map<Pair<String, String>, BidirectionalTopicHandler> params2topic = new ConcurrentHashMap<>();


    /**
     * Constructs the object.
     *
     * @param name actor's name
     */
    public BidirectionalTopicActor(String name, Class<P> paramsClass) {
        super(name);
        this.paramsClass = paramsClass;
    }

    @Override
    protected void doStart() {
        params2topic.values().forEach(BidirectionalTopicHandler::start);
        super.doStart();
    }

    @Override
    protected void doStop() {
        params2topic.values().forEach(BidirectionalTopicHandler::stop);
        super.doStop();
    }

    @Override
    protected void doShutdown() {
        params2topic.values().forEach(BidirectionalTopicHandler::shutdown);
        params2topic.clear();
        super.doShutdown();
    }

    @Override
    public BidirectionalTopicHandler getTopicHandler(String sinkTopic, String sourceTopic) {
        Pair<String, String> key = Pair.of(sinkTopic, sourceTopic);

        return params2topic.computeIfAbsent(key, pair -> {
            try {
                return makeTopicHandler(sinkTopic, sourceTopic);
            } catch (BidirectionalTopicClientException e) {
                throw new IllegalArgumentException(e);
            }
        });
    }

    /**
     * Translates the parameters to a {@link BidirectionalTopicActorParams} and then
     * creates a function that will extract operator-specific parameters.
     */
    @Override
    protected Function<String, Map<String, Object>> makeOperatorParameters(Map<String, Object> actorParameters) {
        String actorName = getName();

        // @formatter:off
        return Util.translate(actorName, actorParameters, paramsClass)
                        .doValidation(actorName)
                        .makeOperationParameters(actorName);
        // @formatter:on
    }

    // may be overridden by junit tests

    protected BidirectionalTopicHandler makeTopicHandler(String sinkTopic, String sourceTopic)
                    throws BidirectionalTopicClientException {

        return new BidirectionalTopicHandler(sinkTopic, sourceTopic);
    }
}
