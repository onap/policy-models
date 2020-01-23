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
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Function;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pipeline controller, used by operations within the pipeline to determine if they should
 * continue to run. If {@link #cancel(boolean)} is invoked, it automatically stops the
 * pipeline.
 */
@NoArgsConstructor
public class PipelineControllerFuture<T> extends CompletableFuture<T> {

    private static final Logger logger = LoggerFactory.getLogger(PipelineControllerFuture.class);

    /**
     * Tracks items added to this controller via one of the <i>add</i> methods.
     */
    private final FutureManager futures = new FutureManager();


    /**
     * Cancels and stops the pipeline, in that order.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        try {
            logger.trace("{}: cancel future", ident(this));
            return super.cancel(mayInterruptIfRunning);

        } finally {
            futures.stop();
        }
    }

    /**
     * Generates a function that, when invoked, will remove the given future. This is
     * typically added onto the end of a pipeline via one of the
     * {@link CompletableFuture#whenComplete(BiConsumer)} methods.
     *
     * @return a function that removes the given future
     */
    public <F> BiConsumer<T, Throwable> delayedRemove(Future<F> future) {
        return (value, thrown) -> {
            logger.trace("{}: remove future {}", ident(this), ident(future));
            remove(future);
        };
    }

    /**
     * Generates a function that, when invoked, will remove the given listener. This is
     * typically added onto the end of a pipeline via one of the
     * {@link CompletableFuture#whenComplete(BiConsumer)} methods.
     *
     * @return a function that removes the given listener
     */
    public BiConsumer<T, Throwable> delayedRemove(Runnable listener) {
        return (value, thrown) -> {
            logger.trace("{}: remove listener {}", ident(this), ident(listener));
            remove(listener);
        };
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
                logger.trace("{}: complete and stop future", ident(this));
                complete(value);
            } else {
                logger.trace("{}: complete exceptionally and stop future", ident(this));
                completeExceptionally(thrown);
            }

            futures.stop();
        };
    }

    /**
     * Adds a function whose return value is to be canceled when this controller is
     * stopped. Note: if the controller is already stopped, then the function will
     * <i>not</i> be executed.
     *
     * @param futureMaker function to be invoked in the future
     */
    public <F> Function<F, CompletableFuture<F>> add(Function<F, CompletableFuture<F>> futureMaker) {

        return input -> {
            if (!isRunning()) {
                logger.trace("{}: discarded new future", ident(this));
                return new CompletableFuture<>();
            }

            CompletableFuture<F> future = futureMaker.apply(input);
            add(future);

            return future;
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
}
