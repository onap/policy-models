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

import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.event.comm.TopicListener;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;

/**
 * Server whose requests are received from a topic, and whose responses are sent to a
 * topic.
 */
public abstract class TopicServer<Q> implements TopicListener {
    private final TopicSink sink;
    private final TopicSource source;
    private final Coder coder;
    private final Class<Q> reqClass;

    /**
     * Constructs the object.
     *
     * @param sink sink to which responses should be published
     * @param source source from which requests arrive
     */
    public TopicServer(TopicSink sink, TopicSource source, Coder coder, Class<Q> reqClass) {
        this.sink = sink;
        this.source = source;
        this.coder = coder;
        this.reqClass = reqClass;

        source.register(this);
    }

    public void shutdown() {
        source.unregister(this);
    }

    @Override
    public void onTopicEvent(CommInfrastructure commType, String topic, String request) {
        Q req;
        try {
            req = coder.decode(request, reqClass);
        } catch (CoderException e) {
            throw new IllegalArgumentException("cannot decode request from " + source.getTopic());
        }

        sink.send(process(req));
    }

    protected abstract String process(Q request);
}
