/*-
 * SdnrActorServiceProviderTest
 * ================================================================================
 * Copyright (C) 2018 Wipro Limited Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.junit.Test;
import org.onap.policy.controlloop.actor.test.BasicActor;
import org.onap.policy.controlloop.actorserviceprovider.Operator;

public class SdnrActorServiceProviderTest extends BasicActor {

    @Test
    public void testConstructor() {
        SdnrActorServiceProvider prov = new SdnrActorServiceProvider();
        assertEquals(0, prov.getSequenceNumber());

        // verify that it has the operators we expect
        var expected = Arrays.asList(SdnrOperation.NAME).stream().sorted().collect(Collectors.toList());
        var actual = prov.getOperationNames().stream().sorted().collect(Collectors.toList());

        assertEquals(expected.toString(), actual.toString());
    }

    @Test
    public void testActorService() {
        // verify that it all plugs into the ActorService
        verifyActorService(SdnrActorServiceProvider.NAME, "service.yaml");
    }

    @Test
    public void testGetOperator() {
        SdnrActorServiceProvider sp = new SdnrActorServiceProvider();

        // should always return the same operator regardless of the name
        Operator oper = sp.getOperator("unknown");
        assertNotNull(oper);
        assertSame(oper, sp.getOperator("another"));
    }
}
