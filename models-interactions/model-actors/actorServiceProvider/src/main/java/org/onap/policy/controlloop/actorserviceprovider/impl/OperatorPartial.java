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

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import lombok.Getter;
import org.onap.policy.controlloop.actorserviceprovider.Operator;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ControlLoopOperationParams;

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
        Thread thread = new Thread(command);
        thread.setDaemon(true);
        thread.start();
    };

    @Getter
    private final String actorName;

    @Getter
    private final String name;

    private final List<String> propertyNames;

    /**
     * Constructs the object.
     *
     * @param actorName name of the actor with which this operator is associated
     * @param name operation name
     * @param propertyNames names of properties required by this operation
     */
    public OperatorPartial(String actorName, String name, List<String> propertyNames) {
        super(actorName + "." + name);
        this.actorName = actorName;
        this.name = name;
        this.propertyNames = propertyNames;
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

    @Override
    public List<String> getPropertyNames(ControlLoopOperationParams params) {
        return propertyNames;
    }
}
