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
import org.onap.policy.controlloop.actorserviceprovider.OperationManager;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.actorserviceprovider.util.ParameterTranslator;
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
    private final Map<String, OperationManager> managers;

    /**
     * Constructs the object.
     *
     * @param name actor name
     * @param managers the operations supported by this actor
     */
    public ActorImpl(String name, OperationManager... managers) {
        super(name);

        Map<String, OperationManager> map = new HashMap<>();
        for (OperationManager mgr : managers) {
            map.compute(mgr.getName(), (mgrName, curMgr) -> {
                if (curMgr == null) {
                    return mgr;
                }

                // TODO: should this throw an exception?
                logger.warn("duplicate operation manager names for actor {} operation {}: {}, ignoring {}", getName(),
                                name, curMgr.getClass().getSimpleName(), mgr.getClass().getSimpleName());
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

    // TODO handle exceptions from operation managers

    /**
     * For each operation, it looks for a set of parameters by the same name and, if
     * found, configures the operation with the parameters.
     */
    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        String actorName = getName();

        for (OperationManager mgr : managers.values()) {
            String mgrName = mgr.getName();
            Map<String, Object> subparams =
                            ParameterTranslator.translateToMap(actorName + "." + mgrName, parameters.get(mgrName));
            if (subparams == null) {
                logger.warn("missing configuration parameters for operation {}.{}", getName(), mgr.getName());

            } else {
                mgr.configure(subparams);
            }
        }
    }

    /**
     * Starts each operation.
     */
    @Override
    protected void doStart() {
        managers.values().forEach(OperationManager::start);
    }

    /**
     * Stops each operation.
     */
    @Override
    protected void doStop() {
        managers.values().forEach(OperationManager::stop);
    }

    /**
     * Shuts down each operation.
     */
    @Override
    protected void doShutdown() {
        managers.values().forEach(OperationManager::shutdown);
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
