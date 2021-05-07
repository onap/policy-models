/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.Map;
import java.util.concurrent.Executor;
import lombok.Getter;
import org.onap.policy.controlloop.actorserviceprovider.Operator;

/**
 * Partial implementation of an operator.
 */
public abstract class OperatorPartial extends StartConfigPartial<Map<String, Object>> implements Operator {

    /**
     * Executor to be used for tasks that may perform blocking I/O. The default executor
     * simply launches a new thread for each command that is submitted to it.
     * <p/>
     * The "get" method may be overridden by junit tests.
     */
    @Getter
    private final Executor blockingExecutor = command -> {
        var thread = new Thread(command);
        thread.setDaemon(true);
        thread.start();
    };

    @Getter
    private final String actorName;

    @Getter
    private final String name;

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     */
    protected OperatorPartial(String actorName, String name) {
        super(actorName + "." + name);
        this.actorName = actorName;
        this.name = name;
    }

    /**
     * Verifies that the operator is running.
     *
     * @throws IllegalStateException if it is not running
     */
    public void verifyRunning() {
        if (!isAlive()) {
            throw new IllegalStateException("operation is not running: " + getFullName());
        }
    }

    /**
     * This method does nothing.
     */
    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        // do nothing
    }

    /**
     * This method does nothing.
     */
    @Override
    protected void doStart() {
        // do nothing
    }

    /**
     * This method does nothing.
     */
    @Override
    protected void doStop() {
        // do nothing
    }

    /**
     * This method does nothing.
     */
    @Override
    protected void doShutdown() {
        // do nothing
    }
}
