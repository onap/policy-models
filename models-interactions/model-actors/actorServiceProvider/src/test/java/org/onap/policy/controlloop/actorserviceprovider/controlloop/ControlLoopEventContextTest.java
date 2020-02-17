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

package org.onap.policy.controlloop.actorserviceprovider.controlloop;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.controlloop.VirtualControlLoopEvent;
import org.onap.policy.controlloop.actorserviceprovider.OperationOutcome;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

public class ControlLoopEventContextTest {
    private static final String ITEM_KEY = "obtain-B";

    private static final UUID REQ_ID = UUID.randomUUID();

    private Map<String, String> enrichment;
    private VirtualControlLoopEvent event;
    private ControlLoopEventContext context;

    /**
     * Initializes data, including {@link #context}.
     */
    @Before
    public void setUp() {
        enrichment = Map.of("abc", "one", "def", "two");

        event = new VirtualControlLoopEvent();
        event.setRequestId(REQ_ID);
        event.setAai(enrichment);

        context = new ControlLoopEventContext(event);
    }

    @Test
    public void testControlLoopEventContext() {
        assertSame(event, context.getEvent());
        assertSame(REQ_ID, context.getRequestId());
        assertEquals(enrichment, context.getEnrichment());

        // null event
        assertThatThrownBy(() -> new ControlLoopEventContext(null));

        // no request id, no enrichment data
        event.setRequestId(null);
        event.setAai(null);
        context = new ControlLoopEventContext(event);
        assertSame(event, context.getEvent());
        assertNotNull(context.getRequestId());
        assertEquals(Map.of(), context.getEnrichment());
    }

    @Test
    public void testContains_testGetProperty_testSetProperty() {
        context.setProperty("abc", "a string");
        context.setProperty("def", 100);

        assertFalse(context.contains("ghi"));

        String strValue = context.getProperty("abc");
        assertEquals("a string", strValue);

        int intValue = context.getProperty("def");
        assertEquals(100, intValue);
    }

    @Test
    public void testObtain() {
        final ControlLoopOperationParams params = mock(ControlLoopOperationParams.class);

        // property is already loaded
        context.setProperty("obtain-A", "value-A");
        assertNull(context.obtain("obtain-A", params));

        // new property - should retrieve
        CompletableFuture<OperationOutcome> future = new CompletableFuture<>();
        when(params.start()).thenReturn(future);
        assertSame(future, context.obtain(ITEM_KEY, params));

        // provide a new future, but it shouldn't be used yet
        CompletableFuture<OperationOutcome> future2 = new CompletableFuture<>();
        when(params.start()).thenReturn(future2);

        // repeat - should get the same future, without invoking start() again
        assertSame(future, context.obtain(ITEM_KEY, params));
        verify(params).start();

        // cancel the future
        future.cancel(false);

        // obtain() should now return a new future
        assertSame(future2, context.obtain(ITEM_KEY, params));
        assertFalse(future2.isDone());
    }
}
