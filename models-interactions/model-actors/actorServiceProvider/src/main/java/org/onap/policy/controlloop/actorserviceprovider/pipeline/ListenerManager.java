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

import java.util.ArrayList;
import java.util.HashMap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.onap.policy.controlloop.actorserviceprovider.Util;

/**
 * Listener manager, used by operations within the pipeline to determine if they should
 * continue to run. When {@link #stop()} is called, the listeners are executed. The
 * various methods synchronize on "this" while they manipulate internal data structures.
 */
@NoArgsConstructor
public class ListenerManager {

    @Getter
    private volatile boolean running = true;

    /**
     * Listeners to be executed when {@link #stop()} is invoked.
     */
    private final HashMap<Runnable, Void> listeners = new HashMap<>(5);

    /**
     * Indicates that operations within the pipeline should stop executing.
     */
    public void stop() {
        ArrayList<Runnable> items;

        synchronized (this) {
            if (!running) {
                return;
            }

            running = false;
            items = new ArrayList<>(listeners.keySet());
            listeners.clear();
        }

        items.forEach(this::runListener);
    }

    /**
     * Adds a listener that is to be invoked when this controller is stopped. Note: if the
     * controller is already stopped, then the listener will be invoked immediately,
     * within the invoking thread.
     *
     * @param listener listener to be added
     */
    public void add(Runnable listener) {
        if (!addOnly(listener)) {
            runListener(listener);
        }
    }

    /**
     * Adds a listener that is to be invoked when this controller is stopped. Note: if the
     * controller is already stopped, then the listener will be invoked immediately,
     * within the invoking thread.
     *
     * @param listener listener to be added
     * @return {@code true} if the the listener was added, {@code false} if it could not
     *         be added because this manager has already been stopped
     */
    protected boolean addOnly(Runnable listener) {
        synchronized (this) {
            if (running) {
                listeners.put(listener, null);
                return true;
            }
        }

        return false;
    }

    /**
     * Runs a listener, catching any exceptions that it may throw.
     *
     * @param listener listener to be executed
     */
    protected void runListener(Runnable listener) {
        // TODO do this asynchronously?
        Util.runFunction(listener, "pipeline listener {} threw an exception", listener);
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
