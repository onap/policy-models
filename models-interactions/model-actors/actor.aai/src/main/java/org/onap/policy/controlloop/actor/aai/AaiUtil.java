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

package org.onap.policy.controlloop.actor.aai;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

/**
 * Utilities used by A&AI classes.
 */
public class AaiUtil {

    private AaiUtil() {
        // do nothing
    }

    /**
     * Makes standard request headers for A&AI requests.
     *
     * @param params operation parameters
     * @return new request headers
     */
    public static Map<String, Object> makeHeaders(ControlLoopOperationParams params) {
        Map<String, Object> headers = new HashMap<>();

        headers.put("X-FromAppId", "POLICY");
        headers.put("X-TransactionId", params.getRequestId().toString());
        headers.put("Accept", MediaType.APPLICATION_JSON);

        return headers;
    }
}
