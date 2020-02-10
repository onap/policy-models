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

package org.onap.policy.controlloop.actorserviceprovider.impl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpActorParams;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ParameterValidationRuntimeException;

public class HttpActorTest {

    private static final String ACTOR = "my-actor";
    private static final String UNKNOWN = "unknown";
    private static final String CLIENT = "my-client";
    private static final int TIMEOUT = 10;

    private HttpActor actor;

    @Before
    public void setUp() {
        actor = new HttpActor(ACTOR);
    }

    @Test
    public void testMakeOperatorParameters() {
        HttpActorParams params = new HttpActorParams();
        params.setClientName(CLIENT);
        params.setTimeoutSec(TIMEOUT);
        params.setPath(Map.of("operA", "urlA", "operB", "urlB"));

        final HttpActor prov = new HttpActor(ACTOR);
        Function<String, Map<String, Object>> maker =
                        prov.makeOperatorParameters(Util.translateToMap(prov.getName(), params));

        assertNull(maker.apply(UNKNOWN));

        // use a TreeMap to ensure the properties are sorted
        assertEquals("{clientName=my-client, path=urlA, timeoutSec=10}",
                        new TreeMap<>(maker.apply("operA")).toString());

        assertEquals("{clientName=my-client, path=urlB, timeoutSec=10}",
                        new TreeMap<>(maker.apply("operB")).toString());

        // with invalid actor parameters
        params.setClientName(null);
        assertThatThrownBy(() -> prov.makeOperatorParameters(Util.translateToMap(prov.getName(), params)))
                        .isInstanceOf(ParameterValidationRuntimeException.class);
    }

    @Test
    public void testHttpActor() {
        assertEquals(ACTOR, actor.getName());
        assertEquals(ACTOR, actor.getFullName());
    }
}
