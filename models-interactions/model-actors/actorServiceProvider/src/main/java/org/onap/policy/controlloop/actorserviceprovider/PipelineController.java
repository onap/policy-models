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

package org.onap.policy.controlloop.actorserviceprovider;

import java.util.IdentityHashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Pipeline controller, used by operations within the pipeline to determine if they should
 * continue to run. Pipelines can be arranged in a hierarchical fashion so that when a
 * pipeline higher up in the hierarchy is stopped, the pipelines beneath it are stopped,
 * too.
 */
@NoArgsConstructor
public class PipelineController {

    @Getter
    private volatile boolean running = true;

    /**
     * Listeners to be executed when {@link #stop()} is invoked.
     */
    private final IdentityHashMap<Runnable, Void> listeners = new IdentityHashMap<>(3);


    /**
     * Constructs a new controller that will also be stopped if the parent controller is
     * stopped. Note: while stopping the parent controller will stop this controller, the
     * reverse is not true.
     *
     * @param parent parent controller
     */
    public PipelineController(PipelineController parent) {
        // add() will set "running" to false if the parent has already stopped
        parent.add(this::stop);
    }

    /**
     * If the pipeline is still running, invokes the specified function in a synchronized
     * fashion so that other threads are prevented from stopping the pipeline until the
     * function has returned. Note: this does <i>not</i> catch any exceptions that may be
     * thrown by the function.
     *
     * @param function function to be executed
     */
    public synchronized void doIfRunning(Runnable function) {
        if (running) {
            function.run();
        }
    }

    /**
     * Indicates that operations within the pipeline should stop executing. Once this has
     * returned, the pipeline should not invoke any further call-backs.
     */
    public synchronized void stop() {
        if (running) {
            running = false;
            listeners.keySet().forEach(this::runListener);
        }
    }

    /**
     * Adds a listener that is to be invoked when this controller is stopped. Note: if the
     * controller is already stopped, then the listener will be invoked immediately.
     *
     * @param listener listener to be added
     */
    public synchronized void add(Runnable listener) {
        if (isRunning()) {
            listeners.put(listener, null);

        } else {
            runListener(listener);
        }
    }

    /**
     * Runs a listener, catching any exceptions that it may throw.
     *
     * @param listener listener to be executed
     */
    private void runListener(Runnable listener) {
        Util.logException(listener, "pipeline listener {} threw an exception", listener);
    }

    /**
     * Removes a listener so that it is not invoked when this controller is stopped.
     *
     * @param listener listener to be removed
     */
    public synchronized void remove(Runnable listener) {
        listeners.remove(listener);
    }
}
