/*-
 * ============LICENSE_START=======================================================
 * ActorService
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import com.google.common.collect.ImmutableList;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActorService {

    private static final Logger logger = LoggerFactory.getLogger(ActorService.class);
    private static ActorService service;

    // USed to load actors
    private final ServiceLoader<Actor> loader;

    private ActorService() {
        loader = ServiceLoader.load(Actor.class);
    }

    /**
     * Get the single instance.
     *
     * @return the instance
     */
    public static synchronized ActorService getInstance() {
        if (service == null) {
            service = new ActorService();
        }
        return service;
    }

    /**
     * Get the actors.
     *
     * @return the actors
     */
    public ImmutableList<Actor> actors() {
        Iterator<Actor> iter = loader.iterator();
        logger.debug("returning actors");
        while (iter.hasNext()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Got {}", iter.next().actor());
            }
        }

        return ImmutableList.copyOf(loader.iterator());
    }
}
