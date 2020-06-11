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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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
    public void testConstructor() {
        assertEquals(DEFAULT_ACTOR, oper.getActorName());
        assertEquals(DEFAULT_OPERATION, oper.getName());
        assertSame(config, oper.getConfig());
    }

    @Test
    public void testStartPreprocessorAsync() {
        assertNotNull(oper.startPreprocessorAsync());
    }

    @Test
    public void testResetPollCount() {
        oper.resetPollCount();
        assertEquals(0, oper.getPollCount());
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
