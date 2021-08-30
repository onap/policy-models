/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
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

package org.onap.policy.models.sim.dmaap.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import javax.ws.rs.core.Response;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.sim.dmaap.parameters.DmaapSimParameterGroup;
import org.onap.policy.models.sim.dmaap.provider.DmaapSimProvider;

public class DmaapSimRestControllerV1Test {
    private static final int LIMIT = 5;
    private static final String TOPIC = "my-topic";
    private static final String TOPIC2 = "my-topic-B";
    private static final String MESSAGE = "my-message";
    private static final String MESSAGE2 = "my-message-B";
    private static final String CONSUMER = "my-consumer";
    private static final String CONSUMER_ID = "my-id";

    private static Coder coder = new StandardCoder();

    private DmaapSimRestControllerV1 rest;

    /**
     * Creates the controller.
     *
     * @throws CoderException if the parameters cannot be loaded
     */
    @Before
    public void setUp() throws CoderException {
        DmaapSimParameterGroup params = coder.decode(new File("src/test/resources/parameters/NormalParameters.json"),
                        DmaapSimParameterGroup.class);
        DmaapSimProvider.setInstance(new DmaapSimProvider(params));
        rest = new DmaapSimRestControllerV1();
    }

    @Test
    public void test() {
        Response resp = rest.getDmaapMessage(TOPIC, CONSUMER, CONSUMER_ID, LIMIT, 0);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        assertEquals("[]", resp.getEntity().toString());

        // add some messages
        resp = rest.postDmaapMessage(TOPIC, Arrays.asList(MESSAGE, MESSAGE2));
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        assertEquals(2, getCount(resp));

        resp = rest.postDmaapMessage(TOPIC2, Arrays.asList(MESSAGE, MESSAGE2, MESSAGE));
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        assertEquals(3, getCount(resp));

        // hadn't registered with topic 2 so nothing expected from there
        resp = rest.getDmaapMessage(TOPIC2, CONSUMER, CONSUMER_ID, LIMIT, 0);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        assertEquals("[]", resp.getEntity().toString());

        // now read from topic 1
        resp = rest.getDmaapMessage(TOPIC, CONSUMER, CONSUMER_ID, LIMIT, 0);
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        assertEquals("[my-message, my-message-B]", resp.getEntity().toString());

        // verify getDmaapTopics
        resp = rest.getDmaapTopics();
        assertEquals(Response.Status.OK.getStatusCode(), resp.getStatus());
        assertThat(resp.getEntity().toString()).contains("POLICY-PDP-PAP");
    }

    private int getCount(Response resp) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) resp.getEntity();

        return (int) map.get("count");
    }

}
