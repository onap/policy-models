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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.Getter;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Operation;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.actorserviceprovider.parameters.TopicPairParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;
import org.onap.policy.controlloop.actorserviceprovider.topic.TopicPair;
import org.onap.policy.controlloop.actorserviceprovider.topic.TopicPairManager;

/**
 * Operator that uses a pair of topics, one for publishing the request, and another for
 * receiving the response. Topic operators may share a {@link TopicPair}.
 */
public abstract class TopicPairOperator extends OperatorPartial {

    /**
     * Manager from which to get the topic pair.
     */
    private final TopicPairManager pairManager;

    /**
     * Keys used to extract the fields used to select responses for this operator.
     */
    private final List<SelectorKey> selectorKeys;

    /*
     * The remaining fields are initialized when configure() is invoked, thus they may
     * change.
     */

    /**
     * Current parameters. While {@link params} may change, the values contained within it
     * will not, thus operations may copy it.
     */
    @Getter
    private TopicPairParams params;

    /**
     * Topic pair associated with the parameters.
     */
    @Getter
    private TopicPair topicPair;

    /**
     * Forwarder associated with the parameters.
     */
    @Getter
    private Forwarder forwarder;


    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     * @param pairManager manager from which to get the topic pair
     * @param selectorKeys keys used to extract the fields used to select responses for
     *        this operator
     */
    public TopicPairOperator(String actorName, String name, TopicPairManager pairManager,
                    List<SelectorKey> selectorKeys) {
        super(actorName, name);
        this.pairManager = pairManager;
        this.selectorKeys = selectorKeys;
    }

    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        params = Util.translate(getFullName(), parameters, TopicPairParams.class);
        ValidationResult result = params.validate(getFullName());
        if (!result.isValid()) {
            throw new ParameterValidationRuntimeException("invalid parameters", result);
        }

        topicPair = pairManager.getTopicPair(params.getSource(), params.getTarget());
        forwarder = topicPair.addForwarder(selectorKeys);
    }

    /**
     * Makes an operator that will construct operations.
     *
     * @param <Q> request type
     * @param <S> response type
     * @param actorName actor name
     * @param operation operation name
     * @param pairManager manager from which to get the topic pair
     * @param operationMaker function to make an operation
     * @param keys keys used to extract the fields used to select responses for this
     *        operator
     * @return a new operator
     */
    // @formatter:off
    public static <Q,S> TopicPairOperator makeOperator(String actorName, String operation, TopicPairManager pairManager,
                    BiFunction<ControlLoopOperationParams, TopicPairOperator, TopicPairOperation<Q,S>> operationMaker,
                    SelectorKey... keys) {
        // @formatter:off

        return makeOperator(actorName, operation, pairManager, Arrays.asList(keys), operationMaker);
    }

    /**
     * Makes an operator that will construct operations.
     *
     * @param <Q> request type
     * @param <S> response type
     * @param actorName actor name
     * @param operation operation name
     * @param pairManager manager from which to get the topic pair
     * @param keys keys used to extract the fields used to select responses for
     *        this operator
     * @param operationMaker function to make an operation
     * @return a new operator
     */
    // @formatter:off
    public static <Q,S> TopicPairOperator makeOperator(String actorName, String operation, TopicPairManager pairManager,
                    List<SelectorKey> keys,
                    BiFunction<ControlLoopOperationParams, TopicPairOperator, TopicPairOperation<Q,S>> operationMaker) {
        // @formatter:on

        return new TopicPairOperator(actorName, operation, pairManager, keys) {
            @Override
            public synchronized Operation buildOperation(ControlLoopOperationParams params) {
                return operationMaker.apply(params, this);
            }
        };
    }
}
