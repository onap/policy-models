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

package org.onap.policy.controlloop.actor.aai;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.onap.policy.controlloop.actor.test.BasicActor;

public class AaiActorTest extends BasicActor {

    @Test
    public void testAaiActor() {
        final AaiActor prov = new AaiActor();

        // verify that it has the operators we expect
        List<String> expected = new LinkedList<>();
        expected.add(AaiCustomQueryOperation.NAME);
        expected.add(AaiGetTenantOperation.NAME);
        expected.add(AaiGetPnfOperation.NAME);

        Collections.sort(expected);

        var actual = prov.getOperationNames().stream().sorted().collect(Collectors.toList());

        assertEquals(expected.toString(), actual.toString());

        // verify that it all plugs into the ActorService
        verifyActorService(AaiActor.NAME, "service.yaml");
    }
}
