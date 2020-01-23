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

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.onap.policy.controlloop.actorserviceprovider.OperationManager;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.actorserviceprovider.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an actor.
 */
public class ActorImpl extends ConfigImpl<Map<String, Object>> implements Actor {
    private static final Logger logger = LoggerFactory.getLogger(ActorImpl.class);

    /**
     * Maps a name to an operation manager.
     */
    private Map<String, OperationManager> managers;

    /**
     * Constructs the object.
     *
     * @param name actor name
     * @param managers the operations supported by this actor
     */
    public ActorImpl(String name, OperationManager... managers) {
        super(name);
        setOperationManagers(managers);
    }

    /**
     * Sets the operation managers supported by this actor, overriding any previous list.
     *
     * @param managers the operations supported by this actor
     */
    protected void setOperationManagers(OperationManager... managers) {
        Map<String, OperationManager> map = new HashMap<>();
        for (OperationManager mgr : managers) {
            map.compute(mgr.getName(), (mgrName, curMgr) -> {
                if (curMgr == null) {
                    return mgr;
                }

                // TODO: should this throw an exception?
                logger.warn("duplicate operation manager names for actor operation {}.{}: {}, ignoring {}", getName(),
                                mgrName, curMgr.getClass().getSimpleName(), mgr.getClass().getSimpleName());
                return curMgr;
            });
        }

        this.managers = ImmutableMap.copyOf(map);
    }

    @Override
    public String getName() {
        return getFullName();
    }

    @Override
    public OperationManager getOperationManager(String name) {
        OperationManager mgr = managers.get(name);
        if (mgr == null) {
            throw new IllegalArgumentException("unknown operation " + getName() + "." + name);
        }

        return mgr;
    }

    @Override
    public Collection<OperationManager> getOperationManagers() {
        return managers.values();
    }

    @Override
    public Set<String> getOperationNames() {
        return managers.keySet();
    }

    /**
     * For each operation, it looks for a set of parameters by the same name and, if
     * found, configures the operation with the parameters.
     */
    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        Function<String, Map<String, Object>> mgrParamsMaker = makeOperationParameters(parameters);
        final String actorName = getName();

        for (OperationManager mgr : managers.values()) {
            String mgrName = mgr.getName();
            Map<String, Object> subparams = mgrParamsMaker.apply(mgrName);
            if (subparams == null) {
                logger.warn("missing configuration parameters for operation {}.{}", actorName, mgr.getName());

            } else {
                Util.logException(() -> mgr.configure(subparams),
                                "failed to configure operation " + actorName + mgr.getName());
            }
        }
    }

    /**
     * Extracts the operation manager parameters from the actor parameters, for a given
     * manager. This method assumes each operation has its own set of parameters.
     *
     * @param actorParameters actor parameters
     * @return a function to extract the operation manager parameters from the actor
     *         parameters
     */
    protected Function<String, Map<String, Object>> makeOperationParameters(Map<String, Object> actorParameters) {

        return mgrName -> Util.translateToMap(getName() + "." + mgrName, actorParameters.get(mgrName));
    }

    /**
     * Starts each operation.
     */
    @Override
    protected void doStart() {
        final String actorName = getName();

        // @formatter:off
        managers.values().forEach(
            mgr -> Util.logException(mgr::start, "failed to start operation " + actorName + mgr.getName()));
        // @formatter:on
    }

    /**
     * Stops each operation.
     */
    @Override
    protected void doStop() {
        final String actorName = getName();

        // @formatter:off
        managers.values().forEach(
            mgr -> Util.logException(mgr::stop, "failed to stop operation " + actorName + mgr.getName()));
        // @formatter:on
    }

    /**
     * Shuts down each operation.
     */
    @Override
    protected void doShutdown() {
        final String actorName = getName();

        // @formatter:off
        managers.values().forEach(mgr -> Util.logException(mgr::shutdown,
                        "failed to shutdown operation " + actorName + mgr.getName()));
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
