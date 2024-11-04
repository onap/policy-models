/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.aai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.onap.policy.common.endpoints.http.client.HttpClientFactoryInstance;
import org.onap.policy.common.endpoints.http.server.HttpServletServerFactoryInstance;
import org.onap.policy.common.parameters.topic.BusTopicParams;
import org.onap.policy.controlloop.actor.test.BasicHttpOperation;
import org.onap.policy.simulators.Util;

/**
 * Superclass for various operator tests.
 */
public abstract class BasicAaiOperation extends BasicHttpOperation {

    /**
     * Constructs the object using a default actor and operation name.
     */
    public BasicAaiOperation() {
        super();
    }

    /**
     * Constructs the object.
     *
     * @param actor actor name
     * @param operation operation name
     */
    public BasicAaiOperation(String actor, String operation) {
        super(actor, operation);
    }

    /**
     * Starts the simulator.
     */
    protected static void initBeforeClass() throws Exception {
        Util.buildAaiSim();

        BusTopicParams clientParams = BusTopicParams.builder().clientName(MY_CLIENT).basePath("aai/")
                        .hostname("localhost").managed(true).port(Util.AAISIM_SERVER_PORT).build();
        HttpClientFactoryInstance.getClientFactory().build(clientParams);
    }

    protected static void destroyAfterClass() {
        HttpClientFactoryInstance.getClientFactory().destroy();
        HttpServletServerFactoryInstance.getServerFactory().destroy();
    }

    protected void verifyHeaders(Map<String, Object> headers) {
        assertEquals("POLICY", headers.get("X-FromAppId").toString());
        assertEquals(params.getRequestId().toString(), headers.get("X-TransactionId"));
        assertEquals("application/json", headers.get("Accept").toString());
    }
}
