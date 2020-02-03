/*-
 * ============LICENSE_START=======================================================
 * TestActorServiceProvider
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.onap.policy.controlloop.actorserviceprovider.spi.Actor;

// TODO combine this with ActorServiceTest

public class ActorServiceProviderTest {

    private static final String DOROTHY = "Dorothy";

    @Test
    public void testActorServiceProvider() {
        ActorService actorService = ActorService.getInstance();
        assertNotNull(actorService);

        assertEquals(1, actorService.getActors().size());

        actorService = ActorService.getInstance();
        assertNotNull(actorService);

        Actor dummyActor = ActorService.getInstance().getActors().iterator().next();
        assertNotNull(dummyActor);

        assertEquals("DummyActor", dummyActor.actor());

        assertEquals(2, dummyActor.recipes().size());
        assertEquals(DOROTHY, dummyActor.recipes().get(0));
        assertEquals("Wizard", dummyActor.recipes().get(1));

        assertEquals(2, dummyActor.recipeTargets(DOROTHY).size());
        assertEquals(2, dummyActor.recipePayloads(DOROTHY).size());
    }
}
