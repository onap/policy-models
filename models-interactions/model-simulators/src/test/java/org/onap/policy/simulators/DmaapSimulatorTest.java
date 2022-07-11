/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.simulators;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.bus.DmaapTopicSink;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.endpoints.parameters.TopicParameterGroup;
import org.onap.policy.common.utils.coder.StandardCoder;

public class DmaapSimulatorTest {
    private static final int MAX_WAIT_SEC = 5;
    private static final String TOPIC = "MY-TOPIC";

    /**
     * Messages from the topic are placed here by the endpoint.
     */
    private BlockingQueue<String> queue;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TopicEndpointManager.getManager().shutdown();
    }

    /**
     * Starts the simulator and the topic.
     *
     * @throws Exception if an error occurs
     */
    @Before
    public void setUp() throws Exception {
        assertNotNull(Util.buildDmaapSim());

        String topicJson = new String(Files.readAllBytes(
                        new File("src/test/resources/org/onap/policy/simulators/dmaap/TopicParameters.json").toPath()),
                        StandardCharsets.UTF_8);
        topicJson = topicJson.replace("${port}", String.valueOf(Util.DMAAPSIM_SERVER_PORT));

        TopicParameterGroup topicConfig = new StandardCoder().decode(topicJson, TopicParameterGroup.class);

        TopicEndpointManager.getManager().addTopics(topicConfig);
        TopicEndpointManager.getManager().start();

        queue = new LinkedBlockingQueue<>();
    }

    @After
    public void tearDown() {
        TopicEndpointManager.getManager().shutdown();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    @Test
    public void test() throws InterruptedException {
        TopicEndpointManager.getManager().getDmaapTopicSource(TOPIC)
                        .register((infra, topic, event) -> queue.add(event));

        DmaapTopicSink sink = TopicEndpointManager.getManager().getDmaapTopicSink(TOPIC);
        sink.send("hello");
        sink.send("world");

        assertEquals("hello", queue.poll(MAX_WAIT_SEC, TimeUnit.SECONDS));
        assertEquals("world", queue.poll(MAX_WAIT_SEC, TimeUnit.SECONDS));
    }
}
