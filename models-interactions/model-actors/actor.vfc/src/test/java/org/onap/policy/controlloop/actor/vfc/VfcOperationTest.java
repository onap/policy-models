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

package org.onap.policy.controlloop.actor.vfc;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.policy.PolicyResult;
import org.onap.policy.vfc.VfcResponse;
import org.onap.policy.vfc.VfcResponseDescriptor;

public class VfcOperationTest extends BasicVfcOperation {

    private VfcOperation oper;

    /**
     * setUp.
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        initConfig();

        oper = new VfcOperation(params, config) {};
    }

    @Test
    public void testConstructor_testGetWaitMsGet() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
        assertSame(config, oper.getConfig());
        assertEquals(1000 * WAIT_SEC_GETS, oper.getWaitMsGet());
    }

    @Test
    public void testStartPreprocessorAsync() {
        assertNotNull(oper.startPreprocessorAsync());
    }

    @Test
    public void testResetGetCount() {
        oper.resetGetCount();
        assertEquals(0, oper.getGetCount());
    }

    @Test
    public void testPostProcess() throws Exception {

        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            oper.postProcessResponse(outcome, PATH, rawResponse, response);
        });

        response.setResponseDescriptor(new VfcResponseDescriptor());
        response.setJobId("sampleJobId");

        // null status
        CompletableFuture<OperationOutcome> future2 = oper.postProcessResponse(outcome, PATH, rawResponse, response);
        assertFalse(future2.isDone());

        response.getResponseDescriptor().setStatus("FinisHeD");
        future2 = oper.postProcessResponse(outcome, PATH, rawResponse, response);
        assertTrue(future2.isDone());
        assertSame(outcome, future2.get());
        assertEquals(PolicyResult.SUCCESS, outcome.getResult());
        assertSame(response, outcome.getResponse());

        // failed
        response.getResponseDescriptor().setStatus("eRRor");
        future2 = oper.postProcessResponse(outcome, PATH, rawResponse, response);
        assertTrue(future2.isDone());
        assertSame(outcome, future2.get());
        assertEquals(PolicyResult.FAILURE, outcome.getResult());
        assertSame(response, outcome.getResponse());

        // unfinished
        response.getResponseDescriptor().setStatus("anything but finished");
        future2 = oper.postProcessResponse(outcome, PATH, rawResponse, response);
        assertFalse(future2.isDone());
    }

    @Test
    public void testGetRequestState() {
        VfcResponse mockResponse = Mockito.mock(VfcResponse.class);
        Mockito.when(mockResponse.getResponseDescriptor()).thenReturn(null);
        assertNull(oper.getRequestState(mockResponse));

        VfcResponseDescriptor mockDescriptor = Mockito.mock(VfcResponseDescriptor.class);
        Mockito.when(mockResponse.getResponseDescriptor()).thenReturn(mockDescriptor);

        // TODO use actual request state value
        Mockito.when(mockDescriptor.getStatus()).thenReturn("COMPLETE");
        assertNotNull(oper.getRequestState(mockResponse));
    }

    @Test
    public void testIsSuccess() {
        assertTrue(oper.isSuccess(rawResponse, response));
    }

}
