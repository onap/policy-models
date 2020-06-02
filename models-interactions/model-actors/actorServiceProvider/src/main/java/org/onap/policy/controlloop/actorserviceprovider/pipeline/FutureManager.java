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

import java.util.HashMap;
import java.util.concurrent.Future;
import lombok.NoArgsConstructor;

/**
 * Manager that manages both futures and listeners. When {@link #stop()} is called, the
 * listeners are executed and the futures are canceled. The various methods synchronize on
 * "this" while they manipulate internal data structures.
 */
@NoArgsConstructor
public class FutureManager extends ListenerManager {

    /**
     * Maps a future to its listener. Records the {@link Runnable} that is passed to
     * {@link ListenerManager#add(Runnable)} when {@link #add(Future)} is invoked. This is
     * needed if {@link #remove(Future)} is invoked, so that the same {@link Runnable} is
     * used each time.
     */
    @SuppressWarnings("rawtypes")
    private final HashMap<Future, Runnable> future2listener = new HashMap<>(5);

    /**
     * Adds a future that is to be canceled when this controller is stopped. Note: if the
     * controller is already stopped, then the future will be canceled immediately, within
     * the invoking thread.
     *
     * @param future future to be added
     */
    public <T> void add(Future<T> future) {
        Runnable listener = () -> future.cancel(false);

        synchronized (this) {
            if (future2listener.putIfAbsent(future, listener) != null) {
                // this future is already in the map, nothing more to do
                return;
            }

            if (addOnly(listener)) {
                // successfully added
                return;
            }
        }

        runListener(listener);
    }

    /**
     * Removes a future so that it is not canceled when this controller is stopped.
     *
     * @param future future to be removed
     */
    public synchronized <T> void remove(Future<T> future) {
        Runnable listener = future2listener.remove(future);
        if (listener != null) {
            remove(listener);
        }
    }

    @Override
    public void stop() {
        super.stop();

        synchronized (this) {
            future2listener.clear();
        }
    }
}
