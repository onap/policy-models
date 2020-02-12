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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.aai.AaiConstants;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardCoderObject;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.policy.PolicyResult;

public class AaiGetOperatorTest extends BasicAaiOperator<Void> {

    private static final String INPUT_FIELD = "input";
    private static final String TEXT = "my-text";

    private AaiGetOperation oper;

    public AaiGetOperatorTest() {
        super(AaiConstants.ACTOR_NAME, AaiGetOperation.TENANT);
    }

    /**
     * Sets up.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        when(client.get(any(), any(), any())).thenReturn(future);

        oper = new AaiGetOperation(params, operator);
    }

    @Test
    public void testGetRetry() {
        // use default if null retry
        assertEquals(AaiGetOperation.DEFAULT_RETRY, oper.getRetry(null));

        // otherwise, use specified value
        assertEquals(0, oper.getRetry(0));
        assertEquals(10, oper.getRetry(10));
    }

    @Test
    public void testStartOperationAsync_testStartQueryAsync_testPostProcessResponse() throws Exception {

        // return a map in the reply
        Map<String, String> reply = Map.of(INPUT_FIELD, TEXT);
        when(rawResponse.readEntity(String.class)).thenReturn(new StandardCoder().encode(reply));

        CompletableFuture<OperationOutcome> future2 = oper.startOperationAsync(1, outcome);
        assertFalse(future2.isDone());

        verify(client).get(callbackCaptor.capture(), any(), any());
        callbackCaptor.getValue().completed(rawResponse);

        assertEquals(PolicyResult.SUCCESS, future2.get(5, TimeUnit.SECONDS).getResult());

        // data should have been cached within the context
        StandardCoderObject data = context.getProperty(AaiGetOperation.getTenantKey(TARGET_ENTITY));
        assertNotNull(data);
        assertEquals(TEXT, data.getString(INPUT_FIELD));
    }

    /**
     * Tests startOperationAsync() when there's a failure.
     */
    @Test
    public void testStartOperationAsyncFailure() throws Exception {

        when(rawResponse.getStatus()).thenReturn(500);

        when(rawResponse.readEntity(String.class)).thenReturn("");

        CompletableFuture<OperationOutcome> future2 = oper.startOperationAsync(1, outcome);
        assertFalse(future2.isDone());

        verify(client).get(callbackCaptor.capture(), any(), any());
        callbackCaptor.getValue().completed(rawResponse);

        assertEquals(PolicyResult.FAILURE, future2.get(5, TimeUnit.SECONDS).getResult());

        // data should NOT have been cached within the context
        assertNull(context.getProperty(AaiGetOperation.getTenantKey(TARGET_ENTITY)));
    }

    @Test
    public void testMakeHeaders() {
        verifyHeaders(oper.makeHeaders());
    }

    @Test
    public void testMakePath() {
        assertEquals(PATH + "/" + TARGET_ENTITY, oper.makePath());
    }

    @Test
    public void testAaiGetOperator() {
        assertEquals(AaiConstants.ACTOR_NAME, oper.getActorName());
        assertEquals(AaiGetOperation.TENANT, oper.getName());
    }

    @Test
    public void testGetTenantKey() {
        assertEquals("AAI.Tenant." + TARGET_ENTITY, AaiGetOperation.getTenantKey(TARGET_ENTITY));
    }
}
