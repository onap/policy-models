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

import org.onap.policy.common.endpoints.event.comm.TopicSink;
import org.onap.policy.common.endpoints.event.comm.TopicSource;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.sdnr.PciMessage;

/**
 * SDNR topic server.
 */
public class SdnrTopicServer extends TopicServer<PciMessage> {

    public SdnrTopicServer(TopicSink sink, TopicSource source) {
        super(sink, source, new StandardCoder(), PciMessage.class);
    }

    @Override
    protected String process(PciMessage request) {
        /*
         * In case the request and response are on the same topic, this may be invoked
         * with a request or with a response object. If the "output" is null, then we know
         * it's a response.
         */
        if (request.getBody().getOutput() != null) {
            return null;
        }

        String response = ResourceUtils.getResourceAsString("org/onap/policy/simulators/sdnr/vpci.sdnr.success.json");
        return response.replace("${replaceMe}", request.getBody().getInput().getCommonHeader().getSubRequestId());
    }
}
