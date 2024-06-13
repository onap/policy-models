/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2020, 2024 Nordix Foundation.
 * Modifications Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.cds;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.onap.policy.controlloop.actor.cds.constants.CdsActorConstants;
import org.onap.policy.controlloop.actor.test.BasicActor;
import org.onap.policy.controlloop.actorserviceprovider.Operator;

class CdsActorTest extends BasicActor {

    @Test
     void testActorService() {
        // verify that it all plugs into the ActorService
        verifyActorService(CdsActorConstants.CDS_ACTOR, "service.yaml");
    }

    @Test
     void testGetOperator() {
        CdsActor sp = new CdsActor();

        // should always return the same operator regardless of the name
        Operator oper = sp.getOperator("unknown");
        assertNotNull(oper);
        assertSame(oper, sp.getOperator("another"));
    }
}
