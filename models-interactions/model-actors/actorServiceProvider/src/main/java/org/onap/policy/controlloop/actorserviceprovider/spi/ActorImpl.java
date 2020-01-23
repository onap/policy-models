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

package org.onap.policy.controlloop.actorserviceprovider.spi;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of an actor.
 */
public class ActorImpl extends ConfigImpl<Map<String,Object>> implements Actor {
    private static final Logger logger = LoggerFactory.getLogger(ActorImpl.class);

    private State state = State.IDLE;

    /**
     * Maps a name to an operation manager.
     */
    private final Map<String, OperationManager> managers;

    /**
     * Constructs the object.
     *
     * @param name actor name
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
    public boolean isAlive() {
        return (state == State.ALIVE);
    }

    @Override
    public boolean isConfigured() {
        return (state == State.CONFIGURED);
    }

    // TODO handle exceptions from operation managers

    /**
     * This method invokes {@link OperationManager#configure(Map)} on each operation. It
     * assumes that all of the operations share the same set of parameters.
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
                continue;
            }

            mgr.configure(subparams);
        }
    }

    @Override
    protected void doStart() {
        managers.values().forEach(OperationManager::start);
    }

    @Override
    protected void doStop() {
        managers.values().forEach(OperationManager::stop);
    }

    @Override
    protected void doShutdown() {
        managers.values().forEach(OperationManager::shutdown);
    }

    @Override
    public OperationManager getOperationManager(String name) {
        // TODO should this throw an exception instead of returning null?
        return managers.get(name);
    }

    @Override
    public Collection<OperationManager> getOperationManagers() {
        return managers.values();
    }

    @Override
    public Set<String> getOperationNames() {
        return managers.keySet();
    }
}
