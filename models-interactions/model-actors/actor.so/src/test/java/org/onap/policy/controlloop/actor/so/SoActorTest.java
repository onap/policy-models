/*
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * Modifications Copyright (C) 2018-2020 AT&T. All rights reserved.
 * Modifications Copyright (C) 2019, 2024 Nordix Foundation.
 * Modifications Copyright (C) 2020 Wipro Limited.
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

package org.onap.policy.controlloop.actor.so;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.onap.policy.controlloop.actor.test.BasicActor;

class SoActorTest extends BasicActor {

    @Test
    void testConstructor() {
        SoActor prov = new SoActor();

        // verify that it has the operators we expect
        var expected = Arrays.asList(VfModuleCreate.NAME, VfModuleDelete.NAME, ModifyNssi.NAME, ModifyCll.NAME).stream()
            .sorted().collect(Collectors.toList());
        var actual = prov.getOperationNames().stream().sorted().collect(Collectors.toList());

        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    void testActorService() {
        // verify that it all plugs into the ActorService
        verifyActorService(SoActor.NAME, "service.yaml");
    }
}
