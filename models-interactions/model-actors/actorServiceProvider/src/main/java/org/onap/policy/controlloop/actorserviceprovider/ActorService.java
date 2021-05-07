/*-
 * ============LICENSE_START=======================================================
 * ActorService
 * ================================================================================
 * Copyright (C) 2017-2018, 2020-2021 AT&T Intellectual Property. All rights reserved.
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Set;
import org.onap.policy.controlloop.actorserviceprovider.impl.StartConfigPartial;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service that manages a set of actors. To use the service, first invoke
 * {@link #configure(Map)} to configure all of the actors, and then invoke
 * {@link #start()} to start all of the actors. When finished using the actor service,
 * invoke {@link #stop()} or {@link #shutdown()}.
 */
public class ActorService extends StartConfigPartial<Map<String, Object>> {
    private static final Logger logger = LoggerFactory.getLogger(ActorService.class);

    private final Map<String, Actor> name2actor;

    /**
     * Constructs the object and loads the list of actors.
     */
    public ActorService() {
        super("actors");

        Map<String, Actor> map = new HashMap<>();

        for (Actor newActor : buildList()) {
            map.compute(newActor.getName(), (name, existingActor) -> {
                if (existingActor == null) {
                    return newActor;
                }

                logger.warn("duplicate actor names for {}: {}, ignoring {}", name,
                                existingActor.getClass().getSimpleName(), newActor.getClass().getSimpleName());
                return existingActor;
            });
        }

        name2actor = ImmutableMap.copyOf(map);
    }

    /**
     * Builds the list of actors, discarding those that cannot be constructed.
     *
     * @return the list of actors, sorted by ascending sequence number
     */
    private List<Actor> buildList() {
        List<Actor> actors = new LinkedList<>();

        Iterator<Actor> iter = loadActors().iterator();
        while (iter.hasNext()) {
            try {
                actors.add(iter.next());
            } catch (ServiceConfigurationError e) {
                logger.warn("unable to load actor", e);
            }
        }

        actors.sort((actor1, actor2) -> {
            var cmp = Integer.compare(actor1.getSequenceNumber(), actor2.getSequenceNumber());
            if (cmp != 0) {
                return cmp;
            }

            return actor1.getClass().getName().compareTo(actor2.getClass().getName());
        });

        return actors;
    }

    /**
     * Gets a particular actor.
     *
     * @param name name of the actor of interest
     * @return the desired actor
     * @throws IllegalArgumentException if no actor by the given name exists
     */
    public Actor getActor(String name) {
        var actor = name2actor.get(name);
        if (actor == null) {
            throw new IllegalArgumentException("unknown actor " + name);
        }

        return actor;
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

    @Override
    protected void doConfigure(Map<String, Object> parameters) {
        logger.info("configuring actors");

        for (Actor actor : name2actor.values()) {
            String actorName = actor.getName();
            Object paramValue = parameters.get(actorName);

            if (paramValue instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> subparams = (Map<String, Object>) paramValue;

                try {
                    actor.configure(subparams);

                } catch (ParameterValidationRuntimeException e) {
                    logger.warn("failed to configure actor {} because:\n{}", actorName, e.getResult().getResult(), e);

                } catch (RuntimeException e) {
                    logger.warn("failed to configure actor {}", actorName, e);
                }

            } else if (actor.isConfigured()) {
                logger.warn("missing configuration parameters for actor {}; using previous parameters", actorName);

            } else {
                logger.warn("missing configuration parameters for actor {}; actor cannot be started", actorName);
            }
        }
    }

    @Override
    protected void doStart() {
        logger.info("starting actors");

        for (Actor actor : name2actor.values()) {
            if (actor.isConfigured()) {
                Util.runFunction(actor::start, "failed to start actor {}", actor.getName());

            } else {
                logger.warn("not starting unconfigured actor {}", actor.getName());
            }
        }
    }

    @Override
    protected void doStop() {
        logger.info("stopping actors");
        name2actor.values().forEach(actor -> Util.runFunction(actor::stop, "failed to stop actor {}", actor.getName()));
    }

    @Override
    protected void doShutdown() {
        logger.info("shutting down actors");

        // @formatter:off
        name2actor.values().forEach(
            actor -> Util.runFunction(actor::shutdown, "failed to shutdown actor {}", actor.getName()));

        // @formatter:on
    }

    // the following methods may be overridden by junit tests

    protected Iterable<Actor> loadActors() {
        return ServiceLoader.load(Actor.class);
    }
}
