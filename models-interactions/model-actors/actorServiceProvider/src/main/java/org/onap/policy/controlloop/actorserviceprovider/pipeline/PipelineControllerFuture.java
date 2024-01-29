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

import static org.onap.policy.controlloop.actorserviceprovider.Util.ident;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline controller, used by operations within the pipeline to determine if they should
 * continue to run. Whenever this is canceled or completed, it automatically cancels all
 * futures and runs all listeners that have been added.
 */
@NoArgsConstructor
public class PipelineControllerFuture<T> extends CompletableFuture<T> {

    private static final Logger logger = LoggerFactory.getLogger(PipelineControllerFuture.class);

    private static final String COMPLETE_EXCEPT_MSG = "{}: complete future with exception";
    private static final String CANCEL_MSG = "{}: cancel future";
    private static final String COMPLETE_MSG = "{}: complete future";

    /**
     * Tracks items added to this controller via one of the <i>add</i> methods.
     */
    private final FutureManager futures = new FutureManager();


    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return doAndStop(() -> super.cancel(mayInterruptIfRunning), CANCEL_MSG, ident(this));
    }

    @Override
    public boolean complete(T value) {
        return doAndStop(() -> super.complete(value), COMPLETE_MSG, ident(this));
    }

    @Override
    public boolean completeExceptionally(Throwable ex) {
        return doAndStop(() -> super.completeExceptionally(ex), COMPLETE_EXCEPT_MSG, ident(this));
    }

    @Override
    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor) {
        return super.completeAsync(() -> doAndStop(supplier, COMPLETE_MSG, ident(this)), executor);
    }

    @Override
    public CompletableFuture<T> completeAsync(Supplier<? extends T> supplier) {
        return super.completeAsync(() -> doAndStop(supplier, COMPLETE_MSG, ident(this)));
    }

    @Override
    public CompletableFuture<T> completeOnTimeout(T value, long timeout, TimeUnit unit) {
        logger.info("{}: set future timeout to {} {}", ident(this), timeout, unit);
        return super.completeOnTimeout(value, timeout, unit);
    }

    @Override
    public <U> PipelineControllerFuture<U> newIncompleteFuture() {
        return new PipelineControllerFuture<>();
    }

    /**
     * Generates a function that, when invoked, will remove the given future. This is
     * typically added onto the end of a pipeline via one of the
     * {@link CompletableFuture#whenComplete(BiConsumer)} methods.
     *
     * @return a function that removes the given future
     */
    public <F> BiConsumer<F, Throwable> delayedRemove(Future<F> future) {
        return (value, thrown) -> remove(future);
    }

    /**
     * Generates a function that, when invoked, will remove the given listener. This is
     * typically added onto the end of a pipeline via one of the
     * {@link CompletableFuture#whenComplete(BiConsumer)} methods.
     *
     * @return a function that removes the given listener
     */
    public <F> BiConsumer<F, Throwable> delayedRemove(Runnable listener) {
        return (value, thrown) -> remove(listener);
    }

    /**
     * Generates a function that, when invoked, will stop all pipeline listeners and
     * complete this future. This is typically added onto the end of a pipeline via one of
     * the {@link CompletableFuture#whenComplete(BiConsumer)} methods.
     *
     * @return a function that stops all pipeline listeners
     */
    public BiConsumer<T, Throwable> delayedComplete() {
        return (value, thrown) -> {
            if (thrown == null) {
                complete(value);
            } else {
                completeExceptionally(thrown);
            }
        };
    }

    /**
     * Adds a future to the controller and arranges for it to be removed from the
     * controller when it completes, whether it throws an exception. If the
     * controller has already been stopped, then the future is canceled and a new,
     * incomplete future is returned.
     *
     * @param future future to be wrapped
     * @return a new future
     */
    public CompletableFuture<T> wrap(CompletableFuture<T> future) {
        if (!isRunning()) {
            logger.trace("{}: not running, skipping next task {}", ident(this), ident(future));
            future.cancel(false);
            return new CompletableFuture<>();
        }

        add(future);
        return future.whenComplete(this.delayedRemove(future));
    }

    /**
     * Adds a function whose return value is to be canceled when this controller is
     * stopped. Note: if the controller is already stopped, then the function will
     * <i>not</i> be executed.
     *
     * @param futureMaker function to be invoked to create the future
     * @return a function to create the future and arrange for it to be managed by this
     *         controller
     */
    public <F> Function<F, CompletableFuture<F>> wrap(Function<F, CompletableFuture<F>> futureMaker) {

        return input -> {
            if (!isRunning()) {
                logger.trace("{}: discarded new future", ident(this));
                return new CompletableFuture<>();
            }

            CompletableFuture<F> future = futureMaker.apply(input);
            add(future);

            return future.whenComplete(delayedRemove(future));
        };
    }

    public <F> void add(Future<F> future) {
        logger.trace("{}: add future {}", ident(this), ident(future));
        futures.add(future);
    }

    public void add(Runnable listener) {
        logger.trace("{}: add listener {}", ident(this), ident(listener));
        futures.add(listener);
    }

    public boolean isRunning() {
        return futures.isRunning();
    }

    public <F> void remove(Future<F> future) {
        logger.trace("{}: remove future {}", ident(this), ident(future));
        futures.remove(future);
    }

    public void remove(Runnable listener) {
        logger.trace("{}: remove listener {}", ident(this), ident(listener));
        futures.remove(listener);
    }

    /**
     * Performs an operation, stops the futures, and returns the value from the operation.
     * Logs a message using the given arguments.
     *
     *
     * @param <R> type of value to be returned
     * @param supplier operation to perform
     * @param message message to be logged
     * @param args message arguments to fill "{}" place-holders
     * @return the operation's result
     */
    private <R> R doAndStop(Supplier<R> supplier, String message, Object... args) {
        try {
            logger.trace(message, args);
            return supplier.get();

        } finally {
            logger.trace("{}: stopping this future", ident(this));
            futures.stop();
        }
    }
}
