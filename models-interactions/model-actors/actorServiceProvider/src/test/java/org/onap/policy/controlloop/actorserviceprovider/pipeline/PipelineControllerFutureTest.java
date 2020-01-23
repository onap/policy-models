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

package org.onap.policy.controlloop.actorserviceprovider.pipeline;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PipelineControllerFutureTest {
    private static final IllegalStateException EXPECTED_EXCEPTION = new IllegalStateException("expected exception");
    private static final String TEXT = "some text";

    @Mock
    private Runnable runnable1;

    @Mock
    private Runnable runnable2;

    @Mock
    private Future<String> future1;

    @Mock
    private Future<String> future2;

    @Mock
    private CompletableFuture<String> compFuture;


    private PipelineControllerFuture<String> controller;


    /**
     * Initializes fields, including {@link #controller}. Adds all runners and futures to
     * the controller.
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        controller = new PipelineControllerFuture<>();

        controller.add(runnable1);
        controller.add(future1);
        controller.add(runnable2);
        controller.add(future2);
    }

    @Test
    public void testCancel_testAddFutureOfFBoolean_testAddRunnable__testIsRunning() {
        assertTrue(controller.isRunning());

        assertTrue(controller.cancel(false));

        assertTrue(controller.isCancelled());
        assertFalse(controller.isRunning());

        verify(runnable1).run();
        verify(runnable2).run();
        verify(future1).cancel(anyBoolean());
        verify(future2).cancel(anyBoolean());

        // re-invoke; nothing should change
        assertTrue(controller.cancel(true));

        assertTrue(controller.isCancelled());
        assertFalse(controller.isRunning());

        verify(runnable1).run();
        verify(runnable2).run();
        verify(future1).cancel(anyBoolean());
        verify(future2).cancel(anyBoolean());
    }

    @Test
    public void testDelayedComplete() throws Exception {
        controller.add(runnable1);

        BiConsumer<String, Throwable> stopper = controller.delayedComplete();

        // shouldn't have run yet
        assertTrue(controller.isRunning());
        verify(runnable1, never()).run();

        stopper.accept(TEXT, null);

        assertTrue(controller.isDone());
        assertEquals(TEXT, controller.get());

        assertFalse(controller.isRunning());
        verify(runnable1).run();

        // re-invoke; nothing should change
        stopper.accept(TEXT, EXPECTED_EXCEPTION);
        assertFalse(controller.isCompletedExceptionally());

        assertFalse(controller.isRunning());
        verify(runnable1).run();
    }

    /**
     * Tests delayedComplete() when an exception is generated.
     */
    @Test
    public void testDelayedCompleteWithException() throws Exception {
        controller.add(runnable1);

        BiConsumer<String, Throwable> stopper = controller.delayedComplete();

        // shouldn't have run yet
        assertTrue(controller.isRunning());
        verify(runnable1, never()).run();

        stopper.accept(TEXT, EXPECTED_EXCEPTION);

        assertTrue(controller.isDone());
        assertThatThrownBy(() -> controller.get()).hasCause(EXPECTED_EXCEPTION);

        assertFalse(controller.isRunning());
        verify(runnable1).run();

        // re-invoke; nothing should change
        stopper.accept(TEXT, null);
        assertTrue(controller.isCompletedExceptionally());

        assertFalse(controller.isRunning());
        verify(runnable1).run();
    }

    @Test
    public void testDelayedRemoveFutureOfF() throws Exception {
        BiConsumer<String, Throwable> remover = controller.delayedRemove(future1);

        remover.accept(TEXT, EXPECTED_EXCEPTION);

        // should not have completed the controller
        assertFalse(controller.isDone());

        verify(future1, never()).cancel(anyBoolean());

        controller.delayedComplete().accept(TEXT, EXPECTED_EXCEPTION);

        verify(future1, never()).cancel(anyBoolean());
        verify(future2).cancel(anyBoolean());
    }

    @Test
    public void testDelayedRemoveRunnable() throws Exception {
        BiConsumer<String, Throwable> remover = controller.delayedRemove(runnable1);

        remover.accept(TEXT, EXPECTED_EXCEPTION);

        // should not have completed the controller
        assertFalse(controller.isDone());

        verify(runnable1, never()).run();

        controller.delayedComplete().accept(TEXT, EXPECTED_EXCEPTION);

        verify(runnable1, never()).run();
        verify(runnable2).run();
    }

    @Test
    public void testRemoveFutureOfF_testRemoveRunnable() {
        controller.remove(runnable2);
        controller.remove(future1);

        controller.cancel(true);

        verify(runnable1).run();
        verify(runnable2, never()).run();
        verify(future1, never()).cancel(anyBoolean());
        verify(future2).cancel(anyBoolean());
    }

    @Test
    public void testAddFunction() {
        AtomicReference<String> value = new AtomicReference<>();

        Function<String, CompletableFuture<String>> func = controller.add(input -> {
            value.set(input);
            return compFuture;
        });

        assertSame(compFuture, func.apply(TEXT));
        assertEquals(TEXT, value.get());

        verify(compFuture, never()).cancel(anyBoolean());

        // should not have completed the controller
        assertFalse(controller.isDone());

        // cancel - should propagate
        controller.cancel(false);

        verify(compFuture).cancel(anyBoolean());
    }

    /**
     * Tests add(Function) when the controller is not running.
     */
    @Test
    public void testAddFunctionNotRunning() {
        AtomicReference<String> value = new AtomicReference<>();

        Function<String, CompletableFuture<String>> func = controller.add(input -> {
            value.set(input);
            return compFuture;
        });

        controller.cancel(false);

        CompletableFuture<String> fut = func.apply(TEXT);
        assertNotSame(compFuture, fut);
        assertFalse(fut.isDone());

        assertNull(value.get());
    }
}
