/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023-2024 Nordix Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.lenient;

import java.util.List;
import java.util.function.BiConsumer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.endpoints.event.comm.client.BidirectionalTopicClientException;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.endpoints.parameters.TopicParameters;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicHandler;
import org.onap.policy.controlloop.actorserviceprovider.topic.BidirectionalTopicManager;
import org.onap.policy.controlloop.actorserviceprovider.topic.Forwarder;
import org.onap.policy.simulators.TopicServer;

/**
 * Superclass for various BidirectionalTopicOperation tests.
 *
 * @param <Q> request type
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BasicBidirectionalTopicOperation<Q> extends BasicOperation {
    protected static final String MY_SINK = "my-sink";
    protected static final String MY_SOURCE = "my-source";
    protected static final int TIMEOUT_SEC = 10;
    protected static final long TIMEOUT_MS = 1000L * TIMEOUT_SEC;

    // sink and source used by the TopicServer
    private static TopicSink serverSink;
    private static TopicSource serverSource;
    private static BidirectionalTopicHandler realTopicHandler;

    protected static BidirectionalTopicManager topicMgr = (sink, source) -> {
        // note: the sink and source names are swapped for the simulator
        assertEquals(serverSource.getTopic(), sink);
        assertEquals(serverSink.getTopic(), source);
        return realTopicHandler;
    };

    @Captor
    protected ArgumentCaptor<BiConsumer<String, StandardCoderObject>> listenerCaptor;

    @Mock
    protected BidirectionalTopicHandler topicHandler;
    @Mock
    protected Forwarder forwarder;
    @Mock
    protected BidirectionalTopicConfig config;

    private TopicServer<Q> topicServer;


    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    protected BasicBidirectionalTopicOperation(String actor, String operation) {
        super(actor, operation);
    }

    /**
     * Starts the topic.
     *
     * @throws BidirectionalTopicClientException if the client cannot be built
     */
    protected static void initBeforeClass(String sinkTopic, String sourceTopic)
                    throws BidirectionalTopicClientException {

        // note: the sink and source names are swapped for the simulator
        var ptopic = new TopicParameters();
        ptopic.setTopic(sourceTopic);
        ptopic.setManaged(true);
        ptopic.setServers(List.of("localhost"));
        ptopic.setTopicCommInfrastructure("NOOP");
        ptopic.setFetchTimeout(500);
        serverSink = TopicEndpointManager.getManager().addTopicSinks(List.of(ptopic)).get(0);

        ptopic.setTopic(sinkTopic);
        serverSource = TopicEndpointManager.getManager().addTopicSources(List.of(ptopic)).get(0);

        serverSink.start();
        serverSource.start();

        if (!sinkTopic.equals(sourceTopic)) {
            // sink and source are different - create other ends for the actor
            initActorTopics(sinkTopic, sourceTopic, ptopic);
        }

        realTopicHandler = new BidirectionalTopicHandler(sinkTopic, sourceTopic);
        realTopicHandler.start();
    }

    private static void initActorTopics(String sinkTopic, String sourceTopic, TopicParameters ptopic) {
        // create sink and source for the actor, too
        ptopic.setTopic(sinkTopic);
        TopicEndpointManager.getManager().addTopicSinks(List.of(ptopic)).get(0).start();

        ptopic.setTopic(sourceTopic);
        TopicEndpointManager.getManager().addTopicSources(List.of(ptopic)).get(0).start();
    }

    protected static void destroyAfterClass() {
        TopicEndpointManager.getManager().shutdown();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
        HttpClientFactoryInstance.getClientFactory().destroy();
    }

    /**
     * Initializes mocks and sets up.
     */
    @Override
    public void setUpBasic() {
        super.setUpBasic();
        topicServer = makeServer(serverSink, serverSource);
        initConfig();
    }

    /**
     * Finish all topic servers and mocks.
     */
    public void tearDownBasic() {
        topicServer.shutdown();
        try {
            closeable.close();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Makes a simulator for the given sink and source.
     *
     * @param sink topic to which the simulator should publish responses
     * @param source topic from which the simulator should receive messages
     * @return a new topic server/simulator
     */
    protected abstract TopicServer<Q> makeServer(TopicSink sink, TopicSource source);

    /**
     * Initializes a configuration.
     */
    protected void initConfig() {
        lenient().when(config.getTopicHandler()).thenReturn(topicHandler);
        lenient().when(config.getForwarder()).thenReturn(forwarder);
        lenient().when(config.getTimeoutMs()).thenReturn(TIMEOUT_MS);
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
