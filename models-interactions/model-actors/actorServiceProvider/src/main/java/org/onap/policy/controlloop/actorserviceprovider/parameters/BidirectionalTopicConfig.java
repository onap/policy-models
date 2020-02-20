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

package org.onap.policy.controlloop.actorserviceprovider.parameters;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicManager;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;
import org.onap.policy.controlloop.actorserviceprovider.topic.SelectorKey;

/**
 * Configuration for Bidirectional Topic Operators.
 */
@Getter
public class BidirectionalTopicConfig extends OperatorConfig {

    /**
     * Topic handler associated with the parameters.
     */
    private BidirectionalTopicHandler topicHandler;

    /**
     * Forwarder associated with the parameters.
     */
    private Forwarder forwarder;

    /**
     * Default timeout, in milliseconds, if none specified in the request.
     */
    private final long timeoutMs;


    /**
     * Constructs the object.
     *
     * @param blockingExecutor executor to be used for tasks that may perform blocking I/O
     * @param params operator parameters
     * @param topicManager manager from which to get the topic handler
     * @param selectorKeys keys used to extract the fields used to select responses for
     *        this operator
     */
    public BidirectionalTopicConfig(Executor blockingExecutor, BidirectionalTopicParams params,
                    BidirectionalTopicManager topicManager, List<SelectorKey> selectorKeys) {
        super(blockingExecutor);
        topicHandler = topicManager.getTopicHandler(params.getSinkTopic(), params.getSourceTopic());
        forwarder = topicHandler.addForwarder(selectorKeys);
        timeoutMs = TimeUnit.MILLISECONDS.convert(params.getTimeoutSec(), TimeUnit.SECONDS);
    }
}
