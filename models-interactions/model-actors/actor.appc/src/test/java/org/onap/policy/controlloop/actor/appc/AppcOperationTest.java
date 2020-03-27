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

package org.onap.policy.controlloop.actor.appc;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.appc.CommonHeader;
import org.onap.policy.appc.Request;
import org.onap.policy.appc.ResponseCode;
import org.onap.policy.appc.ResponseStatus;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation.Status;
import org.onap.policy.controlloop.policy.PolicyResult;

public class AppcOperationTest extends BasicAppcOperation {
    private AppcOperation oper;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        // use same topic name for both sides
        initBeforeClass(MY_SINK, MY_SINK);
    }

    @AfterClass
    public static void tearDownAfterClass() {
        destroyAfterClass();
    }

    /**
     * Sets up.
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        oper = new AppcOperation(params, config) {
            @Override
            protected Pair<String, Request> makeRequest(int attempt) {
                return oper.makeRequest(attempt, MY_VNF);
            }
        };
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
    }

    @Test
    public void testStartPreprocessorAsync() {
        assertNotNull(oper.startPreprocessorAsync());
    }

    @Test
    public void testMakeRequest() {
        Pair<String, Request> result = oper.makeRequest(2, MY_VNF);
        String subreq = result.getLeft();
        assertNotNull(subreq);

        Request request = result.getRight();
        assertEquals(DEFAULT_OPERATION, request.getAction());

        assertNotNull(request.getPayload());

        CommonHeader header = request.getCommonHeader();
        assertNotNull(header);
        assertEquals(params.getRequestId(), header.getRequestId());

        assertEquals(subreq, header.getSubRequestId());

        // a subsequent request should have a different sub-request id
        result = oper.makeRequest(2, MY_VNF);
        assertNotEquals(subreq, result.getLeft());

        assertNotNull(result.getLeft());
        assertEquals(result.getLeft(), result.getRight().getCommonHeader().getSubRequestId());

        // repeat using a null payload
        params = params.toBuilder().payload(null).build();
        oper = new AppcOperation(params, config) {
            @Override
            protected Pair<String, Request> makeRequest(int attempt) {
                return oper.makeRequest(attempt, MY_VNF);
            }
        };
        assertEquals(Map.of(AppcOperation.VNF_ID_KEY, MY_VNF), oper.makeRequest(2, MY_VNF).getRight().getPayload());
    }

    @Test
    public void testConvertPayload() {
        Request request = oper.makeRequest(2, MY_VNF).getRight();

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

        oper = new AppcOperation(params, config) {
            @Override
            protected Pair<String, Request> makeRequest(int attempt) {
                return oper.makeRequest(attempt, MY_VNF);
            }
        };
        request = oper.makeRequest(2, MY_VNF).getRight();

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

        oper = new AppcOperation(params, config) {
            @Override
            protected Pair<String, Request> makeRequest(int attempt) {
                return oper.makeRequest(attempt, MY_VNF);
            }
        };
        request = oper.makeRequest(2, MY_VNF).getRight();

        payload.put(AppcOperation.VNF_ID_KEY, MY_VNF);
        payload.put(KEY1, "abc");
        payload.put(KEY2, null);
        payload.put(KEY3, "def");

        assertEquals(payload, request.getPayload());
    }

    @Test
    public void testGetExpectedKeyValues() {
        Request request = oper.makeRequest(2, MY_VNF).getRight();
        assertEquals(Arrays.asList(request.getCommonHeader().getSubRequestId()),
                        oper.getExpectedKeyValues(50, request));
    }

    @Test
    public void testDetmStatusStringResponse() {
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
    public void testSetOutcome() {
        final ResponseStatus status = response.getStatus();

        // null status
        response.setStatus(null);
        assertSame(outcome, oper.setOutcome(outcome, PolicyResult.SUCCESS, response));
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
        assertNotNull(outcome.getMessage());
        response.setStatus(status);

        // null description
        status.setDescription(null);
        assertSame(outcome, oper.setOutcome(outcome, PolicyResult.FAILURE, response));
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
        assertNotNull(outcome.getMessage());
        status.setDescription(MY_DESCRIPTION);

        for (PolicyResult result : PolicyResult.values()) {
            assertSame(outcome, oper.setOutcome(outcome, result, response));
            assertEquals(result, outcome.getResult());
            assertEquals(MY_DESCRIPTION, outcome.getMessage());
        }
    }
}
