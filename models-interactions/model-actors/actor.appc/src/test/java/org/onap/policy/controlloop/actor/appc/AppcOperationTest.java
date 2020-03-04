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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.appc.CommonHeader;
import org.onap.policy.appc.Request;
import org.onap.policy.appc.ResponseCode;
import org.onap.policy.appc.ResponseStatus;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoderInstantAsMillis;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation.Status;
import org.onap.policy.controlloop.policy.PolicyResult;

public class AppcOperationTest extends BasicAppcOperation {
    private AppcOperation oper;

    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        oper = new AppcOperation(params, config);
    }

    @Test
    public void testAppcOperation() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
    }

    @Test
    public void testStartPreprocessorAsync() throws Exception {
        CompletableFuture<OperationOutcome> future2 = new CompletableFuture<>();
        context = mock(ControlLoopEventContext.class);
        when(context.obtain(eq(AaiCqResponse.CONTEXT_KEY), any())).thenReturn(future2);
        when(context.getEvent()).thenReturn(event);
        params = params.toBuilder().context(context).build();

        AtomicBoolean guardStarted = new AtomicBoolean();

        oper = new AppcOperation(params, config) {
            @Override
            protected CompletableFuture<OperationOutcome> startGuardAsync() {
                guardStarted.set(true);
                return super.startGuardAsync();
            }
        };

        CompletableFuture<OperationOutcome> future3 = oper.startPreprocessorAsync();
        assertNotNull(future3);
        assertFalse(future.isDone());
        assertTrue(guardStarted.get());
        verify(context).obtain(eq(AaiCqResponse.CONTEXT_KEY), any());

        future2.complete(params.makeOutcome());
        assertTrue(executor.runAll(100));
        assertTrue(future3.isDone());
        assertEquals(PolicyResult.SUCCESS, future3.get().getResult());
    }

    @Test
    public void testMakeRequest() throws CoderException {
        final AaiCqResponse cq = populateCq();

        Request request = oper.makeRequest(2);
        assertNotNull(request);
        assertEquals(DEFAULT_OPERATION, request.getAction());

        assertNotNull(request.getPayload());
        assertEquals(MY_VNF, request.getPayload().get(AppcOperation.VNF_ID_KEY));

        CommonHeader header = request.getCommonHeader();
        assertNotNull(header);
        assertEquals(params.getRequestId(), header.getRequestId());

        String subreq = header.getSubRequestId();
        assertNotNull(subreq);

        verifyRequest("expectedRequest.json", request, IGNORE_FIELDS);

        // a subsequent request should have a different sub-request id
        assertNotEquals(subreq, oper.makeRequest(2).getCommonHeader().getSubRequestId());

        // repeat using a null payload
        params = params.toBuilder().payload(null).build();
        oper = new AppcOperation(params, config);
        assertEquals(Map.of(AppcOperation.VNF_ID_KEY, MY_VNF), oper.makeRequest(2).getPayload());

        // missing vnf-id
        cq.setInventoryResponseItems(Arrays.asList());
        assertThatIllegalArgumentException().isThrownBy(() -> oper.makeRequest(1));
    }

    @Test
    public void testConvertPayload() {
        populateCq();
        Request request = oper.makeRequest(2);

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

        oper = new AppcOperation(params, config);
        request = oper.makeRequest(2);

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

        oper = new AppcOperation(params, config);
        request = oper.makeRequest(2);

        payload.put(AppcOperation.VNF_ID_KEY, MY_VNF);
        payload.put(KEY1, "abc");
        payload.put(KEY2, null);
        payload.put(KEY3, "def");

        assertEquals(payload, request.getPayload());
    }

    @Test
    public void testGetExpectedKeyValues() {
        populateCq();
        Request request = oper.makeRequest(2);
        assertEquals(Arrays.asList(request.getCommonHeader().getSubRequestId()),
                        oper.getExpectedKeyValues(50, request));
    }

    @Test
    public void testDetmStatusStringResponse() {
        final ResponseStatus status = response.getStatus();

        // null status
        response.setStatus(null);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.detmStatus("", response))
                        .withMessage("APP-C response is missing the response status");
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

    @Test
    public void testMakeCoder() {
        assertTrue(oper.makeCoder() instanceof StandardCoderInstantAsMillis);
    }


    protected AaiCqResponse populateCq() {
        AaiCqResponse cq = new AaiCqResponse("{}");

        params.getContext().setProperty(AaiCqResponse.CONTEXT_KEY, cq);

        // populate the CQ data with a vnf-id
        GenericVnf genvnf = new GenericVnf();
        genvnf.setVnfId(MY_VNF);
        genvnf.setModelInvariantId(RESOURCE_ID);
        cq.setInventoryResponseItems(Arrays.asList(genvnf));
        return cq;
    }
}
