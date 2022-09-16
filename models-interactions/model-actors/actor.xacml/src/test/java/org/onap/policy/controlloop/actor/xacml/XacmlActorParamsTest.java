/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.controlloop.actor.xacml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Util;
import org.onap.policy.controlloop.actorserviceprovider.parameters.ActorParams;

public class XacmlActorParamsTest {
    private static final String CONTAINER = "my-container";
    private static final String CLIENT = "my-client";
    private static final int TIMEOUT = 10;
    private static final String ONAP_NAME = "onap-nap";
    private static final String ONAP_COMP = "onap-component";
    private static final String ONAP_INST = "onap-instance";
    private static final String MY_ACTION = "my-action";

    private static final String PATH1 = "path #1";
    private static final String PATH2 = "path #2";
    private static final String URI1 = "uri #1";
    private static final String URI2 = "uri #2";

    private Map<String, Map<String, Object>> operations;
    private XacmlActorParams params;

    /**
     * Initializes {@link #operations} with two items and {@link params} with a fully
     * populated object.
     */
    @Before
    public void setUp() {
        operations = new TreeMap<>();
        operations.put(PATH1, Map.of("path", URI1));
        operations.put(PATH2, Map.of("path", URI2));

        params = makeXacmlActorParams();
    }

    @Test
    public void testIsDisabled() {
        // disabled by default
        assertFalse(params.isDisabled());
    }

    @Test
    public void testValidate() {
        assertTrue(params.validate(CONTAINER).isValid());

        // only a few fields are required
        XacmlActorParams sparse = Util.translate(CONTAINER, Map.of(ActorParams.OPERATIONS_FIELD, operations),
                        XacmlActorParams.class);
        assertTrue(sparse.validate(CONTAINER).isValid());

        assertEquals(XacmlActorParams.DEFAULT_ACTION, sparse.getAction());

        // check fields from superclass
        testValidateField(ActorParams.OPERATIONS_FIELD, "null", params2 -> params2.setOperations(null));
        testValidateField("timeoutSec", "minimum", params2 -> params2.setTimeoutSec(-1));
    }

    private void testValidateField(String fieldName, String expected, Consumer<XacmlActorParams> makeInvalid) {

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(fieldName, result.isValid());

        // make invalid params
        XacmlActorParams params2 = makeXacmlActorParams();
        makeInvalid.accept(params2);
        result = params2.validate(CONTAINER);
        assertFalse(fieldName, result.isValid());
        assertThat(result.getResult()).contains(CONTAINER).contains(fieldName).contains(expected);
    }

    private XacmlActorParams makeXacmlActorParams() {
        XacmlActorParams params2 = new XacmlActorParams();
        params2.setClientName(CLIENT);
        params2.setTimeoutSec(TIMEOUT);
        params2.setOperations(operations);

        params2.setOnapName(ONAP_NAME);
        params2.setOnapComponent(ONAP_COMP);
        params2.setOnapInstance(ONAP_INST);
        params2.setAction(MY_ACTION);

        return params2;
    }
}
