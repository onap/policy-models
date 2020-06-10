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

package org.onap.policy.controlloop.actor.so;

import static org.assertj.core.api.Assertions.assertThat;
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


public class SoActorParamsTest {
    private static final String CONTAINER = "my-container";
    private static final String CLIENT = "my-client";
    private static final String PATH_GET = "my-path-get";
    private static final int MAX_GETS = 3;
    private static final int WAIT_SEC_GETS = 20;
    private static final int TIMEOUT = 10;

    private static final String PATH1 = "path #1";
    private static final String PATH2 = "path #2";
    private static final String URI1 = "uri #1";
    private static final String URI2 = "uri #2";

    private Map<String, Map<String, Object>> operations;
    private SoActorParams params;

    /**
     * Initializes {@link #operations} with two items and {@link params} with a fully
     * populated object.
     */
    @Before
    public void setUp() {
        operations = new TreeMap<>();
        operations.put(PATH1, Map.of("path", URI1));
        operations.put(PATH2, Map.of("path", URI2));

        params = makeSoActorParams();
    }

    @Test
    public void testValidate() {
        assertTrue(params.validate(CONTAINER).isValid());

        // only a few fields are required
        SoActorParams sparse = Util.translate(CONTAINER, Map.of(ActorParams.OPERATIONS_FIELD, operations),
                        SoActorParams.class);
        assertTrue(sparse.validate(CONTAINER).isValid());

        testValidateField("maxGets", "minimum", params2 -> params2.setMaxGets(-1));
        testValidateField("waitSecGet", "minimum", params2 -> params2.setWaitSecGet(0));

        // check fields from superclass
        testValidateField(ActorParams.OPERATIONS_FIELD, "null", params2 -> params2.setOperations(null));
        testValidateField("timeoutSec", "minimum", params2 -> params2.setTimeoutSec(-1));

        // check edge cases
        params.setMaxGets(0);
        assertTrue(params.validate(CONTAINER).isValid());
        params.setMaxGets(MAX_GETS);

        params.setWaitSecGet(1);
        assertTrue(params.validate(CONTAINER).isValid());
        params.setWaitSecGet(WAIT_SEC_GETS);
    }

    private void testValidateField(String fieldName, String expected, Consumer<SoActorParams> makeInvalid) {

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(fieldName, result.isValid());

        // make invalid params
        SoActorParams params2 = makeSoActorParams();
        makeInvalid.accept(params2);
        result = params2.validate(CONTAINER);
        assertFalse(fieldName, result.isValid());
        assertThat(result.getResult()).contains(CONTAINER).contains(fieldName).contains(expected);
    }

    private SoActorParams makeSoActorParams() {
        SoActorParams params2 = new SoActorParams();
        params2.setClientName(CLIENT);
        params2.setTimeoutSec(TIMEOUT);
        params2.setOperations(operations);

        params2.setWaitSecGet(WAIT_SEC_GETS);
        params2.setMaxGets(MAX_GETS);
        params2.setPathGet(PATH_GET);

        return params2;
    }
}
