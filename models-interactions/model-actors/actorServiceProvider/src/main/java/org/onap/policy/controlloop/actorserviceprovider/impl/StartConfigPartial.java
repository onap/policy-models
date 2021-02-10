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

import lombok.Getter;
import org.onap.policy.common.capabilities.Configurable;
import org.onap.policy.common.capabilities.Startable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial implementation of an object that is both startable and configurable. It
 * provides the high level methods defined in the interface, while deferring the details
 * to abstract methods that must be provided by the subclasses. It also manages the
 * current {@link #state}.
 *
 * @param <T> type of parameters expected by {@link #configure(Object)}
 */
public abstract class StartConfigPartial<T> implements Startable, Configurable<T> {
    private static final Logger logger = LoggerFactory.getLogger(StartConfigPartial.class);

    @Getter
    private final String fullName;

    public enum State {
        IDLE, CONFIGURED, ALIVE
    }

    private State state = State.IDLE;

    /**
     * Constructs the object.
     *
     * @param fullName full name of this object, used for logging and exception purposes
     */
    protected StartConfigPartial(String fullName) {
        this.fullName = fullName;
    }

    @Override
    public synchronized boolean isAlive() {
        return (state == State.ALIVE);
    }

    /**
     * Determines if this object has been configured.
     *
     * @return {@code true} if this object has been configured, {@code false} otherwise
     */
    public synchronized boolean isConfigured() {
        return (state != State.IDLE);
    }

    @Override
    public synchronized void configure(T parameters) {
        if (isAlive()) {
            throw new IllegalStateException("attempt to reconfigure, but already running " + getFullName());
        }

        logger.info("initializing {}", getFullName());

        doConfigure(parameters);

        state = State.CONFIGURED;
    }

    @Override
    public synchronized boolean start() {
        switch (state) {
            case ALIVE:
                logger.info("{} is already running", getFullName());
                break;

            case CONFIGURED:
                logger.info("starting {}", getFullName());
                doStart();
                state = State.ALIVE;
                break;

            case IDLE:
            default:
                throw new IllegalStateException("attempt to start unconfigured " + getFullName());
        }

        return true;
    }

    @Override
    public synchronized boolean stop() {
        if (isAlive()) {
            logger.info("stopping {}", getFullName());
            state = State.CONFIGURED;
            doStop();

        } else {
            logger.info("{} is not running", getFullName());
        }

        return true;
    }

    @Override
    public synchronized void shutdown() {
        if (!isAlive()) {
            logger.info("{} is not running", getFullName());
            return;
        }

        logger.info("shutting down actor {}", getFullName());
        state = State.CONFIGURED;
        doShutdown();
    }

    /**
     * Configures this object.
     *
     * @param parameters configuration parameters
     */
    protected abstract void doConfigure(T parameters);

    /**
     * Starts this object.
     */
    protected abstract void doStart();

    /**
     * Stops this object.
     */
    protected abstract void doStop();

    /**
     * Shuts down this object.
     */
    protected abstract void doShutdown();
}
