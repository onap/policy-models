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

import org.onap.policy.appclcm.AppcLcmDmaapWrapper;
import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;

/**
 * APPC-LCM topic server.
 */
public class AppcLcmTopicServer extends TopicServer<AppcLcmDmaapWrapper> {
    public AppcLcmTopicServer(TopicSink sink, TopicSource source) {
        super(sink, source, new StandardCoder(), AppcLcmDmaapWrapper.class);
    }

    @Override
    protected String process(AppcLcmDmaapWrapper request) {
        String response = ResourceUtils.getResourceAsString("org/onap/policy/simulators/appclcm/appc.lcm.success.json");
        return response.replace("${replaceMe}", request.getBody().getInput().getCommonHeader().getSubRequestId());
    }
}
