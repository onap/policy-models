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

import org.onap.policy.appclcm.AppcLcmMessageWrapper;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;

/**
 * APPC-LCM topic server.
 */
public class AppcLcmTopicServer extends TopicServer<AppcLcmMessageWrapper> {

    public AppcLcmTopicServer(TopicSink sink, TopicSource source) {
        super(sink, source, new StandardCoder(), AppcLcmMessageWrapper.class);
    }

    @Override
    protected String process(AppcLcmMessageWrapper request) {
        /*
         * In case the request and response are on the same topic, this may be invoked
         * with a request or with a response object. If the "output" is not null, then we
         * know it's a response.
         */
        if (request.getBody().getOutput() != null) {
            return null;
        }

        var response = ResourceUtils.getResourceAsString("org/onap/policy/simulators/appclcm/appc.lcm.success.json");
        return response.replace("${replaceMe}", request.getBody().getInput().getCommonHeader().getSubRequestId());
    }
}
