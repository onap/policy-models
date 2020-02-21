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

package org.onap.policy.controlloop.actor.guard;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.controlloop.actor.test.BasicHttpOperation;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.models.decisions.concepts.DecisionRequest;
import org.onap.policy.models.decisions.concepts.DecisionResponse;

public class GuardOperationTest extends BasicHttpOperation<DecisionRequest> {

    private GuardOperation oper;

    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        super.setUpBasic();

        GuardConfig cguard = mock(GuardConfig.class);
        when(cguard.makeRequest()).thenAnswer(args -> new TreeMap<>(Map.of("action", "guard")));

        config = cguard;
        initConfig();

        oper = new GuardOperation(params, config);
    }

    @Test
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
    }

    @Test
    public void testStartOperationAsync() throws Exception {
        CompletableFuture<OperationOutcome> future2 = oper.start();
        executor.runAll(100);
        assertFalse(future2.isDone());

        DecisionResponse resp = new DecisionResponse();
        resp.setStatus(GuardOperation.PERMIT);
        when(rawResponse.readEntity(String.class)).thenReturn(Util.translate("", resp, String.class));

        verify(client).post(callbackCaptor.capture(), any(), requestCaptor.capture(), any());
        callbackCaptor.getValue().completed(rawResponse);

        executor.runAll(100);
        assertTrue(future2.isDone());

        assertEquals(PolicyResult.SUCCESS, future2.get().getResult());
    }

    @Test
    public void testMakeRequest() throws CoderException {
        verifyPayload("makeReqStd.json", makePayload());
        verifyPayload("makeReqDefault.json", new TreeMap<>());

        Map<String, Object> payload = new TreeMap<>();
        payload.put("action", "some action");
        payload.put("hello", "world");
        payload.put("r u there?", "yes");
        payload.put("requestId", "some request id");

        Map<String, Object> resource = new TreeMap<>();
        payload.put("resource", resource);
        resource.put("abc", "def");
        resource.put("ghi", "jkl");

        verifyPayload("makeReq.json", payload);

        // null payload - start with fresh parameters and operation
        params = params.toBuilder().payload(null).build();
        oper = new GuardOperation(params, config);
        assertThatIllegalArgumentException().isThrownBy(() -> oper.makeRequest());
    }

    private void verifyPayload(String expectedJsonFile, Map<String, Object> payload) throws CoderException {
        params.getPayload().clear();
        params.getPayload().putAll(payload);

        Map<String, Object> requestMap = oper.makeRequest();

        verifyRequest(expectedJsonFile, requestMap, "requestId");
    }

    @Test
    public void testPostProcessResponse() {
        DecisionResponse response = new DecisionResponse();

        // null status
        response.setStatus(null);
        verifyOutcome(response, PolicyResult.FAILURE, "response contains no status");

        // permit, mixed case
        response.setStatus("peRmit");
        verifyOutcome(response, PolicyResult.SUCCESS, "peRmit");

        // indeterminate, mixed case
        response.setStatus("inDETerminate");
        verifyOutcome(response, PolicyResult.SUCCESS, "inDETerminate");

        // deny, mixed case
        response.setStatus("deNY");
        verifyOutcome(response, PolicyResult.FAILURE, "deNY");

        // unknown status
        response.setStatus("unknown");
        verifyOutcome(response, PolicyResult.FAILURE, "unknown");
    }

    private void verifyOutcome(DecisionResponse response, PolicyResult expectedResult, String expectedMessage) {
        oper.postProcessResponse(outcome, BASE_URI, rawResponse, response);
        assertEquals(expectedResult, outcome.getResult());
        assertEquals(expectedMessage, outcome.getMessage());
    }

    @Override
    protected Map<String, Object> makePayload() {
        DecisionRequest req = new DecisionRequest();
        req.setAction("my-action");
        req.setOnapComponent("my-onap-component");
        req.setOnapInstance("my-onap-instance");
        req.setOnapName("my-onap-name");
        req.setRequestId("my-request-id");

        // add resources
        Map<String, Object> resource = new TreeMap<>();
        req.setResource(resource);
        resource.put("actor", "resource-actor");
        resource.put("operation", "resource-operation");

        @SuppressWarnings("unchecked")
        Map<String, Object> map = Util.translate("", req, TreeMap.class);

        return map;
    }
}
