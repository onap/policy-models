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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.onap.policy.controlloop.actorserviceprovider.Operator;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ActorParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an actor.
 */
public class ActorImpl extends StartConfigPartial<Map<String, Object>> implements Actor {
    private static final Logger logger = LoggerFactory.getLogger(ActorImpl.class);

    /**
     * Maps a name to an operator.
     */
    private final Map<String, Operator> name2operator = new ConcurrentHashMap<>();

    /**
     * Constructs the object.
     *
     * @param name actor name
     */
    public ActorImpl(String name) {
        super(name);
    }

    /**
     * This method simply returns {@code 0}.
     */
    @Override
    public int getSequenceNumber() {
        return 0;
    }

    /**
     * Adds an operator supported by this actor.
     *
     * @param operator operation to be added
     */
    protected synchronized void addOperator(Operator operator) {
        /*
         * This method is "synchronized" to prevent the state from changing while the
         * operator is added. The map, itself, does not need synchronization as it's a
         * concurrent map.
         */

        if (isConfigured()) {
            throw new IllegalStateException("attempt to set operators on a configured actor: " + getName());
        }

        name2operator.compute(operator.getName(), (opName, existingOp) -> {
            if (existingOp == null) {
                return operator;
            }

            logger.warn("duplicate names for actor operation {}.{}: {}, ignoring {}", getName(), opName,
                            existingOp.getClass().getSimpleName(), operator.getClass().getSimpleName());
            return existingOp;
        });
    }

    @Override
    public String getName() {
        return getFullName();
    }

    @Override
    public Operator getOperator(String name) {
        Operator operator = name2operator.get(name);
        if (operator == null) {
            throw new IllegalArgumentException("unknown operator " + getName() + "." + name);
        }

        return operator;
    }

    @Override
    public Collection<Operator> getOperators() {
        return name2operator.values();
    }

    @Override
    public Set<String> getOperationNames() {
        return name2operator.keySet();
    }

    /**
     * For each operation, it looks for a set of parameters by the same name and, if
     * found, configures the operation with the parameters.
     */
    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        final String actorName = getName();
        logger.info("configuring operations for actor {}", actorName);

        // function that creates operator-specific parameters, given the operation name
        Function<String, Map<String, Object>> opParamsMaker = makeOperatorParameters(parameters);

        for (Operator operator : name2operator.values()) {
            String operName = operator.getName();
            Map<String, Object> subparams = opParamsMaker.apply(operName);

            if (subparams != null) {

                try {
                    operator.configure(subparams);

                } catch (ParameterValidationRuntimeException e) {
                    logger.warn("failed to configure operation {}.{} because:\n{}", actorName, operName,
                                    e.getResult().getResult(), e);

                } catch (RuntimeException e) {
                    logger.warn("failed to configure operation {}.{}", actorName, operName, e);
                }

            } else if (operator.isConfigured()) {
                logger.warn("missing configuration parameters for operation {}.{}; using previous parameters",
                                actorName, operName);

            } else {
                logger.warn("missing configuration parameters for operation {}.{}; operation cannot be started",
                                actorName, operName);
            }
        }
    }

    /**
     * Extracts the operator parameters from the actor parameters, for a given operator.
     * This method translates the parameters to an {@link ActorParams} and then creates a function
     * that will extract operator-specific parameters.
     *
     * @param actorParameters actor parameters
     * @return a function to extract the operator parameters from the actor parameters.
     *         Note: this function may return {@code null} if there are no parameters for
     *         the given operation name
     */
    protected Function<String, Map<String, Object>> makeOperatorParameters(Map<String, Object> actorParameters) {
        String actorName = getName();

        // @formatter:off
        return Util.translate(actorName, actorParameters, ActorParams.class)
                        .doValidation(actorName)
                        .makeOperationParameters(actorName);
        // @formatter:on
    }

    /**
     * Starts each operation.
     */
    @Override
    protected void doStart() {
        final String actorName = getName();
        logger.info("starting operations for actor {}", actorName);

        for (Operator oper : name2operator.values()) {
            if (oper.isConfigured()) {
                Util.runFunction(oper::start, "failed to start operation {}.{}", actorName, oper.getName());

            } else {
                logger.warn("not starting unconfigured operation {}.{}", actorName, oper.getName());
            }
        }
    }

    /**
     * Stops each operation.
     */
    @Override
    protected void doStop() {
        final String actorName = getName();
        logger.info("stopping operations for actor {}", actorName);

        // @formatter:off
        name2operator.values().forEach(
            oper -> Util.runFunction(oper::stop, "failed to stop operation {}.{}", actorName, oper.getName()));
        // @formatter:on
    }

    /**
     * Shuts down each operation.
     */
    @Override
    protected void doShutdown() {
        final String actorName = getName();
        logger.info("shutting down operations for actor {}", actorName);

        // @formatter:off
        name2operator.values().forEach(oper -> Util.runFunction(oper::shutdown,
                        "failed to shutdown operation {}.{}", actorName, oper.getName()));
        // @formatter:on
    }

    // TODO old code: remove lines down to **HERE**

    @Override
    public String actor() {
        return null;
    }

    @Override
    public List<String> recipes() {
        return Collections.emptyList();
    }

    @Override
    public List<String> recipeTargets(String recipe) {
        return Collections.emptyList();
    }

    @Override
    public List<String> recipePayloads(String recipe) {
        return Collections.emptyList();
    }

    // **HERE**
}
