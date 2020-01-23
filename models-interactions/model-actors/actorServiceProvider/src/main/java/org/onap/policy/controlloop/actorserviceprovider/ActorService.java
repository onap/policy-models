/*-
 * ============LICENSE_START=======================================================
 * ActorService
 * ================================================================================
 * Copyright (C) 2017-2018, 2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.onap.policy.controlloop.actorserviceprovider.spi.ConfigImpl;
import org.onap.policy.controlloop.actorserviceprovider.spi.ParameterTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActorService extends ConfigImpl<Map<String, Object>> {
    private static final Logger logger = LoggerFactory.getLogger(ActorService.class);

    private final Map<String, Actor> name2actor;

    private static class LazyHolder {
        static final ActorService INSTANCE = new ActorService();
    }

    private ActorService() {
        super("actors");

        Map<String, Actor> map = new HashMap<>();
        for (Actor actor : ServiceLoader.load(Actor.class)) {
            map.compute(actor.getName(), (name, curActor) -> {
                if (curActor == null) {
                    return actor;
                }

                // TODO: should this throw an exception?
                logger.warn("duplicate actor names for {}: {}, ignoring {}", name, curActor.getClass().getSimpleName(),
                                actor.getClass().getSimpleName());
                return curActor;
            });
        }

        name2actor = ImmutableMap.copyOf(map);
    }

    /**
     * Get the single instance.
     *
     * @return the instance
     */
    public static ActorService getInstance() {
        return LazyHolder.INSTANCE;
    }

    // TODO handle exceptions from actors

    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        for (Actor actor : name2actor.values()) {
            String actorName = actor.getName();
            Map<String, Object> subparams = ParameterTranslator.translateToMap(actorName, parameters.get(actorName));
            if (subparams == null) {
                logger.warn("missing configuration parameters for actor {}", actorName);

            } else {
                actor.configure(subparams);
            }
        }
    }

    @Override
    protected void doStart() {
        for (Actor actor : name2actor.values()) {
            if (actor.isConfigured()) {
                actor.start();

            } else {
                logger.warn("not starting unconfigured actor {}", actor.getName());
            }
        }
    }

    @Override
    protected void doStop() {
        name2actor.values().forEach(Actor::stop);
    }

    @Override
    protected void doShutdown() {
        name2actor.values().forEach(Actor::shutdown);
    }

    /**
     * Gets a particular actor.
     *
     * @param name name of the actor of interest
     * @return the desired actor, or {@code null} if it does not exist
     */
    public Actor getActor(String name) {
        // TODO: should this throw an exception instead of returning null?
        return name2actor.get(name);
    }

    /**
     * Gets the actors.
     *
     * @return the actors
     */
    public Collection<Actor> getActors() {
        return name2actor.values();
    }

    /**
     * Gets the names of the actors.
     *
     * @return the actor names
     */
    public Set<String> getActorNames() {
        return name2actor.keySet();
    }
}
