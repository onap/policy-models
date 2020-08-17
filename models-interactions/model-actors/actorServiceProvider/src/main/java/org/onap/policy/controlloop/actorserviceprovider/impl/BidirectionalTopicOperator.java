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
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicManager;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;

/**
 * Operator that uses a bidirectional topic. Topic operators may share a
 * {@link BidirectionalTopicHandler}.
 */
public class BidirectionalTopicOperator
                extends TypedOperator<BidirectionalTopicConfig, BidirectionalTopicOperation<?, ?>> {

    /**
     * Manager from which to get the topic handlers.
     */
    private final BidirectionalTopicManager topicManager;

    /**
     * Keys used to extract the fields used to select responses for this operator.
     */
    private final List<SelectorKey> selectorKeys;


    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     * @param propertyNames names of properties required by this operation
     * @param topicManager manager from which to get the topic handler
     * @param selectorKeys keys used to extract the fields used to select responses for
     *        this operator
     */
    protected BidirectionalTopicOperator(String actorName, String name, List<String> propertyNames,
                    BidirectionalTopicManager topicManager, List<SelectorKey> selectorKeys) {
        this(actorName, name, propertyNames, topicManager, selectorKeys, null);
    }

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     * @param propertyNames names of properties required by this operation
     * @param topicManager manager from which to get the topic handler
     * @param selectorKeys keys used to extract the fields used to select responses for
     *        this operator
     */
    public BidirectionalTopicOperator(String actorName, String name, List<String> propertyNames,
                    BidirectionalTopicManager topicManager, List<SelectorKey> selectorKeys,
                    OperationMaker<BidirectionalTopicConfig, BidirectionalTopicOperation<?, ?>> operationMaker) {

        super(actorName, name, propertyNames, operationMaker);
        this.topicManager = topicManager;
        this.selectorKeys = selectorKeys;
    }

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     * @param propertyNames names of properties required by this operation
     * @param topicManager manager from which to get the topic handler
     * @param selectorKeys keys used to extract the fields used to select responses for
     *        this operator
     */
    public BidirectionalTopicOperator(String actorName, String name, List<String> propertyNames,
                    BidirectionalTopicManager topicManager,
                    OperationMaker<BidirectionalTopicConfig, BidirectionalTopicOperation<?, ?>> operationMaker,
                    SelectorKey... selectorKeys) {
        this(actorName, name, propertyNames, topicManager, Arrays.asList(selectorKeys), operationMaker);
    }

    /**
     * Makes a new configuration using the specified parameters.
     *
     * @param parameters operator parameters
     * @return a new configuration
     */
    protected BidirectionalTopicConfig makeConfiguration(Map<String, Object> parameters) {
        BidirectionalTopicParams params = Util.translate(getFullName(), parameters, BidirectionalTopicParams.class);
        ValidationResult result = params.validate(getFullName());
        if (!result.isValid()) {
            throw new ParameterValidationRuntimeException("invalid parameters", result);
        }

        return new BidirectionalTopicConfig(getBlockingExecutor(), params, topicManager, selectorKeys);
    }
}
