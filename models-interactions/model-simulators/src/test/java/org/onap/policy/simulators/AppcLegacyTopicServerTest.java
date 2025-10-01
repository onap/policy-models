/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.common.message.bus.event.Topic.CommInfrastructure;
import org.onap.policy.common.message.bus.event.TopicSink;
import org.onap.policy.common.message.bus.event.TopicSource;
import org.onap.policy.common.utils.resources.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class AppcLegacyTopicServerTest {
    private static final String MY_TOPIC = "my-topic";

    @Mock
    private TopicSink sink;
    @Mock
    private TopicSource source;

    private AppcLegacyTopicServer server;

    /**
     * Sets up.
     */
    @BeforeEach
    void setUp() {
        server = new AppcLegacyTopicServer(sink, source);
    }

    @Test
    void testProcess() {
        String request = ResourceUtils.getResourceAsString("org/onap/policy/simulators/appc/appc.legacy.request.json");
        assertNotNull(request);

        server.onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, request);

        ArgumentCaptor<String> respCaptor = ArgumentCaptor.forClass(String.class);
        verify(sink).send(respCaptor.capture());

        assertThat(respCaptor.getValue()).contains("111be3d2").doesNotContain("replaceMe");
    }

    /**
     * Tests process() when the message is a response.
     */
    @Test
    void testProcessNoResponse() {
        // NOTE: this json file is a RESPONSE, not a request
        String request = ResourceUtils.getResourceAsString("org/onap/policy/simulators/appc/appc.legacy.success.json");
        assertNotNull(request);

        server.onTopicEvent(CommInfrastructure.NOOP, MY_TOPIC, request);

        verify(sink, never()).send(any());
    }
}
