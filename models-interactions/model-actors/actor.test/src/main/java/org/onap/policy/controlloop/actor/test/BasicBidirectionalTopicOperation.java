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
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;

/**
 * Superclass for various BidirectionalTopicOperation tests.
 */
public class BasicBidirectionalTopicOperation extends BasicOperation {
    protected static final String MY_SINK = "my-sink";
    protected static final String MY_SOURCE = "my-source";
    protected static final int TIMEOUT_SEC = 10;
    protected static final long TIMEOUT_MS = 1000L * TIMEOUT_SEC;

    @Captor
    protected ArgumentCaptor<BiConsumer<String, StandardCoderObject>> listenerCaptor;

    @Mock
    protected BidirectionalTopicHandler topicHandler;
    @Mock
    protected Forwarder forwarder;
    @Mock
    protected BidirectionalTopicConfig config;

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
    @Override
    public void setUpBasic() {
        super.setUpBasic();
        initConfig();
    }

    /**
     * Initializes a configuration.
     */
    protected void initConfig() {
        when(config.getTopicHandler()).thenReturn(topicHandler);
        when(config.getForwarder()).thenReturn(forwarder);
        when(config.getTimeoutMs()).thenReturn(TIMEOUT_MS);
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
