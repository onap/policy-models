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

package org.onap.policy.controlloop.actor.sdnc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.endpoints.http.client.HttpClient;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;
import org.onap.policy.sdnc.SdncRequest;

public class SdncOperatorTest extends BasicSdncOperator {

    private SdncRequest request;
    private SdncOperator oper;

    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        oper = new SdncOperator(DEFAULT_ACTOR, DEFAULT_OPERATION) {
            @Override
            protected SdncRequest makeRequest(ControlLoopOperationParams params, int attempt) {
                return request;
            }

            @Override
            public HttpClient getClient() {
                return client;
            }
        };
    }

    @Test
    public void testSdncOperator() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
    }

    @Test
    public void testStartOperationAsync_testStartRequestAsync() throws Exception {
        verifyOperation(oper);
    }

    @Test
    public void testIsSuccess() {
        // success case
        response.getResponseOutput().setResponseCode("200");
        assertTrue(oper.isSuccess(null, response));

        // failure code
        response.getResponseOutput().setResponseCode("555");
        assertFalse(oper.isSuccess(null, response));

        // null code
        response.getResponseOutput().setResponseCode(null);
        assertFalse(oper.isSuccess(null, response));

        // null output
        response.setResponseOutput(null);
        assertFalse(oper.isSuccess(null, response));
    }

    @Test
    public void testStartQueryAsync() {
        assertThatThrownBy(() -> oper.startQueryAsync(null, null, null))
                        .isInstanceOf(UnsupportedOperationException.class);
    }

    @Override
    protected Map<String, String> makeEnrichment() {
        return new TreeMap<>();
    }
}
