/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PipelineControllerFutureTest {
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
    private Executor executor;


    private CompletableFuture<String> compFuture;
    private PipelineControllerFuture<String> controller;


    /**
     * Initializes fields, including {@link #controller}. Adds all runners and futures to
     * the controller.
     */
    @BeforeEach
    void setUp() {
        compFuture = spy(new CompletableFuture<>());

        controller = new PipelineControllerFuture<>();

        controller.add(runnable1);
        controller.add(future1);
        controller.add(runnable2);
        controller.add(future2);
    }

    @Test
     void testCancel_testAddFutureOfFBoolean_testAddRunnable__testIsRunning() {
        assertTrue(controller.isRunning());

        assertTrue(controller.cancel(false));

        assertTrue(controller.isCancelled());
        assertFalse(controller.isRunning());

        verifyStopped();

        // re-invoke; nothing should change
        assertTrue(controller.cancel(true));

        assertTrue(controller.isCancelled());
        assertFalse(controller.isRunning());

        verifyStopped();
    }

    @Test
     void testCompleteT() throws Exception {
        assertTrue(controller.complete(TEXT));
        assertEquals(TEXT, controller.get());

        verifyStopped();

        // repeat - disallowed
        assertFalse(controller.complete(TEXT));
    }

    @Test
     void testCompleteExceptionallyThrowable() {
        assertTrue(controller.completeExceptionally(EXPECTED_EXCEPTION));
        assertThatThrownBy(() -> controller.get()).hasCause(EXPECTED_EXCEPTION);

        verifyStopped();

        // repeat - disallowed
        assertFalse(controller.completeExceptionally(EXPECTED_EXCEPTION));
    }

    @Test
     void testCompleteAsyncSupplierOfQextendsTExecutor() throws Exception {
        CompletableFuture<String> future = controller.completeAsync(() -> TEXT, executor);

        // haven't stopped anything yet
        assertFalse(future.isDone());
        verify(runnable1, never()).run();

        // get the operation and run it
        ArgumentCaptor<Runnable> captor = ArgumentCaptor.forClass(Runnable.class);
        verify(executor).execute(captor.capture());
        captor.getValue().run();

        // should be done now
        assertTrue(future.isDone());

        assertEquals(TEXT, future.get());

        verifyStopped();
    }

    /**
     * Tests completeAsync(executor) when canceled before execution.
     */
    @Test
     void testCompleteAsyncSupplierOfQextendsTExecutorCanceled() {
        CompletableFuture<String> future = controller.completeAsync(() -> TEXT, executor);

        assertTrue(future.cancel(false));

        verifyStopped();

        assertTrue(future.isDone());

        assertThatThrownBy(() -> controller.get()).isInstanceOf(CancellationException.class);
    }

    @Test
     void testCompleteAsyncSupplierOfQextendsT() throws Exception {
        CompletableFuture<String> future = controller.completeAsync(() -> TEXT);
        assertEquals(TEXT, future.get());

        verifyStopped();
    }

    /**
     * Tests completeAsync() when canceled.
     */
    @Test
     void testCompleteAsyncSupplierOfQextendsTCanceled() {
        CountDownLatch canceled = new CountDownLatch(1);

        // run async, but await until canceled
        CompletableFuture<String> future = controller.completeAsync(() -> {
            try {
                canceled.await();
            } catch (InterruptedException e) {
                // do nothing
            }

            return TEXT;
        });

        assertTrue(future.cancel(false));

        // let the future run now
        canceled.countDown();

        verifyStopped();

        assertTrue(future.isDone());

        assertThatThrownBy(() -> controller.get()).isInstanceOf(CancellationException.class);
    }

    @Test
     void testCompleteOnTimeoutTLongTimeUnit() throws Exception {
        CountDownLatch stopped = new CountDownLatch(1);
        controller.add(stopped::countDown);

        CompletableFuture<String> future = controller.completeOnTimeout(TEXT, 1, TimeUnit.MILLISECONDS);

        assertEquals(TEXT, future.get());

        /*
         * Must use latch instead of verifyStopped(), because the runnables may be
         * executed asynchronously.
         */
        assertTrue(stopped.await(5, TimeUnit.SECONDS));
    }

    /**
     * Tests completeOnTimeout() when completed before the timeout.
     */
    @Test
     void testCompleteOnTimeoutTLongTimeUnitNoTimeout() throws Exception {
        CompletableFuture<String> future = controller.completeOnTimeout("timed out", 5, TimeUnit.SECONDS);
        controller.complete(TEXT);

        assertEquals(TEXT, future.get());

        verifyStopped();
    }

    /**
     * Tests completeOnTimeout() when canceled before the timeout.
     */
    @Test
     void testCompleteOnTimeoutTLongTimeUnitCanceled() {
        CompletableFuture<String> future = controller.completeOnTimeout(TEXT, 5, TimeUnit.SECONDS);
        assertTrue(future.cancel(true));

        assertThatThrownBy(() -> controller.get()).isInstanceOf(CancellationException.class);

        verifyStopped();
    }

    @Test
     void testNewIncompleteFuture() {
        PipelineControllerFuture<String> future = controller.newIncompleteFuture();
        assertNotNull(future);
        assertTrue(future instanceof PipelineControllerFuture);
        assertNotSame(controller, future);
        assertFalse(future.isDone());
    }

    @Test
     void testDelayedComplete() throws Exception {
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
     void testDelayedCompleteWithException() {
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
     void testDelayedRemoveFutureOfF() {
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
     void testDelayedRemoveRunnable() {
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
     void testRemoveFutureOfF_testRemoveRunnable() {
        controller.remove(runnable2);
        controller.remove(future1);

        controller.cancel(true);

        verify(runnable1).run();
        verify(runnable2, never()).run();
        verify(future1, never()).cancel(anyBoolean());
        verify(future2).cancel(anyBoolean());
    }

    /**
     * Tests both wrap() methods.
     */
    @Test
     void testWrap() throws Exception {
        controller = spy(controller);

        CompletableFuture<String> future = controller.wrap(compFuture);
        verify(controller, never()).remove(compFuture);

        compFuture.complete(TEXT);
        assertEquals(TEXT, future.get());

        verify(controller).remove(compFuture);
    }

    /**
     * Tests wrap(), when the controller is not running.
     */
    @Test
     void testWrapNotRunning() {
        controller.cancel(false);
        controller = spy(controller);

        assertFalse(controller.wrap(compFuture).isDone());
        verify(controller, never()).add(compFuture);
        verify(controller, never()).remove(compFuture);

        verify(compFuture).cancel(anyBoolean());
    }

    /**
     * Tests wrap(), when the future throws an exception.
     */
    @Test
     void testWrapException() {
        controller = spy(controller);

        CompletableFuture<String> future = controller.wrap(compFuture);
        verify(controller, never()).remove(compFuture);

        compFuture.completeExceptionally(EXPECTED_EXCEPTION);
        assertThatThrownBy(() -> future.get()).hasCause(EXPECTED_EXCEPTION);

        verify(controller).remove(compFuture);
    }

    @Test
     void testWrapFunction() throws Exception {

        Function<String, CompletableFuture<String>> func = controller.wrap(input -> {
            compFuture.complete(input);
            return compFuture;
        });

        CompletableFuture<String> future = func.apply(TEXT);
        assertTrue(compFuture.isDone());

        assertEquals(TEXT, future.get());

        // should not have completed the controller
        assertFalse(controller.isDone());
    }

    /**
     * Tests wrap(Function) when the controller is canceled after the future is added.
     */
    @Test
     void testWrapFunctionCancel() {
        Function<String, CompletableFuture<String>> func = controller.wrap(input -> compFuture);

        CompletableFuture<String> future = func.apply(TEXT);
        assertFalse(future.isDone());

        assertFalse(compFuture.isDone());

        // cancel - should propagate
        controller.cancel(false);

        verify(compFuture).cancel(anyBoolean());
    }

    /**
     * Tests wrap(Function) when the controller is not running.
     */
    @Test
     void testWrapFunctionNotRunning() {
        AtomicReference<String> value = new AtomicReference<>();

        Function<String, CompletableFuture<String>> func = controller.wrap(input -> {
            value.set(input);
            return compFuture;
        });

        controller.cancel(false);

        CompletableFuture<String> fut = func.apply(TEXT);
        assertNotSame(compFuture, fut);
        assertFalse(fut.isDone());

        assertNull(value.get());
    }

    private void verifyStopped() {
        verify(runnable1).run();
        verify(runnable2).run();
        verify(future1).cancel(anyBoolean());
        verify(future2).cancel(anyBoolean());
    }
}
