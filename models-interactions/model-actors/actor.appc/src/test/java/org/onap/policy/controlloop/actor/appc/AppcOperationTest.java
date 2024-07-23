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

package org.onap.policy.controlloop.actor.appc;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.appc.CommonHeader;
import org.onap.policy.appc.Request;
import org.onap.policy.appc.ResponseCode;
import org.onap.policy.appc.ResponseStatus;
import org.onap.policy.controlloop.actorserviceprovider.OperationResult;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation.Status;
import org.onap.policy.controlloop.actorserviceprovider.parameters.BidirectionalTopicConfig;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

@ExtendWith(MockitoExtension.class)
 class AppcOperationTest extends BasicAppcOperation {
    private AppcOperation oper;

    @BeforeAll
     static void setUpBeforeClass() throws Exception {
        // use same topic name for both sides
        initBeforeClass(MY_SINK, MY_SINK);
    }

    @AfterAll
     static void tearDownAfterClass() {
        destroyAfterClass();
    }

    /**
     * Sets up.
     */
    @BeforeEach
    @Override
     void setUp() {
        super.setUp();

        oper = new MyOper(params, config);
    }

    @AfterEach
    @Override
     void tearDown() {
        super.tearDown();
    }

    @Test
     void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
    }

    @Test
     void testMakeRequest() {
        oper.generateSubRequestId(2);
        String subreq = oper.getSubRequestId();
        assertNotNull(subreq);

        Request request = oper.makeRequest(genvnf);
        assertEquals(DEFAULT_OPERATION, request.getAction());

        assertNotNull(request.getPayload());

        CommonHeader header = request.getCommonHeader();
        assertNotNull(header);
        assertEquals(params.getRequestId(), header.getRequestId());

        assertEquals(subreq, header.getSubRequestId());

        request = oper.makeRequest(genvnf);
        assertEquals(subreq, request.getCommonHeader().getSubRequestId());

        // repeat using a null payload
        params = params.toBuilder().payload(null).build();
        oper = new MyOper(params, config);
        assertEquals(Map.of(AppcOperation.VNF_ID_KEY, MY_VNF), oper.makeRequest(genvnf).getPayload());
    }

    @Test
     void testConvertPayload() {
        Request request = oper.makeRequest(genvnf);

        // @formatter:off
        assertEquals(
            Map.of(AppcOperation.VNF_ID_KEY, MY_VNF,
                    KEY1, Map.of("input", "hello"),
                    KEY2, Map.of("output", "world")),
            request.getPayload());
        // @formatter:on


        /*
         * insert invalid json text into the payload.
         */
        Map<String, Object> payload = new TreeMap<>(params.getPayload());
        payload.put("invalid-key", "{invalid json");

        params = params.toBuilder().payload(payload).build();

        oper = new MyOper(params, config);
        request = oper.makeRequest(genvnf);

        // @formatter:off
        assertEquals(
            Map.of(AppcOperation.VNF_ID_KEY, MY_VNF,
                    KEY1, Map.of("input", "hello"),
                    KEY2, Map.of("output", "world")),
            request.getPayload());
        // @formatter:on


        /*
         * insert null item into the payload.
         */
        payload = new TreeMap<>();
        payload.put(KEY1, "abc");
        payload.put(KEY2, null);
        payload.put(KEY3, "def");
        params = params.toBuilder().payload(payload).build();

        oper = new MyOper(params, config);
        request = oper.makeRequest(genvnf);

        payload.put(AppcOperation.VNF_ID_KEY, MY_VNF);
        payload.put(KEY1, "abc");
        payload.put(KEY2, null);
        payload.put(KEY3, "def");

        assertEquals(payload, request.getPayload());
    }

    @Test
     void testGetExpectedKeyValues() {
        oper.generateSubRequestId(2);
        Request request = oper.makeRequest(genvnf);
        assertEquals(Arrays.asList(request.getCommonHeader().getSubRequestId()),
                        oper.getExpectedKeyValues(50, request));
    }

    @Test
     void testDetmStatusStringResponse() {
        final ResponseStatus status = response.getStatus();

        // null status (i.e., it's a Request, not a Response)
        response.setStatus(null);
        assertEquals(Status.STILL_WAITING, oper.detmStatus("", response));
        response.setStatus(status);

        // invalid code
        status.setCode(-45);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.detmStatus("", response))
                        .withMessage("unknown APPC-C response status code: -45");

        status.setCode(ResponseCode.SUCCESS.getValue());
        assertEquals(Status.SUCCESS, oper.detmStatus("", response));

        status.setCode(ResponseCode.FAILURE.getValue());
        assertEquals(Status.FAILURE, oper.detmStatus("", response));

        status.setCode(ResponseCode.ERROR.getValue());
        assertThatIllegalArgumentException().isThrownBy(() -> oper.detmStatus("", response))
                        .withMessage("APP-C request was not accepted, code=" + ResponseCode.ERROR.getValue());

        status.setCode(ResponseCode.REJECT.getValue());
        assertThatIllegalArgumentException().isThrownBy(() -> oper.detmStatus("", response))
                        .withMessage("APP-C request was not accepted, code=" + ResponseCode.REJECT.getValue());

        status.setCode(ResponseCode.ACCEPT.getValue());
        assertEquals(Status.STILL_WAITING, oper.detmStatus("", response));
    }

    @Test
     void testSetOutcome() {
        final ResponseStatus status = response.getStatus();

        // null status
        response.setStatus(null);
        assertSame(outcome, oper.setOutcome(outcome, OperationResult.SUCCESS, response));
        assertEquals(OperationResult.SUCCESS, outcome.getResult());
        assertNotNull(outcome.getMessage());
        assertSame(response, outcome.getResponse());
        response.setStatus(status);

        // null description
        status.setDescription(null);
        assertSame(outcome, oper.setOutcome(outcome, OperationResult.FAILURE, response));
        assertEquals(OperationResult.FAILURE, outcome.getResult());
        assertNotNull(outcome.getMessage());
        assertSame(response, outcome.getResponse());
        status.setDescription(MY_DESCRIPTION);

        for (OperationResult result : OperationResult.values()) {
            assertSame(outcome, oper.setOutcome(outcome, result, response));
            assertEquals(result, outcome.getResult());
            assertEquals(MY_DESCRIPTION, outcome.getMessage());
            assertSame(response, outcome.getResponse());
        }
    }

    private class MyOper extends AppcOperation {

        MyOper(ControlLoopOperationParams params, BidirectionalTopicConfig config) {
            super(params, config, Collections.emptyList());
        }

        @Override
        protected Request makeRequest(int attempt) {
            return makeRequest(genvnf);
        }
    }
}
