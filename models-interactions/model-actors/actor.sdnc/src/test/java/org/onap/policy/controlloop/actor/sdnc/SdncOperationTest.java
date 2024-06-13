/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2023, 2024 Nordix Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.sdnc.SdncHealRequest;
import org.onap.policy.sdnc.SdncHealRequestHeaderInfo;
import org.onap.policy.sdnc.SdncRequest;

@ExtendWith(MockitoExtension.class)
 class SdncOperationTest extends BasicSdncOperation {

    private static final String MY_URI = "my-uri";

    private SdncRequest request;
    private SdncOperation oper;

    /**
     * Sets up.
     */
    @Override
    @BeforeEach
     void setUp() throws Exception {
        super.setUp();

        request = new SdncRequest();
        request.setUrl(MY_URI);

        SdncHealRequest healRequest = new SdncHealRequest();
        request.setHealRequest(healRequest);

        SdncHealRequestHeaderInfo headerInfo = new SdncHealRequestHeaderInfo();
        healRequest.setRequestHeaderInfo(headerInfo);
        headerInfo.setSvcRequestId(SUB_REQ_ID);

        oper = new SdncOperation(params, config, Collections.emptyList()) {
            @Override
            protected SdncRequest makeRequest(int attempt) {
                return request;
            }
        };
    }

    @Test
     void testSdncOperator() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
    }

    @Test
     void testStartOperationAsync_testStartRequestAsync() throws Exception {
        verifyOperation(oper);
    }

    @Test
     void testIsSuccess() {
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
}
