/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation.
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

import org.onap.policy.appc.Request;
import org.onap.policy.common.message.bus.event.TopicSink;
import org.onap.policy.common.message.bus.event.TopicSource;
import org.onap.policy.common.utils.coder.StandardCoderInstantAsMillis;
import org.onap.policy.common.utils.resources.ResourceUtils;

/**
 * Legacy APPC topic server.
 */
public class AppcLegacyTopicServer extends TopicServer<Request> {

    public AppcLegacyTopicServer(TopicSink sink, TopicSource source) {
        super(sink, source, new StandardCoderInstantAsMillis(), Request.class);
    }

    @Override
    protected String process(Request request) {
        /*
         * The request and response are on the same topic, thus this may be invoked with a
         * request or with a response object. If the "action" is null, then we know it's a
         * response.
         */
        if (request.getAction() == null) {
            return null;
        }

        var response = ResourceUtils.getResourceAsString("org/onap/policy/simulators/appc/appc.legacy.success.json");
        return response.replace("${replaceMe}", request.getCommonHeader().getSubRequestId());
    }
}
