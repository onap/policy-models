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
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicManager;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;

/**
 * Operator that uses a bidirectional topic. Topic operators may share a
 * {@link BidirectionalTopicHandler}.
 */
public abstract class BidirectionalTopicOperator extends OperatorPartial {

    /**
     * Manager from which to get the topic handlers.
     */
    private final BidirectionalTopicManager topicManager;

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
    private BidirectionalTopicParams params;

    /**
     * Topic handler associated with the parameters.
     */
    @Getter
    private BidirectionalTopicHandler topicHandler;

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
     * @param topicManager manager from which to get the topic handler
     * @param selectorKeys keys used to extract the fields used to select responses for
     *        this operator
     */
    public BidirectionalTopicOperator(String actorName, String name, BidirectionalTopicManager topicManager,
                    List<SelectorKey> selectorKeys) {
        super(actorName, name);
        this.topicManager = topicManager;
        this.selectorKeys = selectorKeys;
    }

    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        params = Util.translate(getFullName(), parameters, BidirectionalTopicParams.class);
        ValidationResult result = params.validate(getFullName());
        if (!result.isValid()) {
            throw new ParameterValidationRuntimeException("invalid parameters", result);
        }

        topicHandler = topicManager.getTopicHandler(params.getSinkTopic(), params.getSourceTopic());
        forwarder = topicHandler.addForwarder(selectorKeys);
    }

    /**
     * Makes an operator that will construct operations.
     *
     * @param <Q> request type
     * @param <S> response type
     * @param actorName actor name
     * @param operation operation name
     * @param topicManager manager from which to get the topic handler
     * @param operationMaker function to make an operation
     * @param keys keys used to extract the fields used to select responses for this
     *        operator
     * @return a new operator
     */
    // @formatter:off
    public static <Q, S> BidirectionalTopicOperator makeOperator(String actorName, String operation,
                    BidirectionalTopicManager topicManager,
                    BiFunction<ControlLoopOperationParams, BidirectionalTopicOperator,
                        BidirectionalTopicOperation<Q, S>> operationMaker,
                    SelectorKey... keys) {
        // @formatter:off

        return makeOperator(actorName, operation, topicManager, Arrays.asList(keys), operationMaker);
    }

    /**
     * Makes an operator that will construct operations.
     *
     * @param <Q> request type
     * @param <S> response type
     * @param actorName actor name
     * @param operation operation name
     * @param topicManager manager from which to get the topic handler
     * @param keys keys used to extract the fields used to select responses for
     *        this operator
     * @param operationMaker function to make an operation
     * @return a new operator
     */
    // @formatter:off
    public static <Q,S> BidirectionalTopicOperator makeOperator(String actorName, String operation,
                    BidirectionalTopicManager topicManager,
                    List<SelectorKey> keys,
                    BiFunction<ControlLoopOperationParams, BidirectionalTopicOperator,
                        BidirectionalTopicOperation<Q,S>> operationMaker) {
        // @formatter:on

        return new BidirectionalTopicOperator(actorName, operation, topicManager, keys) {
            @Override
            public synchronized Operation buildOperation(ControlLoopOperationParams params) {
                return operationMaker.apply(params, this);
            }
        };
    }
}
