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

package org.onap.policy.sim.dmaap.e2e;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.common.endpoints.event.comm.TopicEndpointManager;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.parameters.TopicParameterGroup;
import org.onap.policy.common.endpoints.parameters.TopicParameters;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.sim.dmaap.rest.CommonRestServer;

/**
 * This tests the simulator using dmaap endpoints to verify that it works from publisher
 * to subscriber.
 */
public class EndToEndTest extends CommonRestServer {
    private static final int MAX_WAIT_SEC = 5;
    private static final String ORIG_TOPIC = "MY-TOPIC";
    private static final String ORIG_TOPIC2 = "MY-TOPIC-B";
    private static final int MAX_MSG = 200;

    private static int ntests = 0;
    private static String topicJson;

    private TopicParameterGroup topicConfig;

    private String topic = "MY-TOPIC";
    private String topic2 = "MY-TOPIC-B";

    /**
     * Messages from the topic are placed here by the endpoint.
     */
    private BlockingQueue<String> queue;

    /**
     * Messages from topic 2 are placed here by the endpoint.
     */
    private BlockingQueue<String> queue2;

    /**
     * Starts the rest server.
     *
     * @throws Exception if an error occurs
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TopicEndpointManager.getManager().shutdown();

        CommonRestServer.setUpBeforeClass();

        topicJson = new String(
                        Files.readAllBytes(new File("src/test/resources/parameters/TopicParameters.json").toPath()),
                        StandardCharsets.UTF_8);
        topicJson = topicJson.replace("${port}", String.valueOf(getPort()));
    }

    /**
     * Starts the topics.
     *
     * @throws CoderException if the parameters cannot be decoded
     */
    @Before
    @Override
    public void setUp() throws CoderException {
        queue = new LinkedBlockingQueue<>();
        queue2 = new LinkedBlockingQueue<>();

        /*
         * change topic names for each tests so any listeners still hanging out on the old
         * topics will not put the messages into the new queues
         */

        ++ntests;
        topic = "my-topic-" + ntests;
        topic2 = "my-topic-b" + ntests;

        String json = topicJson.replace(ORIG_TOPIC2, topic2).replace(ORIG_TOPIC, topic);

        topicConfig = new StandardCoder().decode(json, TopicParameterGroup.class);

        TopicEndpointManager.getManager().addTopics(topicConfig);
        TopicEndpointManager.getManager().start();
    }

    @After
    public void tearDown() {
        TopicEndpointManager.getManager().shutdown();
    }

    @Test
    public void testWithTopicEndpointAtEachEnd() throws InterruptedException {
        // register listeners to add events to appropriate queue
        TopicEndpointManager.getManager().getDmaapTopicSource(topic)
                        .register((infra, topic, event) -> queue.add(event));
        TopicEndpointManager.getManager().getDmaapTopicSource(topic2)
                        .register((infra, topic, event) -> queue2.add(event));

        // publish events
        TopicSink sink = TopicEndpointManager.getManager().getDmaapTopicSink(topic);
        TopicSink sink2 = TopicEndpointManager.getManager().getDmaapTopicSink(topic2);
        for (int x = 0; x < MAX_MSG; ++x) {
            sink.send("hello-" + x);
            sink2.send("world-" + x);
        }

        // verify events where received
        for (int x = 0; x < MAX_MSG; ++x) {
            assertEquals("message " + x, "hello-" + x, queue.poll(MAX_WAIT_SEC, TimeUnit.SECONDS));
            assertEquals("message " + x, "world-" + x, queue2.poll(MAX_WAIT_SEC, TimeUnit.SECONDS));
        }
    }

    @Test
    public void testCambriaFormat() throws Exception {
        test("testCambriaFormat", "application/cambria",
            (wtr, messages) -> messages.forEach(msg -> wtr.write("0." + msg.length() + "." + msg + "\n")));
    }

    @Test
    public void testJson() throws Exception {
        test("testJson", "application/json", (wtr, messages) -> wtr.write("[" + String.join(", ", messages) + "]"));
    }

    @Test
    public void testText() throws Exception {
        test("testText", "text/plain", (wtr, messages) -> messages.forEach(wtr::println));
    }

    /**
     * Uses a raw URL connection to ensure the server can process messages of the given
     * media type.
     *
     * @param testName name of the test
     * @param mediaType media type
     * @param writeMessages function that writes messages to a PrintWriter
     * @throws Exception if an error occurs
     */
    private void test(String testName, String mediaType, BiConsumer<PrintWriter, List<String>> writeMessages)
                    throws Exception {
        String msg1 = "{'abc':10.0}".replace('\'', '"');
        String msg2 = "{'def':20.0}".replace('\'', '"');

        TopicEndpointManager.getManager().getDmaapTopicSource(topic)
                        .register((infra, topic, event) -> queue.add(event));

        TopicParameters sinkcfg = topicConfig.getTopicSinks().get(0);
        URL url = new URL(httpPrefix + "events/" + sinkcfg.getTopic());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-type", mediaType);
        conn.setDoOutput(true);
        conn.connect();

        try (PrintWriter wtr = new PrintWriter(conn.getOutputStream())) {
            writeMessages.accept(wtr, Arrays.asList(msg1, msg2));
        }

        assertEquals(testName + " response code", HttpURLConnection.HTTP_OK, conn.getResponseCode());

        assertEquals(testName + " message 1", msg1, queue.poll(MAX_WAIT_SEC, TimeUnit.SECONDS));
        assertEquals(testName + " message 2", msg2, queue.poll(MAX_WAIT_SEC, TimeUnit.SECONDS));
    }
}
