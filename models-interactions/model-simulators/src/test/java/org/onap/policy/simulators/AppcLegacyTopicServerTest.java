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

package org.onap.policy.simulators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.utils.resources.ResourceUtils;

public class AppcLegacyTopicServerTest {
    private static final String MY_TOPIC = "my-topic";

    @Mock
    private TopicSink sink;
    @Mock
    private TopicSource source;

    private AppcLegacyTopicServer server;

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        server = new AppcLegacyTopicServer(sink, source);
    }

    @Test
    public void testProcessAppcLcmDmaapWrapper() {
        String request = ResourceUtils.getResourceAsString("org/onap/policy/simulators/appc/appc.legacy.request.json");
        assertNotNull(request);

        server.onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, request);

        ArgumentCaptor<String> respCaptor = ArgumentCaptor.forClass(String.class);
        verify(sink).send(respCaptor.capture());

        assertThat(respCaptor.getValue()).contains("111be3d2").doesNotContain("replaceMe");
    }
}
