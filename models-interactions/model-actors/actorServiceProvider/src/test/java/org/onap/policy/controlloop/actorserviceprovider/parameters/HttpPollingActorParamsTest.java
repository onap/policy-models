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

package org.onap.policy.controlloop.actorserviceprovider.parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.Util;


public class HttpPollingActorParamsTest {
    private static final String CONTAINER = "my-container";
    private static final String CLIENT = "my-client";
    private static final String POLL_PATH = "my-poll-path";
    private static final int MAX_POLLS = 3;
    private static final int POLL_WAIT_SEC = 20;
    private static final int TIMEOUT = 10;

    private static final String PATH1 = "path #1";
    private static final String PATH2 = "path #2";
    private static final String URI1 = "uri #1";
    private static final String URI2 = "uri #2";

    private Map<String, Map<String, Object>> operations;
    private HttpPollingActorParams params;

    /**
     * Initializes {@link #operations} with two items and {@link params} with a fully
     * populated object.
     */
    @BeforeEach
    public void setUp() {
        operations = new TreeMap<>();
        operations.put(PATH1, Map.of("path", URI1));
        operations.put(PATH2, Map.of("path", URI2));

        params = makeHttpPollingActorParams();
    }

    @Test
    public void testValidate() {
        assertTrue(params.validate(CONTAINER).isValid());

        // only a few fields are required
        HttpPollingActorParams sparse = Util.translate(CONTAINER, Map.of(ActorParams.OPERATIONS_FIELD, operations),
                        HttpPollingActorParams.class);
        assertTrue(sparse.validate(CONTAINER).isValid());

        testValidateField("maxPolls", "minimum", params2 -> params2.setMaxPolls(-1));
        testValidateField("pollWaitSec", "minimum", params2 -> params2.setPollWaitSec(0));

        // check fields from superclass
        testValidateField(ActorParams.OPERATIONS_FIELD, "null", params2 -> params2.setOperations(null));
        testValidateField("timeoutSec", "minimum", params2 -> params2.setTimeoutSec(-1));

        // check edge cases
        params.setMaxPolls(0);
        assertTrue(params.validate(CONTAINER).isValid());
        params.setMaxPolls(MAX_POLLS);

        params.setPollWaitSec(1);
        assertTrue(params.validate(CONTAINER).isValid());
        params.setPollWaitSec(POLL_WAIT_SEC);
    }

    private void testValidateField(String fieldName, String expected, Consumer<HttpPollingActorParams> makeInvalid) {

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(result.isValid(), fieldName);

        // make invalid params
        HttpPollingActorParams params2 = makeHttpPollingActorParams();
        makeInvalid.accept(params2);
        result = params2.validate(CONTAINER);
        assertFalse(result.isValid(), fieldName);
        assertThat(result.getResult()).contains(CONTAINER).contains(fieldName).contains(expected);
    }

    private HttpPollingActorParams makeHttpPollingActorParams() {
        HttpPollingActorParams params2 = new HttpPollingActorParams();
        params2.setClientName(CLIENT);
        params2.setTimeoutSec(TIMEOUT);
        params2.setOperations(operations);

        params2.setPollWaitSec(POLL_WAIT_SEC);
        params2.setMaxPolls(MAX_POLLS);
        params2.setPollPath(POLL_PATH);

        return params2;
    }
}
