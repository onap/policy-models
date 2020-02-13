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

package org.onap.policy.controlloop.actorserviceprovider.impl;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;

/**
 * Executor that will run tasks until the queue is empty or a maximum number of tasks have
 * been executed. Doesn't actually run anything until {@link #runAll()} is invoked.
 */
public class MyExec implements Executor {

    // TODO move this to policy-common/utils-test

    private final int maxTasks;
    private final Queue<Runnable> commands = new LinkedList<>();

    public MyExec(int maxTasks) {
        this.maxTasks = maxTasks;
    }

    public int getQueueLength() {
        return commands.size();
    }

    @Override
    public void execute(Runnable command) {
        commands.add(command);
    }

    /**
     * Runs all tasks until the queue is empty or the maximum number of tasks have been
     * reached.
     *
     * @return {@code true} if the queue is empty, {@code false} if the maximum number of
     *         tasks have been reached before the queue was completed
     */
    public boolean runAll() {
        for (int count = 0; count < maxTasks && !commands.isEmpty(); ++count) {
            commands.remove().run();
        }

        return commands.isEmpty();
    }
}
