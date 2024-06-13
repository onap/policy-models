/*-
 * ONAP
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2024 Nordix Foundation
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

package org.onap.policy.controlloop.actor.sdnr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.onap.policy.controlloop.actor.test.BasicActor;
import org.onap.policy.controlloop.actorserviceprovider.Operator;

class SdnrActorTest extends BasicActor {

    @Test
    void testConstructor() {
        SdnrActor prov = new SdnrActor();
        assertEquals(0, prov.getSequenceNumber());

        // verify that it has the operators we expect
        var expected = Arrays.asList(SdnrOperation.NAME).stream().sorted().collect(Collectors.toList());
        var actual = prov.getOperationNames().stream().sorted().collect(Collectors.toList());

        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    void testActorService() {
        // verify that it all plugs into the ActorService
        verifyActorService(SdnrActor.NAME, "service.yaml");
    }

    @Test
    void testGetOperator() {
        SdnrActor sp = new SdnrActor();

        // should always return the same operator regardless of the name
        Operator oper = sp.getOperator("unknown");
        assertNotNull(oper);
        assertSame(oper, sp.getOperator("another"));
    }
}
