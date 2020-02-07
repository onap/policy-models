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

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Manager for "start" and "end" callbacks.
 */
public class CallbackManager implements Runnable {
    private final AtomicReference<Instant> startTime = new AtomicReference<>();
    private final AtomicReference<Instant> endTime = new AtomicReference<>();

    /**
     * Determines if the "start" callback can be invoked. If so, it sets the
     * {@link #startTime} to the current time.
     *
     * @return {@code true} if the "start" callback can be invoked, {@code false}
     *         otherwise
     */
    public boolean canStart() {
        return startTime.compareAndSet(null, Instant.now());
    }

    /**
     * Determines if the "end" callback can be invoked. If so, it sets the
     * {@link #endTime} to the current time.
     *
     * @return {@code true} if the "end" callback can be invoked, {@code false}
     *         otherwise
     */
    public boolean canEnd() {
        return endTime.compareAndSet(null, Instant.now());
    }

    /**
     * Gets the start time.
     *
     * @return the start time, or {@code null} if {@link #canStart()} has not been
     *         invoked yet.
     */
    public Instant getStartTime() {
        return startTime.get();
    }

    /**
     * Gets the end time.
     *
     * @return the end time, or {@code null} if {@link #canEnd()} has not been invoked
     *         yet.
     */
    public Instant getEndTime() {
        return endTime.get();
    }

    /**
     * Prevents further callbacks from being executed by setting {@link #startTime}
     * and {@link #endTime}.
     */
    @Override
    public void run() {
        canStart();
        canEnd();
    }
}
