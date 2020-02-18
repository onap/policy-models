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

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.common.utils.time.PseudoExecutor;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.ActorService;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;

/**
 * Superclass for various BidirectionalTopicOperation tests.
 */
public class BasicBidirectionalTopicOperation {
    protected static final UUID REQ_ID = UUID.randomUUID();
    protected static final String DEFAULT_ACTOR = "default-actor";
    protected static final String DEFAULT_OPERATION = "default-operation";
    protected static final String MY_SINK = "my-sink";
    protected static final String MY_SOURCE = "my-source";
    protected static final String TARGET_ENTITY = "my-target";
    protected static final Coder coder = new StandardCoder();
    protected static final int TIMEOUT = 10;

    protected final String actorName;
    protected final String operationName;

    @Captor
    protected ArgumentCaptor<BiConsumer<String, StandardCoderObject>> listenerCaptor;

    @Mock
    protected ActorService service;
    @Mock
    protected BidirectionalTopicHandler topicHandler;
    @Mock
    protected Forwarder forwarder;
    @Mock
    protected BidirectionalTopicOperator operator;

    protected BidirectionalTopicParams topicParams;
    protected ControlLoopOperationParams params;
    protected Map<String, String> enrichment;
    protected VirtualControlLoopEvent event;
    protected ControlLoopEventContext context;
    protected OperationOutcome outcome;
    protected PseudoExecutor executor;

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicBidirectionalTopicOperation() {
        this.actorName = DEFAULT_ACTOR;
        this.operationName = DEFAULT_OPERATION;
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicBidirectionalTopicOperation(String actor, String operation) {
        this.actorName = actor;
        this.operationName = operation;
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
     * Reinitializes {@link #enrichment}, {@link #event}, {@link #context}, and
     * {@link #params}.
     * <p/>
     * Note: {@link #params} is configured to use {@link #executor}.
     */
    protected void makeContext() {
        enrichment = new TreeMap<>(makeEnrichment());

        event = new VirtualControlLoopEvent();
        event.setRequestId(REQ_ID);
        event.setAai(enrichment);

        context = new ControlLoopEventContext(event);

        params = ControlLoopOperationParams.builder().executor(executor).context(context).actorService(service)
                        .actor(actorName).operation(operationName).targetEntity(TARGET_ENTITY).payload(makePayload())
                        .build();
    }

    protected Map<String, String> makePayload() {
        return null;
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
     * Makes enrichment data.
     *
     * @return enrichment data
     */
    protected Map<String, String> makeEnrichment() {
        return new TreeMap<>();
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
