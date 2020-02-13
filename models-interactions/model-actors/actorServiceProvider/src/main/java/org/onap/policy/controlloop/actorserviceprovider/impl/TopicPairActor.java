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

package org.onap.policy.controlloop.actorserviceprovider.impl;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpActorParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.actorserviceprovider.parameters.TopicPairActorParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.TopicPairParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.TopicPair;
import org.onap.policy.controlloop.actorserviceprovider.topic.TopicPairManager;

/**
 * Actor that uses a topic pair. The actor's parameters must be a {@link TopicPairParams},
 * which is shared by all of its operators.
 */
public class TopicPairActor extends ActorImpl implements TopicPairManager {

    /**
     * Maps a topic source and target name to its topic pair.
     */
    private final Map<Pair<String, String>, TopicPair> params2topic = new ConcurrentHashMap<>();


    /**
     * Constructs the object.
     *
     * @param name actor's name
     */
    public TopicPairActor(String name) {
        super(name);
    }

    @Override
    protected void doStart() {
        params2topic.values().forEach(TopicPair::start);
        super.doStart();
    }

    @Override
    protected void doStop() {
        params2topic.values().forEach(TopicPair::stop);
        super.doStop();
    }

    @Override
    protected void doShutdown() {
        params2topic.values().forEach(TopicPair::shutdown);
        params2topic.clear();
        super.doShutdown();
    }

    @Override
    public TopicPair getTopicPair(String source, String target) {
        Pair<String, String> key = Pair.of(source, target);
        return params2topic.computeIfAbsent(key, pair -> new TopicPair(source, target));
    }

    /**
     * Translates the parameters to an {@link HttpActorParams} and then creates a function
     * that will extract operator-specific parameters.
     */
    @Override
    protected Function<String, Map<String, Object>> makeOperatorParameters(Map<String, Object> actorParameters) {
        String actorName = getName();

        TopicPairActorParams params =  Util.translate(actorName, actorParameters, TopicPairActorParams.class);
        ValidationResult result = params.validate(getName());
        if (!result.isValid()) {
            throw new ParameterValidationRuntimeException("invalid parameters", result);
        }

        // create a map of the default parameters
        Map<String, Object> defaultParams = Util.translateToMap(getName(), params.getDefaults());
        Map<String, Map<String, Object>> operations = params.getOperation();

        return operationName -> {
            Map<String, Object> specificParams = operations.get(operationName);
            if (specificParams == null) {
                return null;
            }

            // start with a copy of defaults and overlay with specific
            Map<String, Object> subparams = new TreeMap<>(defaultParams);
            subparams.putAll(specificParams);

            return Util.translateToMap(getName() + "." + operationName, subparams);
        };
    }
}
