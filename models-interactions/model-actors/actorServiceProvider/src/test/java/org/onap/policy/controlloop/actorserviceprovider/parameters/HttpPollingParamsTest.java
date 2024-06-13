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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.controlloop.actorserviceprovider.parameters.HttpPollingParams.HttpPollingParamsBuilder;

public class HttpPollingParamsTest {
    private static final String CONTAINER = "my-container";
    private static final String CLIENT = "my-client";
    private static final String PATH = "my-path";
    private static final String POLL_PATH = "my-poll-path";
    private static final int MAX_POLLS = 3;
    private static final int POLL_WAIT_SEC = 20;
    private static final int TIMEOUT = 10;

    private HttpPollingParams params;

    @BeforeEach
    public void setUp() {
        params = HttpPollingParams.builder().pollPath(POLL_PATH).maxPolls(MAX_POLLS).pollWaitSec(POLL_WAIT_SEC)
                        .clientName(CLIENT).path(PATH).timeoutSec(TIMEOUT).build();
    }

    @Test
    public void testValidate() {
        assertTrue(params.validate(CONTAINER).isValid());

        testValidateField("pollPath", "null", bldr -> bldr.pollPath(null));
        testValidateField("maxPolls", "minimum", bldr -> bldr.maxPolls(-1));
        testValidateField("pollWaitSec", "minimum", bldr -> bldr.pollWaitSec(-1));

        // validate one of the superclass fields
        testValidateField("clientName", "null", bldr -> bldr.clientName(null));

        // check edge cases
        assertTrue(params.toBuilder().maxPolls(0).build().validate(CONTAINER).isValid());
        assertFalse(params.toBuilder().pollWaitSec(0).build().validate(CONTAINER).isValid());
        assertTrue(params.toBuilder().pollWaitSec(1).build().validate(CONTAINER).isValid());
    }

    @Test
    public void testBuilder_testToBuilder() {
        assertEquals(CLIENT, params.getClientName());

        assertEquals(POLL_PATH, params.getPollPath());
        assertEquals(MAX_POLLS, params.getMaxPolls());
        assertEquals(POLL_WAIT_SEC, params.getPollWaitSec());

        assertEquals(params, params.toBuilder().build());
    }

    private void testValidateField(String fieldName, String expected,
                    Function<HttpPollingParamsBuilder<?, ?>, HttpPollingParamsBuilder<?, ?>> makeInvalid) {

        // original params should be valid
        ValidationResult result = params.validate(CONTAINER);
        assertTrue(result.isValid(), fieldName);

        // make invalid params
        result = makeInvalid.apply(params.toBuilder()).build().validate(CONTAINER);
        assertFalse(result.isValid(), fieldName);
        assertThat(result.getResult()).contains(fieldName).contains(expected);
    }
}
