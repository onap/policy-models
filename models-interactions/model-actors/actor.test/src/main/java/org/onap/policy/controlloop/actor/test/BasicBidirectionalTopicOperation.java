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

package org.onap.policy.controlloop.actor.test;

import static org.mockito.Mockito.when;

import java.util.function.BiConsumer;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.common.utils.time.PseudoExecutor;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;

/**
 * Superclass for various BidirectionalTopicOperation tests.
 */
public class BasicBidirectionalTopicOperation extends BasicOperation {
    protected static final String MY_SINK = "my-sink";
    protected static final String MY_SOURCE = "my-source";
    protected static final int TIMEOUT = 10;

    @Captor
    protected ArgumentCaptor<BiConsumer<String, StandardCoderObject>> listenerCaptor;

    @Mock
    protected BidirectionalTopicHandler topicHandler;
    @Mock
    protected Forwarder forwarder;
    @Mock
    protected BidirectionalTopicOperator operator;

    protected BidirectionalTopicParams topicParams;

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicBidirectionalTopicOperation() {
        super();
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicBidirectionalTopicOperation(String actor, String operation) {
        super(actor, operation);
    }

    /**
     * Initializes mocks and sets up.
     */
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        executor = new PseudoExecutor();

        makeContext();

        outcome = params.makeOutcome();
        topicParams = BidirectionalTopicParams.builder().sinkTopic(MY_SINK).sourceTopic(MY_SOURCE).timeoutSec(TIMEOUT)
                        .build();

        initOperator();
    }

    /**
     * Initializes an operator so that it is "alive" and has the given names.
     */
    protected void initOperator() {
        when(operator.isAlive()).thenReturn(true);
        when(operator.getFullName()).thenReturn(actorName + "." + operationName);
        when(operator.getActorName()).thenReturn(actorName);
        when(operator.getName()).thenReturn(operationName);
        when(operator.getTopicHandler()).thenReturn(topicHandler);
        when(operator.getForwarder()).thenReturn(forwarder);
        when(operator.getParams()).thenReturn(topicParams);
    }

    /**
     * Provides a response to the topic {@link #listenerCaptor}.
     *
     * @param listener listener to which to provide the response
     * @param response response to be provided
     */
    protected void provideResponse(BiConsumer<String, StandardCoderObject> listener, String response) {
        try {
            StandardCoderObject sco = coder.decode(response, StandardCoderObject.class);
            listener.accept(response, sco);

        } catch (CoderException e) {
            throw new IllegalArgumentException("response is not a Map", e);
        }
    }
}
