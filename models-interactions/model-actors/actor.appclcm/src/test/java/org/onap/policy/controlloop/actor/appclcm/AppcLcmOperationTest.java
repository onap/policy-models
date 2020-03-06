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

package org.onap.policy.controlloop.actor.appclcm;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Test;
import org.onap.aai.domain.yang.GenericVnf;
import org.onap.policy.aai.AaiCqResponse;
import org.onap.policy.appclcm.AppcLcmBody;
import org.onap.policy.appclcm.AppcLcmCommonHeader;
import org.onap.policy.appclcm.AppcLcmDmaapWrapper;
import org.onap.policy.appclcm.AppcLcmOutput;
import org.onap.policy.appclcm.AppcLcmResponseStatus;
import org.onap.policy.common.utils.coder.Coder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.controlloop.ControlLoopOperation;
import org.onap.policy.controlloop.actor.test.BasicBidirectionalTopicOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.controlloop.ControlLoopEventContext;
import org.onap.policy.controlloop.actorserviceprovider.impl.BidirectionalTopicOperation.Status;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.controlloop.policy.Target;

public class AppcLcmOperationTest extends BasicBidirectionalTopicOperation {

    private static final String EXPECTED_EXCEPTION = "expected exception";
    private static final String PAYLOAD_KEY1 = "key-A";
    private static final String PAYLOAD_VALUE1 = "value-A";
    private static final String MY_MESSAGE = "my-message";
    protected static final String MY_VNF = "my-vnf";
    protected static final String RESOURCE_ID = "my-resource";
    private static final int SUCCESS_CODE = 400;

    private AppcLcmDmaapWrapper response;
    private AppcLcmOperation oper;

    /**
     * Sets up.
     */
    @Before
    public void setUp() {
        super.setUpBasic();

        response = makeResponse();

        oper = new AppcLcmOperation(params, config);
    }

    @Test
    public void testConstructor() {
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

        oper = new AppcLcmOperation(params, config) {
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
    public void testMakeRequest() {
        final AaiCqResponse cqdata = populateCq();

        AppcLcmDmaapWrapper request = oper.makeRequest(2);
        assertEquals("DefaultOperation", request.getBody().getInput().getAction());

        AppcLcmCommonHeader header = request.getBody().getInput().getCommonHeader();
        assertNotNull(header);
        assertEquals(params.getRequestId(), header.getRequestId());

        String subreq = header.getSubRequestId();
        assertNotNull(subreq);

        assertEquals("{vnf-id=my-vnf}", request.getBody().getInput().getActionIdentifiers().toString());

        // a subsequent request should have a different sub-request id
        assertNotEquals(subreq, oper.makeRequest(2).getBody().getInput().getCommonHeader().getSubRequestId());

        // missing vnf
        cqdata.setInventoryResponseItems(Arrays.asList());
        assertThatIllegalArgumentException().isThrownBy(() -> oper.makeRequest(2))
                        .withMessage("target vnf-id could not be found");
    }

    @Test
    public void testConvertPayload() {
        // only builds a payload for ConfigModify
        params = params.toBuilder().operation(AppcLcmConstants.OPERATION_CONFIG_MODIFY).build();
        oper = new AppcLcmOperation(params, config);

        populateCq();
        AppcLcmDmaapWrapper req = oper.makeRequest(2);
        assertEquals("{\"key-A\":\"value-A\"}", req.getBody().getInput().getPayload());

        // coder exception
        oper = new AppcLcmOperation(params, config) {
            @Override
            protected Coder makeCoder() {
                return new StandardCoder() {
                    @Override
                    public String encode(Object object) throws CoderException {
                        throw new CoderException(EXPECTED_EXCEPTION);
                    }
                };
            }
        };

        assertThatIllegalArgumentException().isThrownBy(() -> oper.makeRequest(2))
                        .withMessage("Cannot convert payload");
    }

    @Test
    public void testGetExpectedKeyValues() {
        AaiCqResponse cq = new AaiCqResponse("{}");

        // missing vnf-id
        params.getContext().setProperty(AaiCqResponse.CONTEXT_KEY, cq);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.makeRequest(1));

        // populate the CQ data with a vnf-id
        populateCq();

        AppcLcmDmaapWrapper request = oper.makeRequest(2);
        assertEquals(Arrays.asList(request.getBody().getInput().getCommonHeader().getSubRequestId()),
                        oper.getExpectedKeyValues(50, request));
    }

    @Test
    public void testDetmStatus() {
        assertEquals(Status.SUCCESS, oper.detmStatus(null, response));

        // failure
        response.getBody().getOutput().getStatus().setCode(405);
        assertEquals(Status.FAILURE, oper.detmStatus(null, response));

        // error
        response.getBody().getOutput().getStatus().setCode(200);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.detmStatus(null, response));

        // reject
        response.getBody().getOutput().getStatus().setCode(305);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.detmStatus(null, response));

        // accepted
        response.getBody().getOutput().getStatus().setCode(100);
        assertEquals(Status.STILL_WAITING, oper.detmStatus(null, response));

        // other
        response.getBody().getOutput().getStatus().setCode(-1);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.detmStatus(null, response));

        // null status
        response.getBody().getOutput().setStatus(null);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.detmStatus(null, response));
    }

    @Test
    public void testSetOutcome() {
        oper.setOutcome(outcome, PolicyResult.SUCCESS, response);
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
        assertEquals(MY_MESSAGE, outcome.getMessage());

        // failure
        oper.setOutcome(outcome, PolicyResult.FAILURE, response);
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
        assertEquals(MY_MESSAGE, outcome.getMessage());

        // null message
        response.getBody().getOutput().getStatus().setMessage(null);
        oper.setOutcome(outcome, PolicyResult.SUCCESS, response);
        assertEquals(ControlLoopOperation.SUCCESS_MSG, outcome.getMessage());

        // null status
        response.getBody().getOutput().setStatus(null);
        oper.setOutcome(outcome, PolicyResult.SUCCESS, response);
        assertEquals(ControlLoopOperation.SUCCESS_MSG, outcome.getMessage());
    }

    @Test
    public void testGetStatus() {
        assertNotNull(oper.getStatus(response));

        // null status
        response.getBody().getOutput().setStatus(null);
        assertNull(oper.getStatus(response));

        // null outcome
        response.getBody().setOutput(null);
        assertNull(oper.getStatus(response));

        // null body
        response.setBody(null);
        assertNull(oper.getStatus(response));

        // null response
        assertNull(oper.getStatus(null));
    }

    @Test
    public void testOperationSupportsPayload() {
        // these should support a payload
        Set<String> supported = Set.of(AppcLcmConstants.OPERATION_CONFIG_MODIFY);

        for (String name : supported) {
            params = params.toBuilder().operation(name).build();
            oper = new AppcLcmOperation(params, config);
            assertTrue(name, oper.operationSupportsPayload());
        }

        // these should NOT support a payload
        Set<String> unsupported = AppcLcmConstants.OPERATION_NAMES.stream().filter(name -> !supported.contains(name))
                        .collect(Collectors.toSet());

        for (String name : unsupported) {
            params = params.toBuilder().operation(name).build();
            oper = new AppcLcmOperation(params, config);
            assertFalse(name, oper.operationSupportsPayload());
        }

        // pick an operation that would ordinarily support payloads
        String sup = supported.iterator().next();

        // verify that it still supports payload
        params = params.toBuilder().operation(sup).build();
        oper = new AppcLcmOperation(params, config);
        assertTrue(oper.operationSupportsPayload());

        // try with empty payload
        params = params.toBuilder().payload(Map.of()).build();
        oper = new AppcLcmOperation(params, config);
        assertFalse(oper.operationSupportsPayload());

        // try with null payload
        params = params.toBuilder().payload(null).build();
        oper = new AppcLcmOperation(params, config);
        assertFalse(oper.operationSupportsPayload());
    }

    @Override
    protected void makeContext() {
        super.makeContext();

        Target target = new Target();
        target.setResourceID(RESOURCE_ID);

        params = params.toBuilder().target(target).build();
    }

    @Override
    protected Map<String, Object> makePayload() {
        return Map.of(PAYLOAD_KEY1, PAYLOAD_VALUE1);
    }

    private AppcLcmDmaapWrapper makeResponse() {
        AppcLcmDmaapWrapper response = new AppcLcmDmaapWrapper();

        AppcLcmBody body = new AppcLcmBody();
        response.setBody(body);

        AppcLcmOutput output = new AppcLcmOutput();
        body.setOutput(output);

        AppcLcmResponseStatus status = new AppcLcmResponseStatus();
        output.setStatus(status);
        status.setMessage(MY_MESSAGE);
        status.setCode(SUCCESS_CODE);

        return response;
    }

    protected AaiCqResponse populateCq() {
        AaiCqResponse cq = new AaiCqResponse("{}");
        params.getContext().setProperty(AaiCqResponse.CONTEXT_KEY, cq);

        GenericVnf genvnf = new GenericVnf();
        genvnf.setVnfId(MY_VNF);
        genvnf.setModelInvariantId(RESOURCE_ID);
        cq.setInventoryResponseItems(Arrays.asList(genvnf));

        return cq;
    }
}
