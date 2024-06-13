/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2017-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019, 2023-2024 Nordix Foundation.
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

package org.onap.policy.controlloop.actor.appc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.onap.policy.controlloop.actor.test.BasicActor;

@ExtendWith(MockitoExtension.class)
 class AppcActorTest extends BasicActor {

    @Test
     void testConstructor() {
        AppcActor prov = new AppcActor();
        assertEquals(0, prov.getSequenceNumber());

        // verify that it has the operators we expect
        var expected = Stream.of(ModifyConfigOperation.NAME).toList();
        var actual = prov.getOperationNames().stream().sorted().toList();

        assertEquals(expected.toString(), actual.toString());
    }

    @Test
     void testActorService() {
        // verify that it all plugs into the ActorService
        verifyActorService(AppcActor.NAME, "service.yaml");
    }
}
